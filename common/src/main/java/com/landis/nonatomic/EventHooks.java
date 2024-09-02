package com.landis.nonatomic;

import com.landis.nonatomic.core.Operator;
import com.landis.nonatomic.core.OperatorEntity;
import com.landis.nonatomic.core.OperatorType;
import com.landis.nonatomic.datagroup.Deploy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

//TODO
public class EventHooks {
    /**是否允许干员保持为部署状态
     * @return 分位2决定是(1)否标记再部署 分位1决定是(1)否存在*/
    public static int allowOperatorStayDeploying(ServerPlayer player, OperatorType type, Operator operator, OperatorEntity entity){
        return (player.getServer().isSingleplayer() || operator.deploy.status != Deploy.STATUS_TRACKING) ? 0b01 : 0b10;
    }

    public static boolean allowOperatorRetreat(ServerPlayer player, OperatorType type, Operator operator, OperatorEntity entity){
        return true;
    }
}
