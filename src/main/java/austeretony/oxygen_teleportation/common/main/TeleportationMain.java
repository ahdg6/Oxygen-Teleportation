package austeretony.oxygen_teleportation.common.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import austeretony.oxygen.client.api.OxygenGUIHelper;
import austeretony.oxygen.client.api.OxygenHelperClient;
import austeretony.oxygen.client.command.CommandOxygenClient;
import austeretony.oxygen.client.gui.OxygenGUITextures;
import austeretony.oxygen.client.sync.gui.api.ComplexGUIHandlerClient;
import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.api.network.OxygenNetwork;
import austeretony.oxygen.common.core.api.CommonReference;
import austeretony.oxygen.common.main.OxygenMain;
import austeretony.oxygen.common.privilege.api.Privilege;
import austeretony.oxygen.common.privilege.api.PrivilegeProviderServer;
import austeretony.oxygen.common.privilege.api.PrivilegedGroup;
import austeretony.oxygen.common.sync.gui.api.ComplexGUIHandlerServer;
import austeretony.oxygen_teleportation.client.TeleportToPlayerContextAction;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.TeleportationMenuHandlerClient;
import austeretony.oxygen_teleportation.client.command.TeleportationArgumentExecutorClient;
import austeretony.oxygen_teleportation.client.event.TeleportationEventsClient;
import austeretony.oxygen_teleportation.client.input.TeleportationKeyHandler;
import austeretony.oxygen_teleportation.common.TeleportationManagerServer;
import austeretony.oxygen_teleportation.common.TeleportationMenuHandlerServer;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.event.TeleportationEventsServer;
import austeretony.oxygen_teleportation.common.network.client.CPDownloadImagePart;
import austeretony.oxygen_teleportation.common.network.client.CPStartImageDownload;
import austeretony.oxygen_teleportation.common.network.client.CPSyncAdditionalData;
import austeretony.oxygen_teleportation.common.network.client.CPSyncCooldown;
import austeretony.oxygen_teleportation.common.network.client.CPSyncInvitedPlayers;
import austeretony.oxygen_teleportation.common.network.server.SPChangeJumpProfile;
import austeretony.oxygen_teleportation.common.network.server.SPCreateWorldPoint;
import austeretony.oxygen_teleportation.common.network.server.SPEditWorldPoint;
import austeretony.oxygen_teleportation.common.network.server.SPLeaveCampPoint;
import austeretony.oxygen_teleportation.common.network.server.SPLockPoint;
import austeretony.oxygen_teleportation.common.network.server.SPManageInvitation;
import austeretony.oxygen_teleportation.common.network.server.SPMoveToFavoriteCamp;
import austeretony.oxygen_teleportation.common.network.server.SPMoveToPlayer;
import austeretony.oxygen_teleportation.common.network.server.SPMoveToPoint;
import austeretony.oxygen_teleportation.common.network.server.SPRemoveWorldPoint;
import austeretony.oxygen_teleportation.common.network.server.SPSetFavoriteCamp;
import austeretony.oxygen_teleportation.common.network.server.SPStartImageUpload;
import austeretony.oxygen_teleportation.common.network.server.SPTeleportationRequest;
import austeretony.oxygen_teleportation.common.network.server.SPUploadImagePart;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(
        modid = TeleportationMain.MODID, 
        name = TeleportationMain.NAME, 
        version = TeleportationMain.VERSION,
        dependencies = "required-after:oxygen@[0.8.0,);",//TODO Always check required Oxygen version before build
        certificateFingerprint = "@FINGERPRINT@",
        updateJSON = TeleportationMain.VERSIONS_FORGE_URL)
public class TeleportationMain {

    public static final String 
    MODID = "oxygen_teleportation",    
    NAME = "Oxygen: Teleportation",
    VERSION = "0.8.0",
    VERSION_CUSTOM = VERSION + ":beta:0",
    GAME_VERSION = "1.12.2",
    VERSIONS_FORGE_URL = "https://raw.githubusercontent.com/AustereTony-MCMods/Oxygen-Teleportation/info/mod_versions_forge.json";

    public static final int 
    TELEPORTATION_MOD_INDEX = 1,//Oxygen - 0, Groups - 2, Exchange - 3, Merchants - 4, Players List - 5, Friends List - 6, Interaction - 7, Mail - 8, Chat - 9

    JUMP_PROFILE_SHARED_DATA_ID = 10,

    TELEPORTATION_REQUEST_ID = 10,
    INVITATION_TO_CAMP_ID = 11,

    TELEPORTATION_MENU_SCREEN_ID = 10;

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    private static OxygenNetwork network;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        OxygenHelperServer.registerConfig(new TeleportationConfig());
        if (event.getSide() == Side.CLIENT)
            CommandOxygenClient.registerArgumentExecutor(new TeleportationArgumentExecutorClient("teleportation", true));
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        this.initNetwork();
        TeleportationManagerServer.create();
        CommonReference.registerEvent(new TeleportationEventsServer());
        OxygenHelperServer.registerSharedDataValue(JUMP_PROFILE_SHARED_DATA_ID, Byte.BYTES);
        OxygenHelperServer.registerSharedDataIdentifierForScreen(TELEPORTATION_MENU_SCREEN_ID, OxygenMain.ACTIVITY_STATUS_SHARED_DATA_ID);
        OxygenHelperServer.registerSharedDataIdentifierForScreen(TELEPORTATION_MENU_SCREEN_ID, OxygenMain.DIMENSION_SHARED_DATA_ID);
        OxygenHelperServer.registerSharedDataIdentifierForScreen(TELEPORTATION_MENU_SCREEN_ID, JUMP_PROFILE_SHARED_DATA_ID);
        OxygenHelperServer.registerSharedDataIdentifierForScreen(50, JUMP_PROFILE_SHARED_DATA_ID);//50 - players list menu id
        OxygenHelperServer.registerSharedDataIdentifierForScreen(60, JUMP_PROFILE_SHARED_DATA_ID);//60 - friends list menu id
        ComplexGUIHandlerServer.registerScreen(TELEPORTATION_MENU_SCREEN_ID, new TeleportationMenuHandlerServer());
        if (event.getSide() == Side.CLIENT) {
            TeleportationManagerClient.create();
            CommonReference.registerEvent(new TeleportationEventsClient());
            CommonReference.registerEvent(new TeleportationKeyHandler());
            OxygenHelperClient.registerSharedDataValue(JUMP_PROFILE_SHARED_DATA_ID, Byte.BYTES);
            OxygenGUIHelper.registerScreenId(TELEPORTATION_MENU_SCREEN_ID);
            ComplexGUIHandlerClient.registerScreen(TELEPORTATION_MENU_SCREEN_ID, new TeleportationMenuHandlerClient());
            OxygenGUIHelper.registerContextAction(50, new TeleportToPlayerContextAction());
            OxygenGUIHelper.registerContextAction(60, new TeleportToPlayerContextAction());
            OxygenGUIHelper.registerContextAction(20, new TeleportToPlayerContextAction());//20 - group menu id
            OxygenHelperClient.registerNotificationIcon(TELEPORTATION_REQUEST_ID, OxygenGUITextures.MAP_PIN_ICONS);
            OxygenHelperClient.registerNotificationIcon(INVITATION_TO_CAMP_ID, OxygenGUITextures.REQUEST_ICONS);
        }
    }

    public static void addDefaultPrivileges() {
        if (!PrivilegeProviderServer.getGroup(PrivilegedGroup.OPERATORS_GROUP.groupName).hasPrivilege(EnumTeleportationPrivilege.LOCATIONS_MANAGEMENT.toString())) {
            PrivilegeProviderServer.addPrivileges(PrivilegedGroup.OPERATORS_GROUP.groupName, true,  
                    new Privilege(EnumTeleportationPrivilege.PROCESS_TELEPORTATION_ON_MOVE.toString(), true),
                    new Privilege(EnumTeleportationPrivilege.ENABLE_MOVE_TO_LOCKED_LOCATIONS.toString(), true),
                    new Privilege(EnumTeleportationPrivilege.ENABLE_CROSS_DIM_TELEPORTATION.toString(), true),
                    new Privilege(EnumTeleportationPrivilege.ENABLE_TELEPORTATION_TO_ANY_PLAYER.toString(), true),

                    new Privilege(EnumTeleportationPrivilege.CAMP_TELEPORTATION_DELAY.toString(), 0),
                    new Privilege(EnumTeleportationPrivilege.CAMP_TELEPORTATION_COOLDOWN.toString(), 0),

                    new Privilege(EnumTeleportationPrivilege.LOCATIONS_MANAGEMENT.toString(), true),
                    new Privilege(EnumTeleportationPrivilege.LOCATION_TELEPORTATION_DELAY.toString(), 0),
                    new Privilege(EnumTeleportationPrivilege.LOCATION_TELEPORTATION_COOLDOWN.toString(), 0),

                    new Privilege(EnumTeleportationPrivilege.PLAYER_TELEPORTATION_DELAY.toString(), 0),
                    new Privilege(EnumTeleportationPrivilege.PLAYER_TELEPORTATION_COOLDOWN.toString(), 0));
            LOGGER.info("Default <{}> group privileges added.", PrivilegedGroup.OPERATORS_GROUP.groupName);
        }
    }

    private void initNetwork() {
        network = OxygenHelperServer.createNetworkHandler(MODID);

        network.registerPacket(CPStartImageDownload.class);
        network.registerPacket(CPDownloadImagePart.class);
        network.registerPacket(CPSyncCooldown.class);
        network.registerPacket(CPSyncInvitedPlayers.class);
        network.registerPacket(CPSyncAdditionalData.class);

        network.registerPacket(SPTeleportationRequest.class);
        network.registerPacket(SPCreateWorldPoint.class);
        network.registerPacket(SPRemoveWorldPoint.class);
        network.registerPacket(SPEditWorldPoint.class);
        network.registerPacket(SPStartImageUpload.class);
        network.registerPacket(SPUploadImagePart.class);
        network.registerPacket(SPMoveToPoint.class);    
        network.registerPacket(SPMoveToFavoriteCamp.class);
        network.registerPacket(SPSetFavoriteCamp.class);
        network.registerPacket(SPLockPoint.class);
        network.registerPacket(SPChangeJumpProfile.class);
        network.registerPacket(SPMoveToPlayer.class);
        network.registerPacket(SPManageInvitation.class);
        network.registerPacket(SPLeaveCampPoint.class);
    }

    public static OxygenNetwork network() {
        return network;
    }
}