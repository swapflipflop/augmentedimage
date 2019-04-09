/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ar.sceneform.samples.augmentedimage;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;

import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.samples.common.helpers.SnackbarHelper;
import com.google.ar.sceneform.ux.ArFragment;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import android.widget.Toast;

import org.ds.FileChooser;
import org.ds.FileUtils;
import org.ds.Mission;

/**
 * This application demonstrates using augmented images to place anchor nodes. app to include image
 * tracking functionality.
 */
public class AugmentedImageActivity extends AppCompatActivity {
    private static final String TAG = "Rehearsal";

    private ArFragment arFragment;
    private ImageView fitToScanView;

    // The UI to play next animation.
    private FloatingActionButton butFileOpen;
    // The UI to toggle wearing the hat.
    private FloatingActionButton butQrOpen;

    protected String curMissionPath = null;
    protected Toast m_toastStatus = null;
    protected Mission curMission = null;

    // Augmented image and its associated center pose anchor, keyed by the augmented image in
    // the database.
    private final Map<AugmentedImage, AugmentedImageNode> augmentedImageMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arFragment = (ArFragment)getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        fitToScanView = findViewById(R.id.image_view_fit_to_scan);

        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);

        // Add command buttons
        butFileOpen = findViewById(R.id.but_fileopen);
        butFileOpen.setEnabled(true);
        butFileOpen.setOnClickListener(this::onFileOpen);
        butQrOpen = findViewById(R.id.but_qropen);
        butQrOpen.setEnabled(true);
        butQrOpen.setOnClickListener(this::onQrOpen);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (augmentedImageMap.isEmpty()) {
            fitToScanView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Registered with the Sceneform Scene object, this method is called at the start of each frame.
     *
     * @param frameTime - time since last frame.
     */
    private void onUpdateFrame(FrameTime frameTime) {
        Frame frame = arFragment.getArSceneView().getArFrame();

        // If there is no frame or ARCore is not tracking yet, just return.
        if (frame == null || frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
            return;
        }

        Collection<AugmentedImage> updatedAugmentedImages =
            frame.getUpdatedTrackables(AugmentedImage.class);
        for (AugmentedImage augmentedImage : updatedAugmentedImages) {
            switch (augmentedImage.getTrackingState()) {
                case PAUSED:
                    // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
                    // but not yet tracked.
                    String text = "Detected Image " + augmentedImage.getIndex();
                    SnackbarHelper.getInstance().showMessage(this, text);
                    break;

                case TRACKING:
                    // Have to switch to UI Thread to update View.
                    fitToScanView.setVisibility(View.GONE);

                    // Create a new anchor for newly found images.
                    if (!augmentedImageMap.containsKey(augmentedImage)) {
                        AugmentedImageNode node = new AugmentedImageNode(this);
                        node.setImage(augmentedImage);
                        augmentedImageMap.put(augmentedImage, node);
                        arFragment.getArSceneView().getScene().addChild(node);
                    }
                    break;

                case STOPPED:
                    augmentedImageMap.remove(augmentedImage);
                    break;
            }
        }
    }

    private void onFileOpen(View unusedView) {
        Intent i = new Intent(this, FileChooser.class);
        startActivityForResult(i, FileChooser.ACTIVITY_SELECTFILE);
    }

    private void onQrOpen(View unusedView) {
        Toast toast = Toast.makeText(this, "QR open", Toast.LENGTH_SHORT);
        Log.d(
            TAG,
            String.format(
                "QR open"));
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == FileChooser.ACTIVITY_SELECTFILE)
        {
            if (resultCode == RESULT_OK)
            {
                if (intent != null)
                {
                    Bundle extras = intent.getExtras();
                    if (extras != null)
                    {
                        String filepath = extras.getString(FileChooser.KEY_FILEPATH);
                        loadMission(filepath);
                    }
                }
            }
        }
    }

    protected void loadMission(String filepath)
    {
        if ((filepath == null) || (filepath.length() <= 0))
        {
            showStatus(R.string.error_no_mission);
            return; //abort
        }
        curMissionPath = filepath;
        //TODO: proceed to load & read mission file
        String content = FileUtils.readFile(filepath);
        curMission = new Mission(content);
        Toast toast = Toast.makeText(this, "File open: " + filepath, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    protected void showStatus(int nRscIndex)
    {
        if (m_toastStatus == null)
            m_toastStatus = Toast.makeText(getApplicationContext(), nRscIndex, Toast.LENGTH_SHORT);
        else
        {
            m_toastStatus.setText(nRscIndex);
            m_toastStatus.setDuration(Toast.LENGTH_SHORT);
        }
        m_toastStatus.show();
    }
}
