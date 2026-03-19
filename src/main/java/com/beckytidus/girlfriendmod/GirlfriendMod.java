package com.beckytidus.girlfriendmod;

import com.beckytidus.girlfriendmod.command.BoyFriendCommand;
import com.beckytidus.girlfriendmod.command.GirlFriendCommand;
import com.beckytidus.girlfriendmod.event.EntityAttributeHandler;
import com.beckytidus.girlfriendmod.registry.EntityRegistry;
import com.beckytidus.girlfriendmod.registry.ItemRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GirlfriendMod implements ModInitializer {

    public static final String MOD_ID = "girlfriend-mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Girlfriend Mod...");

        EntityRegistry.register();
        ItemRegistry.register();
        EntityAttributeHandler.register();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            GirlFriendCommand.register(dispatcher);
            BoyFriendCommand.register(dispatcher);
        });
    }
}
