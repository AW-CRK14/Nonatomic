package com.landis.nonatomic.core;

import com.landis.nonatomic.Registries;
import com.landis.nonatomic.datagroup.Deploy;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.system.NonnullDefault;

import java.util.*;

public class Operator {
    public static final Codec<Operator> CODEC = RecordCodecBuilder.create(n -> n.group(
            Identifier.CODEC.fieldOf("identifier").forGetter(i -> i.identifier),
            Deploy.CODEC.fieldOf("deploy_state").forGetter(i -> i.deploy),
            OperatorInfo.CODEC.listOf().fieldOf("infos").forGetter(i -> i.infos.values().stream().toList())
    ).apply(n, Operator::new));

    public final Identifier identifier;

    //    public final OperatorPattern ROOT;
    public final HashMap<Codec<? extends OperatorInfo>, OperatorInfo> infos = new HashMap<>();

    //如果你需要使用你自定义的Deploy 你可以在构造方法里覆盖该变量。记得做数据同步。
    public Deploy deploy;

    @NonnullDefault
    public Player player;


    public Operator(OperatorType operatorType) {
        this.identifier = new Identifier(operatorType);
        this.deploy = new Deploy();
    }

    public Operator(OperatorType operatorType, Deploy deploy, List<? extends OperatorInfo> infos) {
        this.identifier = new Identifier(operatorType);
        this.deploy = deploy;
        for (OperatorInfo info : infos) {
            this.infos.put(info.codec(), info);
        }
    }

    public Operator(UUID operatorUUID, OperatorType operatorType) {
        this.identifier = new Identifier(operatorType);
        this.deploy = new Deploy();
    }

    public Operator(UUID operatorUUID, OperatorType operatorType, Deploy deploy, List<? extends OperatorInfo> infos) {
        this.identifier = new Identifier(operatorType, operatorUUID);
        this.deploy = deploy;
        for (OperatorInfo info : infos) {
            this.infos.put(info.codec(), info);
        }
    }

    public Operator(Identifier identifier, Deploy deploy, List<? extends OperatorInfo> infos) {
        this.identifier = identifier;
        this.deploy = deploy;
        for (OperatorInfo info : infos) {
            this.infos.put(info.codec(), info);
        }
    }

    public void init(Player player) {
        this.player = player;
        deploy.init(this);
        for (OperatorInfo info : infos.values()) {
            info.init(this);
        }
    }


    public record Identifier(OperatorType type, Optional<UUID> uuid) {
        public static final Codec<Identifier> CODEC = RecordCodecBuilder.create(n -> n.group(
                Registries.getOperatorTypeRegistry().byNameCodec().fieldOf("type").forGetter(Identifier::type),
                UUIDUtil.CODEC.optionalFieldOf("uuid").forGetter(Identifier::uuid)
        ).apply(n, Identifier::new));

        public Identifier(OperatorType type) {
            this(type, Optional.empty());
        }

        public Identifier(OperatorType type, UUID uuid) {
            this(type, Optional.of(uuid));
        }
    }

}
