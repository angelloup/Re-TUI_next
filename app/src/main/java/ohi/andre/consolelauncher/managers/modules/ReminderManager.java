package ohi.andre.consolelauncher.managers.modules;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class ReminderManager {

    public static final String EXTRA_ID = "reminder_id";
    public static final String EXTRA_TITLE = "reminder_title";
    public static final String EXTRA_AT = "reminder_at";

    private static final String PREFS = "retui_reminders";
    private static final String KEY_IDS = "ids";
    private static final String KEY_TITLE_PREFIX = "title_";
    private static final String KEY_AT_PREFIX = "at_";

    private ReminderManager() {}

    public static Reminder add(Context context, String title, long atMillis) {
        String id = String.valueOf(System.currentTimeMillis());
        Reminder reminder = new Reminder(id, title, atMillis);
        save(context, reminder);
        schedule(context, reminder);
        return reminder;
    }

    public static void save(Context context, Reminder reminder) {
        if (reminder == null || TextUtils.isEmpty(reminder.id)) return;
        ArrayList<String> ids = new ArrayList<>(ids(context));
        if (!ids.contains(reminder.id)) {
            ids.add(reminder.id);
        }
        prefs(context).edit()
                .putString(KEY_IDS, TextUtils.join(",", ids))
                .putString(KEY_TITLE_PREFIX + reminder.id, safe(reminder.title))
                .putLong(KEY_AT_PREFIX + reminder.id, reminder.atMillis)
                .apply();
        schedule(context, reminder);
    }

    public static void remove(Context context, String id) {
        String clean = safe(id);
        if (TextUtils.isEmpty(clean)) return;
        ArrayList<String> ids = new ArrayList<>(ids(context));
        ids.remove(clean);
        cancel(context, clean);
        prefs(context).edit()
                .putString(KEY_IDS, TextUtils.join(",", ids))
                .remove(KEY_TITLE_PREFIX + clean)
                .remove(KEY_AT_PREFIX + clean)
                .apply();
    }

    public static Reminder get(Context context, String idOrIndex) {
        String key = resolveId(context, idOrIndex);
        if (TextUtils.isEmpty(key)) return null;
        SharedPreferences prefs = prefs(context);
        if (!ids(context).contains(key)) return null;
        return new Reminder(key, prefs.getString(KEY_TITLE_PREFIX + key, ""), prefs.getLong(KEY_AT_PREFIX + key, 0L));
    }

    public static List<Reminder> list(Context context) {
        ArrayList<Reminder> reminders = new ArrayList<>();
        SharedPreferences prefs = prefs(context);
        for (String id : ids(context)) {
            reminders.add(new Reminder(id, prefs.getString(KEY_TITLE_PREFIX + id, ""), prefs.getLong(KEY_AT_PREFIX + id, 0L)));
        }
        Collections.sort(reminders, (left, right) -> Long.compare(left.atMillis, right.atMillis));
        return reminders;
    }

    public static String formatList(Context context) {
        List<Reminder> reminders = list(context);
        if (reminders.isEmpty()) {
            return "No reminders.";
        }
        StringBuilder out = new StringBuilder();
        int index = 1;
        for (Reminder reminder : reminders) {
            if (out.length() > 0) out.append('\n');
            out.append(index++)
                    .append(". ")
                    .append(reminder.title)
                    .append(" @ ")
                    .append(formatWhen(reminder.atMillis));
        }
        return out.toString();
    }

    public static String resolveId(Context context, String idOrIndex) {
        String raw = safe(idOrIndex);
        if (TextUtils.isEmpty(raw)) return "";
        List<Reminder> reminders = list(context);
        try {
            int index = Integer.parseInt(raw);
            if (index >= 1 && index <= reminders.size()) {
                return reminders.get(index - 1).id;
            }
        } catch (Exception ignored) {
        }
        for (Reminder reminder : reminders) {
            if (reminder.id.equals(raw)) return reminder.id;
        }
        return "";
    }

    public static Long parseDateTime(String date, String time) {
        String value = safe(date) + " " + safe(time).toUpperCase(Locale.US).replace(".", "");
        String[] patterns = {
                "dd/MM/yyyy h:mma",
                "dd/MM/yyyy hh:mma",
                "dd/MM/yyyy HH:mm",
                "dd-MM-yyyy h:mma",
                "dd-MM-yyyy HH:mm",
                "yyyy-MM-dd h:mma",
                "yyyy-MM-dd HH:mm"
        };
        for (String pattern : patterns) {
            try {
                SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
                format.setLenient(false);
                Date parsed = format.parse(value);
                if (parsed != null) return parsed.getTime();
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    public static String formatWhen(long atMillis) {
        if (atMillis <= 0L) return "unscheduled";
        return new SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.US).format(new Date(atMillis));
    }

    private static void schedule(Context context, Reminder reminder) {
        if (reminder == null || reminder.atMillis <= System.currentTimeMillis()) return;
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarm == null) return;
        PendingIntent pendingIntent = pendingIntent(context, reminder.id, reminder.title, reminder.atMillis, PendingIntent.FLAG_UPDATE_CURRENT);
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.atMillis, pendingIntent);
    }

    private static void cancel(Context context, String id) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarm == null) return;
        PendingIntent pendingIntent = pendingIntent(context, id, "", 0L, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            alarm.cancel(pendingIntent);
        }
    }

    private static PendingIntent pendingIntent(Context context, String id, String title, long atMillis, int flag) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_AT, atMillis);
        int requestCode = Math.abs(id.hashCode());
        return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE | flag);
    }

    private static List<String> ids(Context context) {
        return parseIds(prefs(context).getString(KEY_IDS, ""));
    }

    private static List<String> parseIds(String raw) {
        ArrayList<String> ids = new ArrayList<>();
        if (TextUtils.isEmpty(raw)) return ids;
        for (String part : raw.split(",")) {
            String id = safe(part);
            if (!TextUtils.isEmpty(id) && !ids.contains(id)) ids.add(id);
        }
        return ids;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static final class Reminder {
        public final String id;
        public final String title;
        public final long atMillis;

        Reminder(String id, String title, long atMillis) {
            this.id = id;
            this.title = title;
            this.atMillis = atMillis;
        }
    }
}
