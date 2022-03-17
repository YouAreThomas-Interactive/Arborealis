package com.youarethomas.arborealis.particles;

import com.youarethomas.arborealis.Arborealis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

public class WarpTreeParticle extends SpriteBillboardParticle {

    protected WarpTreeParticle(ClientWorld clientWorld, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(clientWorld, x, y, z, velocityX, velocityY, velocityZ);
        this.velocityX = velocityX * 0.05D;
        this.velocityY = velocityY * 0.05D;
        this.velocityZ = velocityZ * 0.05D;
        this.collidesWithWorld = false;
        this.red = 0.2f;
        this.green = ((float)Math.random() * 0.2f) + 0.4f;
        this.blue = ((float)Math.random() * 0.2f) + 0.4f;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Environment(value= EnvType.CLIENT)
    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            WarpTreeParticle warpTreeParticle = new WarpTreeParticle(clientWorld, x, y, z, velocityX, velocityY, velocityZ);
            warpTreeParticle.setSprite(this.spriteProvider);
            return warpTreeParticle;
        }
    }
}
