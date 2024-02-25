package com.japanese;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.game.ChatIconManager;
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
    @Inject
    public ChatIconManager chatIconManager;
    @Inject
    private JapaneseConfig config;
    @Inject
    private JapChar japChar;

    private Player player;
    private final JapTransforms japTransforms = new JapTransforms();
    public final String separator = "--";
    protected final HashMap<String, Integer> japCharIds = new HashMap<>();    // colour-char(key) <-> CharIds(val)
    private void loadJapChar()
    {
        String[] japCharArray = japChar.getCharList(); //list of all characters e.g.　black+JapChar.separator+面
        for (int i = 0; i < japCharArray.length; i++) {
            String filePath = getCharPath(japCharArray[i]);
            final BufferedImage image = ImageUtil.loadImageResource(getClass(), filePath);
            final int charID = chatIconManager.registerChatIcon(image);
            japCharIds.put(japCharArray[i], charID);
        }
        log.info("end of making character image hashmap");
    }

    private String[] getNewMenuEntryString(MenuEntry event) {
        String[] newOptTar = new String[2];
        transformOptions targetTranOption;
        if(event.getTarget().isEmpty()) {
            newOptTar[1] = null;
            transformOptions optionTranOption;
            optionTranOption = transformOptions.wordToWord;  // todo:get from config
            //log.info("passing option to getTran : " + event.getOption() );
            newOptTar[0] = japTransforms.getTransformWithColors(event.getOption(), optionTranOption, japCharIds, chatIconManager);
        } else {
//            targetTranOption = transformOptions.doNothing;
            //log.info("inside getNewMenuEntryString"); target = ,ffffff>MorvranChunk,ff7000>  (level-35)
            if (event.getTarget().split("<col=").length == 3){
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
        loadJapChar();


    }

    @Override
    protected  void shutDown() throws  Exception
    {
        log.info("start of plugin");
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
    // get dialog content when talking with npc
    public void onChatMessage(ChatMessage event){
        if (event.getType() == ChatMessageType.DIALOG) {
            String dialogueText = event.getMessage();
            String sender = event.getSender();
            String name = event.getName();

            log.info("dialogue text = " + dialogueText);
        }
    }

    @Provides
    JapaneseConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(JapaneseConfig.class);
    }
}
