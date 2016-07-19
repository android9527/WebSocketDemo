package org.websocket.demo.scheduletask;

public interface ScheduleTask
{
    public interface Callback
    {
        /**
         * Called on heartbeat schedule.
         *
         * @return the offset in milliseconds that the method wants to
         * be called the next time. Return 0 or negative value indicates to stop
         * the schedule of this callback.
         */
        public long doSchedule();
    }

    /**
     * Start to schedule a heartbeat operation.
     *
     * @param callback The operation wants to be called repeat.
     * @param triggerTime The time(in milliseconds) until the operation
     *            will be executed the first time.
     */
    public void startSchedule(Callback callback, long triggerTime);

    /**
     * Stop scheduling a heartbeat operation.
     *
     * @param callback The operation will be stopped.
     */
    public void stopSchedule(Callback callback);
}
