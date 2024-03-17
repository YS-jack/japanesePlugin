package com.japanese;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.game.ChatIconManager;

import javax.inject.Inject;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import com.japanese.JapaneseConfig.*;

@Slf4j
public class JapTransforms {
    private HashMap<String, String> knownDeepL;
    private HashMap<String, String> knownDirect;
    private HashMap<String, String> directWord;
    private HashMap<String, String> menuOptionTran;
    private HashMap<String, String> itemNpcTran;
    private HashMap<String, String> transliterationMap;
    public HashMap<String, String> knownSettingTranslation;
    public HashMap<String, String> knownChatButtonSkillTranslation;
    @Inject
    private Client client;
    @Inject
    JapanesePlugin japanesePlugin;
    @Inject
    private RomToJap romToJap;

    public void initTransHash() throws Exception {
        String transDataDir = "src/main/resources/com/japanese/translations/";
        knownDeepL = new HashMap<>();
        putToDictHash(knownDeepL, transDataDir, "KnownDeepLTranslations.csv");
        knownDirect = new HashMap<>();
        putToDictHash(knownDirect, transDataDir , "KnownDirectTranslations.csv","SkillTranslations.csv");
        directWord = new HashMap<>();
        putToDictHash(directWord, transDataDir , "DirectWordTranslations.txt");
        menuOptionTran = new HashMap<>();
        putToDictHash(menuOptionTran, transDataDir,"KnownMenuOptionsDirect.csv");
        itemNpcTran = new HashMap<>();
        putToDictHash(itemNpcTran, transDataDir ,"ItemAndNPCTranslations.csv");
        transliterationMap = new HashMap<>();
        putToDictHash(transliterationMap, transDataDir,"transliteration.csv");

        //specific translations
        knownSettingTranslation = new HashMap<>();
        putToDictHash(knownSettingTranslation, transDataDir, "knownWidgets/KnownSettingTranslation.csv");
        knownChatButtonSkillTranslation = new HashMap<>();
        putToDictHash(knownChatButtonSkillTranslation, transDataDir, "knownWidgets/widgetTranslations.csv");

        //add known translation maps to knownDirect
        knownDirect.putAll(knownSettingTranslation);
        knownDirect.putAll(knownChatButtonSkillTranslation);
        log.info("end of making hashmap for translations");
//        knownDirect.entrySet().stream()
//                .forEach(entry -> log.info(entry.getKey() + " => " + entry.getValue()));
    }
    private void putToDictHash(HashMap<String, String> dictHash, String dirName, String... dirArray) {
        for (String dir:dirArray) {
            String dir2 = dirName + dir;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(dir2), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length == 2) {
                        dictHash.put(parts[0].trim().toLowerCase(), parts[1].trim());
                    } else if (parts.length == 1){
                        dictHash.put(parts[0].trim().toLowerCase(), parts[0].trim());
                    } else {
                        log.info("no pair found");
                    }
                }
            } catch (IOException e) {
                log.info("error creating hashmap for transform dict, for type : " + dir2);
                e.printStackTrace();
            }
        }
    }
    public enum transformOptions{
        doNothing, wordToWord, API, transliterate, alpToJap
        //wordToWord : translate using word to word dictionary, ignores grammar
        //alpToJap : transform alphabet input already in japanese to japanese characters : eg. petto no neko -> ペットの猫 (= a pet cat)
        //transliterate : similar to alpToJap, but output will all be Katakana form
    }

    public void messageIngame(String str, String colorName) {
        String colorHex = Colors.fromName(colorName).getHex();
        String enWithColors = "<col=" + colorHex + ">" + str;
        ChatIconManager iconManager = japanesePlugin.getChatIconManager();
        HashMap<String, Integer> map = japanesePlugin.getJapCharIds();
        String msg =  getTransformWithColors(enWithColors, transformOptions.wordToWord, map, iconManager);
        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", msg, null);
    }
    //returns concat of "img<color--##.png>"
    //input String enWithColors example : <col=ffffff>Mama Layla<col=ffff00>  (level-3)
    public String getTransformWithColors(String enWithColors, transformOptions transOpt,
                                         HashMap<String, Integer> hashMap, ChatIconManager chatIconManager) {
        return getTWCchild(enWithColors, transOpt,hashMap, chatIconManager, null);
    }
    public String getTransformWithColors(String enWithColors, transformOptions transOpt,
                                         HashMap<String, Integer> hashMap, ChatIconManager chatIconManager, HashMap<String,String> map) {
        return getTWCchild(enWithColors, transOpt,hashMap, chatIconManager, map);
    }
    private String getTWCchild(String enWithColors, transformOptions transOpt,
                               HashMap<String, Integer> hashMap, ChatIconManager chatIconManager,
                               HashMap<String,String> specifiedMap){
        if (enWithColors.contains("<img=")){//ignore if its already in japanese
            return enWithColors;
        }
        String[][] colorWords = getColorWordArray(enWithColors, transOpt, specifiedMap);// = {{"ffffff","White string"},{"ff0000","red"},...}
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
                    String imgName2 = colorWords[i][0] + "--" + "?".codePointAt(0) + ".png";
                    hash = hashMap.getOrDefault(imgName2, -99);
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
    private String[][] getColorWordArray(String enWithColors, transformOptions transOpt, HashMap<String,String> specifiedMap) {
        int colorTagNum = enWithColors.split(">").length - 1;
        String[][] colorWords;// = {{"ffffff","White string"},{"ff0000","red"},...}
        if (colorTagNum == 0) {
            colorWords = new String[1][2];
            Colors white = Colors.white;
            colorWords[0][0] = white.getName();
            colorWords[0][1] = transform(enWithColors.trim(), transOpt,specifiedMap);
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
                colorWords[i][1] = transform(wordArray[i], transOpt, specifiedMap);
                ////log.info("colorWords[" + i + "][0]" + colorWords[i][0] );
                ////log.info("colorWords[" + i + "][1]" + colorWords[i][1] );
            }

        }
        return colorWords;
    }

    private String[] getColorArray(String wordAndColor) {//turns color tags into color names like "white" for all tags, return as list
        String[] parts = wordAndColor.split("<col=");
        String[] colorArray = new String[parts.length - 1];

        for (int i = 0; i < parts.length - 1; i++) {
            Colors c = Colors.fromHex(parts[i+1].split(">")[0]);
            colorArray[i] = c.getName();
            if (colorArray[i] == null || Objects.equals(colorArray[i], "")){
                colorArray[i] = Colors.red.getName();
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

    private String transform(String enString, transformOptions transOpt, HashMap<String,String> specifiedMap) {
        String enStringLower = enString.toLowerCase();
        if(specifiedMap != null) {
            if (specifiedMap.containsKey(enStringLower)) {
                return specifiedMap.get(enStringLower);
            }
        }
        switch(transOpt) {
            case doNothing:
                return enString;
            case wordToWord:
                //log.info("option = " + transOpt);
                //log.info("enword = " + enStringLower);
                return getW2WTranslation(enStringLower);
            case API:
                return enString;
            case transliterate:
                return enString;
            case alpToJap:
                romToJap = japanesePlugin.getRomToJap();
                return romToJap.romJpTransform(enStringLower, false);
            default:
                return enString;
        }
    }

    private String getW2WTranslation(String en) {
        //first, search if the whole sentence has been translated before
        //log.info("transforming : " + en);
        en = en.trim();
        if (!knownDirect.isEmpty()) {
            if (knownDirect.containsKey(en)) {
                return knownDirect.get(en);
            }
        }
        if (menuOptionTran.containsKey(en)) {
            //log.info("found " + en + " in menuOptioinTran");
            return menuOptionTran.get(en);
            //log.info("returning result : " + result);
        } else if (itemNpcTran.containsKey((en))) {
            //log.info("found in ItemNpcTran");
            return itemNpcTran.get(en);
        } else  if (knownDeepL.containsKey(en)) {
            //log.info("found in knownDeepl");
            return knownDeepL.get(en);
        } else {//if not translated before,
            //1. use api translator on the whole sentence if enabled
              //todo
            //2.if api translation not enabled, split up into words and translate each of them and concat them
            String[] wordArray = en.split("[ ,.;:!?]+");
            StringBuilder resultBuilder = new StringBuilder();
            for (String word : wordArray) {
                word = word.trim();
                if (word.matches("(\\d)")){//if its only a number, add the number and continue
                    resultBuilder.append(word);
                    continue;
                }
                if (directWord.containsKey(word)) {
                    //log.info("found in directWord");
                    resultBuilder.append(directWord.get(word));
                } else {//remove s from plural then search for direct translation because they dont exist in "DirectWordTranslations"
                    if (word.endsWith("s")){
                        String enNoS = word.substring(0,word.length()-1);
                        if (knownDirect.containsKey(enNoS)) {
                            resultBuilder.append(knownDirect.get(enNoS));
                        } else if (directWord.containsKey(enNoS)){
                            resultBuilder.append(directWord.get(enNoS));
                        } else {
                            //log.info("couldnt find translation of " + word + ", transliterating");
                            resultBuilder.append(transliterte(word));
                        }
                    }else{
                        if (knownDirect.containsKey(word)) {
                            resultBuilder.append(knownDirect.get(word));
                        } else if (directWord.containsKey(word)){
                            resultBuilder.append(directWord.get(word));
                        } else {
                            //log.info("couldnt find translation of " + word + ", transliterating");
                            resultBuilder.append(transliterte(word));
                        }
//                        log.info("couldnt find translation of " + word + ", transliterating");
//                        resultBuilder.append(transliterte(word));
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
            if(ch.matches("[^\\p{L}\\p{N}\\s]"))
                katakana.append(ch);
             else
                katakana.append(transliterationMap.getOrDefault(ch, ch));
        }
        return katakana.toString();
    }
}