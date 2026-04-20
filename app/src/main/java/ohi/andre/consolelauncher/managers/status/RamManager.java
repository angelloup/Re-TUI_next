package ohi.andre.consolelauncher.managers.status;

import android.app.ActivityManager;
import android.content.Context;
import java.util.regex.Matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import ohi.andre.consolelauncher.UIManager;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.options.Behavior;
import ohi.andre.consolelauncher.managers.xml.options.Theme;
import ohi.andre.consolelauncher.managers.xml.options.Ui;
import ohi.andre.consolelauncher.tuils.SystemUtils;
import ohi.andre.consolelauncher.tuils.Tuils;
import ohi.andre.consolelauncher.tuils.UIUtils;

public class RamManager extends StatusManager {

    private final String AV = "%av";
    private final String TOT = "%tot";

    private List<Pattern> ramPatterns;
    private String ramFormat;
    private int color;
    private int size;
    private final StatusUpdateListener listener;

    private final ActivityManager activityManager;
    private final ActivityManager.MemoryInfo memory;

    public RamManager(Context context, long delay, int size, StatusUpdateListener listener) {
        super(context, delay);
        this.size = size;
        this.listener = listener;
        this.activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        this.memory = new ActivityManager.MemoryInfo();
    }

    @Override
    protected void update() {
        if (ramFormat == null) {
            ramFormat = XMLPrefsManager.get(Behavior.ram_format);
            color = XMLPrefsManager.getColor(Theme.ram_color);
        }

        if (ramPatterns == null) {
            ramPatterns = new ArrayList<>();
            ramPatterns.add(Pattern.compile(AV + "tb", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
            ramPatterns.add(Pattern.compile(AV + "gb", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
            ramPatterns.add(Pattern.compile(AV + "mb", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
            ramPatterns.add(Pattern.compile(AV + "kb", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
            ramPatterns.add(Pattern.compile(AV + "b", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
            ramPatterns.add(Pattern.compile(AV + "%", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));

            ramPatterns.add(Pattern.compile(TOT + "tb", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
            ramPatterns.add(Pattern.compile(TOT + "gb", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
            ramPatterns.add(Pattern.compile(TOT + "mb", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
            ramPatterns.add(Pattern.compile(TOT + "kb", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
            ramPatterns.add(Pattern.compile(TOT + "b", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));

            ramPatterns.add(Tuils.patternNewline);
        }

        String copy = ramFormat;

        double av = SystemUtils.freeRam(activityManager, memory);
        double tot = SystemUtils.totalRam() * 1024L;

        copy = ramPatterns.get(0).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) av, SystemUtils.TERA))));
        copy = ramPatterns.get(1).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) av, SystemUtils.GIGA))));
        copy = ramPatterns.get(2).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) av, SystemUtils.MEGA))));
        copy = ramPatterns.get(3).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) av, SystemUtils.KILO))));
        copy = ramPatterns.get(4).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) av, SystemUtils.BYTE))));
        copy = ramPatterns.get(5).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.percentage(av, tot))));

        copy = ramPatterns.get(6).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) tot, SystemUtils.TERA))));
        copy = ramPatterns.get(7).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) tot, SystemUtils.GIGA))));
        copy = ramPatterns.get(8).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) tot, SystemUtils.MEGA))));
        copy = ramPatterns.get(9).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) tot, SystemUtils.KILO))));
        copy = ramPatterns.get(10).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) tot, SystemUtils.BYTE))));

        copy = ramPatterns.get(11).matcher(copy).replaceAll(Matcher.quoteReplacement(Tuils.NEWLINE));

        if (listener != null) {
            listener.onUpdate(UIManager.Label.ram, UIUtils.span(context, copy, color, size));
        }
    }
}
