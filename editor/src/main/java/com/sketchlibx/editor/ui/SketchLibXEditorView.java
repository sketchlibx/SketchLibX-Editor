package com.sketchlibx.editor.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.Locale;

import com.sketchlibx.editor.core.EditorLanguage;
import com.sketchlibx.editor.core.TextMateBootstrap;
import com.sketchlibx.editor.util.EditorSearch;
import com.sketchlibx.editor.util.ReflectionBridge;

import io.github.rosemoe.sora.widget.CodeEditor;

/**
 * Reusable editor surface with an optional toolbar. It can be placed in an Activity,
 * Fragment, Dialog, bottom sheet or any ViewGroup.
 */
public class SketchLibXEditorView extends LinearLayout {
    public interface Listener {
        default void onContentChanged(String text) { }
        default void onFileTypeChanged(EditorLanguage language) { }
        default void onSearchResultCountChanged(int count) { }
    }

    private static final int BG = Color.rgb(15, 17, 22);
    private static final int BAR = Color.rgb(24, 27, 34);
    private static final int BORDER = Color.rgb(51, 57, 68);
    private static final int FG = Color.rgb(231, 235, 243);
    private static final int MUTED = Color.rgb(151, 160, 175);
    private static final int ACCENT = Color.rgb(122, 162, 247);

    private final LinearLayout topBar;
    private final LinearLayout searchBar;
    private final TextView fileTitle;
    private final TextView languageTitle;
    private final TextView matchInfo;
    private final CodeEditor editor;
    private final EditText searchInput;
    private final EditText replaceInput;
    private final TextView caseButton;
    private final TextView regexButton;
    private final TextView wrapButton;

    private EditorLanguage language = EditorLanguage.PLAIN_TEXT;
    private String fileName = "Untitled";
    private boolean caseSensitive;
    private boolean regex;
    private boolean wrapping;
    private float textSizeSp = 15f;
    private Listener listener;

    public SketchLibXEditorView(Context context) {
        this(context, null);
    }

    public SketchLibXEditorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        setBackgroundColor(BG);
        setWillNotDraw(false);

        TextMateBootstrap.initialize(context);

        topBar = new LinearLayout(context);
        topBar.setOrientation(HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(dp(10), dp(7), dp(7), dp(7));
        topBar.setBackgroundColor(BAR);
        addView(topBar, new LayoutParams(-1, dp(48)));

        fileTitle = label(context, "Untitled", 15, FG, true);
        languageTitle = label(context, "Plain Text", 11, MUTED, false);
        LinearLayout titleColumn = new LinearLayout(context);
        titleColumn.setOrientation(VERTICAL);
        titleColumn.setGravity(Gravity.CENTER_VERTICAL);
        titleColumn.addView(fileTitle, new LinearLayout.LayoutParams(-1, dp(23)));
        titleColumn.addView(languageTitle, new LinearLayout.LayoutParams(-1, dp(17)));
        topBar.addView(titleColumn, new LinearLayout.LayoutParams(0, -2, 1f));

        topBar.addView(action(context, "−", v -> zoom(-1)), widthWrap(context));
        topBar.addView(action(context, "100%", v -> resetZoom()), widthWrap(context));
        topBar.addView(action(context, "+", v -> zoom(1)), widthWrap(context));
        wrapButton = action(context, "Wrap", v -> toggleWrap());
        topBar.addView(wrapButton);
        topBar.addView(action(context, "Undo", v -> invokeEditorAction("undo")), widthWrap(context));
        topBar.addView(action(context, "Redo", v -> invokeEditorAction("redo")), widthWrap(context));
        topBar.addView(action(context, "Select", v -> invokeEditorAction("selectAll")), widthWrap(context));
        topBar.addView(action(context, "Find", v -> toggleSearch(true)), widthWrap(context));
        topBar.addView(action(context, "Theme", v -> toggleTheme()), widthWrap(context));

        searchBar = new LinearLayout(context);
        searchBar.setOrientation(HORIZONTAL);
        searchBar.setGravity(Gravity.CENTER_VERTICAL);
        searchBar.setPadding(dp(8), dp(6), dp(8), dp(6));
        searchBar.setBackgroundColor(Color.rgb(21, 24, 30));
        searchBar.setVisibility(GONE);
        addView(searchBar, new LayoutParams(-1, dp(52)));

        searchInput = field(context, "Find…");
        replaceInput = field(context, "Replace…");
        searchInput.setSingleLine(true);
        replaceInput.setSingleLine(true);
        searchInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        replaceInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        searchBar.addView(searchInput, new LinearLayout.LayoutParams(0, dp(38), 1f));
        searchBar.addView(replaceInput, new LinearLayout.LayoutParams(0, dp(38), 1f));
        searchBar.addView(action(context, "↑", v -> findPrevious()), widthWrap(context));
        searchBar.addView(action(context, "↓", v -> findNext()), widthWrap(context));
        searchBar.addView(action(context, "One", v -> replaceCurrent()), widthWrap(context));
        searchBar.addView(action(context, "All", v -> replaceAllMatches()), widthWrap(context));
        caseButton = action(context, "Aa", v -> {
            caseSensitive = !caseSensitive;
            styleToggle(caseButton, caseSensitive);
            refreshMatchCount();
        });
        regexButton = action(context, ".*", v -> {
            regex = !regex;
            styleToggle(regexButton, regex);
            refreshMatchCount();
        });
        searchBar.addView(caseButton);
        searchBar.addView(regexButton);
        matchInfo = label(context, "", 11, MUTED, false);
        matchInfo.setGravity(Gravity.CENTER);
        searchBar.addView(matchInfo, new LinearLayout.LayoutParams(dp(56), -1));
        searchBar.addView(action(context, "×", v -> toggleSearch(false)), widthWrap(context));

        editor = new CodeEditor(context);
        editor.setTypefaceText(Typeface.MONOSPACE);
        ReflectionBridge.call(editor, "setTextSize", new Class<?>[]{float.class}, textSizeSp);
        editor.setNonPrintablePaintingFlags(
                CodeEditor.FLAG_DRAW_WHITESPACE_LEADING |
                CodeEditor.FLAG_DRAW_LINE_SEPARATOR |
                CodeEditor.FLAG_DRAW_WHITESPACE_IN_SELECTION
        );
        TextMateBootstrap.applyTheme(editor, true);
        addView(editor, new LayoutParams(-1, 0, 1f));

        editor.subscribeAlways(io.github.rosemoe.sora.event.ContentChangeEvent.class, event -> {
            if (listener != null) listener.onContentChanged(getText());
            if (searchBar.getVisibility() == VISIBLE) refreshMatchCount();
        });

        styleToggle(caseButton, false);
        styleToggle(regexButton, false);
    }

    public CodeEditor getEditor() { return editor; }

    public void setListener(@Nullable Listener listener) { this.listener = listener; }

    public void setFileName(@Nullable String name) {
        fileName = name == null || name.trim().isEmpty() ? "Untitled" : name.trim();
        language = EditorLanguage.fromFileName(fileName);
        fileTitle.setText(fileName);
        languageTitle.setText(language.getDisplayName());
        if (listener != null) listener.onFileTypeChanged(language);
    }

    public String getFileName() { return fileName; }

    public EditorLanguage getLanguage() { return language; }

    public void setLanguage(@Nullable EditorLanguage value) {
        language = value == null ? EditorLanguage.PLAIN_TEXT : value;
        languageTitle.setText(language.getDisplayName());
        if (listener != null) listener.onFileTypeChanged(language);
    }

    public void setText(@Nullable CharSequence text) {
        editor.setText(text == null ? "" : text.toString());
    }

    public String getText() {
        CharSequence text = editor.getText();
        return text == null ? "" : text.toString();
    }

    public void openSearch() { toggleSearch(true); }
    public void closeSearch() { toggleSearch(false); }

    public void releaseEditor() {
        editor.release();
    }

    private void toggleSearch(boolean show) {
        searchBar.setVisibility(show ? VISIBLE : GONE);
        if (show) {
            searchInput.requestFocus();
            refreshMatchCount();
        }
    }

    private void findNext() { findMatch(true); }
    private void findPrevious() { findMatch(false); }

    private void findMatch(boolean forward) {
        String query = searchInput.getText().toString();
        List<EditorSearch.Match> matches = EditorSearch.findAll(getText(), query, caseSensitive, regex);
        if (listener != null) listener.onSearchResultCountChanged(matches.size());
        matchInfo.setText(matches.isEmpty() ? "0" : String.format(Locale.ROOT, "%d", matches.size()));
        if (matches.isEmpty()) return;
        EditorSearch.Match chosen = matches.get(0);
        Object currentStartObject = ReflectionBridge.callForResult(editor, "getCursorLeft", new Class<?>[]{});
        int currentStart = currentStartObject instanceof Number ? ((Number) currentStartObject).intValue() : -1;
        if (currentStart >= 0) {
            if (forward) {
                for (EditorSearch.Match match : matches) {
                    if (match.start > currentStart) { chosen = match; break; }
                }
            } else {
                for (int i = matches.size() - 1; i >= 0; i--) {
                    if (matches.get(i).end < currentStart) { chosen = matches.get(i); break; }
                }
            }
        }
        select(chosen.start, chosen.end);
    }

    private void replaceCurrent() {
        String query = searchInput.getText().toString();
        String replacement = replaceInput.getText().toString();
        if (query.isEmpty()) return;
        Object startObject = ReflectionBridge.callForResult(editor, "getSelectionStart", new Class<?>[]{});
        Object endObject = ReflectionBridge.callForResult(editor, "getSelectionEnd", new Class<?>[]{});
        if (!(startObject instanceof Number) || !(endObject instanceof Number)) {
            findNext();
            return;
        }
        int start = ((Number) startObject).intValue();
        int end = ((Number) endObject).intValue();
        String current = getText();
        if (start < 0 || end <= start || end > current.length()) {
            findNext();
            return;
        }
        String selected = current.substring(start, end);
        boolean same = regex ? selected.matches(query) : (caseSensitive ? selected.equals(query) : selected.equalsIgnoreCase(query));
        if (!same) {
            findNext();
            return;
        }
        String updated = current.substring(0, start) + replacement + current.substring(end);
        editor.setText(updated);
        select(start, start + replacement.length());
        refreshMatchCount();
    }

    private void replaceAllMatches() {
        String query = searchInput.getText().toString();
        if (query.isEmpty()) return;
        String updated = EditorSearch.replaceAll(getText(), query, replaceInput.getText().toString(), caseSensitive, regex);
        if (!updated.equals(getText())) editor.setText(updated);
        refreshMatchCount();
    }

    private void refreshMatchCount() {
        List<EditorSearch.Match> matches = EditorSearch.findAll(getText(), searchInput.getText().toString(), caseSensitive, regex);
        matchInfo.setText(String.format(Locale.ROOT, "%d", matches.size()));
        if (listener != null) listener.onSearchResultCountChanged(matches.size());
    }

    private void select(int start, int end) {
        if (!ReflectionBridge.call(editor, "setSelection", new Class<?>[]{int.class, int.class}, start, end)) {
            ReflectionBridge.call(editor, "setSelection", new Class<?>[]{int.class}, start);
        }
    }

    private void zoom(int delta) {
        textSizeSp = Math.max(10f, Math.min(32f, textSizeSp + (delta * 1.5f)));
        applyTextSize();
    }

    private void resetZoom() {
        textSizeSp = 15f;
        applyTextSize();
    }

    private void applyTextSize() {
        ReflectionBridge.call(editor, "setTextSize", new Class<?>[]{float.class}, textSizeSp);
        ReflectionBridge.call(editor, "setTextSize", new Class<?>[]{int.class, float.class}, 0, textSizeSp);
    }

    private void toggleWrap() {
        wrapping = !wrapping;
        if (!ReflectionBridge.call(editor, "setWordwrapEnabled", new Class<?>[]{boolean.class}, wrapping)) {
            ReflectionBridge.call(editor, "setWordWrapEnabled", new Class<?>[]{boolean.class}, wrapping);
        }
        styleToggle(wrapButton, wrapping);
    }

    private void invokeEditorAction(String method) {
        if (!ReflectionBridge.call(editor, method, new Class<?>[]{})) {
            if ("selectAll".equals(method)) {
                select(0, getText().length());
            }
        }
    }

    private void toggleTheme() {
        darkTheme = !darkTheme;
        TextMateBootstrap.applyTheme(editor, darkTheme);
    }

    private boolean darkTheme = true;

    private static TextView label(Context c, String text, float sp, int color, boolean bold) {
        TextView view = new TextView(c);
        view.setText(text);
        view.setTextColor(color);
        view.setTextSize(sp);
        view.setGravity(Gravity.CENTER_VERTICAL);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private static TextView action(Context c, String text, OnClickListener listener) {
        TextView v = label(c, text, 12, FG, false);
        v.setGravity(Gravity.CENTER);
        v.setPadding(dp(c, 7), 0, dp(c, 7), 0);
        v.setOnClickListener(listener);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.TRANSPARENT);
        bg.setStroke(dp(c, 1), BORDER);
        bg.setCornerRadius(dp(c, 8));
        v.setBackground(bg);
        return v;
    }

    private static EditText field(Context c, String hint) {
        EditText e = new EditText(c);
        e.setHint(hint);
        e.setHintTextColor(MUTED);
        e.setTextColor(FG);
        e.setTextSize(13);
        e.setSingleLine(true);
        e.setPadding(dp(c, 10), 0, dp(c, 10), 0);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.rgb(30, 34, 42));
        bg.setCornerRadius(dp(c, 8));
        bg.setStroke(dp(c, 1), BORDER);
        e.setBackground(bg);
        return e;
    }

    private static void styleToggle(TextView view, boolean active) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(active ? Color.rgb(46, 64, 98) : Color.TRANSPARENT);
        bg.setStroke(dp(view.getContext(), 1), active ? ACCENT : BORDER);
        bg.setCornerRadius(dp(view.getContext(), 8));
        view.setBackground(bg);
    }

    private static LinearLayout.LayoutParams widthWrap(Context context) {
        return new LinearLayout.LayoutParams(dp(context, 44), dp(context, 34));
    }

    private static int dp(Context c, int value) { return Math.round(value * c.getResources().getDisplayMetrics().density); }
    private int dp(int value) { return dp(getContext(), value); }
}
