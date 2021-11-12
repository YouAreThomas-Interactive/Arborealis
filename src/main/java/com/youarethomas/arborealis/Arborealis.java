package com.youarethomas.arborealis;

import com.youarethomas.arborealis.block_entities.CarvedWoodEntity;
import com.youarethomas.arborealis.block_entities.HollowedLogEntity;
import com.youarethomas.arborealis.block_entities.WoodenBucketEntity;
import com.youarethomas.arborealis.blocks.*;
import com.youarethomas.arborealis.items.*;
import com.youarethomas.arborealis.models.CarvedStencilModel;
import com.youarethomas.arborealis.models.CarvedWoodModel;
import com.youarethomas.arborealis.models.model_utils.DynamicModelRegistry;
import com.youarethomas.arborealis.runes.*;
import com.youarethomas.arborealis.tool_materials.CopperKnifeMaterial;
import com.youarethomas.arborealis.tool_materials.RegrowthSpoonMaterial;
import com.youarethomas.arborealis.tool_materials.WoodDrillMaterial;
import com.youarethomas.arborealis.util.RuneManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.*;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

public class Arborealis implements ModInitializer {

	public static final String MOD_ID = "arborealis";

	public static final Logger LOGGER = LogManager.getLogger();

	public static final HashMap<Integer, Identifier> LogIDs = new HashMap<>() {{
		put(0, new Identifier("minecraft:oak_log"));
		put(1, new Identifier("minecraft:spruce_log"));
		put(2, new Identifier("minecraft:birch_log"));
		put(3, new Identifier("minecraft:jungle_log"));
		put(4, new Identifier("minecraft:dark_oak_log"));
		put(5, new Identifier("minecraft:acacia_log"));
		put(6, new Identifier("minecraft:crimson_stem"));
		put(7, new Identifier("minecraft:warped_stem"));
	}};

	// Runes
	public static final Light LIGHT = new Light();
	public static final AreaTest AREA_TEST = new AreaTest();
	public static final Chop CHOP = new Chop();
	public static final Pull PULL = new Pull();
	public static final Push PUSH = new Push();

	// Tool Items
	public static final CarvingKnife CARVING_KNIFE = new CarvingKnife(CopperKnifeMaterial.INSTANCE, new FabricItemSettings().maxCount(1));
	public static final WoodDrill WOOD_DRILL = new WoodDrill(WoodDrillMaterial.INSTANCE, new FabricItemSettings().maxCount(1));
	public static final RegrowthSpoon REGROWTH_SPOON = new RegrowthSpoon(RegrowthSpoonMaterial.INSTANCE, new FabricItemSettings().maxCount(1));

	// Items
	public static final TreeCore TREE_CORE = new TreeCore(new FabricItemSettings().maxCount(8).rarity(Rarity.UNCOMMON));

	public static final Item BOTTLED_SAP = new Item(new FabricItemSettings().recipeRemainder(Items.GLASS_BOTTLE));
	public static final InfusedSap INFUSED_SAP = new InfusedSap(new FabricItemSettings().recipeRemainder(Items.GLASS_BOTTLE).rarity(Rarity.UNCOMMON));
	public static final Item GLOWING_SAP = new Item(new FabricItemSettings().recipeRemainder(Items.GLASS_BOTTLE));
	public static final Item CONDUCTIVE_SAP = new Item(new FabricItemSettings().recipeRemainder(Items.GLASS_BOTTLE));

	public static final Item BLANK_STENCIL = new StencilBlank(new FabricItemSettings());
	public static final Item CARVED_STENCIL = new StencilCarved(new FabricItemSettings().maxCount(1));

	// Blocks
	public static final TestBlock TEST_BLOCK = new TestBlock(FabricBlockSettings.of(Material.STONE));

	public static final CarvedWood CARVED_WOOD = new CarvedWood(FabricBlockSettings.of(Material.WOOD));
	public static final CarvedWood CARVED_NETHER_WOOD = new CarvedWood(FabricBlockSettings.of(Material.WOOD));
	public static final HollowedLog HOLLOWED_LOG = new HollowedLog(FabricBlockSettings.of(Material.WOOD));
	public static final HollowedLog HOLLOWED_NETHER_LOG = new HollowedLog(FabricBlockSettings.of(Material.WOOD));
	public static final TreeCoreBlock TREE_CORE_BLOCK = new TreeCoreBlock(FabricBlockSettings.of(Material.WOOD));

	public static final TreeTap TREE_TAP = new TreeTap(FabricBlockSettings.of(Material.METAL));
	public static final WoodenBucket WOODEN_BUCKET = new WoodenBucket(FabricBlockSettings.of(Material.WOOD));

	// Block Entities
	public static BlockEntityType<CarvedWoodEntity> CARVED_WOOD_ENTITY;
	public static BlockEntityType<HollowedLogEntity> HOLLOWED_LOG_ENTITY;
	public static BlockEntityType<WoodenBucketEntity> WOODEN_BUCKET_ENTITY;

	// Tags
	public static final Tag<Block> MODIFIED_LOGS = TagRegistry.block(new Identifier(MOD_ID, "modified_logs"));

	// Item Groups
	public static final ItemGroup ARBOREALIS_GROUP = FabricItemGroupBuilder.create(
			new Identifier(MOD_ID, "arborealis"))
			.icon(() -> new ItemStack(CARVING_KNIFE))
			.appendItems(stacks -> {
				stacks.add(new ItemStack(CARVING_KNIFE));
				stacks.add(new ItemStack(WOOD_DRILL));
				stacks.add(new ItemStack(REGROWTH_SPOON));
				stacks.add(new ItemStack(TREE_CORE));
				stacks.add(new ItemStack(TREE_TAP));
				stacks.add(new ItemStack(BOTTLED_SAP));
				stacks.add(new ItemStack(INFUSED_SAP));
				stacks.add(new ItemStack(GLOWING_SAP));
				stacks.add(new ItemStack(CONDUCTIVE_SAP));

				stacks.add(new ItemStack(WOODEN_BUCKET));
				stacks.add(new ItemStack(BLANK_STENCIL));

				stacks.add(new ItemStack(TEST_BLOCK));
			})
			.build();

	@Override
	public void onInitialize() {
		// Rune registration
		RuneManager.register(new Identifier(MOD_ID, "light"), LIGHT);
		RuneManager.register(new Identifier(MOD_ID, "chop"), CHOP);
		RuneManager.register(new Identifier(MOD_ID, "pull"), PULL);
		RuneManager.register(new Identifier(MOD_ID, "push"), PUSH);
		RuneManager.register(new Identifier(MOD_ID, "test"), AREA_TEST);

		RuneManager.initializeRunes();

		// Block registration
		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "test_block"), TEST_BLOCK);

		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "carved_wood"), CARVED_WOOD);
		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "carved_nether_wood"), CARVED_NETHER_WOOD);
		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "hollowed_log"), HOLLOWED_LOG);
		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "hollowed_nether_log"), HOLLOWED_NETHER_LOG);
		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "tree_core_block"), TREE_CORE_BLOCK);

		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "tree_tap"), TREE_TAP);
		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "wooden_bucket"), WOODEN_BUCKET);

		// Block entity registration
		CARVED_WOOD_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "carved_wood_entity"), FabricBlockEntityTypeBuilder.create(CarvedWoodEntity::new, CARVED_WOOD).build(null));
		HOLLOWED_LOG_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "hollowed_log_entity"), FabricBlockEntityTypeBuilder.create(HollowedLogEntity::new, HOLLOWED_LOG).build(null));
		WOODEN_BUCKET_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "wooden_bucket_entity"), FabricBlockEntityTypeBuilder.create(WoodenBucketEntity::new, WOODEN_BUCKET).build(null));

		// Block item registration
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "test_block"), new BlockItem(TEST_BLOCK, new FabricItemSettings()));

		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "tree_tap"), new BlockItem(TREE_TAP, new FabricItemSettings().maxCount(16)));
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "wooden_bucket"), new BlockItem(WOODEN_BUCKET, new FabricItemSettings().maxCount(16)));

		// Tool Item registration
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "carving_knife"), CARVING_KNIFE);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "wood_drill"), WOOD_DRILL);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "regrowth_spoon"), REGROWTH_SPOON);

		// Item registration
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "tree_core"), TREE_CORE);

		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "bottled_sap"), BOTTLED_SAP);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "infused_sap"), INFUSED_SAP);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "glowing_sap"), GLOWING_SAP);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "conductive_sap"), CONDUCTIVE_SAP);

		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "stencil_blank"), BLANK_STENCIL);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "stencil_carved"), CARVED_STENCIL);

		// Model registration
		DynamicModelRegistry.register(new CarvedWoodModel(), new Identifier("arborealis:block/carved_wood_model"));
		DynamicModelRegistry.register(new CarvedStencilModel(), new ModelIdentifier("arborealis:item/stencil_carved#inventory"));

		// SET IT ON FIRE!
		FlammableBlockRegistry.getDefaultInstance().add(CARVED_WOOD, 5, 5);
		FlammableBlockRegistry.getDefaultInstance().add(HOLLOWED_LOG, 5, 5);

		LOGGER.info("Arborealis Initialised!");
	}
}
