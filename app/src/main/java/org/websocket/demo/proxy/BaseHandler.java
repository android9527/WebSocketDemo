package org.websocket.demo.proxy;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * chenfeiyue
 * 静态子类Handler可以继承 {@link org.websocket.demo.proxy.BaseHandler}，实现 {@link #onHandleMessage(android.os.Message, Object)}方法。<br/>
 * 在 {@link #onHandleMessage(android.os.Message, Object)}方法中，参数 T t 是从 WeakReference 中获取，如果已经为null则 {@link #onHandleMessage(android.os.Message, Object)}不会被调用。
 *
 * @param <T>
 */
public abstract class BaseHandler<T> extends Handler {
    private WeakReference<T> ref;

    /**
     * 构造方法。
     *
     * @param t Handler所在的对象。
     */
    public BaseHandler(T t) {
        ref = new WeakReference<T>(t);
    }

    /**
     * 可以指定Looper的构造方法。
     *
     * @param looper 指定Looper
     * @param t      Handler所在的对象。
     */
    public BaseHandler(Looper looper, T t) {
        super(looper);
        ref = new WeakReference<T>(t);
    }

    @Override
    public final void handleMessage(Message msg) {
        if (ref == null) {
            return;
        }
        T t = ref.get();
        if (t == null) {
            return;
        }
        onHandleMessage(msg, t);
    }

    /**
     * 实现此方法，处理消息。不要调用super.handleMessage(Message) 或 super.dispatchMessage(Message)，以免造成死循环。
     *
     * @param msg Handler的Message。
     * @param t   Handler构造时传入的对象。
     * @since 2014-7-20
     */
    public abstract void onHandleMessage(Message msg, T t);
}