package com.landis.nonatomic;

import com.landis.nonatomic.core.OperatorEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

//TODO
public class EventHooks {
    /**是否允许干员保持为部署状态
     * @return 分位2决定是(1)否标记再部署 分位1决定是(1)否存在*/
    public static int allowOperatorStayDeploying(ServerPlayer player, ResourceLocation operator, OperatorEntity entity){
        return player.getServer().isSingleplayer() ? 0b01 : 0b10;
    }
}
