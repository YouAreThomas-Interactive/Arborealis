package com.youarethomas.arborealis;

import com.youarethomas.arborealis.items.CarvingKnife;
import com.youarethomas.arborealis.tool_materials.CopperKnifeMaterial;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Arborealis implements ModInitializer {

	//public static final CarvingKnife CARVING_KNIFE = new CarvingKnife(new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1));
	public static final CarvingKnife CARVING_KNIFE = new CarvingKnife(CopperKnifeMaterial.INSTANCE, new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1));

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		Registry.register(Registry.ITEM, new Identifier("arborealis", "carving_knife"), CARVING_KNIFE);

		System.out.println("Arborealis Initialised!");
	}
}
