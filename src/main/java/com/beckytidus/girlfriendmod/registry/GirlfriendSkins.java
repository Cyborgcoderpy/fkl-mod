package com.beckytidus.girlfriendmod.registry;

import net.minecraft.util.Identifier;

public class GirlfriendSkins {

    private static final Identifier[] SKINS;

    static {
        // skin 0 = default, 1-20 = variants, 21 = alt
        SKINS = new Identifier[22];
        SKINS[0] = Identifier.of("girlfriend-mod", "textures/entity/girlfriend.png");
        for (int i = 1; i <= 20; i++) {
            SKINS[i] = Identifier.of("girlfriend-mod", "textures/entity/girlfriend_" + i + ".png");
        }
        SKINS[21] = Identifier.of("girlfriend-mod", "textures/entity/girlfriend_alt.png");
    }

    public static Identifier getSkin(int index) {
        if (index < 0 || index >= SKINS.length) return SKINS[0];
        return SKINS[index];
    }

    public static int count() { return SKINS.length; }
}
