package com.phasetranscrystal.nonatomic.core;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.lwjgl.system.NonnullDefault;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class OperatorEntity extends Mob {

    public UUID getBelongingUUID() {
        return belonging;
    }

    public Operator.Identifier getIdentifier() {
        return identifier;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    private @NonnullDefault UUID belonging;
    private @NonnullDefault Operator.Identifier identifier;
    private @NonnullDefault Operator operator;
    public final boolean opeNewCreatedFlag;

    public OperatorEntity(EntityType<? extends OperatorEntity> entityType, Level level) {
        super(entityType, level);
        opeNewCreatedFlag = false;
    }

    //用这个创建您的新干员
    public OperatorEntity(EntityType<? extends OperatorEntity> entityType, ServerLevel level, Player belonging, Operator operatorData) {
        super(entityType, level);
        this.belonging = belonging.getUUID();
        this.operator = operatorData;
        this.identifier = operatorData.identifier;
        this.opeNewCreatedFlag = true;
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        belonging = compoundTag.contains("ope_belonging") ? compoundTag.getUUID("ope_belonging") : null;
        identifier = Operator.Identifier.CODEC.parse(NbtOps.INSTANCE, compoundTag.get("ope_identifier")).mapOrElse(i -> i, e -> null);
    }

    @Override
    public CompoundTag saveWithoutId(CompoundTag compoundTag) {
        compoundTag.putUUID("ope_belonging", belonging);
        compoundTag.put("ope_identifier", Operator.Identifier.CODEC.encode(identifier, NbtOps.INSTANCE, new CompoundTag()).getOrThrow());
        return super.saveWithoutId(compoundTag);
    }

    public void opeInit() {
    }

    /**
     * 可以覆写此方法，此方法在允许数据合并时被调用。
     */
    protected Collection<? extends OperatorInfo> requestExternalData() {
        return Collections.emptyList();
    }

    @Override
    public boolean shouldRender(double d, double e, double f) {
        return super.shouldRender(d, e, f);
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return false;
    }

    public ServerPlayer getOwner() {
        return getOperator().getOpeHandler().owner();
    }

    public void transDimension() {
        BlockPos toPos = identifier.type().findPlaceForGenerate(getOwner(), null);
        if (toPos == null) this.operator.retreat(true);
        else
            teleportTo((ServerLevel) getOwner().level(), toPos.getX() + 0.5, toPos.getY(), toPos.getZ() + 0.5, Set.of(), getYRot(), getXRot());
    }

    //可以在这里写一点撤退动画
    public void onRetreat() {
        this.remove(RemovalReason.UNLOADED_WITH_PLAYER);
    }
}
