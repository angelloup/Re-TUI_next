package ohi.andre.consolelauncher.managers.modules;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;

import ohi.andre.consolelauncher.UIManager;
import ohi.andre.consolelauncher.managers.TerminalManager;
import ohi.andre.consolelauncher.managers.notifications.reply.ReplyManager;
import ohi.andre.consolelauncher.tuils.Tuils;

public final class ModulePromptManager {

    private static final String PREFS = "retui_module_prompt";
    private static final String KEY_ACTIVE = "active";
    private static final String KEY_MODULE = "module";
    private static final String KEY_FLOW = "flow";
    private static final String KEY_STEP = "step";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DATE = "date";
    private static final String KEY_TIME = "time";
    private static final String KEY_EDIT_ID = "edit_id";
    private static final String KEY_PACKAGE = "package";
    private static final String KEY_APP_NAME = "app_name";

    private ModulePromptManager() {}

    public static boolean isActive(Context context) {
        return prefs(context).getBoolean(KEY_ACTIVE, false);
    }

    public static boolean isNotificationReplyActive(Context context) {
        SharedPreferences prefs = prefs(context);
        return prefs.getBoolean(KEY_ACTIVE, false)
                && ModuleManager.NOTIFICATIONS.equals(prefs.getString(KEY_MODULE, ""))
                && "reply".equals(prefs.getString(KEY_FLOW, ""));
    }

    public static String getNotificationReplyPackage(Context context) {
        return prefs(context).getString(KEY_PACKAGE, "");
    }

    public static void startReminderAdd(Context context) {
        prefs(context).edit()
                .clear()
                .putBoolean(KEY_ACTIVE, true)
                .putString(KEY_MODULE, ModuleManager.REMINDER)
                .putString(KEY_FLOW, "add")
                .putString(KEY_STEP, "title")
                .apply();
        prompt(context, "What do you want to be reminded about?");
    }

    public static void startReminderEdit(Context context) {
        prefs(context).edit()
                .clear()
                .putBoolean(KEY_ACTIVE, true)
                .putString(KEY_MODULE, ModuleManager.REMINDER)
                .putString(KEY_FLOW, "edit_select")
                .putString(KEY_STEP, "select")
                .apply();
        prompt(context, "Which reminder do you want to edit?\n" + ReminderManager.formatList(context));
    }

    public static void startReminderRemove(Context context) {
        prefs(context).edit()
                .clear()
                .putBoolean(KEY_ACTIVE, true)
                .putString(KEY_MODULE, ModuleManager.REMINDER)
                .putString(KEY_FLOW, "remove")
                .putString(KEY_STEP, "select")
                .apply();
        prompt(context, "Which reminder do you want to remove?\n" + ReminderManager.formatList(context));
    }

    public static void startNotificationReply(Context context, String pkg, String appName) {
        if (TextUtils.isEmpty(pkg)) {
            Tuils.sendOutput(context, "No notification selected.");
            return;
        }
        String label = TextUtils.isEmpty(appName) ? pkg : appName;
        Log.i("RetuiReplyDebug", "module reply prompt started pkg=" + pkg + " label=" + label);
        prefs(context).edit()
                .clear()
                .putBoolean(KEY_ACTIVE, true)
                .putString(KEY_MODULE, ModuleManager.NOTIFICATIONS)
                .putString(KEY_FLOW, "reply")
                .putString(KEY_STEP, "text")
                .putString(KEY_PACKAGE, pkg)
                .putString(KEY_APP_NAME, label)
                .apply();
        prompt(context, "Reply to " + label + ":");
    }

    public static boolean handleInput(Context context, String input) {
        if (!isActive(context)) return false;
        SharedPreferences prefs = prefs(context);
        String module = prefs.getString(KEY_MODULE, "");
        if (ModuleManager.NOTIFICATIONS.equals(module)) {
            return handleNotificationInput(context, prefs, input);
        }
        if (!ModuleManager.REMINDER.equals(module)) return false;

        String value = input == null ? "" : input.trim();
        if ("cancel".equalsIgnoreCase(value)) {
            clear(context, "Module prompt cancelled.");
            return true;
        }

        String flow = prefs.getString(KEY_FLOW, "");
        String step = prefs.getString(KEY_STEP, "");

        if ("add".equals(flow)) {
            return handleAdd(context, prefs, step, value);
        }
        if ("edit_select".equals(flow) || "edit".equals(flow)) {
            return handleEdit(context, prefs, flow, step, value);
        }
        if ("remove".equals(flow)) {
            return handleRemove(context, prefs, step, value);
        }

        clear(context, "Unknown module prompt.");
        return true;
    }

    public static List<ModuleManager.ModuleSuggestion> getSuggestions(Context context) {
        ArrayList<ModuleManager.ModuleSuggestion> suggestions = new ArrayList<>();
        if (!isActive(context)) return suggestions;
        String step = prefs(context).getString(KEY_STEP, "");
        if ("confirm".equals(step)) {
            suggestions.add(ModuleManager.ModuleSuggestion.command("save", "save"));
            suggestions.add(ModuleManager.ModuleSuggestion.command("edit", "edit"));
            suggestions.add(ModuleManager.ModuleSuggestion.command("cancel", "cancel"));
        } else {
            suggestions.add(ModuleManager.ModuleSuggestion.command("cancel", "cancel"));
        }
        return suggestions;
    }

    private static boolean handleNotificationInput(Context context, SharedPreferences prefs, String input) {
        String value = input == null ? "" : input.trim();
        if ("cancel".equalsIgnoreCase(value)) {
            clear(context, "Notification reply cancelled.");
            return true;
        }
        if (TextUtils.isEmpty(value)) {
            prompt(context, "Reply text cannot be empty. Type a reply or cancel.");
            return true;
        }

        Intent intent = new Intent(ReplyManager.ACTION);
        intent.putExtra(ReplyManager.ID, prefs.getString(KEY_PACKAGE, ""));
        intent.putExtra(ReplyManager.WHAT, value);
        Log.i("RetuiReplyDebug", "module reply input captured pkg="
                + prefs.getString(KEY_PACKAGE, "")
                + " text=" + value);
        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
        clear(context, "Reply sent to " + prefs.getString(KEY_APP_NAME, prefs.getString(KEY_PACKAGE, "")) + ".");
        return true;
    }

    private static boolean handleAdd(Context context, SharedPreferences prefs, String step, String value) {
        if ("title".equals(step)) {
            if (TextUtils.isEmpty(value)) {
                prompt(context, "Reminder text cannot be empty. What do you want to be reminded about?");
                return true;
            }
            prefs.edit().putString(KEY_TITLE, value).putString(KEY_STEP, "date").apply();
            prompt(context, "What date?\nAccepted: 10/05/2026 or 2026-05-10");
            return true;
        }
        if ("date".equals(step)) {
            prefs.edit().putString(KEY_DATE, value).putString(KEY_STEP, "time").apply();
            prompt(context, "What time?\nAccepted: 11:30PM or 23:30");
            return true;
        }
        if ("time".equals(step)) {
            prefs.edit().putString(KEY_TIME, value).putString(KEY_STEP, "confirm").apply();
            confirm(context, prefs);
            return true;
        }
        if ("confirm".equals(step)) {
            if ("edit".equalsIgnoreCase(value)) {
                prefs.edit().putString(KEY_STEP, "title").apply();
                prompt(context, "What do you want to be reminded about?");
            } else if ("save".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) || "confirm".equalsIgnoreCase(value)) {
                saveNewReminder(context, prefs);
            } else {
                prompt(context, "Type save, edit, or cancel.");
            }
            return true;
        }
        return true;
    }

    private static boolean handleEdit(Context context, SharedPreferences prefs, String flow, String step, String value) {
        if ("edit_select".equals(flow)) {
            ReminderManager.Reminder reminder = ReminderManager.get(context, value);
            if (reminder == null) {
                prompt(context, "Reminder not found. Enter a list number or type cancel.\n" + ReminderManager.formatList(context));
                return true;
            }
            prefs.edit()
                    .putString(KEY_FLOW, "edit")
                    .putString(KEY_STEP, "title")
                    .putString(KEY_EDIT_ID, reminder.id)
                    .putString(KEY_TITLE, reminder.title)
                    .putString(KEY_DATE, new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US).format(new java.util.Date(reminder.atMillis)))
                    .putString(KEY_TIME, new java.text.SimpleDateFormat("h:mma", java.util.Locale.US).format(new java.util.Date(reminder.atMillis)))
                    .apply();
            prompt(context, "Current reminder: " + reminder.title + "\nNew text? Press enter to keep.");
            return true;
        }

        if ("title".equals(step)) {
            SharedPreferences.Editor editor = prefs.edit().putString(KEY_STEP, "date");
            if (!TextUtils.isEmpty(value)) editor.putString(KEY_TITLE, value);
            editor.apply();
            prompt(context, "New date? Press enter to keep.\nCurrent: " + prefs.getString(KEY_DATE, ""));
            return true;
        }
        if ("date".equals(step)) {
            SharedPreferences.Editor editor = prefs.edit().putString(KEY_STEP, "time");
            if (!TextUtils.isEmpty(value)) editor.putString(KEY_DATE, value);
            editor.apply();
            prompt(context, "New time? Press enter to keep.\nCurrent: " + prefs.getString(KEY_TIME, ""));
            return true;
        }
        if ("time".equals(step)) {
            SharedPreferences.Editor editor = prefs.edit().putString(KEY_STEP, "confirm");
            if (!TextUtils.isEmpty(value)) editor.putString(KEY_TIME, value);
            editor.apply();
            confirm(context, prefs);
            return true;
        }
        if ("confirm".equals(step)) {
            if ("edit".equalsIgnoreCase(value)) {
                prefs.edit().putString(KEY_STEP, "title").apply();
                prompt(context, "New text? Press enter to keep.");
            } else if ("save".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) || "confirm".equalsIgnoreCase(value)) {
                saveEditedReminder(context, prefs);
            } else {
                prompt(context, "Type save, edit, or cancel.");
            }
        }
        return true;
    }

    private static boolean handleRemove(Context context, SharedPreferences prefs, String step, String value) {
        if ("select".equals(step)) {
            ReminderManager.Reminder reminder = ReminderManager.get(context, value);
            if (reminder == null) {
                prompt(context, "Reminder not found. Enter a list number or type cancel.\n" + ReminderManager.formatList(context));
                return true;
            }
            prefs.edit().putString(KEY_EDIT_ID, reminder.id).putString(KEY_STEP, "confirm").apply();
            prompt(context, "Remove this reminder?\n" + reminder.title + "\n" + ReminderManager.formatWhen(reminder.atMillis) + "\nType save to remove, or cancel.");
            return true;
        }
        if ("confirm".equals(step)) {
            if ("save".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) || "confirm".equalsIgnoreCase(value)) {
                String id = prefs.getString(KEY_EDIT_ID, "");
                ReminderManager.remove(context, id);
                clear(context, "Reminder removed.");
                refreshReminder(context);
            } else {
                prompt(context, "Type save to remove, or cancel.");
            }
        }
        return true;
    }

    private static void saveNewReminder(Context context, SharedPreferences prefs) {
        Long at = ReminderManager.parseDateTime(prefs.getString(KEY_DATE, ""), prefs.getString(KEY_TIME, ""));
        if (at == null) {
            prefs.edit().putString(KEY_STEP, "date").apply();
            prompt(context, "I could not parse that date/time. What date?");
            return;
        }
        if (at <= System.currentTimeMillis()) {
            prefs.edit().putString(KEY_STEP, "date").apply();
            prompt(context, "That reminder time is in the past. What date?");
            return;
        }
        ReminderManager.Reminder reminder = ReminderManager.add(context, prefs.getString(KEY_TITLE, ""), at);
        clear(context, "Reminder saved:\n" + reminder.title + "\n" + ReminderManager.formatWhen(reminder.atMillis));
        refreshReminder(context);
    }

    private static void saveEditedReminder(Context context, SharedPreferences prefs) {
        Long at = ReminderManager.parseDateTime(prefs.getString(KEY_DATE, ""), prefs.getString(KEY_TIME, ""));
        if (at == null) {
            prefs.edit().putString(KEY_STEP, "date").apply();
            prompt(context, "I could not parse that date/time. What date?");
            return;
        }
        if (at <= System.currentTimeMillis()) {
            prefs.edit().putString(KEY_STEP, "date").apply();
            prompt(context, "That reminder time is in the past. What date?");
            return;
        }
        ReminderManager.Reminder reminder = new ReminderManager.Reminder(
                prefs.getString(KEY_EDIT_ID, ""),
                prefs.getString(KEY_TITLE, ""),
                at);
        ReminderManager.save(context, reminder);
        clear(context, "Reminder updated:\n" + reminder.title + "\n" + ReminderManager.formatWhen(reminder.atMillis));
        refreshReminder(context);
    }

    private static void confirm(Context context, SharedPreferences prefs) {
        Long at = ReminderManager.parseDateTime(prefs.getString(KEY_DATE, ""), prefs.getString(KEY_TIME, ""));
        String when = at == null ? prefs.getString(KEY_DATE, "") + " " + prefs.getString(KEY_TIME, "") : ReminderManager.formatWhen(at);
        prompt(context, "Reminder:\n"
                + prefs.getString(KEY_TITLE, "") + "\n"
                + when + "\n"
                + "Type save, edit, or cancel.");
    }

    private static void prompt(Context context, String message) {
        Tuils.sendOutput(context, message, TerminalManager.CATEGORY_OUTPUT);
        refreshReminder(context);
    }

    private static void clear(Context context, String message) {
        prefs(context).edit().clear().apply();
        Tuils.sendOutput(context, message, TerminalManager.CATEGORY_OUTPUT);
    }

    private static void refreshReminder(Context context) {
        Intent update = new Intent(UIManager.ACTION_MODULE_COMMAND);
        update.putExtra(UIManager.EXTRA_MODULE_COMMAND, "update");
        update.putExtra(UIManager.EXTRA_MODULE_NAME, ModuleManager.REMINDER);
        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(update);
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
