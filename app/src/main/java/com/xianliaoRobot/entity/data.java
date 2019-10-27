package com.xianliaoRobot.entity;


import android.util.Log;

public class data {
    private static Integer id;
    private static String title;
    private static Integer type;
    private static String text;
    private static String chat;
    public static Integer getId() {
        return id;
    }
    public static void setId(Integer id) {
        data.id = id;
    }
    public static String getTitle() {
        return title;
    }
    public static void setTitle(String title) {
        data.title = title;
    }
    public static Integer getType() {
        return type;
    }
    public static void setType(Integer type) {
        data.type = type;
    }


    public static String getText() {
        return text;
    }

    public static void setText(String text) {
        data.text = text;
    }

    public static String getChat() {
        return chat;
    }

    public static void setChat(String chat) {
        data.chat = chat;
    }
}
