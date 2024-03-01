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
    @Setter
    private JapTransforms japTransforms;

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

            String widgetText = widget.getText();
            if (widgetText != null) {
                if (!widgetText.isEmpty() && !widgetText.isBlank() && !widgetText.contains("<img=")) {//if widgetText contains text
                    String translatedTextWithColors;
                    if (widgetText.matches("^[0-9,%.]*$"))//if its only numbers
                        return;
                    if (widgetText.contains("<")) {
                        int grandParentId = widget.getParent().getParent().getId();
                        if (grandParentId == ComponentID.SKILLS_CONTAINER){
                            String hex = Colors.blue.getHex();
                            widget.setTextColor(Colors.hexToInt(hex));
                            translatedTextWithColors = changeWidgetTextsWithBr(widget);//returns <img> with <br>
                            widget.setText(translatedTextWithColors);

                            return;
                        }
                        if (grandParentId == ComponentID.CHATBOX_BUTTONS) {
                            if (widgetText.startsWith("<br>")){
                                widget.setXTextAlignment(WidgetTextAlignment.RIGHT);
                            } else if(widgetText.endsWith("<br>")){
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
        String str = widget.getText();
        String translatedTextWithColors;
        String enWithColors = "<col=" + colorHex + ">" + str;
        ChatIconManager iconManager = japanesePlugin.getChatIconManager();
        HashMap<String, Integer> map = japanesePlugin.getJapCharIds();
        translatedTextWithColors = japTransforms.getTransformWithColors(enWithColors, transformOptions.wordToWord, map, iconManager);
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
                    log.info("only contains int and commas");
                    imgBuild.append(text);
                    continue;
                }

                String enWithColors = "<col=" + colorHex + ">" + text;
                log.info("enWithColors = " + enWithColors);
                ChatIconManager iconManager = japanesePlugin.getChatIconManager();
                HashMap<String, Integer> map = japanesePlugin.getJapCharIds();
                String w = japTransforms.getTransformWithColors(enWithColors, transformOptions.alpToJap, map, iconManager);
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

    private String getLastTag(String str) {//,ff00>,00ffff>Hide</col>
        if (str.contains("<col=")){
            String[] s = str.split("<col=");
            int colNumber = s.length - 1;
            return s[colNumber - 1].split(">")[0];
        }
        return "";
    }
}
