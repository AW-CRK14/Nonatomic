package com.landis.nonatomic.registry;

import com.landis.nonatomic.Nonatomic;
import com.landis.nonatomic.extend.TestTool;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

public class ItemRegistry {
    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(Nonatomic.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<TestTool.I1> I1 = REGISTER.register("i1", TestTool.I1::new);
    public static final RegistrySupplier<TestTool.I2> I2 = REGISTER.register("i2", TestTool.I2::new);
}
