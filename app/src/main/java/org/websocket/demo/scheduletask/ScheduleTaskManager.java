package org.websocket.demo.scheduletask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.SparseArray;

import org.websocket.demo.util.LogUtil;

public class ScheduleTaskManager extends BroadcastReceiver implements
        ScheduleTask
{
    
    private static final String WAKELOCK_TAG = "schedule_task";

    private static final String SCHEDULE_TASK_ACTION = "com.gimis.vtd.intent.action.SCHEDULETASK";

    private static final ExecutorService sExecutor = Executors
            .newCachedThreadPool();

    private final Context mContext;
    private final AlarmManager mAlarmManager;
    PowerManager.WakeLock mWakeLock;

    static class Alarm
    {
        public PendingIntent mAlaramSender;
        public Callback mCallback;
    }

    private final SparseArray<Alarm> mAlarms;

    public ScheduleTaskManager(Context context)
    {
        mContext = context;
        mAlarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        PowerManager powerManager = (PowerManager) context
                .getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                WAKELOCK_TAG);
        mAlarms = new SparseArray<Alarm>();
    }

    public synchronized void startSchedule(Callback callback, long triggerTime)
    {
        Alarm alarm = findAlarm(callback);
        if (alarm == null)
        {
            alarm = new Alarm();
            int id = nextId();
            alarm.mCallback = callback;
            Intent i = new Intent(SCHEDULE_TASK_ACTION);
            i.putExtra("id", id);
            alarm.mAlaramSender = PendingIntent.getBroadcast(mContext, id, i, 0);

            if (mAlarms.size() == 0)
            {
                IntentFilter filter = new IntentFilter();
                filter.addAction(SCHEDULE_TASK_ACTION);
                mContext.registerReceiver(this, filter);
            }
            mAlarms.append(id, alarm);
        }
        setAlarm(alarm, triggerTime);
    }

    public synchronized void stopSchedule(Callback callback)
    {
        Alarm alarm = findAlarm(callback);
        if (alarm != null)
        {
            cancelAlarm(alarm);
        }
    }

    public synchronized void stopAll()
    {
        for (int i = 0; i < mAlarms.size(); i++)
        {
            Alarm alarm = mAlarms.valueAt(i);
            cancelAlarm(alarm);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        int id = intent.getIntExtra("id", 0);
        Alarm alarm = mAlarms.get(id);
        if (alarm == null)
        {
            return;
        }
        sExecutor.execute(new Worker(alarm));
    }

    private class Worker implements Runnable
    {
        private final Alarm mAlarm;

        public Worker(Alarm alarm)
        {
            mAlarm = alarm;
        }

        public void run()
        {
            mWakeLock.acquire();
            try
            {

                LogUtil.d("AndroidHeartBeatService", "-----------> sendHeartbeat");

                Callback callback = mAlarm.mCallback;
                long nextSchedule = callback.doSchedule();
                if (nextSchedule <= 0)
                {
                    cancelAlarm(mAlarm);
                }
                else
                {
                    setAlarm(mAlarm, nextSchedule);
                }
            }
            finally
            {
                mWakeLock.release();
            }
        }
    }

    private Alarm findAlarm(Callback callback)
    {
        for (int i = 0; i < mAlarms.size(); i++)
        {
            Alarm alarm = mAlarms.valueAt(i);
            if (alarm.mCallback == callback)
            {
                return alarm;
            }
        }
        return null;
    }

    synchronized void setAlarm(Alarm alarm, long offset)
    {
        long triggerAtTime = SystemClock.elapsedRealtime() + offset;
        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime,
                alarm.mAlaramSender);
    }

    synchronized void cancelAlarm(Alarm alarm)
    {
        mAlarmManager.cancel(alarm.mAlaramSender);
        int index = mAlarms.indexOfValue(alarm);
        if (index >= 0)
        {
            mAlarms.delete(mAlarms.keyAt(index));
        }

        // Unregister the BroadcastReceiver if there isn't a alarm anymore.
        if (mAlarms.size() == 0)
        {
            mContext.unregisterReceiver(this);
        }
    }

    private static int sNextId = 0;

    private static synchronized int nextId()
    {
        return sNextId++;
    }
}
