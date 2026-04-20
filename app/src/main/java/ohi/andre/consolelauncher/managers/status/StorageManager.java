package ohi.andre.consolelauncher.managers.status;

import android.content.Context;
import java.util.regex.Matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import ohi.andre.consolelauncher.UIManager;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.options.Behavior;
import ohi.andre.consolelauncher.managers.xml.options.Theme;
import ohi.andre.consolelauncher.tuils.SystemUtils;
import ohi.andre.consolelauncher.tuils.Tuils;
import ohi.andre.consolelauncher.tuils.UIUtils;

public class StorageManager extends StatusManager {

    private final String INT_AV = "%iav";
    private final String INT_TOT = "%itot";
    private final String EXT_AV = "%eav";
    private final String EXT_TOT = "%etot";

    private List<Pattern> storagePatterns;
    private String storageFormat;
    private int color;
    private int size;
    private final StatusUpdateListener listener;

    public StorageManager(Context context, long delay, int size, StatusUpdateListener listener) {
        super(context, delay);
        this.size = size;
        this.listener = listener;
    }

    @Override
    protected void update() {
        if (storageFormat == null) {
            storageFormat = XMLPrefsManager.get(Behavior.storage_format);
            color = XMLPrefsManager.getColor(Theme.storage_color);
        }

        if (storagePatterns == null) {
            storagePatterns = new ArrayList<>();

            storagePatterns.add(Pattern.compile(INT_AV + "tb", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
            storagePatterns.add(Pattern.compile(INT_AV + "gb", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
            storagePatterns.add(Pattern.compile(INT_AV + "mb", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
            storagePatterns.add(Pattern.compile(INT_AV + "kb", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
            storagePatterns.add(Pattern.compile(INT_AV + "b", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
            storagePatterns.add(Pattern.compile(INT_AV + "%", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));

            storagePatterns.add(Pattern.compile(INT_TOT + "tb", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
            storagePatterns.add(Pattern.compile(INT_TOT + "gb", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
            storagePatterns.add(Pattern.compile(INT_TOT + "mb", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
            storagePatterns.add(Pattern.compile(INT_TOT + "kb", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
            storagePatterns.add(Pattern.compile(INT_TOT + "b", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));

            storagePatterns.add(Pattern.compile(EXT_AV + "tb", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
            storagePatterns.add(Pattern.compile(EXT_AV + "gb", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
            storagePatterns.add(Pattern.compile(EXT_AV + "mb", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
            storagePatterns.add(Pattern.compile(EXT_AV + "kb", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
            storagePatterns.add(Pattern.compile(EXT_AV + "b", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
            storagePatterns.add(Pattern.compile(EXT_AV + "%", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));

            storagePatterns.add(Pattern.compile(EXT_TOT + "tb", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
            storagePatterns.add(Pattern.compile(EXT_TOT + "gb", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
            storagePatterns.add(Pattern.compile(EXT_TOT + "mb", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
            storagePatterns.add(Pattern.compile(EXT_TOT + "kb", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
            storagePatterns.add(Pattern.compile(EXT_TOT + "b", Pattern.CASE_INSENSITIVE | Pattern.LITERAL));

            storagePatterns.add(Tuils.patternNewline);

            storagePatterns.add(Pattern.compile(INT_AV, Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
            storagePatterns.add(Pattern.compile(INT_TOT, Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
            storagePatterns.add(Pattern.compile(EXT_AV, Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
            storagePatterns.add(Pattern.compile(EXT_TOT, Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
        }

        double iav = SystemUtils.getAvailableInternalMemorySize(SystemUtils.BYTE);
        double itot = SystemUtils.getTotalInternalMemorySize(SystemUtils.BYTE);
        double eav = SystemUtils.getAvailableExternalMemorySize(SystemUtils.BYTE);
        double etot = SystemUtils.getTotalExternalMemorySize(SystemUtils.BYTE);

        String copy = storageFormat;

        copy = storagePatterns.get(0).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) iav, SystemUtils.TERA))));
        copy = storagePatterns.get(1).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) iav, SystemUtils.GIGA))));
        copy = storagePatterns.get(2).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) iav, SystemUtils.MEGA))));
        copy = storagePatterns.get(3).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) iav, SystemUtils.KILO))));
        copy = storagePatterns.get(4).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) iav, SystemUtils.BYTE))));
        copy = storagePatterns.get(5).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.percentage(iav, itot))));

        copy = storagePatterns.get(6).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) itot, SystemUtils.TERA))));
        copy = storagePatterns.get(7).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) itot, SystemUtils.GIGA))));
        copy = storagePatterns.get(8).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) itot, SystemUtils.MEGA))));
        copy = storagePatterns.get(9).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) itot, SystemUtils.KILO))));
        copy = storagePatterns.get(10).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) itot, SystemUtils.BYTE))));

        copy = storagePatterns.get(11).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) eav, SystemUtils.TERA))));
        copy = storagePatterns.get(12).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) eav, SystemUtils.GIGA))));
        copy = storagePatterns.get(13).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) eav, SystemUtils.MEGA))));
        copy = storagePatterns.get(14).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) eav, SystemUtils.KILO))));
        copy = storagePatterns.get(15).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) eav, SystemUtils.BYTE))));
        copy = storagePatterns.get(16).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.percentage(eav, etot))));

        copy = storagePatterns.get(17).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) etot, SystemUtils.TERA))));
        copy = storagePatterns.get(18).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) etot, SystemUtils.GIGA))));
        copy = storagePatterns.get(19).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) etot, SystemUtils.MEGA))));
        copy = storagePatterns.get(20).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) etot, SystemUtils.KILO))));
        copy = storagePatterns.get(21).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) etot, SystemUtils.BYTE))));

        copy = storagePatterns.get(22).matcher(copy).replaceAll(Matcher.quoteReplacement(Tuils.NEWLINE));

        copy = storagePatterns.get(23).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) iav, SystemUtils.GIGA))));
        copy = storagePatterns.get(24).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) itot, SystemUtils.GIGA))));
        copy = storagePatterns.get(25).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) eav, SystemUtils.GIGA))));
        copy = storagePatterns.get(26).matcher(copy).replaceAll(Matcher.quoteReplacement(String.valueOf(SystemUtils.formatSize((long) etot, SystemUtils.GIGA))));

        if (listener != null) {
            listener.onUpdate(UIManager.Label.storage, UIUtils.span(context, copy, color, size));
        }
    }
}
