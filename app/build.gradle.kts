plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.prm392_finalproject"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.prm392_finalproject"
        minSdk = 35
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("mysql:mysql-connector-java:5.1.49")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase BoM (Bill of Materials)
    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))

    // Firebase Authentication for Google Sign-In
    implementation("com.google.firebase:firebase-auth-ktx")

    // Google Sign-In SDK
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // Firebase Analytics (optional but recommended)
    implementation("com.google.firebase:firebase-analytics")
//    implementation(fileTree(mapOf(
//        "dir" to "C:\\Users\\BINH\\OneDrive\\Desktop\\projectprm\\PRM392_FinalProject\\app\\zalopaylibs",
//        "include" to listOf("*.aar", "*.jar"),
//        "exclude" to listOf("")
//    )))
    implementation(fileTree(mapOf(

        "dir" to "C:\\Users\\Gia Bao\\Documents\\Zalo Received Files",

        "dir" to "D:\\ZaloPay",

        "include" to listOf("*.aar", "*.jar"),
        "exclude" to listOf("")
    )))

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


    implementation("com.squareup.picasso:picasso:2.8")

    // ===== MATERIAL DESIGN =====
    implementation ("com.google.android.material:material:1.9.0")

    // ===== GOOGLE MAPS & LOCATION =====
    implementation ("com.google.android.gms:play-services-maps:18.1.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.google.android.gms:play-services-places:17.0.0")

    // ===== RETROFIT & NETWORKING =====
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.11.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("commons-codec:commons-codec:1.14")

    // ===== MPANDROIDCHART FOR CHARTS =====
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // RecyclerView (hiển thị danh sách chat)
    implementation ("androidx.recyclerview:recyclerview:1.3.2")

    // OkHttp (gửi request đến Coze API)
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")

    // GSON (nếu cần parse JSON từ Coze API response)
    implementation ("com.google.code.gson:gson:2.10.1")


}