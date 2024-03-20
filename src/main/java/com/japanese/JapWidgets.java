package com.japanese;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.ComponentID;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
    public void changeWidgetTexts(Widget widgetExceptions) throws Exception { //widgetExceptions = parent widgets to ignore searching for texts
        collectWidgetIdsWithText();//for collecting ids, only for development
    }

    private void collectWidgetIdsWithText() throws Exception {//for collecting ids, only for development
        Widget[] roots = client.getWidgetRoots();
        for (Widget root : roots) {
            changeEndChildTextAndRecord(root);
        }
    }
    private void changeEndChildTextAndRecord(Widget widget) throws Exception {//for collecting ids, only for development
        if(!widget.isHidden()&& widget.getId() != ComponentID.CHATBOX_MESSAGE_LINES) {//
            if (widget.getId() == ComponentID.CHATBOX_INPUT) {
                japanesePlugin.getRomToJap().drawOverlay(widget);
            }
            Widget[] dynamicChildren = widget.getDynamicChildren();
            Widget[] nestedChildren = widget.getNestedChildren();
            Widget[] staticChildren = widget.getStaticChildren();
            for (Widget dynamicChild : dynamicChildren) {changeEndChildTextAndRecord(dynamicChild);}
            for (Widget nestedChild : nestedChildren) {changeEndChildTextAndRecord(nestedChild);}
            for (Widget staticChild : staticChildren) {changeEndChildTextAndRecord(staticChild);}

            if (widget.getId() == ComponentID.CHATBOX_REPORT_TEXT)
                return;
            String widgetText = widget.getText();
            if (widgetText != null) {
                if (!widgetText.isEmpty() && !widgetText.isBlank() && !widgetText.contains("<img=")) {//if widgetText contains text
//                    if (widget.getParent().getId() == 14024705 || widget.getParent().getId() == 14024714) { //parent of skill guide, or parent of element in list
//                        String dir = "src/main/resources/com/japanese/dump/";
//                        if (widget.getText().matches("\\d{1,2}"))
//                            return;
//                        writeToFile(widgetText + "|", dir + "skillGuideDump.txt");
//                    }
                    //check for specific widget
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
                                translatedTextWithColors = changeWidgetTextsWithBr(widget);//returns <img> with <br>
                                widget.setText(translatedTextWithColors);
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
                    widget.setText(removeTag(widgetText));
                    translatedTextWithColors = changeWidgetTextsWithBr(widget);
                    widget.setText(translatedTextWithColors);

                    insertBrAfterTransform(widget);
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
        translatedTextWithColors = japTransforms.getTransformWithColors(enWithColors, option, map, iconManager);
        return translatedTextWithColors;
    }
    private String changeWidgetTextsWithBr(Widget widget) throws Exception {//if widget contains multiple lines, breaks line of output as well
        String[] textBrSeparated = widget.getText().trim().split("<br>");
        String colorHex = getColorHex(widget);
        StringBuilder imgBuild = new StringBuilder();
        int lastStringLen = 0;
        for (int i = 0; i<textBrSeparated.length; i++) {
            String text = textBrSeparated[i];
            if (text.isEmpty() || text.matches("<col=(\\d)>")) {
                imgBuild.append("<br>");
                lastStringLen = 0;
            } else {
                if(lastStringLen >0){
                    imgBuild.append("<br>");
                }
                text = removeTag(text);
                lastStringLen = text.length();
                if (text.matches("^[0-9,]+$")) {
                    //log.info("only contains int and commas");
                    imgBuild.append(text);
                    continue;
                }
                String enWithColors = "<col=" + colorHex + ">" + text;
                //log.info("enWithColors = " + enWithColors);
                ChatIconManager iconManager = japanesePlugin.getChatIconManager();
                HashMap<String, Integer> map = japanesePlugin.getJapCharIds();
                transformOptions option = getWidgetTransformConfig(widget);
                String w;
                //if texts inside settings
                Widget grandParent = getGrandNParent(widget,4);
                if (grandParent != null) {
                    if (grandParent.getId() == ComponentID.SETTINGS_INIT) {
                        HashMap<String, String> settingHash = japTransforms.knownSettingTranslation;
                        w = japTransforms.getTransformWithColors(enWithColors, option, map, iconManager, settingHash);
                        imgBuild.append(w);
                        continue;
                    }
                }
                grandParent = getGrandNParent(widget, 2);
                if (grandParent != null) {
                    if (grandParent.getId() == ComponentID.CHATBOX_BUTTONS || grandParent.getId() == ComponentID.SKILLS_CONTAINER) {
                        HashMap<String, String> settingHash = japTransforms.knownChatButtonSkillTranslation;
                        w = japTransforms.getTransformWithColors(enWithColors, option, map, iconManager, settingHash);
                        imgBuild.append(w);
                        continue;
                    }
                }
                w = japTransforms.getTransformWithColors(enWithColors, option, map, iconManager);
                imgBuild.append(w);
            }
        }
        return imgBuild.toString();
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

    private transformOptions getWidgetTransformConfig(Widget widget) throws Exception {

        Widget g5Parent = getGrandNParent(widget, 5);
        if (g5Parent != null) {
            if (g5Parent.getId() == ComponentID.CHATBOX_MESSAGES) {
                if (japanesePlugin.config.npcDialogueConfig() == JapaneseConfig.GameTextProcessChoice.簡易翻訳) {
                    return transformOptions.wordToWord;
                }
                if (japanesePlugin.config.npcDialogueConfig() == JapaneseConfig.GameTextProcessChoice.そのまま) {
                    return transformOptions.doNothing;
                }
                if (japanesePlugin.config.npcDialogueConfig() == JapaneseConfig.GameTextProcessChoice.API翻訳) {
                    return transformOptions.API;
                } else {
                    japTransforms.messageIngame("開発者に報告してください：JapWidgets getWidgetTransformConfig エラー", "red");
                    return transformOptions.wordToWord;
                }
            }
        }
        if (japanesePlugin.config.widgetTextConfig() == JapaneseConfig.GameTextProcessChoice.簡易翻訳) {
            return transformOptions.wordToWord;
        }
        if (japanesePlugin.config.widgetTextConfig() == JapaneseConfig.GameTextProcessChoice.そのまま) {
            return transformOptions.doNothing;
        }
        if (japanesePlugin.config.widgetTextConfig() == JapaneseConfig.GameTextProcessChoice.API翻訳) {
            return transformOptions.API;
        } else {
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
