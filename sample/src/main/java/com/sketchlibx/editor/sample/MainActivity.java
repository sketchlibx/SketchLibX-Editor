package com.sketchlibx.editor.sample;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.sketchlibx.editor.ui.SketchLibXEditorView;

public final class MainActivity extends AppCompatActivity {
    private SketchLibXEditorView editorView;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editorView = new SketchLibXEditorView(this);
        editorView.setFileName("MainActivity.java");
        editorView.setText("package demo;\n\npublic final class MainActivity {\n    public static void main(String[] args) {\n        System.out.println(\"Hello SketchLibX\");\n    }\n}\n");
        setContentView(editorView);
    }
    @Override protected void onDestroy() {
        if (editorView != null) editorView.releaseEditor();
        super.onDestroy();
    }
}
