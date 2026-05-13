pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "procar-backend"
include("gateway")
include("gateway-api")
include("auth")
include("auth-api")
include("auction-provider-api")
include("auction-provider-procar")
include("admin-jetpack")
include("customer-jetpack")
include("user-api")
include("user")
include("commons")
include("smoke-tests")
