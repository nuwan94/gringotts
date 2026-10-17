plugins {
    id("gringotts.library-conventions")
}

description = "Transactional outbox for publishing domain events (relayed to Kafka by Debezium)."

dependencies {
    api(project(":platform:common-domain"))
    api(libs.spring.jdbc)
    api(libs.spring.tx)
    implementation(libs.spring.boot.autoconfigure)
    implementation(libs.jackson.databind)
    implementation(libs.slf4j.api)

    testImplementation(project(":platform:common-test"))
    testImplementation(libs.spring.boot.starter.jdbc)
    testImplementation(libs.flyway.core)
    testImplementation(libs.flyway.postgresql)
    testRuntimeOnly(libs.postgresql)
}
