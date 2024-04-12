package com.japanese;

import com.sun.jna.platform.win32.COM.util.annotation.ComObject;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Varbits;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.ComponentID;

import java.awt.*;
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
    @Inject
    Varbits varbits;
    private int count = 0; //count how many widgets there are, for testing
    private List<String> stringTranslatingInThread = new ArrayList<>();
    public List<Widget> dialogOptionWidgets = new ArrayList<>();
    public boolean displayDialog = false;
    private int dialogDisplayCount = 0;
    private int accountManagementTabId = 7143424;
    private int gimTabId = 47251456;
    private int gimInGroupTabId = 47579136;//N 726.0
    private int groupingTabId = 47644672;
    private int groupingTabId2 = 4980736;
    private int settingTabId = 7602179;
    private int tradeScreenId = 21954562;
    private int showLastSearchGEId = 10616884;
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
                && widget.getId() != ComponentID.IGNORE_LIST_NAMES_CONTAINER
                && widget.getId() != ComponentID.CLAN_GUEST_LAYER
                && widget.getId() != ComponentID.CLAN_LAYER
                && widget.getId() != groupingTabId
                && widget.getId() != groupingTabId2
                && widget.getId() != settingTabId
                && widget.getId() != ComponentID.EMOTES_WINDOW
                && widget.getId() != ComponentID.MUSIC_CONTAINER
                && widget.getId() != tradeScreenId
                && widget.getId() != ComponentID.GRAND_EXCHANGE_WINDOW_CONTAINER
                && widget.getId() != ComponentID.CHATBOX_GE_SEARCH_RESULTS
                && widget.getId() != ComponentID.CHATBOX_FULL_INPUT
                && widget.getId() != showLastSearchGEId
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
//                        String dir = "/com/japanese/dump/";
//                        if (widget.getText().matches("\\d{1,2}"))
//                            return;
//                        writeToFile(widgetText + "|", dir + "skillGuideDump.txt");
//                    }
                    //check for specific widget

                    //log.info(widgetText);
                    if (getGrandNParent(widget,4) != null) {
                        if (getGrandNParent(widget, 4).getId() == ComponentID.SETTINGS_INIT) {
                            if (widget.getText().matches("F\\d{1,2}") || widget.getText().equals("ESC"))
                                return;
                        } else{
                            Widget g6Parent = getGrandNParent(widget,6);
                            if (g6Parent != null) {//for writing to file
//                                if (g6Parent.getId() == ComponentID.SETTINGS_INIT) {
//                                    ///for dumping texts for translation ease
//                                    String dir = "/com/japanese/dump/";
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
                    boolean setbr;
                    switch(widget.getId()) {
                        default:
                            setbr = true;
                            break;
                    }
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
        int enCharSize = 8;
        int jpCharSize = 14;
        if (width <= 0 )
            return;
        StringBuilder stringBuilder = new StringBuilder();
        String[] imgArray = str.split("(?=<img=[^>]+>)|(?<=<img=[^>]>)");//extractImg(str);
        if (imgArray.length > 2) {
            int currentWidth = 0;
            for (int i = 0; i < imgArray.length; i++) {
                stringBuilder.append(imgArray[i]);
                if(imgArray[i].startsWith("<img")) currentWidth += jpCharSize;
                else currentWidth += enCharSize;
                if (currentWidth + jpCharSize > width && i + 1 < widget.getText().length()) {
                    stringBuilder.append("<br>");
                    currentWidth = 0;
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
                        String stringToShow = getNewTextWithBr(widget, option, setbr);
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
            }
            return;
        }
        String translatedTextWithColors = getNewTextWithBr(widget, option, setbr);
        widget.setText(translatedTextWithColors);
        if (setbr)
            insertBrAfterTransform(widget);
        //                    translatedTextWithColors = changeWidgetTextsWithBr(widget);
//                    widget.setText(translatedTextWithColors);
    }

    private String getNewTextWithBr(Widget widget, transformOptions option, boolean setbr) throws Exception {
        String colorHex = getColorHex(widget);
        if (widget.getId() == ComponentID.CHARACTER_SUMMARY_CONTAINER) {
            if (removeTag(widget.getText()).matches(".*\\p{Alpha}+.*"))
                colorHex = Colors.orange.getHex();
            else {
                widget.setTextColor(Colors.hexToInt(Colors.green.getHex()));
                widget.setText(removeTag(widget.getText()));
                return widget.getText();
            }
        }
        ChatIconManager iconManager = japanesePlugin.getChatIconManager();
        HashMap<String, Integer> map = japanesePlugin.getJapCharIds();

        if (setbr) {//for most including setting_init,chat_buttons, npc dialog
            String line = widget.getText().replace("<br>"," ").trim();
            line = removeTag(line);
            String enWithColors = "<col=" + colorHex + ">" + line;
            //log.info("enWithColors = " + enWithColors);
            HashMap<String, String> settingHash = japTransforms.knownSettingTranslation;
            if (option != transformOptions.API) {
                Widget grandParent = getGrandNParent(widget, 4);
                if (grandParent != null) {//for setting
                    if (grandParent.getId() == ComponentID.SETTINGS_INIT) {
                        return japTransforms.getTransformWithColors(enWithColors, option, map, iconManager, settingHash);
                    }
                }
                //for chat buttons
                grandParent = getGrandNParent(widget, 2);
                if (grandParent != null) {
                    if (grandParent.getId() == ComponentID.CHATBOX_BUTTONS) {
                        return japTransforms.getTransformWithColors(enWithColors, option, map, iconManager, settingHash);
                    }
                }
                //Character summary
                if (widget.getId() == ComponentID.CHARACTER_SUMMARY_CONTAINER) {//only for text, if its only numbers and symbols doesnt get to here, returned above
                    HashMap<String, String> chSummary = japTransforms.knownCharacterSummary;
                    if (line.matches(".*Total XP:.*")) {
                        enWithColors = "<col=" + Colors.orange.getHex() + ">" + "Total XP:";
                        String xp = "<col="+ Colors.green.getHex() +">" + removeTag(widget.getText().split("(?=<col=)")[1].trim());
                        return japTransforms.getTransformWithColors(enWithColors, option, map, iconManager, chSummary)
                                + xp;
                    }
                    else if (line.matches("Time Played:.*")) {
                        enWithColors = "<col=" + Colors.orange.getHex() + ">" + "Time Played:";
//                        + "<col="+ Colors.green.getHex() +">" + removeTag(line.split("Played:")[1]);
//                        return japTransforms.getTransformWithColors(enWithColors, option, map, iconManager, chSummary);
                        if (line.contains("Click to reveal")) {
                            enWithColors =enWithColors + "Click to reveal";
                            return japTransforms.getTransformWithColors(enWithColors, option, map, iconManager, chSummary);
                        } else if (line.contains("days")) {
                            String days = line.split("(?=\\d day)")[1].split("day")[0].trim();
                            String hours = line.split("(?=\\d hour)")[1].split("hour")[0].trim();
                            String wordColDays = "<col=ff00>days,";
                            String wordColHours = "<col=ff00>hours";
                            return japTransforms.getTransformWithColors(enWithColors, option, map, iconManager, chSummary)
                                    + "<col=ff00>" + days + japTransforms.getTransformWithColors(wordColDays, option, map, iconManager, chSummary)
                                    + "<col=ff00>" + hours + japTransforms.getTransformWithColors(wordColHours, option, map, iconManager, chSummary);
                        } else if (line.contains("hours")) {
                            String hours = line.split("(?<=\\d hour)")[1].split("hour")[0].trim();
                            String minutes = line.split("(?<=\\d minute)")[1].split("minute")[0].trim();
                            String wordColHours = "<col=ff00>hours,";
                            String wordColMin = "<col=ff00>minutes";
                            return japTransforms.getTransformWithColors(enWithColors, option, map, iconManager, chSummary)
                                    + "<col=ff00>" + hours + japTransforms.getTransformWithColors(wordColHours, option, map, iconManager, chSummary)
                                    + "<col=ff00>" + minutes + japTransforms.getTransformWithColors(wordColMin, option, map, iconManager, chSummary);
                        } else if (!line.contains("a minute")){// <1h, >1 min
                            String minutes = line.split("(?<=\\d minute)")[1].split("minute")[0].trim();
                            String wordColMin = "<col=ff00>minutes";
                            return japTransforms.getTransformWithColors(enWithColors, option, map, iconManager, chSummary)
                                    + "<col=ff00>" + minutes + japTransforms.getTransformWithColors(wordColMin, option, map, iconManager, chSummary);
                        } else {//time played : a minute
                            enWithColors = enWithColors + "<col=ff00>a minute";
                            return japTransforms.getTransformWithColors(enWithColors, option, map, iconManager, chSummary);
                        }
                    } else
                        return japTransforms.getTransformWithColors(enWithColors, option, map, iconManager, chSummary);
                }

                //"completed quests" and "quest points"
                grandParent = getGrandNParent(widget,2);
                if (grandParent != null && grandParent.getId() == ComponentID.QUEST_LIST_BOX) {
                    if (line.contains("Completed:")) {
                        String completed = "<col=ff981f>Completed:<col=00ff00>" + line.split("(?=(\\d+/\\d+))")[1];
                        return japTransforms.getTransformWithColors(completed, option, map, iconManager, japTransforms.knownSpecificWidgets);
                    } else if (line.contains("Quest Points:")) {
                        String questCom = "<col=ff981f>Quest Points:<col=00ff00>" + line.split("(?=(\\d+/\\d+))")[1];
                        return japTransforms.getTransformWithColors(questCom, option, map, iconManager, japTransforms.knownSpecificWidgets);
                    }
                }

                //quest list tab, dont do it for now
                grandParent = getGrandNParent(widget,4);
                if (grandParent != null && grandParent.getId() == ComponentID.QUEST_LIST_CONTAINER) {
                    if (line.equals("Free Quests") || line.equals("Members' Quests") || line.equals("Miniquests")){
                        return japTransforms.getTransformWithColors(enWithColors, option, map, iconManager, japTransforms.knownSpecificWidgets);
                    }
                    else
                        return enWithColors;//quest names
                }

                //list of Achievement Diaries in tab, not for now
                grandParent = getGrandNParent(widget,1);
                Widget achievementTabWidget = client.getWidget(ComponentID.ACHIEVEMENT_DIARY_CONTAINER);
                if (grandParent != null && grandParent.getId() == ComponentID.ACHIEVEMENT_DIARY_CONTAINER){
                    return enWithColors;
                } else if (grandParent != null && achievementTabWidget != null && achievementTabWidget.getParentId() == grandParent.getId()) {
                    //if its the top part of achievement diaries tab
                    enWithColors = "<col=" + Colors.orange.getHex() + ">" + "Achievement Diaries";
                    return japTransforms.getTransformWithColors(enWithColors, option, map, iconManager, japTransforms.knownSpecificWidgets);
                }

                //the spell book tab
                grandParent = getGrandNParent(widget,2);
                if (grandParent != null) {
                    //normal spellbook
                    Widget normSpellParent = getGrandNParent(client.getWidget(ComponentID.SPELLBOOK_LUMBRIDGE_HOME_TELEPORT), 3);
                    if (normSpellParent.getId() == grandParent.getId()) {
                        return widget.getText();
                    }
                    Widget ancSpellParent = getGrandNParent(client.getWidget(ComponentID.SPELLBOOK_EDGEVILLE_HOME_TELEPORT), 3);
                    if (ancSpellParent.getId() == grandParent.getId()) {
                        return widget.getText();
                    }
                    Widget arcSpellParent = getGrandNParent(client.getWidget(ComponentID.SPELLBOOK_ARCEUUS_HOME_TELEPORT), 3);
                    if (arcSpellParent.getId() == grandParent.getId()) {
                        return widget.getText();
                    }

                    Widget luSpellParent = getGrandNParent(client.getWidget(ComponentID.SPELLBOOK_LUNAR_HOME_TELEPORT), 3);
                    if (luSpellParent.getId() == grandParent.getId()) {
                        return widget.getText();
                    }
                }

                //the title of friends/ignore list
                if (widget.getId() == ComponentID.FRIEND_LIST_TITLE || widget.getId() == ComponentID.IGNORE_LIST_TITLE) {
                    enWithColors = "<col=" + Colors.orange.getHex() + ">";
                    if (widget.getId() == ComponentID.FRIEND_LIST_TITLE) enWithColors = enWithColors + "Friends List";
                    else enWithColors = enWithColors + "Ignores";
                    return japTransforms.getTransformWithColors(enWithColors, option, map, iconManager, japTransforms.knownSpecificWidgets)
                            + "<col=" + Colors.orange.getHex() + ">" + line.split("(?=W\\d+)")[1];
                }
                //friends list
                if (widget.getId() == ComponentID.FRIEND_LIST_NAMES_CONTAINER) {
                    if(widget.getName() != null && !widget.getName().isEmpty()) {//its the name of the friend
                        return widget.getText();
                    } else { // its the friends current world or "offline"
                        return japTransforms.getTransformWithColors(enWithColors, option, map, iconManager, japTransforms.knownSpecificWidgets);
                    }
                }

                //account management tab
                grandParent = getGrandNParent(widget, 5);
                if (grandParent != null && grandParent.getId() == accountManagementTabId){
                    return widget.getText();
                }
                grandParent = getGrandNParent(widget, 4);
                if (grandParent != null && grandParent.getId() == accountManagementTabId){
                    return widget.getText();
                }
                grandParent = getGrandNParent(widget, 3);
                if (grandParent != null && grandParent.getId() == accountManagementTabId){
                    return widget.getText();
                }

                //utc time in grouping tab
                if(line.matches("\\d+:\\d+ UTC"))
                    return widget.getText();
                //grouping tab - gim

                grandParent = getGrandNParent(widget, 1);
                if (grandParent != null && grandParent.getId() == gimTabId) {//Id	47644680
                    if (line.contains("Iron Group"))
                        return japTransforms.getTransformWithColors(enWithColors, option, map, iconManager, japTransforms.knownSpecificWidgets);
                    else
                        return widget.getText();
                }
                grandParent = getGrandNParent(widget, 2);
                if (grandParent != null && grandParent.getId() == gimInGroupTabId){
                    return widget.getText();
                }//
                grandParent = getGrandNParent(widget, 3);
                if (grandParent != null && grandParent.getId() == gimInGroupTabId){
                    return widget.getText();
                }


                //grouping tab - fc
                grandParent = getGrandNParent(widget, 1);
                if (grandParent != null && grandParent.getId() == ComponentID.FRIENDS_CHAT_ROOT) {
                    switch(widget.getId()) {
                        case ComponentID.FRIENDS_CHAT_TITLE:
                        case ComponentID.FRIENDS_CHAT_OWNER://chat title, chat owner (needs translation when not in any fc
                            return widget.getText();
                        default:
                            return japTransforms.getTransformWithColors(enWithColors, option, map, iconManager, japTransforms.knownSpecificWidgets);
                    }

                }
                if(widget.getId() == ComponentID.FRIENDS_CHAT_LIST){//list of channel names, fine as is
                    return widget.getText();
                }
                Widget fcRootParent = client.getWidget(ComponentID.FRIENDS_CHAT_ROOT);
                if (grandParent != null && fcRootParent!= null && fcRootParent.getId() == grandParent.getId()){
                    //the utc time in fc tab, fine as is
                    return widget.getText();
                }

                //clan, guest clan, grouping tab is skipped for now in the loop

                //add other screen if needed
            }
            int wId = widget.getId();
            HashMap<String, String> specificMap = null;
            switch (wId) {
                case ComponentID.DIALOG_NPC_NAME:
                    specificMap = japTransforms.knownNpc;
                    break;
                case ComponentID.DIALOG_NPC_TEXT:
                case ComponentID.DIALOG_PLAYER_TEXT:
                case ComponentID.DIALOG_SPRITE_TEXT:
                case ComponentID.DIALOG_OPTION_OPTIONS:
                    specificMap = japTransforms.knownGameMsgAndDialog;
            }
            return japTransforms.getTransformWithColors(enWithColors, option, map, iconManager,specificMap);
            //for non setting/chat widgets
//            w = japTransforms.getTransformWithColors(enWithColors, option, map, iconManager);
        } else {//only for current xp+next level xp + xp left mouse tool tip
            String[] lineArray = widget.getText().split("<br>");
            StringBuilder stringBuilder = new StringBuilder();
            for (String line : lineArray) {
                line = removeTag(line);
                if (stringBuilder.length() > 0)
                    stringBuilder.append("<br>");
                //line = removeTag(line);
                String enWithColors = "<col=" + colorHex + ">" + line;

                if (option != transformOptions.API) {
                    Widget grandParent = getGrandNParent(widget, 2);
                    if (grandParent != null) {//for chat buttons
                        if (grandParent.getId() == ComponentID.CHATBOX_BUTTONS || grandParent.getId() == ComponentID.SKILLS_CONTAINER) {
                            HashMap<String, String> settingHash = japTransforms.knownSpecificWidgets;
                            stringBuilder.append( japTransforms.getTransformWithColors(enWithColors, option, map, iconManager, settingHash));
                        }
                    }
                } else
                    stringBuilder.append(japTransforms.getTransformWithColors(line, option, map, iconManager));
            }
            return stringBuilder.toString();
        }
    }
    String getColorHex(Widget widget) {
        String colorHex;
        int widgetColor = widget.getTextColor();

        if (widget.getText().isEmpty())//isBlank())
            colorHex = Colors.white.getHex();
        else
            colorHex = Colors.IntToHex(widgetColor);
        return colorHex;
    }
    public String removeTag(String str) {
        return str.replaceAll("<[^>]*>","");//.
                //replace("<","").replace(">","");
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
