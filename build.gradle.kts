plugins {
    java
    id("com.github.weave-mc.weave-gradle") version "fac948db7f"
}

group = "com.pwetutils"
version = "1.4.14"

minecraft.version("1.8.9")

repositories {
    maven("https://jitpack.io")
    maven("https://repo.spongepowered.org/maven/")
}

dependencies {
    compileOnly("com.github.weave-mc:weave-loader:v0.2.6")

    compileOnly("org.spongepowered:mixin:0.8.5")
}

tasks.compileJava {
    options.release.set(11)
}
