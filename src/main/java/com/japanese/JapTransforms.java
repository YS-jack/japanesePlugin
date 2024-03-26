package com.japanese;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.client.game.ChatIconManager;

import javax.inject.Inject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
public class JapTransforms {
    public HashMap<String, String> knownAPI;
    public HashMap<String, String> knownDirect;
    public HashMap<String, String> directWord;
    public HashMap<String, String> knownMenuOption;
    public HashMap<String, String> knownItemAndWidgets;
    public HashMap<String, String> knownNpc;
    public HashMap<String, String> knownObject;
    public HashMap<String, String> transliterationMap;
    public HashMap<String, String> knownSettingTranslation;
    public HashMap<String, String> knownChatButtonSkillTranslation;
    public HashMap<String, String> knownPlayerNames;
    private List<String> sentMenuOption = new ArrayList<>();
    private List<String> sentNpcName = new ArrayList<>();
    private List<String> sentItemAndWidgetsName = new ArrayList<>();
    private List<String> sentObjectName = new ArrayList<>();
    public List<String> sentApiTranslate = new ArrayList<>();

    public DiscordWebhook webhook;
    @Inject
    private Client client;

    @Inject
    JapanesePlugin japanesePlugin;
    @Inject
    private RomToJap romToJap;
    @Inject
    private ApiTranslate apiTranslate;

    public void initTransHash() throws Exception {
        String transDataDir = "src/main/resources/com/japanese/translations/";
        knownAPI = new HashMap<>();
        putToDictHash(knownAPI, transDataDir, "KnownAPITranslations.csv");
        knownDirect = new HashMap<>();
        putToDictHash(knownDirect, transDataDir , "KnownDirectTranslations.csv","SkillTranslations.csv");
        directWord = new HashMap<>();
        putToDictHash(directWord, transDataDir , "BigTranslationDict.txt");

        knownMenuOption = new HashMap<>();
        putToDictHash(knownMenuOption, transDataDir,"KnownMenuOptionsDirect.csv");
        knownItemAndWidgets = new HashMap<>();
        putToDictHash(knownItemAndWidgets, transDataDir ,"KnownItemAndWidgets.csv");
        knownNpc = new HashMap<>();
        putToDictHash(knownNpc, transDataDir ,"KnownNpc.csv");
        knownObject = new HashMap<>();
        putToDictHash(knownObject, transDataDir ,"KnownObject.csv");

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

        String sentWebhookDir = "src/main/resources/com/japanese/webhookSent/";

        putSentToList(sentApiTranslate, sentWebhookDir, "sentAPITranslationMsg.txt");
        putSentToList(sentItemAndWidgetsName, sentWebhookDir,"sentItemAndWidgetsName.txt");
        putSentToList(sentMenuOption,sentWebhookDir,"sentMenuOptions.txt");
        putSentToList(sentNpcName, sentWebhookDir, "sentNpcName.txt");
        putSentToList(sentObjectName, sentWebhookDir, "sentObjName.txt");

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
    private void putSentToList(List<String> list, String dirName, String... dirArray) {
        for (String dir:dirArray) {
            String dir2 = dirName + dir;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(dir2), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    list.add(line.toLowerCase());
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

    public void messageIngame(String str, String colorName) throws Exception {
        String colorHex = Colors.fromName(colorName).getHex();
        String enWithColors = "<col=" + colorHex + ">" + str;
        ChatIconManager iconManager = japanesePlugin.getChatIconManager();
        HashMap<String, Integer> map = japanesePlugin.getJapCharIds();
        String msg;
        String japaneseCharacters = "[\\p{IsHiragana}\\p{IsKatakana}]";
        if (enWithColors.matches(".*" + japaneseCharacters + ".*"))//error when passing japanese string
            msg =  getTransformWithColors(enWithColors, transformOptions.doNothing, map, iconManager);
        else
            msg =  getTransformWithColors(enWithColors, transformOptions.wordToWord, map, iconManager);
        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", msg, null);
    }
    //returns concat of "img<color--##.png>"
    //input String enWithColors example : <col=ffffff>Mama Layla<col=ffff00>  (level-3)
    //<col=ff9040>Amulet of glory(3)</col><col=ffffff> -> <col=ff9040>Stardust</col>
    public String getTransformWithColors(String enWithColors, transformOptions transOpt,
                                         HashMap<String, Integer> hashMap, ChatIconManager chatIconManager) throws Exception {
        return getTWCchild(enWithColors, transOpt,hashMap, chatIconManager, null, true);
    }
    public String getTransformWithColors(String enWithColors, transformOptions transOpt,
                                         HashMap<String, Integer> hashMap, ChatIconManager chatIconManager, boolean addApiDict) throws Exception {
        return getTWCchild(enWithColors, transOpt,hashMap, chatIconManager, null, addApiDict);
    }
    public String getTransformWithColors(String enWithColors, transformOptions transOpt,
                                         HashMap<String, Integer> hashMap, ChatIconManager chatIconManager, HashMap<String,String> map) throws Exception {
        return getTWCchild(enWithColors, transOpt,hashMap, chatIconManager, map, true);
    }
    private String getTWCchild(String enWithColors, transformOptions transOpt,
                               HashMap<String, Integer> hashMap, ChatIconManager chatIconManager,
                               HashMap<String,String> specifiedMap, boolean addApiDict) throws Exception {
        if (enWithColors.contains("<img=")){//ignore if its already in japanese
            return enWithColors;
        }
        String[][] colorWords = getColorWordArray(enWithColors, transOpt, specifiedMap, addApiDict);// = {{"ffffff","White string"},{"ff0000","red"},...}


        //"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"
        ////log.info("\n");
        return buildJapStringImage(colorWords, hashMap,chatIconManager);
    }
    public String buildJapStringImage(String[][] colorWords, HashMap<String, Integer> hashMap, ChatIconManager chatIconManager) {
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
        return imgTagStrings.toString();
    }
    private String[][] getColorWordArray(String enWithColors, transformOptions transOpt, HashMap<String,String> specifiedMap, boolean addApiDict) throws Exception {
        enWithColors = enWithColors.replace("</col>","");
        Pattern re = Pattern.compile("(?<=\\d)>|(?<=\\p{IsAlphabetic})>");
        int colorTagNum = re.split(enWithColors).length - 1;
        String[][] colorWords;// = {{"ffffff","White string"},{"ff0000","red"},...}
        if (colorTagNum == 0) {
            colorWords = new String[1][2];
            Colors white = Colors.white;
            colorWords[0][0] = white.getName();
            colorWords[0][1] = transform(enWithColors.trim(), transOpt,specifiedMap, addApiDict);
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
                colorWords[i][1] = transform(wordArray[i], transOpt, specifiedMap, addApiDict);
                ////log.info("colorWords[" + i + "][0]" + colorWords[i][0] );
                ////log.info("colorWords[" + i + "][1]" + colorWords[i][1] );
            }

        }
        return colorWords;
    }

    private String[] getColorArray(String wordAndColor) {//turns color tags into color names like "white" for all tags, return as list
        String[] parts = wordAndColor.split("<col=");
        String[] colorArray = new String[parts.length - 1];
        Pattern re = Pattern.compile("(?<=\\d)>|(?<=\\p{IsAlphabetic})>");
//        int colorTagNum = re.split(enWithColors).length - 1;
        for (int i = 0; i < parts.length - 1; i++) {
            //Colors c = Colors.fromHex(parts[i+1].split(">")[0]);
            Colors c = Colors.fromHex(re.split(parts[i+1])[0]);
            colorArray[i] = c.getName();
            if (colorArray[i] == null || Objects.equals(colorArray[i], "")){
                colorArray[i] = Colors.red.getName();
            }
            ////log.info("added color : " + c.getName());
        }
        return colorArray;
    }
    private String[] getWordArray(String wordAndColor) {
        Pattern re = Pattern.compile("(?<=\\d)>|(?<=\\p{IsAlphabetic})>");
        String[] parts = re.split(wordAndColor);
//        String[] parts = wordAndColor.split(">");
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

    private String transform(String enString, transformOptions transOpt, HashMap<String,String> specifiedMap, boolean addApiDict) throws Exception {
        String enStringLower = enString.toLowerCase();
        String re = "^[^\\p{Alnum}]+$";
        if (enString.matches(re))
            return enString;

        if(specifiedMap != null) {
            if (specifiedMap.containsKey(enStringLower)) {
                return specifiedMap.get(enStringLower);
            } else {
                Thread thread = new Thread(() -> {
                    try {
                    reportUnknownToDisc(specifiedMap, enStringLower);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                thread.setDaemon(false);
                thread.start();
            }
        }

        switch(transOpt) {
            case API:
                if (japanesePlugin.getApiTranslate().deeplCount < japanesePlugin.getApiTranslate().deeplLimit - enString.length() - 5
                && japanesePlugin.getApiTranslate().keyValid
                && japanesePlugin.config.useDeepl())
                    return japanesePlugin.getApiTranslate().getDeepl(enString, "en", "ja", addApiDict, specifiedMap);
            case wordToWord:
                //log.info("option = " + transOpt);
                //log.info("enword = " + enStringLower);
                if (transOpt == transformOptions.wordToWord ||
                        (japanesePlugin.config.translatorOption() == JapaneseConfig.TranslatorConfig.簡易翻訳))
                    return getW2WTranslation(enStringLower);
            case doNothing:
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

    public String getW2WTranslation(String en) {
        //first, search if the whole sentence has been translated before
        //log.info("transforming : " + en);
        en = en.trim().toLowerCase();
        String knownTranslation = getKnownTranslation(en);
        if (knownTranslation != null)
            return knownTranslation;
        else {//if not translated before,
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
                            resultBuilder.append(word);
                        }
//                        log.info("couldnt find translation of " + word + ", transliterating");
//                        resultBuilder.append(transliterte(word));
                    }
                }
            }
            return resultBuilder.toString();
        }
    }

    public String getKnownTranslation(String en){
        en = en.toLowerCase();
        if (knownMenuOption.containsKey(en))
            return knownMenuOption.get(en);
        if (knownItemAndWidgets.containsKey(en))
            return knownItemAndWidgets.get(en);
        if (knownNpc.containsKey(en))
            return knownNpc.get(en);
        if (knownObject.containsKey(en))
            return knownObject.get(en);

        if (knownDirect.containsKey(en))
            return knownDirect.get(en);
        if (knownAPI.containsKey(en))
            return knownAPI.get(en);

        return null;
    }

    public String replaceAllKnown(String enPassed){
        if (enPassed.isEmpty()) return "";
        String[] enArray = enPassed.split("(?<=\\p{Punct})|(?=\\p{Punct})|\\s+");
        String en = enPassed.toLowerCase();
        for (int i = Math.min(5,en.length()); i > 0; i--) {
            for (int j = 0; j < en.length()-i+1; j++) {
                if (knownObject.containsKey(en.substring(j,j+i)))
                    return en.substring(0,j) + knownObject.get(en.substring(j,j+i)) + en.substring(j+i);
            }
        }
        return enPassed;
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
    private void reportUnknownToDisc(HashMap<String, String> map, String enString) throws IOException {
        String url = japanesePlugin.config.webHookUrl();
        if (url.isEmpty())
            return;
        String filePath = "src/main/resources/com/japanese/webhookSent/";
        if (map == japanesePlugin.getJapTransforms().knownMenuOption && !japanesePlugin.getJapTransforms().sentMenuOption.contains(enString)) {
            if (sendToWebhook("MenuOption|" + enString)) {
                sentMenuOption.add(enString);
                writeToFile(enString, filePath + "sentMenuOptions.txt");
            }
        } else if (map == japanesePlugin.getJapTransforms().knownItemAndWidgets && !japanesePlugin.getJapTransforms().sentItemAndWidgetsName.contains(enString)) {
            if (sendToWebhook("ItemAndWidgetsName|" + enString)) {
                sentItemAndWidgetsName.add(enString);
                writeToFile(enString, filePath + "sentItemAndWidgetsName.txt");
            }
        } else if (map == japanesePlugin.getJapTransforms().knownNpc && !japanesePlugin.getJapTransforms().sentNpcName.contains(enString)) {
            if (sendToWebhook("NpcName|" + enString)) {
                sentNpcName.add(enString);
                writeToFile(enString, filePath + "sentNpcName.txt");
            }
        } else if (map == japanesePlugin.getJapTransforms().knownObject && !japanesePlugin.getJapTransforms().sentObjectName.contains(enString)) {
            if (sendToWebhook("ObjName|" + enString)) {
                sentObjectName.add(enString);
                writeToFile(enString, filePath + "sentObjName.txt");
            }
        }
    }

    private boolean sendToWebhook(String content) {
        try {
            webhook.setContent(content);
            webhook.execute();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void writeToFile(String text, String filePath) throws IOException {
        //String filePath = "src/main/resources/com/japanese/translations/KnownAPITranslations.csv";
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filePath, true), StandardCharsets.UTF_8))) {
            writer.write(text + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}