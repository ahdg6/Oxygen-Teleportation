package austeretony.oxygen_teleportation.client.gui.teleportation.camps.context;

import austeretony.alternateui.screen.contextmenu.AbstractContextAction;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen.client.api.OxygenHelperClient;
import austeretony.oxygen.client.core.api.ClientReference;
import austeretony.oxygen_teleportation.client.gui.teleportation.CampsGUISection;

public class EditContextAction extends AbstractContextAction {

    private CampsGUISection section;

    public EditContextAction(CampsGUISection section) {
        this.section = section;
    }

    @Override
    protected String getName(GUIBaseElement currElement) {
        return ClientReference.localize("teleportation.gui.menu.edit");
    }

    @Override
    public boolean isValid(GUIBaseElement currElement) {
        return this.section.getCurrentPoint().isOwner(OxygenHelperClient.getPlayerUUID());
    }

    @Override
    public void execute(GUIBaseElement currElement) {
        this.section.openPointEditingCallback();
    }
}