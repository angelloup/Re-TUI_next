package ohi.andre.consolelauncher.managers.modules;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import ohi.andre.consolelauncher.LauncherActivity;
import ohi.andre.consolelauncher.R;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "retui_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        String id = intent.getStringExtra(ReminderManager.EXTRA_ID);
        String title = intent.getStringExtra(ReminderManager.EXTRA_TITLE);
        if (title == null || title.trim().length() == 0) {
            title = "Reminder";
        }

        createChannel(context);

        Intent launch = new Intent(context, LauncherActivity.class);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent content = PendingIntent.getActivity(
                context,
                id == null ? 0 : Math.abs(id.hashCode()),
                launch,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Re:T-UI reminder")
                .setContentText(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(title))
                .setContentIntent(content)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(id == null ? 4001 : Math.abs(id.hashCode()), builder.build());
        }
    }

    private void createChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null || manager.getNotificationChannel(CHANNEL_ID) != null) return;
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Re:T-UI Reminders",
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Reminder notifications created by Re:T-UI modules.");
        manager.createNotificationChannel(channel);
    }
}
