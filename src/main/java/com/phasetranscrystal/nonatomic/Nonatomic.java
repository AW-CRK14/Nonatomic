package com.phasetranscrystal.nonatomic;

import com.phasetranscrystal.nonatomic.core.OperatorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(Nonatomic.MOD_ID)
public final class Nonatomic {
    public static final String MOD_ID = "nonatomic";

    public Nonatomic(IEventBus modEventBus, ModContainer modContainer) {
        OPERATOR_TYPE_REGISTER.register(modEventBus);
        TestObjects.initTest(modEventBus);
    }

    public static final DeferredRegister<OperatorType> OPERATOR_TYPE_REGISTER = DeferredRegister.create(Registries.Keys.OPERATOR_TYPE, Nonatomic.MOD_ID);

    public static final DeferredHolder<OperatorType,OperatorType.Placeholder> PLACE_HOLDER = OPERATOR_TYPE_REGISTER.register("placeholder", OperatorType.Placeholder::new);
}
