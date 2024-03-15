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
class KatKanjCandiOvl extends Overlay //remove abstract when actually making overlays with this
{
    private Client client;
    private JapaneseConfig config;
    private JapanesePlugin plugin;
    private final PanelComponent panelComponent = new PanelComponent();
    private int[] ovlPos;
    private int inputWidth = 400;
    private int japCharSize = 14; // px width of each japanese characters


    @Inject
    public KatKanjCandiOvl(Client client, JapanesePlugin plugin) {
        setPosition(OverlayPosition.BOTTOM_LEFT);
        this.client = client;
        this.plugin = plugin;
    }
    @Override
    public Dimension render(Graphics2D graphics) {
        String[] jpMsg = plugin.romToJap.kanjKatCandidates.toArray(new String[plugin.romToJap.kanjKatCandidates.size()]);
        int candSelectN = plugin.romToJap.instCandidateSelection;
        int msgCount = plugin.romToJap.inputCount;
        if (msgCount == 0) return null;

        panelComponent.getChildren().clear();



        Color bgColor = new Color(127, 82, 33);
        Color bgSelectedColor = new Color(213, 161, 98);
        panelComponent.setBackgroundColor(bgColor);
        int maxWidth = 0;
        for(int i = 0; i < jpMsg.length; i++) {
            String jp = jpMsg[i];
            String numbering = Integer.toString(i);
            if (i == candSelectN)
                numbering = numbering + " < ";
            panelComponent.getChildren().add(LineComponent.builder()
                    .left(numbering)
                    .right(jp.trim())
                    .build());
            if (maxWidth < jp.trim().length()*japCharSize)
                maxWidth = jp.trim().length()*japCharSize;
        }

        // Set the size of the overlay
        panelComponent.setPreferredSize(new Dimension(maxWidth + japCharSize*3,0));
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
