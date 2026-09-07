import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
    id("java-library")
    id("com.vanniktech.maven.publish") version "0.37.0" apply false
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "com.vanniktech.maven.publish")

    group = "org.allaymc.stateupdater"
    project.version = rootProject.property(project.name + ".version").toString()

    val minifyJsonTask by tasks.registering {
        dependsOn(tasks.processResources)

        doLast {
            val resourcesDir = layout.buildDirectory.dir("resources/main").get().asFile
            if (!resourcesDir.exists()) {
                logger.warn("Resources directory does not exist: ${resourcesDir.absolutePath}")
                return@doLast
            }

            resourcesDir.walkTopDown().filter { it.isFile && it.extension == "json" }.forEach { file ->
                try {
                    val parsed = JsonSlurper().parse(file)
                    val minified = JsonOutput.toJson(parsed)
                    file.writeText(minified)
                    logger.lifecycle("Minified: ${file.name}")
                } catch (e: Exception) {
                    logger.warn("Failed to minify ${file.name}: ${e.message}")
                }
            }
        }
    }

    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"
        }

        // We already have sources jar, so no need to build Javadoc, which would cause a lot of warnings
        withType<Javadoc> {
            enabled = false
        }

        withType<Test> {
            useJUnitPlatform()
        }

        named("classes") {
            dependsOn(minifyJsonTask)
        }
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
        withSourcesJar()
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("com.fasterxml.jackson.core:jackson-databind:2.22.2")
        implementation("org.allaymc:nbt:3.0.12")

        compileOnly("org.projectlombok:lombok:1.18.48")
        annotationProcessor("org.projectlombok:lombok:1.18.48")

        testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.1.3")
        testAnnotationProcessor("org.projectlombok:lombok:1.18.48")
    }

    configure<MavenPublishBaseExtension> {
        publishToMavenCentral()
        signAllPublications()

        coordinates(project.group.toString(), project.name, project.version.toString())

        pom {
            name.set(project.name)
            description.set("Updates Minecraft: Bedrock Edition block & item states to the latest version.")
            inceptionYear.set("2024")
            url.set("https://github.com/AllayMC/StateUpdater")

            scm {
                connection.set("scm:git:git://github.com/AllayMC/StateUpdater.git")
                developerConnection.set("scm:git:ssh://github.com/AllayMC/StateUpdater.git")
                url.set("https://github.com/AllayMC/StateUpdater")
            }

            licenses {
                license {
                    name.set("LGPL 3.0")
                    url.set("https://www.gnu.org/licenses/lgpl-3.0.en.html")
                }
            }

            developers {
                developer {
                    name.set("CloudburstMC Team")
                    organization.set("CloudburstMC")
                    organizationUrl.set("https://github.com/CloudburstMC")
                }
                developer {
                    name.set("AllayMC Team")
                    organization.set("AllayMC")
                    organizationUrl.set("https://github.com/AllayMC")
                }
            }
        }
    }
}