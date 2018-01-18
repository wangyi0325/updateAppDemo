package com.example.json.updateappdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.text.NumberFormat;

public class MainActivity extends Activity  {
    private ProgressDialog pDialog;
    private String nowVersion;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
        textView = new TextView(MainActivity.this);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(130,130);
        textView.setLayoutParams(params);
        textView.setBackgroundColor(Color.BLUE);
        textView.setTextColor(Color.WHITE);
        textView.setGravity(Gravity.CENTER);

        checkUpdate();
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(
                    getPackageName(), 0);
            nowVersion = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    public void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    NotificationManager mNotificationManager;
    Notification mNotification;
    private static final int NOTIFY_ID = 0;
    public void download(final String downloadurl, View view) {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int icon = R.drawable.icon_river;
        CharSequence tickerText = "开始下载";
        long when = System.currentTimeMillis();
        mNotification = new Notification(icon, tickerText, when);

        // 放置在"正在运行"栏目中
        mNotification.flags = Notification.FLAG_ONGOING_EVENT;
        RemoteViews contentView = new RemoteViews(this.getPackageName(), R.layout.download_notification_layout);
        contentView.setTextViewText(R.id.rate, "AngryBird.apk");
        // 指定个性化视图
        mNotification.contentView = contentView;

        // intent为null,表示点击通知时不跳转
        Intent resultIntent = new Intent(MainActivity.this,
                MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(MainActivity.this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // 指定内容意图
       // mNotification.contentIntent = contentIntent;

        mNotificationManager.notify(NOTIFY_ID, mNotification);

        new Thread() {
            public void run() {
                setDownLoad(downloadurl);
            };
        }.start();
    }


    /**
     * 下载更新,
     */
    protected void checkUpdate() {
        // TODO Auto-generated method stub
        progressDialogShow(this, "正在查询...");
        RequestParams params = new RequestParams("http://112.126.73.72:1990/hzz/sys/dic/queryAppVersion");
        params.setAsJsonContent(true);
        params.addBodyParameter("version","1.0.3");
        x.http().post(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                // TODO Auto-generated method stub
                progressDialogDismiss();
                System.out.println("提示网络错误");
            }

            @Override
            public void onFinished() {
                // TODO Auto-generated method stub

            }

            @Override
            public void onSuccess(String arg0) {
                // TODO Auto-generated method stub
                progressDialogDismiss();
                try {
                    JSONObject object = new JSONObject(arg0);
                    int success = object.getInt("code");
                    if (success==0) {
                        if(object.getString("data")!=null){
                            JSONObject jsonObject = new JSONObject(object.getString("data"));
                            String downloadurl = jsonObject.getString("appUrl");
                            String versionname = jsonObject.getString("version");
                            if (nowVersion.equals(versionname)) {
                                System.out.println("当前版本为最新");
                            } else {
                                // 不同，弹出更新提示对话框
                                setUpDialog(versionname, downloadurl, "更新了一些bug");
                            }
                        }
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     *
     * @param versionname
     *            地址中版本的名字
     * @param downloadurl
     *            下载包的地址
     * @param desc
     *            版本的描述
     */
    protected void setUpDialog(String versionname, final String downloadurl,
                               String desc) {
        // TODO Auto-generated method stub
        AlertDialog dialog = new AlertDialog.Builder(this).setCancelable(false)
                .setTitle("下载" + versionname + "版本").setMessage(desc)
                .setNegativeButton("取消", null)
                .setPositiveButton("下载", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // TODO Auto-generated method stub
//                        Intent intent = new Intent(MainActivity.this, DownAPKService.class);
//                        intent.putExtra("downloadURL",downloadurl);
//                        startService(intent);
                        download(downloadurl, LayoutInflater.from(MainActivity.this).inflate(R.layout.download_notification_layout,null));
                    }
                }).create();
        dialog.show();
    }


    private void progressDialogShow(Context context, String msg) {
        pDialog = new ProgressDialog(context);
        pDialog.setMessage(msg);
        // pDialog.setCancelable(false);
        pDialog.show();
    }

    private void progressDialogDismiss() {
        try {
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 下载包
     *
     * @param downloadurl
     *            下载的url
     *
     */
    private ProgressDialog progressDialog;
    @SuppressLint("SdCardPath")
    protected void setDownLoad(String downloadurl) {
        // TODO Auto-generated method stub
        RequestParams params = new RequestParams(downloadurl);
        params.setAutoRename(true);//断点下载
        params.setSaveFilePath("/mnt/sdcard/demo.apk");
        x.http().get(params, new Callback.ProgressCallback<File>() {

            @Override
            public void onCancelled(CancelledException arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                // TODO Auto-generated method stub
                if(progressDialog!=null && progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                System.out.println("提示更新失败");
            }

            @Override
            public void onFinished() {
                // TODO Auto-generated method stub

            }

            @Override
            public void onSuccess(File arg0) {
                // TODO Auto-generated method stub
                if(progressDialog!=null && progressDialog.isShowing()){
                    progressDialog.dismiss();
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    //判读版本是否在7.0以上
                    Uri apkUri = FileProvider.getUriForFile(MainActivity.this, "com.example.json.updateappdemo.fileprovider",arg0);
                    Intent install = new Intent(Intent.ACTION_VIEW);
                    install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    install.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    startActivity(install);

                }else{
                    //以前的启动方法
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setDataAndType(Uri.fromFile(new File(Environment
                                    .getExternalStorageDirectory(), "demo.apk")),
                            "application/vnd.android.package-archive");
                    startActivity(intent);
                }
            }

            @Override
            public void onLoading(long arg0, long arg1, boolean arg2) {
                // TODO Auto-generated method stub
                progressDialog.setMax((int)arg0);
                progressDialog.setProgress((int)arg1);
                Message msg = handler.obtainMessage();
                Double rate =(double)arg1/arg0;
                NumberFormat num = NumberFormat.getPercentInstance();
                String rates = num.format(rate);
                msg.what = 1;
                msg.obj = rates;
                handler.sendMessage(msg);
            }

            @Override
            public void onStarted() {
                // TODO Auto-generated method stub
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//设置为水平进行条
//                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);//设置为圆形加载框
                progressDialog.setMessage("玩命下载中...");
                progressDialog.setProgress(0);
                progressDialog.setCanceledOnTouchOutside(false);
//                progressDialog.setProgressDrawable(getResources().getDrawable(R.drawable.icon_taskpos));
                progressDialog.show();
            }

            @Override
            public void onWaiting() {
                // TODO Auto-generated method stub

            }
        });
    }
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    String rate = (String) msg.obj;
                    rate = rate.replace("%","");
                    int rates = Integer.parseInt(rate);

                    if (rates < 100) {
                        // 更新进度
                        RemoteViews contentView = mNotification.contentView;
                        contentView.setTextViewText(R.id.rate, rate + "%");
                        contentView.setProgressBar(R.id.progress, 100, rates, false);
                        contentView.setTextViewText(R.id.fileName,"下载中，请稍后....");
                    } else {
//                         下载完毕后变换通知形式
//                        mNotification.flags = Notification.FLAG_AUTO_CANCEL;
//                        mNotification.contentView = null;
//                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
//                        PendingIntent contentIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);
//                        mNotification.setLatestEventInfo(mContext, "下载完成", "文件已下载完毕", contentIntent);
                        RemoteViews contentView = mNotification.contentView;
                        contentView.setTextViewText(R.id.rate, rate + "%");
                        contentView.setProgressBar(R.id.progress, 100, rates, false);
                        contentView.setTextViewText(R.id.fileName,"下载完成");
                    }

                    // 最后别忘了通知一下,否则不会更新
                    mNotificationManager.notify(NOTIFY_ID, mNotification);
                    break;
                case 0:
                    // 取消通知
                    mNotificationManager.cancel(NOTIFY_ID);
                    break;
            }
        };
    };




}
