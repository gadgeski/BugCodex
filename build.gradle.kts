// <project-root>/build.gradle.kts

plugins {
    // libs.versions.toml の定義に合わせてエイリアスを修正
    // キーが "android-application" なので、アクセサは "android.application" になります
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // Hilt (libs.plugins.hilt.android)
    alias(libs.plugins.hilt.android) apply false

    // KSP (定義があったので有効化)
    alias(libs.plugins.ksp) apply false

    // Spotless (コード整形)
    // ルートプロジェクトで設定ブロックを実行するため、apply false を付けずに適用します
    alias(libs.plugins.spotless)
}

// Spotlessの設定（復活）
spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**", ".gradle/**", "**/.gradle/**", "**/generated/**")
        ktlint()
            .editorConfigOverride(
                mapOf(
                    "ktlint_function_naming_ignore_when_annotated_with" to "Composable,Preview",
                ),
            )
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**", ".gradle/**", "**/.gradle/**")
        ktlint()
            .editorConfigOverride(
                mapOf(
                    "ktlint_function_naming_ignore_when_annotated_with" to "Composable,Preview",
                ),
            )
    }
    format("misc") {
        target("**/*.md", "**/*.yml", "**/*.yaml", "**/.gitignore")
        targetExclude("**/build/**", ".gradle/**", "**/.gradle/**")
        trimTrailingWhitespace()
        endWithNewline()
    }
}
