package com.example.background.workers;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.background.R;

import static com.example.background.Constants.KEY_IMAGE_URI;

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
        String resourceUri = getInputData().getString(KEY_IMAGE_URI);



        try {
            if(TextUtils.isEmpty(resourceUri)) {
                Log.e(TAG, "Invalid input uri");
                throw new IllegalArgumentException("Invalid input uri");
            }
            ContentResolver resolver = appContext.getContentResolver();
            // Create the bitmap from the resource URI
            Bitmap bitmap = BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(resourceUri)));

            // blur the bitmap
            Bitmap blurredBitmap = WorkerUtils.blurBitmap(bitmap, appContext);

            // display notification
            Uri outputUri = WorkerUtils.writeBitmapToFile(appContext, blurredBitmap);
            WorkerUtils.makeStatusNotification("Output is " + outputUri.toString(), appContext);

            // to be used for chaining
            Data outputData = new Data.Builder()
                    .putString(KEY_IMAGE_URI, outputUri.toString())
                    .build();

            return Result.success(outputData);
        } catch (Throwable e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
}
