package com.youarethomas.arborealis.runes;

import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import com.youarethomas.arborealis.mixin_access.ClientWorldMixinAccess;
import com.youarethomas.arborealis.util.ArborealisUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Muffle extends Rune {

    @Override
    public boolean showRadiusEffect() {
        return true;
    }

    @Override
    public void onClientTick(World world, BlockPos pos, CarvedLogEntity be) {
        ((ClientWorldMixinAccess)world).setMuted(ArborealisUtil.isWithinRadius(MinecraftClient.getInstance().player.getPos(), Vec3d.of(pos), be.radius));
    }

    @Override
    public void onRuneLost(World world, BlockPos pos, CarvedLogEntity be) {
        if (world.isClient)
            ((ClientWorldMixinAccess)world).setMuted(false);
    }
}
