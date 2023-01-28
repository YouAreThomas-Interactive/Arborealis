package com.youarethomas.arborealis.util;

import net.minecraft.util.Identifier;

public record ArborealisConstants() {
    public static final String MOD_ID = "arborealis";

    // Client Receivers (Executed on the client)
    public static final Identifier UPDATE_PROJECTOR = new Identifier(MOD_ID, "update_projector");
    public static final Identifier INIT_DOWNLOAD_TREE_STRUCTURES = new Identifier(MOD_ID, "init_download_tree_structures");
    public static final Identifier UPDATE_TREE_STRUCTURES = new Identifier(MOD_ID, "update_tree_structures");
    public static final Identifier CLIENT_RUNE_PUSH = new Identifier(MOD_ID, "client_rune_push");

    // Server Receivers (Executed on the server)
    public static final Identifier SCROLL_BAG_UPDATE = new Identifier(MOD_ID, "scroll_bag_update");
    public static final Identifier WARP_TREE_TELEPORT = new Identifier(MOD_ID, "warp_tree_teleport");
}
