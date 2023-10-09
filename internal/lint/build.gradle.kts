plugins {
    id("java-library")
    id("kotlin")
}

java {
    targetCompatibility = JavaVersion.VERSION_17
    sourceCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly("com.android.tools.lint:lint-api:31.1.2")
    testImplementation("com.android.tools.lint:lint:31.1.2")
    testImplementation("com.android.tools.lint:lint-tests:31.1.2")
    testImplementation("junit:junit:4.13.2")
}
