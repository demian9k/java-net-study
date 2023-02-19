tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    compileTestJava {
        options.encoding = "UTF-8"
    }
}

plugins {
    application
    id("org.openjfx.javafxplugin") version "0.0.13"
}

javafx {
    version = "17"
    modules = listOf("javafx.controls","javafx.fxml","javafx.base","javafx.media","javafx.graphics","javafx.swing","javafx.web")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.guava:guava:31.1-jre")
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use JUnit4 test framework
            useJUnit("4.13.2")
        }
    }
}

application {
//    mainClass.set("io.demian.net_study.step1_IO.chat.Server")
}
