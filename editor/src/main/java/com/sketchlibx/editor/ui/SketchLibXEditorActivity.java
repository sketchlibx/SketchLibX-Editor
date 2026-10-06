package com.sketchlibx.editor.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/** Ready-made Activity host. Subclass it or use SketchLibXEditorView directly. */
public class SketchLibXEditorActivity extends AppCompatActivity {
    public static final String EXTRA_FILE_NAME = "sketchlibx.fileName";
    public static final String EXTRA_CONTENT = "sketchlibx.content";

    protected SketchLibXEditorView editorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editorView = new SketchLibXEditorView(this);
        editorView.setFileName(getIntent().getStringExtra(EXTRA_FILE_NAME));
        editorView.setText(getIntent().getStringExtra(EXTRA_CONTENT));
        setContentView(editorView);
    }

    public SketchLibXEditorView getEditorView() { return editorView; }

    @Override
    protected void onDestroy() {
        if (editorView != null) editorView.releaseEditor();
        super.onDestroy();
    }
}
