package com.phasetranscrystal.nonatomic;

import com.phasetranscrystal.nonatomic.core.OpeHandler;
import com.phasetranscrystal.nonatomic.core.Operator;
import com.phasetranscrystal.nonatomic.core.OperatorEntity;
import com.phasetranscrystal.nonatomic.core.OperatorType;
import com.phasetranscrystal.nonatomic.core.player_opehandler.OpeHandlerNoRepetition;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.NonnullDefault;

import java.util.Optional;
import java.util.UUID;

public class TestObjects {
    public static void initTest(IEventBus bus) {
        bus.addListener(EntityAttributeCreationEvent.class, event -> {
            event.put(EntityTypeRegistry.TEST.get(), Zombie.createAttributes().build());
        });

        bus.addListener(EntityRenderersEvent.RegisterRenderers.class, event -> {
            event.registerEntityRenderer(EntityTypeRegistry.TEST.get(), VillagerKnightRender::new);
        });

        GameBusConsumer.registerHandlerEventsArchLike(bus, OpeProvider.INSTANCE);

        ItemRegistry.REGISTER.register(bus);
        EntityTypeRegistry.REGISTER.register(bus);
        OperatorTypeRegistry.REGISTER.register(bus);
    }

    public static void initTestRegistry() {

    }


    @NonnullDefault
    public static OpeHandlerNoRepetition.LevelContainer opeHandlerGroupProvider(MinecraftServer server) {
        return server.overworld().getData(DataAttachmentRegistry.OPE_HANDLER);
    }


    public static class TestTool extends Item {
        public TestTool() {
            super(new Properties());
        }

        public static class I1 extends TestTool {
            @Override
            public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
                if (!level.isClientSide()) {
                    opeHandlerGroupProvider(level.getServer()).deploy(OperatorTypeRegistry.TEST_OPERATOR.get(), (ServerPlayer) player, null);
                }
                return super.use(level, player, interactionHand);
            }
        }

        public static class I2 extends TestTool {

            @Override
            public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity livingEntity, InteractionHand interactionHand) {
                if (player instanceof ServerPlayer p && livingEntity instanceof OperatorEntity ope) {
                    opeHandlerGroupProvider(p.getServer()).findOperator(OperatorTypeRegistry.TEST_OPERATOR.get(), (ServerPlayer) player).get().retreat(false);
                }
                return super.interactLivingEntity(itemStack, player, livingEntity, interactionHand);
            }
        }

        public static class I3 extends TestTool {
            @Override
            public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
                if (!level.isClientSide()) {
                    opeHandlerGroupProvider(level.getServer()).findOperator(OperatorTypeRegistry.TEST_OPERATOR.get(), (ServerPlayer) player).get().deploy(true, false, -1, null, Operator.STATUS_WORKING);
                }
                return super.use(level, player, interactionHand);
            }
        }
    }

    public static class OpeProvider implements OpeHandler.GroupProvider {
        public static final OpeProvider INSTANCE = new OpeProvider();

        private OpeProvider() {
        }

        @Override
        public Optional<OpeHandler> withUUID(UUID playerUUID, MinecraftServer server) {
            return Optional.ofNullable(opeHandlerGroupProvider(server).getDataFor(playerUUID));
        }

        @Override
        public Optional<OpeHandler> withPlayer(ServerPlayer player) {
            return Optional.ofNullable(opeHandlerGroupProvider(player.getServer()).getDataFor(player));
        }
    }

    public static class OperatorTypeRegistry {
        public static final DeferredRegister<OperatorType> REGISTER = DeferredRegister.create(Registries.Keys.OPERATOR_TYPE, Nonatomic.MOD_ID);
        public static final DeferredHolder<OperatorType, TestOperator> TEST_OPERATOR = REGISTER.register("test", TestOperator::new);
    }

    public static class TestOperator extends OperatorType {
        @Override
        public Operator createDefaultInstance(OpeHandler handler) {
            return new Operator(OperatorTypeRegistry.TEST_OPERATOR.get());
        }

        @Override
        public @Nullable EntityType<? extends OperatorEntity> getEntityType() {
            return EntityTypeRegistry.TEST.get();
        }
    }

    public static class EntityTypeRegistry {
        public static final DeferredRegister<EntityType<?>> REGISTER = DeferredRegister.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, Nonatomic.MOD_ID);

        public static final DeferredHolder<EntityType<?>, EntityType<OperatorEntity>> TEST = REGISTER.register("test", () -> EntityType.Builder.<OperatorEntity>of(OperatorEntity::new, MobCategory.MISC).sized(0.6F, 1.9F).clientTrackingRange(32).canSpawnFarFromPlayer().build("test"));
    }

    public static class ItemRegistry {
        public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(net.minecraft.core.registries.Registries.ITEM, Nonatomic.MOD_ID);

        public static final DeferredHolder<Item, TestTool.I1> I1 = REGISTER.register("i1", TestTool.I1::new);
        public static final DeferredHolder<Item, TestTool.I2> I2 = REGISTER.register("i2", TestTool.I2::new);
    }

    public static class DataAttachmentRegistry {

        public static final DeferredRegister<AttachmentType<?>> REGISTER = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Nonatomic.MOD_ID);

        public static final DeferredHolder<AttachmentType<?>, AttachmentType<OpeHandlerNoRepetition.LevelContainer>> OPE_HANDLER =
                REGISTER.register("ope_handler", () -> AttachmentType.builder(() -> new OpeHandlerNoRepetition.LevelContainer(4)).serialize(OpeHandlerNoRepetition.LevelContainer.CODEC).build());
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
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Nonatomic.MOD_ID, "textures/entity/villager_knight.png");

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
