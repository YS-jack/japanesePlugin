package com.japanese;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.MessageNode;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.ComponentID;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.runelite.api.Client;

import javax.inject.Inject;
import com.japanese.JapTransforms.transformOptions;
import net.runelite.api.widgets.WidgetTextAlignment;
import net.runelite.client.game.ChatIconManager;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class JapWidgets {
    //for every widget with no child, if it has widget, translate and replace it
    private List<Widget> widgetWithText;
    @Inject
    @Setter
    private JapTransforms japTransforms = new JapTransforms();

    @Inject
    Client client;
    @Inject
    JapanesePlugin japanesePlugin;
    private int count = 0; //count how many widgets there are, for testing
    private List<String> stringTranslatingInThread = new ArrayList<>();
    public List<Widget> dialogOptionWidgets = new ArrayList<>();
    public boolean displayDialog = false;
    private int dialogDisplayCount = 0;
    public void changeWidgetTexts(Widget widgetExceptions) throws Exception { //widgetExceptions = parent widgets to ignore searching for texts
        dialogDisplayCount = 0;
        int localDialogCount = dialogDisplayCount;
        Widget[] roots = client.getWidgetRoots();
        for (Widget root : roots) {
            changeEndChildTextAndRecord(root);
        }
        displayDialog = dialogDisplayCount != localDialogCount;
        //log.info("displayDialog=" + displayDialog);
    }
    private void changeEndChildTextAndRecord(Widget widget) throws Exception {//for collecting ids, only for development
        if(!widget.isHidden()
                && widget.getId() != ComponentID.CHATBOX_MESSAGE_LINES
                //&& widget.getId() != ComponentID.CHATBOX_FRAME
        ){//
            if (widget.getId() == ComponentID.CHATBOX_INPUT) {
                japanesePlugin.getRomToJap().drawOverlay(widget);
                return;
            }

            if (widget.getId() == ComponentID.DIALOG_OPTION_OPTIONS){//dialog options are shown via overlay, so quest helper selection is visible
                //log.info("found dialog option:" + widget.getParentId());
                Widget[] dialogOptions = widget.getDynamicChildren();
                dialogDisplayCount++;
                displayDialog = true;
                if (!dialogOptionWidgets.equals(Arrays.asList(dialogOptions))) {
                    dialogOptionWidgets.clear();
                    dialogOptionWidgets.addAll(Arrays.asList(dialogOptions));
                }
                return;
            }
            Widget[] dynamicChildren = widget.getDynamicChildren();
            Widget[] nestedChildren = widget.getNestedChildren();
            Widget[] staticChildren = widget.getStaticChildren();
            for (Widget dynamicChild : dynamicChildren) {changeEndChildTextAndRecord(dynamicChild);}
            for (Widget nestedChild : nestedChildren) {changeEndChildTextAndRecord(nestedChild);}
            for (Widget staticChild : staticChildren) {changeEndChildTextAndRecord(staticChild);}


            String widgetText = widget.getText();
            if (widgetText != null) {
                if (widget.getId() == ComponentID.CHATBOX_REPORT_TEXT
                        ||getWidgetTransformConfig(widget) == transformOptions.doNothing
                        ||(client.getLocalPlayer().getName()!=null && widget.getText().equals(client.getLocalPlayer().getName()))//if its player name then leave it
                )
                    return;

                if (!widgetText.isEmpty() && !widgetText.isBlank() && !widgetText.contains("<img=")) {//if widgetText contains text
//                    if (widget.getParent().getId() == 14024705 || widget.getParent().getId() == 14024714) { //parent of skill guide, or parent of element in list
//                        String dir = "src/main/resources/com/japanese/dump/";
//                        if (widget.getText().matches("\\d{1,2}"))
//                            return;
//                        writeToFile(widgetText + "|", dir + "skillGuideDump.txt");
//                    }
                    //check for specific widget
                    if (widgetText.toLowerCase().contains("hail, group iron man!"))
                        log.info("found hail, group iron man!");
                    //log.info(widgetText);
                    if (getGrandNParent(widget,4) != null) {
                        if (getGrandNParent(widget, 4).getId() == ComponentID.SETTINGS_INIT) {
                            if (widget.getText().matches("F\\d{1,2}") || widget.getText().equals("ESC"))
                                return;
                        } else{
                            Widget g6Parent = getGrandNParent(widget,6);
                            if (g6Parent != null) {
//                                if (g6Parent.getId() == ComponentID.SETTINGS_INIT) {
//                                    ///for dumping texts for translation ease
//                                    String dir = "src/main/resources/com/japanese/dump/";
//                                    writeToFile(widgetText + "|", dir + "settingsDump");
//                                    if (widget.getText().matches("F\\d{1,2}") || widget.getText().equals("ESC"))
//                                        return;
//                                }
                            }
                        }
                    }
                    String translatedTextWithColors;
                    if (widgetText.matches("^[0-9,%.]*$"))//if its only numbers
                        return;
                    if (widgetText.contains("<")) {
                        Widget grandParent = getGrandNParent(widget,2);
                        if (grandParent != null) {
                            int grandParentId = grandParent.getId();
                            if (grandParentId == ComponentID.SKILLS_CONTAINER) {
                                String hex;
                                if (!widgetText.contains("<br>")) {//mouse hover of member skill in f2p world
                                    hex = Colors.red.getHex();
                                    widget.setTextColor(Colors.hexToInt(hex));
                                    int yPos = widget.getRelativeY();
                                    widget.setRelativeY(yPos + 3);
                                } else if (!widgetText.contains("Total level")) {
                                    hex = Colors.blue.getHex();
                                    widget.setTextColor(Colors.hexToInt(hex));
                                    if (!containsNumber(widgetText)) {
                                        int yPos = widget.getRelativeY();
                                        widget.setRelativeY(yPos + 3);
                                    }
                                }
                                boolean setbr = false;
                                changeWidgetTextsWithBr(widget, setbr);
//                                translatedTextWithColors = changeWidgetTextsWithBr(widget, setbr);//returns <img> with <br>
                                //widget.setText(translatedTextWithColors);
                                return;
                            }
                            if (grandParentId == ComponentID.CHATBOX_BUTTONS) {
                                if (widgetText.startsWith("<br>")) {
                                    widget.setXTextAlignment(WidgetTextAlignment.RIGHT);
                                } else {
                                    widget.setXTextAlignment(WidgetTextAlignment.LEFT);
                                }
                                widget.setText(removeTag(widgetText));
                                translatedTextWithColors = getImageText(widget);
                                widget.setText(translatedTextWithColors);
                                return;
                            }
                        }
                    }

                    //first argument of getTransformWithColors example : <col=ffffff>Mama Layla<col=ffff00>  (level-3)
                    //widget.setText(removeTag(widgetText));
                    boolean setbr = true;
                    changeWidgetTextsWithBr(widget, setbr);
//                    translatedTextWithColors = changeWidgetTextsWithBr(widget);
//                    widget.setText(translatedTextWithColors);
//
//                    insertBrAfterTransform(widget);
                    //setNiceWidgetHeight(widget);
                }
            }
        }
    }
    private void insertBrAfterTransform(Widget widget){
        int width = widget.getWidth();
        String str = widget.getText();
        if (width <= 0 )
            return;
        int charPerLine = width/14;
        if (charPerLine <= 0)
            return;
        StringBuilder stringBuilder = new StringBuilder();
        String[] imgArray = extractImg(str);
        if (imgArray.length > 2) {
            for (int i = 0; i < imgArray.length; i++) {
                stringBuilder.append(imgArray[i]);
                if ((i + 1) % charPerLine == 0 && i + 1 < widget.getText().length()) {
                    stringBuilder.append("<br>");
                }
            }
            String newTxt = stringBuilder.toString();
            widget.setText(newTxt);
            //set good size for widget
        }
    }
    private void setNiceWidgetHeight(Widget widget){
        int lineNum = countOccurrences(widget.getText(),"<br>") + 1;
        int lineHeight = lineNum * 17;
        if (widget.getOriginalHeight() < lineHeight) {
            widget.setOriginalHeight(lineHeight);
//        widget.getParent().setOriginalHeight(lineHeight);
//        for (Widget w : widget.getParent().getDynamicChildren())
//            w.setOriginalHeight(lineHeight);

        }
        //widget.revalidate();
    }
    private int countOccurrences(String str, String findStr) {
        int count = 0;
        int index = 0;
        while ((index = str.indexOf(findStr, index)) != -1) {
            count++;
            index += findStr.length(); // Move to the end of the current occurrence to find the next one
        }
        return count;
    }
    public static boolean containsWidget(Widget[] widgetArray, Widget widget) {
        for (Widget element : widgetArray) {
            if (element.equals(widget)) {
                return true;
            }
        }
        return false;
    }
    private String[] extractImg(String input) {
        List<String> images = new ArrayList<>();
        Pattern pattern = Pattern.compile("(<.*?>)");
        Matcher matcher = pattern.matcher(input);
        while(matcher.find()){
            images.add(matcher.group(1));
        }
        return images.toArray(new String[0]);
    }
    private String getImageText (Widget widget) throws Exception {//automatically inserts <br> regarding the width of widget
        String colorHex = getColorHex(widget);
        String str = widget.getText().trim();
        String translatedTextWithColors;
        String enWithColors = "<col=" + colorHex + ">" + str;
        ChatIconManager iconManager = japanesePlugin.getChatIconManager();
        HashMap<String, Integer> map = japanesePlugin.getJapCharIds();
        transformOptions option = getWidgetTransformConfig(widget);

        if (option == transformOptions.API &&
                !japanesePlugin.getJapTransforms().knownAPI.containsKey(str.toLowerCase()) ) {
            if(!stringTranslatingInThread.contains(widget.getText())) {
                stringTranslatingInThread.add(widget.getText());
                Thread thread = new Thread(() -> {
                    try {
                        if (removeTag(widget.getText()).isEmpty())
                            return;
                        String ret = japanesePlugin.getJapTransforms().getTransformWithColors(enWithColors, option, map, iconManager);
                        //String withBr = insertBr(ret, chatMessage);
                        widget.setText(ret);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

                thread.setDaemon(false);
                thread.start();
            }
            return widget.getText();
        }
        else {
            translatedTextWithColors = japTransforms.getTransformWithColors(enWithColors, option, map, iconManager);
            return translatedTextWithColors;
        }
    }
    private void changeWidgetTextsWithBr(Widget widget, boolean setbr) throws Exception {//if widget contains multiple lines, breaks line of output as well
        transformOptions option = getWidgetTransformConfig(widget);
        String textBr2Space = widget.getText().replace("<br>"," ").trim();

        if (option == transformOptions.API && !japanesePlugin.getJapTransforms().knownAPI.containsKey(textBr2Space)) {

            if(!stringTranslatingInThread.contains(textBr2Space)) {
                stringTranslatingInThread.add(textBr2Space);
                Thread thread = new Thread(() -> {//process with new thread because games freezes while waiting for api response
                    try {
                        if (removeTag(widget.getText()).isEmpty())
                            return;
                        if(widget.getText().toLowerCase().contains("hail"))
                            log.info("found hail gim");
                        String stringToShow = getNewTextWithBr(widget, option);
                        widget.setText(stringToShow);
                        insertBrAfterTransform(widget);
                        stringTranslatingInThread.remove(textBr2Space);

                        //widget.setHidden(false);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                thread.setDaemon(false);
                thread.start();

                //widget.setHidden(true);
            }
            return;
        }
        String translatedTextWithColors = getNewTextWithBr(widget, option);
        widget.setText(translatedTextWithColors);
        insertBrAfterTransform(widget);
    }

    private String getNewTextWithBr(Widget widget, transformOptions option) throws Exception {
        String textSpaceNotBr = widget.getText().replace("<br>"," ").trim();
        textSpaceNotBr = removeTag(textSpaceNotBr);
        String colorHex = getColorHex(widget);
        if (textSpaceNotBr.toLowerCase().contains("hail, group iron man!"))
            log.info("found the dialog");
//        StringBuilder imgBuild = new StringBuilder();
//        int lastStringLen = 0;
        ChatIconManager iconManager = japanesePlugin.getChatIconManager();
        HashMap<String, Integer> map = japanesePlugin.getJapCharIds();
//        for (int i = 0; i<textBrSeparated.length; i++) {
//            String text = textBrSeparated[i];
//        if (textSpaceNotBr.isEmpty() || textSpaceNotBr.matches("<col=(\\d)>")) {
//            imgBuild.append("<br>");
//            lastStringLen = 0;
//        } else {
//            if(lastStringLen >0){
//                imgBuild.append("<br>");
//            }

//            textSpaceNotBr = removeTag(text);
//            lastStringLen = text.length();
//            if (textSpaceNotBr.matches("^[0-9,]+$")) {
//                //log.info("only contains int and commas");
//                imgBuild.append(textSpaceNotBr);
//                continue;
//            }
        String enWithColors = "<col=" + colorHex + ">" + textSpaceNotBr;
        //log.info("enWithColors = " + enWithColors);
//        String w;
        if (option != transformOptions.API) {
            Widget grandParent = getGrandNParent(widget, 4);
            if (grandParent != null) {//for setting
                if (grandParent.getId() == ComponentID.SETTINGS_INIT) {
                    HashMap<String, String> settingHash = japTransforms.knownSettingTranslation;
//                    w = japTransforms.getTransformWithColors(enWithColors, option, map, iconManager, settingHash);
//                    imgBuild.append(w);
//                        continue;
                    return japTransforms.getTransformWithColors(enWithColors, option, map, iconManager, settingHash);
                }
            }
            grandParent = getGrandNParent(widget, 2);
            if (grandParent != null) {//for chat buttons
                if (grandParent.getId() == ComponentID.CHATBOX_BUTTONS || grandParent.getId() == ComponentID.SKILLS_CONTAINER) {
                    HashMap<String, String> settingHash = japTransforms.knownChatButtonSkillTranslation;
//                    w = japTransforms.getTransformWithColors(enWithColors, option, map, iconManager, settingHash);
//                    imgBuild.append(w);
//                        continue;
                    return japTransforms.getTransformWithColors(enWithColors, option, map, iconManager, settingHash);
                }
            }
            //add other screen if needed
        }

            //for non setting/chat widgets
//            w = japTransforms.getTransformWithColors(enWithColors, option, map, iconManager);

        return japTransforms.getTransformWithColors(enWithColors, option, map, iconManager);
//            imgBuild.append(w);
////        }
////        }
//        return imgBuild.toString();
    }
    String getColorHex(Widget widget) {
        int widgetColor = widget.getTextColor();
        String colorHex;
        if (widget.getText().isBlank())
            colorHex = Colors.white.getHex();
        else
            colorHex = Colors.IntToHex(widgetColor);
        return colorHex;
    }
    public String removeTag(String str) {
        return str.replaceAll("<[^>]*>","").
                replace("<","").replace(">","");
    }

    public transformOptions getWidgetTransformConfig(Widget widget) throws Exception {

        Widget g5Parent = getGrandNParent(widget, 5);
        if (g5Parent != null) {//for chat dialogs
            if (g5Parent.getId() == ComponentID.CHATBOX_MESSAGES || widget.getParentId() == ComponentID.DIALOG_OPTION_OPTIONS) {
                if (japanesePlugin.config.npcDialogueConfig() == JapaneseConfig.GameTextProcessChoice.簡易翻訳)
                    return transformOptions.wordToWord;
                if (japanesePlugin.config.npcDialogueConfig() == JapaneseConfig.GameTextProcessChoice.そのまま)
                    return transformOptions.doNothing;
                if (japanesePlugin.config.npcDialogueConfig() == JapaneseConfig.GameTextProcessChoice.DeepL翻訳) {
                    if (japanesePlugin.getApiTranslate().deeplCount < japanesePlugin.getApiTranslate().deeplLimit-500)
                        return transformOptions.API;
                    else {
                        if (japanesePlugin.config.translatorOption() == JapaneseConfig.TranslatorConfig.簡易翻訳)
                            return transformOptions.wordToWord;
                        else
                            return transformOptions.doNothing;
                    }
                } else {
                    japTransforms.messageIngame("開発者に報告してください：JapWidgets getWidgetTransformConfig エラー", "red");
                    return transformOptions.wordToWord;
                }
            }
        } // for every other widgets, such as interfaces, buttons, etc
        if (japanesePlugin.config.widgetTextConfig() == JapaneseConfig.jpEnChoice.日本語)
            return transformOptions.wordToWord;

        if (japanesePlugin.config.widgetTextConfig() == JapaneseConfig.jpEnChoice.英語)
            return transformOptions.doNothing;

//        if (japanesePlugin.config.widgetTextConfig() == JapaneseConfig.GameTextProcessChoice.DeepL翻訳) {
//            if (japanesePlugin.getApiTranslate().deeplCount < japanesePlugin.getApiTranslate().deeplLimit-500)
//                return transformOptions.API;
//            else {
//                if (japanesePlugin.config.translatorOption() == JapaneseConfig.TranslatorConfig.簡易翻訳)
//                    return transformOptions.wordToWord;
//                else
//                    return transformOptions.doNothing;
//            }
//        }
        else {
            japTransforms.messageIngame("開発者に報告してください：JapWidgets getWidgetTransformConfig エラー", "red");
            return transformOptions.wordToWord;
        }
    }
    public static boolean containsNumber(String s) {
        Pattern p = Pattern.compile(".*\\d.*");
        Matcher m = p.matcher(s);
        return m.matches();
    }
    private void writeToFile(String line, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                writer.write(line);
                writer.newLine(); // Writes a new line
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private Widget getGrandNParent(Widget widget, int n) {
        for (int i = 0; i < n; i++) {
            if (widget.getParent() != null) {
                widget = widget.getParent();
            }else{
                return null;
            }
        }
        return widget;
    }
}
