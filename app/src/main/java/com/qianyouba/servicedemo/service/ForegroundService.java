package com.qianyouba.servicedemo.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.qianyouba.servicedemo.MainActivity;
import com.qianyouba.servicedemo.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2015/8/25 0025.
 */
public class ForegroundService extends Service {
    private static final Class[] mStartForegroundSignature = new Class[]{int.class, Notification.class};
    private static final Class[] mStopForegroundSignature = new Class[]{boolean.class};
    private NotificationManager mNM;
    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        try {
            mStartForeground = ForegroundService.class.getMethod("startForeground", mStartForegroundSignature);
            mStopForeground = ForegroundService.class.getMethod("stopForeground", mStopForegroundSignature);
        } catch (NoSuchMethodException e) {
            mStartForeground = mStopForeground = null;
        }
        // 我们并不需要为 notification.flags 设置 FLAG_ONGOING_EVENT，因为
        // 前台服务的 notification.flags 总是默认包含了那个标志位
        PendingIntent contentIntent = PendingIntent.getActivity(this, 1, new Intent(this, MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Foreground service")
                .setContentText("Foreground service started")
                .setTicker("Foreground service started")
                .setContentIntent(contentIntent)
                .setWhen(System.currentTimeMillis())
                //.setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true)
                //.setOngoing(true)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setSmallIcon(R.mipmap.ic_launcher);
        Notification notification = builder.getNotification();

        // 注意使用  startForeground ，id 为 0 将不会显示 notification
        startForegroundCompat(1, notification);
        //startForeground(1, notification);
        mNM.notify(1, notification);
        Log.e("ForegroundService","onCreate");
    }

    private void startForegroundCompat(int id, Notification n) {
        if (mStartForeground != null) {
            mStartForegroundArgs[0] = id;
            mStartForegroundArgs[1] = n;

            try {
                mStartForeground.invoke(this, mStartForegroundArgs);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return;
        }
        //setForeground(ture);
        mNM.notify(id, n);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForegroundCompat(1);
        //stopForeground(true);
        Log.e("ForegroundService","onDestroy");
    }

    private void stopForegroundCompat(int id) {
        if (mStopForeground != null) {
            mStopForegroundArgs[0] = Boolean.TRUE;

            try {
                mStopForeground.invoke(this, mStopForegroundArgs);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return;
        }

        //  在 setForeground 之前调用 cancel，因为我们有可能在取消前台服务之后
        //  的那一瞬间被kill掉。这个时候 notification 便永远不会从通知一栏移除
        mNM.cancel(id);
        //setForeground(false);
    }
}
