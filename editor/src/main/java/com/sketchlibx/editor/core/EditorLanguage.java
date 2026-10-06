package com.sketchlibx.editor.core;

import java.util.Locale;

/** Describes the logical language selected for a document. */
public enum EditorLanguage {
    PLAIN_TEXT("Plain Text"),
    JAVA("Java"), KOTLIN("Kotlin"), KTS("Kotlin Script"),
    C("C"), CPP("C++"), HEADER("C/C++ Header"), CSHARP("C#"),
    PYTHON("Python"), JAVASCRIPT("JavaScript"), TYPESCRIPT("TypeScript"),
    JSX("JSX"), TSX("TSX"), HTML("HTML"), CSS("CSS"), SCSS("SCSS"), LESS("LESS"),
    XML("XML"), JSON("JSON"), YAML("YAML"), TOML("TOML"),
    MARKDOWN("Markdown"), SQL("SQL"), SHELL("Shell"), BASH("Bash"),
    PHP("PHP"), RUBY("Ruby"), GO("Go"), RUST("Rust"), SWIFT("Swift"),
    DART("Dart"), GROOVY("Groovy"), LUA("Lua"), PERL("Perl"),
    INI("INI / Properties"), DIFF("Diff / Patch"), CSV("CSV"),
    DOCKERFILE("Dockerfile"), MAKEFILE("Makefile"), GRADLE("Gradle"),
    UNKNOWN("Text / Code");

    private final String displayName;

    EditorLanguage(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    /**
     * The bundled grammar is intentionally universal. Language detection still gives the
     * host app a stable language identity and can later be paired with a registered grammar pack.
     */
    public String getTextMateScope() {
        return "source.sketchlibx";
    }

    public static EditorLanguage fromFileName(String fileName) {
        if (fileName == null) return PLAIN_TEXT;
        String name = fileName.trim().toLowerCase(Locale.ROOT).replace('\\', '/');
        if (name.isEmpty()) return PLAIN_TEXT;

        String base = name.substring(name.lastIndexOf('/') + 1);
        if (base.equals("dockerfile") || base.endsWith(".dockerfile")) return DOCKERFILE;
        if (base.equals("makefile") || base.equals("gnumakefile")) return MAKEFILE;
        if (base.equals("cmakelists.txt") || base.equals("meson.build") || base.equals("build.gradle") || base.equals("settings.gradle")) return GRADLE;
        if (base.equals(".editorconfig") || base.equals(".gitignore") || base.equals(".gitattributes") || base.equals(".gitmodules")) return INI;
        if (base.equals("gemfile") || base.equals("rakefile")) return RUBY;
        if (base.equals("podfile")) return SWIFT;
        if (base.equals("requirements.txt") || base.equals("pipfile")) return PLAIN_TEXT;

        if (name.endsWith(".java")) return JAVA;
        if (name.endsWith(".gradle.kts") || name.endsWith(".gradle")) return GRADLE;
        if (name.endsWith(".kt")) return KOTLIN;
        if (name.endsWith(".kts")) return KTS;
        if (name.endsWith(".c") || name.endsWith(".i")) return C;
        if (name.endsWith(".cc") || name.endsWith(".cpp") || name.endsWith(".cxx") || name.endsWith(".c++") || name.endsWith(".ipp")) return CPP;
        if (name.endsWith(".h") || name.endsWith(".hh") || name.endsWith(".hpp") || name.endsWith(".hxx") || name.endsWith(".h++")) return HEADER;
        if (name.endsWith(".cs")) return CSHARP;
        if (name.endsWith(".py") || name.endsWith(".pyw") || name.endsWith(".pyi")) return PYTHON;
        if (name.endsWith(".js") || name.endsWith(".mjs") || name.endsWith(".cjs")) return JAVASCRIPT;
        if (name.endsWith(".ts")) return TYPESCRIPT;
        if (name.endsWith(".jsx")) return JSX;
        if (name.endsWith(".tsx")) return TSX;
        if (name.endsWith(".html") || name.endsWith(".htm") || name.endsWith(".xhtml") || name.endsWith(".vue")) return HTML;
        if (name.endsWith(".css")) return CSS;
        if (name.endsWith(".scss")) return SCSS;
        if (name.endsWith(".less")) return LESS;
        if (name.endsWith(".xml") || name.endsWith(".axml") || name.endsWith(".svg")) return XML;
        if (name.endsWith(".json") || name.endsWith(".json5") || name.endsWith(".geojson")) return JSON;
        if (name.endsWith(".yaml") || name.endsWith(".yml")) return YAML;
        if (name.endsWith(".toml")) return TOML;
        if (name.endsWith(".md") || name.endsWith(".markdown") || name.endsWith(".mdown") || name.endsWith(".mkd")) return MARKDOWN;
        if (name.endsWith(".sql")) return SQL;
        if (name.endsWith(".sh")) return SHELL;
        if (name.endsWith(".bash")) return BASH;
        if (name.endsWith(".php") || name.endsWith(".phtml")) return PHP;
        if (name.endsWith(".rb") || name.endsWith(".rake")) return RUBY;
        if (name.endsWith(".go")) return GO;
        if (name.endsWith(".rs")) return RUST;
        if (name.endsWith(".swift")) return SWIFT;
        if (name.endsWith(".dart")) return DART;
        if (name.endsWith(".groovy")) return GROOVY;
        if (name.endsWith(".lua")) return LUA;
        if (name.endsWith(".pl") || name.endsWith(".pm")) return PERL;
        if (name.endsWith(".ini") || name.endsWith(".cfg") || name.endsWith(".conf") || name.endsWith(".properties")) return INI;
        if (name.endsWith(".diff") || name.endsWith(".patch")) return DIFF;
        if (name.endsWith(".csv")) return CSV;
        if (name.endsWith(".txt") || name.endsWith(".log") || name.endsWith(".out")) return PLAIN_TEXT;
        return UNKNOWN;
    }
}
