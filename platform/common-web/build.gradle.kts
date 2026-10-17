plugins {
    id("gringotts.library-conventions")
}

description = "Servlet web conventions: correlation IDs and RFC 9457 problem details."

dependencies {
    api(project(":platform:common-domain"))
    api(libs.spring.webmvc)
    implementation(libs.spring.boot.autoconfigure)
    implementation(libs.slf4j.api)
    compileOnly(libs.jakarta.servlet.api)

    testImplementation(libs.jakarta.servlet.api)
    testImplementation(libs.jackson.databind)
}
