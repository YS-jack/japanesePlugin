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
        if (!config.DeeplAPICount() || !config.useDeepl())// && !config.GoogleAPICount() && !config.AzureAPICount())
            return null;

        int enCharSize = 8;
        int jpCharSize = 15;
        boolean deeplKeyValid = plugin.getApiTranslate().keyValid;
        String deeplCount = Long.toString(plugin.getApiTranslate().deeplCount);
        String deeplLimit = Long.toString(plugin.getApiTranslate().deeplLimit);
//        String googleCount = Long.toString(plugin.getApiTranslate().googleCount);
//        String googleLimit = Long.toString(plugin.getApiTranslate().googleLimit);
//        String azureCount = Long.toString(plugin.getApiTranslate().azureCount);
//        String azureLimit = Long.toString(plugin.getApiTranslate().azureLimit);

        Color bgColorCount = new Color(80, 148, 144);
        Color bgColorInvalid = new Color(194, 93, 93);
        panelComponent.getChildren().clear();
        int len;
        if (deeplKeyValid) {
            panelComponent.setBackgroundColor(bgColorCount);
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("DeepL:")
                    .right(deeplCount + " / " + deeplLimit)
                    .build());
            len = (deeplLimit.length()*2+10)*enCharSize;
        } else {
            panelComponent.setBackgroundColor(bgColorInvalid);
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("APIキーが無効です。\n有効なキーが入力されるまでは\n簡易翻訳が行われます。APIキーが有効であることと\n上限に到達していないことを確認してください")
                    .build());
            len = ("簡易翻訳が行われます。APIキーが有効であることと".length()+2)*jpCharSize;
        }
        panelComponent.setPreferredSize(new Dimension(len,0));
        return panelComponent.render(graphics);
    }


}
