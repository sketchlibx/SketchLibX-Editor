package com.sketchlibx.editor.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/** DialogFragment wrapper for editor dialogs and previews. */
public class SketchLibXEditorDialogFragment extends DialogFragment {
    private static final String ARG_FILE_NAME = "fileName";
    private static final String ARG_CONTENT = "content";
    private static final String STATE_FILE_NAME = "sketchlibx.dialog.fileName";
    private static final String STATE_CONTENT = "sketchlibx.dialog.content";
    private SketchLibXEditorView editorView;

    public static SketchLibXEditorDialogFragment newInstance(@Nullable String fileName, @Nullable String content) {
        SketchLibXEditorDialogFragment fragment = new SketchLibXEditorDialogFragment();
        Bundle args = new Bundle(); args.putString(ARG_FILE_NAME, fileName); args.putString(ARG_CONTENT, content); fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        editorView = new SketchLibXEditorView(requireContext());
        Bundle args = getArguments();
        if (savedInstanceState != null) {
            editorView.setFileName(savedInstanceState.getString(STATE_FILE_NAME));
            editorView.setText(savedInstanceState.getString(STATE_CONTENT, ""));
        } else if (args != null) {
            editorView.setFileName(args.getString(ARG_FILE_NAME));
            editorView.setText(args.getString(ARG_CONTENT));
        }
        dialog.setContentView(editorView, new ViewGroup.LayoutParams(-1, -1));
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog == null) return;
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.setGravity(Gravity.CENTER);
        }
    }

    public SketchLibXEditorView getEditorView() { return editorView; }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (editorView != null) {
            outState.putString(STATE_FILE_NAME, editorView.getFileName());
            outState.putString(STATE_CONTENT, editorView.getText());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        if (editorView != null) editorView.releaseEditor();
        editorView = null;
        super.onDestroyView();
    }
}
