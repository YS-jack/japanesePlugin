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
class APICountOverlay  extends Overlay {
    private Client client;
    private JapaneseConfig config;
    private JapanesePlugin plugin;
    private final PanelComponent panelComponent = new PanelComponent();
    @Inject
    ApiTranslate apiTranslate;

    @Inject
    public APICountOverlay(Client client, JapanesePlugin plugin, JapaneseConfig config) {
        setPosition(OverlayPosition.BOTTOM_RIGHT);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
    }
    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.DeeplAPICount() && !config.GoogleAPICount() && !config.AzureAPICount())
            return null;

        int enCharSize = 8;
        String deeplCount = Long.toString(plugin.getApiTranslate().deeplCount);
        String deeplLimit = Long.toString(plugin.getApiTranslate().deeplLimit);
        String googleCount = Long.toString(plugin.getApiTranslate().googleCount);
        String googleLimit = Long.toString(plugin.getApiTranslate().googleLimit);
        String azureCount = Long.toString(plugin.getApiTranslate().azureCount);
        String azureLimit = Long.toString(plugin.getApiTranslate().azureLimit);

        Color bgColor = new Color(80, 148, 144);
        panelComponent.setBackgroundColor(bgColor);
        panelComponent.getChildren().clear();
        if (config.DeeplAPICount())
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("DeepL:")
                    .right(deeplCount + " / " + deeplLimit)
                    .build());

        if (config.GoogleAPICount())
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Google Cloud:")
                    .right(googleCount + " / " + googleLimit)
                    .build());

        if (config.AzureAPICount())
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Azure AI:")
                    .right(azureCount + " / " + azureLimit)
                    .build());

        int max = (Math.max(Math.max(deeplCount.length() + deeplLimit.length(), googleCount.length() + googleLimit.length()),
                azureCount.length() + azureLimit.length()));

        panelComponent.setPreferredSize(new Dimension((max + 3) *enCharSize*2,0));
        return panelComponent.render(graphics);
    }


}
