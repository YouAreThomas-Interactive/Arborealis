package com.youarethomas.arborealis.util;

import net.minecraft.util.Identifier;

public record ArborealisConstants() {
    public static final String MOD_ID = "arborealis";

    // Client Receivers
    public static final Identifier TREE_MAP_UPDATE = new Identifier(MOD_ID, "tree_map_update");
    public static final Identifier CLIENT_RUNE_PUSH = new Identifier(MOD_ID, "client_rune_push");

    // Server Receivers
    public static final Identifier SCROLL_BAG_UPDATE = new Identifier(MOD_ID, "scroll_bag_update");
}
