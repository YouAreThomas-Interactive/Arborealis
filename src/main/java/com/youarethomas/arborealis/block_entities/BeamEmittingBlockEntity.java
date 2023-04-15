package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.items.lenses.ProjectionModifierItem;
import com.youarethomas.arborealis.util.ArborealisUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeamEmittingBlockEntity extends BlockEntity {

    private int lightLevel = 0;

    private final Map<Direction, ProjectionBeam> beams = new HashMap<>() {{
       put(Direction.NORTH, new ProjectionBeam(Direction.NORTH));
       put(Direction.SOUTH, new ProjectionBeam(Direction.SOUTH));
       put(Direction.EAST, new ProjectionBeam(Direction.EAST));
       put(Direction.WEST, new ProjectionBeam(Direction.WEST));
       put(Direction.UP, new ProjectionBeam(Direction.UP));
       put(Direction.DOWN, new ProjectionBeam(Direction.DOWN));
    }};

    public int getLightLevel() {
        return lightLevel;
    }
    public void setLightLevel(int lightLevel) {
        this.lightLevel = lightLevel;
        markDirty();
    }

    public ProjectionBeam getBeam(Direction direction) {
        return beams.get(direction);
    }

    public BeamEmittingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void recalculateAllBeams() {
        for (ProjectionBeam beam : beams.values()) {
            beam.recalculateBeam();
        }
    }

    public void setAllBeamItemStacks(ItemStack stack) {
        for (ProjectionBeam beam : beams.values()) {
            beam.setBeamItemStack(stack);
        }
    }

    // Serialize the BlockEntity - storing data
    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        tag.putInt("light_level", lightLevel);

        for (Direction dir : Direction.values()) {
            tag.put("beam_" + dir.getName(), getBeam(dir).serialize());
        }

    }

    // Deserialize the BlockEntity - retrieving data
    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        for (Direction direction : Direction.values()) {
            beams.replace(direction, new ProjectionBeam(tag.getCompound("beam_" + direction.getName())));
        }

        lightLevel = tag.getInt("light_level");

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

    public class ProjectionBeam {
        private final Direction direction;

        private boolean showBeam = false;
        private int throwDistance = 0;
        private BlockPos beamEndBlock = null;
        private BlockPos beamEndBlockLast = null;
        private ItemStack modifierStack = ItemStack.EMPTY;
        private ItemStack modifierStackLast = ItemStack.EMPTY;

        // Create new beam face
        public ProjectionBeam(Direction direction) {
            this.direction = direction;
        }

        // Create beam face from nbt
        public ProjectionBeam(NbtCompound nbt) {
            direction = Direction.byId(nbt.getInt("direction"));
            showBeam = nbt.getBoolean("show_beam");
            throwDistance = nbt.getInt("throw_distance");
            if (nbt.contains("beam_end_block")) beamEndBlock = NbtHelper.toBlockPos(nbt.getCompound("beam_end_block"));
            if (nbt.contains("beam_end_block_last")) beamEndBlockLast = NbtHelper.toBlockPos(nbt.getCompound("beam_end_block_last"));
            modifierStack = ItemStack.fromNbt(nbt.getCompound("beam_stack"));
            modifierStackLast = ItemStack.fromNbt(nbt.getCompound("last_beam_stack"));

            markDirty();
        }

        public Direction getDirection() {
            return direction;
        }

        public boolean getShowBeam() {
            return showBeam;
        }
        public void setShowBeam(boolean showBeam) {
            this.showBeam = showBeam;
            markDirty();
        }

        public int getThrowDistance() {
            return throwDistance;
        }
        public void setThrowDistance(int throwDistance) {
            this.throwDistance = throwDistance;
            markDirty();
        }

        public BlockPos getBeamEndBlock() {
            return beamEndBlock;
        }
        public void setBeamEndBlock(BlockPos beamEndBlock) {
            this.beamEndBlock = beamEndBlock;
            markDirty();
        }

        public ItemStack getBeamItemStack() {
            return modifierStack;
        }
        public void setBeamItemStack(ItemStack modifierStack) {
            this.modifierStack = modifierStack;
            markDirty();
        }

        public NbtElement serialize() {
            NbtCompound nbt = new NbtCompound();

            nbt.putInt("direction", direction.getId());
            nbt.putBoolean("show_beam", showBeam);
            nbt.putInt("throw_distance", throwDistance);
            if (beamEndBlock != null) nbt.put("beam_end_block", NbtHelper.fromBlockPos(beamEndBlock));
            if (beamEndBlockLast != null) nbt.put("beam_end_block_last", NbtHelper.fromBlockPos(beamEndBlockLast));

            NbtCompound stackNbt = new NbtCompound();
            modifierStack.writeNbt(stackNbt);
            nbt.put("beam_stack", stackNbt);

            NbtCompound lastStackNbt = new NbtCompound();
            modifierStackLast.writeNbt(lastStackNbt);
            nbt.put("last_beam_stack", lastStackNbt);

            return nbt;
        }

        /**
         * Call from clientTick to display beamParticles
         */
        public void createBeamParticles(World world, BlockPos pos, BlockState state) {
            // Beam particles
            for (Direction dir : Direction.values()) {
                if (showBeam && lightLevel > 0 && throwDistance > 0 && world.random.nextInt(lightLevel * 4) < throwDistance) {
                    // Get the box for the beam
                    Box beamBox = new Box(pos.offset(dir, 1), pos.offset(dir, 1 + throwDistance));

                    // Calculate a random coordinate within that box
                    double randX = beamBox.minX + (((beamBox.maxX + 1) - beamBox.minX) * world.random.nextFloat());
                    double randY = beamBox.minY + (((beamBox.maxY + 1) - beamBox.minY) * world.random.nextFloat());
                    double randZ = beamBox.minZ + (((beamBox.maxZ + 1) - beamBox.minZ) * world.random.nextFloat());

                    world.addParticle(ParticleTypes.END_ROD, randX, randY, randZ, 0, 0, 0);
                }
            }
        }

        public void recalculateBeam() {
            boolean beamChanged = false;

            // Recalculate beam length
            if (showBeam && lightLevel > 0) { // Recalculating an active beam
                int beamRange = -1;
                boolean hitBlock = false;
                for (int i = 0; i < getLightLevel(); i++) {
                    BlockPos testPos = pos.offset(direction, i + 1);

                    if (!world.getBlockState(testPos).isIn(Arborealis.PROJECTOR_TRANSPARENT)) {
                        beamRange = i;
                        beamEndBlock = testPos; // Save blockpos hit
                        hitBlock = true;
                        break;
                    }
                }

                if (!hitBlock) beamEndBlock = null;

                // Set beam length to distance, otherwise if it was never blocked cap it at the light source level
                if ((beamRange != -1 && throwDistance != beamRange) || (beamRange == -1 && throwDistance != getLightLevel())) {
                    throwDistance = beamRange == -1 ? getLightLevel() : beamRange;
                    beamChanged = true;
                }
            } else if (throwDistance != 0) { // Turning off the beam
                throwDistance = 0;
                beamChanged = true;
            }

            // Reset old properties and re-apply
            if (beamChanged || modifierStack != modifierStackLast) {
                // Reset all the old shit first
                if (modifierStackLast.getItem() instanceof ProjectionModifierItem modifierItem && beamEndBlockLast != null) {
                    modifierItem.onDeactivated(beamEndBlockLast, world, BeamEmittingBlockEntity.this, this);
                }

                // Process prisms
                BlockPos endPos = beamEndBlock;
                if (endPos != null && world.getBlockState(endPos).isOf(Arborealis.PRISM_BLOCK)) {
                    PrismBlockEntity prismBlockEntity = (PrismBlockEntity) world.getBlockEntity(endPos);

                    if (prismBlockEntity != null) {
                        // Only change light level if no other projectors are shooting it...
                        prismBlockEntity.setLightLevel(getLightLevel() - getThrowDistance() - 1);

                        prismBlockEntity.setSideInput(direction.getOpposite(), showBeam);
                        prismBlockEntity.setAllBeamItemStacks(getBeamItemStack());
                        prismBlockEntity.checkBeamInputs();
                    }
                }

                System.out.println("Beam changed. Resetting modifiers");
            } else {
                return;  // Bail if nothing has changed - re-process not necessary
            }

            // Reapply effects if applicable
            if (throwDistance > 0) {
                if (modifierStack.getItem() instanceof ProjectionModifierItem modifierItem && beamEndBlock != null) {
                    modifierItem.onActivated(beamEndBlock, world, BeamEmittingBlockEntity.this, this);
                }
            }

            beamEndBlockLast = beamEndBlock;
            modifierStackLast = modifierStack;
            markDirty();
        }
    }
}
