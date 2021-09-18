package com.youarethomas.arborealis;

import com.youarethomas.arborealis.block_entities.CarvedWoodEntity;
import com.youarethomas.arborealis.block_entities.HollowedLogEntity;
import com.youarethomas.arborealis.blocks.CarvedWood;
import com.youarethomas.arborealis.blocks.HollowedLog;
import com.youarethomas.arborealis.blocks.TestBlock;
import com.youarethomas.arborealis.items.CarvingKnife;
import com.youarethomas.arborealis.items.TreeCore;
import com.youarethomas.arborealis.items.WoodDrill;
import com.youarethomas.arborealis.models.CarvedWoodModel;
import com.youarethomas.arborealis.models.utils.DynamicModelRegistry;
import com.youarethomas.arborealis.tool_materials.CopperKnifeMaterial;
import com.youarethomas.arborealis.tool_materials.WoodDrillMaterial;
import com.youarethomas.arborealis.util.RuneManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Arborealis implements ModInitializer {

	public static final String MOD_ID = "arborealis";


	// Items
	public static final CarvingKnife CARVING_KNIFE = new CarvingKnife(CopperKnifeMaterial.INSTANCE, new FabricItemSettings().maxCount(1));
	public static final WoodDrill WOOD_DRILL = new WoodDrill(WoodDrillMaterial.INSTANCE, new FabricItemSettings().maxCount(1));
	public static final TreeCore TREE_CORE = new TreeCore(new FabricItemSettings().maxCount(8));

	// Blocks
	public static final TestBlock TEST_BLOCK = new TestBlock(FabricBlockSettings.of(Material.STONE));
	public static final CarvedWood CARVED_WOOD = new CarvedWood(FabricBlockSettings.of(Material.WOOD));
	public static final HollowedLog HOLLOWED_LOG = new HollowedLog(FabricBlockSettings.of(Material.WOOD));
	public static final Block TREE_CORE_BLOCK = new Block(FabricBlockSettings.of(Material.ICE));

	// Block Entities
	public static BlockEntityType<CarvedWoodEntity> CARVED_WOOD_ENTITY;
	public static BlockEntityType<HollowedLogEntity> HOLLOWED_LOG_ENTITY;

	// Item Groups
	public static final ItemGroup ARBOREALIS_GROUP = FabricItemGroupBuilder.create(
			new Identifier(MOD_ID, "arborealis"))
			.icon(() -> new ItemStack(CARVING_KNIFE))
			.appendItems(stacks -> {
				stacks.add(new ItemStack(TEST_BLOCK));
				stacks.add(new ItemStack(CARVING_KNIFE));
				stacks.add(new ItemStack(TREE_CORE));
				stacks.add(new ItemStack(HOLLOWED_LOG));
				stacks.add(new ItemStack(WOOD_DRILL));
			})
			.build();

	@Override
	public void onInitialize() {
		RuneManager.initializeRunes();

		// Block registration
		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "test_block"), TEST_BLOCK);
		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "carved_wood"), CARVED_WOOD);
		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "hollowed_log"), HOLLOWED_LOG);

		// Block entity registration
		CARVED_WOOD_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, MOD_ID + ":carved_wood_entity", FabricBlockEntityTypeBuilder.create(CarvedWoodEntity::new, CARVED_WOOD).build(null));
		HOLLOWED_LOG_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, MOD_ID + ":hollowed_log_entity", FabricBlockEntityTypeBuilder.create(HollowedLogEntity::new, HOLLOWED_LOG).build(null));

		// Block item registration
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "test_block"), new BlockItem(TEST_BLOCK, new FabricItemSettings()));
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "hollowed_log"), new BlockItem(HOLLOWED_LOG, new FabricItemSettings()));

		// Item registration
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "carving_knife"), CARVING_KNIFE);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "wood_drill"), WOOD_DRILL);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "tree_core"), TREE_CORE);

		// Model registration
		DynamicModelRegistry.register(new CarvedWoodModel(), new Identifier("arborealis:block/carved_wood_model"));

		System.out.println("Arborealis Initialised!");
	}
}
