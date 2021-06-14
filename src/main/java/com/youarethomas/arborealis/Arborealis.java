package com.youarethomas.arborealis;

import com.youarethomas.arborealis.block_entities.CarvedWoodEntity;
import com.youarethomas.arborealis.blocks.CarvedWood;
import com.youarethomas.arborealis.blocks.TestBlock;
import com.youarethomas.arborealis.items.CarvingKnife;
import com.youarethomas.arborealis.tool_materials.CopperKnifeMaterial;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Arborealis implements ModInitializer {

	// Items
	public static final CarvingKnife CARVING_KNIFE = new CarvingKnife(CopperKnifeMaterial.INSTANCE, new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1));

	// Blocks
	public static final TestBlock TEST_BLOCK = new TestBlock(FabricBlockSettings.of(Material.STONE));
	public static final CarvedWood CARVED_WOOD = new CarvedWood(FabricBlockSettings.of(Material.WOOD));

	public static BlockEntityType<CarvedWoodEntity> CARVED_WOOD_ENTITY;

	// Item Groups
	public static final ItemGroup ARBOREALIS_GROUP = FabricItemGroupBuilder.create(
			new Identifier("arborealis", "arborealis"))
			.icon(() -> new ItemStack(CARVING_KNIFE))
			.appendItems(stacks -> {
				stacks.add(new ItemStack(CARVING_KNIFE));
				stacks.add(new ItemStack(TEST_BLOCK));
				stacks.add(new ItemStack(CARVED_WOOD));
			})
			.build();

	@Override
	public void onInitialize() {

		// Item registration
		Registry.register(Registry.ITEM, new Identifier("arborealis", "carving_knife"), CARVING_KNIFE);

		// Block registration
		Registry.register(Registry.BLOCK, new Identifier("arborealis", "test_block"), TEST_BLOCK);
		Registry.register(Registry.BLOCK, new Identifier("arborealis", "carved_wood"), CARVED_WOOD);

		// Block entity registration
		CARVED_WOOD_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "arborealis:carved_wood_entity", FabricBlockEntityTypeBuilder.create(CarvedWoodEntity::new, CARVED_WOOD).build(null));

		// Block item registration
		Registry.register(Registry.ITEM, new Identifier("arborealis", "test_block"), new BlockItem(TEST_BLOCK, new FabricItemSettings()));
		Registry.register(Registry.ITEM, new Identifier("arborealis", "carved_wood"), new BlockItem(CARVED_WOOD, new FabricItemSettings()));

		System.out.println("Arborealis Initialised!");
	}
}
