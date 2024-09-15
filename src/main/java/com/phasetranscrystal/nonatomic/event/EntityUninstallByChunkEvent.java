package com.phasetranscrystal.nonatomic.event;

import net.minecraft.world.level.entity.EntityAccess;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * 在实体被区块卸载时触发。
 * 此事件仅在{@link net.neoforged.neoforge.common.NeoForge#EVENT_BUS 游戏主线}上，并只在{@link net.neoforged.fml.LogicalSide#SERVER 逻辑服务端}上生效。<p>
 * 此事件{@link ICancellableEvent 可取消}，取消后实体不会被卸载。注意，这不会阻止区块被卸载，可能引发其它问题。您需要对实体执行一些操作，例如将其传送到安全位置等。<p>
 * <strong>不要在你对自己的行为不确定的时候取消这个事件</strong>
 * <p>
 * Fired when entity unload by chunk.
 * Event only fired on {@link net.neoforged.neoforge.common.NeoForge#EVENT_BUS GameBus} and {@link net.neoforged.fml.LogicalSide#SERVER Server Side}.<p>
 * This event is {@link ICancellableEvent Cancellable} then the entity won't unload. Notes that this won't prevent chunk from uninstall so may lead to some other problems.
 * You may need to do something such us teleport the entity to safe position.<p>
 * <strong>DON'T CANCEL THIS EVENT UNTIL YOU KNOW WHAT YOU'RE DOING.</strong>
 *
 * @see com.phasetranscrystal.nonatomic.mixin.EntityUnloadMixin EntityUnloadMixin
 */
public class EntityUninstallByChunkEvent extends Event implements ICancellableEvent {
    public final EntityAccess entity;

    public EntityUninstallByChunkEvent(EntityAccess entity) {
        this.entity = entity;
    }
}
