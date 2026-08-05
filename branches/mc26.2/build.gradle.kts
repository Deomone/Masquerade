plugins {
    id("java")
}

version = property("plugin_version") as String
group = property("maven_group") as String

base {
    archivesName = property("archives_base_name") as String
}

val javaVersion = (property("java_version") as String).toInt()
val outputDir = file(project.property("output_dir") as String)

repositories {
    maven("https://repo.papermc.io/repository/maven-public/") { name = "PaperMC" }
    mavenCentral()
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${property("paper_version")}")
}

val templateProperties = mapOf(
    "version" to project.version,
    "apiVersion" to project.property("api_version")
)

tasks.processResources {
    inputs.properties(templateProperties)
    filesMatching("plugin.yml") {
        expand(templateProperties)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = javaVersion
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)
}

val exportJar = tasks.register<Copy>("exportJar") {
    group = "build"
    description = "Copies the built plugin jar into the shared outputs directory"
    from(tasks.named("jar"))
    into(outputDir)
}

tasks.named("build") {
    finalizedBy(exportJar)
}
