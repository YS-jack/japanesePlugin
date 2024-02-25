package com.japanese;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.game.ChatIconManager;

import javax.inject.Inject;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
//for testing


@Slf4j
public class JapTransforms {
    private HashMap<String, String> knownDeepL;
    private HashMap<String, String> knownDirect;
    private HashMap<String, String> directWord;
    private HashMap<String, String> menuOptionTran;
    private HashMap<String, String> itemNpcTran;
    private HashMap<String, String> transliterationMap;
    // didnt work : src/main/resources/com/japanese/translations
    public void initTransHash() throws Exception {
        String transDataDir = "src/main/resources/com/japanese/translations";
        knownDeepL = new HashMap<>();
        putToDictHash(knownDeepL, transDataDir + "/KnownDeepLTranslations.csv");
        knownDirect = new HashMap<>();
        putToDictHash(knownDirect, transDataDir + "/KnownDirectTranslations.csv");
        directWord = new HashMap<>();
        putToDictHash(directWord, transDataDir + "/DirectWordTranslations.txt");
        menuOptionTran = new HashMap<>();
        putToDictHash(menuOptionTran, transDataDir + "/MenuOptionsDirect.csv");
        itemNpcTran = new HashMap<>();
        putToDictHash(itemNpcTran, transDataDir + "/ItemAndNPCTranslations.csv");
        transliterationMap = new HashMap<>();
        putToDictHash(transliterationMap, transDataDir + "/transliteration.csv");
    }
    private void putToDictHash(HashMap<String, String> dictHash, String dir) {

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(dir), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    dictHash.put(parts[0].trim().toLowerCase(), parts[1].trim().toLowerCase());
                    ////log.info("parts[1] = " + parts[1].trim() +", equal to 調理する?:"+parts[1].trim().equals("調理する"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public enum transformOptions{
        doNothing, wordToWord, DeepL, transliterate, alpToJap
        //wordToWord : translate using word to word dictionary, ignores grammar
        //alpToJap : transform alphabet input already in japanese to japanese characters : eg. petto no neko -> ペットの猫 (= a pet cat)
        //transliterate : similar to alpToJap, but output will all be Katakana form
    }
    //returns concat of "img<color--##.png>"

    public String getTransformWithColors(String enWithColors, transformOptions transOpt,HashMap<String, Integer> hashMap, ChatIconManager chatIconManager) {
        if (enWithColors.contains("<img=")){
            return enWithColors;
        }
        String[][] colorWords = getColorWordArray(enWithColors, transOpt);// = {{"ffffff","White string"},{"ff0000","red"},...}
        //todo-translate words in colorWords as specified with transOpt
        StringBuilder imgTagStrings = new StringBuilder();
        for (int i = 0; i < colorWords.length; i++) {

            for (int j = 0; j < colorWords[i][1].length();) {
                ////log.info("getTransformWithColors: the word is " + colorWords[i][1] + "codePointAt("+j+") = " + colorWords[i][1].codePointAt(j));
                int codePoint = colorWords[i][1].codePointAt(j);
                imgTagStrings.append("<img=");
                String imgName = colorWords[i][0] + "--" + codePoint + ".png";
                int hash = hashMap.getOrDefault(imgName, -99);
                if (hash == -99) {
                    //log.info("error creating hash for character : " + codePoint + ", with img name : " + imgName);
                }
                imgTagStrings.append(chatIconManager.chatIconIndex(hash));
                imgTagStrings.append(">");
                j += Character.isHighSurrogate(colorWords[i][1].charAt(j)) ? 2 : 1;
            }
        }
        //"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"
        ////log.info("\n");
        return imgTagStrings.toString();
    }
    private String[][] getColorWordArray(String enWithColors, transformOptions transOpt) {
        int colorTagNum = enWithColors.split(">").length - 1;
        String[][] colorWords;// = {{"ffffff","White string"},{"ff0000","red"},...}
        if (colorTagNum == 0) {
            colorWords = new String[1][2];
            Colors white = Colors.white;
            colorWords[0][0] = white.getName();
            colorWords[0][1] = transform(enWithColors.trim(), transOpt);
            ////log.info("transform result : " + colorWords[0][1]);
        }
        else {
            colorWords = new String[colorTagNum][2];
            ////log.info("target had " + colorTagNum + " color tags");
            String[] colorArray = getColorArray(enWithColors);
            ////log.info("colorArray[0] = " + colorArray[0]);
            String[] wordArray = getWordArray(enWithColors);
            ////log.info("wordArray[0] = " + wordArray[0]);

            for (int i = 0; i < colorTagNum; i++) {
                colorWords[i][0] = colorArray[i];
                colorWords[i][1] = transform(wordArray[i], transOpt);
                ////log.info("colorWords[" + i + "][0]" + colorWords[i][0] );
                ////log.info("colorWords[" + i + "][1]" + colorWords[i][1] );
            }

        }
        return colorWords;
    }

    private String[] getColorArray(String wordAndColor) {
        String[] parts = wordAndColor.split("<col=");
        String[] colorArray = new String[parts.length - 1];

        for (int i = 0; i < parts.length - 1; i++) {
            Colors c = Colors.fromHex(parts[i+1].split(">")[0]);
            colorArray[i] = c.getName();
            if (colorArray[i] == null || Objects.equals(colorArray[i], "")){
                colorArray[i] = Colors.white.getName();
            }
            ////log.info("added color : " + c.getName());
        }
        return colorArray;
    }
    private String[] getWordArray(String wordAndColor) {
        String[] parts = wordAndColor.split(">");
        String[] wordArray = new String[parts.length - 1];

        for (int i = 0; i < parts.length - 1; i++) {
            wordArray[i] = parts[i+1].split("<")[0];
            if (wordArray[i] == null || Objects.equals(wordArray[i], "")) {
                wordArray[i] = "?";
            }
            ////log.info("added words : " + wordArray[i]);
        }
        return  wordArray;
    }

    private String transform(String enString, transformOptions transOpt) {
        String enStringLower = enString.toLowerCase();
        switch(transOpt) {
            case doNothing:
                return enString;
            case wordToWord:
                ////log.info("option = " + transOpt);
                return getW2WTranslation(enStringLower);
            case DeepL:
            case transliterate:
            case alpToJap:
            default:
                return enString;
        }
    }

    private String getW2WTranslation(String en) {
        //log.info("transforming : " + en);
        en = en.toLowerCase();
        if (knownDeepL.containsKey(en)) {
            //log.info("found in knownDeepl");
            return knownDeepL.get(en);
        } else if (knownDirect.containsKey(en)) {
            //log.info("found in knownDirect");
            return knownDirect.get(en);
        } else if (menuOptionTran.containsKey(en)) {
            //log.info("found " + en + " in menuOptioinTran");
            return menuOptionTran.get(en);
            //log.info("returning result : " + result);
        } else if (itemNpcTran.containsKey((en))) {
            //log.info("found in ItemNpcTran");
            return itemNpcTran.get(en);
        }  else {

            String[] wordArray = en.split(" ");
            StringBuilder resultBuilder = new StringBuilder();
            for (String word : wordArray) {
                if (directWord.containsKey(word)) {
                    //log.info("found in directWord");
                    resultBuilder.append(directWord.get(word));
                } else {
                    if (word.endsWith("s")){
                        String enNoS = word.substring(0,word.length()-1);
                        if (knownDirect.containsKey(enNoS)) {
                            resultBuilder.append(knownDirect.get(enNoS));
                        } else if (directWord.containsKey(enNoS)){
                            resultBuilder.append(directWord.get(enNoS));
                        } else {
                            resultBuilder.append(transliterationMap.getOrDefault(word,"?"));
                        }
                    }else{
                        //log.info("couldn't find word, transliterating");
                        resultBuilder.append(transliterte(word));
                    }
                }
            }
            return resultBuilder.toString();
        }
    }
    private String transliterte(String word) {
        StringBuilder katakana = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            String ch = String.valueOf(word.charAt(i));
            katakana.append(transliterationMap.getOrDefault(ch, ch));
        }
        return katakana.toString();
    }
}