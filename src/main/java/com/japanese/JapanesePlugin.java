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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

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
    public RomToJap romToJap = new RomToJap();
    @Inject
    private OverlayManager overlayManager;
    @Inject @Getter
    private ChatInputOverlay chatInputOverlay;
    @Inject
    private KatKanjCandiOvl katKanjCandiOvl;
    @Inject
    private JapWidgets japWidgets;
    @Inject
    private ChatModifier chatModifier;
    private Player player;
    @Getter
    private final JapTransforms japTransforms = new JapTransforms();
    public final String separator = "--";
    @Getter
    protected final HashMap<String, Integer> japCharIds = new HashMap<>();    // colour-char(key) <-> CharIds(val)
    @Getter
    protected final HashMap<String,Integer> chatButtonsIds = new HashMap<>(); //button name (allSelected.png ...) <-> img Ids
    public String dialogueText;
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


    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged)
    {
        if(gameStateChanged.getGameState() == GameState.LOGGED_IN)
        {
            //String enWithColors = "<col=000000>aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
            ChatIconManager iconManager = getChatIconManager();
            HashMap<String, Integer> map = getJapCharIds();
            final transformOptions option = transformOptions.wordToWord;
            //String translatedTextWithColors = "<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">";
            //client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", translatedTextWithColors, null);
           // client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says: " + config.greeting(), null);
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
    private void onBeforeRender(BeforeRender event) throws IOException {
        //null to look through everything, otherwise specify widget parent not to search through for texts
        japWidgets.changeWidgetTexts(null);
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage){
        if (client.getGameState() != GameState.LOGGED_IN && client.getGameState() != GameState.HOPPING)
            return;

        chatModifier.modifyChat(chatMessage);

    }
    @Override
    protected void startUp() throws Exception
    {
        log.info("start of plugin");
        japTransforms.initTransHash();
        romToJap.initRom2JpHash();
        japWidgets.setJapTransforms(japTransforms);
        chatModifier.setJapTransforms(japTransforms);
        overlayManager.add(chatInputOverlay);
        overlayManager.add(katKanjCandiOvl);
        loadJapChar();

    }

    @Override
    protected  void shutDown() throws  Exception
    {
        log.info("end of plugin");
    }
    @Provides
    JapaneseConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(JapaneseConfig.class);
    }

}
