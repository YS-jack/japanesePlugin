package com.japanese;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JapChar {
    private String pathToChar = "/com/japanese/char";
    private String pathToChatButtons = "/com/japanese/specific/chatButtons";
    public String[] colors = {"black", "yellow", "blue", "red", "orange", "white", "lightblue", "green"};
    public String[] getCharList() {//get list of all characters( all colours * char)
        //if not in jar file format
//        FilenameFilter pngFilter = new FilenameFilter() {
//            @Override
//            public boolean accept(File dir, String name) {
//                return name.toLowerCase().endsWith(".png");
//            }
//        };
//        File colorDir = new File(pathToChar + "/");
//        File[] files = colorDir.listFiles(pngFilter); //list of files that end with ".png"
//        if (files == null){return null;}
//        String[] fileNames = new String[files.length];
//        for (int j = 0; j < files.length; j++) {
//            fileNames[j] = files[j].getName();
//        }
//        return fileNames;
        // if in jar file
        String indexFilePath = pathToChar+ "/image_name_list.txt";
        List<String> fileNames = new ArrayList<>();

        try (InputStream is = getClass().getResourceAsStream(indexFilePath);
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader reader = new BufferedReader(isr)) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().endsWith(".png")) {
                    fileNames.add(line);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading the index file: " + e.getMessage());
            return null;
        }

        // Convert List to array
        return fileNames.toArray(new String[0]);
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
