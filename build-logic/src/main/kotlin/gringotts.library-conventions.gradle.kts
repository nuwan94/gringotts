// Shared platform libraries consumed by services.
plugins {
    `java-library`
    id("gringotts.java-conventions")
}

java {
    withSourcesJar()
}
