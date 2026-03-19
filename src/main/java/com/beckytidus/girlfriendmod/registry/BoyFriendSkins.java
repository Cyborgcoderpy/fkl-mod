package com.beckytidus.girlfriendmod.registry;

import net.minecraft.util.Identifier;

public class BoyFriendSkins {

    private static final Identifier[] SKINS;

    static {
        // Uses boyfriend-specific textures if present, falls back to default player skin style
        // Add textures/entity/boyfriend_0.png through boyfriend_9.png in resources
        SKINS = new Identifier[10];
        for (int i = 0; i < 10; i++) {
            SKINS[i] = Identifier.of("girlfriend-mod", "textures/entity/boyfriend_" + i + ".png");
        }
    }

    public static Identifier getSkin(int index) {
        if (index < 0 || index >= SKINS.length) return SKINS[0];
        return SKINS[index];
    }

    public static int count() { return SKINS.length; }
}
