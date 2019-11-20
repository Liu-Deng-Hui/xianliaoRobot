package com.xianliaoRobot.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.xianliaoRobot.MyApplication;
import com.xianliaoRobot.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity {
    private String url = "http:/robot.zzlzd.com/api/";
    private String user;
    private String pwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SharedPreferences sharedPre=MyApplication.getContext().getSharedPreferences("config", MODE_PRIVATE);
        int login = sharedPre.getInt("login",-1);
        if(login==0){
            Intent intent=new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClass(MainActivity.this,RobotActivity.class);
            startActivity(intent);
        }
        if (ActivityCompat.checkSelfPermission(MyApplication.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((MainActivity) MyApplication.getContext(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
    }

    public void login(View view) throws JSONException {
        EditText editText1 = (EditText) findViewById(R.id.login_user_edit);
        user = editText1.getText().toString().trim();
        EditText editText2 = (EditText) findViewById(R.id.login_passwd_edit);
        pwd = editText2.getText().toString().trim();
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
                                editor.putString("robot",info.getString("robot"));
                                editor.putInt("login",0);
                                //提交
                                editor.commit();
                                Toast.makeText(MainActivity.this,"登录成功！",Toast.LENGTH_LONG).show();
                                Intent intent=new Intent();
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setClass(MainActivity.this,RobotActivity.class);
                                startActivity(intent);

                            }else{
                                Toast.makeText(MainActivity.this, info.getString("msg"), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this,"登录失败！",Toast.LENGTH_LONG).show();
                        }

                    }
                    @Override
                    public void onError(Response<String> response) {
                        Toast.makeText(MainActivity.this,"登录失败，请检查网络！",Toast.LENGTH_LONG).show();
                        super.onError(response);
                    }
                });
    }
}
