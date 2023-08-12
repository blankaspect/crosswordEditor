/*====================================================================*\

Gradle build script : CrosswordEditor

\*====================================================================*/

// Plug-ins

plugins {
	java
}

//----------------------------------------------------------------------

// Functions

fun _path(vararg components : String) : String =
		components.map { it.replace('/', File.separatorChar) }.joinToString(separator = File.separator)

//----------------------------------------------------------------------

// Properties

val javaVersion = 17

val packageName		= "crosswordeditor"
val mainClassName	= "uk.blankaspect.${packageName}.App"

val jarDir		= _path("${buildDir}", "bin")
val jarFilename	= "crosswordEditor.jar"

//----------------------------------------------------------------------

// Compile

tasks.compileJava {
	options.release.set(javaVersion)
}

//----------------------------------------------------------------------

// Create executable JAR

tasks.jar {
	destinationDirectory.set(file(jarDir))
	archiveFileName.set(jarFilename)
	manifest {
		attributes(
			"Application-Name" to project.name,
			"Main-Class"       to mainClassName
		)
	}
}

//----------------------------------------------------------------------

// Run main class

tasks.register<JavaExec>("runMain") {
	classpath = sourceSets["main"].runtimeClasspath
	mainClass.set(mainClassName)
}

//----------------------------------------------------------------------

// Run executable JAR

tasks.register<JavaExec>("runJar") {
	classpath = files(tasks.jar)
}

//----------------------------------------------------------------------
