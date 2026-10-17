// Baseline for every JVM module: toolchain, compiler flags, formatting, tests, coverage.
plugins {
    java
    jacoco
    id("com.diffplug.spotless")
}

val libs = the<VersionCatalogsExtension>().named("libs")

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(libs.findVersion("java").get().requiredVersion)
    }
}

dependencies {
    // Spring Boot's BOM is the single source of truth for third-party versions.
    val bom = platform(libs.findLibrary("spring-boot-dependencies").get())
    implementation(bom)
    testImplementation(bom)
    annotationProcessor(bom)

    testImplementation(libs.findLibrary("spring-boot-starter-test").get())
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = libs.findVersion("java").get().requiredVersion.toInt()
    options.compilerArgs.addAll(listOf("-parameters", "-Xlint:all,-processing,-serial", "-Werror"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading")
    testLogging {
        events("failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
    finalizedBy(tasks.named("jacocoTestReport"))
}

tasks.named<JacocoReport>("jacocoTestReport") {
    reports {
        xml.required = true
        html.required = true
    }
}

spotless {
    java {
        target("src/**/*.java")
        googleJavaFormat()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}
