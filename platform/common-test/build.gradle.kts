plugins {
    id("gringotts.library-conventions")
}

description = "Shared test support: Testcontainers definitions and ArchUnit architecture rules."

dependencies {
    api(libs.testcontainers.junit)
    api(libs.testcontainers.postgresql)
    api(libs.testcontainers.kafka)
    api(libs.spring.boot.testcontainers)
    api(libs.archunit.junit5)
}
