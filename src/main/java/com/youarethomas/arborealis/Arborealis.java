package com.youarethomas.arborealis;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import com.youarethomas.arborealis.block_entities.*;
import com.youarethomas.arborealis.blocks.*;
import com.youarethomas.arborealis.items.*;
import com.youarethomas.arborealis.mixins.AxeItemAccessor;
import com.youarethomas.arborealis.mixins.CreateLeavesBlockInvoker;
import com.youarethomas.arborealis.misc.StencilBagDyeRecipe;
import com.youarethomas.arborealis.items.tool_materials.CopperKnifeMaterial;
import com.youarethomas.arborealis.items.tool_materials.RegrowthSpoonMaterial;
import com.youarethomas.arborealis.items.tool_materials.WoodDrillMaterial;
import com.youarethomas.arborealis.util.ArborealisConstants;
import com.youarethomas.arborealis.util.RuneManager;
import com.youarethomas.arborealis.gui.StencilBagScreenHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;

import java.util.Map;

public class Arborealis implements ModInitializer {

	public static final String MOD_ID = ArborealisConstants.MOD_ID;
	public static final Random RANDOM = Random.create();

	public static final Logger LOGGER = LogUtils.getLogger();

	// Tool Items
	public static final CarvingKnife CARVING_KNIFE = new CarvingKnife(CopperKnifeMaterial.INSTANCE, new FabricItemSettings());
	public static final LogDrill LOG_DRILL = new LogDrill(WoodDrillMaterial.INSTANCE, new FabricItemSettings().maxCount(1));
	public static final RegrowthSpoon REGROWTH_SPOON = new RegrowthSpoon(RegrowthSpoonMaterial.INSTANCE, new FabricItemSettings().maxCount(1));

	// Items
	public static final TreeCore TREE_CORE = new TreeCore(new FabricItemSettings().maxCount(8).rarity(Rarity.UNCOMMON));

	public static final Item BOTTLED_SAP = new Item(new FabricItemSettings().recipeRemainder(Items.GLASS_BOTTLE));
	public static final InfusedSap INFUSED_SAP = new InfusedSap(new FabricItemSettings().recipeRemainder(Items.GLASS_BOTTLE).rarity(Rarity.UNCOMMON));
	public static final Item GLOWING_SAP = new Item(new FabricItemSettings().recipeRemainder(Items.GLASS_BOTTLE));
	public static final Item CONDUCTIVE_SAP = new Item(new FabricItemSettings().recipeRemainder(Items.GLASS_BOTTLE));

	public static final Item BLANK_STENCIL = new StencilBlank(new FabricItemSettings());
	public static final Item CARVED_STENCIL = new StencilCarved(new FabricItemSettings().maxCount(1));
	public static final Item STENCIL_BAG = new StencilBag(new FabricItemSettings().maxCount(1));

	public static final Item WARP_GRAFT = new Item(new FabricItemSettings());

	// Blocks
	public static final CarvedLog CARVED_LOG = new CarvedLog(FabricBlockSettings.of(Material.WOOD).sounds(BlockSoundGroup.WOOD));
	public static final CarvedNetherLog CARVED_NETHER_LOG = new CarvedNetherLog(FabricBlockSettings.of(Material.WOOD).sounds(BlockSoundGroup.WOOD));
	public static final HollowedLog HOLLOWED_LOG = new HollowedLog(FabricBlockSettings.of(Material.WOOD).sounds(BlockSoundGroup.WOOD));
	public static final HollowedLog HOLLOWED_NETHER_LOG = new HollowedLog(FabricBlockSettings.of(Material.WOOD).sounds(BlockSoundGroup.WOOD));

	public static final WarpSapling WARP_SAPLING = new WarpSapling(FabricBlockSettings.of(Material.PLANT));
	public static final Block WARP_LEAVES = CreateLeavesBlockInvoker.createLeavesBlock(BlockSoundGroup.GRASS);
	public static final Block WARP_WOOD = new PillarBlock(FabricBlockSettings.of(Material.WOOD).strength(2.0f).sounds(BlockSoundGroup.WOOD));
	public static final Block WARP_LOG = new PillarBlock(FabricBlockSettings.of(Material.WOOD).sounds(BlockSoundGroup.WOOD));
	public static final Block STRIPPED_WARP_LOG = new PillarBlock(FabricBlockSettings.of(Material.WOOD).sounds(BlockSoundGroup.WOOD));
	public static final Block STRIPPED_WARP_WOOD = new PillarBlock(FabricBlockSettings.of(Material.WOOD).sounds(BlockSoundGroup.WOOD));
	public static final WarpCore WARP_CORE = new WarpCore(FabricBlockSettings.of(Material.WOOD));

	public static final TreeTap TREE_TAP = new TreeTap(FabricBlockSettings.of(Material.METAL));
	public static final WoodenBucket WOODEN_BUCKET = new WoodenBucket(FabricBlockSettings.of(Material.WOOD));

	// Block Entities
	public static BlockEntityType<CarvedLogEntity> CARVED_LOG_ENTITY;
	public static BlockEntityType<CarvedNetherLogEntity> CARVED_NETHER_LOG_ENTITY;
	public static BlockEntityType<HollowedLogEntity> HOLLOWED_LOG_ENTITY;
	public static BlockEntityType<WoodenBucketEntity> WOODEN_BUCKET_ENTITY;
	public static BlockEntityType<WarpCoreEntity> WARP_CORE_ENTITY;

	// Screen Handlers
	public static ScreenHandlerType<StencilBagScreenHandler> STENCIL_BAG_SCREEN_HANDLER;

	// Recipe Serializers
	public static SpecialRecipeSerializer<StencilBagDyeRecipe> STENCIL_BAG_DYE;

	// Particle Types
	public static DefaultParticleType WARP_TREE_PARTICLE = FabricParticleTypes.simple();

	// Tags
	public static final TagKey<Block> MODIFIED_LOGS = TagKey.of(Registry.BLOCK_KEY, new Identifier(MOD_ID, "modified_logs"));
	public static final TagKey<Block> WARP_LOGS = TagKey.of(Registry.BLOCK_KEY, new Identifier(MOD_ID, "warp_logs"));

	// Item Groups
	public static final ItemGroup ARBOREALIS_GROUP = FabricItemGroupBuilder.create(
			new Identifier(MOD_ID, "arborealis"))
			.icon(() -> new ItemStack(CARVING_KNIFE))
			.appendItems(stacks -> {
				stacks.add(new ItemStack(CARVING_KNIFE));
				stacks.add(new ItemStack(LOG_DRILL));
				stacks.add(new ItemStack(REGROWTH_SPOON));
				stacks.add(new ItemStack(TREE_CORE));
				stacks.add(new ItemStack(TREE_TAP));
				stacks.add(new ItemStack(BOTTLED_SAP));
				stacks.add(new ItemStack(INFUSED_SAP));
				stacks.add(new ItemStack(GLOWING_SAP));
				stacks.add(new ItemStack(CONDUCTIVE_SAP));

				stacks.add(new ItemStack(WOODEN_BUCKET));
				stacks.add(new ItemStack(BLANK_STENCIL));
				stacks.add(new ItemStack(STENCIL_BAG));
				stacks.add(new ItemStack(WARP_GRAFT));
				stacks.add(new ItemStack(WARP_SAPLING));
				stacks.add(new ItemStack(WARP_LEAVES));
				stacks.add(new ItemStack(WARP_CORE));
				stacks.add(new ItemStack(WARP_WOOD));
				stacks.add(new ItemStack(WARP_LOG));
				stacks.add(new ItemStack(STRIPPED_WARP_LOG));
				stacks.add(new ItemStack(STRIPPED_WARP_WOOD));
			})
			.build();

	@Override
	public void onInitialize() {
		RuneManager.registerRunes();
		RuneManager.initializeRunePatterns(new Identifier(MOD_ID, "runes"));

		// Block registration
		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "carved_log"), CARVED_LOG);
		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "carved_nether_log"), CARVED_NETHER_LOG);
		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "hollowed_log"), HOLLOWED_LOG);
		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "hollowed_nether_log"), HOLLOWED_NETHER_LOG);

		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "warp_sapling"), WARP_SAPLING);
		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "warp_leaves"), WARP_LEAVES);
		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "warp_wood"), WARP_WOOD);
		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "warp_log"), WARP_LOG);
		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "stripped_warp_log"), STRIPPED_WARP_LOG);
		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "stripped_warp_wood"), STRIPPED_WARP_WOOD);
		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "warp_core"), WARP_CORE);

		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "tree_tap"), TREE_TAP);
		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "wooden_bucket"), WOODEN_BUCKET);

		// Block entity registration
		CARVED_LOG_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "carved_log_entity"), FabricBlockEntityTypeBuilder.create(CarvedLogEntity::new, CARVED_LOG).build(null));
		CARVED_NETHER_LOG_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "carved_nether_log_entity"), FabricBlockEntityTypeBuilder.create(CarvedNetherLogEntity::new, CARVED_NETHER_LOG).build(null));
		HOLLOWED_LOG_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "hollowed_log_entity"), FabricBlockEntityTypeBuilder.create(HollowedLogEntity::new, HOLLOWED_LOG).build(null));
		WOODEN_BUCKET_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "wooden_bucket_entity"), FabricBlockEntityTypeBuilder.create(WoodenBucketEntity::new, WOODEN_BUCKET).build(null));
		WARP_CORE_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "warp_core_entity"), FabricBlockEntityTypeBuilder.create(WarpCoreEntity::new, WARP_CORE).build(null));

		// Block item registration
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "warp_sapling"), new BlockItem(WARP_SAPLING, new FabricItemSettings()));
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "warp_leaves"), new BlockItem(WARP_LEAVES, new FabricItemSettings()));
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "warp_wood"), new BlockItem(WARP_WOOD, new FabricItemSettings()));
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "warp_log"), new BlockItem(WARP_LOG, new FabricItemSettings()));
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "stripped_warp_log"), new BlockItem(STRIPPED_WARP_LOG, new FabricItemSettings()));
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "stripped_warp_wood"), new BlockItem(STRIPPED_WARP_WOOD, new FabricItemSettings()));
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "warp_core"), new BlockItem(WARP_CORE, new FabricItemSettings()));

		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "tree_tap"), new BlockItem(TREE_TAP, new FabricItemSettings().maxCount(16)));
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "wooden_bucket"), new BlockItem(WOODEN_BUCKET, new FabricItemSettings().maxCount(16)));

		// Tool Item registration
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "carving_knife"), CARVING_KNIFE);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "log_drill"), LOG_DRILL);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "regrowth_spoon"), REGROWTH_SPOON);

		// Item registration
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "tree_core"), TREE_CORE);

		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "bottled_sap"), BOTTLED_SAP);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "infused_sap"), INFUSED_SAP);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "glowing_sap"), GLOWING_SAP);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "conductive_sap"), CONDUCTIVE_SAP);

		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "stencil_blank"), BLANK_STENCIL);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "stencil_carved"), CARVED_STENCIL);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "stencil_bag"), STENCIL_BAG);

		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "warp_graft"), WARP_GRAFT);

		// Screen Handler
		STENCIL_BAG_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier(MOD_ID, "stencil_bag"), StencilBagScreenHandler::new);

		// Recipe Serializers
		STENCIL_BAG_DYE =  RecipeSerializer.register("crafting_special_bag_dye", new SpecialRecipeSerializer<>(StencilBagDyeRecipe::new));

		// Particles
		Registry.register(Registry.PARTICLE_TYPE, new Identifier(MOD_ID, "warp_tree_particle"), WARP_TREE_PARTICLE);

		// SET IT ON FIRE!
		FlammableBlockRegistry.getDefaultInstance().add(CARVED_LOG, 5, 5);
		FlammableBlockRegistry.getDefaultInstance().add(HOLLOWED_LOG, 5, 5);

		FlammableBlockRegistry.getDefaultInstance().add(WARP_WOOD, 5, 5);
		FlammableBlockRegistry.getDefaultInstance().add(WARP_LOG, 5, 5);
		FlammableBlockRegistry.getDefaultInstance().add(STRIPPED_WARP_WOOD, 5, 5);
		FlammableBlockRegistry.getDefaultInstance().add(STRIPPED_WARP_LOG, 5, 5);
		FlammableBlockRegistry.getDefaultInstance().add(WARP_LEAVES, 60, 30);

		// Add wood stripping
		Map<Block, Block> axeStripped = new ImmutableMap.Builder<Block, Block>().putAll(AxeItemAccessor.getStrippedBlocks())
				.put(WARP_LOG, STRIPPED_WARP_LOG).put(WARP_WOOD, STRIPPED_WARP_WOOD)
				.build();
		AxeItemAccessor.setStrippedBlocks(axeStripped);

		// Server networking handlers
		ServerPlayNetworking.registerGlobalReceiver(ArborealisConstants.SCROLL_BAG_UPDATE, (server, player, handler, buf, responseSender) -> {
			NbtCompound nbt = buf.readNbt();

			ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
			if (stack.isOf(STENCIL_BAG))
				stack.setNbt(nbt);
		});

		ServerPlayNetworking.registerGlobalReceiver(ArborealisConstants.WARP_TREE_TELEPORT, WarpCoreEntity::teleportPlayer);

		LOGGER.info("Arborealis Initialised!");
	}
}
