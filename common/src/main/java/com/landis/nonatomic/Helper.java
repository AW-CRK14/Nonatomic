package com.landis.nonatomic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Helper {
    public static  <F,S> Map<F,S> listElementAsValue(List<S> list, Function<S,F> keyProvider){
        HashMap<F,S> map = new HashMap<>();
        for (S s : list) {
            map.put(keyProvider.apply(s), s);
        }
        return map;
    }
}
