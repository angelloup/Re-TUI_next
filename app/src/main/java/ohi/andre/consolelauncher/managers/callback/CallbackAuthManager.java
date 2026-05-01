package ohi.andre.consolelauncher.managers.callback;

import android.content.Context;
import android.content.SharedPreferences;

import java.security.SecureRandom;

public final class CallbackAuthManager {

    private static final String PREFS = "retui_callback_auth";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_TOKEN = "token";
    private static final int TOKEN_BYTES = 32;

    private CallbackAuthManager() {}

    public static boolean isEnabled(Context context) {
        SharedPreferences prefs = prefs(context);
        return prefs.getBoolean(KEY_ENABLED, false) && !prefs.getString(KEY_TOKEN, "").isEmpty();
    }

    public static String getToken(Context context) {
        return prefs(context).getString(KEY_TOKEN, "");
    }

    public static String getOrCreateToken(Context context) {
        SharedPreferences prefs = prefs(context);
        String token = prefs.getString(KEY_TOKEN, "");
        if (token == null || token.isEmpty()) {
            token = newToken();
            prefs.edit()
                    .putString(KEY_TOKEN, token)
                    .putBoolean(KEY_ENABLED, true)
                    .apply();
        }
        return token;
    }

    public static String rotateToken(Context context) {
        String token = newToken();
        prefs(context).edit()
                .putString(KEY_TOKEN, token)
                .putBoolean(KEY_ENABLED, true)
                .apply();
        return token;
    }

    public static void setEnabled(Context context, boolean enabled) {
        SharedPreferences.Editor editor = prefs(context).edit().putBoolean(KEY_ENABLED, enabled);
        if (enabled && getToken(context).isEmpty()) {
            editor.putString(KEY_TOKEN, newToken());
        }
        editor.apply();
    }

    public static boolean isAuthorized(Context context, String candidate) {
        if (!isEnabled(context) || candidate == null) {
            return false;
        }
        return constantTimeEquals(getToken(context), candidate);
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static String newToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        new SecureRandom().nextBytes(bytes);
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            builder.append(String.format("%02x", b & 0xff));
        }
        return builder.toString();
    }

    private static boolean constantTimeEquals(String expected, String candidate) {
        if (expected == null || candidate == null) {
            return false;
        }
        int diff = expected.length() ^ candidate.length();
        int length = Math.min(expected.length(), candidate.length());
        for (int i = 0; i < length; i++) {
            diff |= expected.charAt(i) ^ candidate.charAt(i);
        }
        return diff == 0;
    }
}
