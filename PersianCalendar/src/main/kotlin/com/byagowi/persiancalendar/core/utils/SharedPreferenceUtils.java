package com.byagowi.persiancalendar.core.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 共享存储工具类
 * Create by Frank 0n 2017/10/24 9:33
 *
 * @author Frank
 */

public class SharedPreferenceUtils {
    /**
     * 保存当前用户信息的文件名
     */
    public static final String USERINFO = "user_info";
    public static final String USER_NAME = "user_name";
    public static final String USER_ICON = "user_icon";
    public static final String USER_SEX = "user_sex";
    public static final String USER_PSW = "user_password";
    public static final String JUMP = "jump";

    /**
     * 保存数据
     *
     * @param context  上下文对象
     * @param fileName 文件名
     * @param key      保存对象的key索引
     * @param o        需要保存的对象
     */
    public static void saveData(Context context, String fileName, String key, Object o) {
        //覆盖写入
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        if (o instanceof String) {
            edit.putString(key, (String) o);
        } else if (o instanceof Integer) {
            edit.putInt(key, (Integer) o);
        } else if (o instanceof Boolean) {
            edit.putBoolean(key, (Boolean) o);
        } else if (o instanceof Float) {
            edit.putFloat(key, (Float) o);
        } else if (o instanceof Long) {
            edit.putLong(key, (Long) o);
        } else {
            edit.putString(key, o.toString());
        }
        edit.apply();
    }

    /**
     * 获取数据
     *
     * @param context 上下文对象
     * @param key     获取数据的关键字
     * @param o       获取的数据的对象
     * @return 返回获取的数据
     */
    public static Object getData(Context context, String key, Object o, String fileName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        if (o instanceof String) {
            return sharedPreferences.getString(key, "");
        } else if (o instanceof Integer) {
            return sharedPreferences.getInt(key, 0);
        } else if (o instanceof Boolean) {
            return sharedPreferences.getBoolean(key, false);
        } else if (o instanceof Float) {
            return sharedPreferences.getFloat(key, 0f);
        } else if (o instanceof Long) {
            return sharedPreferences.getLong(key, 0L);
        } else {
            return sharedPreferences.getString(key, o.toString());
        }
    }

    /**
     * 清楚相对应的数据
     */
    public static void removeData(Context context, String key, String fileName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.remove(key);
        edit.commit();
    }

    /**
     * 清楚所有数据
     */
    public static void removeAll(Context context, String fileName) {
        context.getSharedPreferences(fileName, Context.MODE_PRIVATE).edit().clear().commit();
    }

    /**
     * 默认账号
     */
    public static void selfAccount(Context context) {
        SharedPreferenceUtils.saveData(context, SharedPreferenceUtils.USERINFO, SharedPreferenceUtils.USER_NAME, "administrator");
        SharedPreferenceUtils.saveData(context, SharedPreferenceUtils.USERINFO, SharedPreferenceUtils.USER_PSW, "administrator");
        SharedPreferenceUtils.saveData(context, SharedPreferenceUtils.USERINFO, SharedPreferenceUtils.USER_SEX, "");
        SharedPreferenceUtils.saveData(context, SharedPreferenceUtils.USERINFO, SharedPreferenceUtils.USER_ICON, "");
    }
}
