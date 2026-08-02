# markdown (native)

Rust-крейт, собирающийся в `libmarkdown.so` — вся логика разбора Markdown для richnote
теперь здесь, а не в Kotlin. Kotlin-сторона (`app/src/main/java/.../markdown/`) — только
тонкая JNI-обвязка + декодер JSON обратно в те же `InlineNode`/`BlockNode`, которыми
уже пользуется `ui/MarkdownView.kt` (там ничего менять не пришлось).

Этот каталог полностью независим от Gradle — никакого `externalNativeBuild`/CMake в
`app/build.gradle.kts` нет и не планируется, поскольку `.so` собирается вручную и
подкладывается в проект отдельно (как ты и просил).

## Структура

```
markdown/
├── Cargo.toml
└── src/
    ├── lib.rs      # JNI_OnLoad + native-метод parseMarkdown
    ├── ast.rs       # InlineNode/BlockNode (зеркало Kotlin-версии) + ручная JSON-сериализация
    └── parser.rs    # сам разбор — прямой перенос прежней Kotlin-логики
```

## Сборка

Нужен Android NDK и `cargo-ndk` (ставится один раз):

```bash
cargo install cargo-ndk
rustup target add \
    aarch64-linux-android \
    armv7-linux-androideabi \
    x86_64-linux-android \
    i686-linux-android
```

Сборка всех ABI сразу с раскладкой прямо в `jniLibs` основного модуля:

```bash
cd markdown
cargo ndk \
    -t arm64-v8a -t armeabi-v7a -t x86_64 -t x86 \
    -o ../app/src/main/jniLibs \
    build --release
```

Реальному Android-устройству почти всегда достаточно `arm64-v8a` — если собираешь
только под своё устройство, можно ограничиться `-t arm64-v8a` и не тратить время на
остальные три ABI.

Результат — `libmarkdown.so` в `app/src/main/jniLibs/<abi>/`. Gradle подхватывает файлы
из этой директории в APK автоматически по конвенции — никакой дополнительной
конфигурации в `app/build.gradle.kts` не требуется.

`cargo test` (без `cargo ndk`, на хосте) гоняет юнит-тесты парсера — быстрая проверка
логики без пересборки `.so` под Android:

```bash
cd markdown
cargo test
```

## Контракт JSON

Единственное, что летит через границу JNI, — UTF-8 JSON-строка. `MarkdownNative.parseMarkdown`
возвращает JSON-массив блоков; Kotlin декодирует его обратно через `org.json` (входит
в Android SDK, никакой доп. зависимости).

```
Block  := {"type":"paragraph","inline":[Inline...]} | {"type":"thematic_break"}
Inline := {"type":"text","value":"..."}
        | {"type":"bold","children":[Inline...]}
        | {"type":"italic","children":[Inline...]}
        | {"type":"code","value":"..."}
```

Если формат когда-нибудь поменяется — менять сразу в трёх местах: `ast.rs` (сериализация),
`MarkdownParser.kt` (декодирование), `MarkdownAst.kt` (Kotlin-модель, если меняется сама
структура узлов, а не только JSON-обёртка).

## Если менялось имя пакета/класса на Kotlin-стороне

Метод регистрируется явно через `JNI_OnLoad`/`RegisterNatives` (не через стандартную
mangled-конвенцию `Java_com_..._MarkdownNative_parseMarkdown`) — специально, чтобы
переименование Kotlin-пакета не ломало линковку молча. Единственное место, которое
нужно поправить при очередном переезде пакета, — константа `NATIVE_CLASS` в `lib.rs`.

## ProGuard/R8 (на будущее)

Пока сборка идёт через `assembleDebug` (без минификации) — не актуально. Если/когда
включите `isMinifyEnabled = true` для release-сборки, `MarkdownNative` и его метод
`parseMarkdown` нужно будет исключить из обфускации/удаления мёртвого кода
(`-keep class com.rich_beluga.richnote.markdown.MarkdownNative { *; }` в
`proguard-rules.pro`) — иначе R8 может переименовать или выпилить класс/метод,
на который JNI_OnLoad ссылается по строковому имени, и линковка молча сломается
в release-сборке, но не в debug.
