/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.4/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    // JOGL
    maven { url  = uri("https://jogamp.org/deployment/maven") }
    // glm
    maven("https://raw.githubusercontent.com/kotlin-graphics/mary/master")
}

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // This dependency is used by the application.
    implementation("com.google.guava:guava:32.1.1-jre")

    // JOGL
    implementation("org.jogamp.gluegen:gluegen-rt-main:2.4.0")
    implementation("org.jogamp.jogl:jogl-all-main:2.4.0")

    // glm
    implementation("io.github.kotlin-graphics:glm:0.9.9.1-12")
    implementation("io.github.kotlin-graphics:kool:0.9.79")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

application {
    // Define the main class for the application.
    mainClass.set("com.mundoludo.modern3d.App")
}

tasks.register<JavaExec>("hello_triangle") {
    dependsOn("classes")
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.mundoludo.modern3d.tut01.HelloTriangle")
}
tasks.register<JavaExec>("fragment_position") {
    dependsOn("classes")
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.mundoludo.modern3d.tut02.FragPosition")
}
tasks.register<JavaExec>("vertex_colors") {
    dependsOn("classes")
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.mundoludo.modern3d.tut02.VertexColors")
}
tasks.register<JavaExec>("cpu_position_offset") {
    dependsOn("classes")
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.mundoludo.modern3d.tut03.CpuPositionOffset")
}
tasks.register<JavaExec>("shader_position_offset") {
    dependsOn("classes")
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.mundoludo.modern3d.tut03.VertPositionOffset")
}
tasks.register<JavaExec>("shader_calc_offset") {
    dependsOn("classes")
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.mundoludo.modern3d.tut03.VertCalcOffset")
}
tasks.register<JavaExec>("frag_change_color") {
    dependsOn("classes")
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.mundoludo.modern3d.tut03.FragChangeColor")
}
tasks.register<JavaExec>("ortho_cube") {
    dependsOn("classes")
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.mundoludo.modern3d.tut04.OrthoCube")
}
tasks.register<JavaExec>("shader_perspective") {
    dependsOn("classes")
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.mundoludo.modern3d.tut04.ShaderPerspective")
}
tasks.register<JavaExec>("matrix_perspective") {
    dependsOn("classes")
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.mundoludo.modern3d.tut04.MatrixPerspective")
}
tasks.register<JavaExec>("aspect_ratio") {
    dependsOn("classes")
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.mundoludo.modern3d.tut04.AspectRatio")
}
tasks.register<JavaExec>("overlap_no_depth") {
    dependsOn("classes")
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.mundoludo.modern3d.tut05.OverlapNoDepth")
}
tasks.register<JavaExec>("base_vertex_overlap") {
    dependsOn("classes")
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.mundoludo.modern3d.tut05.BaseVertexOverlap")
}
tasks.register<JavaExec>("depth_buffer") {
    dependsOn("classes")
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.mundoludo.modern3d.tut05.DepthBuffer")
}
tasks.register<JavaExec>("vertex_clipping") {
    dependsOn("classes")
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.mundoludo.modern3d.tut05.VertexClipping")
}
tasks.register<JavaExec>("translation") {
    dependsOn("classes")
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.mundoludo.modern3d.tut06.Translation")
}
tasks.register<JavaExec>("scale") {
    dependsOn("classes")
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.mundoludo.modern3d.tut06.Scale")
}
tasks.register<JavaExec>("rotation") {
    dependsOn("classes")
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.mundoludo.modern3d.tut06.Rotation")
}
tasks.register<JavaExec>("hierarchy") {
    dependsOn("classes")
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.mundoludo.modern3d.tut06.Hierarchy")
}
tasks.register<JavaExec>("world_scene") {
    dependsOn("classes")
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.mundoludo.modern3d.tut07.WorldScene")
}
tasks.register<JavaExec>("world_with_ubo") {
    dependsOn("classes")
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.mundoludo.modern3d.tut07.WorldWithUBO")
}
tasks.register<JavaExec>("gimbal_lock") {
    dependsOn("classes")
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.mundoludo.modern3d.tut08.GimbalLock")
}
