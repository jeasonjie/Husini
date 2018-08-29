package com.planet.husini;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

import javax.inject.Inject;

import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    Thread tmp = null;
    String xml = "";

    @Inject
    Retrofit mRetrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testPermission();

//        tmp = new ReadLog(this);
//        tmp.start();

//        new Thread(new Runnable() {
//                        @Override
//            public void run() {
//                    try {
//                        Thread.sleep(2000);
//                        readLog();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//            }
//        }).start();

    }

    private void showDialogTest() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("呼死你")
                .setMessage("赶紧点")
                .setPositiveButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showDialogTest();
                    }
                });

        builder.show();
    }

    private void testPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)) {
                Log.e("============", "shouldShowRequestPermissionRationale");
//                showDialogTest();
            } else
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 100);
        } else {
            callPhone();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callPhone();
            } else {
                testPermission();
                Log.e("============", "else");
//                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        tmp.interrupt();
//        tmp = null;
        isRuning = false;
    }

    /**
     * 拨打电话
     */
    private void callPhone() {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "**"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
        startActivity(intent);

    }

    private static final int WHAT_NEXT_LOG = 778;
    boolean isRuning = true;

    /**
     * 读取logcat信息，尝试筛选打电话的log
     */
    private void readLog() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
                Process logcatProcess = null;
                BufferedReader bufferedReader = null;
                StringBuilder log = new StringBuilder();
                String line;
                String[] catchParams = {"logcat", "-b system", "-b events", "-b main", "-b radio", "Telecom:I *:S"};
                try {
                    while (isRuning) {
                        logcatProcess = Runtime.getRuntime().exec(catchParams);
                        bufferedReader = new BufferedReader(new InputStreamReader(logcatProcess.getInputStream()));
                        while ((line = bufferedReader.readLine()) != null) {
                            log.append(line);
                            Message message = mHandler.obtainMessage();
                            message.what = WHAT_NEXT_LOG;
                            message.obj = line;
                            mHandler.sendMessage(message);
                        }
                    }
                    if(log.toString().contains("MiuiCallsManager"))
                        Log.e("===MiuiCallsManager===", "111");
//                    ArrayList commandLine = new ArrayList();
//                    commandLine.add( "logcat");
//                    String[] catchParams = {"logcat", "Telecom:I *:S"};
//                    ShellUtils.CommandResult result = ShellUtils.execCommand(catchParams, false, true);
//                    if(result.successMsg.contains("MiuiCallsManager"))
//                        Log.e("===============", result.successMsg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
//            }
//        }).start();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WHAT_NEXT_LOG:
                    String line = (String) msg.obj;
//                    if(line.contains("MiuiCallsManager"))
                        Log.e("===============", line);
//                    else
                    break;
            }
        }
    };

    /**
     *显示RAM的可用和总容量
     */
    private void showRAMInfo(){
        ActivityManager am=(ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi=new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        String[] available=fileSize(mi.availMem);
        String[] total=fileSize(mi.totalMem);
//        Log.e("========", "RAM "+available[0]+available[1]+"/"+total[0]+total[1]);
        Log.e("========", Formatter.formatFileSize(MainActivity.this, mi.totalMem));
    }

    /**
     *显示ROM的可用和总容量 获取手机内部存储空间
     */
    private void showROMInfo(){
        File file= Environment.getDataDirectory();
        StatFs statFs=new StatFs(file.getPath());
        long blockSize=statFs.getBlockSize();
        long totalBlocks=statFs.getBlockCount();
        long availableBlocks=statFs.getAvailableBlocks();

        String[] total=fileSize(totalBlocks*blockSize);
        String[] available=fileSize(availableBlocks*blockSize);

//        Log.e("========", "ROM "+available[0]+available[1]+"/"+total[0]+total[1]);
        Log.e("========", Formatter.formatFileSize(MainActivity.this, blockSize * totalBlocks));
    }

    /**
     *显示SD卡的可用和总容量，获取手机外部存储空间
     */
    private void showSDInfo(){
        if(Environment.getExternalStorageState().equals
                (Environment.MEDIA_MOUNTED)){//sd卡是否可用
            File file=Environment.getExternalStorageDirectory();
            StatFs statFs=new StatFs(file.getPath());
            long blockSize=statFs.getBlockSize();
            long totalBlocks=statFs.getBlockCount();
            long availableBlocks=statFs.getAvailableBlocks();

            String[] total=fileSize(totalBlocks*blockSize);
            String[] available=fileSize(availableBlocks*blockSize);

//            Log.e("===========", "SD "+available[0]+available[1]+"/"+total[0]+total[1]);
            Log.e("===========", Formatter.formatFileSize(MainActivity.this, blockSize * totalBlocks));
        }else {
            Log.e("============", "SD CARD 已删除");
        }
    }
    /*返回为字符串数组[0]为大小[1]为单位KB或者MB*/
    private String[] fileSize(long size){
        float sizef = size;
        int kmg = 1024;
        String str="";
        if(sizef>=kmg ){
            str="KB";
            sizef/=kmg ;
            if(sizef>=kmg ){
                str="MB";
                sizef/=kmg ;
                if(sizef>=kmg){
                    str="G";
                    sizef/=kmg;
                }
            }
        }
        /*将每3个数字用,分隔如:1,000*/
        DecimalFormat formatter=new DecimalFormat();
        formatter.setGroupingSize(3);
        String result[]=new String[2];
        result[0]=formatter.format(size);
        result[1]=str;
        return result;
    }
}
