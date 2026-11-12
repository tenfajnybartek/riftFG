import org.gradle.jvm.toolchain.JavaLanguageVersion

dependencies {
    implementation(project(":nms:api"))
    api(project(":nms:v1_21_9"))
    paperweight.paperDevBundle("26.1.1.build.29-alpha")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}