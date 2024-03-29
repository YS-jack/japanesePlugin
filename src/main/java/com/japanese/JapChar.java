package com.japanese;

import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.io.FilenameFilter;

@Slf4j
public class JapChar {
    private String pathToChar = "src/main/resources/com/japanese/char";
    private String pathToChatButtons = "src/main/resources/com/japanese/specific/chatButtons";
    public String[] colors = {"black", "yellow", "blue", "red", "orange", "white", "lightblue", "green"};
    public String[] getCharList() {//get list of all characters( all colours * char)
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
    public String[] getChatButtonList() {
        FilenameFilter pngFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".png");
            }
        };
        File chatButtonDir = new File(pathToChatButtons + "/");
        File[] files = chatButtonDir.listFiles(pngFilter); //list of files that end with ".png"
        if (files == null){return null;}
        String[] fileNames = new String[files.length];
        for (int j = 0; j < files.length; j++) {
            fileNames[j] = files[j].getName();
        }
        return fileNames;
    }

}
