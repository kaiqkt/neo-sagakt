plugins {
    `java-library`
}

dependencies {
    implementation(project(mapOf("path" to ":core")))
    implementation("javax.jms:javax.jms-api:2.0.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0")
}