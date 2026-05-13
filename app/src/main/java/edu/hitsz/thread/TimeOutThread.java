package edu.hitsz.thread;

public class TimeOutThread extends Thread {
    private final Object lock = new Object();
    private long expireAt; // 目标执行的绝对时间戳
    private final Runnable onExpire;

    public TimeOutThread(long durationMs, Runnable onExpire) {
        this.expireAt = System.currentTimeMillis() + durationMs;
        this.onExpire = onExpire;
        setDaemon(true);
    }

    public void extend(long durationMs) {
        synchronized (lock) {
            expireAt = Math.max(expireAt, System.currentTimeMillis()) + durationMs;
            lock.notify();
        }
    }

    @Override
    public void run() {
        synchronized (lock) {
            while (true) {
                long remain = expireAt - System.currentTimeMillis();
                if (remain <= 0) {
                    break;
                }
                try {
                    lock.wait(remain); // 等待剩余时间
                } catch (InterruptedException e) {
                    interrupt();
                    return;
                }
            }
        }
        onExpire.run();
    }
}
