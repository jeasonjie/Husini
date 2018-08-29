package com.planet.husini;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog;
import android.support.v4.content.PermissionChecker;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InterceptCallReciever extends BroadcastReceiver {
    private int mCurrentState = TelephonyManager.CALL_STATE_IDLE ;
    private int mOldState = TelephonyManager.CALL_STATE_IDLE ;
    SharedPreferences spf = null;
    private static String FLAG_CALL_STATE = "FLAG_CALL_STATE";

    private boolean isIncoming = false;
    private int lastCallState  = TelephonyManager.CALL_STATE_IDLE;

    Context context;
    int oldCount = -1;
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
//        test1();
        spf = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {// 如果是去电
//            Log.e("=============", "oldCount: " + oldCount);
//            Log.e("============", "Intent.ACTION_NEW_OUTGOING_CALL");
//
//            TelephonyManager tManager = (TelephonyManager) context
//                    .getSystemService(Service.TELEPHONY_SERVICE);
//            switch (tManager.getCallState()) {
//
//                case TelephonyManager.CALL_STATE_RINGING:
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
//            String mIncomingNumber = intent.getStringExtra("incoming_number");
//                    break;
//            }
//            Log.e("=============", "mIncomingNumber: " + number);

            TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);

        } else {
//            String state = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
//            String phoneNumber = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
//            int stateChange = 0;
//
//            if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)){
//                ConstantUtil.isCall =false;
//                //空闲状态
//                stateChange =TelephonyManager.CALL_STATE_IDLE;
//                if (isIncoming){
////                    onIncomingCallEnded(context,phoneNumber);
//                }else {
//                    onOutgoingCallEnded(context,phoneNumber);
//                }
//            }else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
//                //摘机状态
//                ConstantUtil.isCall = false;
//                stateChange = TelephonyManager.CALL_STATE_OFFHOOK;
//                if (lastCallState != TelephonyManager.CALL_STATE_RINGING) {
//                    //如果最近的状态不是来电响铃的话，意味着本次通话是去电
//                    isIncoming = false;
//                    onOutgoingCallStarted(context, phoneNumber);
//                } else {
//                    //否则本次通话是来电
//                    isIncoming = true;
////                    onIncomingCallAnswered(context, phoneNumber);
//                }
//            } else if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)){
////                //否则本次通话是来电
////                isIncoming = true;
//                    //来电响铃状态
//                    ConstantUtil.isCall =false;
//                    stateChange = TelephonyManager.CALL_STATE_RINGING;
//                    lastCallState = stateChange;
////                    onIncomingCallReceived(context,contactNum);
//
//            }
        }
    }

    protected void onOutgoingCallStarted(Context context,String number){
        //正在通话中会走这个方法。在这里处理自己的逻辑
//        putRed(context);
        Log.e("+++++++++++++++++++", "onOutgoingCallStarted");

    }

    protected void onOutgoingCallEnded(Context context,String number){
        //电话挂断时候会走这个方法。在这里处理自己的逻辑
        Log.e("+++++++++++++++++++", "onOutgoingCallEnded");
//        Intent intent =new Intent(context,MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(intent);
    }


    /**
     * 挂断电话
     */
    public void endCall() {
        try {
            Class clazz = context.getClassLoader().loadClass(
                    "android.os.ServiceManager");
            Method method = clazz.getDeclaredMethod("getService", String.class);
            IBinder iBinder = (IBinder) method.invoke(null,
                    Context.TELEPHONY_SERVICE);
            ITelephony itelephony = ITelephony.Stub.asInterface(iBinder);
            itelephony.endCall();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, final String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            mOldState = spf.getInt(FLAG_CALL_STATE, TelephonyManager.CALL_STATE_IDLE);


            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    mCurrentState = TelephonyManager.CALL_STATE_IDLE;
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    mCurrentState = TelephonyManager.CALL_STATE_OFFHOOK;
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    mCurrentState = TelephonyManager.CALL_STATE_RINGING;
                    break;
            }

            if(mOldState == TelephonyManager.CALL_STATE_IDLE && mCurrentState == TelephonyManager.CALL_STATE_OFFHOOK ) {
                Log.e("======", "onCallStateChanged: 接通");
                spf.edit().putInt(FLAG_CALL_STATE, mCurrentState).commit();
//                endCall();
//                Log.e("===========", "incomingNumber: " + incomingNumber);
//                deleteCallLog(incomingNumber);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(5000);//小米手机大约等待5秒后，手机屏幕显示已接通，并且时间显示00:01秒
////                        while (true) {
//////                            String tmp = incomingNumber;
//////                            int newCount = test(tmp);
//////                            Log.e("==========", "newCount:  " + newCount);
//////                            if(newCount > oldCount) {
//////                                getCallLogState(tmp);
//////                                break;
//////                            }
//////
//////                        }
////                        test1();
////                            test();
//
                            endCall();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
//
                    }
                }).start();

            } else if (mOldState == TelephonyManager.CALL_STATE_OFFHOOK && mCurrentState == TelephonyManager.CALL_STATE_IDLE) {
                Log.e("======", "onCallStateChanged: 挂断");
                spf.edit().putInt(FLAG_CALL_STATE, mCurrentState).commit();
            }
        }
    }


    /**
     * 监听数据库状态，当CallLog.Calls.CONTENT_URI系统表数据变化时，会有回调
     */
    private void test() {
        context.getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true, new CallLogObserver(new Handler()));
    }

    class CallLogObserver extends ContentObserver {

        public CallLogObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            Log.e("----------", "onChange");
            getCallLogState("**");
            context.getContentResolver().unregisterContentObserver(this);
            super.onChange(selfChange);
        }
    }

    public void deleteCallLog(String incomingNumber) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = Uri.parse("content://call_log/calls");
        Cursor cursor = resolver.query(uri, new String[] { "duration" }, "number=?",
                new String[] { incomingNumber }, "_id desc limit 1");
        if (cursor.moveToNext()) {
//            String id = cursor.getString(0);
            int durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION);
            long durationTime = cursor.getLong(durationIndex);
//            resolver.delete(uri, "_id=?", new String[] { id });
            if(durationTime > 0) {
                Log.e("============", "dur: " + durationTime);
            }
        }
    }

    /**
     * 问题：尝试读取通话记录，但是在高android版本中通话结束才会生成一条新的通话记录在数据库中
     * 现在读取的CallLog.Calls.DURATION，一直是上一条的记录值
     * @param number
     * @return
     */
    private boolean getCallLogState(String number) {
        boolean isLink = false;
        ContentResolver cr = context.getContentResolver();
        PermissionChecker.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG);
        final Cursor cursor = cr.query(CallLog.Calls.CONTENT_URI,
                new String[]{CallLog.Calls.NUMBER,CallLog.Calls.TYPE,CallLog.Calls.DURATION},
                CallLog.Calls.NUMBER +"=?",
                new String[]{number},
                CallLog.Calls.DATE + " desc");
        int i = 0;
        while(cursor.moveToNext()){

            if (i == 0) {//第一个记录 也就是当前这个电话的记录
                int durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION);
                long durationTime = cursor.getLong(durationIndex);
//                Log.d("test", "getCallLogState: -----------------duration= " + durationTime);
                if(durationTime > 0){
//                    LogUtil.log("到这里了 这是if里 durationTime = "+durationTime);
                    Log.e("============", "durationTime: " + durationTime);
                    isLink = true;

                } else {
//                    LogUtil.log("到这里了 这是else里");
                    Log.e("============", "这是else里: ");
                    isLink = false;
                }
            }
            i++;
//            int durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION);
//            long durationTime = cursor.getLong(durationIndex);


        }
        return isLink;
    }

    private int test(String number) {
        ContentResolver cr = context.getContentResolver();
        PermissionChecker.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG);
        final Cursor cursor = cr.query(CallLog.Calls.CONTENT_URI,
                new String[]{CallLog.Calls.NUMBER,CallLog.Calls.TYPE,CallLog.Calls.DURATION},
                CallLog.Calls.NUMBER +"=?",
                new String[]{number},
                CallLog.Calls.DATE + " desc");
//        Log.e("==========", "count: " + cursor.getCount());
        int tmp = cursor.getCount();
        cursor.close();
        return tmp;
    }


    /**
     * 通过反射，尝试监听电话真正接通时的状态。未果
     */
    private void test1() {
        try {
//            ClassLoader classLoader = context.getClassLoader();
            final ClassLoader classLoader = this.getClass().getClassLoader();
            final Class<?> classCallManager = classLoader.loadClass("com.android.internal.telephony.CallManager");
            Method method_getInstance = classCallManager.getDeclaredMethod("getInstance");
            method_getInstance.setAccessible(true);
            Object callManagerInstance = method_getInstance.invoke(null);//callManager对象

//            Method method_getActiveFgCall = classCallManager.getDeclaredMethod("getActiveFgCall");
//            Object callInstance = method_getActiveFgCall.invoke(null);//Call对象


//            final ClassLoader classLoader1 = this.getClass().getClassLoader();
//            final Class<?> classCallManager1 = classLoader1.loadClass("com.android.internal.telephony.PhoneFactory");
//            Method method_getInstance1 = classCallManager1.getDeclaredMethod("getDefaultPhone");
//            method_getInstance1.setAccessible(true);
//            Object PhoneFactoryInstance = method_getInstance1.invoke(null);//PhoneFactory对象
//
//            Method method_registerPhone = classCallManager.getDeclaredMethod("registerPhone");
//            method_registerPhone.invoke(null, PhoneFactoryInstance);

//            Method method_registerForPreciseCallStateChanged = classCallManager.getDeclaredMethod("getState");
//            method_getInstance.setAccessible(true);
//            method_registerForPreciseCallStateChanged.invoke(callManagerInstance);
////            while(true) {
//                Log.e("------------", "Phone state = " + method_registerForPreciseCallStateChanged.invoke(callManagerInstance));
            Method method_registerForPreciseCallStateChanged = classCallManager.getDeclaredMethod("registerForPreciseCallStateChanged");
            method_getInstance.setAccessible(true);
            method_registerForPreciseCallStateChanged.invoke(callManagerInstance, mHandler, PHONE_STATE_CHANGED);
//            while(true) {
                Log.e("------------", "Phone state = " + method_registerForPreciseCallStateChanged.invoke(callManagerInstance));
//            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            System.out.println("此处接收被调用方法内部未被捕获的异常");
            Throwable t = e.getTargetException();// 获取目标异常
            t.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

//        CallManager mCM = CallManager.getInstance();
//        Phone  phone = PhoneFactory.getDefaultPhone();
//
//        mCM.registerPhone(phone);
//        mCM.registerForPreciseCallStateChanged(mHandler, PHONE_STATE_CHANGED, null);
    }

    private static final int PHONE_STATE_CHANGED=102;

    private Handler mHandler=new Handler(){

        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case PHONE_STATE_CHANGED:
//                    updatePhoneSateChange();
                    Log.e("========", "lsdjf-----------");
                    break;
                default:
                    break;
            }
        };

    };
}
