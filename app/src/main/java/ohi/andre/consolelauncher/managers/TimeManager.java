package ohi.andre.consolelauncher.managers;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.options.Behavior;
import ohi.andre.consolelauncher.managers.xml.options.Theme;
import ohi.andre.consolelauncher.tuils.SimpleMutableEntry;
import ohi.andre.consolelauncher.tuils.Tuils;

/**
 * Created by francescoandreuzzi on 26/07/2017.
 */

public class TimeManager {

    Map.Entry<Integer, SimpleDateFormat>[] outputDateFormatList;
    Map.Entry<Integer, SimpleDateFormat>[] statusDateFormatList;

    public static Pattern extractor = Pattern.compile("%t([0-9]*)", Pattern.CASE_INSENSITIVE);

    public static TimeManager instance;

    public TimeManager(Context context) {
        instance = this;
        String separator = XMLPrefsManager.get(Behavior.time_format_separator);

        outputDateFormatList = createList(context, XMLPrefsManager.get(Behavior.output_time_format), separator);
        statusDateFormatList = createList(context, XMLPrefsManager.get(Behavior.status_time_format), separator);

        instance = this;
    }

    private Map.Entry<Integer, SimpleDateFormat>[] createList(Context context, String format, String separator) {
        String[] formats = format.split(separator);
        Map.Entry<Integer, SimpleDateFormat>[] list = new Map.Entry[formats.length];

        Pattern colorPattern = Pattern.compile("#(?:\\d|[a-fA-F]){6}");

        for(int c = 0; c < list.length; c++) {
            try {
                formats[c] = Tuils.patternNewline.matcher(formats[c]).replaceAll(Tuils.NEWLINE);

                int color = XMLPrefsManager.getColor(Theme.time_color);
                Matcher m = colorPattern.matcher(formats[c]);
                if(m.find()) {
                    color = Color.parseColor(m.group());
                    formats[c] = m.replaceAll(Tuils.EMPTYSTRING);
                }

                list[c] = new SimpleMutableEntry<>(color, new SimpleDateFormat(formats[c]));
            } catch (Exception e) {
                Tuils.sendOutput(Color.RED, context,"Invalid time format: " + formats[c]);
                if (c > 0) list[c] = list[0];
                else list[c] = new SimpleMutableEntry<>(Color.RED, new SimpleDateFormat("HH:mm:ss"));
            }
        }
        return list;
    }

    private Map.Entry<Integer, SimpleDateFormat> get(int index, boolean isStatus) {
        Map.Entry<Integer, SimpleDateFormat>[] list = isStatus ? statusDateFormatList : outputDateFormatList;
        if(list == null || list.length == 0) return null;
        if(index < 0 || index >= list.length) index = 0;

        return list[index];
    }

    public CharSequence replace(CharSequence cs) {
        return replace(null, Integer.MAX_VALUE, cs, -1, TerminalManager.NO_COLOR, false);
    }

    public CharSequence replace(CharSequence cs, int color) {
        return replace(null, Integer.MAX_VALUE, cs, -1, color, false);
    }

    public CharSequence replace(CharSequence cs, long tm, int color) {
        return replace(null, Integer.MAX_VALUE, cs, tm, color, false);
    }

    public CharSequence replace(CharSequence cs, long tm) {
        return replace(null, Integer.MAX_VALUE, cs, tm, TerminalManager.NO_COLOR, false);
    }

    public CharSequence replace(Context context, int size, CharSequence cs) {
        return replace(context, size, cs, -1, TerminalManager.NO_COLOR, false);
    }

    public CharSequence replace(Context context, int size, CharSequence cs, int color) {
        return replace(context, size, cs, -1, color, false);
    }

    public CharSequence replace(Context context, int size, CharSequence cs, long tm, int color, boolean isStatus) {
        if(tm == -1) {
            tm = System.currentTimeMillis();
        }

        if(cs instanceof String) {
            Tuils.log(Thread.currentThread().getStackTrace());
            Tuils.log("cant span a string!", cs.toString());
        }

        Date date = new Date(tm);

        Matcher matcher = extractor.matcher(cs);
        while(matcher.find()) {
            String number = matcher.group(1);
            if(number == null || number.length() == 0) number = "0";

            Map.Entry<Integer, SimpleDateFormat> entry = get(Integer.parseInt(number), isStatus);
            if(entry == null) continue;

            CharSequence s = span(context, entry, color, date, size);
            cs = TextUtils.replace(cs, new String[] {matcher.group(0)}, new CharSequence[] {s});
        }

        Map.Entry<Integer, SimpleDateFormat> entry = get(0, isStatus);
        cs = TextUtils.replace(cs, new String[] {"%t"}, new CharSequence[] {span(context, entry, color, date, size)});

        return cs;
    }

    public CharSequence getCharSequence(String s) {
        return getCharSequence(null, Integer.MAX_VALUE, s, -1, TerminalManager.NO_COLOR, true);
    }

    public CharSequence getCharSequence(String s, int color) {
        return getCharSequence(null, Integer.MAX_VALUE, s, -1, color, true);
    }

    public CharSequence getCharSequence(String s, long tm, int color) {
        return getCharSequence(null, Integer.MAX_VALUE, s, tm, color, true);
    }

    public CharSequence getCharSequence(String s, long tm) {
        return getCharSequence(null, Integer.MAX_VALUE, s, tm, TerminalManager.NO_COLOR, true);
    }

    public CharSequence getCharSequence(Context context, int size, String s) {
        return getCharSequence(context, size, s, -1, TerminalManager.NO_COLOR, true);
    }

    public CharSequence getCharSequence(Context context, int size, String s, int color) {
        return getCharSequence(context, size, s, -1, color, true);
    }

    public CharSequence getCharSequence(Context context, int size, String s, long tm, int color, boolean isStatus) {
        if(tm == -1) {
            tm = System.currentTimeMillis();
        }

        Date date = new Date(tm);

        Matcher matcher = extractor.matcher(s);
        if(matcher.find()) {
            String number = matcher.group(1);
            if(number == null || number.length() == 0) number = "0";

            Map.Entry<Integer, SimpleDateFormat> entry = get(Integer.parseInt(number), isStatus);
            if(entry == null) {
                return null;
            }

            return span(context, entry, color, date, size);
        } else return null;
    }

    private CharSequence span(Context context, Map.Entry<Integer, SimpleDateFormat> entry, int color, Date date, int size) {
        if(entry == null) return Tuils.EMPTYSTRING;

        String tf = entry.getValue().format(date);
        int clr = color != TerminalManager.NO_COLOR ? color : entry.getKey();

        SpannableString spannableString = new SpannableString(tf);
        spannableString.setSpan(new ForegroundColorSpan(clr), 0, tf.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        if(size != Integer.MAX_VALUE && context != null) {
            spannableString.setSpan(new AbsoluteSizeSpan(Tuils.convertSpToPixels(size, context)), 0, tf.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannableString;
    }

    public void dispose() {
        outputDateFormatList = null;
        statusDateFormatList = null;

        instance = null;
    }
}
