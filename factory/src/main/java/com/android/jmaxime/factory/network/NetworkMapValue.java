package com.android.jmaxime.factory.network;

import java.util.HashMap;
import java.util.Map;

public class NetworkMapValue {
    private Map<String, String> mMap = new HashMap<>();

    public NetworkMapValue add(String keyName, String keyValue) {
        if (isValid(keyName) && isValid(keyValue)) {
            mMap.put(keyName, keyValue);
        }
        return this;
    }

    Map<String, String> getMap() {
        return mMap;
    }

    private boolean isValid(String chainTested) {
        return chainTested != null && !chainTested.isEmpty();
    }
}
