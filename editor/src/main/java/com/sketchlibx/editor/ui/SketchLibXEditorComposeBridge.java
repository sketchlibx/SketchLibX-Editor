package com.sketchlibx.editor.ui;

import android.content.Context;
import android.view.View;

/** Tiny bridge for Java/Kotlin/Compose hosts that use AndroidView. */
public final class SketchLibXEditorComposeBridge {
    private SketchLibXEditorComposeBridge() { }

    public static SketchLibXEditorView create(Context context) {
        return new SketchLibXEditorView(context);
    }

    public static View asView(SketchLibXEditorView view) {
        return view;
    }
}
