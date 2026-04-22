package ohi.andre.consolelauncher.managers.settings;

import ohi.andre.consolelauncher.managers.xml.options.Notifications;

public final class NotificationSettings {

    private NotificationSettings() {}

    public static boolean showTerminal() {
        return LauncherSettings.getBoolean(Notifications.show_notifications)
                || "enabled".equalsIgnoreCase(LauncherSettings.get(Notifications.show_notifications));
    }

    public static boolean printToOutput() {
        return LauncherSettings.getBoolean(Notifications.terminal_notifications);
    }

    public static String format() {
        return LauncherSettings.get(Notifications.notification_format);
    }

    public static int defaultColor() {
        return LauncherSettings.getColor(Notifications.default_notification_color);
    }

    public static String defaultColorRaw() {
        return LauncherSettings.get(Notifications.default_notification_color);
    }

    public static boolean appNotificationsEnabledByDefault() {
        return LauncherSettings.getBoolean(Notifications.app_notification_enabled_default);
    }

    public static boolean clickOpensNotification() {
        return LauncherSettings.getBoolean(Notifications.click_notification);
    }

    public static boolean longClickOpensNotificationActions() {
        return LauncherSettings.getBoolean(Notifications.long_click_notification);
    }

    public static boolean showExcludeAppPopupAction() {
        return LauncherSettings.getBoolean(Notifications.notification_popup_exclude_app);
    }

    public static boolean showExcludeNotificationPopupAction() {
        return LauncherSettings.getBoolean(Notifications.notification_popup_exclude_notification);
    }

    public static boolean showReplyPopupAction() {
        return LauncherSettings.getBoolean(Notifications.notification_popup_reply);
    }
}
