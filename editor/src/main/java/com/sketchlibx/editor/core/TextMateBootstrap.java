package com.sketchlibx.editor.core;

import android.content.Context;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme;
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage;
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeModel;
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
            loadTheme(themeRegistry, THEME_DARK, ASSET_ROOT + "sketchlibx-dark.json", true);
            loadTheme(themeRegistry, THEME_LIGHT, ASSET_ROOT + "sketchlibx-light.json", false);
            themeRegistry.setTheme(THEME_DARK);
            try {
                GrammarRegistry.getInstance().loadGrammars(ASSET_ROOT + "languages.json");
            } catch (Exception e) {
                Log.w(TAG, "Unable to load bundled TextMate grammar; editor will fall back gracefully", e);
            }
            INITIALIZED.set(true);
        }
    }

    private static void loadTheme(ThemeRegistry registry, String name, String assetPath, boolean dark) {
        try {
            IThemeSource source = IThemeSource.fromInputStream(
                    FileProviderRegistry.getInstance().tryGetInputStream(assetPath),
                    assetPath,
                    null
            );
            ThemeModel model = new ThemeModel(source, name);
            model.setDark(dark);
            registry.loadTheme(model);
        } catch (Exception e) {
            Log.w(TAG, "Unable to load theme " + name, e);
        }
    }

    public static void applyTheme(CodeEditor editor, boolean dark) {
        initialize(editor.getContext());
        ThemeRegistry registry = ThemeRegistry.getInstance();
        registry.setTheme(dark ? THEME_DARK : THEME_LIGHT);
        editor.setColorScheme(TextMateColorScheme.create(registry));
        editor.setEditorLanguage(TextMateLanguage.create(SCOPE, true));
    }
}
