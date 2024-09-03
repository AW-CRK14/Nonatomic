package com.landis.nonatomic.neoforge.registry;

import com.landis.nonatomic.Nonatomic;
import com.landis.nonatomic.core.player_opehandler.OpeHandlerNoRepetition;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class DataAttachmentRegistry {

    public static final DeferredRegister<AttachmentType<?>> REGISTER = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Nonatomic.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<OpeHandlerNoRepetition.LevelContainer>> OPE_HANDLER =
            REGISTER.register("ope_handler", () -> AttachmentType.builder(() -> new OpeHandlerNoRepetition.LevelContainer(4,true)).serialize(OpeHandlerNoRepetition.LevelContainer.CODEC).build());
}
