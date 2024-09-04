package com.landis.nonatomic.core.info;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;

public interface IAttributesProvider extends IBelongingOperatorProvider {
    Logger LOGGER = LogManager.getLogger("BreaNonatomic:AttributesProvider");

    Collection<MarkedModifier> getAttributes();

    default void remove(MarkedModifier modifier){
        getBelonging().modifyAttribute(true,modifier);
    }

    default void attach(MarkedModifier modifier){
        getBelonging().modifyAttribute(false,modifier);
    }

    record MarkedModifier(Attribute attribute, AttributeModifier modifier, boolean isPermanent) {
        public static final Codec<MarkedModifier> CODEC = RecordCodecBuilder.create(n -> n.group(
                BuiltInRegistries.ATTRIBUTE.byNameCodec().fieldOf("attribute").forGetter(MarkedModifier::attribute),
                AttributeModifier.CODEC.fieldOf("modifier").forGetter(MarkedModifier::modifier),
                Codec.BOOL.fieldOf("isPermanent").forGetter(MarkedModifier::isPermanent)
        ).apply(n,MarkedModifier::new));

        public static final MapCodec<HashSet<MarkedModifier>> SET_CODEC = MarkedModifier.CODEC.listOf().xmap(HashSet::new, set -> set.stream().toList()).fieldOf("attributes");

        public MarkedModifier(Attribute attribute, AttributeModifier modifier) {
            this(attribute, modifier, false);
        }

        public void attach(LivingEntity entity) {
            AttributeInstance instance = entity.getAttribute(attribute);
            if (instance == null) {
                LOGGER.debug("Not allowed attribute ({}) for entity(type = {}). Ignoring.", attribute.getDescriptionId(), entity.getType().getDescriptionId());
                return;
            }

            try {
                if (isPermanent) instance.addPermanentModifier(modifier);
                else instance.addTransientModifier(modifier);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Attribute modifier ({}) has been attached to entity (type = {}, uuid = {}).", attribute.getDescriptionId(), entity.getType(), entity.getUUID());
                LOGGER.warn(e);
            }
        }

        public void remove(LivingEntity entity) {
            AttributeInstance instance = entity.getAttribute(attribute);
            if (instance == null) {
                LOGGER.debug("Not allowed attribute ({}) for entity(type = {}). Ignoring.", attribute.getDescriptionId(), entity.getType().getDescriptionId());
                return;
            }

            if (isPermanent) instance.removePermanentModifier(modifier.getId());
            instance.removeModifier(modifier.getId());
        }
    }
}
