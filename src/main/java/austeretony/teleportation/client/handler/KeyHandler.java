package austeretony.teleportation.client.handler;

import org.lwjgl.input.Keyboard;

import austeretony.oxygen.client.reference.ClientReference;
import austeretony.teleportation.client.TeleportationManagerClient;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.main.TeleportationMain;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeyHandler {

    public static final KeyBinding 
    OPEN_MENU = registerKeyBinding("key.teleportation.openMenu", Keyboard.KEY_P, TeleportationMain.NAME),
    MOVE_TO_CAMP = registerKeyBinding("key.teleportation.moveToCamp", Keyboard.KEY_H, TeleportationMain.NAME);

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (OPEN_MENU.isPressed())
            TeleportationManagerClient.instance().openMenuSynced();
        else if (MOVE_TO_CAMP.isPressed()) {
            if (TeleportationConfig.ENABLE_FAVORITE_CAMP.getBooleanValue())
                TeleportationManagerClient.instance().getCampsManager().moveToCampSynced(TeleportationManagerClient.instance().getPlayerProfile().getFavoriteCampId());
        }
    }

    public static KeyBinding registerKeyBinding(String name, int keyCode, String category) {
        KeyBinding keyBinding = new KeyBinding(name, keyCode, category);
        ClientReference.registerKeyBinding(keyBinding);
        return keyBinding;
    }
}
