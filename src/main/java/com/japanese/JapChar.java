package com.japanese;

import joptsimple.util.PathConverter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLite;

import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JapChar {
    public final File COMMON_DIR = new File(RuneLite.RUNELITE_DIR.getPath() + File.separator + "Japanese_Plugin_resources");
    public final File charDir = new File(COMMON_DIR+ File.separator + "char");
    @Getter
    private final String pathToChar = charDir.getPath();
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

}
