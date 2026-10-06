package com.sketchlibx.editor.core;

import android.content.Context;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme;
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage;
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver;
import io.github.rosemoe.sora.widget.CodeEditor;
import org.eclipse.tm4e.core.registry.IThemeSource;

/** One-time TextMate setup owned by SketchLibX. */
public final class TextMateBootstrap {
    private static final String TAG = "SketchLibXTextMate";
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);
    private static final String SCOPE = "source.sketchlibx";
    private static final String THEME_DARK = "sketchlibx-dark";
    private static final String THEME_LIGHT = "sketchlibx-light";
    private static final String ASSET_ROOT = "sketchlibx_editor/textmate/";

    private TextMateBootstrap() { }

    public static void initialize(Context context) {
        if (INITIALIZED.get()) return;
        synchronized (TextMateBootstrap.class) {
            if (INITIALIZED.get()) return;
            Context app = context.getApplicationContext();
            FileProviderRegistry.getInstance().addFileProvider(new AssetsFileResolver(app.getAssets()));

            ThemeRegistry themeRegistry = ThemeRegistry.getInstance();
            boolean darkLoaded = loadTheme(themeRegistry, THEME_DARK, ASSET_ROOT + "sketchlibx-dark.json", true);
            boolean lightLoaded = loadTheme(themeRegistry, THEME_LIGHT, ASSET_ROOT + "sketchlibx-light.json", false);

            // Never let a missing/corrupt optional theme prevent the editor from opening.
            if (darkLoaded) {
                try {
                    themeRegistry.setTheme(THEME_DARK);
                } catch (RuntimeException e) {
                    Log.w(TAG, "Unable to activate dark theme", e);
                }
            } else if (lightLoaded) {
                try {
                    themeRegistry.setTheme(THEME_LIGHT);
                } catch (RuntimeException e) {
                    Log.w(TAG, "Unable to activate light theme", e);
                }
            }

            try {
                GrammarRegistry.getInstance().loadGrammars(ASSET_ROOT + "languages.json");
            } catch (Exception e) {
                Log.w(TAG, "Unable to load bundled TextMate grammar; editor will fall back gracefully", e);
            }
            INITIALIZED.set(true);
        }
    }

    private static boolean loadTheme(ThemeRegistry registry, String name, String assetPath, boolean dark) {
        try {
            java.io.InputStream input = FileProviderRegistry.getInstance().tryGetInputStream(assetPath);
            if (input == null) {
                Log.w(TAG, "Theme asset not found: " + assetPath);
                return false;
            }
            IThemeSource source = IThemeSource.fromInputStream(input, assetPath, null);
            ThemeModel model = new ThemeModel(source, name);
            model.setDark(dark);
            registry.loadTheme(model);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "Unable to load theme " + name, e);
            return false;
        }
    }

    public static void applyTheme(CodeEditor editor, boolean dark) {
        initialize(editor.getContext());
        ThemeRegistry registry = ThemeRegistry.getInstance();

        String themeName = dark ? THEME_DARK : THEME_LIGHT;
        try {
            if (registry.findThemeByThemeName(themeName) != null) {
                registry.setTheme(themeName);
                editor.setColorScheme(TextMateColorScheme.create(registry));
            }
        } catch (RuntimeException e) {
            Log.w(TAG, "Unable to apply TextMate color scheme", e);
        }

        try {
            editor.setEditorLanguage(TextMateLanguage.create(SCOPE, true));
        } catch (RuntimeException e) {
            // Keep the editor usable even if a bundled grammar is damaged or missing.
            Log.w(TAG, "Unable to apply bundled TextMate language; using plain text", e);
            editor.setEditorLanguage(null);
        }
    }
}
