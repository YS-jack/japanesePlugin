package com.japanese;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MessageNode;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.widgets.ComponentID;

import com.japanese.JapanesePlugin;
import com.japanese.JapTransforms.transformOptions;
import com.japanese.JapWidgets;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.game.ChatIconManager;
import net.runelite.api.clan.ClanID;

import javax.inject.Inject;
import java.awt.*;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.floor;

@Slf4j
public class ChatModifier {
    @Inject
    private JapanesePlugin japanesePlugin;
    @Inject
    @Setter
    private JapTransforms japTransforms = new JapTransforms();

    @Inject
    private JapWidgets japWidgets = new JapWidgets();
    @Inject
    private ChatMessageManager chatMessageManager;
    @Inject
    private Client client;

    public void modifyChat(ChatMessage chatMessage) {
        switch (chatMessage.getType()) {
            case MESBOX:
                //log.info("got a MESBOX");
                return;
            case DIALOG:
                //log.info("got a DIALOG");
                return;
        }
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

        String translatedTextWithColors = translateMessage(chatMessage);
        String withBr = insertBr(translatedTextWithColors, chatMessage);

        MessageNode messageNode = chatMessage.getMessageNode();
        messageNode.setRuneLiteFormatMessage(withBr);
        client.refreshChat();
    }

    private String translateMessage(ChatMessage chatMessage) {
        MessageNode messageNode = chatMessage.getMessageNode();
        String message = messageNode.getValue();
        String name = messageNode.getName();
        String chatName = messageNode.getSender();

        log.info("message = '" + message+"', name = '" + name + "', chat name = '" + chatName);

        message = japWidgets.removeTag(message);//preprocess the text
        String colorHex = getChatColor(chatMessage);
        String enWithColors = "<col=" + colorHex + ">" + message;

        ChatIconManager iconManager = japanesePlugin.getChatIconManager();
        HashMap<String, Integer> map = japanesePlugin.getJapCharIds();
        final transformOptions option = getChatConfig(chatMessage);
        return japTransforms.getTransformWithColors(enWithColors, option, map, iconManager);
    }
    private JapTransforms.transformOptions getChatConfig(ChatMessage chatMessage) {//todo:read from config
        return JapTransforms.transformOptions.wordToWord;
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
