package com.japanese;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;


import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;
import java.awt.*;
import java.util.List;

import com.japanese.JapTransforms;

@Slf4j
@ParametersAreNonnullByDefault
class ChatOptionOverlay  extends Overlay {
    private Client client;
    private JapaneseConfig config;
    private JapanesePlugin plugin;
    private final PanelComponent panelComponent = new PanelComponent();
    private JapTransforms.transformOptions transformOptions;
    private int japCharSize = 16; // px width of each japanese characters
    private int enCharSize = 9;
    @Inject
    public ChatOptionOverlay(Client client, JapanesePlugin plugin, JapaneseConfig config) {
        setPosition(OverlayPosition.BOTTOM_RIGHT);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
    }
    @Override
    public Dimension render(Graphics2D graphics) {
        if (!plugin.getJapWidgets().displayDialog ||
            config.npcDialogueConfig() == JapaneseConfig.GameTextProcessChoice.そのまま)
            return null;

        panelComponent.getChildren().clear();
        Color bgColor = new Color(127, 82, 33);
        panelComponent.setBackgroundColor(bgColor);
        List<Widget> widgetList = plugin.getJapWidgets().dialogOptionWidgets;
        int maxLen = 0;
        for (int i = 0; i < widgetList.size();i++) {
            Widget w = widgetList.get(i);
            String enString = w.getText();
            if (enString.isEmpty())
                continue;
            try {
                transformOptions = plugin.getJapWidgets().getWidgetTransformConfig(w);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            String jpString = "";
            try {
                switch (transformOptions) {
                    case API:
                        jpString = plugin.getApiTranslate().getDeepl(enString, "", "", true);
                        break;
                    case wordToWord:
                        jpString = plugin.getJapTransforms().getW2WTranslation(enString);
                        break;
                    default:
                        jpString = enString;
                        break;
                }
            } catch (Exception e) {}
            if (i == 0)
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("＜ " + jpString + "＞")
                        .build());
            else
                panelComponent.getChildren().add(LineComponent.builder()
                        .left(i + " : " + jpString)
                        .build());
            int panelString = (Integer.toString(i).length() + 3) *enCharSize + (4+jpString.length()*japCharSize);
            if (maxLen < panelString)
                maxLen = panelString;
        }
        panelComponent.setPreferredSize(new Dimension((maxLen + 3),0));
        return panelComponent.render(graphics);
    }


}
