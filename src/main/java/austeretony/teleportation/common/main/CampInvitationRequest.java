package austeretony.teleportation.common.main;

import java.util.UUID;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.api.notification.AbstractNotification;
import austeretony.oxygen.common.core.api.CommonReference;
import austeretony.oxygen.common.notification.EnumNotifications;
import austeretony.teleportation.common.TeleportationManagerServer;
import austeretony.teleportation.common.config.TeleportationConfig;
import net.minecraft.entity.player.EntityPlayer;

public class CampInvitationRequest extends AbstractNotification {

    public final int index;

    public final String ownerUsername, campName;

    public final UUID ownerUUID;

    public final long pointId;

    public CampInvitationRequest(int index, UUID ownerUUID, String ownerUsername, long pointId, String campName) {
        this.index = index;
        this.ownerUUID = ownerUUID;
        this.ownerUsername = ownerUsername;
        this.pointId = pointId;
        this.campName = campName;
    }

    @Override
    public EnumNotifications getType() {
        return EnumNotifications.REQUEST;
    }

    @Override
    public String getDescription() {
        return "teleportation.invitation.request";
    }

    @Override
    public String[] getArguments() {
        return new String[]{this.ownerUsername, this.campName};
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public int getExpireTime() {
        return TeleportationConfig.INVITATION_REQUEST_EXPIRE_TIME.getIntValue();
    }

    @Override
    public void accepted(EntityPlayer player) {
        UUID targetUUID = CommonReference.uuid(player);
        TeleportationManagerServer.instance().getSharedCampsManager().invite(this.ownerUUID, this.pointId, targetUUID);
        OxygenHelperServer.saveWorldDataDelegated(TeleportationManagerServer.instance().getSharedCampsManager());

        OxygenHelperServer.addObservedPlayer(this.ownerUUID, targetUUID, true);

        OxygenHelperServer.sendMessage(player, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessages.INVITATION_REQUEST_ACCEPTED.ordinal(), this.ownerUsername, this.campName);
        if (OxygenHelperServer.isOnline(this.ownerUUID))
            OxygenHelperServer.sendMessage(CommonReference.playerByUUID(this.ownerUUID), TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessages.INVITATION_REQUEST_ACCEPTED_OWNER.ordinal(), this.campName, CommonReference.username(player));
        OxygenHelperServer.setRequesting(this.ownerUUID, false);
    }

    @Override
    public void rejected(EntityPlayer player) {
        OxygenHelperServer.sendMessage(player, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessages.INVITATION_REQUEST_REJECTED.ordinal(), this.ownerUsername, this.campName);
        if (OxygenHelperServer.isOnline(this.ownerUUID))
            OxygenHelperServer.sendMessage(CommonReference.playerByUUID(this.ownerUUID), TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessages.INVITATION_REQUEST_REJECTED_OWNER.ordinal(), this.campName, CommonReference.username(player));
        OxygenHelperServer.setRequesting(this.ownerUUID, false);
    }

    @Override
    public void expired() {
        OxygenHelperServer.setRequesting(this.ownerUUID, false);
    }
}
