package ohi.andre.consolelauncher.commands.main.raw;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import ohi.andre.consolelauncher.LauncherActivity;
import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.UIManager;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.managers.modules.ModuleManager;
import ohi.andre.consolelauncher.managers.modules.UpcomingEventsManager;

public class events implements CommandAbstraction {

    @Override
    public String exec(ExecutePack pack) {
        Object arg = pack.get(Object.class, 0);
        String input = arg == null ? "" : arg.toString().trim().toLowerCase();

        if ("-access".equals(input) || "access".equals(input)) {
            if (UpcomingEventsManager.hasCalendarPermission(pack.context)) {
                return "Calendar access already granted.";
            }
            if (pack.context instanceof Activity) {
                ActivityCompat.requestPermissions((Activity) pack.context,
                        new String[]{Manifest.permission.READ_CALENDAR},
                        LauncherActivity.COMMAND_REQUEST_PERMISSION);
                return "Calendar access requested.";
            }
            return "Calendar access must be granted from Android settings.";
        }

        if ("-module".equals(input) || "module".equals(input) || "-print".equals(input)) {
            return exampleScript();
        }

        if ("-install".equals(input) || "-add".equals(input) || "install".equals(input)) {
            return "Events is an editable Termux module now.\n"
                    + "Create ~/retui/events.sh with: events -module\n"
                    + "Then run: module -add events termux:/data/data/com.termux/files/home/retui/events.sh";
        }

        if (!ModuleManager.isKnown(pack.context, ModuleManager.EVENTS)) {
            return "Events is an editable Termux module. Run events -module for the script, then module -add events termux:/data/data/com.termux/files/home/retui/events.sh";
        }
        Intent intent = new Intent(UIManager.ACTION_MODULE_COMMAND);
        intent.putExtra(UIManager.EXTRA_MODULE_COMMAND, "show");
        intent.putExtra(UIManager.EXTRA_MODULE_NAME, ModuleManager.EVENTS);
        LocalBroadcastManager.getInstance(pack.context.getApplicationContext()).sendBroadcast(intent);
        return "Module opened: events";
    }

    private String exampleScript() {
        return "#!/data/data/com.termux/files/usr/bin/sh\n"
                + "\n"
                + "echo \"::title Events\"\n"
                + "\n"
                + "EVENTS_FILE=\"%RETUI_CALENDAR_UPCOMING_MONTH\"\n"
                + "if [ ! -s \"$EVENTS_FILE\" ]; then\n"
                + "  echo \"::body No upcoming events this month.\"\n"
                + "else\n"
                + "  while IFS='	' read -r date time title location; do\n"
                + "    if [ -n \"$time\" ]; then\n"
                + "      line=\"$date $time - $title\"\n"
                + "    else\n"
                + "      line=\"$date - $title\"\n"
                + "    fi\n"
                + "    [ -n \"$location\" ] && line=\"$line @ $location\"\n"
                + "    echo \"::body $line\"\n"
                + "  done < \"$EVENTS_FILE\"\n"
                + "fi\n"
                + "\n"
                + "echo \"::suggest refresh | command | module -refresh events\"\n"
                + "echo \"::suggest access | command | events -access\"\n"
                + "echo \"::suggest calendar | command | intent -view content://com.android.calendar/time/%RETUI_NOW\"";
    }

    @Override
    public int[] argType() {
        return new int[]{CommandAbstraction.PLAIN_TEXT};
    }

    @Override
    public int priority() {
        return 4;
    }

    @Override
    public int helpRes() {
        return R.string.help_events;
    }

    @Override
    public String onArgNotFound(ExecutePack info, int index) {
        return info.context.getString(R.string.help_events);
    }

    @Override
    public String onNotArgEnough(ExecutePack info, int nArgs) {
        return info.context.getString(R.string.help_events);
    }
}
