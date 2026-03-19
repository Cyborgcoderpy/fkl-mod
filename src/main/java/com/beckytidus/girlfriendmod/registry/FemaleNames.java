package com.beckytidus.girlfriendmod.registry;

import java.util.Random;

public class FemaleNames {
    private static final String[] NAMES = {
        "Aria","Belle","Clara","Diana","Elena","Fiona","Grace","Hana",
        "Iris","Jade","Kira","Luna","Mia","Nora","Olive","Piper",
        "Quinn","Rose","Sara","Tara","Uma","Vera","Wren","Xena","Yara","Zoe",
        "Alice","Bianca","Chloe","Daisy","Emma","Flora","Greta","Holly",
        "Isla","Jess","Kayla","Lena","Maya","Nina","Opal","Pearl",
        "Rina","Stella","Tessa","Ursa","Vivi","Wendy","Xara","Yuki","Zara"
    };

    public static String random(Random rng) {
        return NAMES[rng.nextInt(NAMES.length)];
    }
}
