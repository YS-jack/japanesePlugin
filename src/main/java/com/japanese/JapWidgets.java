package com.japanese;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.InterfaceID;

import java.util.List;
import net.runelite.api.Client;
import java.util.Arrays;

import javax.inject.Inject;
import com.japanese.JapTransforms.transformOptions;
import com.japanese.JapanesePlugin;
import net.runelite.api.widgets.WidgetTextAlignment;
import net.runelite.client.game.ChatIconManager;

import java.util.HashMap;

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
    public void changeWidgetTexts(Widget widgetExceptions) { //widgetExceptions = parent widgets to ignore searching for texts
        collectWidgetIdsWithText();//for collecting ids, only for development
    }

    private void collectWidgetIdsWithText(){//for collecting ids, only for development
        Widget[] roots = client.getWidgetRoots();
        for (Widget root : roots) {
            changeEndChildTextAndRecord(root);
        }
    }
    private void changeEndChildTextAndRecord(Widget widget){//for collecting ids, only for development
        if(!widget.isHidden()) {
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
                    String translatedTextWithColors;
                    if (widgetText.matches("^[0-9,%.]*$"))//if its only numbers
                        return;
                    if (widgetText.contains("<")) {
                        int grandParentId = widget.getParent().getParent().getId();
                        if (grandParentId == ComponentID.SKILLS_CONTAINER){
                            String hex;
                            if (!widgetText.contains("<br>")){//mouse hover of member skill in f2p world
                                hex = Colors.red.getHex();
                                widget.setTextColor(Colors.hexToInt(hex));
                            } else {
                                hex = Colors.blue.getHex();
                                widget.setTextColor(Colors.hexToInt(hex));
                            }
                            translatedTextWithColors = changeWidgetTextsWithBr(widget);//returns <img> with <br>
                            widget.setText(translatedTextWithColors);
                            return;
                        }
                        if (grandParentId == ComponentID.CHATBOX_BUTTONS) {
                            if (widgetText.startsWith("<br>")){
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
                    //first argument of getTransformWithColors example : <col=ffffff>Mama Layla<col=ffff00>  (level-3)
                    translatedTextWithColors = changeWidgetTextsWithBr(widget);
                    widget.setText(translatedTextWithColors);
                }
            }
        }
    }
    private String getImageText (Widget widget) {
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
    private String changeWidgetTextsWithBr(Widget widget) {//if widget contains multiple lines, breaks line of output as well
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
                String w = japTransforms.getTransformWithColors(enWithColors, option, map, iconManager);
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
    private String removeTag(String str) {
        return str.replaceAll("<[^>]*>","").
                replace("<","").replace(">","");
    }

    private transformOptions getWidgetTransformConfig(Widget widget) {

        Widget g5Parent = widget.getParent().getParent().getParent().getParent().getParent();
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
        } else {
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
    }
}
