package com.xianliaoRobot.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadTask;
import com.arialyy.aria.util.CommonUtil;
import com.xianliaoRobot.MyApplication;
import com.xianliaoRobot.R;
import com.xianliaoRobot.entity.data;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Author       wildma
 * Github       https://github.com/wildma
 * CreateDate   2018/11/08
 * Desc	        ${MQTT服务}
 */

public class MyMqttService extends Service {
    String oleImgTitle="";
    public final   String             TAG            = MyMqttService.class.getSimpleName();
    private static MqttAndroidClient  mqttAndroidClient;
    private        MqttConnectOptions mMqttConnectOptions;
    public         String             HOST           = "tcp://47.97.187.27:1883";//服务器地址（协议+地址+端口号）
    public         String             USERNAME       = "admin";//用户名
    public         String             PASSWORD       = "password";//密码
    public static  String             PUBLISH_TOPIC  = "tourist_enter";//发布主题
    public static  String             RESPONSE_TOPIC = "message_arrived";//响应主题
    @SuppressLint("MissingPermission")
    public         String             CLIENTID       = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            ? Build.getSerial() : Build.SERIAL;//客户端ID，一般以客户端唯一标识符表示，这里用设备序列号表示

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        init();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 发布 （模拟其他客户端发布消息）
     *
     * @param message 消息
     */
    public void publish(String message){
        SharedPreferences sharedPre=getSharedPreferences("config", MODE_PRIVATE);
        try {
            JSONObject mqtt = new JSONObject(sharedPre.getString("robot",""));
            String topic = mqtt.getString("mqpublic");
            Integer qos = 2;
            Boolean retained = false;
            try {
                //参数分别为：主题、消息的字节数组、服务质量、是否在服务器保留断开连接后的最后一条消息
                mqttAndroidClient.publish(topic, message.getBytes(), qos.intValue(), retained.booleanValue());
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*
    *
    *  SharedPreferences sharedPre=getSharedPreferences("config", MODE_PRIVATE);
        try {
            JSONObject mqtt = new JSONObject(sharedPre.getString("robot",""));
            String topic = mqtt.getString("mqpublic");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    * */
    /**
     * 响应 （收到其他客户端的消息后，响应给对方告知消息已到达或者消息有问题等）
     *
     * @param message 消息
     */
    public void response(String message) {
        SharedPreferences sharedPre=getSharedPreferences("config", MODE_PRIVATE);
        try {
            JSONObject mqtt = new JSONObject(sharedPre.getString("robot",""));
            String topic = mqtt.getString("mqpublic");
            Integer qos = 2;
            Boolean retained = false;
            try {
                //参数分别为：主题、消息的字节数组、服务质量、是否在服务器保留断开连接后的最后一条消息
                mqttAndroidClient.publish(topic, message.getBytes(), qos.intValue(), retained.booleanValue());
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化
     */
    private void init() {
        Aria.download(this).register();
        SharedPreferences sharedPre=getSharedPreferences("config", MODE_PRIVATE);
        try {
            JSONObject mqtt = new JSONObject(sharedPre.getString("robot",""));
            String serverURI = HOST; //服务器地址（协议+地址+端口号）
            //CLIENTID
            mqttAndroidClient = new MqttAndroidClient(this, serverURI,mqtt.getString("mquser"));
            mqttAndroidClient.setCallback(mqttCallback); //设置监听订阅消息的回调
            mMqttConnectOptions = new MqttConnectOptions();
            mMqttConnectOptions.setCleanSession(true); //设置是否清除缓存
            mMqttConnectOptions.setConnectionTimeout(10); //设置超时时间，单位：秒
            mMqttConnectOptions.setKeepAliveInterval(20); //设置心跳包发送间隔，单位：秒
            mMqttConnectOptions.setUserName(mqtt.getString("mquser")); //设置用户名
            mMqttConnectOptions.setPassword(mqtt.getString("mqpwd").toCharArray()); //设置密码

            // last will message
            boolean doConnect = true;
            String message = "{\"terminal_uid\":\"" + CLIENTID + "\"}";
            String topic = mqtt.getString("mqpublic");
            Integer qos = 2;
            Boolean retained = false;
            if ((!message.equals("")) || (!topic.equals(""))) {
                // 最后的遗嘱
                try {
                    mMqttConnectOptions.setWill(topic, message.getBytes(), qos.intValue(), retained.booleanValue());
                } catch (Exception e) {
                    Log.i(TAG, "Exception Occured", e);
                    doConnect = false;
                    iMqttActionListener.onFailure(null, e);
                }
            }
            if (doConnect) {
                doClientConnection();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 连接MQTT服务器
     */
    private void doClientConnection() {
        if (!mqttAndroidClient.isConnected() && isConnectIsNomarl()) {
            try {
                mqttAndroidClient.connect(mMqttConnectOptions, null, iMqttActionListener);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 判断网络是否连接
     */
    private boolean isConnectIsNomarl() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            String name = info.getTypeName();
            Log.i(TAG, "当前网络名称：" + name);
            return true;
        } else {
            Log.i(TAG, "没有可用网络");
            /*没有可用网络的时候，延迟3秒再尝试重连*/
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doClientConnection();
                }
            }, 3000);
            return false;
        }
    }

    //MQTT是否连接成功的监听
    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken arg0) {
            Log.i(TAG, "连接成功 ");
            SharedPreferences sharedPre=getSharedPreferences("config", MODE_PRIVATE);
            try {
                JSONObject mqtt = new JSONObject(sharedPre.getString("robot",""));
                try {
                    mqttAndroidClient.subscribe(mqtt.getString("mqpublic"), 2);//订阅主题，参数：主题、服务质量
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            arg1.printStackTrace();
            Log.i(TAG, "连接失败 ");
            doClientConnection();//连接失败，重连（可关闭服务器进行模拟）
        }
    };

    //订阅主题的回调
    private MqttCallback mqttCallback = new MqttCallback() {
        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            Log.i(TAG, "收到消息： " + new String(message.getPayload()));
            //业务逻辑开始
            String msg = new String(message.getPayload());
            oleImgTitle = data.getTitle();
            JSONObject result = new JSONObject(msg);
            data.setId(result.getInt("id"));
            data.setTitle(result.getString("title"));
            data.setType(result.getInt("type"));
            data.setText(result.getString("text"));
            data.setChat(result.getString("chat"));
            if(data.getType()!=null){
                if(data.getType()==0){
                    sendMsg();
                }
                if(data.getType()==1){
                    Aria.download(this)
                            .load(data.getText())     //读取下载地址
                            .setFilePath(Environment.getExternalStorageDirectory() + "/DCIM/100ANDRO/"+data.getTitle()) //设置文件保存的完整路径
                            .start();   //启动下载
                }
            }
            //收到其他客户端的消息后，响应给对方告知消息已到达或者消息有问题等
            response("message arrived");
        }
        @Download.onTaskStart void taskStart(DownloadTask task) {
            Log.e(TAG, "----------------------------------------------- ");
        }
        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {

        }

        @Override
        public void connectionLost(Throwable arg0) {
            Log.i(TAG, "连接断开 ");
            doClientConnection();//连接断开，重连
        }
    };

    @Override
    public void onDestroy() {
        try {
            mqttAndroidClient.disconnect(); //断开连接
        } catch (MqttException e) {
            e.printStackTrace();
        }
        super.onDestroy();
        Aria.download(this).unRegister();
    }
    public void sendMsg(){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "home")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(data.getTitle())
                .setContentText(data.getText())
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(data.getId(), mBuilder.build());
    }
    /** 删除单个文件
     * @param filePath$Name 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    private boolean deleteSingleFile(String filePath$Name) {
        File file = new File(filePath$Name);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                Log.e("--Method--", "Copy_Delete.deleteSingleFile: 删除单个文件" + filePath$Name + "成功！");
                return true;
            } else {
                Log.e("--Method--", "Copy_Delete.deleteSingleFile: 删除单个文件" + filePath$Name + "失败！");
                return false;
            }
        } else {
            Log.e("--Method--", "Copy_Delete.deleteSingleFile: 删除单个文件失败：" + filePath$Name + "不存在！");
            return false;
        }
    }
    @Download.onTaskComplete void taskComplete(DownloadTask task) {
        Log.e(data.getTitle(),"下载完成");
        //读取上次文件
        SharedPreferences sharedPre=getSharedPreferences("image", MODE_PRIVATE);
       String oldimg =  sharedPre.getString("oldimgtitle","");
       if(!data.getTitle().equals(oldimg)&&!oldimg.isEmpty()){
           deleteSingleFile(Environment.getExternalStorageDirectory() + "/DCIM/100ANDRO/"+oldimg);
       }
        //储存现在文件
        SharedPreferences newsharedPre=MyApplication.getContext().getSharedPreferences("image", MyApplication.getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor=newsharedPre.edit();
        editor.putString("oldimgtitle",data.getTitle());
        editor.commit();
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
        sendMsg();
    }
}
