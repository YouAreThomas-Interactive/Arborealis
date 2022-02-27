package com.youarethomas.arborealis.misc;

import com.youarethomas.arborealis.util.ArborealisNbt;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.List;

public class ArborealisPersistentState extends PersistentState {

    private List<BlockPos> warpCores = new ArrayList<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.put("warp_cores", ArborealisNbt.serializeBlockPosList(warpCores));
        return nbt;
    }

    public static ArborealisPersistentState fromNbt(NbtCompound nbt) {
        ArborealisPersistentState state = new ArborealisPersistentState();
        state.setWarpCoreList(ArborealisNbt.deserializeBlockPosList(nbt.getList("warp_cores", NbtElement.COMPOUND_TYPE)));

        return state;
    }

    public List<BlockPos> getWarpCoreList() {
        return warpCores;
    }

    public void setWarpCoreList(List<BlockPos> blockPosList) {
        warpCores = blockPosList;
    }

    public void addWarpCore(BlockPos pos) {
        if (!warpCores.contains(pos))
            warpCores.add(pos);
        markDirty();
    }

    public void removeWarpCore(BlockPos pos) {
        warpCores.remove(pos);
        markDirty();
    }
}
