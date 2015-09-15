package com.qianyouba.servicedemo.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.qianyouba.servicedemo.MainActivity;
import com.qianyouba.servicedemo.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2015/8/25 0025.
 */
public class LocalService extends Service {
    private static final String TAG = LocalService.class.getName();
    private NotificationManager mNM;
    Notification notification;
    ;
    /**
     * 在 Local Service 中我们直接继承 Binder 而不是 IBinder,因为 Binder 实现了 IBinder 接口，这样我们可以少做很多工作。
     * @author newcj
     */
    public class SimpleBinder extends Binder {
        /**
         * 获取 Service 实例
         * @return
         */
        public LocalService getService(){
            return LocalService.this;
        }

        public void upload(String uri, Map<String, String> params,
                           final Map<String, File> fileMap){
            long fileSize = 0;
            Map<String, String> base_config = new HashMap<String,String>();
            base_config.put("url", Constants.SERVER_IP + uri);
            base_config.put("charset", "GBK");
            Map<String, Map> reqParams = new HashMap<String, Map>();
            reqParams.put("base_config", base_config);
            reqParams.put("form_field", params);
            if(fileMap!=null) {
                reqParams.put("file_part", fileMap);
                for(Map.Entry<String,File> entry:fileMap.entrySet()){
                    fileSize += entry.getValue().length();
                }
            }
            new UploadUtil(fileSize).execute(reqParams);
        }
    }
    private void showNotification(int id){
        PendingIntent contentIntent = PendingIntent.getActivity(this, 1, new Intent(this, MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);
        Notification.Builder builder = new Notification.Builder(this);
        RemoteViews view = new RemoteViews(getPackageName(), R.layout.upload_notify);
        builder.setContentTitle("正在上传……")
                .setContent(view)
                .setTicker("开始上传")
                .setContentIntent(contentIntent)
                .setWhen(System.currentTimeMillis())
                        //.setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true)
                        //.setOngoing(true)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setSmallIcon(R.mipmap.ic_launcher);
        notification = builder.getNotification();

        // 注意使用  startForeground ，id 为 0 将不会显示 notification
        startForeground(id, notification);
        mNM.notify(id, notification);
    }
    private class UploadUtil extends AsyncTask<Map<String, Map>, Long, List> implements MultipartUtility.OnUploadListener{
        public UploadUtil(long totalSize){
            this.total = totalSize;
        }
        private int notify_id;
        private long total;
        @Override
        protected List doInBackground(Map<String, Map>... params) {
            Map<String, String> base_config = params[0].get("base_config");
            Map<String, String> form_field = params[0].get("form_field");
            Map<String, File> file_part = params[0].get("file_part");
            MultipartUtility multipart = null;
            try {
                multipart = new MultipartUtility(base_config.get("url"), base_config.get("charset"));
                multipart.addHeaderField("User-Agent", "WinHttpClient");
                multipart.addHeaderField("Test-Header", "Header-Value");
                multipart.setUploadListener(this);
                for (Map.Entry<String, String> entry : form_field.entrySet()) {
                    multipart.addFormField(entry.getKey(), entry.getValue());
                }
                if(file_part!=null) {
                    for (Map.Entry<String, File> entry : file_part.entrySet()) {
                        multipart.addFilePart(entry.getKey(), entry.getValue());
                    }
                }
                List<String> response = multipart.finish();
                StringBuilder result = new StringBuilder();
                for (String msg : response) {
                    result.append(msg);
                }
                List resultList = new ArrayList();
                resultList.add(result.toString());
                resultList.add(file_part);
                return resultList;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            notify_id = ++Constants.id;
            showNotification(notify_id);
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            super.onProgressUpdate(values);
            int progress = (int)(values[0]/total);
            notification.contentView.setTextViewText(R.id.progress_text,progress*100 + "%");
            notification.contentView.setProgressBar(R.id.progress_bar, 1, progress, false);
            mNM.cancel(notify_id);
        }

        @Override
        protected void onPostExecute(List resultList) {

        }
        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        public void progress(long size) {
            publishProgress(size);
        }
    }

    public SimpleBinder sBinder;
    /**
     * onBind 是 Service 的虚方法，因此我们不得不实现它。
     * 返回 null，表示客服端不能建立到此服务的连接。
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG,"onBind");
        return sBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sBinder = new SimpleBinder();
        mNM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Log.e(TAG, "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }
}
