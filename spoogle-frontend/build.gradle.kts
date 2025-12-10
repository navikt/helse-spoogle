plugins {
    base
    id("com.github.node-gradle.node") version "7.1.0"
}

tasks.assemble {
    dependsOn("npm_run_build")
}

tasks.check {
    dependsOn("npm_run_test")
}

project.layout.buildDirectory = File("dist")
