plugins {
    `java-library`
    `maven-publish`
    jacoco
    id("pl.allegro.tech.build.axion-release") version "1.10.0"
    id("com.gradle.build-scan") version "2.1"
    id("com.adarshr.test-logger") version "1.6.0"
    id("org.sonarqube") version "2.7"
}

repositories {
    maven {
        credentials {
            username = project.properties.get("helixRepoUser") as String?
            password = project.properties.get("helixRepoPassword") as String?
        }
        url = uri("https://repo.helix.re/repository/maven-central/")
    }
}

dependencies {
    implementation("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:20190610.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.4.2")
}

group = "com.github.bgalek.security.svg"
version = scmVersion.version

tasks {
    jar {
        manifest {
            attributes(mapOf("Implementation-Title" to project.name, "Implementation-Version" to project.version))
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.register<Jar>("sourcesJar") {
    from(sourceSets.main.get().allJava)
    archiveClassifier.set("sources")
}

tasks.register<Jar>("javadocJar") {
    from(tasks.javadoc)
    archiveClassifier.set("javadoc")
}

jacoco {
    toolVersion = "0.8.2"
    reportsDir = file("$buildDir/reports/jacoco")
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
        xml.destination = file("$buildDir/reports/jacoco/report.xml")
        csv.isEnabled = false
        html.isEnabled = false
    }
}

publishing {
    publications {
        create<MavenPublication>("sonatype") {
            artifactId = "safe-svg-java8"
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set("safe-svg-java8 ")
                description.set("Simple and lightweight library that helps to validate SVG files in security manners.")
                url.set("https://github.com/bgalek/safe-svg/")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("bgalek")
                        name.set("Bartosz Gałek")
                        email.set("bartosz@galek.com.pl")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/bgalek/safe-svg.git")
                    developerConnection.set("scm:git:ssh://github.com:bgalek/safe-svg.git")
                    url.set("https://github.com/bgalek/safe-svg/")
                }
            }
        }
    }
    repositories {
        maven {
            credentials {
                username = project.properties.get("helixRepoUser") as String?
                password = project.properties.get("helixRepoPassword") as String?
            }
            val releasesRepoUrl = uri("https://repo.helix.re/repository/helix/")
            val snapshotsRepoUrl = uri("https://repo.helix.re/repository/helix-snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
        }
    }
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
}