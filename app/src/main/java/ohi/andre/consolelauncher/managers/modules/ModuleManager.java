package ohi.andre.consolelauncher.managers.modules;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class ModuleManager {

    public static final String MUSIC = "music";
    public static final String NOTIFICATIONS = "notifications";
    public static final String TIMER = "timer";
    public static final String CALENDAR = "calendar";

    private static final String PREFS = "retui_modules";
    private static final String KEY_DOCK = "dock";
    private static final String KEY_SCRIPT_IDS = "script_ids";
    private static final String KEY_SCRIPT_PREFIX = "script_";
    private static final String KEY_SCRIPT_PATH_PREFIX = "script_path_";
    private static final List<String> BUILT_INS = Arrays.asList(MUSIC, NOTIFICATIONS, TIMER, CALENDAR);

    private ModuleManager() {}

    public static List<String> getBuiltIns() {
        return new ArrayList<>(BUILT_INS);
    }

    public static List<String> getDock(Context context) {
        String raw = prefs(context).getString(KEY_DOCK, null);
        if (TextUtils.isEmpty(raw)) {
            return new ArrayList<>(BUILT_INS);
        }
        return parseList(raw);
    }

    public static void setDock(Context context, List<String> modules) {
        LinkedHashSet<String> valid = new LinkedHashSet<>();
        for (String module : modules) {
            String id = normalize(module);
            if (isKnown(context, id)) {
                valid.add(id);
            }
        }
        prefs(context).edit().putString(KEY_DOCK, TextUtils.join(",", valid)).apply();
    }

    public static void addToDock(Context context, List<String> modules) {
        LinkedHashSet<String> dock = new LinkedHashSet<>(getDock(context));
        for (String module : modules) {
            String id = normalize(module);
            if (isKnown(context, id)) {
                dock.add(id);
            }
        }
        setDock(context, new ArrayList<>(dock));
    }

    public static void removeFromDock(Context context, List<String> modules) {
        LinkedHashSet<String> dock = new LinkedHashSet<>(getDock(context));
        for (String module : modules) {
            dock.remove(normalize(module));
        }
        setDock(context, new ArrayList<>(dock));
    }

    public static void hideFromDock(Context context, String module) {
        String id = normalize(module);
        List<String> dock = getDock(context);
        dock.remove(id);
        setDock(context, dock);
    }

    public static boolean isKnown(Context context, String module) {
        String id = normalize(module);
        return BUILT_INS.contains(id) || getScriptIds(context).contains(id);
    }

    public static List<String> listAll(Context context) {
        LinkedHashSet<String> all = new LinkedHashSet<>(BUILT_INS);
        all.addAll(getScriptIds(context));
        return new ArrayList<>(all);
    }

    public static void setScriptText(Context context, String module, String text) {
        String id = normalize(module);
        if (TextUtils.isEmpty(id)) {
            return;
        }
        LinkedHashSet<String> ids = new LinkedHashSet<>(getScriptIds(context));
        ids.add(id);
        prefs(context).edit()
                .putStringSet(KEY_SCRIPT_IDS, ids)
                .putString(KEY_SCRIPT_PREFIX + id, text == null ? "" : text)
                .apply();
    }

    public static void setScriptModule(Context context, String module, String path) {
        String id = normalize(module);
        if (TextUtils.isEmpty(id) || TextUtils.isEmpty(path)) {
            return;
        }
        LinkedHashSet<String> ids = new LinkedHashSet<>(getScriptIds(context));
        ids.add(id);
        prefs(context).edit()
                .putStringSet(KEY_SCRIPT_IDS, ids)
                .putString(KEY_SCRIPT_PATH_PREFIX + id, normalizeScriptPath(path))
                .putString(KEY_SCRIPT_PREFIX + id, "No module output yet. Run module -refresh " + id)
                .apply();
    }

    public static void removeScriptModule(Context context, String module) {
        String id = normalize(module);
        if (TextUtils.isEmpty(id) || BUILT_INS.contains(id)) {
            return;
        }
        LinkedHashSet<String> ids = new LinkedHashSet<>(getScriptIds(context));
        ids.remove(id);
        LinkedHashSet<String> dock = new LinkedHashSet<>(getDock(context));
        dock.remove(id);
        prefs(context).edit()
                .putStringSet(KEY_SCRIPT_IDS, ids)
                .putString(KEY_DOCK, TextUtils.join(",", dock))
                .remove(KEY_SCRIPT_PREFIX + id)
                .remove(KEY_SCRIPT_PATH_PREFIX + id)
                .apply();
    }

    public static String getScriptText(Context context, String module) {
        String id = normalize(module);
        return prefs(context).getString(KEY_SCRIPT_PREFIX + id, null);
    }

    public static String getScriptPath(Context context, String module) {
        String id = normalize(module);
        return prefs(context).getString(KEY_SCRIPT_PATH_PREFIX + id, "");
    }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String lower = value.trim().toLowerCase(Locale.US);
        String id = lower.replaceAll("[^a-z0-9_-]", "");
        if ("notif".equals(id) || "notification".equals(id)) {
            return NOTIFICATIONS;
        }
        if ("cal".equals(id)) {
            return CALENDAR;
        }
        return id;
    }

    public static String displayName(String module) {
        String id = normalize(module);
        if (NOTIFICATIONS.equals(id)) {
            return "NOTIFICATIONS";
        }
        return id.toUpperCase(Locale.US);
    }

    private static List<String> parseList(String raw) {
        ArrayList<String> out = new ArrayList<>();
        for (String part : raw.split(",")) {
            String id = normalize(part);
            if (!TextUtils.isEmpty(id) && !out.contains(id)) {
                out.add(id);
            }
        }
        return out;
    }

    private static String normalizeScriptPath(String path) {
        String trimmed = path == null ? "" : path.trim();
        if (trimmed.toLowerCase(Locale.US).startsWith("termux:")) {
            return trimmed.substring("termux:".length()).trim();
        }
        return trimmed;
    }

    private static Set<String> getScriptIds(Context context) {
        return prefs(context).getStringSet(KEY_SCRIPT_IDS, new LinkedHashSet<>());
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
