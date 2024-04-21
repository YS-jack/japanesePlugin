package com.japanese;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;

/*
 * Copyright (c) 2017, Aria <aria@ar1as.space>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
@Slf4j
class MouseTooltipOverlay extends Overlay
{
    /**
     * Menu types which are on widgets.
     */
    private static final Set<MenuAction> WIDGET_MENU_ACTIONS = ImmutableSet.of(
            MenuAction.WIDGET_TYPE_1,
            MenuAction.WIDGET_TARGET,
            MenuAction.WIDGET_CLOSE,
            MenuAction.WIDGET_TYPE_4,
            MenuAction.WIDGET_TYPE_5,
            MenuAction.WIDGET_CONTINUE,
            MenuAction.ITEM_USE_ON_ITEM,
            MenuAction.WIDGET_USE_ON_ITEM,
            MenuAction.ITEM_FIRST_OPTION,
            MenuAction.ITEM_SECOND_OPTION,
            MenuAction.ITEM_THIRD_OPTION,
            MenuAction.ITEM_FOURTH_OPTION,
            MenuAction.ITEM_FIFTH_OPTION,
            MenuAction.ITEM_USE,
            MenuAction.WIDGET_FIRST_OPTION,
            MenuAction.WIDGET_SECOND_OPTION,
            MenuAction.WIDGET_THIRD_OPTION,
            MenuAction.WIDGET_FOURTH_OPTION,
            MenuAction.WIDGET_FIFTH_OPTION,
            MenuAction.EXAMINE_ITEM,
            MenuAction.WIDGET_TARGET_ON_WIDGET,
            MenuAction.CC_OP_LOW_PRIORITY,
            MenuAction.CC_OP
    );

    private final TooltipManager tooltipManager;
    private final Client client;
    private final JapaneseConfig config;
    @Inject
    private JapanesePlugin japanesePlugin;

    @Inject
    MouseTooltipOverlay(Client client, TooltipManager tooltipManager, JapaneseConfig config)
    {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        // additionally allow tooltips above the full screen world map and welcome screen
        drawAfterInterface(InterfaceID.FULLSCREEN_CONTAINER_TLI);
        this.client = client;
        this.tooltipManager = tooltipManager;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (client.isMenuOpen())
        {
            return null;
        }

        MenuEntry[] menuEntries = client.getMenuEntries();
        int last = menuEntries.length - 1;

        if (last < 0)
        {
            return null;
        }

        MenuEntry menuEntry = menuEntries[last];
        String target = menuEntry.getTarget();
        String option = menuEntry.getOption();
        MenuAction type = menuEntry.getType();

        if (type == MenuAction.RUNELITE_OVERLAY || type == MenuAction.CC_OP_LOW_PRIORITY)
        {
            // These are always right click only
            return null;
        }

        if (Strings.isNullOrEmpty(option))
        {
            return null;
        }

        // Trivial options that don't need to be highlighted, add more as they appear.
        String codeWalkHere = japanesePlugin.getJapTransforms().getCharImgTagsFromJapString("ここまで歩く", Colors.white);
        String codeCancel = japanesePlugin.getJapTransforms().getCharImgTagsFromJapString("キャンセル", Colors.white);
        String codeContinue = japanesePlugin.getJapTransforms().getCharImgTagsFromJapString("続ける", Colors.white);
        String codeSlide = japanesePlugin.getJapTransforms().getCharImgTagsFromJapString("スライド", Colors.orange);

        if (option.equals(codeWalkHere) || option.equals(codeCancel) || option.equals(codeContinue) || target.contains(codeSlide))
            return null;

        if (WIDGET_MENU_ACTIONS.contains(type))
        {
            final int widgetId = menuEntry.getParam1();
            final int groupId = WidgetUtil.componentToInterface(widgetId);

            if (!config.mouseTooltip()){
                return null;
            }
        }

        // If this varc is set, a tooltip will be displayed soon
        int tooltipTimeout = client.getVarcIntValue(VarClientInt.TOOLTIP_TIMEOUT);
        if (tooltipTimeout > client.getGameCycle())
        {
            return null;
        }

        // If this varc is set, a tooltip is already being displayed
        int tooltipDisplayed = client.getVarcIntValue(VarClientInt.TOOLTIP_VISIBLE);
        if (tooltipDisplayed == 1)
        {
            return null;
        }

        tooltipManager.addFront(new Tooltip(option + (Strings.isNullOrEmpty(option) ? "" : " " + target)));

//        try {
//            String[] newOptionTarget = getNewMenuEntryString();
//            String newOption = newOptionTarget[0];
//            String newTarget = newOptionTarget[1];
//            tooltipManager.addFront(new Tooltip(newOption + (Strings.isNullOrEmpty(newTarget) ? "" : " " + newTarget)));
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

        return null;
    }

    private String[] getNewMenuEntryString() throws Exception {
        MenuEntry[] menuEntries = client.getMenuEntries();
        int last = menuEntries.length - 1;
        MenuEntry event = menuEntries[last];
        String target = event.getTarget();
        String option = event.getOption();

        String[] newOptTar = new String[2];
        JapTransforms.transformOptions targetTranOption;
        JapTransforms japTransforms = japanesePlugin.getJapTransforms();
        if(target.isEmpty()) {//the event is for walk here(no target) or cancel
            newOptTar[1] = "";
            JapTransforms.transformOptions optionTranOption;
            optionTranOption = JapTransforms.transformOptions.wordToWord;
            //log.info("passing option to getTran : " + option );
            HashMap<String,String> map = japTransforms.knownMenuOption;
            //newOptTar[0] = japTransforms.getTransformWithColors(option, optionTranOption, japCharIds, chatIconManager, map);
            if (option.matches(".*<col=.*"))
                newOptTar[0] = getWithColCode(option, optionTranOption, map);
            else
                newOptTar[0] = japTransforms.transform(option,optionTranOption,map,true);
        } else {
            targetTranOption = JapTransforms.transformOptions.wordToWord;//todo get target translation method from config
            if (event.getActor() instanceof Player){
                //log.info("player :" + target + ", option:" + option);
                targetTranOption = JapTransforms.transformOptions.doNothing;
            } else if (Objects.equals(option, "Walk here") && !target.isBlank()) {
                //log.info("for walk here > player :" + target + ", option:" + option);
                targetTranOption = JapTransforms.transformOptions.doNothing;
            }

            //translating menu target
            //log.info("passing target to getTran : " + target );
            Widget geWidget = client.getWidget(ComponentID.GRAND_EXCHANGE_WINDOW_CONTAINER);
            if (geWidget != null && !geWidget.isHidden())//dont change target name if opening ge widget
                newOptTar[0] = target;
            else {
                HashMap<String, String> map = japanesePlugin.getMap(event);
                //newOptTar[0] = japTransforms.getTransformWithColors(target.replace("(level-", "(レベル"),
                //        targetTranOption, japCharIds, chatIconManager, map);
                if (target.matches(".*<col=.*"))
                    newOptTar[0] = getWithColCode(target.replace("(level-", "(レベル"), targetTranOption, map);
                else
                    newOptTar[0] = japTransforms.transform(target.replace("(level-", "(レベル"),targetTranOption,map,true);
            }
            //log.info("new option = " + newOptTar[1]);

            //translating menu option
            JapTransforms.transformOptions optionTranOption;
            optionTranOption = JapTransforms.transformOptions.wordToWord; //todo get from config? might not need to if have all option translated in knownMenuOption
            HashMap<String,String> map = japTransforms.knownMenuOption;
            //log.info("passing option to getTran : " + option );
            //newOptTar[1] = spaceImageText + //add space because for some reason the first letter disappears
            //        japTransforms.getTransformWithColors(option, optionTranOption, japCharIds, chatIconManager,map);
            if (target.matches(".*<col=.*"))
                newOptTar[1] = getWithColCode(option, optionTranOption, map);
            else
                newOptTar[1] = japTransforms.transform(option,optionTranOption,map,true);
        }
        return newOptTar;
    }

    private String getWithColCode(String text, JapTransforms.transformOptions optionTranOption, HashMap<String,String> map) throws Exception {
        Pattern pattern = Pattern.compile("(<col=.+>|</col>)");
        Matcher matcher = pattern.matcher(text);

        List<String> parts = new ArrayList<>();
        int start = 0;
        while (matcher.find()) {
            if (start != matcher.start()) {
                parts.add(text.substring(start, matcher.start()));
            }
            parts.add(matcher.group());
            start = matcher.end();
        }

        // Add any remaining part of the string after the last match
        if (start != text.length()) {
            parts.add(text.substring(start));
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String str : parts){
            if (str.matches("(<col=.+>|</col>)"))
                stringBuilder.append(str);
            else {
                String translation = japanesePlugin.getJapTransforms().transform(str,optionTranOption,map,true);
                stringBuilder.append(translation);
            }
        }

        return stringBuilder.toString();
    }
}
