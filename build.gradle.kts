import org.gradle.plugin.compatibility.compatibility

plugins {
    id("atlas.java-conventions")
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "2.2.1"
}

group = "gg.getatlas"

dependencies {
    implementation(libs.gson)
}

gradlePlugin {
    website = "https://getatlas.gg/docs/releasing"
    vcsUrl = "https://github.com/getatlasgg/gradle-plugin"
    plugins {
        create("publish") {
            id = "gg.getatlas.publish"
            implementationClass = "gg.getatlas.publish.AtlasPublishPlugin"
            displayName = "Atlas"
            description = "Releases the plugin jars and other files a build makes to templates on an Atlas panel, the self-hosted control panel for Minecraft networks."
            tags = listOf("atlas", "minecraft", "minecraft-server", "paper", "velocity", "release", "deployment")

            compatibility {
                features {
                    configurationCache = true
                    isolatedProjects = true
                }
            }
        }
    }
}
