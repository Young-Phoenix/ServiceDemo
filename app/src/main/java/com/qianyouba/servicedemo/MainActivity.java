package com.qianyouba.servicedemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.qianyouba.servicedemo.service.Constants;
import com.qianyouba.servicedemo.service.ForegroundService;
import com.qianyouba.servicedemo.service.LocalService;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private class SimpleServiceConnection implements ServiceConnection{
        public static final String COMMIT_SHOP_INFO="MobiApi/Index/add_agents";
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocalService.SimpleBinder sBinder = (LocalService.SimpleBinder)service;
            //文件信息
            Map<String, File> fileMap = new HashMap<String, File>();
            fileMap.put("image" + 0, imageFile);
            fileMap.put("image" + 1, imageFile);
            fileMap.put("image" + 2, imageFile);
            fileMap.put("image" + 3, imageFile);
            fileMap.put("video_inside", new File(innerVideoPath));
            fileMap.put("video_outside", new File(outerVideoPath));
            //文本信息
            Map<String, String> params = new HashMap<String, String>();
            params.put(Constants.TOKEN, );
            params.put(Constants.GEOLAT,"35.291828");
            params.put(Constants.GEOLNG,"113.919982");
            params.put("username","abcedfg");
            params.put("password", "123456");
            params.put("agent_name", "豆比豆比豆豆比");
            params.put("type", "1");
            params.put("linkman", "大龙");
            params.put("tel", "123456");
            params.put("mobile", "123456789");
            params.put("description", "诚信房产中介，talk is cheap,show me the code");
            params.put("address", "不告诉你");
            sBinder.upload(COMMIT_SHOP_INFO,params,fileMap);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
    private ServiceConnection sc;
    private NotificationManager mNM;
    private boolean isBind;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sc = new SimpleServiceConnection();
        mNM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Button start = (Button)findViewById(R.id.start);
        Button stop = (Button)findViewById(R.id.stop);
        Button send = (Button)findViewById(R.id.send);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startService(new Intent(MainActivity.this, ForegroundService.class));
                bindService(new Intent(MainActivity.this,LocalService.class),sc, Context.BIND_AUTO_CREATE);
                isBind = true;
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //stopService(new Intent(MainActivity.this, ForegroundService.class));
                if(isBind){
                    unbindService(sc);
                    isBind = false;
                }
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PendingIntent contentIntent = PendingIntent.getActivity(MainActivity.this, 2, new Intent(MainActivity.this, MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);
                Notification.Builder builder = new Notification.Builder(MainActivity.this);
                builder.setContentTitle("通知标题")
                        .setContentText("通知测试内容")
                        .setTicker("小豆比，有通知来了")
                        .setContentIntent(contentIntent)
                        .setWhen(System.currentTimeMillis())
                                //.setPriority(Notification.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        //.setOngoing(true)
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setSmallIcon(R.mipmap.ic_launcher);
                Notification notification = builder.getNotification();
                mNM.notify(2, notification);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
