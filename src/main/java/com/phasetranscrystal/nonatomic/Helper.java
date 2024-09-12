package com.phasetranscrystal.nonatomic;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Helper {

    public static <K, V> Codec<Map<K, V>> mapLikeWithKeyProvider(Codec<V> valueCodec, Function<V, K> keyProvider) {
        return mapLikeCodec(valueCodec.xmap(v -> Pair.of(keyProvider.apply(v), v), Pair::getSecond));
    }

    public static <K, V> Codec<Map<K, V>> mapLikeCodec(Codec<K> keyCodec, Codec<V> valueCodec) {
        return mapLikeCodec(Codec.pair(keyCodec, valueCodec));
    }

    public static <K, V> Codec<Map<K, V>> mapLikeCodec(Codec<Pair<K,V>> pairCodec) {
        return pairCodec.listOf().xmap(Helper::pairsToMap, Helper::mapToPairs);
    }

    public static <K, V> Map<K, V> pairsToMap(List<Pair<K, V>> pairs) {
        Map<K, V> map = new HashMap<>();
        for (Pair<K, V> p : pairs) {
            map.put(p.getFirst(), p.getSecond());
        }
        return map;
    }

    public static <K, V> List<Pair<K, V>> mapToPairs(Map<K, V> map) {
        List<Pair<K, V>> pairs = new ArrayList<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            pairs.add(Pair.of(entry.getKey(), entry.getValue()));
        }
        return pairs;
    }
}
