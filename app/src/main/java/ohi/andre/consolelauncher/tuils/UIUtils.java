package ohi.andre.consolelauncher.tuils;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.io.File;

import ohi.andre.consolelauncher.managers.settings.AppearanceSettings;

public class UIUtils {

    public static Typeface globalTypeface = null;
    public static String fontPath = null;

    public static Typeface getTypeface(Context context) {
        if (globalTypeface != null) return globalTypeface;

        boolean systemFont = AppearanceSettings.useSystemFont();
        if (systemFont) {
            globalTypeface = Typeface.MONOSPACE;
            return globalTypeface;
        }

        String fontName = AppearanceSettings.fontFile();
        if (fontName == null || fontName.length() == 0) {
            globalTypeface = Typeface.MONOSPACE;
            return globalTypeface;
        }

        File tuiFolder = Tuils.getFolder();
        File fontFile = new File(tuiFolder, fontName);

        if (fontFile.exists()) {
            try {
                globalTypeface = Typeface.createFromFile(fontFile);
                fontPath = fontFile.getAbsolutePath();
            } catch (Exception e) {
                globalTypeface = Typeface.MONOSPACE;
            }
        } else {
            globalTypeface = Typeface.MONOSPACE;
        }

        return globalTypeface;
    }

    public static void cancelFont() {
        globalTypeface = null;
        fontPath = null;
    }

    public static float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }

    public static int dpToPx(Context context, int valueInDp) {
        return (int) dpToPx(context, (float) valueInDp);
    }

    public static int convertSpToPixels(float sp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    public static SpannableString span(CharSequence text, int color) {
        if (text == null) return null;
        SpannableString ss = new SpannableString(text);
        ss.setSpan(new ForegroundColorSpan(color), 0, text.length(), 0);
        return ss;
    }

    public static SpannableString span(Context context, int size, CharSequence text) {
        return span(context, text, Integer.MAX_VALUE, size);
    }

    public static SpannableString span(Context context, CharSequence text, int color, int size) {
        return span(context, Integer.MAX_VALUE, color, text, size);
    }

    public static SpannableString span(int bgColor, int foreColor, CharSequence text) {
        return span(null, bgColor, foreColor, text, Integer.MAX_VALUE);
    }

    public static SpannableString span(Context context, int bgColor, int foreColor, CharSequence text, int size) {
        if (text == null) {
            text = Tuils.EMPTYSTRING;
        }

        SpannableString spannableString;
        if (text instanceof SpannableString) spannableString = (SpannableString) text;
        else spannableString = new SpannableString(text);

        if (size != Integer.MAX_VALUE && context != null)
            spannableString.setSpan(new AbsoluteSizeSpan(convertSpToPixels(size, context)), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (foreColor != Integer.MAX_VALUE)
            spannableString.setSpan(new ForegroundColorSpan(foreColor), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (bgColor != Integer.MAX_VALUE)
            spannableString.setSpan(new BackgroundColorSpan(bgColor), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableString;
    }

    public static int span(int bgColor, SpannableString text, String section, int fromIndex) {
        int index = text.toString().indexOf(section, fromIndex);
        if (index == -1) return index;

        text.setSpan(new BackgroundColorSpan(bgColor), index, index + section.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return index + section.length();
    }

    public static SpannableString span(Context context, String text, int color, int size) {
        SpannableString ss = new SpannableString(text);
        ss.setSpan(new ForegroundColorSpan(color), 0, text.length(), 0);
        if (size != Integer.MAX_VALUE && context != null) {
            ss.setSpan(new AbsoluteSizeSpan(convertSpToPixels(size, context)), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return ss;
    }
}
