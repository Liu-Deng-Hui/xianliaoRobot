package com.xianliaoRobot.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadTask;
import com.xianliaoRobot.MyApplication;
import com.xianliaoRobot.R;
import com.xianliaoRobot.entity.data;
import com.xianliaoRobot.permission.DialogHelper;
import com.xianliaoRobot.permission.PermissionConstants;
import com.xianliaoRobot.permission.PermissionUtils;
import com.xianliaoRobot.service.MyMqttService;

import java.io.File;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE };
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
        final Button btn = findViewById(R.id.start);
        final Button XiaLiao =findViewById(R.id.startXianLiao);
        //开启服务
       // startService(new Intent(MainActivity.this, MyMqttService.class));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //打开系统设置中辅助功能
                Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
                Toast.makeText(MainActivity.this, "找到闲聊机器人服务，开启即可", Toast.LENGTH_LONG).show();
            }
        });
        XiaLiao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toXiaLiao();
            }
        });
    }
    public void publish(View view) {
        //模拟闸机设备发送消息过来
       // MyMqttService.publish("tourist enter");
        /*new Thread(connectNet).start();
        new Thread(saveFileRunnable).start();*/



        File file = new File(Environment.getExternalStorageDirectory() + "/DCIM/100ANDRO/"+data.getTitle());
        // 最后通知图库更新
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // 判断SDK版本是不是4.4或者高于4.4
            String[] paths = new String[]{file.getAbsolutePath()};
            MediaScannerConnection.scanFile(MyApplication.getContext(), paths, null, null);
        } else {
            final Intent intent;
            if (file.isDirectory()) {
                intent = new Intent(Intent.ACTION_MEDIA_MOUNTED);
                intent.setClassName("com.android.providers.media", "com.android.providers.media.MediaScannerReceiver");
                intent.setData(Uri.fromFile(Environment.getExternalStorageDirectory()));
            } else {
                intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(file));
            }
            MyApplication.getContext().sendBroadcast(intent);
        }

    }

    @Download.onTaskComplete void taskComplete(DownloadTask task) {
        //在这里处理任务完成的状态
        Log.i(TAG, "下载完成 ：" +Environment.getExternalStorageDirectory());
    }
    void toXiaLiao(){
        if (!checkPackage("org.xianliao")){
            Toast.makeText(MainActivity.this, "请先安装指定应用！", Toast.LENGTH_LONG).show();
        }
        Intent intent = new Intent();
        intent.setAction("Android.intent.action.VIEW");
        intent.setClassName("org.xianliao", "org.sugram.base.LaunchActivity");
        startActivity(intent);
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
