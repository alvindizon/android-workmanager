/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.example.background;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkManager;

import android.app.Application;
import android.net.Uri;
import android.text.TextUtils;

import com.example.background.workers.BlurWorker;
import com.example.background.workers.CleanupWorker;
import com.example.background.workers.SaveFileToImageWorker;

import static com.example.background.Constants.KEY_IMAGE_URI;

public class BlurViewModel extends AndroidViewModel {

    private Uri mImageUri;
    private WorkManager workManager;

    public BlurViewModel(@NonNull Application application) {
        super(application);
        workManager = WorkManager.getInstance(application);
    }

    /**
     * Create the WorkRequest to apply the blur and save the resulting image
     * @param blurLevel The amount to blur the image
     */
    void applyBlur(int blurLevel) {
        // Start with Cleanup temp images WorkRequest
        WorkContinuation continuation =
                workManager.beginWith(new OneTimeWorkRequest.Builder(CleanupWorker.class).build());

        // blur the image x number of times
        for(int i = 0; i < blurLevel; i++) {
            // Continue with blur image WorkRequest
            OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest.Builder(BlurWorker.class);

            if(i == 0) {
                builder.setInputData(createInputDataForUri());
            }

            OneTimeWorkRequest blurRequest = builder.build();
            continuation = continuation.then(blurRequest);
        }

        // Save temp blurred image to final save location
        OneTimeWorkRequest save = new OneTimeWorkRequest.Builder(SaveFileToImageWorker.class)
                .build();
        continuation = continuation.then(save);

        // Actually start the work
        continuation.enqueue();


    }

    private Uri uriOrNull(String uriString) {
        if (!TextUtils.isEmpty(uriString)) {
            return Uri.parse(uriString);
        }
        return null;
    }

    /**
     * Setters
     */
    void setImageUri(String uri) {
        mImageUri = uriOrNull(uri);
    }

    /**
     * Getters
     */
    Uri getImageUri() {
        return mImageUri;
    }

    private Data createInputDataForUri() {
        Data.Builder builder = new Data.Builder();
        if(mImageUri != null) {
            builder.putString(KEY_IMAGE_URI, mImageUri.toString());
        }
        return builder.build();
    }

}