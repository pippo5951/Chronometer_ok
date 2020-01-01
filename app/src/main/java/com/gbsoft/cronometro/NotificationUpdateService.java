package com.gbsoft.cronometro;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class NotificationUpdateService extends Service {

    public static final String ACTION_START = "com.embedonix.simplechronometer.START_NOTIFY_SERVICE";
    public static final String ACTION_STOP = "com.embedonix.simplechronometer.STOP_NOTIFY_SERVICE";

    private CronometroApplication mApplication;
    private long mStartTime;
    private boolean mThreadCanRun;

    private UpdateNotification mUpdateNotification;
    private Thread mUpdateThread;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = (CronometroApplication)getApplication();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent.getAction().equalsIgnoreCase(ACTION_START)) {
            //todo make sure this is OK! e.g. check against current time
            mStartTime = intent.getLongExtra("START_TIME", System.currentTimeMillis());
            start();
            return START_REDELIVER_INTENT;
        } else { //attualmente altro caso è solo fermarsi!
            stop();
            stopSelf();
        }

        return START_NOT_STICKY;
    }



    private void start() {

        mThreadCanRun = true;

        if(mUpdateNotification == null) {
            mUpdateNotification = new UpdateNotification();
            mUpdateThread = new Thread(mUpdateNotification);
            mUpdateThread.start();
        } else {
            stop();
            start();
        }

    }

    private void stop() {

        mThreadCanRun = false;

        if(mUpdateThread != null) {
            mUpdateThread.interrupt();
        }

        mUpdateThread = null;
        mUpdateNotification = null;
        mApplication.cancelNotification();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class UpdateNotification implements Runnable {

        @Override
        public void run() {
            while(mThreadCanRun) {

                long since = System.currentTimeMillis() - mStartTime;
                mApplication.showNotification(ChronometerState.IN_BACKGROUND,
                        Helpers.ConvertTimeToString(since));

                //fermarsi per 1 secondo
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }
    }
}
