package ohi.andre.consolelauncher.tuils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.options.Theme;
import ohi.andre.consolelauncher.managers.xml.options.Ui;

/**
 * Centralizes the styling of TUI widgets (Music, Notifications, etc.)
 * to ensure visual consistency and reduce UIManager bloat.
 */
public class TuiWidgetDecorator {

    public static void decorateWidget(View widgetRoot, int borderViewId, int labelViewId) {
        if (widgetRoot == null) return;

        Context context = widgetRoot.getContext();
        int widgetColor = XMLPrefsManager.getColor(Theme.music_widget_color);
        int widgetBgColor = XMLPrefsManager.getColor(Theme.window_terminal_bg);
        boolean useDashed = XMLPrefsManager.getBoolean(Ui.enable_dashed_border);

        // 1. Decorate Border
        View borderView = widgetRoot.findViewById(borderViewId);
        if (borderView != null) {
            GradientDrawable gd = new GradientDrawable();
            gd.setShape(GradientDrawable.RECTANGLE);
            if (useDashed) {
                gd.setStroke((int) Tuils.dpToPx(context, 1.5f), widgetColor,
                        Tuils.dpToPx(context, XMLPrefsManager.getInt(Ui.dashed_border_dash_length)),
                        Tuils.dpToPx(context, XMLPrefsManager.getInt(Ui.dashed_border_gap_length)));
            } else {
                gd.setStroke((int) Tuils.dpToPx(context, 1.5f), widgetColor);
            }
            gd.setColor(widgetBgColor);
            borderView.setBackground(gd);
        }

        // 2. Decorate Label
        TextView widgetLabel = widgetRoot.findViewById(labelViewId);
        if (widgetLabel != null) {
            widgetLabel.setTextColor(widgetColor);
            widgetLabel.setTypeface(Tuils.getTypeface(context), Typeface.BOLD);
            try {
                GradientDrawable gd = (GradientDrawable) ResourcesCompat.getDrawable(
                        context.getResources(), R.drawable.apps_drawer_header_border, null);
                if (gd != null) {
                    gd = (GradientDrawable) gd.mutate();
                    if (useDashed) {
                        gd.setStroke((int) Tuils.dpToPx(context, 1.5f), widgetColor,
                                Tuils.dpToPx(context, XMLPrefsManager.getInt(Ui.dashed_border_dash_length)),
                                Tuils.dpToPx(context, XMLPrefsManager.getInt(Ui.dashed_border_gap_length)));
                    } else {
                        gd.setStroke((int) Tuils.dpToPx(context, 1.5f), widgetColor);
                    }
                    gd.setColor(widgetBgColor);
                    widgetLabel.setBackground(gd);
                }
            } catch (Exception ignored) {}
        }
    }

    public static GradientDrawable getRowBackground(Context context) {
        int widgetColor = XMLPrefsManager.getColor(Theme.music_widget_color);
        int widgetBgColor = XMLPrefsManager.getColor(Theme.window_terminal_bg);
        int rowBackground = ColorUtils.blendARGB(widgetBgColor, Color.BLACK, 0.22f);
        int strokeColor = ColorUtils.setAlphaComponent(widgetColor, 140);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(Tuils.dpToPx(context, 4));
        bg.setColor(rowBackground);
        bg.setStroke((int) Tuils.dpToPx(context, 1.2f), strokeColor);
        return bg;
    }
}
