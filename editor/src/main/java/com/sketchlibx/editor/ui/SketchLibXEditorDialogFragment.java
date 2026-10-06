package com.sketchlibx.editor.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/** DialogFragment wrapper for quick editor dialogs and code previews. */
public class SketchLibXEditorDialogFragment extends DialogFragment {
    private static final String ARG_FILE_NAME = "fileName";
    private static final String ARG_CONTENT = "content";
    private SketchLibXEditorView editorView;

    public static SketchLibXEditorDialogFragment newInstance(@Nullable String fileName, @Nullable String content) {
        SketchLibXEditorDialogFragment fragment = new SketchLibXEditorDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILE_NAME, fileName);
        args.putString(ARG_CONTENT, content);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        editorView = new SketchLibXEditorView(requireContext());
        Bundle args = getArguments();
        if (args != null) {
            editorView.setFileName(args.getString(ARG_FILE_NAME));
            editorView.setText(args.getString(ARG_CONTENT));
        }
        dialog.setContentView(editorView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.setGravity(Gravity.CENTER);
        }
        return dialog;
    }

    public SketchLibXEditorView getEditorView() { return editorView; }

    @Override
    public void onDestroyView() {
        if (editorView != null) editorView.releaseEditor();
        editorView = null;
        super.onDestroyView();
    }
}
