package com.youarethomas.arborealis.misc;

import com.youarethomas.arborealis.util.ArborealisNbt;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;

public class ArborealisPersistentState extends PersistentState {

    private Map<BlockPos, String> warpCores = new HashMap<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.put("warp_cores", ArborealisNbt.serializeCorePosList(warpCores));
        return nbt;
    }

    public static ArborealisPersistentState fromNbt(NbtCompound nbt) {
        ArborealisPersistentState state = new ArborealisPersistentState();
        state.setWarpCoreList(ArborealisNbt.deserializeCorePosList(nbt.getList("warp_cores", NbtElement.COMPOUND_TYPE)));

        return state;
    }

    public Map<BlockPos, String> getWarpCoreList() {
        return warpCores;
    }

    public void setWarpCoreList(Map<BlockPos, String> blockPosList) {
        warpCores = blockPosList;
    }

    public void addWarpCore(BlockPos pos, String name) {
        if (!warpCores.containsKey(pos))
            warpCores.put(pos, name);
        markDirty();
    }

    public void removeWarpCore(BlockPos pos) {
        warpCores.remove(pos);
        markDirty();
    }
}
