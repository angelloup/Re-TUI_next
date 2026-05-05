package ohi.andre.consolelauncher.commands.main.raw;

import android.content.Intent;

import java.io.File;
import java.util.Arrays;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import ohi.andre.consolelauncher.LauncherActivity;
import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.UIManager;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.commands.main.specific.APICommand;
import ohi.andre.consolelauncher.commands.main.specific.ParamCommand;
import ohi.andre.consolelauncher.managers.modules.ModuleManager;
import ohi.andre.consolelauncher.managers.notifications.NotificationManager;
import ohi.andre.consolelauncher.managers.notifications.NotificationService;
import ohi.andre.consolelauncher.managers.settings.LauncherSettings;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.options.Notifications;
import ohi.andre.consolelauncher.tuils.Tuils;
import ohi.andre.consolelauncher.tuils.interfaces.Reloadable;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;

/**
 * Created by francescoandreuzzi on 29/04/2017.
 */

public class notifications extends ParamCommand implements APICommand {

    @Override
    public boolean willWorkOn(int api) {
        return api >= JELLY_BEAN_MR2;
    }

    private enum Param implements ohi.andre.consolelauncher.commands.main.Param {

        inc {
            @Override
            public String exec(ExecutePack pack) {
                String output = NotificationManager.setState(pack.getLaunchInfo().componentName.getPackageName(), true);
                NotificationService.requestReload(pack.context);
                if(output == null || output.length() == 0) return null;
                return output;
            }

            @Override
            public int[] args() {
                return new int[] {CommandAbstraction.VISIBLE_PACKAGE};
            }

            @Override
            public String onArgNotFound(ExecutePack pack, int index) {
                return pack.context.getString(R.string.output_appnotfound);
            }
        },
        exc {
            @Override
            public String exec(ExecutePack pack) {
                String output = NotificationManager.setState(pack.getLaunchInfo().componentName.getPackageName(), false);
                NotificationService.requestReload(pack.context);
                if(output == null || output.length() == 0) return null;
                return output;
            }

            @Override
            public int[] args() {
                return new int[] {CommandAbstraction.VISIBLE_PACKAGE};
            }

            @Override
            public String onArgNotFound(ExecutePack pack, int index) {
                return pack.context.getString(R.string.output_appnotfound);
            }
        },
        color {
            @Override
            public String exec(ExecutePack pack) {
                String color = pack.getString();
                String output = NotificationManager.setColor(pack.getLaunchInfo().componentName.getPackageName(), color);
                NotificationService.requestReload(pack.context);
                if(output == null || output.length() == 0) return null;
                return output;
            }

            @Override
            public int[] args() {
                return new int[] {CommandAbstraction.COLOR, CommandAbstraction.VISIBLE_PACKAGE};
            }

            @Override
            public String onArgNotFound(ExecutePack pack, int index) {
                int res;
                if(index == 1) res = R.string.output_invalidcolor;
                else res = R.string.output_appnotfound;

                return pack.context.getString(res);
            }
        },
        format {
            @Override
            public String exec(ExecutePack pack) {
                String s = pack.getString();
                String output = NotificationManager.setFormat(pack.getLaunchInfo().componentName.getPackageName(), s);
                NotificationService.requestReload(pack.context);
                if(output == null || output.length() == 0) return null;
                return output;
            }

            @Override
            public int[] args() {
                return new int[] {CommandAbstraction.NO_SPACE_STRING, CommandAbstraction.VISIBLE_PACKAGE};
            }

            @Override
            public String onArgNotFound(ExecutePack pack, int index) {
                return pack.context.getString(R.string.invalid_integer);
            }
        },
        add_filter {
            @Override
            public String exec(ExecutePack pack) {
                int id = pack.getInt();
                String output = NotificationManager.addFilter(pack.getString(), id);
                NotificationService.requestReload(pack.context);
                if(output == null || output.length() == 0) return null;
                return output;
            }

            @Override
            public int[] args() {
                return new int[] {CommandAbstraction.INT, CommandAbstraction.PLAIN_TEXT};
            }

            @Override
            public String onArgNotFound(ExecutePack pack, int index) {
                return pack.context.getString(R.string.invalid_integer);
            }
        },
        add_format {
            @Override
            public String exec(ExecutePack pack) {
                int id = pack.getInt();
                String output = NotificationManager.addFormat(pack.getString(), id);
                NotificationService.requestReload(pack.context);
                if(output == null || output.length() == 0) return null;
                return output;
            }

            @Override
            public int[] args() {
                return new int[] {CommandAbstraction.INT, CommandAbstraction.PLAIN_TEXT};
            }

            @Override
            public String onArgNotFound(ExecutePack pack, int index) {
                return pack.context.getString(R.string.invalid_integer);
            }
        },
        rm_filter {
            @Override
            public String exec(ExecutePack pack) {
                String output = NotificationManager.rmFilter(pack.getInt());
                NotificationService.requestReload(pack.context);
                if(output == null || output.length() == 0) return null;
                return output;
            }

            @Override
            public int[] args() {
                return new int[] {CommandAbstraction.INT};
            }

            @Override
            public String onArgNotFound(ExecutePack pack, int index) {
                return pack.context.getString(R.string.invalid_integer);
            }
        },
        rm_format {
            @Override
            public String exec(ExecutePack pack) {
                String output = NotificationManager.rmFormat(pack.getInt());
                NotificationService.requestReload(pack.context);
                if(output == null || output.length() == 0) return null;
                return output;
            }

            @Override
            public int[] args() {
                return new int[] {CommandAbstraction.INT};
            }

            @Override
            public String onArgNotFound(ExecutePack pack, int index) {
                return pack.context.getString(R.string.invalid_integer);
            }
        },
        file {
            @Override
            public int[] args() {
                return new int[0];
            }

            @Override
            public String exec(ExecutePack pack) {
                pack.context.startActivity(Tuils.openFile(pack.context, new File(Tuils.getFolder(), NotificationManager.PATH)));
                return null;
            }
        },
        ls {
            @Override
            public int[] args() {
                return new int[0];
            }

            @Override
            public String exec(ExecutePack pack) {
                NotificationManager manager = NotificationManager.create(pack.context);
                return manager.describeRules();
            }
        },
        access {
            @Override
            public int[] args() {
                return new int[0];
            }

            @Override
            public String exec(ExecutePack pack) {
                try {
                    pack.context.startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                } catch (Exception e) {
                    return pack.context.getString(R.string.activity_not_found);
                }
                return null;
            }
        },
        open {
            @Override
            public int[] args() {
                return new int[0];
            }

            @Override
            public String exec(ExecutePack pack) {
                if (LauncherActivity.instance != null && LauncherActivity.instance.getUIManager() != null) {
                    LauncherActivity.instance.runOnUiThread(() -> LauncherActivity.instance.getUIManager().openNotificationShade());
                    return null;
                }

                try {
                    @SuppressWarnings("WrongConstant")
                    Object sbservice = pack.context.getSystemService("statusbar");
                    Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
                    java.lang.reflect.Method expand = statusbarManager.getMethod("expandNotificationsPanel");
                    expand.invoke(sbservice);
                    return null;
                } catch (Exception e) {
                    return e.toString();
                }
            }
        },
        next {
            @Override
            public int[] args() {
                return new int[0];
            }

            @Override
            public String exec(ExecutePack pack) {
                if (LauncherActivity.instance != null && LauncherActivity.instance.getUIManager() != null) {
                    LauncherActivity.instance.runOnUiThread(() -> LauncherActivity.instance.getUIManager().nextNotificationPage());
                    return null;
                }
                return "Notification module is not available.";
            }
        },
        prev {
            @Override
            public int[] args() {
                return new int[0];
            }

            @Override
            public String exec(ExecutePack pack) {
                if (LauncherActivity.instance != null && LauncherActivity.instance.getUIManager() != null) {
                    LauncherActivity.instance.runOnUiThread(() -> LauncherActivity.instance.getUIManager().previousNotificationPage());
                    return null;
                }
                return "Notification module is not available.";
            }
        },
        reply {
            @Override
            public int[] args() {
                return new int[0];
            }

            @Override
            public String exec(ExecutePack pack) {
                if (LauncherActivity.instance != null && LauncherActivity.instance.getUIManager() != null) {
                    LauncherActivity.instance.runOnUiThread(() -> LauncherActivity.instance.getUIManager().startCurrentNotificationReply());
                    return null;
                }
                return "Notification module is not available.";
            }
        },
        tutorial {
            @Override
            public int[] args() {
                return new int[0];
            }

            @Override
            public String exec(ExecutePack pack) {
                pack.context.startActivity(Tuils.webPage("https://github.com/DvilSpawn/Re-TUI/wiki/Notifications"));
                return null;
            }
        },
        on {
            @Override
            public int[] args() {
                return new int[0];
            }

            @Override
            public String exec(ExecutePack pack) {
                return setNotificationTerminal(pack, true);
            }
        },
        off {
            @Override
            public int[] args() {
                return new int[0];
            }

            @Override
            public String exec(ExecutePack pack) {
                return setNotificationTerminal(pack, false);
            }
        };

        static Param get(String p) {
            p = p.toLowerCase();
            Param[] ps = values();
            for (Param p1 : ps)
                if (p.endsWith(p1.label()))
                    return p1;
            return null;
        }

        static String[] labels() {
            Param[] ps = values();
            String[] ss = new String[ps.length];

            for (int count = 0; count < ps.length; count++) {
                ss[count] = ps[count].label();
            }

            return ss;
        }

        @Override
        public String label() {
            return Tuils.MINUS + name();
        }

        @Override
        public String onArgNotFound(ExecutePack pack, int index) {
            return null;
        }

        @Override
        public String onNotArgEnough(ExecutePack pack, int n) {
            return pack.context.getString(R.string.help_notifications);
        }
    }

    @Override
    protected ohi.andre.consolelauncher.commands.main.Param paramForString(MainPack pack, String param) {
        return Param.get(param);
    }

    @Override
    protected String doThings(ExecutePack pack) {
        return null;
    }

    private static String setNotificationTerminal(ExecutePack pack, boolean enabled) {
        if (enabled) {
            ModuleManager.addToDock(pack.context, Arrays.asList(ModuleManager.NOTIFICATIONS));
        } else {
            ModuleManager.removeFromDock(pack.context, Arrays.asList(ModuleManager.NOTIFICATIONS));
        }

        Intent rebuild = new Intent(UIManager.ACTION_MODULE_COMMAND);
        rebuild.putExtra(UIManager.EXTRA_MODULE_COMMAND, "rebuild");
        LocalBroadcastManager.getInstance(pack.context.getApplicationContext()).sendBroadcast(rebuild);

        return enabled ? "Notification module added to dock." : "Notification module removed from dock.";
    }

    @Override
    public String[] params() {
        return Param.labels();
    }

    @Override
    public int priority() {
        return 3;
    }

    @Override
    public int helpRes() {
        return R.string.help_notifications;
    }
}
