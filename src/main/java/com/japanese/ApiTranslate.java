package com.japanese;

import com.deepl.api.*;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.io.Writer;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

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
    public long googleCount = 0;
    public long googleLimit = 500000;
    public long azureCount = 0;
    public long azureLimit = 500000;
    Translator deeplTranslator;
    public void apiCountInit() throws Exception {
        String authKey = config.DeeplApiKey();
        deeplTranslator = new Translator(authKey);

        Usage usage = deeplTranslator.getUsage();
        if (usage.anyLimitReached()) {
            System.out.println("Translation limit reached.");
            deeplCount = deeplLimit;
        }
        if (usage.getCharacter() != null) {
            deeplCount = usage.getCharacter().getCount();
            deeplLimit = usage.getCharacter().getLimit();
            System.out.printf("Character usage: %d of %d%n",
                    deeplCount,
                    deeplLimit);
        }
        //deeplCount = 1;
    }
    public enum apiType{
        deepL, Google, Azure
    }

    public String getDeepl(String enText, String source, String target) throws Exception {
        if (!japanesePlugin.getJapTransforms().knownAPI.containsKey(enText.toLowerCase())) {
            log.info(enText + "\nNot in KnownDeepl dict");

//            String authKey = config.DeeplApiKey();
//            deeplTranslator = new Translator(authKey);

            TextResult result = deeplTranslator.translateText(enText, source, target);


            String resultText = result.getText();
            System.out.println(resultText);

            updateCount(apiType.deepL);
            updateKnown(enText, resultText);

            return resultText;
        } else {
            log.info(enText + "\nalready in KnownDeepl dict");
            log.info("deepL Count = " + Long.toString(deeplCount));
            return japanesePlugin.getJapTransforms().knownAPI.getOrDefault(enText.toLowerCase(), enText);
        }
    }
    private void updateCount(apiType type) throws DeepLException, InterruptedException {
        if (type == apiType.deepL){
            Usage usage = deeplTranslator.getUsage();
            if (usage.getCharacter() != null) {
                deeplCount = usage.getCharacter().getCount();
                deeplLimit = usage.getCharacter().getLimit();
                log.info("updated deepl count:" + deeplCount+"\nupdated deepl limit" + deeplLimit);
            }
        }
    }
    private void updateKnown(String en, String jp) throws IOException{
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
}
