package com.japanese;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.game.ChatIconManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.ui.overlay.Overlay;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Objects;
import com.japanese.JapWidgets;

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
    @Inject
    private OverlayManager overlayManager;
//    @Inject
//    private JapaneseOverlay japaneseOverlay;
    @Inject
    private JapWidgets japWidgets;
    private Player player;
    @Getter
    private final JapTransforms japTransforms = new JapTransforms();

    public final String separator = "--";
    @Getter
    protected final HashMap<String, Integer> japCharIds = new HashMap<>();    // colour-char(key) <-> CharIds(val)
    @Getter
    protected final HashMap<String,Integer> chatButtonsIds = new HashMap<>(); //button name (allSelected.png ...) <-> img Ids
    public String dialogueText;
    public String dialogueNPC;
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
//    private  void loadChatButtons()
//    {
//        String[] chatButtonsArray = japChar.getChatButtonList(); //list of all characters e.g.　black+JapChar.separator+面
//        for (String chatButtonsId : chatButtonsArray) {
//            String filePath = getCharPath(chatButtonsId);
//            final BufferedImage image = ImageUtil.loadImageResource(getClass(), filePath);
//            final int charID = chatIconManager.registerChatIcon(image);
//            chatButtonsIds.put(chatButtonsId,charID);
//        }
//        log.info("end of making character image hashmap");
//    }

    private String[] getNewMenuEntryString(MenuEntry event) {
        String[] newOptTar = new String[2];
        transformOptions targetTranOption;
        if(event.getTarget().isEmpty()) {//the event is for walk here or cancel
            newOptTar[1] = null;
            transformOptions optionTranOption;
            optionTranOption = transformOptions.wordToWord;  // todo:get from config
            //log.info("passing option to getTran : " + event.getOption() );
            newOptTar[0] = japTransforms.getTransformWithColors(event.getOption(), optionTranOption, japCharIds, chatIconManager);
        } else {
//            targetTranOption = transformOptions.doNothing;
            //log.info("inside getNewMenuEntryString"); target = ,ffffff>MorvranChunk,ff7000>  (level-35)
            if (event.getTarget().split("<col=").length == 3){//todo meant to detect a player, but currently detects anything with cmb lv
                targetTranOption = transformOptions.doNothing;
                //log.info("target " +event.getTarget()+ " is a player");
            } else {
                targetTranOption = transformOptions.wordToWord; // todo:get from config
                //log.info("target " +event.getTarget()+ " is not a player");
            }
            //log.info("passing target to getTran : " + event.getTarget() );
            newOptTar[0] = japTransforms.getTransformWithColors(event.getTarget(), targetTranOption, japCharIds, chatIconManager);
            //log.info("new option = " + newTarOp[1]);

            transformOptions optionTranOption;
            optionTranOption = transformOptions.wordToWord;  // todo:get from config
            //log.info("passing option to getTran : " + event.getOption() );
            newOptTar[1] = japTransforms.getTransformWithColors(event.getOption(), optionTranOption, japCharIds, chatIconManager);
            //log.info("new target = " + newTarOp[0] + "\n\n");
        }
        return newOptTar;
    }
    protected String getCharPath(String colChar) {
        return "char/" + colChar;
    }
    @Override
    protected void startUp() throws Exception
    {
        log.info("start of plugin");
        japTransforms.initTransHash();
        japWidgets.setJapTransforms(japTransforms);
//        overlayManager.add(japaneseOverlay);
        loadJapChar();

    }

    @Override
    protected  void shutDown() throws  Exception
    {
        log.info("end of plugin");
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged)
    {
        if(gameStateChanged.getGameState() == GameState.LOGGED_IN)
        {
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says: " + config.greeting(), null);
        }
    }
    @Subscribe
    public void onClientTick(ClientTick clientTick)
    {
        try {
            MenuEntry[] event = client.getMenuEntries();
            for (int i = 0; i < event.length; i++) {
                MenuEntry e = event[i];
                if (e.getOption().contains("<img=") ||e.getTarget().contains("<img")) continue;
                String[] newOptTar = getNewMenuEntryString(e); //returns [newTarget, newOption]
//                log.info("target = " + e.getTarget() + ", option = " + e.getOption());
                String newOption = newOptTar[0]; //String with multiple <img=...> which spells the new option's translation, with correct colours
                String newTarget = newOptTar[1];

                if (newOption != null) {
                    if (newTarget == null) {
                        e.setOption(e.getOption().replace(e.getOption(), newOption));
                    } else {
                        //log.info("in else");
                        e.setTarget(e.getTarget().replace(e.getTarget(), newTarget));
                        e.setOption(e.getOption().replace(e.getOption(), newOption));
                        //event.getMenuEntry().setOption(event.getOption().replace(enOption,newEnOption));
                        //event.getMenuEntry().setTarget(event.getTarget().replace(enTarget, newEnTarget));
                        //e.setOption(e.getOption().replace(enOption,"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"));
                        //e.setTarget(e.getTarget().replace(enTarget,"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"));
                    }
                }
            }
        }
        catch (Exception e){
            //System.out.print(e.getMessage());
        }
    }

    @Subscribe
    private void onBeforeRender(BeforeRender event) {
        //null to look through everything, otherwise specify widget parent not to search through for texts
        japWidgets.changeWidgetTexts(null);
    }
    @Subscribe
    // get dialog content when talking with npc
    public void onChatMessage(ChatMessage event){
        if (event.getType() == ChatMessageType.DIALOG) {
            log.info("dialog's event.getMessage() = " + event.getMessage());
            log.info("dialog's event.getSender() = " + event.getSender());
            log.info("dialog's event.getname() = " + event.getName());
            String dialogEn = event.getMessage();
            dialogueNPC = dialogEn.split("\\|")[0];
            dialogueText = dialogEn.split("\\|")[1];
            //String npcNameJp = japTransforms.getTransformWithColors(npcName, transformOptions.wordToWord, japCharIds, chatIconManager);
            //String dialogTextJp = japTransforms.getTransformWithColors(dialogText, transformOptions.wordToWord, japCharIds, chatIconManager);
            //event.setMessage(event.getMessage().replace(dialogEn, npcNameJp+"|"+dialogTextJp));
            //event.setMessage(event.getMessage().replace(dialogEn, "me|whats up?"));
            //client.refreshChat();

            //String senderJap = japTransforms.getTransformWithColors(event.getSender(), transformOptions.wordToWord, japCharIds, chatIconManager);
            //event.setSender(senderJap);

            //String nameJap = japTransforms.getTransformWithColors(event.getName(), transformOptions.wordToWord, japCharIds, chatIconManager);

        }
    }

    @Provides
    JapaneseConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(JapaneseConfig.class);
    }

}
