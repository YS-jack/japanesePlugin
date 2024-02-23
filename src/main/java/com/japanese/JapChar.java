package com.japanese;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Stream;

@Slf4j
public class JapChar {
    private String pathToChar = "src/main/resources/com/japanese/char";
    private String[] charPath;
    private String[][] allCharPath;
    public String[] colors = {"black", "yellow", "blue", "red", "orange", "white", "lightblue", "green"};
    public String[] getCharList() {//get array of colour + char string
        FilenameFilter pngFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".png");
            }
        };
        File colorDir = new File(pathToChar + "/");
        File[] files = colorDir.listFiles(pngFilter); //list of files that end with ".png"
        if (files == null){return null;}
        String[] fileNames = new String[files.length];
        for (int j = 0; j < files.length; j++) {
            fileNames[j] = files[j].getName();
        }
        return fileNames;
    }

    public String[] getCharPathList(int len) {
        charPath = new String[len];
        return charPath;
    }
}
