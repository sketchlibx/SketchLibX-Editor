# SketchLibX Editor

A reusable, Android-first code and text editor component designed for apps that need a proper editor without embedding a whole IDE.

Package: `com.sketchlibx.editor`

The project is intentionally split into two layers:

- `SketchLibXEditorView` is the public reusable UI surface.
- Sora Editor provides the low-level editing engine, while SketchLibX owns file detection, the toolbar, search/replace UI, theme handling, and host wrappers.

That means an app can use the editor as a normal custom `View`, or use the ready-made Activity, Fragment, or DialogFragment wrappers.

## What is included

### Editing

- Incremental syntax highlighting through TextMate
- Auto-completion support from the underlying editor engine
- Undo/redo support
- Automatic indentation and editor navigation
- Bracket pair highlighting and matching
- Sticky-scroll, magnifier and diagnostic support from the engine
- Physical keyboard shortcuts similar to modern desktop editors
- Word wrapping
- Non-printable character display
- Monospace editor presentation
- Zoom in, zoom out and reset zoom controls
- Search and replace
- Case-sensitive search
- Regular-expression search
- Replace-one and replace-all
- Dark and light syntax themes

Sora Editor documents incremental highlight, auto-completion, scale text, undo/redo, search/replace, word-wrap, diagnostics, magnifier, sticky scroll, bracket highlighting and TextMate/Tree-sitter language support. SketchLibX wraps those capabilities instead of reimplementing a large editor engine. See the upstream project and documentation for the underlying editor behavior.

### File/language detection

The built-in detector recognizes common source and configuration files including:

`java`, `kt`, `kts`, `c`, `cpp`, `h`, `hpp`, `cs`, `py`, `js`, `mjs`, `ts`, `jsx`, `tsx`, `html`, `css`, `scss`, `less`, `xml`, `svg`, `json`, `yaml`, `yml`, `toml`, `md`, `sql`, `sh`, `bash`, `php`, `rb`, `go`, `rs`, `swift`, `dart`, `groovy`, `lua`, `pl`, `ini`, `properties`, `diff`, `patch`, `csv`, `gradle`, `Dockerfile`, `Makefile`, `txt`, and `log`.

Unknown extensions are not rejected. They safely fall back to `Text / Code` mode, so the editor remains useful for custom formats, generated files, configuration files and project-specific extensions.

The bundled TextMate grammar is deliberately lightweight and license-friendly: it provides broad language-agnostic highlighting for comments, strings, numbers, constants, keywords, operators, functions, XML/HTML tags and common Markdown markup. More precise grammars can be added later without changing the public SketchLibX API.

## Public API

The main classes live under:

```text
com.sketchlibx.editor
com.sketchlibx.editor.core
com.sketchlibx.editor.ui
com.sketchlibx.editor.util
```

Most applications only need:

```java
import com.sketchlibx.editor.ui.SketchLibXEditorView;
```

## Use as a normal custom View

```java
SketchLibXEditorView editor = new SketchLibXEditorView(this);
editor.setFileName("MainActivity.java");
editor.setText("public class MainActivity {\n    // edit here\n}\n");
setContentView(editor);
```

Release the editor with the host lifecycle:

```java
@Override
protected void onDestroy() {
    if (editor != null) editor.releaseEditor();
    super.onDestroy();
}
```

## Use from XML

`SketchLibXEditorView` has the normal `Context` + `AttributeSet` constructor, so it can also be inflated from XML:

```xml
<com.sketchlibx.editor.ui.SketchLibXEditorView
    android:id="@+id/editor"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

Then configure it in Java/Kotlin:

```java
SketchLibXEditorView editor = findViewById(R.id.editor);
editor.setFileName("README.md");
editor.setText("# Hello\n\nThis is an editable Markdown file.");
```

## Use as a Fragment

```java
SketchLibXEditorFragment fragment = SketchLibXEditorFragment.newInstance(
        "settings.xml",
        "<settings>\n    <item name=\"enabled\">true</item>\n</settings>"
);
getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.container, fragment)
        .commit();
```

## Use as a DialogFragment

```java
SketchLibXEditorDialogFragment dialog = SketchLibXEditorDialogFragment.newInstance(
        "snippet.cpp",
        "#include <iostream>\n\nint main() { return 0; }"
);
dialog.show(getSupportFragmentManager(), "sketchlibx-editor");
```

## Use the ready-made Activity

Launch `SketchLibXEditorActivity` and pass:

```java
intent.putExtra(SketchLibXEditorActivity.EXTRA_FILE_NAME, "MainActivity.kt");
intent.putExtra(SketchLibXEditorActivity.EXTRA_CONTENT, sourceText);
```

You can subclass `SketchLibXEditorActivity` when your application needs a custom save action, file picker, title handling or project integration.

## Compose / AndroidView

The editor remains a normal Android `View`, so it works with `AndroidView`:

```kotlin
AndroidView(
    factory = { context ->
        SketchLibXEditorView(context).apply {
            setFileName("Example.kt")
            setText("fun main() = println(\"Hello\")")
        }
    },
    modifier = Modifier.fillMaxSize()
)
```

This keeps Compose optional; the library itself does not require Compose.

## Find and replace

The built-in toolbar exposes:

- Find previous / next
- Replace current match
- Replace all
- Case-sensitive mode
- Regex mode
- Live match count

The search engine is kept in `com.sketchlibx.editor.util.EditorSearch`, so applications can also reuse the search logic independently of the UI.

## Build the library

The project uses:

- Android Gradle Plugin `8.7.3`
- Gradle `8.9`
- Java/JDK `17`
- `compileSdk 35`
- `minSdk 21`

AGP `8.7.3` is paired with Gradle `8.9`. JitPack is configured for JDK 17. Sora Editor 0.24.6 supports API 21 as its final API-21 release.

From a machine with Android SDK 35 installed:

```bash
gradle :editor:assembleRelease
gradle :editor:testReleaseUnitTest
```

The main AAR will be under:

```text
editor/build/outputs/aar/
```

There is intentionally no GitHub Actions workflow in this repository. JitPack is the publishing/remote-build path for this library, so an extra workflow is not required for publishing.

## Publish through JitPack

Push this repository to GitHub, for example:

```text
https://github.com/<YOUR_GITHUB_USERNAME>/SketchLibX-Editor
```

Create a Git tag such as `v0.1.0` and push it:

```bash
git tag v0.1.0
git push origin v0.1.0
```

After JitPack builds the tag, consuming apps can use:

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.<YOUR_GITHUB_USERNAME>:SketchLibX-Editor:v0.1.1")
}
```

For Maven Central, keep the existing `maven-publish` configuration and add your Sonatype signing/credential configuration in a private `~/.gradle/gradle.properties` or CI secret store. Do not commit credentials.

## Versioning

The library version in this fixed build is `0.1.1`.

Suggested release flow:

```text
0.1.x  feature/fix releases
0.2.x  API additions or a larger editor UI revision
1.0.0  stable public API
```

The public SketchLibX classes are intentionally separated from Sora Editor implementation details. Host applications should avoid directly depending on Sora classes when they only need the SketchLibX API.

## Performance notes

Large files are still text buffers. Applications opening very large generated files should consider lazy loading or read-only mode instead of pushing an entire multi-megabyte/binary file into an editor widget.

Binary data is not treated as a binary viewer. For files that are not valid text, use a dedicated hex/binary viewer and keep SketchLibX Editor for source/text content.

## Third-party dependency and license

SketchLibX's own source is intended to be MIT licensed.

The editor engine is provided by the open-source Sora Editor project. Sora Editor is distributed under LGPL-2.1; its licensing and dependency notices continue to apply to that dependency. Do not remove upstream notices from redistributed dependency artifacts.

See `THIRD_PARTY_NOTICES.md` for the dependency note. The bundled grammar is intentionally universal; file extensions are detected and exposed through the public API, while language-specific TextMate packs can be layered on later without changing the public view API.

## Project layout

```text
SketchLibX-Editor/
├── editor/
│   └── src/main/
│       ├── assets/sketchlibx_editor/textmate/
│       └── java/com/sketchlibx/editor/
│           ├── SketchLibXEditor.java
│           ├── core/
│           ├── ui/
│           └── util/
├── sample/
├── build.gradle
├── settings.gradle
└── README.md
```

## Roadmap

The current foundation is deliberately stable and reusable. Natural next additions are optional language-specific grammar packs, code folding/outline integrations, document sessions/tabs, LSP adapters, diagnostics providers, formatter hooks, diff mode, minimap and multi-cursor actions.

Those features can be layered on without changing the basic `SketchLibXEditorView` integration model.

## Build/publishing compatibility

The fixed project uses Android Gradle Plugin 8.7.3 with Gradle 8.9 because that pairing is explicitly compatible. The Android library publication is configured inside `afterEvaluate`, which is required because AGP creates the Android `SoftwareComponent` during that lifecycle phase. JitPack uses JDK 17 through `jitpack.yml`.
