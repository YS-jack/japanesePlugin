package com.japanese;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;
import java.awt.*;

@Slf4j
@ParametersAreNonnullByDefault
class ChatInputOverlay extends Overlay //remove abstract when actually making overlays with this
{
    private Client client;
    private JapaneseConfig config;
    private JapanesePlugin plugin;
    private final PanelComponent panelComponent = new PanelComponent();
    private int[] ovlPos;
    private int inputWidth = 400;
    private int japCharSize = 14; // px width of each japanese characters
    private int enCharSize = 8;
    @Inject
    public ChatInputOverlay(Client client, JapanesePlugin plugin) {
        setPosition(OverlayPosition.BOTTOM_LEFT);
        this.client = client;
        this.plugin = plugin;
    }
    @Override
    public Dimension render(Graphics2D graphics) {
        int msgLength = plugin.getRomToJap().inputCount;
        String jpMsg = plugin.getRomToJap().chatJpMsg;

        if (msgLength == 0 || plugin.getJapWidgets().displayDialog || plugin.config.selfConfig().equals(JapaneseConfig.ChatConfigSelf.そのまま表示)) return null;

        panelComponent.getChildren().clear();

        // Set the size of the overlay
        panelComponent.setPreferredSize(new Dimension(Math.min(japCharSize*(msgLength + 1)+enCharSize*5,inputWidth),0));
        Color bgColor = new Color(127, 82, 33);
        panelComponent.setBackgroundColor(bgColor);
        if(getLen(jpMsg) +japCharSize*4 +enCharSize*10> inputWidth) {
            String[] newMsgs = splitMsg(jpMsg);
            for (int i = 0;i < newMsgs.length; i++) {
                if (i == newMsgs.length - 1) {
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left(newMsgs[i])
                            .right("(" + Integer.toString(msgLength) + "\n/80)")
                            .build());
                } else {
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left(newMsgs[i])
                            .build());
                }
            }
        } else {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left(jpMsg)
                    .right(" (" + Integer.toString(msgLength) + "\n/80)")
                    .build());
        }
        return panelComponent.render(graphics);
    }

    private int getLen(String str) {
        return str.length()*japCharSize;
    }

    private String[] splitMsg(String str) {//splits message
        int chunkSize = inputWidth/japCharSize-4;
        int length = str.length();
        int numOfChunks = (int) Math.ceil((double) length / chunkSize);
        String[] chunks = new String[numOfChunks];

        // Split the string into chunks
        for (int i = 0; i < numOfChunks; i++) {
            int startIndex = i * chunkSize;
            int endIndex = Math.min(startIndex + chunkSize, length);
            chunks[i] = str.substring(startIndex, endIndex);
        }

        return chunks;
    }
}
