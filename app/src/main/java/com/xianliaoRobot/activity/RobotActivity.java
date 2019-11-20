package com.xianliaoRobot.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.xianliaoRobot.MyApplication;
import com.xianliaoRobot.R;
import com.xianliaoRobot.permission.DialogHelper;
import com.xianliaoRobot.permission.PermissionConstants;
import com.xianliaoRobot.permission.PermissionUtils;
import com.xianliaoRobot.service.MyMqttService;

import java.util.List;


public class RobotActivity extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private String TAG = this.getClass().getSimpleName();
    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
        verifyStoragePermissions(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    /**
     * 开始设置
     * @param view
     */
    public void seting(View view){
        //打开系统设置中辅助功能
        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
        Toast.makeText(this,"找到闲聊机器人服务，开启即可",Toast.LENGTH_LONG).show();
    }

    /***
     * 跳转开始进行辅助
     * @param v
     */
    public void go(View v) {
        if (!checkPackage("org.xianliao")){
            Toast.makeText(this,"请先安装指定应用！",Toast.LENGTH_LONG).show();
        }
        Intent intent = new Intent();
        intent.setAction("Android.intent.action.VIEW");
        intent.setClassName("org.xianliao", "org.sugram.base.LaunchActivity");
        startActivity(intent);
    }
    /***
     * 清除账号信息
     * @param view
     */
    public void clear(View view){
        //清空账号信息
        SharedPreferences preferences = MyApplication.getContext().getSharedPreferences("config", MyApplication.getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
        Toast.makeText(this,"账号信息已清空！",Toast.LENGTH_LONG).show();
        //停止服务
        Intent MyMqtt = new Intent(RobotActivity.this, MyMqttService.class);
        startService(MyMqtt);
        //跳转新页面
        Intent intent = new Intent();
        intent.setAction("Android.intent.action.VIEW");
        intent.setClassName("com.xianliaoRobot", "com.xianliaoRobot.activity.MainActivity");
        startActivity(intent);
    }

    /***
     * 检测有没有闲聊app
     * @param packageName
     * @return
     */
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
                        DialogHelper.showRationaleDialog(shouldRequest, RobotActivity.this);
                    }
                })
                .callback(new PermissionUtils.FullCallback() {
                    //权限申请成功后开启mqtt服务
                    @Override
                    public void onGranted(List<String> permissionsGranted) {
                        mIntent = new Intent(RobotActivity.this, MyMqttService.class);
                        startService(mIntent);
                    }

                    @Override
                    public void onDenied(List<String> permissionsDeniedForever,
                                         List<String> permissionsDenied) {
                        Log.d(TAG, "onDenied: 权限被拒绝");
                        if (!permissionsDeniedForever.isEmpty()) {
                            DialogHelper.showOpenAppSettingDialog(RobotActivity.this);
                        }
                    }
                })
                .request();
    }

    /***
     * 停止服务
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(mIntent);
    }

    /***
     * 相关权限
     * @param activity
     */
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
