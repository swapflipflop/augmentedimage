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

### Resources
* Sample QR codes in `app\sampledata\images\qr`.
  * Those ending b.png have logos in the middle. You can photoshop in any icon without ruining the QR code.
* Sample mission files in `app\sampledata\Missions`.
* Use this site for editing .md files: https://dillinger.io/

## Issues

### Unsolved
* No idea how to get Unity to use Android file system only. Sample file choosers were showing development PC storage paths.
  * For now, switch to 'pure' Android.
  * Presume there will be a way to 'import' android activities in Unity, for merging codes.
* ZXing library is slow; scans sometimes take a long time and fails.
  * Alternate com.google.android.gms.vision library did Not work.

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