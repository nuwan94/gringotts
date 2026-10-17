pluginManagement {
    includeBuild("build-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}

rootProject.name = "gringotts"

include(
    "platform:common-money",
    "platform:common-domain",
    "platform:common-web",
    "platform:common-events",
    "platform:common-test",
)
