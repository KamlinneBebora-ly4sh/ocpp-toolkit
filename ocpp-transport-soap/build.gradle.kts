plugins {
    kotlin("jvm")
    `maven-publish`
}

coreProject()

dependencies {
    implementation("org.http4k:http4k-core:_")
    implementation("org.http4k:http4k-contract:_")
    implementation("org.http4k:http4k-server-undertow:_")

    implementation(project(":ocpp-soap"))
    implementation(project(":ocpp-transport"))
    implementation(project(":utils"))
    implementation(project(":operation-information"))

    testImplementation(project(":ocpp-1-6-soap"))
    testImplementation(kotlin("test-junit"))
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        named<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = "ocpp-transport-soap"
            version = project.version.toString()

            from(components["java"])

            pom {
                name.set("OCPP Transport SOAP")
                artifactId = "ocpp-transport-soap"
                description.set("OCPP Transport SOAP")
            }
        }
    }
}
