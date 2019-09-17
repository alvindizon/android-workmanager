package com.example.background.workers;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.example.background.Constants.KEY_IMAGE_URI;

/**
 * This worker takes in the URI of a temporary blurred image, and saves the temp image
 * to a final location. The output of this worker is the URI of the final save location
 */
public class SaveFileToImageWorker extends Worker {
    private static final String TAG = SaveFileToImageWorker.class.getSimpleName();
    public static final String TITLE = "Blurred Image";
    public static final SimpleDateFormat DATE_FORMATTER =
            new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z", Locale.getDefault());

    public SaveFileToImageWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context appContext = getApplicationContext();

        ContentResolver resolver = appContext.getContentResolver();
        try {
            // get temporary image
            String resourceUri = getInputData().getString(KEY_IMAGE_URI);
            Bitmap inputBitmap  = BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(resourceUri)));

            // save temp to final location
            String outputUri = MediaStore.Images.Media.insertImage(
                    resolver, inputBitmap, TITLE, DATE_FORMATTER.format(new Date()));

            if(TextUtils.isEmpty(outputUri)) {
                Log.e(TAG, "Writing to MediaStore failed");
                return Result.failure();
            }

            Data outputData = new Data.Builder()
                    .putString(KEY_IMAGE_URI, outputUri)
                    .build();

            return Result.success(outputData);
        } catch (Exception e) {
            Log.e(TAG, "Unable to save image to Gallery", e);
            return Worker.Result.failure();
        }

    }
}
