package com.sketchlibx.editor.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.Locale;

import com.sketchlibx.editor.core.EditorLanguage;
import com.sketchlibx.editor.core.TextMateBootstrap;
import com.sketchlibx.editor.util.EditorSearch;

import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.event.ContentChangeEvent;

/** Reusable editor surface that can be embedded in any Android ViewGroup. */
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
    private boolean darkTheme = true;
    private boolean toolbarVisible = true;
    private float textSizeSp = 15f;
    private Listener listener;

    public SketchLibXEditorView(Context context) {
        this(context, null);
    }

    public SketchLibXEditorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        setBackgroundColor(BG);
        TextMateBootstrap.initialize(context);

        topBar = new LinearLayout(context);
        topBar.setOrientation(HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(dp(10), dp(7), dp(7), dp(7));
        topBar.setBackgroundColor(BAR);

        HorizontalScrollView topScroll = new HorizontalScrollView(context);
        topScroll.setHorizontalScrollBarEnabled(false);
        topScroll.setFillViewport(false);
        topScroll.addView(topBar, new HorizontalScrollView.LayoutParams(-2, dp(48)));
        addView(topScroll, new LayoutParams(-1, dp(48)));

        fileTitle = label(context, "Untitled", 15, FG, true);
        fileTitle.setSingleLine(true);
        fileTitle.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        languageTitle = label(context, "Plain Text", 11, MUTED, false);
        LinearLayout titleColumn = new LinearLayout(context);
        titleColumn.setOrientation(VERTICAL);
        titleColumn.setGravity(Gravity.CENTER_VERTICAL);
        titleColumn.addView(fileTitle, new LinearLayout.LayoutParams(dp(180), dp(23)));
        titleColumn.addView(languageTitle, new LinearLayout.LayoutParams(dp(180), dp(17)));
        topBar.addView(titleColumn, new LinearLayout.LayoutParams(dp(190), -2));

        topBar.addView(action(context, "−", v -> zoom(-1)), widthWrap(context));
        topBar.addView(action(context, "100%", v -> resetZoom()), widthWrap(context));
        topBar.addView(action(context, "+", v -> zoom(1)), widthWrap(context));
        wrapButton = action(context, "Wrap", v -> toggleWrap());
        topBar.addView(wrapButton, widthWrap(context));
        topBar.addView(action(context, "Undo", v -> editor.undo()), widthWrap(context));
        topBar.addView(action(context, "Redo", v -> editor.redo()), widthWrap(context));
        topBar.addView(action(context, "Select", v -> editor.selectAll()), widthWrap(context));
        topBar.addView(action(context, "Find", v -> toggleSearch(true)), widthWrap(context));
        topBar.addView(action(context, "Theme", v -> setDarkTheme(!darkTheme)), widthWrap(context));

        searchBar = new LinearLayout(context);
        searchBar.setOrientation(HORIZONTAL);
        searchBar.setGravity(Gravity.CENTER_VERTICAL);
        searchBar.setPadding(dp(8), dp(6), dp(8), dp(6));
        searchBar.setBackgroundColor(Color.rgb(21, 24, 30));
        searchBar.setVisibility(GONE);
        HorizontalScrollView searchScroll = new HorizontalScrollView(context);
        searchScroll.setHorizontalScrollBarEnabled(false);
        searchScroll.addView(searchBar, new HorizontalScrollView.LayoutParams(-2, dp(52)));
        addView(searchScroll, new LayoutParams(-1, dp(52)));

        searchInput = field(context, "Find…");
        replaceInput = field(context, "Replace…");
        searchInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        replaceInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        searchInput.addTextChangedListener(new SimpleTextWatcher() { @Override public void afterTextChanged(android.text.Editable s) { refreshMatchCount(); } });
        searchBar.addView(searchInput, new LinearLayout.LayoutParams(dp(190), dp(38)));
        searchBar.addView(replaceInput, new LinearLayout.LayoutParams(dp(190), dp(38)));
        searchBar.addView(action(context, "↑", v -> findPrevious()), widthWrap(context));
        searchBar.addView(action(context, "↓", v -> findNext()), widthWrap(context));
        searchBar.addView(action(context, "One", v -> replaceCurrent()), widthWrap(context));
        searchBar.addView(action(context, "All", v -> replaceAllMatches()), widthWrap(context));
        caseButton = action(context, "Aa", v -> { caseSensitive = !caseSensitive; styleToggle(caseButton, caseSensitive); refreshMatchCount(); });
        regexButton = action(context, ".*", v -> { regex = !regex; styleToggle(regexButton, regex); refreshMatchCount(); });
        searchBar.addView(caseButton, widthWrap(context));
        searchBar.addView(regexButton, widthWrap(context));
        matchInfo = label(context, "0", 11, MUTED, false);
        matchInfo.setGravity(Gravity.CENTER);
        searchBar.addView(matchInfo, new LinearLayout.LayoutParams(dp(56), -1));
        searchBar.addView(action(context, "×", v -> toggleSearch(false)), widthWrap(context));

        editor = new CodeEditor(context);
        editor.setTypefaceText(Typeface.MONOSPACE);
        editor.setTextSize(textSizeSp);
        editor.setUndoEnabled(true);
        editor.setNonPrintablePaintingFlags(
                CodeEditor.FLAG_DRAW_WHITESPACE_LEADING |
                CodeEditor.FLAG_DRAW_LINE_SEPARATOR |
                CodeEditor.FLAG_DRAW_WHITESPACE_IN_SELECTION
        );
        editor.setWordwrap(false);
        TextMateBootstrap.applyTheme(editor, true);
        addView(editor, new LayoutParams(-1, 0, 1f));

        editor.subscribeAlways(ContentChangeEvent.class, event -> {
            if (listener != null) listener.onContentChanged(getText());
            if (searchBar.getVisibility() == VISIBLE) refreshMatchCount();
        });

        styleToggle(caseButton, false);
        styleToggle(regexButton, false);
        setDarkTheme(true);
    }

    public CodeEditor getEditor() { return editor; }
    public void setListener(@Nullable Listener listener) { this.listener = listener; }

    public void setFileName(@Nullable String name) {
        fileName = name == null || name.trim().isEmpty() ? "Untitled" : name.trim();
        language = EditorLanguage.fromFileName(fileName);
        fileTitle.setText(fileName);
        languageTitle.setText(language.getDisplayName());
        applyLanguage();
        if (listener != null) listener.onFileTypeChanged(language);
    }

    public String getFileName() { return fileName; }
    public EditorLanguage getLanguage() { return language; }

    public void setLanguage(@Nullable EditorLanguage value) {
        language = value == null ? EditorLanguage.PLAIN_TEXT : value;
        languageTitle.setText(language.getDisplayName());
        applyLanguage();
        if (listener != null) listener.onFileTypeChanged(language);
    }

    private void applyLanguage() {
        TextMateBootstrap.applyTheme(editor, darkTheme);
    }

    public void setText(@Nullable CharSequence text) {
        editor.setText(text == null ? "" : text.toString());
    }

    public String getText() {
        CharSequence text = editor.getText();
        return text == null ? "" : text.toString();
    }

    public void setEditable(boolean editable) { editor.setEditable(editable); }
    public boolean isEditable() { return editor.isEditable(); }
    public void setWordWrapEnabled(boolean enabled) { wrapping = enabled; editor.setWordwrap(enabled); styleToggle(wrapButton, enabled); }
    public boolean isWordWrapEnabled() { return editor.isWordwrap(); }
    public void setTextSizeSp(float sizeSp) {
        if (!(sizeSp > 0f)) throw new IllegalArgumentException("sizeSp must be > 0");
        textSizeSp = Math.max(8f, Math.min(48f, sizeSp));
        editor.setTextSize(textSizeSp);
    }
    public float getTextSizeSp() { return textSizeSp; }
    public void setToolbarVisible(boolean visible) { toolbarVisible = visible; getChildAt(0).setVisibility(visible ? VISIBLE : GONE); }
    public boolean isToolbarVisible() { return toolbarVisible; }
    public void setDarkTheme(boolean dark) { darkTheme = dark; TextMateBootstrap.applyTheme(editor, dark); }
    public boolean isDarkTheme() { return darkTheme; }

    public void openSearch() { toggleSearch(true); }
    public void closeSearch() { toggleSearch(false); }
    public void releaseEditor() { editor.release(); }

    private void toggleSearch(boolean show) {
        searchBar.setVisibility(show ? VISIBLE : GONE);
        if (show) { searchInput.requestFocus(); refreshMatchCount(); }
    }
    private void findNext() { findMatch(true); }
    private void findPrevious() { findMatch(false); }

    private void findMatch(boolean forward) {
        String query = searchInput.getText().toString();
        List<EditorSearch.Match> matches = EditorSearch.findAll(getText(), query, caseSensitive, regex);
        updateMatchInfo(matches.size());
        if (matches.isEmpty()) return;
        int currentStart = editor.getCursor() == null ? -1 : editor.getCursor().getLeft();
        EditorSearch.Match chosen = matches.get(0);
        if (currentStart >= 0) {
            if (forward) {
                for (EditorSearch.Match match : matches) if (match.start > currentStart) { chosen = match; break; }
            } else {
                chosen = matches.get(matches.size() - 1);
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
        int start = editor.getCursor().getLeft();
        int end = editor.getCursor().getRight();
        String current = getText();
        if (start < 0 || end <= start || end > current.length()) { findNext(); return; }
        String selected = current.substring(start, end);
        if (!matchesSelection(selected, query)) { findNext(); return; }
        String updated = current.substring(0, start) + computeReplacement(selected, replacement, query) + current.substring(end);
        editor.setText(updated);
        refreshMatchCount();
    }

    private String computeReplacement(String selected, String replacement, String query) {
        if (!regex) return replacement;
        try { return java.util.regex.Pattern.compile(query, regexFlags()).matcher(selected).replaceFirst(replacement == null ? "" : replacement); }
        catch (RuntimeException ignored) { return selected; }
    }

    private boolean matchesSelection(String selected, String query) {
        if (regex) {
            try { return java.util.regex.Pattern.compile(query, regexFlags()).matcher(selected).matches(); }
            catch (RuntimeException ignored) { return false; }
        }
        return caseSensitive ? selected.equals(query) : selected.equalsIgnoreCase(query);
    }

    private int regexFlags() { return java.util.regex.Pattern.MULTILINE | (caseSensitive ? 0 : java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.UNICODE_CASE); }

    private void replaceAllMatches() {
        String query = searchInput.getText().toString();
        if (query.isEmpty()) return;
        String current = getText();
        String updated = EditorSearch.replaceAll(current, query, replaceInput.getText().toString(), caseSensitive, regex);
        if (!updated.equals(current)) editor.setText(updated);
        refreshMatchCount();
    }

    private void refreshMatchCount() { updateMatchInfo(EditorSearch.findAll(getText(), searchInput.getText().toString(), caseSensitive, regex).size()); }
    private void updateMatchInfo(int count) { matchInfo.setText(String.format(Locale.ROOT, "%d", count)); if (listener != null) listener.onSearchResultCountChanged(count); }

    private void select(int start, int end) {
        if (start < 0 || end < start || end > getText().length()) return;
        CharPosition a = editor.getCursor().getIndexer().getCharPosition(start);
        CharPosition b = editor.getCursor().getIndexer().getCharPosition(end);
        editor.setSelectionRegion(a.line, a.column, b.line, b.column);
    }

    private void zoom(int delta) { setTextSizeSp(textSizeSp + delta * 1.5f); }
    private void resetZoom() { setTextSizeSp(15f); }
    private void toggleWrap() { setWordWrapEnabled(!wrapping); }

    private static TextView label(Context c, String text, float sp, int color, boolean bold) {
        TextView view = new TextView(c);
        view.setText(text); view.setTextColor(color); view.setTextSize(sp); view.setGravity(Gravity.CENTER_VERTICAL);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private static TextView action(Context c, String text, OnClickListener listener) {
        TextView v = label(c, text, 12, FG, false);
        v.setGravity(Gravity.CENTER); v.setPadding(dp(c, 7), 0, dp(c, 7), 0); v.setOnClickListener(listener);
        GradientDrawable bg = new GradientDrawable(); bg.setColor(Color.TRANSPARENT); bg.setStroke(dp(c, 1), BORDER); bg.setCornerRadius(dp(c, 8)); v.setBackground(bg);
        return v;
    }

    private static EditText field(Context c, String hint) {
        EditText e = new EditText(c);
        e.setHint(hint); e.setHintTextColor(MUTED); e.setTextColor(FG); e.setTextSize(13); e.setSingleLine(true); e.setPadding(dp(c, 10), 0, dp(c, 10), 0);
        GradientDrawable bg = new GradientDrawable(); bg.setColor(Color.rgb(30, 34, 42)); bg.setCornerRadius(dp(c, 8)); bg.setStroke(dp(c, 1), BORDER); e.setBackground(bg);
        return e;
    }

    private static void styleToggle(TextView view, boolean active) {
        GradientDrawable bg = new GradientDrawable(); bg.setColor(active ? Color.rgb(46, 64, 98) : Color.TRANSPARENT); bg.setStroke(dp(view.getContext(), 1), active ? ACCENT : BORDER); bg.setCornerRadius(dp(view.getContext(), 8)); view.setBackground(bg);
    }

    private static LinearLayout.LayoutParams widthWrap(Context context) { return new LinearLayout.LayoutParams(dp(context, 50), dp(context, 34)); }
    private int dp(int value) { return dp(getContext(), value); }
    private static int dp(Context c, int value) { return Math.round(value * c.getResources().getDisplayMetrics().density); }

    private abstract static class SimpleTextWatcher implements android.text.TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
    }
}
