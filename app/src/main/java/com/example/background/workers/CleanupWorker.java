package com.example.background.workers;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;

import static com.example.background.Constants.OUTPUT_PATH;

/**
 * This class will be responsible for cleaning up temporary outputs before the actual
 * blurring happens.
 */
public class CleanupWorker extends Worker {
    private static final String TAG = CleanupWorker.class.getSimpleName();

    public CleanupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context appContext = getApplicationContext();
        WorkerUtils.makeStatusNotification("Cleaning up temp files...", appContext);
        WorkerUtils.sleep();
        try {
            File outputDirectory = new File(appContext.getFilesDir(), OUTPUT_PATH);
            if(outputDirectory.exists()) {
                File[] entries = outputDirectory.listFiles();
                if(entries != null && entries.length > 0) {
                    for(File entry : entries) {
                        String name = entry.getName();
                        if(!TextUtils.isEmpty(name) && name.endsWith(".png")) {
                            boolean deleted = entry.delete();
                            Log.i(TAG, String.format("Deleted %s - %s", name, deleted));
                        }
                    }
                }
            }
            return Worker.Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error cleaning up", e);
            return Worker.Result.failure();
        }
    }
}
