package ohi.andre.consolelauncher.commands.tuixt;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ohi.andre.consolelauncher.LauncherActivity;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.options.Ui;
import ohi.andre.consolelauncher.tuils.Tuils;

public class ThemerActivity extends AppCompatActivity {

    private final List<String> configFiles = Arrays.asList(
            "theme.xml", "ui.xml", "behavior.xml", "cmd.xml",
            "suggestions.xml", "toolbar.xml", "notifications.xml",
            "apps.xml", "rss.xml", "alias.txt", "ascii.txt", "Fonts", "View Crash Log"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        root.setPadding(16, 16, 16, 16);
        root.setFitsSystemWindows(true);

        TextView header = new TextView(this);
        header.setText("> Re:T-UI Settings Hub");
        header.setTextColor(Color.GREEN);
        header.setTypeface(Typeface.MONOSPACE);
        header.setTextSize(18);
        root.addView(header);

        View divider = new View(this);
        divider.setBackgroundColor(Color.parseColor("#222222"));
        divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
        root.addView(divider);

        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        recyclerView.setAdapter(new RecyclerView.Adapter<ViewHolder>() {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                TextView tv = new TextView(parent.getContext());
                tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                tv.setPadding(20, 30, 20, 30);
                tv.setTextColor(Color.WHITE);
                tv.setTypeface(Typeface.MONOSPACE);
                tv.setTextSize(16);
                return new ViewHolder(tv);
            }

            @Override
            public void onBindViewHolder(ViewHolder holder, int position) {
                String fileName = configFiles.get(position);
                ((TextView) holder.itemView).setText("- " + fileName);
                holder.itemView.setOnClickListener(v -> {
                    if (fileName.equals("Fonts")) {
                        File tui = Tuils.getFolder();
                        File fontsDir = new File(tui, "fonts");
                        if (!fontsDir.exists()) {
                            fontsDir.mkdirs();
                        }

                        File[] fonts = fontsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".ttf") || name.toLowerCase().endsWith(".otf"));
                        
                        List<String> options = new ArrayList<>();
                        options.add("Default (System Font)");
                        if (fonts != null) {
                            for (File f : fonts) options.add(f.getName());
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(ThemerActivity.this);
                        builder.setTitle("Select a Font");
                        builder.setItems(options.toArray(new String[0]), (dialog, which) -> {
                            if (which == 0) {
                                applySystemFont();
                            } else {
                                applyFont(fonts[which - 1]);
                            }
                        });
                        builder.show();
                    } else if (fileName.equals("View Crash Log")) {
                        File crashFile = new File(Tuils.getFolder(), "crash.txt");
                        if (!crashFile.exists() || crashFile.length() == 0) {
                            Toast.makeText(ThemerActivity.this, "No crash log found.", Toast.LENGTH_SHORT).show();
                        } else {
                            Intent intent = new Intent(ThemerActivity.this, TuixtActivity.class);
                            intent.putExtra(TuixtActivity.PATH, crashFile.getAbsolutePath());
                            startActivity(intent);
                        }
                    } else {
                        Intent intent = new Intent(ThemerActivity.this, TuixtActivity.class);
                        intent.putExtra(TuixtActivity.PATH, new File(Tuils.getFolder(), fileName).getAbsolutePath());
                        startActivityForResult(intent, LauncherActivity.TUIXT_REQUEST);
                    }
                });
            }

            @Override
            public int getItemCount() {
                return configFiles.size();
            }

            private void applySystemFont() {
                // Set system_font to true in ui.xml
                XMLPrefsManager.XMLPrefsRoot.UI.write(Ui.system_font, "true");

                // Sweep any current font files in root to 'old'
                sweepCurrentFonts();

                finalizeFontChange("System font applied!");
            }

            private void applyFont(File source) {
                // Set system_font to false in ui.xml
                XMLPrefsManager.XMLPrefsRoot.UI.write(Ui.system_font, "false");

                sweepCurrentFonts();

                // Copy selected font to root TUI folder
                File tuiFolder = Tuils.getFolder();
                File dest = new File(tuiFolder, source.getName());
                try {
                    Log.e("TUI-THEMER", "Copying font from " + source.getAbsolutePath() + " to " + dest.getAbsolutePath());
                    Tuils.copy(source, dest);
                    
                    if (dest.exists() && dest.length() > 0) {
                        Log.e("TUI-THEMER", "Copy successful! Size: " + dest.length());
                    } else {
                        Log.e("TUI-THEMER", "Copy failed or file is empty!");
                    }
                    
                    finalizeFontChange("Font applied!");
                } catch (Exception e) {
                    Toast.makeText(ThemerActivity.this, "Error applying font: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            private void sweepCurrentFonts() {
                File tuiFolder = Tuils.getFolder();
                File[] currentFiles = tuiFolder.listFiles();
                if (currentFiles != null) {
                    for (File f : currentFiles) {
                        String name = f.getName().toLowerCase();
                        if (name.endsWith(".ttf") || name.endsWith(".otf")) {
                            Tuils.insertOld(f);
                        }
                    }
                }
            }

            private void finalizeFontChange(String message) {
                // Clear font cache before reload
                Tuils.cancelFont();
                
                Toast.makeText(ThemerActivity.this, message + " Reloading...", Toast.LENGTH_SHORT).show();
                
                // Trigger reload with a slight delay for FS stability
                recyclerView.postDelayed(() -> {
                    if (LauncherActivity.instance != null) {
                        LauncherActivity.instance.reload();
                    }
                    finish();
                }, 500);
            }
        });

        root.addView(recyclerView);
        setContentView(root);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LauncherActivity.TUIXT_REQUEST && resultCode == TuixtActivity.SAVE_PRESSED) {
            if (LauncherActivity.instance != null) {
                LauncherActivity.instance.reload();
            }
            finish();
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
