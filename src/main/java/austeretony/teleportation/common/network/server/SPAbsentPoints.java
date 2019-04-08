package austeretony.teleportation.common.network.server;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.oxygen.common.reference.CommonReference;
import austeretony.teleportation.common.TeleportationManagerServer;
import austeretony.teleportation.common.main.TeleportationMain;
import austeretony.teleportation.common.network.client.CPCommand;
import austeretony.teleportation.common.network.client.CPSyncInvitedPlayers;
import austeretony.teleportation.common.network.client.CPSyncWorldPoints;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class SPAbsentPoints extends ProxyPacket {

    private long[] absentCamps, absentLocations;

    private int campsSize, locationsSize;

    public SPAbsentPoints() {}

    public SPAbsentPoints(int campsSize, long[] absentCamps, int locationsSize, long[] absentLocations) {
        this.campsSize = campsSize;
        this.absentCamps = absentCamps;
        this.locationsSize = locationsSize;
        this.absentLocations = absentLocations;
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        buffer.writeInt(this.campsSize);
        for (long id : this.absentCamps) {
            if (id == 0) break;
            buffer.writeLong(id);
        }
        buffer.writeInt(this.locationsSize);
        for (long id : this.absentLocations) {
            if (id == 0) break;
            buffer.writeLong(id);
        }
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        EntityPlayerMP playerMP = getEntityPlayerMP(netHandler);
        this.absentCamps = new long[buffer.readInt()];
        int i = 0;
        if (this.absentCamps.length > 0) {
            for (; i < this.absentCamps.length; i++)
                this.absentCamps[i] = buffer.readLong(); 
            TeleportationMain.network().sendTo(new CPSyncWorldPoints(WorldPoint.EnumWorldPoints.CAMP, this.absentCamps), playerMP);
            TeleportationManagerServer.instance().getImagesLoader().loadAndSendCampPreviewImagesDelegated(playerMP, this.absentCamps);
            if (!TeleportationManagerServer.instance().getPlayerProfile(CommonReference.uuid(playerMP)).getSharedCamps().isEmpty())
                TeleportationMain.network().sendTo(new CPSyncInvitedPlayers(), playerMP);
        }
        this.absentLocations = new long[buffer.readInt()];
        if (this.absentLocations.length > 0) {
            i = 0;
            for (; i < this.absentLocations.length; i++)
                this.absentLocations[i] = buffer.readLong(); 
            TeleportationMain.network().sendTo(new CPSyncWorldPoints(WorldPoint.EnumWorldPoints.LOCATION, this.absentLocations), playerMP);
            TeleportationManagerServer.instance().getImagesManager().downloadLocationPreviewsToClientDelegated(playerMP, this.absentLocations);
        }
        OxygenHelperServer.syncPlayersData(playerMP, TeleportationMain.JUMP_PROFILE_DATA_ID);
        TeleportationMain.network().sendTo(new CPCommand(CPCommand.EnumCommand.OPEN_MENU), playerMP);
        TeleportationManagerServer.instance().getPlayerProfile(CommonReference.uuid(playerMP)).setSyncing(false);
    }
}
