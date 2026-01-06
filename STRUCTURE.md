```
BugCodex/
├── Readme.md
├── STRUCTURE.md
├── app
│   ├── build.gradle.kts
│   ├── lint-baseline.xml
│   ├── proguard-rules.pro
│   ├── schemas
│   │   ├── com.example.bugmemo.data.db.AppDatabase
│   │   │   ├── 2.json
│   │   │   ├── 3.json
│   │   │   └── 4.json
│   │   ├── com.gadgeski.bugcodex.data.db.AppDatabase
│   │   │   └── 4.json
│   │   └── com.gadgeski.bugmemo.data.db.AppDatabase
│   │       └── 4.json
│   └── src
│       ├── androidTest
│       │   └── java
│       │       └── com
│       │           └── gadgeski
│       │               └── bugcodex
│       │                   ├── ExampleInstrumentedTest.kt
│       │                   └── data
│       │                       ├── RoomNotesRepositoryTest.kt
│       │                       └── db
│       │                           └── NoteDaoTest.kt
│       ├── main
│       │   ├── AndroidManifest.xml
│       │   ├── ic_launcher-playstore.png
│       │   ├── java
│       │   │   └── com
│       │   │       └── gadgeski
│       │   │           └── bugcodex
│       │   │               ├── BugCodexApp.kt
│       │   │               ├── MainActivity.kt
│       │   │               ├── core
│       │   │               │   ├── AppLocaleManager.kt
│       │   │               │   └── FeatureFlags.kt
│       │   │               ├── data
│       │   │               │   ├── Models.kt
│       │   │               │   ├── NotesRepository.kt
│       │   │               │   ├── RoomNotesRepository.kt
│       │   │               │   ├── db
│       │   │               │   │   ├── AppDatabase.kt
│       │   │               │   │   ├── Converters.kt
│       │   │               │   │   ├── FolderDao.kt
│       │   │               │   │   ├── MindMapDao.kt
│       │   │               │   │   ├── MindMapEntity.kt
│       │   │               │   │   ├── NoteDao.kt
│       │   │               │   │   ├── NoteEntity.kt
│       │   │               │   │   └── NoteFts.kt
│       │   │               │   ├── local
│       │   │               │   │   └── Converters.kt
│       │   │               │   ├── prefs
│       │   │               │   │   └── SettingsRepository.kt
│       │   │               │   └── remote
│       │   │               │       ├── GistModels.kt
│       │   │               │       └── GistService.kt
│       │   │               ├── di
│       │   │               │   ├── AppModule.kt
│       │   │               │   └── NetworkModule.kt
│       │   │               ├── ui
│       │   │               │   ├── AppScaffold.kt
│       │   │               │   ├── NotesViewModel.kt
│       │   │               │   ├── common
│       │   │               │   │   ├── MarkdownEditUtils.kt
│       │   │               │   │   └── MarkdownText.kt
│       │   │               │   ├── components
│       │   │               │   │   ├── Common.kt
│       │   │               │   │   └── MarkdownToolbar.kt
│       │   │               │   ├── mindmap
│       │   │               │   │   └── MindMapViewModel.kt
│       │   │               │   ├── navigation
│       │   │               │   │   └── Nav.kt
│       │   │               │   ├── screens
│       │   │               │   │   ├── AllNotesScreen.kt
│       │   │               │   │   ├── BugsScreen.kt
│       │   │               │   │   ├── FoldersScreen.kt
│       │   │               │   │   ├── MindMapScreen.kt
│       │   │               │   │   ├── NoteEditorScreen.kt
│       │   │               │   │   ├── SearchScreen.kt
│       │   │               │   │   └── SettingsScreen.kt
│       │   │               │   ├── theme
│       │   │               │   │   ├── Color.kt
│       │   │               │   │   ├── Theme.kt
│       │   │               │   │   └── Type.kt
│       │   │               │   └── utils
│       │   │               │       ├── GistContentBuilder.kt
│       │   │               │       ├── IcebergEditorVisualTransformation.kt
│       │   │               │       └── MarkdownTextHelper.kt
│       │   │               └── util
│       │   │                   └── HingeSensor.kt
│       │   └── res
│       │       ├── drawable
│       │       │   ├── ic_launcher_background.xml
│       │       │   └── ic_launcher_foreground.xml
│       │       ├── mipmap-anydpi-v26
│       │       │   ├── ic_launcher.xml
│       │       │   └── ic_launcher_round.xml
│       │       ├── mipmap-hdpi
│       │       │   ├── ic_launcher.webp
│       │       │   ├── ic_launcher_foreground.webp
│       │       │   └── ic_launcher_round.webp
│       │       ├── mipmap-mdpi
│       │       │   ├── ic_launcher.webp
│       │       │   ├── ic_launcher_foreground.webp
│       │       │   └── ic_launcher_round.webp
│       │       ├── mipmap-xhdpi
│       │       │   ├── ic_launcher.webp
│       │       │   ├── ic_launcher_foreground.webp
│       │       │   └── ic_launcher_round.webp
│       │       ├── mipmap-xxhdpi
│       │       │   ├── ic_launcher.webp
│       │       │   ├── ic_launcher_foreground.webp
│       │       │   └── ic_launcher_round.webp
│       │       ├── mipmap-xxxhdpi
│       │       │   ├── ic_launcher.webp
│       │       │   ├── ic_launcher_foreground.webp
│       │       │   └── ic_launcher_round.webp
│       │       ├── values
│       │       │   ├── colors.xml
│       │       │   ├── strings.xml
│       │       │   └── themes.xml
│       │       ├── values-en
│       │       │   └── strings.xml
│       │       └── xml
│       │           ├── backup_rules.xml
│       │           ├── data_extraction_rules.xml
│       │           └── locale_config.xml
│       └── test
│           └── java
│               └── com
│                   └── gadgeski
│                       └── bugcodex
│                           ├── ExampleUnitTest.kt
│                           ├── data
│                           │   ├── FakeNotesRepository.kt
│                           │   └── db
│                           │       └── ConvertersTest.kt
│                           └── ui
│                               └── utils
│                                   └── MarkdownTextHelperTest.kt
├── build.gradle.kts
├── docs
│   ├── architecture
│   │   ├── algorithms.md
│   │   ├── app_scaffold.md
│   │   ├── nav.md
│   │   ├── overview.md
│   │   └── settingsscreen + appLocalemanager.md
│   └── img
│       ├── Bug Memo-FontBold.png
│       ├── BugMemo-Home.png
│       ├── BugMemo-Mindmap.png
│       ├── BugMemo-Search.png
│       ├── BugMemo_Setting.png
│       ├── Iceberg-Tech_Dashboard.png
│       ├── Iceberg-Tech_Directories.png
│       ├── Iceberg-Tech_Home.png
│       └── Iceberg-Tech_Search.png
├── gradle
│   ├── libs.versions.toml
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradle.properties
├── gradlew
├── gradlew.bat
├── local.properties
└── settings.gradle.kts

60 directories, 111 files
```
