package com.japanese;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.game.ChatIconManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;


import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Objects;

import com.japanese.JapTransforms.transformOptions;

@Slf4j
@PluginDescriptor(
        name = "Japanese",
        description = "plugin to translate game texts into Japanese",
        tags = {"japan", "translate", "日本","にほ","niho","nippo"},
        enabledByDefault = false
)

public class JapanesePlugin extends Plugin{
    @Inject
    private Client client;
    @Inject @Getter
    public ChatIconManager chatIconManager;
    @Inject
    public JapaneseConfig config;
    @Inject
    private JapChar japChar;
    @Inject @Getter
    private RomToJap romToJap = new RomToJap();
    @Inject
    private OverlayManager overlayManager;
    @Inject @Getter
    private ChatInputOverlay chatInputOverlay;
    @Inject
    private KatKanjCandiOvl katKanjCandiOvl;
    @Inject
    private APICountOverlay apiCountOverlay;
    @Inject @Getter
    private JapWidgets japWidgets;
    @Inject @Getter
    private ApiTranslate apiTranslate;
    @Inject
    private ChatOptionOverlay chatOptionOverlay;
    @Inject
    private ChatModifier chatModifier;
    @Getter @Inject
    private JapTransforms japTransforms = new JapTransforms();
    public final String separator = "--";
    @Getter
    protected final HashMap<String, Integer> japCharIds = new HashMap<>();    // colour-char(key) <-> CharIds(val)
    @Getter
    protected final HashMap<String,Integer> chatButtonsIds = new HashMap<>(); //button name (allSelected.png ...) <-> img Ids
    private HashMap<String,String> examineJpEnMap = new HashMap<>();
    public String dialogueText;
    private String spaceImageText;

    private void loadJapChar()
    {
        String[] japCharArray = japChar.getCharList(); //list of all characters e.g.　black+JapChar.separator+面
        for (String s : japCharArray) {
            String filePath = getCharPath(s);
            final BufferedImage image = ImageUtil.loadImageResource(getClass(), filePath);
            final int charID = chatIconManager.registerChatIcon(image);
            japCharIds.put(s, charID);
        }
        log.info("end of making character image hashmap");
    }


    private String[] getNewMenuEntryString(MenuEntry event) throws Exception {
        String[] newOptTar = new String[2];
        transformOptions targetTranOption;
        if(event.getTarget().isEmpty()) {//the event is for walk here(no target) or cancel
            newOptTar[1] = null;
            transformOptions optionTranOption;
            optionTranOption = transformOptions.wordToWord;
            //log.info("passing option to getTran : " + event.getOption() );
            HashMap<String,String> map = japTransforms.knownMenuOption;
            newOptTar[0] = japTransforms.getTransformWithColors(event.getOption(), optionTranOption, japCharIds, chatIconManager, map);
        } else {
            targetTranOption = transformOptions.wordToWord;//todo get target translation method from config
            if (event.getActor() instanceof Player){
                //log.info("player :" + event.getTarget() + ", option:" + event.getOption());
                targetTranOption = transformOptions.doNothing;
            } else if (Objects.equals(event.getOption(), "Walk here") && !event.getTarget().isBlank()) {
                //log.info("for walk here > player :" + event.getTarget() + ", option:" + event.getOption());
                targetTranOption = transformOptions.doNothing;
            }

            //translating menu target
            //log.info("passing target to getTran : " + event.getTarget() );
            Widget geWidget = client.getWidget(ComponentID.GRAND_EXCHANGE_WINDOW_CONTAINER);
            if (geWidget != null && !geWidget.isHidden())//dont change target name if opening ge widget
                newOptTar[0] = event.getTarget();
            else {
                HashMap<String, String> map = getMap(event);
                newOptTar[0] = japTransforms.getTransformWithColors(event.getTarget().replace("(level-", "(レベル"),
                        targetTranOption, japCharIds, chatIconManager, map);
            }
            //log.info("new option = " + newOptTar[1]);

            if (event.getOption().equals("Examine")
                    && !examineJpEnMap.containsKey(newOptTar[0])
                    && !config.gameMessageConfig().equals(JapaneseConfig.GameTextProcessChoice.そのまま)) {
                //log.info(event.getTarget());
                String enTarget = event.getTarget().replaceAll("  \\(level-\\d+\\)","");
                examineJpEnMap.put(newOptTar[0], japWidgets.removeTag(enTarget));
            }
            //translating menu option
            transformOptions optionTranOption;
            optionTranOption = transformOptions.wordToWord; //todo get from config? might not need to if have all option translated in knownMenuOption
            HashMap<String,String> map = japTransforms.knownMenuOption;
            //log.info("passing option to getTran : " + event.getOption() );
            newOptTar[1] = spaceImageText + //add space because for some reason the first letter disappears
                    japTransforms.getTransformWithColors(event.getOption(), optionTranOption, japCharIds, chatIconManager,map);
            //log.info("new target = " + newOptTar[0] + "\n\n");
            //log.info("translation = " + newOptTar[1] + "\n\n");
        }
        return newOptTar;
    }
    protected String getCharPath(String colChar) {
        return "char/" + colChar;
    }
    private HashMap<String, String> getMap(MenuEntry event){
        String target = event.getTarget();
        MenuAction action = event.getType();
//        if (action == MenuAction.WIDGET_TARGET || action == MenuAction.WIDGET_CLOSE
//                || action == MenuAction.CC_OP || action == MenuAction.CC_OP_LOW_PRIORITY)

        switch (action) {
            case GAME_OBJECT_FIRST_OPTION:
            case GAME_OBJECT_SECOND_OPTION:
            case GAME_OBJECT_THIRD_OPTION:
            case GAME_OBJECT_FOURTH_OPTION:
            case GAME_OBJECT_FIFTH_OPTION:
            case EXAMINE_OBJECT:
                return japTransforms.knownObject;

            case NPC_FIRST_OPTION:
            case NPC_SECOND_OPTION:
            case NPC_THIRD_OPTION:
            case NPC_FOURTH_OPTION:
            case NPC_FIFTH_OPTION:
            case EXAMINE_NPC:
                return japTransforms.knownNpc;

            case WIDGET_TARGET:
            case GROUND_ITEM_FIRST_OPTION:
            case CC_OP:
            case CC_OP_LOW_PRIORITY:
            case GROUND_ITEM_SECOND_OPTION:
            case GROUND_ITEM_THIRD_OPTION:
            case GROUND_ITEM_FOURTH_OPTION:
            case GROUND_ITEM_FIFTH_OPTION:
            case EXAMINE_ITEM_GROUND:
                log.info("menu target:" + target+", option:"+event.getOption()+", action type:" + action);
                return japTransforms.knownItemAndWidgets;
        }
        return null;
    }
    private String getItemOrNpc(String color) {
        String col = color.split("<col=")[1].split(">")[0].toLowerCase();
        if (col.equals("ffff"))
            return "object";
        else if (col.equals("ff9040"))
            return "item";
        else
            return null;
    }


    @Subscribe
    public void onClientTick(ClientTick clientTick) {
        //return;
        if (config.menuEntryConfig() == JapaneseConfig.jpEnChoice.英語)
            return;
        MenuEntry[] event = client.getMenuEntries();
        if (event.length == 1 && event[0].getOption().equals("Cancel")&&!client.isMenuOpen())
            return;
        boolean show = false;
        if (event.length > 1 && !client.isMenuOpen()) {
            for (MenuEntry e : event){
                if (!e.getOption().equals("Walk here")
                && !e.getOption().equals("Cancel")
                && !e.getOption().equals("Examine")
                ){
                    show = true;
                    break;
                }
            }
            if (!show)
                return;
        }
//        if (show)
        {
            {
                try {
                    for (MenuEntry e : event) {
                        if (e.getOption().contains("<img=") || e.getTarget().contains("<img")) continue;
                        String[] newOptTar = getNewMenuEntryString(e); //returns [newTarget, newOption]
                        String newOption = newOptTar[0]; //String with multiple <img=...> which spells the new option's translation, with correct colours
                        String newTarget = newOptTar[1];

                        if (newOption != null) {
                            if (newTarget == null) {
                                e.setOption(e.getOption().replace(e.getOption(), newOption));
                            } else {
                                e.setOption(e.getOption().replace(e.getOption(), newOption));
                                e.setTarget(e.getTarget().replace(e.getTarget(), newTarget));

                            }
                        }
                    }
                } catch (Exception e) {
                    //System.out.print(e.getMessage());
                }
            }
        }
    }

    @Subscribe
    private void onBeforeRender(BeforeRender event) throws Exception {
        //null to look through everything, otherwise specify widget parent not to search through for texts
        japWidgets.changeWidgetTexts(null);
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) throws Exception {
        if (client.getGameState() != GameState.LOGGED_IN && client.getGameState() != GameState.HOPPING)
            return;

        chatModifier.modifyChat(chatMessage);

    }
    @Subscribe
    public void onOverheadTextChanged(OverheadTextChanged event) throws Exception {
        chatModifier.translateOverhead(event);
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event){
        String targetNameJp = event.getMenuOption();
        String targetNameEn = examineJpEnMap.get(targetNameJp);
        chatModifier.setLastExamined(targetNameEn);
    }
    @Override
    protected void startUp() throws Exception
    {
        log.info("start of plugin");
        loadJapChar();
        japTransforms.initTransHash();
        romToJap.initRom2JpHash();
        apiTranslate.apiCountInit();
        chatModifier.initChatModifier();

        japWidgets.setJapTransforms(japTransforms);
        chatModifier.setJapTransforms(japTransforms);
        overlayManager.add(chatInputOverlay);
        overlayManager.add(katKanjCandiOvl);
        overlayManager.add(apiCountOverlay);
        overlayManager.add(chatOptionOverlay);

        String[][] space = {{"blue"," "}};
        spaceImageText = japTransforms.buildJapStringImage(space,japCharIds,chatIconManager);

        if (!config.webHookUrl().isEmpty())
            japTransforms.webhook = new DiscordWebhook(config.webHookUrl());
    }

    @Override
    protected  void shutDown() throws  Exception
    {
        log.info("end of plugin");
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged change) throws Exception {
        if (change.getGroup().equals(JapaneseConfig.GROUP)){
            if(change.getKey().equals("DeeplAPIOption") || change.getKey().equals("useDeepl")){
                apiTranslate.apiCountInit();
            }
            if(change.getKey().equals("webhookUrl")) {
                if (!config.webHookUrl().isEmpty())
                    japTransforms.webhook = new DiscordWebhook(config.webHookUrl());
            }
        }
    }
    @Provides
    JapaneseConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(JapaneseConfig.class);
    }

}
