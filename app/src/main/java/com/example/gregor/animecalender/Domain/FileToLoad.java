package com.example.gregor.animecalender.Domain;

import android.support.annotation.NonNull;

/**
 * Created by Gregor on 5-11-2015.
 */
public class FileToLoad {
    String fileName, fileDirectory;

    public FileToLoad(@NonNull String fileName, @NonNull String fileDirectory) {
        this.fileName = fileName;
        this.fileDirectory = fileDirectory;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileDirectory() {
        return fileDirectory;
    }
}
