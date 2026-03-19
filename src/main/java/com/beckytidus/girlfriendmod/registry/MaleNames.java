package com.beckytidus.girlfriendmod.registry;

import java.util.Random;

public class MaleNames {
    private static final String[] NAMES = {
        "Alex","Blake","Cole","Dylan","Ethan","Finn","Gabe","Hunter",
        "Ivan","Jake","Kane","Liam","Mason","Noah","Owen","Percy",
        "Quinn","Ryan","Sean","Tyler","Uma","Victor","Wade","Xander","York","Zane",
        "Aaron","Ben","Chris","Dave","Erik","Felix","Greg","Henry",
        "Ian","Joel","Kyle","Leo","Mike","Nate","Oscar","Paul",
        "Reed","Sam","Tom","Umar","Vince","Will","Xavier","Yusuf","Zack"
    };

    public static String random(Random rng) {
        return NAMES[rng.nextInt(NAMES.length)];
    }
}
