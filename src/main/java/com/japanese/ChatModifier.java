package com.japanese;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;

import com.japanese.JapTransforms.transformOptions;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.game.ChatIconManager;
import net.runelite.api.clan.ClanID;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ChatModifier {
    @Inject
    private JapanesePlugin japanesePlugin;
    @Inject
    @Setter
    private JapTransforms japTransforms;
    @Inject
    private JapWidgets japWidgets = new JapWidgets();
    @Inject
    private ChatMessageManager chatMessageManager;
    @Inject
    private Client client;

    @Inject
    private JapaneseConfig config;
    @Setter
    private String lastExamined = "";
    private String examineStringImage;
    public void initChatModifier () {
        String[][] colWords = {{"blue","英語名:"}};
        examineStringImage = japanesePlugin.getJapTransforms().buildJapStringImage(colWords,japanesePlugin.getJapCharIds(),japanesePlugin.chatIconManager);
    }
    public void translateOverhead(OverheadTextChanged event) throws Exception {
        String enMsg = event.getOverheadText();
        Actor actor = event.getActor();

        transformOptions option = getOverheadOption(actor);
        if(option == transformOptions.doNothing)
            return;

        String colorHex = Colors.yellow.getHex();
        String enWithColors = "<col=" + colorHex + ">" + enMsg;

        ChatIconManager iconManager = japanesePlugin.getChatIconManager();
        HashMap<String, Integer> map = japanesePlugin.getJapCharIds();
        JapTransforms jt = japanesePlugin.getJapTransforms();


        if (option == transformOptions.API && !japanesePlugin.getJapTransforms().knownAPI.containsKey(enMsg.toLowerCase())) {
            Thread thread = new Thread(() -> {//process with new thread because games freezes while waiting for api response
                try {
                    int count = 0;
                    while(true) {
                        if (japanesePlugin.getJapTransforms().knownAPI.containsKey(enMsg.toLowerCase()) || !(actor instanceof Player) || count > 50)
                            break;
                        else {
                            Thread.sleep(100);
                            count++;
                        }
                    }
                    if (count > 50)
                        return;
                    String japaneseMsg;
                    if(actor instanceof Player)
                        japaneseMsg = jt.getTransformWithColors(enWithColors, option, map, iconManager, false);
                    else
                        japaneseMsg = jt.getTransformWithColors(enWithColors, option, map, iconManager, true);
                    event.getActor().setOverheadText(japaneseMsg);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            thread.setDaemon(false);
            thread.start();
        } else {
            String japaneseMsg = jt.getTransformWithColors(enWithColors, option, map, iconManager);
            event.getActor().setOverheadText(japaneseMsg);
        }

    }
    private transformOptions getOverheadOption(Actor actor){
        if (actor instanceof NPC || actor instanceof GameObject){//is overhead of NPC
            switch (japanesePlugin.config.npcDialogueConfig()) {
                case そのまま:
                    return transformOptions.doNothing;
                case 簡易翻訳:
                    return transformOptions.wordToWord;
                case DeepL翻訳:

                    return transformOptions.API;
            }
        }

        String name = actor.getName();
        if(name != null) {
            name = japWidgets.removeTag(name);
            if (name.equals(client.getLocalPlayer().getName())) {
                switch (japanesePlugin.config.selfConfig()) {
                    case そのまま表示:
                        return transformOptions.doNothing;
                    case ローマ字変換:
                        return transformOptions.alpToJap;
                }
            }
            if (client.isFriended(name, true)) {
                switch (japanesePlugin.config.friendConfig()) {
                    case そのまま表示:
                        return transformOptions.doNothing;
                    case ローマ字変換:
                        return transformOptions.alpToJap;
                    case 簡易翻訳:
                        return transformOptions.wordToWord;
                    case DeepL翻訳:
                        return transformOptions.API;
                }
            }
            switch (japanesePlugin.config.publicConfig()) {
                case そのまま表示:
                    return transformOptions.doNothing;
                case ローマ字変換:
                    return transformOptions.alpToJap;
                case 簡易翻訳:
                    return transformOptions.wordToWord;
                case DeepL翻訳:
                    return transformOptions.API;
            }
        }
        return transformOptions.doNothing;
    }
    public void modifyChat(ChatMessage chatMessage) throws Exception {//for chat box
        transformOptions tranOp;

        MessageNode node = chatMessage.getMessageNode();
        log.info("message type = '" + chatMessage.getType() + "' message ='" +
                node.getValue() + "', sender = '" + chatMessage.getSender() +
                "', name=" + chatMessage.getName());

        switch (chatMessage.getType()) {
            case MESBOX:
                //log.info("got a MESBOX");
                return;
            case DIALOG:
            default:
                tranOp = getChatConfig(chatMessage);
        }
        if (tranOp == transformOptions.doNothing)
            return;

        if (chatMessage.getMessageNode().getValue().startsWith("To talk in your Iron Group's channel, start each line of chat with " )) {
            String GIMName = client.getClanChannel(ClanID.GROUP_IRONMAN).getName();
            if (GIMName != null)
                chatMessage.getMessageNode().setSender(GIMName);
            else {
                String clanName = client.getClanChannel(ClanID.CLAN).getName();
                if (clanName != null)
                    chatMessage.getMessageNode().setSender(clanName);
            }
        }

        String translatedTextWithColors = translateMessage(chatMessage, tranOp);
        String examineText = "";
        switch (chatMessage.getType()) {
            case ITEM_EXAMINE:
            case NPC_EXAMINE:
            case OBJECT_EXAMINE:
                examineText = "<br>" + examineStringImage + lastExamined;
        }

        String withBr = insertBr(translatedTextWithColors, chatMessage);

        MessageNode messageNode = chatMessage.getMessageNode();
        messageNode.setRuneLiteFormatMessage(withBr + examineText);
        client.refreshChat();

    }

    private String translateMessage(ChatMessage chatMessage, transformOptions option) throws Exception {
        MessageNode messageNode = chatMessage.getMessageNode();
        String message = messageNode.getValue();
        String name = messageNode.getName();
        name = japWidgets.removeTag(name);
        String chatName = messageNode.getSender();

        log.info("message = '" + message+"', name = '" + name + "', chat name = '" + chatName);

        message = japWidgets.removeTag(message);//preprocess the text
        String colorHex = getChatColor(chatMessage);
        String enWithColors = "<col=" + colorHex + ">" + message;

        ChatIconManager iconManager = japanesePlugin.getChatIconManager();
        HashMap<String, Integer> map = japanesePlugin.getJapCharIds();
        JapTransforms jt = japanesePlugin.getJapTransforms();

        if (option == transformOptions.API && !japanesePlugin.getJapTransforms().knownAPI.containsKey(message.toLowerCase())) {
            String finalName = name;
            Thread thread = new Thread(() -> {//process with new thread because games freezes while waiting for api response
                try {
                    String ret;
                    if (finalName.isEmpty())//not sent by player
                        ret = jt.getTransformWithColors(enWithColors, option, map, iconManager, true);
                    else // sent by player, so don't add to dict nor send webhook
                        ret = jt.getTransformWithColors(enWithColors, option, map, iconManager, false);
                    //translatingAPI = false;
                    String withBr = insertBr(ret, chatMessage);

                    MessageNode messageNode2 = chatMessage.getMessageNode();
                    messageNode2.setRuneLiteFormatMessage(withBr);
                    client.refreshChat();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            thread.setDaemon(false);
            thread.start();
            return message;
        }else
            return jt.getTransformWithColors(enWithColors, option, map, iconManager);
    }
    private transformOptions getChatConfig(ChatMessage chatMessage) {//todo:read from config
        String name = japWidgets.removeTag(chatMessage.getName());
        if (isInStringArray(name, japanesePlugin.config.playerListDoNothing().split(",")))
            return transformOptions.doNothing;
        else if (isInStringArray(name, japanesePlugin.config.playerListAPI().split(",")))
            return transformOptions.API;
        else if (isInStringArray(name, japanesePlugin.config.playerListRom2Jap().split(",")))
            return transformOptions.alpToJap;
        else if (isInStringArray(name, japanesePlugin.config.playerListWord2Word().split(",")))
            return transformOptions.wordToWord;

        //if its by the player
        if (Objects.equals(name, client.getLocalPlayer().getName()))
            switch (japanesePlugin.config.selfConfig()) {
                case そのまま表示:
                    return transformOptions.doNothing;
                case ローマ字変換:
                    return transformOptions.alpToJap;
            }
        //if its from a friend
        boolean isFriend = client.isFriended(name,true);
        if (isFriend)
            switch (japanesePlugin.config.friendConfig()) {
                case ローマ字変換:
                    return transformOptions.alpToJap;
                case そのまま表示:
                    return transformOptions.doNothing;
                case 簡易翻訳:
                    return transformOptions.wordToWord;
                case DeepL翻訳:
                    return transformOptions.API;
            }
        switch (chatMessage.getType()){
            case PUBLICCHAT:
                return getChatsChatConfig(japanesePlugin.config.publicConfig());
            case CLAN_CHAT:
                return getChatsChatConfig(japanesePlugin.config.clanConfig());
            case CLAN_GUEST_CHAT:
                return getChatsChatConfig(japanesePlugin.config.clanGuestConfig());
            case FRIENDSCHAT:
                return getChatsChatConfig(japanesePlugin.config.friendChatConfig());
            case CLAN_GIM_CHAT:
                if (!Objects.equals(name, "null") && !name.isEmpty())
                    return getChatsChatConfig(japanesePlugin.config.gimConfig());

            default:
                switch (japanesePlugin.config.gameMessageConfig()) {
                    case そのまま:
                        return transformOptions.doNothing;
                    case 簡易翻訳:
                        return transformOptions.wordToWord;
                    case DeepL翻訳:
                        return transformOptions.API;
                }
        }
        return JapTransforms.transformOptions.wordToWord;
    }
    private transformOptions getChatsChatConfig(JapaneseConfig.ChatConfig chatConfig) {
        switch (chatConfig) {
            case ローマ字変換:
                return transformOptions.alpToJap;
            case そのまま表示:
                return transformOptions.doNothing;
            case 簡易翻訳:
                return transformOptions.wordToWord;
            case DeepL翻訳:
                return transformOptions.API;
            default:
                switch (japanesePlugin.config.gameMessageConfig()) {
                    case DeepL翻訳:
                        return transformOptions.API;
                    case 簡易翻訳:
                        return transformOptions.wordToWord;
                    default:
                        return transformOptions.doNothing;
                }
        }
    }
    private boolean isInStringArray(String item, String[] array) {
        for (String s:array)
            if (item.equals(s.trim()))
                return true;
        return false;
    }
    private String getChatColor(ChatMessage chatMessage) {
        return Colors.black.getHex();
    }

    private String insertBr(String str, ChatMessage chatMessage) {
        MessageNode messageNode = chatMessage.getMessageNode();
        String name = messageNode.getName();
        String chatName = messageNode.getSender();
        int nameCharCount = replaceTagWithAA(name).length()+2; // +2 because of ": " after name
        int chatNameCount = (chatName == null ? 0:chatName.length()+4); //+2 because of [] brackets
        int enCharCount = nameCharCount + chatNameCount + 8; //+8 because timestamp is probably on
        double enWidth = 5.8; //width of 1 en character
        double jpWidth = 13.1; //width of 1 <img=> character
        int chatWidth = 486;
        int width = chatWidth - (int) (enCharCount*enWidth+2); //-2 just to be safe

        String regex = "(<img=\\d+>)|.";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);

        StringBuilder stringBuilder = new StringBuilder();
        double wLeft = width;
        while(matcher.find()){
            String c = matcher.group();
            if (c.matches("<img=\\d+>"))
                wLeft -= jpWidth;
            else
                wLeft -= enWidth;
            if (wLeft - jpWidth < 0){
                wLeft = width;
                stringBuilder.append("<br>");
                stringBuilder.append(c);
            } else {
                stringBuilder.append(c);
            }
        }
        return stringBuilder.toString();
    }

    private String replaceTagWithAA (String string){ //"<img=41>sand in sand" into "11sand in sand" for easy counting
        return string.replaceAll("<img=(\\d+)>","AA");
    }
}
