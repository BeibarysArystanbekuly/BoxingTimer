TimerBoxing

TimerBoxing is a customizable boxing and interval training timer for Android. Users can create and save different training timers with their own round duration, rest duration, and number of rounds.

Functionality
Create a custom boxing timer.
Set the duration of each round.
Set the duration of each rest period.
Set the total number of rounds.
Save timer presets locally.
View saved timer presets.
Edit existing timer presets.
Delete timer presets.
Automatically switch between round and rest states.
Display the remaining time during a workout.

C:.
│   .gitignore
│   Boxing Timer.jpg
│   Boxing Timer.pdf
│   build.gradle.kts
│   gradle.properties
│   gradlew
│   gradlew.bat
│   local.properties
│   Overview.docx
│   settings.gradle.kts
│   
├───.gradle
│   ├───9.6.0
│   │   │   gc.properties
│   │   │   
│   │   ├───checksums
│   │   │       checksums.lock
│   │   │       md5-checksums.bin
│   │   │       sha1-checksums.bin
│   │   │       
│   │   ├───executionHistory
│   │   │       executionHistory.lock
│   │   │       
│   │   ├───expanded
│   │   ├───fileChanges
│   │   │       last-build.bin
│   │   │       
│   │   ├───fileHashes
│   │   │       fileHashes.bin
│   │   │       fileHashes.lock
│   │   │       resourceHashesCache.bin
│   │   │       
│   │   ├───kotlin-dsl-plugin-entries
│   │   │       jar-entries.bin
│   │   │       kotlin-dsl-plugin-entries.lock
│   │   │       
│   │   └───vcsMetadata
│   ├───buildOutputCleanup
│   │       buildOutputCleanup.lock
│   │       cache.properties
│   │       
│   └───vcs-1
│           gc.properties
│           
├───.idea
│   │   .gitignore
│   │   .name
│   │   AndroidProjectSystem.xml
│   │   compiler.xml
│   │   gradle.xml
│   │   misc.xml
│   │   runConfigurations.xml
│   │   vcs.xml
│   │   workspace.xml
│   │   
│   ├───caches
│   │       deviceStreaming.xml
│   │       
│   └───codeStyles
│           codeStyleConfig.xml
│           Project.xml
│           
├───app
│   │   .gitignore
│   │   build.gradle.kts
│   │   
│   └───src
│       ├───androidTest
│       │   └───java
│       │       └───com
│       │           └───example
│       │               └───timerboxing
│       │                       ExampleInstrumentedTest.kt
│       │                       
│       ├───main
│       │   │   AndroidManifest.xml
│       │   │   
│       │   ├───java
│       │   │   └───com
│       │   │       └───example
│       │   │           └───timerboxing
│       │   │               │   MainActivity.kt
│       │   │               │   
│       │   │               └───ui
│       │   │                   └───theme
│       │   │                           Color.kt
│       │   │                           Theme.kt
│       │   │                           Type.kt
│       │   │                           
│       │   ├───keepRules
│       │   │       rules.keep
│       │   │       
│       │   └───res
│       │       ├───drawable
│       │       │       ic_launcher_background.xml
│       │       │       ic_launcher_foreground.xml
│       │       │       
│       │       ├───mipmap-anydpi-v26
│       │       │       ic_launcher.xml
│       │       │       ic_launcher_round.xml
│       │       │       
│       │       ├───mipmap-hdpi
│       │       │       ic_launcher.webp
│       │       │       ic_launcher_round.webp
│       │       │       
│       │       ├───mipmap-mdpi
│       │       │       ic_launcher.webp
│       │       │       ic_launcher_round.webp
│       │       │       
│       │       ├───mipmap-xhdpi
│       │       │       ic_launcher.webp
│       │       │       ic_launcher_round.webp
│       │       │       
│       │       ├───mipmap-xxhdpi
│       │       │       ic_launcher.webp
│       │       │       ic_launcher_round.webp
│       │       │       
│       │       ├───mipmap-xxxhdpi
│       │       │       ic_launcher.webp
│       │       │       ic_launcher_round.webp
│       │       │       
│       │       ├───values
│       │       │       colors.xml
│       │       │       strings.xml
│       │       │       themes.xml
│       │       │       
│       │       └───xml
│       │               backup_rules.xml
│       │               data_extraction_rules.xml
│       │               
│       └───test
│           └───java
│               └───com
│                   └───example
│                       └───timerboxing
│                               ExampleUnitTest.kt
│                               
└───gradle
    │   gradle-daemon-jvm.properties
    │   libs.versions.toml
    │   
    └───wrapper


    Build and Run
Clone the repository or download the project.
Open the project in Android Studio.
Wait for Gradle synchronization to finish.
Select an Android emulator or connect an Android device.
Click the Run button in Android Studio.
The application will build and launch on the selected device.
            gradle-wrapper.jar
            gradle-wrapper.properties
            
