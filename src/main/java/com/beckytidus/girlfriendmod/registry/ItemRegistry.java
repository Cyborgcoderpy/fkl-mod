package com.beckytidus.girlfriendmod.registry;

import com.beckytidus.girlfriendmod.item.BoyFriendSummonerItem;
import com.beckytidus.girlfriendmod.item.GirlFriendSummonerItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ItemRegistry {
    public static final Item GIRLFRIEND_SUMMONER = Registry.register(
            Registries.ITEM,
            Identifier.of("girlfriend-mod", "girlfriend_summoner"),
            new GirlFriendSummonerItem(new Item.Settings().maxCount(1))
    );

    public static final Item BOYFRIEND_SUMMONER = Registry.register(
            Registries.ITEM,
            Identifier.of("girlfriend-mod", "boyfriend_summoner"),
            new BoyFriendSummonerItem(new Item.Settings().maxCount(1))
    );

    public static void register() {}
}
