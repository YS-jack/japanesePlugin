package com.japanese;

import com.deepl.api.*;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class ApiTranslate {
    @Inject
    private JapaneseConfig config;
    @Inject
    private JapTransforms japTransforms;
    @Inject
    private JapanesePlugin japanesePlugin;
    public long deeplCount = 0;
    public long deeplLimit = 500000;
    public boolean keyValid = false;
    private List<String> sentAPITranslation = new ArrayList<>();
    private
//    public long googleCount = 0;
//    public long googleLimit = 500000;
//    public long azureCount = 0;
//    public long azureLimit = 500000;
    Translator deeplTranslator;
    public void apiCountInit() throws Exception {
        if (!config.useDeepl())
            return;
        String authKey = config.DeeplApiKey();
        String japaneseCharacters = "[\\p{IsHiragana}\\p{IsKatakana}]";
        if (authKey.isEmpty() || authKey.isBlank() || authKey.matches(".*" + japaneseCharacters + ".*")){
            keyValid = false;
            return;
        }

        deeplTranslator = new Translator(authKey);
        Usage usage;

        try {
            usage = deeplTranslator.getUsage();
        } catch (Exception e) {
            log.info("error with DeepL aquiring authorization");
            keyValid = false;
            //japTransforms.messageIngame("DeepL開始時にエラーが発生しました。<br>APIキーが有効であることと上限に到達していないことを確認してください。", "red");
            return;
        }

        if (usage.anyLimitReached()) {
            System.out.println("Translation limit reached.");
            //japTransforms.messageIngame("DeepLの翻訳上限に達しました。設定から", "red");
            deeplCount = deeplLimit;
        }
        if (usage.getCharacter() != null) {
            keyValid = true;
            deeplCount = usage.getCharacter().getCount();
            deeplLimit = usage.getCharacter().getLimit();
            System.out.printf("Character usage: %d of %d%n",
                    deeplCount,
                    deeplLimit);
        }
        log.info("deeplCount = " + deeplCount);
    }
    public enum apiType{
        deepL, Google, Azure
    }

    public String getDeepl(String enText, String source, String target, boolean addApiDict, HashMap<String,String> map) throws Exception {
        if (source.isEmpty())
            source = "en";
        if (target.isEmpty())
            target = "ja";
        if (!japanesePlugin.getJapTransforms().knownAPI.containsKey(enText.toLowerCase())) {//if not in known api
            log.info(enText + "\nNot in KnownDeepl dict");
            TextResult result;
            try {
                result = deeplTranslator.translateText(enText, source, target);
            } catch (Exception e) {
                return enText;
            }
            String resultText = result.getText();
            System.out.println(resultText);

            updateCount(apiType.deepL, enText);
            updateKnown(enText, resultText, addApiDict);
            if (addApiDict)
                sendWebhook(enText, resultText, map);

            return resultText;
        } else {
            log.info(enText + "\nalready in KnownDeepl dict");
            log.info("deepL Count = " + Long.toString(deeplCount));
            return japanesePlugin.getJapTransforms().knownAPI.getOrDefault(enText.toLowerCase(), enText);
        }
    }
    private void updateCount(apiType type, String enText) throws DeepLException, InterruptedException {
        if (type == apiType.deepL){
            Usage usage = deeplTranslator.getUsage();
            if (usage.getCharacter() != null) {
                long oldCount = deeplCount;
                deeplCount = usage.getCharacter().getCount();
                deeplLimit = usage.getCharacter().getLimit();

                if (oldCount == deeplCount)
                    deeplCount += enText.length();
                log.info("updated deepl count:" + deeplCount+"\nupdated deepl limit" + deeplLimit);

            }
        }
    }
    private void updateKnown(String en, String jp, boolean addApiDict) throws IOException{
        if (addApiDict)
            writeToFile(en.toLowerCase(), jp);
        japanesePlugin.getJapTransforms().knownAPI.put(en.toLowerCase(), jp);
    }
    private void writeToFile(String en, String jp) throws IOException {
        String filePath = "src/main/resources/com/japanese/translations/KnownAPITranslations.csv";
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filePath, true), StandardCharsets.UTF_8))) {
            writer.write(en + "|" + jp + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void sendWebhook(String en, String jp, HashMap<String, String> map) throws IOException {
        if (japanesePlugin.getJapTransforms().sentApiTranslate.contains(en+"|"+jp))
            return;
        String type;
        if (map.equals(japanesePlugin.getJapTransforms().knownObject))
            type = "deepl_Object";
        else if (map.equals(japanesePlugin.getJapTransforms().knownNpc))
            type = "deepl_NPC";
        else if (map.equals(japanesePlugin.getJapTransforms().knownItemAndWidgets))
            type = "deepl_ItemAndWidgets";
        else if (map.equals(japanesePlugin.getJapTransforms().knownMenuOption))
            type = "deepl_MenuOption";
        else
            type = "deepl_gameMsgDialogOther";
        if (sendToWebhook(type + "|" + en + "|" + jp)) {
            writeToSentFile(en + "|" + jp, "src/main/resources/com/japanese/webhookSent/sentAPITranslationMsg.txt");
            japanesePlugin.getJapTransforms().sentApiTranslate.add(en+"|"+jp);
        }
    }

    private boolean sendToWebhook(String content) {
        DiscordWebhook webhook = japanesePlugin.getJapTransforms().webhook;
        try {
            webhook.setContent(content);
            webhook.execute();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void writeToSentFile(String text, String filePath) throws IOException {
        //String filePath = "src/main/resources/com/japanese/translations/KnownAPITranslations.csv";
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filePath, true), StandardCharsets.UTF_8))) {
            writer.write(text + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
