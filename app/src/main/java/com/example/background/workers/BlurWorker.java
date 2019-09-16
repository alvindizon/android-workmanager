package com.example.background.workers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.background.R;

public class BlurWorker extends Worker {
    private static final String TAG = BlurWorker.class.getSimpleName();

    public BlurWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "doWork: start");
        Context appContext = getApplicationContext();

        // open the test image and blur it
        try {
            Bitmap bitmap = BitmapFactory.decodeResource(
                    appContext.getResources(),
                    R.drawable.test);
            Bitmap blurredBitmap = WorkerUtils.blurBitmap(bitmap, appContext);
            Uri uri = WorkerUtils.writeBitmapToFile(appContext, blurredBitmap);
            WorkerUtils.makeStatusNotification("Output is " + uri.toString(), appContext);
            Log.d(TAG, "doWork: success");
            return Result.success();
        } catch (Throwable e) {
            Log.d(TAG, "doWork: error");
            e.printStackTrace();
            return Result.failure();
        }
    }
}
