plugins {
    id("gringotts.spring-service-conventions")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation(project(":platform:common-web"))
}