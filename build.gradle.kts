plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.eclipse.leshan:leshan-server-core:1.5.0")
    implementation("org.eclipse.leshan:leshan-client-core:1.5.0")
    implementation("org.eclipse.leshan:leshan-server-cf:1.5.0")
    implementation("org.eclipse.leshan:leshan-client-cf:1.5.0")
    implementation("org.slf4j:slf4j-simple:1.7.30")
}

tasks.test {
    useJUnitPlatform()
}