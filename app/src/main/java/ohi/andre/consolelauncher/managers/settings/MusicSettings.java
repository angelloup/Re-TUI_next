package ohi.andre.consolelauncher.managers.settings;

import android.text.TextUtils;

import ohi.andre.consolelauncher.managers.xml.options.Behavior;

public final class MusicSettings {

    private MusicSettings() {}

    public static boolean enabled() {
        return LauncherSettings.getBoolean(Behavior.enable_music);
    }

    public static boolean showWidget() {
        return LauncherSettings.getBoolean(Behavior.show_music_widget);
    }

    public static String preferredPackage() {
        return LauncherSettings.get(Behavior.preferred_music_app);
    }

    public static boolean acceptsPackage(String packageName) {
        String preferredPackage = preferredPackage();
        return TextUtils.isEmpty(preferredPackage) || preferredPackage.equals(packageName);
    }
}
