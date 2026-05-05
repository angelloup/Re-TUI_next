package ohi.andre.consolelauncher.managers.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import java.util.ArrayList;
import java.util.Collections;

import ohi.andre.consolelauncher.managers.TerminalManager;
import ohi.andre.consolelauncher.tuils.Tuils;

public class DevReplyReceiver extends BroadcastReceiver {

    public static final String ACTION_DEV_REPLY = "ohi.andre.consolelauncher.DEV_REPLY";
    public static final String RESULT_KEY = "retui_dev_reply_text";
    public static final int NOTIFICATION_ID = 201;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle results = RemoteInput.getResultsFromIntent(intent);
        CharSequence reply = results == null ? null : results.getCharSequence(RESULT_KEY);
        Log.i("RetuiReplyDebug", "dev reply received action="
                + (intent == null ? "null" : intent.getAction())
                + " reply=" + (reply == null ? "<null>" : reply)
                + " remoteInputKeys=" + bundleKeys(results)
                + " intentExtraKeys=" + bundleKeys(intent == null ? null : intent.getExtras()));

        StringBuilder out = new StringBuilder("DEVUTILS NOTIFY REPLY");
        out.append(Tuils.NEWLINE)
                .append("action: ")
                .append(intent == null ? "null" : intent.getAction())
                .append(Tuils.NEWLINE)
                .append("reply: ")
                .append(reply == null ? "<null>" : reply);

        out.append(Tuils.NEWLINE)
                .append("remote_input_keys: ")
                .append(bundleKeys(results));

        Bundle extras = intent == null ? null : intent.getExtras();
        out.append(Tuils.NEWLINE)
                .append("intent_extra_keys: ")
                .append(bundleKeys(extras));

        Tuils.sendOutput(context.getApplicationContext(), out.toString(), TerminalManager.CATEGORY_OUTPUT);
        NotificationManagerCompat.from(context.getApplicationContext()).cancel(NOTIFICATION_ID);
    }

    private static String bundleKeys(Bundle bundle) {
        if (bundle == null || bundle.isEmpty()) {
            return "[]";
        }
        ArrayList<String> keys = new ArrayList<>(bundle.keySet());
        Collections.sort(keys);
        return keys.toString();
    }
}
