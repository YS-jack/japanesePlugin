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
    private int enCharSize = 8;
    private final int candListMax = 7;//max vert number of words


    @Inject
    public KatKanjCandiOvl(Client client, JapanesePlugin plugin) {
        setPosition(OverlayPosition.BOTTOM_LEFT);
        this.client = client;
        this.plugin = plugin;
    }
    @Override
    public Dimension render(Graphics2D graphics) {
        String[] jpMsg = plugin.getRomToJap().kanjKatCandidates.toArray(new String[0]);
        int candSelectN = plugin.getRomToJap().instCandidateSelection;
        int msgCount = plugin.getRomToJap().inputCount;
        if (msgCount == 0) return null;
        if (jpMsg.length == 1 && jpMsg[0].matches("[^\\p{IsAlphabetic}\\p{IsHiragana}\\p{IsKatakana}]+")) return  null;
        panelComponent.getChildren().clear();
        int panelN = jpMsg.length/candListMax + 1;

        Color bgColor = new Color(127, 82, 33);
        panelComponent.setBackgroundColor(bgColor);

        int[] panelWordLen = new int[panelN];
        int panelWidth = 0;
        for (int j = 0; j < panelN; j++) {
            for(int i = 0; i < candListMax && i + j * candListMax < jpMsg.length; i++) {
                if (jpMsg[i + j * candListMax].length() > panelWordLen[j]) {
                    String word = jpMsg[i + j * candListMax].split("\\d")[0];
                    panelWordLen[j] = word.length();
                }
            }
            panelWidth += panelWordLen[j] * japCharSize;
        }
        panelWidth += japCharSize*2*panelN + enCharSize*3*panelN + enCharSize*2*(panelN-1);
        //if (panelN > 1)
         //   panelWidth += japCharSize*(panelN-1);
        for(int i = 0; i < candListMax; i++) {
            StringBuilder jp = new StringBuilder();
            String numbering;

            for (int j = 0; j < panelN; j++) {

                if (i + j * candListMax < jpMsg.length) {
                    numbering = Integer.toString(i + j * candListMax) + "  ";
                    if (i + j * candListMax == candSelectN) 
                        numbering = "＞" + numbering;
                    else
                        numbering = "＿" + numbering;
                    if (j > 0) {
                        jp.append("　");

                    }
                    jp.append(numbering);

                    String word = jpMsg[i + j * candListMax].split("\\d")[0];
                    jp.append(word);

                    int w = panelWordLen[j] - jpMsg[i + j * candListMax].length();
                    jp.append("　".repeat(Math.max(0, w)));
                }
            }
            panelComponent.getChildren().add(LineComponent.builder()
                    .left(jp.toString().trim())
                    .build());
        }
        // Set the size of the overlay
        panelComponent.setPreferredSize(new Dimension(panelWidth,0));
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
