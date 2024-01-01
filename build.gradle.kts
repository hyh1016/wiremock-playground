plugins {
    groovy
}

group = "yhproject.playground"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    testImplementation("org.mockito:mockito-junit-jupiter:5.8.0")
    testImplementation("org.wiremock:wiremock:3.3.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
