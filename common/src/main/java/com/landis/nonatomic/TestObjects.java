package com.landis.nonatomic;

import com.landis.nonatomic.core.*;
import com.landis.nonatomic.core.player_opehandler.OpeHandlerNoRepetition;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.NonnullDefault;

import java.util.Optional;
import java.util.UUID;

public class TestObjects {
    public static void initTest(){
        EntityAttributeRegistry.register(EntityTypeRegistry.TEST, Zombie::createAttributes);

        EntityRendererRegistry.register(EntityTypeRegistry.TEST, VillagerKnightRender::new);

        Nonatomic.registerHandlerEventsArchLike(OpeProvider.INSTANCE);
    }

    public static void initTestRegistry(){
        TestObjects.ItemRegistry.REGISTER.register();
        TestObjects.EntityTypeRegistry.REGISTER.register();
        REGISTER.register();
    }


    @ExpectPlatform
    @NonnullDefault
    public static OpeHandlerNoRepetition.LevelContainer opeHandlerGroupProvider(MinecraftServer server){
        throw new UnsupportedOperationException();
    }


    public static class TestTool extends Item {
        public TestTool() {
            super(new Properties());
        }

        public static class I1 extends TestTool {
            @Override
            public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
                if (!level.isClientSide()) {
                    opeHandlerGroupProvider(level.getServer()).deploy(TestObjects.TEST_OPERATOR.get(),(ServerPlayer) player,null);
                }
                return super.use(level, player, interactionHand);
            }
        }

        public static class I2 extends TestTool {

            @Override
            public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity livingEntity, InteractionHand interactionHand) {
                if(player instanceof ServerPlayer p && livingEntity instanceof OperatorEntity ope) {
                    opeHandlerGroupProvider(p.getServer()).findOperator(TestObjects.TEST_OPERATOR.get(), (ServerPlayer) player).get().retreat(false);
                }
                return super.interactLivingEntity(itemStack, player, livingEntity, interactionHand);
            }
        }

        public static class I3 extends TestTool {
            @Override
            public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
                if (!level.isClientSide()) {
                    opeHandlerGroupProvider(level.getServer()).findOperator(TestObjects.TEST_OPERATOR.get(), (ServerPlayer) player).get().deploy(true,false,-1,null, Operator.STATUS_WORKING);
                }
                return super.use(level, player, interactionHand);
            }
        }
    }

    public static class OpeProvider implements OpeHandler.GroupProvider{
        public static final OpeProvider INSTANCE = new OpeProvider();

        private OpeProvider(){}

        @Override
        public Optional<OpeHandler> withUUID(UUID playerUUID, MinecraftServer server) {
            return Optional.ofNullable(opeHandlerGroupProvider(server).getDataFor(playerUUID));
        }

        @Override
        public Optional<OpeHandler> withPlayer(ServerPlayer player) {
            return Optional.ofNullable(opeHandlerGroupProvider(player.getServer()).getDataFor(player));
        }
    }


    public static final DeferredRegister<OperatorType> REGISTER = DeferredRegister.create(Nonatomic.MOD_ID, Registries.Keys.OPERATOR_TYPE);

    public static final RegistrySupplier<TestOperator> TEST_OPERATOR = REGISTER.register("test", TestOperator::new);

    public static class TestOperator extends OperatorType {
        @Override
        public Operator createDefaultInstance(OpeHandler handler) {
            return new Operator(TEST_OPERATOR.get());
        }

        @Override
        public @Nullable EntityType<? extends OperatorEntity> getEntityType() {
            return EntityTypeRegistry.TEST.get();
        }
    }

    public static class EntityTypeRegistry {
        public static final DeferredRegister<EntityType<?>> REGISTER = DeferredRegister.create(Nonatomic.MOD_ID, net.minecraft.core.registries.Registries.ENTITY_TYPE);

        public static final RegistrySupplier<EntityType<OperatorEntity>> TEST = REGISTER.register("test", () -> EntityType.Builder.<OperatorEntity>of(OperatorEntity::new, MobCategory.MISC).sized(0.6F,1.9F).clientTrackingRange(32).canSpawnFarFromPlayer().build("test"));
    }

    public static class ItemRegistry {
        public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(Nonatomic.MOD_ID, net.minecraft.core.registries.Registries.ITEM);

        public static final RegistrySupplier<TestTool.I1> I1 = REGISTER.register("i1", TestTool.I1::new);
        public static final RegistrySupplier<TestTool.I2> I2 = REGISTER.register("i2", TestTool.I2::new);
    }

    public static class VillagerKnightModel<T extends OperatorEntity> extends HumanoidModel<T> {
        public VillagerKnightModel(ModelPart modelPart) {
            super(modelPart);
        }

        @Override
        public void prepareMobModel(T mobEntity, float f, float g, float h) {
            this.rightArmPose = ArmPose.EMPTY;
            this.leftArmPose = ArmPose.EMPTY;
            ItemStack itemStack = mobEntity.getItemInHand(InteractionHand.MAIN_HAND);
            if (itemStack.is(Items.BOW) && mobEntity.isAggressive()) {
                if (mobEntity.getMainArm() == HumanoidArm.RIGHT) {
                    this.rightArmPose = ArmPose.BOW_AND_ARROW;
                } else {
                    this.leftArmPose = ArmPose.BOW_AND_ARROW;
                }
            }
            super.prepareMobModel(mobEntity, f, g, h);
        }

        public boolean isAttacking(T entity) {
            return entity.isAggressive();
        }
    }

    public static class VillagerKnightRender<T extends OperatorEntity> extends HumanoidMobRenderer<T, VillagerKnightModel<T>> {
        private static final ResourceLocation TEXTURE = new ResourceLocation(Nonatomic.MOD_ID, "textures/entity/villager_knight.png");
        public VillagerKnightRender(EntityRendererProvider.Context context) {
            this(context, ModelLayers.PLAYER, ModelLayers.PLAYER_INNER_ARMOR, ModelLayers.PLAYER_OUTER_ARMOR);
        }

        public VillagerKnightRender(EntityRendererProvider.Context ctx, ModelLayerLocation layer, ModelLayerLocation legsArmorLayer, ModelLayerLocation bodyArmorLayer) {
            this(ctx, new VillagerKnightModel<>(ctx.bakeLayer(layer)), new VillagerKnightModel<>(ctx.bakeLayer(legsArmorLayer)), new VillagerKnightModel<>(ctx.bakeLayer(bodyArmorLayer)));
        }
        protected VillagerKnightRender(EntityRendererProvider.Context ctx, VillagerKnightModel<T> bodyModel, VillagerKnightModel<T> legsArmorModel, VillagerKnightModel<T> bodyArmorModel) {
            super(ctx, bodyModel, 0.5F);
            this.addLayer(new HumanoidArmorLayer<>(this, legsArmorModel, bodyArmorModel, ctx.getModelManager()));
        }

        @Override
        public @NotNull ResourceLocation getTextureLocation(OperatorEntity entity) {
            return TEXTURE;
        }

    }
}
