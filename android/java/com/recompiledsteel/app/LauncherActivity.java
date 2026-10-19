package com.recompiledsteel.app;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textview.MaterialTextView;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The first screen: the banner, what is installed, and the buttons. It
 * installs the game from the player's own Xbox 360 files (a folder they pick,
 * scanned like the PC setup programs do), checks that the game and its title
 * update are there before starting, and after a start that failed saves the
 * app's log so it can be sent without a PC.
 */
public class LauncherActivity extends Activity {
    private static final int PICK_FOLDER = 1;
    private static final int PICK_DRIVER = 2;

    private static final int BACKGROUND = Color.rgb(12, 16, 20);
    private static final int CARD = Color.rgb(22, 28, 34);
    private static final int CARD_EDGE = Color.rgb(40, 50, 58);
    private static final int TEAL = Color.rgb(70, 215, 225);
    private static final int TEAL_DARK = Color.rgb(28, 127, 138);
    private static final int TEXT = Color.rgb(225, 230, 234);
    private static final int MUTED = Color.rgb(140, 152, 160);
    private static final int GOOD = Color.rgb(90, 210, 120);
    private static final int WARN = Color.rgb(240, 180, 70);
    private static final int BAD = Color.rgb(240, 95, 90);

    private TextView status;
    private TextView message;
    private TextView footer;
    private View messageContainer;
    private LinearProgressIndicator progress;
    private MaterialButton start;
    private MaterialButton install;
    private MaterialButton access;
    private String crashNote = "";
    private String uiNote = "";
    private boolean installing;
    private boolean awaitingGameReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BACKGROUND);

        MaterialToolbar toolbar = new MaterialToolbar(this);
        toolbar.setTitle("ReCompiled Steel");
        toolbar.setSubtitle("ANDROID  ·  VERSÃO " + BuildConfigVersion.NAME);
        toolbar.setTitleTextColor(TEXT);
        toolbar.setSubtitleTextColor(MUTED);
        toolbar.setBackgroundColor(CARD);
        toolbar.setElevation(dp(3));
        root.addView(toolbar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(68)));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.HORIZONTAL);
        content.setGravity(Gravity.TOP);
        content.setPadding(dp(18), dp(16), dp(18), dp(18));
        scroll.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.08f);
        leftParams.rightMargin = dp(10);
        content.addView(left, leftParams);

        LinearLayout right = new LinearLayout(this);
        right.setOrientation(LinearLayout.VERTICAL);
        content.addView(right, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.92f));

        MaterialCardView hero = new MaterialCardView(this);
        hero.setCardBackgroundColor(CARD);
        hero.setStrokeColor(CARD_EDGE);
        hero.setStrokeWidth(dp(1));
        hero.setRadius(dp(20));
        hero.setCardElevation(dp(2));
        hero.setClipToOutline(true);
        ImageView banner = new ImageView(this);
        banner.setImageResource(R.drawable.launcher_banner);
        banner.setScaleType(ImageView.ScaleType.CENTER_CROP);
        banner.setContentDescription("Real Steel");
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int heroHeight = Math.min(dp(140), Math.max(dp(88), (int)(screenHeight * 0.22f)));
        hero.addView(banner, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, heroHeight));
        LinearLayout.LayoutParams heroParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        heroParams.bottomMargin = dp(12);
        left.addView(hero, heroParams);

        addSectionLabel(left, "ESTADO DO SISTEMA");
        LinearLayout stateCard = card(left);
        status = new MaterialTextView(this);
        status.setTextSize(15);
        status.setTextColor(TEXT);
        status.setLineSpacing(dp(5), 1.05f);
        status.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        stateCard.addView(status);

        progress = new LinearProgressIndicator(this);
        progress.setMax(1000);
        progress.setIndicatorColor(TEAL);
        progress.setTrackColor(CARD_EDGE);
        progress.setVisibility(View.GONE);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressParams.topMargin = dp(12);
        stateCard.addView(progress, progressParams);

        MaterialCardView notice = new MaterialCardView(this);
        notice.setCardBackgroundColor(CARD);
        notice.setStrokeColor(CARD_EDGE);
        notice.setStrokeWidth(dp(1));
        notice.setRadius(dp(16));
        notice.setVisibility(View.GONE);
        messageContainer = notice;
        message = new MaterialTextView(this);
        message.setTextSize(14);
        message.setTextColor(TEXT);
        message.setLineSpacing(dp(3), 1.0f);
        message.setPadding(dp(14), dp(12), dp(14), dp(12));
        notice.addView(message, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams noticeParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        noticeParams.topMargin = dp(10);
        left.addView(notice, noticeParams);

        footer = new MaterialTextView(this);
        footer.setTextSize(12);
        footer.setTextColor(MUTED);
        footer.setLineSpacing(dp(3), 1.0f);
        footer.setPadding(dp(2), dp(14), dp(2), dp(4));
        left.addView(footer);

        LinearLayout actionCard = card(right);
        addSectionLabel(actionCard, "JOGAR");
        access = addMenuButton(actionCard, "Permitir acesso aos arquivos", false,
                v -> askFileAccess());
        start = addMenuButton(actionCard, "Iniciar jogo", true, v -> startGame());
        install = addMenuButton(actionCard, "Instalar / reparar", false, v -> pickFolder());

        addSectionLabel(actionCard, "FERRAMENTAS E AJUSTES");
        addMenuButton(actionCard, "Gráficos e desempenho", false, v -> graphicsSettingsMenu());
        addMenuButton(actionCard, "Driver gráfico", false, v -> driverMenu());
        addMenuButton(actionCard, "Salvar diagnóstico", false, v -> {
            File saved = RunState.saveDiagnostics(this, "diagnostics");
            showMessage(saved != null ? "Diagnóstico salvo em:\n" + saved.getAbsolutePath()
                                      : "Não foi possível salvar o diagnóstico. Verifique o espaço disponível.");
            refresh();
        });
        addMenuButton(actionCard, "Sair", false, v -> finish());

        setContentView(root);
        checkLastRun();
    }

    private void addSectionLabel(LinearLayout parent, String text) {
        TextView label = new MaterialTextView(this);
        label.setText(text);
        label.setTextColor(TEAL);
        label.setTextSize(12);
        label.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        label.setLetterSpacing(0.12f);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(14);
        params.bottomMargin = dp(6);
        parent.addView(label, params);
    }

    private LinearLayout card(LinearLayout parent) {
        MaterialCardView surface = new MaterialCardView(this);
        surface.setCardBackgroundColor(CARD);
        surface.setStrokeColor(CARD_EDGE);
        surface.setStrokeWidth(dp(1));
        surface.setRadius(dp(20));
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        surface.addView(card, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(8);
        parent.addView(surface, params);
        return card;
    }

    private MaterialButton addMenuButton(LinearLayout parent, String label, boolean primary,
                                         View.OnClickListener listener) {
        MaterialButton button = new MaterialButton(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(14);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setMinHeight(dp(52));
        button.setInsetTop(0);
        button.setInsetBottom(0);
        button.setMaxLines(2);
        button.setCornerRadius(dp(16));
        button.setBackgroundTintList(ColorStateList.valueOf(primary ? TEAL : CARD));
        button.setTextColor(primary ? Color.rgb(0, 48, 52) : TEXT);
        button.setStrokeWidth(primary ? 0 : dp(1));
        button.setStrokeColor(ColorStateList.valueOf(primary ? TEAL : CARD_EDGE));
        button.setRippleColor(ColorStateList.valueOf(Color.rgb(52, 88, 94)));
        button.setFocusable(true);
        button.setOnClickListener(listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(7);
        parent.addView(button, params);
        return button;
    }

    private void showMessage(String text) {
        if (message == null) return;
        uiNote = text == null ? "" : text;
        message.setText(text == null ? "" : text);
        if (messageContainer != null) {
            messageContainer.setVisibility(text == null || text.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Only inspect a fresh result after returning from the game, not after
        // ordinary file-picker/settings round trips.
        if (awaitingGameReturn) {
            awaitingGameReturn = false;
            checkLastRun();
        }
        refresh();
        View focus = access.getVisibility() == View.VISIBLE ? access
                    : start.isEnabled() ? start : install;
        if (focus != null) focus.requestFocus();
    }

    private int dp(float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                                               getResources().getDisplayMetrics());
    }

    /** A start whose process stopped before recording a normal Activity end. */
    private void checkLastRun() {
        String state = RunState.read(this, "run_state");
        String step = RunState.read(this, "run_step");
        boolean failed = false;
        String why = "";
        if (state.startsWith("running ")) {
            // Installing an update ends a running game: not a crash.
            long started = 0;
            long updated = 0;
            try {
                started = Long.parseLong(state.substring(8).trim());
                updated = getPackageManager().getPackageInfo(getPackageName(), 0).lastUpdateTime;
            } catch (Exception ignored) {
            }
            if (updated > started && started != 0) {
                RunState.write(this, "run_state", "reported");
                return;
            }
            failed = true;
            why = "The last start stopped unexpectedly (step: " + step + ").";
        }
        if (!failed) return;
        File saved = RunState.saveDiagnostics(this, "diagnostics_failed_start");
        StringBuilder note = new StringBuilder(why).append("\n");
        note.append(saved != null ? "Diagnostics saved: " + saved.getAbsolutePath()
                                  : "Diagnostics could not be saved.");
        File crash = new File(RunState.logsDir(this), "native_crash.txt");
        if (crash.isFile()) {
            note.append("\nCrash report: ").append(crash.getAbsolutePath());
            try {
                String text = new String(Files.readAllBytes(crash.toPath()), StandardCharsets.UTF_8);
                int from = Math.max(0, text.lastIndexOf("== native crash"));
                String last = text.substring(from);
                note.append("\n\n").append(last.length() > 1800 ? last.substring(0, 1800) : last);
            } catch (Exception ignored) {
            }
        }
        crashNote = note.toString();
        RunState.write(this, "run_state", "reported");
    }

    private void line(SpannableStringBuilder text, int color, String label, String value) {
        int from = text.length();
        text.append("●  ");
        text.setSpan(new ForegroundColorSpan(color), from, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.append(label).append("   ");
        int v = text.length();
        text.append(value).append("\n");
        text.setSpan(new ForegroundColorSpan(MUTED), v, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void refresh() {
        boolean canRead = RunState.hasFileAccess();
        File root = RunState.dataRoot();
        boolean game = new File(root, "game/default.xex").isFile();
        boolean update = new File(root, "game/default.xexp").isFile() && new File(root, "tu/pac").isDirectory();
        File addonDir = new File(root, "user/0000000000000000/584111E0/00000002");
        String[] addonList = addonDir.list();
        int addons = addonList != null ? addonList.length : 0;

        SpannableStringBuilder text = new SpannableStringBuilder();
        line(text, canRead ? GOOD : BAD, "Arquivos",
             canRead ? "acesso permitido" : "permita o acesso abaixo");
        if (canRead) {
            line(text, game ? GOOD : BAD, "Jogo", game ? "instalado" : "não instalado — use Instalar / reparar");
            line(text, update ? GOOD : (game ? BAD : MUTED), "Atualização",
                 update ? "instalada" : "ausente (obrigatória)");
            line(text, addons > 0 ? GOOD : MUTED, "Conteúdo extra", addons > 0 ? addons + " instalado(s)" : "nenhum (opcional)");
        }
        boolean custom = GpuDriver.selected(this) != null;
        line(text, custom ? WARN : MUTED, "Driver gráfico",
             custom ? GpuDriver.label(this) + " — personalizado" : "do sistema (padrão)");
        if (text.length() > 0) text.delete(text.length() - 1, text.length());
        status.setText(text);

        if (!installing) showMessage(crashNote.isEmpty() ? uiNote : crashNote);
        footer.setText("Pasta do jogo: " + root.getAbsolutePath() + "   ·   Logs: "
                       + RunState.logsDir(this).getAbsolutePath() + "\n" + Build.MANUFACTURER + " "
                       + Build.MODEL + ", Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")");
        access.setVisibility(canRead ? View.GONE : View.VISIBLE);
        start.setEnabled(!installing && canRead && game && update);
        install.setEnabled(!installing && canRead);
        install.setText(game && update ? "Reinstalar arquivos" : "Instalar arquivos");
    }

    private void askFileAccess() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            refresh();
            return;
        }
        try {
            startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                                     Uri.parse("package:" + getPackageName())));
        } catch (Exception e) {
            try {
                startActivity(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
            } catch (Exception fallbackError) {
                showMessage("Não foi possível abrir as permissões de arquivos. Ative o acesso nas configurações do Android e volte para este menu.\n"
                            + fallbackError.getMessage());
            }
        }
    }

    // ---------------------------------------------------------------- install

    private void pickFolder() {
        crashNote = "";
        showMessage("Selecione a pasta que contém os arquivos do jogo e da atualização. As subpastas também serão verificadas.");
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        try {
            startActivityForResult(intent, PICK_FOLDER);
        } catch (Exception e) {
            showMessage("Este dispositivo não conseguiu abrir o seletor de pastas.\n" + e.getMessage());
        }
    }

    /** The real path of a picked folder on internal storage or an SD card, or null. */
    private static String treeToPath(Uri tree) {
        try {
            String id = DocumentsContract.getTreeDocumentId(tree);
            if (id.startsWith("raw:")) return id.substring(4);
            int colon = id.indexOf(':');
            if (colon < 0) return null;
            String volume = id.substring(0, colon);
            String rel = id.substring(colon + 1);
            File base = "primary".equalsIgnoreCase(volume) ? Environment.getExternalStorageDirectory()
                                                           : new File("/storage/" + volume);
            File folder = rel.isEmpty() ? base : new File(base, rel);
            return folder.isDirectory() ? folder.getAbsolutePath() : null;
        } catch (Exception e) {
            return null;
        }
    }

    // ------------------------------------------------------- graphics settings

    private static final String CFG_RESOLUTION = "realsteel_native_resolution";
    private static final String CFG_AA = "realsteel_native_aa";
    private static final String CFG_ANISOTROPY = "realsteel_native_anisotropy";
    private static final String CFG_CROWD = "realsteel_native_crowd";
    private static final String CFG_TEXTURES = "realsteel_native_texture_quality";
    private static final String CFG_DOF = "realsteel_native_dof";
    private static final String CFG_KEEP_SPEED = "realsteel_native_keep_speed";
    private static final String CFG_FRAME_SKIP = "realsteel_native_frame_skip";
    private static final String CFG_FPS_CAP = "realsteel_guest_fps_cap";

    /** Defaults match the Android renderer; existing non-graphics settings are left alone. */
    private Map<String, String> readGraphicsSettings() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put(CFG_RESOLUTION, "1280x720");
        values.put(CFG_AA, "off");
        values.put(CFG_ANISOTROPY, "2");
        values.put(CFG_CROWD, "true");
        values.put(CFG_TEXTURES, "full");
        values.put(CFG_DOF, "false");
        values.put(CFG_KEEP_SPEED, "true");
        values.put(CFG_FRAME_SKIP, "0");
        values.put(CFG_FPS_CAP, "60");
        File config = new File(RunState.dataRoot(), "realsteel_settings.cfg");
        if (!config.isFile()) return values;
        try {
            for (String line : Files.readAllLines(config.toPath(), StandardCharsets.UTF_8)) {
                int equals = line.indexOf('=');
                if (equals <= 0 || line.startsWith("#")) continue;
                String key = line.substring(0, equals).trim();
                if (values.containsKey(key)) values.put(key, line.substring(equals + 1).trim());
            }
        } catch (Exception ignored) {
        }
        return values;
    }

    private void graphicsSettingsMenu() {
        if (!RunState.hasFileAccess()) {
            new MaterialAlertDialogBuilder(this)
                .setTitle("Acesso a arquivos necessário")
                .setMessage("As opções gráficas são salvas junto às configurações do jogo. Permita o acesso aos arquivos para continuar.")
                .setPositiveButton("Permitir acesso", (d, w) -> askFileAccess())
                .setNegativeButton("Agora não", null)
                .show();
            return;
        }
        Map<String, String> current = readGraphicsSettings();
        String[] titles = {
            "Preset rápido", "Resolução interna", "Antisserrilhamento", "Filtro de textura",
            "Multidão", "Qualidade das texturas", "Profundidade de campo",
            "Espera pelo renderizador", "Pular quadros", "Limite de velocidade",
            "Restaurar padrões"
        };
        String[] descriptions = {
            "Combina várias opções para priorizar desempenho ou qualidade.",
            "Menor resolução pode aliviar a GPU, com imagem mais suave.",
            "Reduz bordas serrilhadas; níveis altos podem custar desempenho.",
            "Nitidez das texturas em ângulos inclinados.",
            "Desative para reduzir o trabalho de renderização.",
            "Texturas pela metade usam menos memória.",
            "Efeito visual de profundidade de campo.",
            "Pular esperas ajuda apenas quando o renderizador é o gargalo.",
            "Pula quadros enviados à GPU, sem alterar a simulação. Experimental: pode deixar a imagem entrecortada ou perder efeitos transitórios; só ajuda se o renderizador for o gargalo.",
            "Limites maiores não aceleram a CPU; são experimentais.",
            "Volta às opções recomendadas para Android."
        };
        String[] values = {
            "Toque para escolher", resolutionLabel(current.get(CFG_RESOLUTION)),
            aaLabel(current.get(CFG_AA)), anisotropyLabel(current.get(CFG_ANISOTROPY)),
            onOffLabel(current.get(CFG_CROWD)), textureLabel(current.get(CFG_TEXTURES)),
            onOffLabel(current.get(CFG_DOF)), speedLabel(current.get(CFG_KEEP_SPEED)),
            frameSkipLabel(current.get(CFG_FRAME_SKIP)),
            gameSpeedLabel(current.get(CFG_FPS_CAP)), "Recomendados"
        };
        Runnable[] actions = {
            this::showGraphicsPresetChoice,
            () -> chooseGraphicsSetting(CFG_RESOLUTION,
                new String[] {"180p — mínimo; imagem muito suave", "240p — muito baixo",
                    "360p — desempenho", "480p — equilibrado para aparelhos modestos",
                    "540p — equilibrado", "720p — padrão", "900p", "1080p", "1440p",
                    "2160p / 4K", "2880p"},
                new String[] {"320x180", "426x240", "640x360", "854x480", "960x540",
                    "1280x720", "1600x900", "1920x1080", "2560x1440", "3840x2160",
                    "5120x2880"}, "Resolução interna"),
            () -> chooseGraphicsSetting(CFG_AA,
                new String[] {"Desativado — mais rápido", "FXAA — leve", "MSAA — padrão do jogo",
                    "MSAA + FXAA — melhor imagem, mais lento"},
                new String[] {"off", "fxaa", "msaa", "msaa_fxaa"}, "Antisserrilhamento"),
            () -> chooseGraphicsSetting(CFG_ANISOTROPY,
                new String[] {"Desativado / trilinear", "Anisotrópico 2x", "Anisotrópico 4x",
                    "Anisotrópico 8x", "Anisotrópico 16x"},
                new String[] {"1", "2", "4", "8", "16"}, "Filtro de textura"),
            () -> chooseGraphicsSetting(CFG_CROWD,
                new String[] {"Ativada", "Desativada — pode melhorar desempenho"},
                new String[] {"true", "false"}, "Multidão"),
            () -> chooseGraphicsSetting(CFG_TEXTURES,
                new String[] {"Completa", "Metade — menos memória"},
                new String[] {"full", "half"}, "Qualidade das texturas"),
            () -> chooseGraphicsSetting(CFG_DOF, new String[] {"Ativada", "Desativada"},
                new String[] {"true", "false"}, "Profundidade de campo"),
            () -> chooseGraphicsSetting(CFG_KEEP_SPEED,
                new String[] {"Ativado — não espera pelo renderizador",
                    "Desativado — aguarda o renderizador"},
                new String[] {"true", "false"}, "Espera pelo renderizador"),
            () -> chooseGraphicsSetting(CFG_FRAME_SKIP,
                new String[] {"0 — não pular (padrão)", "1 — pular 1 a cada 2 quadros",
                    "2 — pular 2 a cada 3 quadros", "3 — pular 3 a cada 4 quadros"},
                new String[] {"0", "1", "2", "3"}, "Pular quadros (experimental)"),
            () -> chooseGraphicsSetting(CFG_FPS_CAP,
                new String[] {"50% — limite de 30 FPS", "75% — limite de 45 FPS",
                    "Normal — limite de 60 FPS",
                    "150% — limite de 90 FPS (se o aparelho sustentar)",
                    "200% — limite de 120 FPS (se o aparelho sustentar)",
                    "Sem limite — experimental; não acelera a CPU"},
                new String[] {"30", "45", "60", "90", "120", "0"}, "Limite de velocidade"),
            this::confirmRestoreGraphicsDefaults
        };

        ScrollView scroll = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        TextView intro = new MaterialTextView(this);
        intro.setText("As alterações são salvas e aplicadas na próxima vez que iniciar o jogo.");
        intro.setTextColor(MUTED);
        intro.setTextSize(14);
        intro.setPadding(dp(4), dp(2), dp(4), dp(10));
        list.addView(intro);
        for (int i = 0; i < titles.length; ++i) {
            final int index = i;
            addMaterialPreference(list, titles[i], values[i], descriptions[i], () -> {
                if (activeGraphicsDialog != null) activeGraphicsDialog.dismiss();
                actions[index].run();
            });
        }
        scroll.setFillViewport(false);
        scroll.addView(list, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        activeGraphicsDialog = new MaterialAlertDialogBuilder(this)
            .setTitle("Gráficos e desempenho")
            .setView(scroll)
            .setNegativeButton("Concluído", null)
            .create();
        activeGraphicsDialog.show();
        sizeMaterialDialog(activeGraphicsDialog, 720, 680);
    }

    private AlertDialog activeGraphicsDialog;

    private void addMaterialPreference(LinearLayout parent, String title, String value,
                                      String description, Runnable action) {
        MaterialCardView card = new MaterialCardView(this);
        card.setCardBackgroundColor(CARD);
        card.setStrokeColor(CARD_EDGE);
        card.setStrokeWidth(dp(1));
        card.setRadius(dp(14));
        card.setClickable(action != null);
        card.setFocusable(action != null);
        card.setRippleColor(ColorStateList.valueOf(Color.rgb(40, 64, 72)));
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(12), dp(16), dp(12));
        TextView heading = new MaterialTextView(this);
        heading.setText(title);
        heading.setTextColor(TEXT);
        heading.setTextSize(15);
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        content.addView(heading);
        TextView selected = new MaterialTextView(this);
        selected.setText(value);
        selected.setTextColor(TEAL);
        selected.setTextSize(14);
        selected.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        valueParams.topMargin = dp(3);
        content.addView(selected, valueParams);
        TextView supporting = new MaterialTextView(this);
        supporting.setText(description);
        supporting.setTextColor(MUTED);
        supporting.setTextSize(12);
        LinearLayout.LayoutParams supportingParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        supportingParams.topMargin = dp(3);
        content.addView(supporting, supportingParams);
        card.addView(content, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        if (action != null) card.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = dp(8);
        parent.addView(card, cardParams);
    }

    private void sizeMaterialDialog(AlertDialog dialog, int maxWidthDp, int maxHeightDp) {
        if (dialog.getWindow() == null) return;
        int margin = dp(32);
        int width = Math.min(dp(maxWidthDp), getResources().getDisplayMetrics().widthPixels - margin);
        int height = Math.min(dp(maxHeightDp), getResources().getDisplayMetrics().heightPixels - margin);
        dialog.getWindow().setLayout(Math.max(dp(280), width), Math.max(dp(220), height));
    }

    private void chooseGraphicsSetting(String key, String[] labels, String[] values, String title) {
        String current = readGraphicsSettings().get(key);
        int checked = -1;
        for (int i = 0; i < values.length; ++i) {
            if (values[i].equals(current)) { checked = i; break; }
        }
        new MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                Map<String, String> change = new LinkedHashMap<>();
                change.put(key, values[which]);
                if (saveGraphicsSettings(change)) {
                    dialog.dismiss();
                    graphicsSettingsMenu();
                }
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void showGraphicsPresetChoice() {
        String[] labels = {
            "Extremo baixo — 180p, limite normal",
            "Mínimo — 360p, velocidade normal",
            "Desempenho — 480p, velocidade normal",
            "Equilibrado — 540p, velocidade normal",
            "Alto — 720p, velocidade normal",
            "Ultra — 1080p (pode reduzir FPS)"
        };
        // resolution, AA, anisotropy, crowd, textures, DOF, keep speed, FPS cap
        String[][] presets = {
            {"320x180", "off", "1", "false", "half", "false", "true", "60"},
            {"640x360", "off", "1", "false", "half", "false", "true", "60"},
            {"854x480", "off", "1", "false", "half", "false", "true", "60"},
            {"960x540", "off", "2", "false", "half", "false", "true", "60"},
            {"1280x720", "fxaa", "4", "true", "full", "false", "true", "60"},
            {"1920x1080", "msaa_fxaa", "8", "true", "full", "true", "true", "60"}
        };
        // Keep this list-only for the same Android AlertDialog behavior noted above.
        new MaterialAlertDialogBuilder(this)
            .setTitle("Preset de qualidade")
            .setSingleChoiceItems(labels, -1, (dialog, which) -> {
                if (which < 0 || which >= presets.length) return;
                String[] keys = {CFG_RESOLUTION, CFG_AA, CFG_ANISOTROPY, CFG_CROWD,
                    CFG_TEXTURES, CFG_DOF, CFG_KEEP_SPEED, CFG_FPS_CAP};
                Map<String, String> changes = new LinkedHashMap<>();
                for (int i = 0; i < keys.length; ++i) changes.put(keys[i], presets[which][i]);
                if (saveGraphicsSettings(changes)) {
                    dialog.dismiss();
                    graphicsSettingsMenu();
                }
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void confirmRestoreGraphicsDefaults() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Restaurar padrões gráficos?")
            .setMessage("As opções gráficas voltarão aos valores recomendados para Android. Outras configurações do jogo serão mantidas.")
            .setPositiveButton("Restaurar", (d, w) -> {
                restoreGraphicsDefaults();
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void restoreGraphicsDefaults() {
        Map<String, String> defaults = new LinkedHashMap<>();
        defaults.put(CFG_RESOLUTION, "1280x720");
        defaults.put(CFG_AA, "off");
        defaults.put(CFG_ANISOTROPY, "2");
        defaults.put(CFG_CROWD, "true");
        defaults.put(CFG_TEXTURES, "full");
        defaults.put(CFG_DOF, "false");
        defaults.put(CFG_KEEP_SPEED, "true");
        defaults.put(CFG_FRAME_SKIP, "0");
        defaults.put(CFG_FPS_CAP, "60");
        saveGraphicsSettings(defaults);
    }

    /** Merge only graphics keys so controls, audio and online settings are preserved. */
    private boolean saveGraphicsSettings(Map<String, String> changes) {
        if (!RunState.hasFileAccess()) {
            graphicsSettingsMenu();
            return false;
        }
        try {
            File root = RunState.dataRoot();
            if (!root.isDirectory() && !root.mkdirs()) throw new IllegalStateException("Cannot create " + root);
            File config = new File(root, "realsteel_settings.cfg");
            List<String> oldLines = config.isFile()
                ? Files.readAllLines(config.toPath(), StandardCharsets.UTF_8) : new ArrayList<>();
            List<String> newLines = new ArrayList<>();
            Set<String> updated = new HashSet<>();
            if (oldLines.isEmpty()) newLines.add("# Real Steel settings (updated by Android launcher)");
            for (String line : oldLines) {
                int equals = line.indexOf('=');
                if (equals > 0) {
                    String key = line.substring(0, equals).trim();
                    if (changes.containsKey(key)) {
                        newLines.add(key + "=" + changes.get(key));
                        updated.add(key);
                        continue;
                    }
                }
                newLines.add(line);
            }
            for (Map.Entry<String, String> entry : changes.entrySet()) {
                if (!updated.contains(entry.getKey())) newLines.add(entry.getKey() + "=" + entry.getValue());
            }
            Files.write(config.toPath(), newLines, StandardCharsets.UTF_8);
            crashNote = "Opções gráficas salvas. Serão aplicadas na próxima vez que você iniciar o jogo.";
            refresh();
            return true;
        } catch (Exception e) {
            new MaterialAlertDialogBuilder(this)
                .setTitle("Não foi possível salvar os gráficos")
                .setMessage(String.valueOf(e.getMessage()))
                .setPositiveButton("Fechar", null)
                .show();
            return false;
        }
    }

    private String resolutionLabel(String value) {
        if ("320x180".equals(value)) return "180p (muito baixa)";
        if ("426x240".equals(value)) return "240p (ultrabaixa)";
        if ("640x360".equals(value)) return "360p";
        if ("854x480".equals(value)) return "480p";
        if ("960x540".equals(value)) return "540p";
        if ("1280x720".equals(value)) return "720p (padrão)";
        if ("1600x900".equals(value)) return "900p";
        if ("1920x1080".equals(value)) return "1080p";
        if ("2560x1440".equals(value)) return "1440p";
        if ("3840x2160".equals(value)) return "2160p / 4K";
        if ("5120x2880".equals(value)) return "2880p";
        return value + " (personalizada)";
    }

    private String aaLabel(String value) {
        if ("msaa".equals(value)) return "MSAA";
        if ("msaa_fxaa".equals(value)) return "MSAA + FXAA";
        if ("fxaa".equals(value)) return "FXAA";
        return "Desativado";
    }

    private String anisotropyLabel(String value) {
        return "1".equals(value) ? "Desativado / trilinear" : value + "x";
    }

    private String textureLabel(String value) {
        return "half".equals(value) ? "Metade" : "Completa";
    }

    private String onOffLabel(String value) {
        return "true".equalsIgnoreCase(value) ? "Ativado" : "Desativado";
    }

    private String speedLabel(String value) {
        return "true".equalsIgnoreCase(value)
            ? "Ativado (renderizador)" : "Desativado (pode aguardar)";
    }

    private String frameSkipLabel(String value) {
        if ("1".equals(value)) return "Pular 1 de cada 2 (até ~30 quadros/s)";
        if ("2".equals(value)) return "Pular 2 de cada 3 (até ~20 quadros/s)";
        if ("3".equals(value)) return "Pular 3 de cada 4 (até ~15 quadros/s)";
        return "Desativado (padrão)";
    }

    private String gameSpeedLabel(String value) {
        if ("30".equals(value)) return "50% (30 FPS)";
        if ("45".equals(value)) return "75% (45 FPS)";
        if ("90".equals(value)) return "150% (limite 90 FPS)";
        if ("120".equals(value)) return "200% (limite 120 FPS)";
        if ("0".equals(value)) return "Sem limite (experimental)";
        return "Normal (60 FPS)";
    }

    // --------------------------------------------------------- graphics driver

    private static final String DRIVER_WARNING =
        "Drivers gráficos personalizados não são produzidos, testados nem compatibilizados pelo ReCompiled Steel.\n\n"
        + "O uso é opcional e por sua conta e risco. Um driver pode causar falhas, imagem incorreta ou comportamento inesperado. "
        + "Ele é carregado dentro do app e compartilha as permissões de arquivos do app. Use somente uma fonte confiável.\n\n"
        + "Se ocorrer um problema, volte para o driver do sistema. Você pode fazer isso a qualquer momento neste menu.";

    private void driverMenu() {
        boolean custom = GpuDriver.selected(this) != null;
        ScrollView scroll = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        TextView intro = new MaterialTextView(this);
        intro.setText("O jogo usa o driver Vulkan do Android por padrão. Drivers personalizados são opcionais e podem causar instabilidade.");
        intro.setTextColor(MUTED);
        intro.setTextSize(14);
        intro.setPadding(dp(4), dp(2), dp(4), dp(10));
        content.addView(intro);
        addMaterialPreference(content, "Driver em uso",
            custom ? "Personalizado" : "Sistema (recomendado)", GpuDriver.label(this), null);
        final AlertDialog[] dialogRef = new AlertDialog[1];
        addMaterialPreference(content, custom ? "Escolher outro driver" : "Selecionar driver",
            "Arquivo .so ou pacote ZIP", "Somente bibliotecas Vulkan arm64 confiáveis.", () -> {
                if (dialogRef[0] != null) dialogRef[0].dismiss();
                driverWarning();
            });
        if (custom) {
            addMaterialPreference(content, "Voltar ao driver do sistema", "Recomendado para diagnosticar problemas",
                "Desativa e remove o driver personalizado selecionado.", () -> {
                    if (dialogRef[0] != null) dialogRef[0].dismiss();
                    GpuDriver.useSystem(this);
                    crashNote = "Driver gráfico do sistema selecionado.";
                    refresh();
                });
        }
        scroll.addView(content, new ScrollView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        dialogRef[0] = new MaterialAlertDialogBuilder(this)
            .setTitle("Driver gráfico Vulkan")
            .setView(scroll)
            .setNegativeButton("Concluído", null)
            .create();
        dialogRef[0].show();
        sizeMaterialDialog(dialogRef[0], 680, 540);
    }

    private void driverWarning() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Driver personalizado — atenção")
            .setMessage(DRIVER_WARNING)
            .setPositiveButton("Entendi, escolher arquivo", (d, w) -> {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                try {
                    startActivityForResult(intent, PICK_DRIVER);
                } catch (Exception e) {
                    showMessage("Não foi possível abrir o seletor de arquivos. O menu continua disponível.\n"
                                + e.getMessage());
                }
            })
            .setNegativeButton("Voltar", null)
            .show();
    }

    private String displayName(Uri uri) {
        try (Cursor c = getContentResolver().query(uri, new String[] {OpenableColumns.DISPLAY_NAME},
                                                   null, null, null)) {
            if (c != null && c.moveToFirst()) return c.getString(0);
        } catch (Exception ignored) {
        }
        String last = uri.getLastPathSegment();
        return last != null ? last : "driver";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_DRIVER) {
            if (resultCode != RESULT_OK || data == null || data.getData() == null) {
                showMessage("Seleção cancelada. Você voltou ao menu.");
                return;
            }
            Uri uri = data.getData();
            String name = displayName(uri);
            showMessage("Lendo " + name + "…");
            new Thread(() -> {
                String error;
                try {
                    error = GpuDriver.install(this, uri, name);
                } catch (Throwable t) {
                    error = t.getClass().getSimpleName() + ": " + String.valueOf(t.getMessage());
                }
                final String result = error;
                runOnUiThread(() -> {
                    crashNote = result.isEmpty()
                        ? "Driver gráfico selecionado: " + GpuDriver.label(this)
                          + "\nSerá usado na próxima inicialização. Você pode voltar ao driver do sistema em Graphics driver."
                        : "O driver não foi instalado. " + result;
                    refresh();
                });
            }, "RS driver").start();
            return;
        }
        if (requestCode != PICK_FOLDER) return;
        if (resultCode != RESULT_OK || data == null || data.getData() == null) {
            showMessage("Seleção cancelada. Você voltou ao menu.");
            return;
        }
        String folder = treeToPath(data.getData());
        if (folder == null) {
            showMessage("Não foi possível acessar essa pasta diretamente. Escolha uma pasta no armazenamento do aparelho ou no cartão SD (não um atalho da nuvem/Downloads).");
            return;
        }
        showMessage("Verificando arquivos em:\n" + folder);
        new Thread(() -> {
            Map<String, String> found = null;
            String error = "";
            try {
                found = Installer.find(folder);
            } catch (Throwable t) {
                error = t.getClass().getSimpleName() + ": " + String.valueOf(t.getMessage());
            }
            final Map<String, String> result = found;
            final String failure = error;
            runOnUiThread(() -> {
                if (!failure.isEmpty() || result == null) {
                    showMessage("Não foi possível verificar essa pasta. O menu continua aberto.\n" + failure);
                } else {
                    confirmInstall(folder, result);
                }
            });
        }, "RS scan").start();
    }

    private void confirmInstall(String folder, Map<String, String> found) {
        String game = found.getOrDefault("game", "");
        String update = found.getOrDefault("update", "");
        String addons = found.getOrDefault("addons", "0");
        if (game.isEmpty()) {
            showMessage("O pacote do jogo não foi encontrado nessa pasta (" + found.getOrDefault("looked", "0")
                        + " arquivos verificados). Confira a pasta e tente novamente.");
            return;
        }
        if (update.isEmpty()) {
            showMessage("O jogo foi encontrado, mas falta a atualização obrigatória (Title Update). Inclua o pacote na pasta de origem e tente novamente.");
            return;
        }
        showMessage("");
        new MaterialAlertDialogBuilder(this)
            .setTitle("Instalar Real Steel")
            .setMessage("Origem: " + folder + "\n\n• Jogo: " + game + "\n• Atualização: " + update
                        + "\n• Add-ons: " + addons + "\n\nDestino: " + RunState.dataRoot().getAbsolutePath()
                        + " (aprox. 210 MB, mais add-ons). Os saves serão mantidos.")
            .setPositiveButton("Instalar", (d, w) -> runInstall(folder))
            .setNegativeButton("Voltar", null)
            .show();
    }

    private void runInstall(String folder) {
        installing = true;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        progress.setProgress(0);
        progress.setVisibility(View.VISIBLE);
        showMessage("Preparando a instalação…");
        refresh();
        File root = RunState.dataRoot();
        ArrayDeque<String> lines = new ArrayDeque<>();
        new Thread(() -> {
            String error;
            try {
                error = Installer.run(folder, root.getAbsolutePath(), new File(root, "user").getAbsolutePath(),
                    new Installer.Listener() {
                        @Override
                        public void onProgress(double fraction, String what) {
                            runOnUiThread(() -> {
                                progress.setProgress((int) (fraction * 1000));
                                showMessage(what + "… " + (int) (fraction * 100) + "%");
                            });
                        }

                        @Override
                        public void onLog(String line) {
                            synchronized (lines) {
                                lines.addLast(line);
                                while (lines.size() > 6) lines.removeFirst();
                            }
                        }
                    });
            } catch (Throwable t) {
                error = t.getClass().getSimpleName() + ": " + String.valueOf(t.getMessage());
            }
            final String result = error == null ? "Instalação interrompida sem detalhes." : error;
            runOnUiThread(() -> {
                installing = false;
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                progress.setVisibility(View.GONE);
                refresh();
                if (result.isEmpty()) {
                    showMessage("Instalação concluída. O menu permanece aberto; toque em Iniciar jogo quando quiser.");
                    start.requestFocus();
                } else {
                    StringBuilder text = new StringBuilder("A instalação não foi concluída. O menu continua aberto.\n")
                        .append(result);
                    synchronized (lines) {
                        for (String line : lines) text.append("\n").append(line);
                    }
                    showMessage(text.toString());
                }
            });
        }, "RS install").start();
    }

    private void startGame() {
        if (installing) return;
        if (!RunState.hasFileAccess()) {
            showMessage("A permissão de arquivos está desativada. Conceda o acesso e tente novamente.");
            refresh();
            return;
        }
        File root = RunState.dataRoot();
        File game = new File(root, "game/default.xex");
        File update = new File(root, "game/default.xexp");
        File updateData = new File(root, "tu/pac");
        if (!game.isFile() || game.length() == 0 || !update.isFile() || update.length() == 0
                || !updateData.isDirectory()) {
            showMessage("Os arquivos obrigatórios do jogo ou da atualização estão ausentes/incompletos.\n"
                        + "O app continua aberto: use Instalar / reparar para selecionar a pasta de origem.");
            refresh();
            return;
        }
        crashNote = "";
        try {
            start.setEnabled(false);
            awaitingGameReturn = true;
            startActivity(new Intent(this, RealSteelActivity.class));
            // Keep LauncherActivity underneath the isolated game process.
            // It resumes here after a normal exit or a native crash.
        } catch (Exception e) {
            awaitingGameReturn = false;
            showMessage("Não foi possível iniciar o jogo. O launcher continua aberto.\n"
                        + e.getClass().getSimpleName() + ": " + e.getMessage());
            refresh();
        }
    }
}
