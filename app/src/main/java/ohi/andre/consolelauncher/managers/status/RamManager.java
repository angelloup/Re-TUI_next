package ohi.andre.consolelauncher.managers.status;

import android.app.ActivityManager;
import android.content.Context;
import android.text.Matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import ohi.andre.consolelauncher.UIManager;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.classes.Behavior;
import ohi.andre.consolelauncher.managers.xml.classes.Theme;
import ohi.andre.consolelauncher.managers.xml.classes.Ui;
import ohi.andre.consolelauncher.tuils.Tuils;

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

        double av = Tuils.freeRam(activityManager, memory);
        double tot = Tuils.totalRam() * 1024L;

        copy = ramPatterns.get(0).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(Tuils.formatSize((long) av, Tuils.TERA))));
        copy = ramPatterns.get(1).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(Tuils.formatSize((long) av, Tuils.GIGA))));
        copy = ramPatterns.get(2).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(Tuils.formatSize((long) av, Tuils.MEGA))));
        copy = ramPatterns.get(3).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(Tuils.formatSize((long) av, Tuils.KILO))));
        copy = ramPatterns.get(4).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(Tuils.formatSize((long) av, Tuils.BYTE))));
        copy = ramPatterns.get(5).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(Tuils.percentage(av, tot))));

        copy = ramPatterns.get(6).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(Tuils.formatSize((long) tot, Tuils.TERA))));
        copy = ramPatterns.get(7).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(Tuils.formatSize((long) tot, Tuils.GIGA))));
        copy = ramPatterns.get(8).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(Tuils.formatSize((long) tot, Tuils.MEGA))));
        copy = ramPatterns.get(9).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(Tuils.formatSize((long) tot, Tuils.KILO))));
        copy = ramPatterns.get(10).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(Tuils.formatSize((long) tot, Tuils.BYTE))));

        copy = ramPatterns.get(11).matcher(copy).replaceAll(Matcher.quoteReplacement(Tuils.NEWLINE));

        if (listener != null) {
            listener.onUpdate(UIManager.Label.ram, Tuils.span(context, copy, color, size));
        }
    }
}
