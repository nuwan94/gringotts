// Deployable Spring Boot microservices.
plugins {
    id("gringotts.java-conventions")
    id("org.springframework.boot")
}

val libs = the<VersionCatalogsExtension>().named("libs")

dependencies {
    val cloudBom = platform(libs.findLibrary("spring-cloud-dependencies").get())
    implementation(cloudBom)
    testImplementation(cloudBom)

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation(libs.findLibrary("archunit-junit5").get())
}

springBoot {
    buildInfo()
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName = "${project.name}.jar"
}

tasks.named<Jar>("jar") {
    enabled = false
}
