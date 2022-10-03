package com.worldedit1234.hunt.entity;

import com.mojang.logging.LogUtils;
import com.worldedit1234.hunt.setup.Setup;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class OwnedFireball extends LargeFireball {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Vec3 origin;
    private final LivingEntity owner;

    public OwnedFireball(Level level, @NotNull LivingEntity owner, Vec3 vector) {
        super(level, owner,
                vector.x * Setup.FIREBALL_SPEED,
                vector.y * Setup.FIREBALL_SPEED,
                vector.z * Setup.FIREBALL_SPEED,
                Setup.FIREBALL_POWER);

        this.origin = new Vec3(owner.getX(), owner.getY(), owner.getZ());
        this.owner = owner;
        LOGGER.info("New Fireball {}", this.getUUID());
    }

    @Override
    public void tick() {
        super.tick();
        if (Mth.sqrt((float) this.distanceToSqr(origin)) > Setup.MAX_FIREBALL_TRAVEL) {
            this.kill();
            LOGGER.info("Fireball {} is too far from origin.", this.getUUID());
        }
    }

    @NotNull
    @Override
    public LivingEntity getOwner() {
        return this.owner;
    }
}
