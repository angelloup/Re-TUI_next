package ohi.andre.consolelauncher.commands.main.raw;

import android.content.Intent;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.commands.tuixt.ThemerActivity;
import ohi.andre.consolelauncher.tuils.Tuils;

public class themer implements CommandAbstraction {

    @Override
    public String exec(ExecutePack pack) {
        MainPack info = (MainPack) pack;
        Intent intent = new Intent(info.context, ThemerActivity.class);
        info.context.startActivity(intent);
        return Tuils.EMPTYSTRING;
    }

    @Override
    public int[] argType() {
        return new int[0];
    }

    @Override
    public int priority() {
        return 3;
    }

    @Override
    public int helpRes() {
        return 0; // Will add a help string later if needed
    }

    @Override
    public String onArgNotFound(ExecutePack pack, int index) {
        return null;
    }

    @Override
    public String onNotArgEnough(ExecutePack pack, int nArgs) {
        return null;
    }
}
