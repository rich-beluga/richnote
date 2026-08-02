# app/src/main/jniLibs

Сюда `cargo ndk` кладёт собранные `libmarkdown.so` (см. `/markdown/README.md` в корне
репозитория за инструкцией сборки). Gradle подхватывает файлы отсюда в APK
автоматически по конвенции AGP — никакой конфигурации в `app/build.gradle.kts` не нужно.

Ожидаемая структура после сборки (нужен как минимум `arm64-v8a` — это подходит
подавляющему большинству современных Android-устройств):

```
jniLibs/
├── arm64-v8a/libmarkdown.so
├── armeabi-v7a/libmarkdown.so
├── x86_64/libmarkdown.so
└── x86/libmarkdown.so
```

Пока здесь пусто — `EditorScreen` не падает: `MarkdownNative.isAvailable == false`,
`MarkdownParser.parse` откатывается на нераспарсенный текст (см. markdown/README.md
в корне репозитория, раздел про JSON-контракт, и комментарии в MarkdownNative.kt).
