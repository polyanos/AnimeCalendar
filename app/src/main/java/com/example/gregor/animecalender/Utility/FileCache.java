package com.example.gregor.animecalender.Utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.gregor.animecalender.Domain.FileToLoad;
import com.example.gregor.animecalender.Domain.ImageToLoad;
import com.example.gregor.animecalender.Utility.Interface.Api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by Gregor on 27-10-2015.
 */
public class FileCache {
    private final static String STANDARD_ANIME_IMAGE_DIRECTORY = "image";
    private final static String STANDARD_XML_DIRECTORY = "xml";
    private final static String STANDARD_CHARACTER_IMAGE_DIRECTORY = "character";
    private final static String TAG = "FileCache";
    private final static String IMAGE_EXTENSION = ".jpg";
    private final static String XML_EXTENSION = ".xml";
    private final static String GZIP_EXTENSION = ".gz";

    private final static char SEPERATION_CHAR = File.separatorChar;

    private Context context;

    public FileCache(Context context) {
        this.context = context;
    }

    public String getStandardAnimeImageDirectory() {
        return STANDARD_ANIME_IMAGE_DIRECTORY;
    }

    public String getStandardXmlDirectory() {
        return STANDARD_XML_DIRECTORY;
    }

    public String getStandardCharacterImageDirectory() {
        return STANDARD_CHARACTER_IMAGE_DIRECTORY;
    }

    /**
     * @param fileToLoad
     * @return
     */
    public Bitmap loadImage(FileToLoad fileToLoad, Api api) {
        String directory = getPath(fileToLoad, api);
        String filename = fileToLoad.getFileName() + IMAGE_EXTENSION;
        InputStream file = loadFile(filename, directory);
        if (file != null) {
            return BitmapFactory.decodeStream(file);
        } else {
            return null;
        }
    }

    /**
     * @param fileToLoad
     * @return
     */
    public InputStream loadXmlFile(FileToLoad fileToLoad, Api api) throws IOException {
        String directory = getPath(fileToLoad, api);
        String filename = fileToLoad.getFileName() + XML_EXTENSION + GZIP_EXTENSION;
        InputStream inputStream = loadFile(filename, directory);
        if (inputStream != null) {
            GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
            return gzipInputStream;
        } else {
            return null;
        }
    }

    /**
     * @param xmlData
     * @param fileData
     * @return
     */
    public void saveXmlFile(String xmlData, FileToLoad fileData, Api api) throws IOException {
        String directory = getPath(fileData, api);
        String filename = fileData.getFileName() + XML_EXTENSION + GZIP_EXTENSION;
        File file = new File(directory, filename);
        GZIPOutputStream gzipOutputStream = null;

        Log.d(TAG, "Writing " + filename + " in drirectory " + directory + " to disk.");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            gzipOutputStream = new GZIPOutputStream(fileOutputStream);
            gzipOutputStream.write(xmlData.getBytes());
            gzipOutputStream.close();
        } finally {
            if (gzipOutputStream != null) gzipOutputStream.close();
        }

        Log.d(TAG, "Succesfully written the file to disk.");
    }

    /**
     * @param xmlDataStream
     * @param fileData
     * @return
     */
    public void saveXmlFile(InputStream xmlDataStream, FileToLoad fileData, Api api) throws IOException {
        byte[] buffer = new byte[1024];
        String directory = getPath(fileData, api);
        String filename = fileData.getFileName() + XML_EXTENSION + GZIP_EXTENSION;

        File file = new File(directory, filename);
        GZIPOutputStream gzipOutputStream = null;

        Log.d(TAG, "Writing " + filename + " in drirectory " + directory + " to disk.");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            gzipOutputStream = new GZIPOutputStream(fileOutputStream);
            int readBytes;
            while ((readBytes = xmlDataStream.read(buffer)) != -1) {
                gzipOutputStream.write(buffer, 0, readBytes);
            }
            gzipOutputStream.close();
        } finally {
            if (gzipOutputStream != null) gzipOutputStream.close();
        }

        Log.d(TAG, "Succesfully written the file to disk.");
    }

    /**
     * @return
     */
    public void createStandardDirectories() {
        int createdDirectories = 0;
        String[] apiDirectories = new String[]{new AniDBApi(null).getApiDirectory(), new AnilistApi(null).getApiDirectory()};
        String[] directories = new String[]{STANDARD_ANIME_IMAGE_DIRECTORY, STANDARD_CHARACTER_IMAGE_DIRECTORY, STANDARD_XML_DIRECTORY};

        Log.d(TAG, "Trying to create: " + apiDirectories.length * directories.length + " directories.");

        for (String apiDirectory : apiDirectories) {
            for (String directory : directories) {
                if (createDirectory(apiDirectory + SEPERATION_CHAR + directory))
                    createdDirectories++;
            }
        }

        Log.d(TAG, "Created " + createdDirectories + " directories.");
    }

    /**
     * @param image
     * @param imageToLoad
     * @throws IOException
     */
    public void saveImage(Bitmap image, ImageToLoad imageToLoad, Api api) throws IOException {
        FileOutputStream fileOutputStream = null;
        String directory = getPath(imageToLoad, api);
        String filename = imageToLoad.getFileName() + IMAGE_EXTENSION;
        try {
            File file = new File(directory, filename);
            Log.d(TAG, "Saving image in: " + file.getPath());
            fileOutputStream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.close();
        } catch (IOException ex) {
            Log.e(TAG, "Failed to save image " + filename + " in " + directory);
            throw new IOException(ex);
        } finally {
            if (fileOutputStream != null) fileOutputStream.close();
        }
    }


    public void clearCachedImages() {
        String[] apiDirectories = new String[]{new AniDBApi(null).getApiDirectory(), new AnilistApi(null).getApiDirectory()};
        String[] directories = new String[]{STANDARD_ANIME_IMAGE_DIRECTORY, STANDARD_CHARACTER_IMAGE_DIRECTORY};
        for(String api : apiDirectories){
            for(String dir : directories){
                emptyDirectory(api + SEPERATION_CHAR + dir);
            }
        }
    }

    public void clearCachedXMLFiles() {
        String[] apiDirectories = new String[]{new AniDBApi(null).getApiDirectory(), new AnilistApi(null).getApiDirectory()};
        String dir = STANDARD_XML_DIRECTORY;

        for(String api : apiDirectories){
            emptyDirectory(api + SEPERATION_CHAR + dir);
        }
    }

    private boolean emptyDirectory(String dir){
        boolean succeeded = true;
        File root = context.getFilesDir();
        File directory = new File(root.getPath() + SEPERATION_CHAR + dir);
        Log.d(TAG, "Deleting the folder " + directory.getPath());
        for(File file : directory.listFiles()){
            succeeded = file.delete();
            if(!succeeded){
                break;
            }
        }
        if(succeeded){
            Log.d(TAG, "All files have been deleted from " + directory.getPath());
        } else{
            Log.d(TAG, "A file couldn't be deleted.");
        }
        return succeeded;
    }

    private boolean createDirectory(String dirName) {
        File file = new File(context.getFilesDir().getPath() + SEPERATION_CHAR + dirName);
        Log.d(TAG, "Creating the " + file.getPath() + " directory");
        if (!file.exists()) {
            if (file.mkdirs()) {
                return true;
            } else {
                Log.i(TAG, "Failed to create the " + file.getPath() + " directory.");
            }
        } else {
            Log.d(TAG, "Directory " + file.getPath() + " already exists, skipping creation.");
        }
        return false;
    }

    /**
     * @param completeFileName
     * @param fullDirectory
     * @return
     */
    private InputStream loadFile(String completeFileName, String fullDirectory) {
        File file = new File(fullDirectory, completeFileName);

        Log.d(TAG, "Trying to load the file " + completeFileName + " in " + fullDirectory);
        if (file.exists()) {
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(file);
            } catch (FileNotFoundException ex) {
                Log.e(TAG, "The file " + completeFileName + " couldn't be opened. The given error message was: " + ex.getMessage());
                return null;
            }
            Log.d(TAG, "The file " + completeFileName + " was successfully loaded.");
            return inputStream;
        } else {
            Log.d(TAG, "The file " + completeFileName + " didn't exist.");
            return null;
        }
    }

    private String getPath(FileToLoad fileToLoad, Api api) {
        return context.getFilesDir().getPath() + SEPERATION_CHAR + api.getApiDirectory() + SEPERATION_CHAR + fileToLoad.getFileDirectory();
    }
}
