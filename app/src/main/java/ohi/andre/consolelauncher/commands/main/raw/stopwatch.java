package ohi.andre.consolelauncher.commands.main.raw;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.commands.main.specific.ParamCommand;
import ohi.andre.consolelauncher.managers.ClockManager;
import ohi.andre.consolelauncher.tuils.Tuils;

public class stopwatch extends ParamCommand {

    private enum Param implements ohi.andre.consolelauncher.commands.main.Param {
        stop {
            @Override
            public String exec(ExecutePack pack) {
                return ClockManager.getInstance(pack.context).stopStopwatch();
            }

            @Override
            public int[] args() {
                return new int[0];
            }
        },
        reset {
            @Override
            public String exec(ExecutePack pack) {
                return ClockManager.getInstance(pack.context).resetStopwatch();
            }

            @Override
            public int[] args() {
                return new int[0];
            }
        },
        status {
            @Override
            public String exec(ExecutePack pack) {
                return ClockManager.getInstance(pack.context).getStopwatchStatus();
            }

            @Override
            public int[] args() {
                return new int[0];
            }
        };

        static Param get(String p) {
            p = p.toLowerCase();
            for (Param param : values()) {
                if (p.endsWith(param.label())) {
                    return param;
                }
            }
            return null;
        }

        static String[] labels() {
            Param[] values = values();
            String[] labels = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                labels[i] = values[i].label();
            }
            return labels;
        }

        @Override
        public String label() {
            return Tuils.MINUS + name();
        }

        @Override
        public String onNotArgEnough(ExecutePack pack, int n) {
            return pack.context.getString(R.string.help_stopwatch);
        }

        @Override
        public String onArgNotFound(ExecutePack pack, int index) {
            return pack.context.getString(R.string.help_stopwatch);
        }
    }

    @Override
    public String[] params() {
        return Param.labels();
    }

    @Override
    protected ohi.andre.consolelauncher.commands.main.Param paramForString(MainPack pack, String param) {
        return Param.get(param);
    }

    @Override
    protected String doThings(ExecutePack pack) {
        Object first = pack.get(Object.class, 0);
        if (first == null) {
            return ClockManager.getInstance(pack.context).startStopwatch();
        }

        String token = first.toString().trim();
        if (token.startsWith(Tuils.MINUS)) {
            return null;
        }

        return pack.context.getString(R.string.help_stopwatch);
    }

    @Override
    public int priority() {
        return 3;
    }

    @Override
    public int helpRes() {
        return R.string.help_stopwatch;
    }
}
