package com.sketchlibx.editor;

import static org.junit.Assert.assertEquals;

import com.sketchlibx.editor.core.EditorLanguage;

import org.junit.Test;

public class EditorLanguageTest {
    @Test public void detectsCommonLanguages() {
        assertEquals(EditorLanguage.JAVA, EditorLanguage.fromFileName("MainActivity.java"));
        assertEquals(EditorLanguage.KOTLIN, EditorLanguage.fromFileName("Editor.kt"));
        assertEquals(EditorLanguage.MARKDOWN, EditorLanguage.fromFileName("README.md"));
        assertEquals(EditorLanguage.XML, EditorLanguage.fromFileName("layout.xml"));
        assertEquals(EditorLanguage.CPP, EditorLanguage.fromFileName("native.cpp"));
        assertEquals(EditorLanguage.HEADER, EditorLanguage.fromFileName("native.h"));
    }
}
