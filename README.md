# SketchLibX Editor

A reusable Android code and text editor built around [Sora Editor](https://github.com/Rosemoe/sora-editor). It is designed for apps that need an editor without having to build the editing layer from scratch.

## Features

- Syntax highlighting with TextMate support
- Java, Kotlin, XML, C/C++, HTML, CSS, JavaScript, Markdown, JSON and other common text/code files
- Automatic file/language detection
- Undo/redo and automatic indentation
- Search and replace with case-sensitive and regex modes
- Word wrap and editor zoom controls
- Light and dark editor themes
- Ready-to-use View, Activity, Fragment and DialogFragment wrappers
- Works with traditional Android Views and `AndroidView` in Compose

## Requirements

- Android SDK 36
- Minimum Android version: API 21
- JDK 17

## Installation

SketchLibX Editor is distributed through JitPack.

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.sketchlibx:SketchLibX-Editor:1.0.0-alpha1'
}
```

For Kotlin DSL:

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.sketchlibx:SketchLibX-Editor:1.0.0-alpha1")
}
```

## Basic usage

The main reusable component is `SketchLibXEditorView`.

```java
import com.sketchlibx.editor.ui.SketchLibXEditorView;

SketchLibXEditorView editor = new SketchLibXEditorView(this);
editor.setFileName("MainActivity.java");
editor.setText("public class MainActivity {\n    \n}");

setContentView(editor);
```

Release the editor with the host lifecycle:

```java
@Override
protected void onDestroy() {
    if (editor != null) {
        editor.releaseEditor();
    }
    super.onDestroy();
}
```

## XML

```xml
<com.sketchlibx.editor.ui.SketchLibXEditorView
    android:id="@+id/editor"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

## Other integrations

Ready-made wrappers are available for common Android setups:

- `SketchLibXEditorActivity`
- `SketchLibXEditorFragment`
- `SketchLibXEditorDialogFragment`
- `SketchLibXEditorView`

The editor is a normal Android `View`, so it can also be hosted with Compose using `AndroidView`.

## Search and replace

The built-in editor supports:

- Find next / previous
- Replace current match
- Replace all
- Case-sensitive search
- Regular-expression search
- Match count

Search utilities are also available through `com.sketchlibx.editor.util.EditorSearch`.

## How it works

SketchLibX Editor provides the public API and Android integration layer, while Sora Editor handles the core text-editing engine. This keeps the public API focused on application integration and allows the underlying editor engine to handle editing, highlighting and related editor features.

Sora Editor is an upstream dependency of this project. Its license and notices remain applicable; see `THIRD_PARTY_NOTICES.md`.

## Version

**1.0.0-alpha1** — first alpha release.

This is an early release, so public APIs may still change before the stable `1.0.0` release.

## License

SketchLibX Editor is released under the MIT License. See `LICENSE` for details.
