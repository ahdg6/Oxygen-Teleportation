package austeretony.teleportation.client.gui.menu.camps.callback;

import java.util.UUID;

import austeretony.alternateui.screen.browsing.GUIScroller;
import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.button.GUISlider;
import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.panel.GUIButtonPanel;
import austeretony.alternateui.screen.panel.GUIButtonPanel.GUIEnumOrientation;
import austeretony.alternateui.screen.text.GUITextLabel;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.oxygen.common.api.OxygenHelperClient;
import austeretony.oxygen.common.main.OxygenMain;
import austeretony.oxygen.common.main.OxygenPlayerData;
import austeretony.oxygen.common.main.SharedPlayerData;
import austeretony.teleportation.client.TeleportationManagerClient;
import austeretony.teleportation.client.gui.menu.CampsGUISection;
import austeretony.teleportation.client.gui.menu.TeleportationMenuGUIScreen;
import austeretony.teleportation.client.gui.menu.camps.InvitedPlayerGUIButton;
import austeretony.teleportation.common.config.TeleportationConfig;
import net.minecraft.client.resources.I18n;

public class InvitationsGUICallback extends AbstractGUICallback {

    private final TeleportationMenuGUIScreen screen;

    private final CampsGUISection section;

    private GUIButtonPanel invitedPlayersPanel;

    private GUIButton cancelButton;

    public InvitationsGUICallback(TeleportationMenuGUIScreen screen, CampsGUISection section, int width, int height) {
        super(screen, section, width, height);
        this.screen = screen;
        this.section = section;
    }

    @Override
    public void init() {
        this.addElement(new InvitationsBackgroundGUIFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new GUITextLabel(2, 2).setDisplayText(I18n.format("teleportation.gui.menu.invitationsCallback"), true, GUISettings.instance().getTitleScale()));               

        this.invitedPlayersPanel = new GUIButtonPanel(GUIEnumOrientation.VERTICAL, 0, 12, 137, 10).setButtonsOffset(1).setTextScale(GUISettings.instance().getPanelTextScale());
        this.addElement(this.invitedPlayersPanel);
        GUIScroller scroller = new GUIScroller(TeleportationConfig.MAX_INVITED_PLAYERS_PER_CAMP.getIntValue(), 5);
        this.invitedPlayersPanel.initScroller(scroller);
        GUISlider slider = new GUISlider(this.getX() + 138, this.getY() + 12, 2, 54);//TODO Fix this mess
        slider.setDynamicBackgroundColor(GUISettings.instance().getEnabledSliderColor(), GUISettings.instance().getDisabledSliderColor(), GUISettings.instance().getHoveredSliderColor());
        scroller.initSlider(slider);

        this.addElement(this.cancelButton = new GUIButton(this.getWidth() - 55, this.getHeight() - 12, 40, 10).enableDynamicBackground().setDisplayText(I18n.format("teleportation.gui.closeButton"), true, GUISettings.instance().getButtonTextScale()));
    }

    private OxygenPlayerData.EnumStatus getPlayerStatus(SharedPlayerData playerData) {
        return OxygenPlayerData.EnumStatus.values()[playerData.getData(OxygenMain.STATUS_DATA_ID).get(0)];
    }

    private void updatePlayers() {
        this.invitedPlayersPanel.reset();
        InvitedPlayerGUIButton button;
        for (UUID playerUUID : TeleportationManagerClient.instance().getSharedCampsManager().getInvitedPlayers(this.section.getCurrentPoint().getId())) {
            button = new InvitedPlayerGUIButton(playerUUID, this.section.getCurrentPoint().getId());
            button.enableDynamicBackground(GUISettings.instance().getEnabledElementColor(), GUISettings.instance().getEnabledElementColor(), GUISettings.instance().getHoveredElementColor());
            button.setTextDynamicColor(GUISettings.instance().getEnabledTextColor(), GUISettings.instance().getDisabledTextColor(), GUISettings.instance().getHoveredTextColor());
            button.setDisplayText(OxygenHelperClient.getObservedSharedData(playerUUID).getUsername());
            button.setTextAlignment(EnumGUIAlignment.LEFT, 2);
            this.invitedPlayersPanel.addButton(button);
        }
    }

    @Override
    protected void onOpen() {
        this.updatePlayers();
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (element == this.cancelButton)
            this.close();
    }
}
