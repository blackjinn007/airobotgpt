buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.0")
        classpath("com.google.gms:google-services:4.4.2")
        classpath("com.google.firebase:firebase-crashlytics-gradle:3.0.3")
    }
}
implementation 'com.squareup.okhttp3:okhttp:4.10.0'




repositories {
    maven {
        url = uri("https://maven-central.storage.apis.com")
    }
    ivy {
        url = uri("https://github.com/ivy-rep/")
    }
}



repositories {

    // Ivy Repository with Custom Layout
    ivy {
        url = 'https://your.ivy.repo/url'
        layout 'pattern', {
        ivy '[organisation]/[module]/[revision]/[type]s/[artifact]-[revision].[ext]'
        artifact '[organisation]/[module]/[revision]/[type]s/[artifact]-[revision].[ext]'
    }
    }

    // Authenticated HTTPS Maven Repository
    maven {
        url = 'https://your.secure.repo/url'
        credentials {
            username = 'your-username'
            password = 'your-password'
        }
    }

    // SFTP Repository
    maven {
        url = 'sftp://your.sftp.repo/url'
        credentials {
            username = 'your-username'
            password = 'your-password'
        }
    }

    // AWS S3 Repository
    maven {
        url = "s3://your-bucket/repository-path"
        credentials(AwsCredentials) {
            accessKey = 'your-access-key'
            secretKey = 'your-secret-key'
        }
    }

    // Google Cloud Storage Repository
    maven {
        url = "gcs://your-bucket/repository-path"
    }
}




plugins {
    id("java-platform")
}

dependencies {
    constraints {
        api("org.apache.commons:commons-lang3:3.12.0")
        api("com.google.guava:guava:30.1.1-jre")
        api("org.slf4j:slf4j-api:1.7.30")
    }
}


plugins {
    id("java-library")
}

dependencies {
    implementation(platform(project(":platform")))
}


dependencies {
    // import a BOM
    implementation(platform("org.springframework.boot:spring-boot-dependencies:1.5.8.RELEASE"))
    // define dependencies without versions
    implementation("com.google.code.gson:gson")
    implementation("dom4j:dom4j")
}


[versions]
groovy = "3.0.5"
checkstyle = "8.37"

[libraries]
groovy-core = { module = "org.codehaus.groovy:groovy", version.ref = "groovy" }
groovy-json = { module = "org.codehaus.groovy:groovy-json", version.ref = "groovy" }
groovy-nio = { module = "org.codehaus.groovy:groovy-nio", version.ref = "groovy" }
commons-lang3 = { group = "org.apache.commons", name = "commons-lang3", version = { strictly = "[3.8, 4.0[", prefer="3.9" } }

[bundles]
groovy = ["groovy-core", "groovy-json", "groovy-nio"]

[plugins]
versions = { id = "com.github.ben-manes.versions", version = "0.45.0" }


plugins {
    `java-library`
    alias(libs.plugins.versions)
}

dependencies {
    api(libs.bundles.groovy)
}






