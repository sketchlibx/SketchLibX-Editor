package com.sketchlibx.editor.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EditorSearch {
    public static final class Match {
        public final int start;
        public final int end;
        public Match(int start, int end) { this.start = start; this.end = end; }
    }

    private EditorSearch() { }

    public static List<Match> findAll(String text, String query, boolean caseSensitive, boolean regex) {
        List<Match> result = new ArrayList<>();
        if (text == null || query == null || query.isEmpty()) return result;
        int flags = Pattern.MULTILINE | (caseSensitive ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        try {
            Pattern pattern = Pattern.compile(regex ? query : Pattern.quote(query), flags);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                // Zero-width matches cannot select a real editor range; skip them safely.
                if (matcher.end() > matcher.start()) {
                    result.add(new Match(matcher.start(), matcher.end()));
                }
            }
        } catch (RuntimeException ignored) {
            // Invalid regex patterns are treated as no matches instead of crashing the editor.
        }
        return result;
    }

    public static String replaceAll(String text, String query, String replacement, boolean caseSensitive, boolean regex) {
        if (text == null || query == null || query.isEmpty()) return text;
        int flags = Pattern.MULTILINE | (caseSensitive ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        try {
            Pattern pattern = Pattern.compile(regex ? query : Pattern.quote(query), flags);
            Matcher matcher = pattern.matcher(text);
            if (regex) {
                // Preserve normal Java regex replacement semantics, including $1 and \1 groups.
                return matcher.replaceAll(replacement == null ? "" : replacement);
            }
            return matcher.replaceAll(Matcher.quoteReplacement(replacement == null ? "" : replacement));
        } catch (RuntimeException ignored) {
            return text;
        }
    }
}
