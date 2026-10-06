package com.sketchlibx.editor.ui;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/** Fragment wrapper around SketchLibXEditorView. */
public class SketchLibXEditorFragment extends Fragment {
    private static final String ARG_FILE_NAME = "fileName";
    private static final String ARG_CONTENT = "content";
    private SketchLibXEditorView editorView;

    public static SketchLibXEditorFragment newInstance(@Nullable String fileName, @Nullable String content) {
        SketchLibXEditorFragment fragment = new SketchLibXEditorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILE_NAME, fileName);
        args.putString(ARG_CONTENT, content);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(android.view.LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        editorView = new SketchLibXEditorView(requireContext());
        Bundle args = getArguments();
        if (args != null) {
            editorView.setFileName(args.getString(ARG_FILE_NAME));
            editorView.setText(args.getString(ARG_CONTENT));
        }
        return editorView;
    }

    public SketchLibXEditorView getEditorView() { return editorView; }

    @Override
    public void onDestroyView() {
        if (editorView != null) editorView.releaseEditor();
        editorView = null;
        super.onDestroyView();
    }
}
