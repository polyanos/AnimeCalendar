package com.example.gregor.animecalender.Utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.widget.ImageView;

import com.example.gregor.animecalender.Domain.Dimension;
import com.example.gregor.animecalender.Domain.ImageToLoad;
import com.example.gregor.animecalender.R;
import com.example.gregor.animecalender.Utility.Interface.Api;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Gregor on 27-10-2015.
 */
public class ImageLoader {
    private final String TAG = "ImageLoader";

    private FileCache fileChache;
    private Map<ImageView, String> viewCache;
    private Context context;
    private Executor threadManager;

    /**
     * @param context
     */
    public ImageLoader(Context context) {
        fileChache = new FileCache(context);
        viewCache = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
        this.context = context;
        threadManager = Executors.newSingleThreadExecutor();
    }

    /**
     * Tries to load the specified image from the local storage or web storage. After the file has been loaded a redraw request will be posted on the UI thread.
     * @param view The ImageView which will show the image.
     * @param imageToLoad The information required to load the image.
     */
    public void ShowImage(ImageView view, ImageToLoad imageToLoad, Api api) {
        if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("show_images", true)) {
            view.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.stub, context.getTheme()));
            return;
        }

        if (imageReused(view, imageToLoad)) {
            Log.i(TAG, "Reused ImageView with image: " + imageToLoad.getFileName());
            return;
        }
        Bitmap image = loadImageFromDisk(imageToLoad, api);
        if (image != null) {
            Log.i(TAG, "Loaded image " + imageToLoad.getFileName() + " from local storage.");
            viewCache.put(view, String.valueOf(imageToLoad.getFileName()));
            view.setImageDrawable(new BitmapDrawable(context.getResources(), image));
            return;
        } else {
            view.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.stub, context.getTheme()));
            Log.i(TAG, "Loaded image " + imageToLoad.getFileName() + " from web storage.");
            threadManager.execute(new WebImageLoader(view, imageToLoad, api));
        }
    }

    /**
     * Tries to load the specified image from the local storage or web storage. This method wont post a redraw request but will block until it has loaded the image and return it..
     * @param imageToLoad The ImageView which will show the image.
     * @return The image that has been loaded.
     */
    public Bitmap getImage(ImageToLoad imageToLoad, Api api) {
        if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("show_images", true)) {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.stub);
        }

        Bitmap image = loadImageFromDisk(imageToLoad, api);
        if (image != null) {
            Log.i(TAG, "Loaded image " + imageToLoad.getFileName() + " from local storage.");
            return image;
        } else {
            image = loadImageFromWeb(imageToLoad, api);
            if (image != null) {
                Log.i(TAG, "Loaded image " + imageToLoad.getFileName() + " from web storage.");
                return image;
            } else {
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.stub);
            }
        }
    }

    private Bitmap loadImageFromDisk(ImageToLoad imageToLoad, Api api) {
        Bitmap image;
        if ((image = fileChache.loadImage(imageToLoad, api)) != null) {
            if (imageToLoad.cropImage()) {
                return cropBitmap(image, imageToLoad.getNewDimensions());
            } else {
                return image;
            }
        } else {
            return null;
        }
    }

    private Bitmap cropBitmap(Bitmap image, Dimension newDimensions) {
        float scale = (float) newDimensions.getWidth() / image.getWidth();
        int newWidth = (int) (image.getWidth() * scale);
        int newHeight = (int) (image.getHeight() * scale);
        int targetWidth = newDimensions.getWidth() > newWidth ? newWidth : newDimensions.getWidth();
        int targetHeight = newDimensions.getHeight() > newHeight ? newHeight : newDimensions.getHeight();
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        image = Bitmap.createScaledBitmap(image, newWidth, newHeight, false);
        return Bitmap.createBitmap(image, 0, 0, targetWidth, targetHeight);
    }

    @Nullable
    private Bitmap loadImageFromWeb(ImageToLoad imageToLoad, Api api) {
        Bitmap image;
        try {
            URL url = new URL(imageToLoad.getImageUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            image = BitmapFactory.decodeStream(connection.getInputStream());
            if (imageToLoad.isSaveInCache()) {
                fileChache.saveImage(image, imageToLoad, api);
            }
            if (imageToLoad.cropImage()) {
                image = cropBitmap(image, imageToLoad.getNewDimensions());
            }
        } catch (MalformedURLException e) {
            Log.wtf(TAG, "Invalid url detected. Check if the data is passed correctly.");
            image = null;
        } catch (IOException e) {
            Log.d(TAG, "An error occured with either saving or decoding the image. The exact error message was: " + e.getMessage());
            image = null;
        }
        return image;
    }

    private boolean imageReused(ImageView imageView, ImageToLoad imageToLoad) {
        String showedImageName = viewCache.get(imageView);
        if (showedImageName == null || !showedImageName.equals(imageToLoad.getFileName())) {
            return false;
        }
        return true;
    }

    private class WebImageLoader implements Runnable {

        private ImageView imageView;
        private ImageToLoad imageToLoad;
        private Handler handler;
        private Api api;

        public WebImageLoader(ImageView imageView, ImageToLoad imageToLoad, Api api) {
            this.imageView = imageView;
            this.imageToLoad = imageToLoad;
            this.api = api;
            handler = new Handler(Looper.getMainLooper());
        }

        /**
         * Starts executing the active part of the class' code. This method is
         * called when a thread is started that has been created with a class which
         * implements {@code Runnable}.
         */
        @Override
        public void run() {
            if (imageReused(imageView, imageToLoad)) {
                return;
            }
            Bitmap imageBitmap = loadImageFromWeb(imageToLoad, api);
            if (imageReused(imageView, imageToLoad)) {
                return;
            }
            viewCache.put(imageView, imageToLoad.getFileName());
            handler.post(new DisplayImage(imageView, imageBitmap));
        }
    }

    private class DisplayImage implements Runnable {
        ImageView imageView;
        Bitmap imageBitmap;

        public DisplayImage(ImageView imageView, Bitmap imageBitmap) {
            this.imageView = imageView;
            this.imageBitmap = imageBitmap;
        }

        /**
         * Starts executing the active part of the class' code. This method is
         * called when a thread is started that has been created with a class which
         * implements {@code Runnable}.
         */
        @Override
        public void run() {
            imageView.setImageDrawable(new BitmapDrawable(context.getResources(), imageBitmap));
        }
    }
}
