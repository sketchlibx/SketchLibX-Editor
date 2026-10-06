package com.sketchlibx.editor;

import static org.junit.Assert.assertEquals;

import com.sketchlibx.editor.util.EditorSearch;

import org.junit.Test;

public class EditorSearchTest {
    @Test public void literalReplaceHonoursCase() {
        assertEquals("Hello X X", EditorSearch.replaceAll("Hello x X", "x", "X", true, false));
        assertEquals("Hello X X", EditorSearch.replaceAll("Hello x X", "x", "X", false, false));
    }

    @Test public void regexFindWorks() {
        assertEquals(3, EditorSearch.findAll("a1 b22 c333", "\\d+", true, true).size());
    }

    @Test public void regexReplacementKeepsCaptureGroups() {
        assertEquals("item-1 item-22", EditorSearch.replaceAll("1 22", "(\\d+)", "item-$1", true, true));
    }

    @Test public void zeroWidthRegexDoesNotCreateFakeSelections() {
        assertEquals(0, EditorSearch.findAll("abc", "^", true, true).size());
    }
}
