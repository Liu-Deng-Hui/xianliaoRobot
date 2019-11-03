package com.xianliaoRobot.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadTask;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.model.Response;
import com.xianliaoRobot.MyApplication;
import com.xianliaoRobot.R;
import com.xianliaoRobot.entity.data;
import com.xianliaoRobot.permission.DialogHelper;
import com.xianliaoRobot.permission.PermissionConstants;
import com.xianliaoRobot.permission.PermissionUtils;
import com.xianliaoRobot.service.MyMqttService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private String TAG = this.getClass().getSimpleName();
    private Intent mIntent;
    private String url = "http://h2.frp.fztool.com/api/";
    private String user;
    private String pwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        requestPermission();
        verifyStoragePermissions(this);
        SharedPreferences sharedPre=getSharedPreferences("config", MODE_PRIVATE);
        int login = sharedPre.getInt("login",-1);
        if(login==0){
            Intent intent=new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClass(MainActivity.this,RobotActivity.class);
            startActivity(intent);
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    public void login(View view) throws JSONException {
        EditText editText1 = (EditText) findViewById(R.id.login_user_edit);
        user = editText1.getText().toString();
        EditText editText2 = (EditText) findViewById(R.id.login_passwd_edit);
        pwd = editText2.getText().toString();
        if (user.equals("") || pwd == null) {
            Toast.makeText(MainActivity.this, "账号不能为空，请填写账号！", Toast.LENGTH_LONG).show();
        }
        if (pwd.equals("") || pwd == null) {
            Toast.makeText(MainActivity.this, "密码不能为空，请填写密码！", Toast.LENGTH_LONG).show();
        }
        TelephonyManager tm = (TelephonyManager) MyApplication.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(MyApplication.getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        String imei = tm.getDeviceId();
        HashMap params = new HashMap();
        params.put("user", user);
        params.put("pwd", pwd);
        params.put("imei", imei);
        JSONObject json = new JSONObject(params);
        OkGo.<String>post(url+"login")
                .tag(this)
                .upJson(json)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject info = new JSONObject(response.body().toString());
                            if(info.getInt("code")==0){
                                SharedPreferences sharedPre=MyApplication.getContext().getSharedPreferences("config", MyApplication.getContext().MODE_PRIVATE);
                                SharedPreferences.Editor editor=sharedPre.edit();
                                editor.putString("user",user);
                                editor.putString("pwd",pwd);
                                editor.putInt("login",0);
                                //提交
                                editor.commit();
                                Toast.makeText(MainActivity.this, "登录成功！", Toast.LENGTH_LONG).show();
                                Intent intent=new Intent();
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setClass(MainActivity.this,RobotActivity.class);
                                startActivity(intent);

                            }else{
                                Toast.makeText(MainActivity.this, info.getString("msg"), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "登录失败！", Toast.LENGTH_LONG).show();
                        }

                    }
                    @Override
                    public void onError(Response<String> response) {
                        Toast.makeText(MainActivity.this, "登录失败，请检查网络！", Toast.LENGTH_LONG).show();
                        super.onError(response);
                    }
                });
    }
    public boolean checkPackage(String packageName)
    {
        if (packageName == null || "".equals(packageName))
            return false;
        try
        {
            this.getPackageManager().getApplicationInfo(packageName, PackageManager
                    .GET_UNINSTALLED_PACKAGES);
            return true;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            return false;
        }
    }
    /**
     * 申请权限
     */
    public void requestPermission() {
        PermissionUtils.permission(PermissionConstants.PHONE)
                .rationale(new PermissionUtils.OnRationaleListener() {
                    @Override
                    public void rationale(final ShouldRequest shouldRequest) {
                        Log.d(TAG, "onDenied: 权限被拒绝后弹框提示");
                        DialogHelper.showRationaleDialog(shouldRequest, MainActivity.this);
                    }
                })
                .callback(new PermissionUtils.FullCallback() {
                    @Override
                    public void onGranted(List<String> permissionsGranted) {
                        mIntent = new Intent(MainActivity.this, MyMqttService.class);
                        //开启服务
                        startService(mIntent);
                    }

                    @Override
                    public void onDenied(List<String> permissionsDeniedForever,
                                         List<String> permissionsDenied) {
                        Log.d(TAG, "onDenied: 权限被拒绝");
                        if (!permissionsDeniedForever.isEmpty()) {
                            DialogHelper.showOpenAppSettingDialog(MainActivity.this);
                        }
                    }
                })
                .request();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //停止服务
        stopService(mIntent);
    }
    public static void verifyStoragePermissions(Activity activity) {
          // Check if we have write permission
          int permission = ActivityCompat.checkSelfPermission(activity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE);
          if (permission != PackageManager.PERMISSION_GRANTED) {
                  // We don't have permission so prompt the user
                  ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
              }
      }
}
