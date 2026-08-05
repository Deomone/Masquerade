plugins {
    id("fabric-loom") version "1.17.17"
    id("java")
}

version = "${property("mod_version")}+mc${property("minecraft_version")}"
group = property("maven_group") as String

base {
    archivesName = property("archives_base_name") as String
}

val javaVersion = (property("java_version") as String).toInt()

loom {
    splitEnvironmentSourceSets()

    mods {
        register("masquerade") {
            sourceSet("main")
            sourceSet("client")
        }
    }

    accessWidenerPath = file("src/main/resources/masquerade.accesswidener")

    runs {
        named("client") {
            programArguments.addAll("--username", "DevSteve")
        }
        register("clientAlt") {
            client()
            displayName = "Minecraft Client Alt"
            generateRunConfig = true
            runDirectory = layout.projectDirectory.dir("run-alt")
            programArguments.addAll("--username", "DevAlex")
        }
    }
}

repositories {
    maven("https://maven.fabricmc.net/") { name = "Fabric" }
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")
}

val templateProperties = mapOf(
    "version" to project.version,
    "minecraft_version" to project.property("minecraft_version"),
    "loader_version" to project.property("loader_version"),
    "fabric_version" to project.property("fabric_version"),
    "java_version" to javaVersion
)

tasks.processResources {
    inputs.properties(templateProperties)
    filesMatching("fabric.mod.json") {
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

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${base.archivesName.get()}" }
    }
}

val outputDir = file(project.property("output_dir") as String)

val exportJar = tasks.register<Copy>("exportJar") {
    group = "build"
    description = "Copies the remapped jar into the shared outputs directory"
    from(tasks.named("remapJar"))
    into(outputDir)
}

tasks.named("build") {
    finalizedBy(exportJar)
}
