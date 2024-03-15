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

    @Inject
    public ChatInputOverlay(Client client, JapanesePlugin plugin) {
        setPosition(OverlayPosition.BOTTOM_LEFT);
        this.client = client;
        this.plugin = plugin;
    }
    @Override
    public Dimension render(Graphics2D graphics) {
        int msgLength = plugin.romToJap.inputCount;
        String jpMsg = plugin.romToJap.chatJpMsg;

        if (msgLength == 0) return null;

        panelComponent.getChildren().clear();

        // Set the size of the overlay
        panelComponent.setPreferredSize(new Dimension(Math.min(14*(msgLength + 1)+8*5,inputWidth),0));

        Color bgColor = new Color(127, 82, 33);
        panelComponent.setBackgroundColor(bgColor);
        if(getLen(jpMsg) > inputWidth) {
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
        return str.length()*14;
    }

    private String[] splitMsg(String string) {//splits message into 2, first with length of chat input width, second (and third if needed) with remaining
        String[] ret = {
                string.substring(0, string.length() / 2),  // First half
                string.substring(string.length() / 2)       // Second half
        };
        return ret;
    }
}
