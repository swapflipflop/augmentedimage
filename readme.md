# Mission Rehearsal app fragment

Extended from sceneform-android-sdk-1.7.0\samples\augmentedimage sample.
This app does _**Not**_ use Unity; instead using Android branch.


## Features
* Hardcoded paths:
  * Selected/main mission file will be: /sdcard/Missions.csv
* Expected file format:
  * not checked.
  * CSV, comma-separated. Team members are semi-colon (;) separated
* File chooser for mission CSV files.
  * It copies chosen file and overrides the main file.
  * Sends result (file path) back to main app now, and everyone in future.
* TODO: alternate file selection via scanning QR code, using ZXing library
  * QR code string of the form: `file:///<path>`
    * e.g.: `file:///mission1.csv`
* Spawns one of the ship layers as well as image frame corners. The layer is huge! (and opaque).

### Resources
* Sample QR codes in `app\sampledata\images\qr`.
  * Those ending b.png have logos in the middle. You can photoshop in any icon without ruining the QR code.
* Sample mission files in `app\sampledata\Missions`.
* Use this site for editing .md files: https://dillinger.io/
* Branch only. The 1st set of ship layers.
  * Raw OBJ & MTL file sets in `app\sampledata\modeldata\Ship\Ship Layers`
  * Converted to SFB (SceneForm Asset) in `app\src\main\assets\models` using Google Sceneform Tools plugin from within Android Studio.
    * Refer to guide @ https://mobikul.com/how-to-generate-sfb-model-from-obj-files-for-arcore/
    * In case the above page disappears, the steps are roughly:
      * In Android Studio, select File menu> Settings> Plugins.
        * Search for and install 'Google Sceneform Tools' plugin.
      * Place your source OBJ & MTL files in `app\sampledata`.
      * Return to Android Studio's project panel.
        * Select an OBJ file from `app\sampledata`, right-click select (1st command) 'Import Sceneform Asset'.
        * Change the output path if needed. E.g.: from default `app\src\main\assets\` to `app\src\main\assets\models`.
        * Click 'Finish' button to convert.
          * If it succeeds, you'll get a corresponding SFA file created. It can then be used in the ARCore app.
          * If it fails (e.g. missing MTL file in the set), it will ask if you would like to revert. Revert. 
      * In this app, the SFAs are loaded in AugmentedImageNode.java.

## Issues

### Unsolved
* No idea how to get Unity to use Android file system only. Sample file choosers were showing development PC storage paths.
  * For now, switch to 'pure' Android.
  * Presume there will be a way to 'import' android activities in Unity, for merging codes.
* ZXing library is slow; scans sometimes take a long time and fails.
  * Alternate com.google.android.gms.vision library did Not work.
* Wasn't able to configure/build correctly with either version of Node.JS for mobile. They are:
  * J2V8: https://github.com/eclipsesource/J2V8
    * Build issues:
      * unable to build in Windows 7, owing to clang/docker or other missing tools.
      * able to build in Centos (Linux), but unable to load in Android app, owing to missing static symbols.
      * unable to build in Ubuntu 18 (Linux), owing to Docker error - can't seem to reach docker registry. J2V8 uses docker containers for cross-platform compilation.
  * Node.JS mobile: https://github.com/janeasystems/nodejs-mobile
    * pre-built binaries provided, but unable to setup Android project correctly to incorporate it. Errors like:
      * configuration: wrong gradle/cmake versions/CMakeLists.txt.
      * compiler unable to find system include files!? <memory>, etc.

### Solved / Notes
* When using `android.support.constraint.ConstraintLayout`, errors encountered complaining of attributes not found, like: `layout_constraintLeft_toLeftOf`
  * Solution: add the following dependency to app's `build.gradle` file:
    * `implementation 'com.android.support.constraint:constraint-layout:1.1.3'`
    * This is because `ConstraintLayout` is not a standard inclusion.

* Symbol 'R' cannot be resolved.
  * Solution:
    * Do a rebuild. I.e. must clean gradle and resync.
    * If that fails, check and ensure you are Not using **gradle version 3.3.0**! Use this instead in project's `build.gradle` file:
      * `classpath 'com.android.tools.build:gradle:3.2.1'`
* `Files.readAllBytes` requires minimum API Level 26, which is set in  app's `build.gradle`.

## TODO
* Parse mission file.
  * Add sample route waypoints.
* Integrate ZXing QR code scanner app.
  * Got it working separately.
  * How to share usage of Camera without exploding?
* Scan faces to select team member?
* Scan ship to load ship model?
  * Sample ship images to recognise: app\src\main\assets\*.jpg less default.jpg
    * Hardcoded list for now.
* Figure out how to import Android activity in Unity.