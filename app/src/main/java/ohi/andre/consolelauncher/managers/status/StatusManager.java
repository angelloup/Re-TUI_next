package ohi.andre.consolelauncher.managers.status;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

public abstract class StatusManager {
    protected final Context context;
    protected final Handler handler;
    private final Runnable updateRunnable;
    protected long delay;
    protected boolean running = false;

    public StatusManager(Context context, long delay) {
        this.context = context;
        this.delay = delay;
        this.handler = new Handler(Looper.getMainLooper());
        this.updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (running) {
                    update();
                    handler.postDelayed(this, StatusManager.this.delay);
                }
            }
        };
    }

    public void start() {
        if (!running) {
            running = true;
            handler.post(updateRunnable);
        }
    }

    public void stop() {
        running = false;
        handler.removeCallbacks(updateRunnable);
    }

    protected abstract void update();
}
