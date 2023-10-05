import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.20"
    id("maven-publish")
}

allprojects {

    group = "com.kaiqkt"
    version = "1.0.0"

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "maven-publish")

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("org.slf4j:slf4j-api:2.0.5")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }

    kotlin {
        explicitApi()
    }

    publishing {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/kaiqkt/neo-sagakt")
                credentials {
                    username = project.findProperty("gpr.user") as String? ?: System.getenv("GPR_USER")
                    password = project.findProperty("gpr.key") as String? ?: System.getenv("GPR_API_KEY")
                }
            }
        }
        publications {
            publications {
                create<MavenPublication>("maven") {
                    from(components["java"])
                }
            }
        }
    }
}