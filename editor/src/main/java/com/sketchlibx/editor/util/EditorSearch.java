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
        int flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        Pattern p;
        try {
            p = Pattern.compile(regex ? query : Pattern.quote(query), flags);
        } catch (RuntimeException badPattern) {
            return result;
        }
        Matcher matcher = p.matcher(text);
        while (matcher.find()) {
            if (matcher.end() == matcher.start()) {
                if (matcher.start() < text.length()) result.add(new Match(matcher.start(), matcher.start() + 1));
            } else {
                result.add(new Match(matcher.start(), matcher.end()));
            }
        }
        return result;
    }

    public static String replaceAll(String text, String query, String replacement, boolean caseSensitive, boolean regex) {
        if (text == null || query == null || query.isEmpty()) return text;
        int flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        try {
            Pattern p = Pattern.compile(regex ? query : Pattern.quote(query), flags);
            Matcher m = p.matcher(text);
            return m.replaceAll(Matcher.quoteReplacement(replacement == null ? "" : replacement));
        } catch (RuntimeException ignored) {
            return text;
        }
    }
}
