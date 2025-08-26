rootProject.name = "smartAttendance"

dependencyResolutionManagement {
    versionCatalogs {
        repositories {
            mavenCentral()
        }
        create("ktorLibs") {
            from("io.ktor:ktor-version-catalog:3.2.0")
        }
    }
}