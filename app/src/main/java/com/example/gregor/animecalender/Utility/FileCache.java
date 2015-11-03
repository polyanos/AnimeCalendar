package com.example.gregor.animecalender.Utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Gregor on 27-10-2015.
 */
public class FileCache {
    Context context;

    public FileCache(Context context){
        this.context = context;
    }

    public Bitmap loadImage(String name){
        File file = new File(context.getFilesDir(), name);
        if(file.exists()) {
            return BitmapFactory.decodeFile(file.getPath());
        } else {
            return null;
        }
    }

    public void saveImage(Bitmap image, String name) throws IOException {
        File file = new File(context.getFilesDir(), name);
        if(!file.exists()){
            file.createNewFile();
        }
        FileOutputStream os = new FileOutputStream(file);
        image.compress(Bitmap.CompressFormat.JPEG, 90, os);
        os.close();
    }
}
