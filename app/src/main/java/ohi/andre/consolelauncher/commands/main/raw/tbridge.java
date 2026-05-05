package ohi.andre.consolelauncher.commands.main.raw;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.managers.termux.TermuxBridgeManager;

public class tbridge implements CommandAbstraction {

    public static final String CD_SCRIPT =
            "target=\"$1\"; [ -d \"$target\" ] || { echo \"not a directory: $target\" >&2; exit 2; }; "
                    + "cd \"$target\" && pwd";
    public static final String LIST_DIRS_SCRIPT =
            "dir=\"$1\"; [ -d \"$dir\" ] || { echo \"not a directory: $dir\" >&2; exit 2; }; "
                    + "find \"$dir\" -mindepth 1 -maxdepth 1 -type d -printf '%f/\\n' 2>/dev/null | sort";
    public static final String LIST_FILES_SCRIPT =
            "dir=\"$1\"; [ -d \"$dir\" ] || { echo \"not a directory: $dir\" >&2; exit 2; }; "
                    + "find \"$dir\" -mindepth 1 -maxdepth 1 -type f -printf '%f\\n' 2>/dev/null | sort";
    public static final String LIST_ALL_SCRIPT =
            "dir=\"$1\"; [ -d \"$dir\" ] || { echo \"not a directory: $dir\" >&2; exit 2; }; "
                    + "{ find \"$dir\" -mindepth 1 -maxdepth 1 -type d -printf '%f/\\n' 2>/dev/null; "
                    + "find \"$dir\" -mindepth 1 -maxdepth 1 -type f -printf '%f\\n' 2>/dev/null; } | sort";
    public static final String OPEN_FILE_SCRIPT =
            "target=\"$1\"; [ -e \"$target\" ] || { echo \"not found: $target\" >&2; exit 2; }; "
                    + "[ -f \"$target\" ] || { echo \"is directory: $target\" >&2; exit 3; }; "
                    + "command -v termux-open >/dev/null || { echo \"termux-open missing\" >&2; exit 4; }; "
                    + "termux-open \"$target\" && printf 'opening %s\\n' \"$target\"";
    public static final String SHARE_FILE_SCRIPT =
            "target=\"$1\"; [ -e \"$target\" ] || { echo \"not found: $target\" >&2; exit 2; }; "
                    + "[ -f \"$target\" ] || { echo \"is directory: $target\" >&2; exit 3; }; "
                    + "if command -v termux-share >/dev/null; then termux-share \"$target\" && printf 'sharing %s\\n' \"$target\"; "
                    + "else echo \"termux-share missing; install Termux:API for share support\" >&2; exit 4; fi";
    private static final String STATUS_SCRIPT =
            "printf 'termux_home=%s\\n' \"$HOME\"; "
                    + "printf 'pwd=%s\\n' \"$PWD\"; "
                    + "command -v find >/dev/null && echo 'find=available' || echo 'find=missing'; "
                    + "[ -d /storage/emulated/0 ] && echo 'shared_storage=visible' || echo 'shared_storage=not_visible'";

    @Override
    public String exec(ExecutePack pack) {
        MainPack info = (MainPack) pack;
        String input = info.getString();
        if (input == null || input.trim().length() == 0) {
            return info.res.getString(helpRes());
        }

        String[] parts = input.trim().split("\\s+", 2);
        String option = parts[0].toLowerCase();

        if ("-status".equals(option) || "-doctor".equals(option)) {
            return localStatus(info);
        }

        if ("-setup".equals(option)) {
            return setupText();
        }

        if ("-dirs".equals(option) || "-files".equals(option) || "-ls".equals(option)) {
            return retiredFileListingMessage();
        }

        if (!ensureReady(info)) {
            return null;
        }

        if ("-probe".equals(option)) {
            TermuxBridgeManager.dispatchShell(info.context, "probe", STATUS_SCRIPT, TermuxBridgeManager.TERMUX_HOME);
            return "Termux bridge probe dispatched.";
        }

        return info.res.getString(helpRes());
    }

    private String localStatus(MainPack info) {
        TermuxBridgeManager.BridgeStatus status = TermuxBridgeManager.status(info.context);
        StringBuilder builder = new StringBuilder();
        builder.append("Re:T-UI Termux Bridge").append('\n');
        builder.append("role: scripts, modules, callbacks, automation").append('\n');
        builder.append("termux: ").append(status.termuxInstalled ? "installed" : "missing").append('\n');
        builder.append("RUN_COMMAND declared: ").append(status.runCommandDeclared ? "yes" : "no").append('\n');
        builder.append("RUN_COMMAND granted: ").append(status.runCommandGranted ? "yes" : "no").append('\n');
        builder.append("files: use the files command / Re:T-UI Files app").append('\n');
        builder.append("current path: ").append(info.currentDirectory.getAbsolutePath()).append('\n');
        builder.append("probe: tbridge -probe").append('\n');
        builder.append("setup: tbridge -setup");
        return builder.toString();
    }

    private boolean ensureReady(MainPack info) {
        TermuxBridgeManager.BridgeStatus status = TermuxBridgeManager.status(info.context);
        if (!status.termuxInstalled) {
            ohi.andre.consolelauncher.tuils.Tuils.sendOutput(info.context, "Termux is not installed.");
            return false;
        }
        if (!status.runCommandDeclared) {
            ohi.andre.consolelauncher.tuils.Tuils.sendOutput(info.context, "This Termux build does not expose RUN_COMMAND.");
            return false;
        }
        if (!status.runCommandGranted) {
            TermuxBridgeManager.requestRunCommandPermissionIfPossible(info.context);
            ohi.andre.consolelauncher.tuils.Tuils.sendOutput(info.context, "Grant Re:T-UI the Termux RUN_COMMAND permission, then retry.");
            ohi.andre.consolelauncher.tuils.Tuils.sendOutput(info.context, "Termux must also set allow-external-apps=true.");
            return false;
        }
        return true;
    }

    private String setupText() {
        return "Termux bridge setup for scripts, modules, and automation:\n"
                + "1. Install Termux.\n"
                + "2. In Termux run: termux-setup-storage\n"
                + "3. In Termux run: mkdir -p ~/.termux && echo allow-external-apps=true >> ~/.termux/termux.properties\n"
                + "4. Restart Termux.\n"
                + "5. Grant Re:T-UI the RUN_COMMAND permission from Android app settings.\n"
                + "6. Run: tbridge -doctor\n"
                + "\nUse files for file navigation. TBridge is now the Termux runtime for scripts and modules.";
    }

    private String retiredFileListingMessage() {
        return "TBridge file listing is retired from the public command surface.\n"
                + "Use files for interactive file navigation, or use ls/open/share with file_backend=termux for bridge-backed quick actions.\n"
                + "TBridge now focuses on Termux runtime checks, scripts, modules, callbacks, and automation.";
    }

    @Override
    public int helpRes() {
        return R.string.help_tbridge;
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
    public String onNotArgEnough(ExecutePack pack, int nArgs) {
        return pack.context.getString(helpRes());
    }

    @Override
    public String onArgNotFound(ExecutePack pack, int index) {
        return pack.context.getString(helpRes());
    }
}
