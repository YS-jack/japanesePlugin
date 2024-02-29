package com.japanese;
import com.japanese.JapaneseConfig.GameTextProcessChoice;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.api.WorldType;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;
import java.awt.*;
import java.util.EnumSet;

@Slf4j
@ParametersAreNonnullByDefault
class JapaneseOverlay extends Overlay //remove abstract when actually making overlays with this
{
    private Client client;
    private JapaneseConfig config;
    private JapanesePlugin plugin;
    private final PanelComponent panelComponent = new PanelComponent();

    @Inject
    private JapaneseOverlay(Client client, JapaneseConfig config, JapanesePlugin plugin) {
        setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
        this.client = client;
        this.config = config;
        this.plugin = plugin;
    }
//
    @Override
    public Dimension render(Graphics2D graphics) {
        if (config.npcDialogueConfig() == GameTextProcessChoice.そのまま
                || plugin.dialogueNPC.isEmpty()
                || plugin.dialogueText == null){
            panelComponent.getChildren().clear();
            return panelComponent.render(graphics);
        }
//        else {
//            panelComponent.getChildren().clear();
//            String overlayTitle = "NPC Dialog";
//            // Build overlay title
//            panelComponent.getChildren().add(TitleComponent.builder()
//                    .text(plugin.dialogueNPC)
//                    .color(Color.RED)
//                    .build());
//
//            // Set the size of the overlay (width)
//            panelComponent.setPreferredSize(new Dimension(
//                    graphics.getFontMetrics().stringWidth(overlayTitle) + 30,
//                    0));
//
//            // Add a line on the overlay for world number
//            panelComponent.getChildren().add(LineComponent.builder()
//                    .right(plugin.dialogueText)
//                    .rightColor(Color.black)
//                    .build());
//            Color bgColor = new Color(210,193,156);
//            panelComponent.setBackgroundColor(bgColor);
//            // Show world type goes here ...
//            EnumSet<WorldType> worldType = client.getWorldType();
//            String currentWorldType;
//
////            if (worldType.contains(WorldType.MEMBERS)) {
////                currentWorldType = "Members";
////            } else {
////                currentWorldType = "Free";
////            }
////
////            panelComponent.getChildren().add(LineComponent.builder()
////                    .left("Type:")
////                    .right(currentWorldType)
////                    .build());

            return panelComponent.render(graphics);
//        }
    }
}
