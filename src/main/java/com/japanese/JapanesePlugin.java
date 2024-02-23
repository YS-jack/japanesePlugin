package com.japanese;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.game.ChatIconManager;
import net.runelite.client.util.ImageUtil;
import java.awt.image.BufferedImage;

@Slf4j
@PluginDescriptor(
        name = "Japanese",
        description = "plugin to translate game texts into Japanese",
        tags = {"japan", "translate", "日本","にほ"},
        enabledByDefault = false
)

public class JapanesePlugin extends Plugin{
    @Inject
    private Client client;
    @Inject
    private ChatIconManager chatIconManager;
    @Inject
    private JapaneseConfig config;
    private int[] japCharIds;
    private String[] charPath;
    private void loadJapChar()
    {
        if (japCharIds != null){return;}
        if (charPath != null){return;}
        int japCharNum = 2;
        japCharIds = new int[japCharNum];
        charPath = new String[japCharNum];
        charPath[0] = "char/blue/動.png";
        charPath[1] = "char/yellow/人.png";
        for (int i = 0; i < japCharNum; i++) {
            final BufferedImage image = ImageUtil.loadImageResource(getClass(), charPath[i]);
            japCharIds[i] = chatIconManager.registerChatIcon(image);
            log.info("japCharid["+i+"] = " +japCharIds[i]);
        }
    }
    @Override
    protected void startUp() throws Exception
    {
        log.info("プラグイン開始");
        loadJapChar();
    }

    @Override
    protected  void shutDown() throws  Exception
    {
        log.info("プラグイン停止");
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
    public void onMenuEntryAdded(MenuEntryAdded event)
    {
        try {
            String enOption = event.getMenuEntry().getOption().trim();
            String enTarget = event.getMenuEntry().getTarget().trim();
            String newEnOption = "";
            String newEnTarget = "";
            if (enTarget.isEmpty()) {
                //todo- translate the option to japanese
                event.getMenuEntry().setOption(event.getOption().replace(enOption,"<img="+chatIconManager.chatIconIndex(japCharIds[0])+">"));
            }
            else {
                newEnOption = enTarget;
                newEnTarget = enOption;
                //todo - translate the new* to japanese
                //event.getMenuEntry().setOption(event.getOption().replace(enOption,newEnOption));
                //event.getMenuEntry().setTarget(event.getTarget().replace(enTarget, newEnTarget));
                event.getMenuEntry().setOption(event.getOption().replace(enOption,"<img="+chatIconManager.chatIconIndex(japCharIds[0])+">"));
                event.getMenuEntry().setTarget(event.getTarget().replace(enTarget,"<img="+chatIconManager.chatIconIndex(japCharIds[1])+">"));
            }


        }
        catch (Exception e){
            log.error(e.getMessage());
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
