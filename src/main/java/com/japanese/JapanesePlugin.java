package com.japanese;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.ComponentID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.game.ChatIconManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;


import java.awt.image.BufferedImage;
import java.io.IOException;
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
    private HashMap<String,String> examineEnJpMap = new HashMap<>();
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

    private String[] getNewMenuEntryString(MenuEntry event) throws Exception {
        String[] newOptTar = new String[2];
        transformOptions targetTranOption;
        if(event.getTarget().isEmpty()) {//the event is for walk here or cancel
            newOptTar[1] = null;
            transformOptions optionTranOption;
            optionTranOption = transformOptions.wordToWord;
            //log.info("passing option to getTran : " + event.getOption() );
            newOptTar[0] = japTransforms.getTransformWithColors(event.getOption(), optionTranOption, japCharIds, chatIconManager);
        } else {
            targetTranOption = transformOptions.wordToWord;
            if (event.getActor() instanceof Player){
                //log.info("player :" + event.getTarget() + ", option:" + event.getOption());
                targetTranOption = transformOptions.doNothing;
            } else if (Objects.equals(event.getOption(), "Walk here") && !event.getTarget().isBlank()) {
                //log.info("for walk here > player :" + event.getTarget() + ", option:" + event.getOption());
                targetTranOption = transformOptions.doNothing;
            }

            //log.info("passing target to getTran : " + event.getTarget() );
            newOptTar[0] = japTransforms.getTransformWithColors(event.getTarget().replace("(level-","(レベル"), targetTranOption, japCharIds, chatIconManager);
            //log.info("new option = " + newOptTar[1]);

            transformOptions optionTranOption;
            optionTranOption = transformOptions.wordToWord;
            //log.info("passing option to getTran : " + event.getOption() );
            newOptTar[1] = spaceImageText + //add space because for some reason the first letter disappears
                    japTransforms.getTransformWithColors(event.getOption(), optionTranOption, japCharIds, chatIconManager);
//            if (event.getType() == MenuAction.WALK)
//                newOptTar[1] = "<img=22616>" + newOptTar[1];
            //log.info("new target = " + newOptTar[0] + "\n\n");
            //log.info("translation = " + newOptTar[1] + "\n\n");
        }
        return newOptTar;
    }
    protected String getCharPath(String colChar) {
        return "char/" + colChar;
    }


//    @Subscribe
//    public void onGameStateChanged(GameStateChanged gameStateChanged) throws Exception {
////        if(gameStateChanged.getGameState() == GameState.LOGGED_IN)
////        {
//////            //String enWithColors = "<col=000000>aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
//////            ChatIconManager iconManager = getChatIconManager();
//////            HashMap<String, Integer> map = getJapCharIds();
//////            final transformOptions option = transformOptions.wordToWord;
//////            //String translatedTextWithColors = "<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"+"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">";
//////            //client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", translatedTextWithColors, null);
//////           // client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says: " + config.greeting(), null);
////        }
//    }
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
//    @Subscribe
//    public void onPostClientTick(PostClientTick clientTick) {
//        if (config.menuEntryConfig() == JapaneseConfig.jpEnChoice.英語)
//            return;
//        MenuEntry[] event = client.getMenuEntries();
//
//        if (event.length == 1 && event[0].getOption().equals("Cancel")&&!client.isMenuOpen())
//            return;
//        if (event.length > 1 && !client.isMenuOpen()) {
//
//            //log.info("option=" + event[event.length - 1].getOption());
//            if (event[event.length - 1].getOption().equals("Walk here")) {
//                return;
//            }
//        }
//        try {
////            for (int i = 0; i < event.length;i++){
////                log.info("i = " + i + ", target = " + event[i].getTarget() + ", option = " + event[i].getOption());
////            }
//            for (int i = 0; i < event.length; i++) {
//                MenuEntry e = event[i];
//                if (e.getOption().contains("<img=")) continue;
//                String[] newOptTar = getNewMenuEntryString(e); //returns [newTarget, newOption]
////                log.info("target = " + e.getTarget() + ", option = " + e.getOption());
//                String newOption = newOptTar[0]; //String with multiple <img=...> which spells the new option's translation, with correct colours
//                String newTarget = newOptTar[1];
//                int maxWidth = 0;
//                if (newOption != null) {
//                    if (newTarget == null) {
//                        e.setOption(e.getOption().replace(e.getOption(), newOption));
//                        if ((e.getOption().split("<img").length-1) * 14 > e.getOption().length())
//                            maxWidth = (e.getOption().split("<img").length-1) * 14;
//                    } else {
//                        //log.info("in else");
//                        if ((e.getOption().split("<img").length-1 + e.getTarget().split("<img").length-1) * 14 > e.getOption().length())
//                            maxWidth = (e.getOption().split("<img").length-1) * 14;
//                        e.setTarget(e.getTarget().replace(e.getTarget(), newTarget));
//                        e.setOption(e.getOption().replace(e.getOption(), newOption));
//                        //event.getMenuEntry().setOption(event.getOption().replace(enOption,newEnOption));
//                        //event.getMenuEntry().setTarget(event.getTarget().replace(enTarget, newEnTarget));
//                        //e.setOption(e.getOption().replace(enOption,"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"));
//                        //e.setTarget(e.getTarget().replace(enTarget,"<img="+chatIconManager.chatIconIndex(japCharIds.get("black--51.png"))+">"));
//                    }
//
//                }
//            }
//        }
//        catch (Exception e){
//            //System.out.print(e.getMessage());
//        }
//    }

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
        String targetName = event.getMenuOption();
        chatModifier.setLastExamined(targetName);
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
    }

    @Override
    protected  void shutDown() throws  Exception
    {
        log.info("end of plugin");
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged change) throws Exception {
        if (change.getGroup().equals(JapaneseConfig.GROUP)
        && (change.getKey().equals("DeeplAPIOption") || change.getKey().equals("useDeepl"))){
            apiTranslate.apiCountInit();
        }
    }
    @Provides
    JapaneseConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(JapaneseConfig.class);
    }

}
