package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.mixin_access.ServerWorldMixinAccess;
import com.youarethomas.arborealis.runes.Rune;
import com.youarethomas.arborealis.util.ArborealisNbt;
import com.youarethomas.arborealis.util.ArborealisUtil;
import com.youarethomas.arborealis.util.TreeManager;
import com.youarethomas.arborealis.util.TreeStructure;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

public class BeamEmittingBlockEntity extends BlockEntity {

    public enum BeamModifier {
        NONE,
        STENCIL,
        INFUSION,
        IMPLOSION
    }

    public Map<Direction, Boolean> faceBeamActive = new HashMap<>() {{
        put(Direction.UP, false);
        put(Direction.DOWN, false);
        put(Direction.NORTH, false);
        put(Direction.EAST, false);
        put(Direction.SOUTH, false);
        put(Direction.WEST, false);
    }};
    public Map<Direction, Integer> faceThrowDistance = new HashMap<>() {{
        put(Direction.UP, 0);
        put(Direction.DOWN, 0);
        put(Direction.NORTH, 0);
        put(Direction.EAST, 0);
        put(Direction.SOUTH, 0);
        put(Direction.WEST, 0);
    }};
    public Map<Direction, BlockPos> faceBeamEndBlock = new HashMap<>() {{
        put(Direction.UP, null);
        put(Direction.DOWN, null);
        put(Direction.NORTH, null);
        put(Direction.EAST, null);
        put(Direction.SOUTH, null);
        put(Direction.WEST, null);
    }};

    private int lightLevel;
    private BeamModifier beamModifier = BeamModifier.NONE;
    private BeamModifier beamModifierLastCheck = BeamModifier.NONE;
    private int[] stencilPattern;
    private ArborealisUtil.Colour beamColour;

    public BeamEmittingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // region NBT Properties
    public boolean getBeamActive(Direction direction) {
        return faceBeamActive.get(direction);
    }
    public void setBeamActive(Direction direction, boolean active) {
        faceBeamActive.replace(direction, active);
        markDirty();
    }

    public int getThrowDistance(Direction direction) {
        return faceThrowDistance.get(direction);
    }
    public void setThrowDistance(Direction direction, int throwDistance) {
        faceThrowDistance.replace(direction, throwDistance);
        markDirty();
    }

    public BlockPos getBeamEndBlock(Direction direction) {
        return faceBeamEndBlock.get(direction);
    }
    public void setBeamEndBlock(Direction direction, BlockPos lastBlock) {
        faceBeamEndBlock.replace(direction, lastBlock);
        markDirty();
    }

    public int getLightLevel() {
        return lightLevel;
    }
    public void setLightLevel(int lightLevel) {
        this.lightLevel = lightLevel;
        markDirty();
    }

    public int[] getStencilPattern() {
        return stencilPattern;
    }
    public void setStencilPattern(int[] stencilPattern) {
        this.stencilPattern = stencilPattern;
        markDirty();
    }

    public BeamModifier getBeamModifier() {
        return beamModifier;
    }
    public void setBeamModifier(BeamModifier beamModifier) {
        this.beamModifier = beamModifier;
        markDirty();
    }

    public BeamModifier getBeamModifierLastCheck() {
        return beamModifierLastCheck;
    }
    public void setBeamModifierLastCheck(BeamModifier beamModifierLastCheck) {
        this.beamModifierLastCheck = beamModifierLastCheck;
        markDirty();
    }

    public ArborealisUtil.Colour getBeamColour() {
        return beamColour;
    }
    public void setBeamColour(ArborealisUtil.Colour beamColour) {
        this.beamColour = beamColour;
        markDirty();
    }
    // endregion

    /**
     * Call from clientTick to display beamParticles
     */
    public void createBeamParticles(World world, BlockPos pos, BlockState state, BeamEmittingBlockEntity be) {
        // Beam particles
        for (Direction dir : Direction.values()) {
            if (be.getBeamActive(dir) && be.getLightLevel() > 0 && be.getThrowDistance(dir) > 0 && world.random.nextInt(be.getLightLevel() * 4) < be.getThrowDistance(dir)) {
                // Get the box for the beam
                Box beamBox = new Box(pos.offset(dir, 1), pos.offset(dir, 1 + be.getThrowDistance(dir)));

                // Calculate a random coordinate within that box
                double randX = beamBox.minX + (((beamBox.maxX + 1) - beamBox.minX) * world.random.nextFloat());
                double randY = beamBox.minY + (((beamBox.maxY + 1) - beamBox.minY) * world.random.nextFloat());
                double randZ = beamBox.minZ + (((beamBox.maxZ + 1) - beamBox.minZ) * world.random.nextFloat());

                world.addParticle(ParticleTypes.END_ROD, randX, randY, randZ, 0, 0, 0);
            }
        }
    }

    public void recalculateBeam(Direction dir) {
        // Recalculate beam lengths
        boolean beamChanged = false;

        if (getBeamActive(dir) && getLightLevel() > 0) {
            int beamRange = -1;
            boolean hitBlock = false;
            for (int i = 0; i < getLightLevel(); i++) {
                BlockPos testPos = pos.offset(dir, i + 1);

                if (!world.getBlockState(testPos).isIn(Arborealis.PROJECTOR_TRANSPARENT)) {
                    beamRange = i;
                    setBeamEndBlock(dir, testPos); // Save blockpos hit
                    hitBlock = true;
                    break;
                }
            }

            if (!hitBlock) setBeamEndBlock(dir, null);

            // Set beam length to distance, otherwise if it was never blocked cap it at the light source level
            if ((beamRange != -1 && getThrowDistance(dir) != beamRange) || (beamRange == -1 && getThrowDistance(dir) != getLightLevel())) {
                setThrowDistance(dir, beamRange == -1 ? getLightLevel() : beamRange);
                beamChanged = true;
            }
        } else if (getThrowDistance(dir) != 0) {
            setThrowDistance(dir, 0);
            beamChanged = true;
        }

        if (!beamChanged && beamModifier == beamModifierLastCheck) {
            return;  // Bail if nothing has changed - re-process not necessary
        } else {
            // Handle prisms
            BlockPos endPos = getBeamEndBlock(dir);
            if (endPos != null && world.getBlockState(endPos).isOf(Arborealis.PRISM_BLOCK)) {
                PrismBlockEntity prismBlockEntity = (PrismBlockEntity) world.getBlockEntity(endPos);

                if (prismBlockEntity != null) {
                    prismBlockEntity.setLightLevel(getLightLevel() - getThrowDistance(dir) - 1);
                    prismBlockEntity.setBeamModifier(beamModifier);
                    prismBlockEntity.setBeamColour(beamColour);
                    prismBlockEntity.setStencilPattern(stencilPattern);
                    prismBlockEntity.recalculateAllBeams();
                }
            }

            resetProjection(beamModifier, dir); // Handles resetting blocks if the project is turned off or blocked
            System.out.println("Beam changed. Resetting modifiers");
        }

        if (getThrowDistance(dir) > 0) {
            processModifiers(dir);
        }
    }

    public void recalculateAllBeams() {
        for (Direction dir : Direction.values()) {
            recalculateBeam(dir);
        }

        setBeamModifierLastCheck(beamModifier);
    }

    private void processModifiers(Direction dir) {
        BlockPos endPos = getBeamEndBlock(dir);
        if (endPos == null)
            return;

        BlockState stateAtPos = world.getBlockState(endPos);

        if (beamModifier == BeamModifier.STENCIL) {// If the block clicked on is wood, create a new carved wood block, otherwise set the existing one
            if (stateAtPos.isIn(BlockTags.LOGS) || stateAtPos.isOf(Blocks.PUMPKIN)) {
                // Swap the block out with a carved wood block...
                if (stateAtPos.isIn(BlockTags.LOGS_THAT_BURN)) {
                    world.setBlockState(endPos, Arborealis.CARVED_LOG.getDefaultState());
                } else {
                    world.setBlockState(endPos, Arborealis.CARVED_NETHER_LOG.getDefaultState());
                }

                CarvedLogEntity carvedLog = (CarvedLogEntity) world.getBlockEntity(endPos);

                // ... and assign relevant NBT data
                if (carvedLog != null && getStencilPattern() != null) {
                    carvedLog.setLogState(stateAtPos);

                    carvedLog.setFaceCatalysed(dir.getOpposite(), true);
                    carvedLog.showProjectedRune(dir.getOpposite(), getStencilPattern());
                }
            } else if (stateAtPos.isIn(Arborealis.CARVED_LOGS) && getStencilPattern() != null) {
                CarvedLogEntity carvedLog = (CarvedLogEntity) world.getBlockEntity(endPos);

                if (carvedLog != null) {
                    carvedLog.setFaceCatalysed(dir.getOpposite(), true);
                    carvedLog.showProjectedRune(dir.getOpposite(), getStencilPattern());
                }
            }
        } else if (beamModifier == BeamModifier.INFUSION) {
            if (!stateAtPos.isOf(Arborealis.HOLLOWED_LOG)) return;

            HollowedLogEntity hollowedLogEntity = (HollowedLogEntity) world.getBlockEntity(endPos);

            if (hollowedLogEntity != null) {
                hollowedLogEntity.setHasInfusionBeam(true);
            }
        } else if (beamModifier == BeamModifier.IMPLOSION) {
            if (!stateAtPos.isOf(Arborealis.HOLLOWED_LOG)) return;

            ServerWorldMixinAccess serverWorld = (ServerWorldMixinAccess) world;
            TreeManager treeManager = serverWorld.getTreeManager();

            TreeStructure tree;
            if (treeManager.isBlockInTreeStructure(endPos))
                tree = treeManager.getTreeStructureFromPos(endPos, world);
            else
                tree = treeManager.constructTreeStructureFromBlock(endPos, (ServerWorld) world);

            if (tree != null && tree.isNatural()) {
                // Get all runes from the tree
                List<Rune> runesOnTree = new ArrayList<>();
                for (BlockPos logPos : tree.logs) {
                    if (world.getBlockState(logPos).isIn(Arborealis.CARVED_LOGS)) {
                        CarvedLogEntity carvedLog = (CarvedLogEntity)world.getBlockEntity(logPos);
                        runesOnTree.addAll(carvedLog.runesPresentLastCheck.stream().filter(newRune -> runesOnTree.stream().noneMatch(rune -> newRune.name.equals(rune.name))).toList());
                    }
                }

                // Build core item with all runes stored on it
                ItemStack implodedCore = Arborealis.LIFE_CORE.getDefaultStack().split(1);

                if (runesOnTree.size() > 0) {
                    NbtCompound nbt = implodedCore.getOrCreateNbt();
                    NbtElement runeList = ArborealisNbt.serializeRuneList(runesOnTree);
                    nbt.put("rune_list", runeList);
                    implodedCore.setNbt(nbt);
                }

                // Chop tree and drop core
                tree.chopTreeStructure(world, false);
                Vec3d coreSpawnPos = Vec3d.ofCenter(endPos);
                world.spawnEntity(new ItemEntity(world, coreSpawnPos.x, coreSpawnPos.y, coreSpawnPos.z, implodedCore));
            }
        } else if (beamModifier == BeamModifier.NONE) {
            resetProjection(beamModifierLastCheck, dir);
        }
    }

    private void resetProjection(BeamModifier lastModifier, Direction dir) {
        if (getBeamEndBlock(dir) == null) {
            return;
        }

        // Reset everything
        if (lastModifier == BeamModifier.STENCIL && world.getBlockState(getBeamEndBlock(dir)).isIn(Arborealis.CARVED_LOGS)) {
            CarvedLogEntity carvedLog = (CarvedLogEntity) world.getBlockEntity(getBeamEndBlock(dir));

            if (carvedLog != null) {
                carvedLog.setFaceCatalysed(dir.getOpposite(), false);
                carvedLog.showProjectedRune(dir.getOpposite(), new int[49]);

                boolean blockReset = true;
                for (Direction faceDir : Direction.values()) {
                    if (!Arrays.deepEquals(ArrayUtils.toObject(carvedLog.getFaceArray(faceDir)), ArrayUtils.toObject(new int[49]))) {
                        blockReset = false;
                    }
                }

                // If no sides are carved, reset to respective log block.
                if (blockReset) {
                    if (!world.isClient) {
                        world.setBlockState(getBeamEndBlock(dir), carvedLog.getLogState());
                    }
                }
            }
        } else if (lastModifier == BeamModifier.INFUSION && world.getBlockState(getBeamEndBlock(dir)).isOf(Arborealis.HOLLOWED_LOG)) {
            HollowedLogEntity entity = (HollowedLogEntity) world.getBlockEntity(getBeamEndBlock(dir));
            if (entity != null)
                entity.setHasInfusionBeam(false);
        }
    }

    // Serialize the BlockEntity - storing data
    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        for (Direction face : Direction.values()) {
            tag.putBoolean("active_" + face.getName(), faceBeamActive.get(face));
            tag.putInt("throw_" + face.getName(), faceThrowDistance.get(face));

            if (faceBeamEndBlock.get(face) != null)
                tag.put("last_block_" + face.getName(), NbtHelper.fromBlockPos(faceBeamEndBlock.get(face)));
        }

        tag.putString("beam_modifier", beamModifier.toString());
        tag.putString("beam_modifier_last", beamModifierLastCheck.toString());
        tag.putInt("light_level", lightLevel);
        if (stencilPattern != null) tag.putIntArray("stencil_pattern", stencilPattern);
        if (beamColour != null) tag.put("beam_colour", ArborealisNbt.serializeColour(beamColour));
    }

    // Deserialize the BlockEntity - retrieving data
    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        for (Direction face : Direction.values()) {
            faceBeamActive.replace(face, tag.getBoolean("active_" + face.getName()));
            faceThrowDistance.replace(face, tag.getInt("throw_" + face.getName()));
            if (tag.contains("last_block_" + face.getName()))
                faceBeamEndBlock.replace(face, NbtHelper.toBlockPos(tag.getCompound("last_block_" + face.getName())));
        }

        beamModifier = Enum.valueOf(BeamModifier.class, tag.getString("beam_modifier"));
        beamModifierLastCheck = Enum.valueOf(BeamModifier.class, tag.getString("beam_modifier_last"));
        lightLevel = tag.getInt("light_level");
        if (tag.contains("stencil_pattern")) stencilPattern = tag.getIntArray("stencil_pattern");
        if (tag.contains("beam_colour")) beamColour = ArborealisNbt.deserializeColour(tag.getCompound("beam_colour"));

        this.markDirty();
    }

    @Override
    public void markDirty() {
        super.markDirty();

        if (this.getWorld() != null) {
            if (!this.getWorld().isClient())
                ((ServerWorld) world).getChunkManager().markForUpdate(getPos());
            else
                world.updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL | Block.FORCE_STATE);
        }
    }

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return this.createNbt();
    }
}
