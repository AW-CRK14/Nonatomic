package com.phasetranscrystal.nonatomic;

import com.phasetranscrystal.nonatomic.core.Operator;
import com.phasetranscrystal.nonatomic.core.OperatorEntity;
import com.phasetranscrystal.nonatomic.core.OperatorInfo;
import com.phasetranscrystal.nonatomic.event.EntityUninstallByChunkEvent;
import com.phasetranscrystal.nonatomic.event.OperatorEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.entity.EntityAccess;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;

public class EventHooks {

    public static Operator findOperator(MinecraftServer server, OperatorEntity unGeneratedEntity) {
        return NeoForge.EVENT_BUS.post(new OperatorEvent.FindOperator(server, unGeneratedEntity)).getResult();
    }

    public static boolean onPlayerLogoutOpe(ServerPlayer player, Operator operator, OperatorEntity entity) {
        return NeoForge.EVENT_BUS.post(new OperatorEvent.OnPlayerLogout(player, operator, entity)).result();
    }

    public static boolean preDeploy(Operator operator, OperatorEntity entity, ServerPlayer player) {
        return !NeoForge.EVENT_BUS.post(new OperatorEvent.Deploy.Pre(operator, entity, player)).isCanceled();
    }

    public static void onDeploy(Operator operator, OperatorEntity entity, ServerPlayer player) {
        NeoForge.EVENT_BUS.post(new OperatorEvent.Deploy.Post(operator, entity, player));
    }

    public static void deployFailed(Operator operator, ServerPlayer player, int flag) {
        NeoForge.EVENT_BUS.post(new OperatorEvent.DeployFailed(operator, player, flag));
    }

    public static void onLoad(Operator operator, OperatorEntity entity, ServerPlayer player) {
        NeoForge.EVENT_BUS.post(new OperatorEvent.OperatorLoaded(operator, entity, player));
    }

    public static Optional<ResourceLocation> preRetreat(ServerPlayer player, Operator operator, Operator.RetreatReason reason) {
        var event = NeoForge.EVENT_BUS.post(new OperatorEvent.Retreat.Pre(player, operator, reason));
        return event.isCanceled() ? Optional.empty() : Optional.of(event.getFinalStatus());
    }

    public static void onRetreat(Operator operator, Operator.RetreatReason reason) {
        NeoForge.EVENT_BUS.post(new OperatorEvent.Retreat.Post(operator, reason));
    }

    public static int allowDataMerge(Operator.RetreatReason reason, OperatorEntity entity, Operator operator, OperatorInfo info) {
        var event = NeoForge.EVENT_BUS.post(new OperatorEvent.MergeData(reason, entity, operator, info));
        return event.isCanceled() ? event.isDelete() ? -1 : 0 : 1;
    }

    public static boolean ifTakeDeployPlace(Operator operator, ResourceLocation status) {
        return NeoForge.EVENT_BUS.post(new OperatorEvent.JudgeDeployingPlace(operator, status)).getResult();
    }

    public static boolean onEntityUninstallByChunk(EntityAccess access){
        return NeoForge.EVENT_BUS.post(new EntityUninstallByChunkEvent(access)).isCanceled();
    }
}
