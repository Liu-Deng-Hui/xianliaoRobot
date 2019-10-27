package com.xianliaoRobot.service;

import android.accessibilityservice.AccessibilityService;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.xianliaoRobot.entity.data;

import java.io.File;
import java.util.List;

import static android.content.ContentValues.TAG;

public class XianLiaoService extends AccessibilityService {
    private Integer liaoTian  = 0;
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();
       printEventLog(event);
        if(eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED){
            Log.d(TAG, "检测到通知信息");
            Log.d(TAG, "聊天标志位："+liaoTian);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancelAll();
            //进入群聊聊天界面
            if(liaoTian==1){
                Log.d(TAG, "当前页面为闲聊聊天窗口");
                //拿到根节点
                AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
                if (nodeInfo == null) {
                    return;
                }
                if(data.getType()!=null){
                    if(data.getType()==0){
                        //输入指定文字
                        openChatSend(nodeInfo);
                        openOnclickSend(nodeInfo);
                    }
                    if(data.getType()==1){
                        openChatImgSend(nodeInfo);//点击+
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        openChatClictImg(nodeInfo);//点击图片
                    }
                }
            }
        }
        if(eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            //判断是否是登录后的指导界面
            if ("org.sugram.dao.common.GuideActivity".equals(event.getClassName())) {
                liaoTian =0;
                Log.d(TAG, "登录后首次启动界面");
                //拿到根节点
                AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
                if (nodeInfo == null) {
                    return;
                }
                List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("跳过");
                if (list.isEmpty()) {
                    return;
                }
                for (AccessibilityNodeInfo info : list) {
                    AccessibilityNodeInfo parent = info.getParent();
                    Log.d(TAG, "进行跳过");
                    if (parent != null) {
                        info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        break;
                    }
                }
            }
            //登录页面
            if ("org.sugram.dao.login.LoginActivity".equals(event.getClassName())) {
                    liaoTian =0;
                    Log.d(TAG, "登录页面");
                    //拿到根节点
                    AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
                    if (nodeInfo == null) {
                        return;
                    }
                    //查找账号
                    List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("org.xianliao:id/et_login_password_phone");
                    if (list.isEmpty()) {
                        return;
                    }
                    for (AccessibilityNodeInfo info : list) {
                        AccessibilityNodeInfo parent = info.getParent();
                        Log.d(TAG, "输入账号");
                        if (parent != null) {
                            ClipboardManager clipboard = (ClipboardManager)this.getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("text", "15225687370");
                            clipboard.setPrimaryClip(clip);
                            //焦点（n是AccessibilityNodeInfo对象）
                            info.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                            ////粘贴进入内容
                            info.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                            break;
                        }
                    }
                    //查找密码按钮
                    list = nodeInfo.findAccessibilityNodeInfosByViewId("org.xianliao:id/et_login_password_code");
                    if (list.isEmpty()) {
                        return;
                    }
                    for (AccessibilityNodeInfo info : list) {
                        AccessibilityNodeInfo parent = info.getParent();
                        Log.d(TAG, "输入密码");
                            if (parent != null) {
                                ClipboardManager clipboard = (ClipboardManager)this.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("text", "123456");
                                clipboard.setPrimaryClip(clip);
                                //焦点（n是AccessibilityNodeInfo对象）
                                info.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                                ////粘贴进入内容
                                info.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                                break;
                            }
                        }
                     //查找登录
                    list = nodeInfo.findAccessibilityNodeInfosByViewId("org.xianliao:id/btn_login_password_next");
                    if (list.isEmpty()) {
                        return;
                    }
                    for (AccessibilityNodeInfo info : list) {
                        AccessibilityNodeInfo parent = info.getParent();
                        Log.d(TAG, "登录");
                        if (parent != null) {
                            info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            break;
                        }
                    }
                }
                //进入闲聊主界面
                if ("org.sugram.base.MainActivity".equals(event.getClassName())) {
                    liaoTian =0;
                    Log.d(TAG, "闲聊主界面");
                    //拿到根节点
                    AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
                    if (nodeInfo == null) {
                        return;
                    }
                    //点击通讯录
                    openChat(nodeInfo);
                    //点击已保存群聊
                    openSaveChat(nodeInfo);
                    //点击指定（test）群聊
                    openChatName(nodeInfo,"test");
                }
                //闲聊聊天界面
                if("org.sugram.dao.dialogs.SGChatActivity".equals(event.getClassName())){
                    Log.d(TAG, "聊天页面设置标志位");
                    liaoTian = 1;
                }
                if("org.sugram.foundation.ui.imagepicker.ui.ImageGridActivity".equals(event.getClassName())){
                    Log.d(TAG, "图片选择界面");
                    liaoTian = 1;
                    //拿到根节点
                    AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
                    if (nodeInfo == null) {
                        return;
                    }
                    if(data.getType()!=null){
                        if(data.getType()==1){
                            openChatCheckImg(nodeInfo);//选择图片
                            openChatCheckImgOk(nodeInfo);//发送图片
                        }
                    }
                }
            }
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void openChatImgSend(AccessibilityNodeInfo nodeInfo) {
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("org.xianliao:id/plus_iv");
        if (list.isEmpty()) {
            Log.e(TAG,"检测不到+号按钮");
            return;
        }
        for (AccessibilityNodeInfo info : list) {
            AccessibilityNodeInfo parent = info.getParent();
            Log.d(TAG,"点击+号");
            if (parent != null) {
                info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void openChatClictImg(AccessibilityNodeInfo nodeInfo) {
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("图片");
        if (list.isEmpty()) {
            Log.e(TAG,"检测不到图片按钮");
            return;
        }
        for (AccessibilityNodeInfo info : list) {
            AccessibilityNodeInfo parent = info.getParent();
            Log.d(TAG,"点击图片按钮");
            if (parent != null) {
                parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void openChatCheckImg(AccessibilityNodeInfo nodeInfo) {
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("org.xianliao:id/cb_check");
        if (list.isEmpty()) {
            Log.e(TAG,"检测不到选择图片按钮");
            return;
        }
        for (AccessibilityNodeInfo info : list) {
            AccessibilityNodeInfo parent = info.getParent();
            Log.d(TAG,"点击选择图片按钮");
            if (parent != null) {
                parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void openChatCheckImgOk(AccessibilityNodeInfo nodeInfo) {
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("org.xianliao:id/btn_ok");
        if (list.isEmpty()) {
            Log.e(TAG,"检测不到选择图片完成按钮");
            return;
        }
        for (AccessibilityNodeInfo info : list) {
            AccessibilityNodeInfo parent = info.getParent();
            Log.d(TAG,"点击选择图片完成按钮");
            if (parent != null) {
                info.performAction(AccessibilityNodeInfo.ACTION_CLICK);//发送完等2s，然后删除图片
                break;
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void openOnclickSend(AccessibilityNodeInfo nodeInfo) {
        Log.d(TAG, "点击消息发送按钮");
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("org.xianliao:id/send_btn");
        if (list.isEmpty()) {
            Log.d(TAG,"检测不到发送按钮");
            return;
        }
        for (AccessibilityNodeInfo info : list) {
            AccessibilityNodeInfo parent = info.getParent();
            Log.d(TAG,"点击发送");
            if (parent != null) {
                info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void openChatSend(AccessibilityNodeInfo nodeInfo) {
        Log.d(TAG, "指定群:"+data.getChat()+"发送："+data.getText());
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("org.xianliao:id/send_edt");
        if (list.isEmpty()) {
            Log.d(TAG,"信息为空");
            return;
        }
        for (AccessibilityNodeInfo info : list) {
            AccessibilityNodeInfo parent = info.getParent();
            Log.d(TAG,"信息输入");
            if (parent != null) {
                ClipboardManager clipboard = (ClipboardManager)this.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("text", data.getText());
                clipboard.setPrimaryClip(clip);
                //焦点（n是AccessibilityNodeInfo对象）
                info.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                ////粘贴进入内容
                info.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                break;
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void openChatName(AccessibilityNodeInfo nodeInfo,String ChatName) {
        Log.d(TAG, "查找指定群:"+ChatName);
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(ChatName);
        if (list.isEmpty()) {
            return;
        }
        for (AccessibilityNodeInfo info : list) {
            AccessibilityNodeInfo parent = info.getParent();
            Log.d(TAG, "找到群聊【"+ChatName+"】，进行点击");
            if (parent != null) {
                parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void openChat(AccessibilityNodeInfo nodeInfo) {
        Log.d(TAG, "查找通讯录");
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("org.xianliao:id/tab_title");
        if (list.isEmpty()) {
            return;
        }
        if(list.size()==4){
            AccessibilityNodeInfo parent = list.get(1).getParent();
            Log.d(TAG, "找到通讯录，进行点击"+parent.getText());
            if (parent != null) {
                parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void openSaveChat(AccessibilityNodeInfo nodeInfo) {
        Log.d(TAG, "查找已保存群聊");
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("org.xianliao:id/layout_group_chat");
        if (list.isEmpty()) {
            return;
        }
        for (AccessibilityNodeInfo info : list) {
            AccessibilityNodeInfo parent = info.getParent();
            Log.d(TAG, "找到已保存群聊，进行点击");
            if (parent != null) {
                info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    @Override
    public void onInterrupt() {
        Toast.makeText(this, "闲聊机器人服务已关闭", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Toast.makeText(this, "闲聊机器人服务已开启", Toast.LENGTH_SHORT).show();
    }
    private void printEventLog(AccessibilityEvent event) {
        Log.i(TAG, "-------------------------------------------------------------");
        int eventType = event.getEventType(); //事件类型
        Log.i(TAG, "PackageName:" + event.getPackageName() + ""); // 响应事件的包名
        Log.i(TAG, "Source Class:" + event.getClassName() + ""); // 事件源的类名
        Log.i(TAG, "Description:" + event.getContentDescription()+ ""); // 事件源描述
        Log.i(TAG, "Event Type(int):" + eventType + "");

        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:// 通知栏事件
                Log.i(TAG, "event type:TYPE_NOTIFICATION_STATE_CHANGED");
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED://窗体状态改变
                Log.i(TAG, "event type:TYPE_WINDOW_STATE_CHANGED");
                break;
            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED://View获取到焦点
                Log.i(TAG, "event type:TYPE_VIEW_ACCESSIBILITY_FOCUSED");
                break;
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_START:
                Log.i(TAG, "event type:TYPE_VIEW_ACCESSIBILITY_FOCUSED");
                break;
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_END:
                Log.i(TAG, "event type:TYPE_GESTURE_DETECTION_END");
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                Log.i(TAG, "event type:TYPE_WINDOW_CONTENT_CHANGED");
                break;
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                Log.i(TAG, "event type:TYPE_VIEW_CLICKED");
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                Log.i(TAG, "event type:TYPE_VIEW_TEXT_CHANGED");
                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                Log.i(TAG, "event type:TYPE_VIEW_SCROLLED");
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:
                Log.i(TAG, "event type:TYPE_VIEW_TEXT_SELECTION_CHANGED");
                break;
            default:
                Log.i(TAG, "no listen event");
        }

        for (CharSequence txt : event.getText()) {
            Log.i(TAG, "text:" + txt);
        }

        Log.i(TAG, "-------------------------------------------------------------");
    }
}