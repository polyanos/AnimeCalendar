package com.example.gregor.animecalender.Utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

import com.example.gregor.animecalender.Domain.ImageToLoad;
import com.example.gregor.animecalender.R;

import java.net.HttpURLConnection;
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
     *
     * @param context
     */
    public ImageLoader(Context context) {
        fileChache = new FileCache(context);
        viewCache = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
        this.context = context;
        threadManager = Executors.newSingleThreadExecutor();
    }

    /**
     *
     * @param view
     * @param imageToLoad
     */
    public void ShowImage(ImageView view, ImageToLoad imageToLoad) {
        if (imageReused(view, imageToLoad)) {
            Log.i(TAG, "Reused ImageView with image: " + imageToLoad.getImageName());
            return;
        }
        Bitmap image = loadImageFromDisk(imageToLoad);
        if (image != null) {
            Log.i(TAG, "Loaded image " + imageToLoad.getImageName() + " from local storage.");
            viewCache.put(view, imageToLoad.getImageName());
            view.setImageDrawable(new BitmapDrawable(context.getResources(), image));
            return;
        } else {
            Log.i(TAG, "Loaded image " + imageToLoad.getImageName() + " from web storage.");
            threadManager.execute(new WebImageLoader(view, imageToLoad));
        }
    }

    /**
     *
     * @param imageToLoad
     * @return
     */
    public Bitmap getImage(ImageToLoad imageToLoad) {
        Bitmap image = loadImageFromDisk(imageToLoad);
        if (image != null) {
            Log.i(TAG, "Loaded image " + imageToLoad.getImageName() + " from local storage.");
            return image;
        } else {
            Log.i(TAG, "Loaded image " + imageToLoad.getImageName() + " from web storage.");
            image = loadImageFromWeb(imageToLoad);

            if (image != null) {
                return image;
            } else {
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.stub);
            }
        }
    }

    private Bitmap loadImageFromDisk(ImageToLoad imageToLoad) {
        Bitmap image;
        if ((image = fileChache.loadImage(imageToLoad.getImageName())) != null) {
            return image;
        } else {
            return null;
        }
    }

    @Nullable
    private Bitmap loadImageFromWeb(ImageToLoad imageToLoad) {
        Bitmap image;
        try {
            URL url = new URL(imageToLoad.getImageUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            image = BitmapFactory.decodeStream(connection.getInputStream());
            if (imageToLoad.saveInCache()) {
                fileChache.saveImage(image, imageToLoad.getImageName());
            }
            return image;
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    private boolean imageReused(ImageView imageView, ImageToLoad imageToLoad) {
        String showedImageName = viewCache.get(imageView);
        if (showedImageName == null || !showedImageName.equals(imageToLoad.getImageName())) {
            return false;
        }
        return true;
    }

    private class WebImageLoader implements Runnable {

        private ImageView imageView;
        private ImageToLoad imageToLoad;
        private Handler handler;

        public WebImageLoader(ImageView imageView, ImageToLoad imageToLoad) {
            this.imageView = imageView;
            this.imageToLoad = imageToLoad;
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
            Bitmap imageBitmap = loadImageFromWeb(imageToLoad);
            if (imageReused(imageView, imageToLoad)) {
                return;
            }
            viewCache.put(imageView, imageToLoad.getImageName());
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
