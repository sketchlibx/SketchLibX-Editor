package com.sketchlibx.editor.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/** Ready-made Activity host. */
public class SketchLibXEditorActivity extends AppCompatActivity {
    public static final String EXTRA_FILE_NAME = "sketchlibx.fileName";
    public static final String EXTRA_CONTENT = "sketchlibx.content";
    private static final String STATE_FILE_NAME = "sketchlibx.state.fileName";
    private static final String STATE_CONTENT = "sketchlibx.state.content";

    protected SketchLibXEditorView editorView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editorView = new SketchLibXEditorView(this);
        Intent intent = getIntent();
        if (savedInstanceState != null) {
            editorView.setFileName(savedInstanceState.getString(STATE_FILE_NAME));
            editorView.setText(savedInstanceState.getString(STATE_CONTENT, ""));
        } else {
            editorView.setFileName(intent != null ? intent.getStringExtra(EXTRA_FILE_NAME) : null);
            editorView.setText(intent != null ? intent.getStringExtra(EXTRA_CONTENT) : null);
        }
        setContentView(editorView);
    }

    public SketchLibXEditorView getEditorView() { return editorView; }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (editorView != null) {
            outState.putString(STATE_FILE_NAME, editorView.getFileName());
            outState.putString(STATE_CONTENT, editorView.getText());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        if (editorView != null) editorView.releaseEditor();
        super.onDestroy();
    }
}
