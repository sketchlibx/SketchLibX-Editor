package com.sketchlibx.editor;

import android.content.Context;

import androidx.annotation.NonNull;

import com.sketchlibx.editor.ui.SketchLibXEditorView;

/** Public facade kept intentionally small so host applications do not depend on Sora APIs. */
public final class SketchLibXEditor {
    private SketchLibXEditor() { }

    @NonNull
    public static SketchLibXEditorView create(@NonNull Context context) {
        return new SketchLibXEditorView(context);
    }

    public static String version() {
        return BuildConfig.VERSION_NAME;
    }
}
