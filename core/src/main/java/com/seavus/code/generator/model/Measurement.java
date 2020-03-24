package com.seavus.code.generator.model;

import java.util.ArrayList;
import java.util.List;

public abstract class Measurement {
    private final String key;

    public Measurement(String key) {
        this.key = key;
    }

    public List<Measurement> getChildren() {
        return new ArrayList<>();
    }

    public String getKey() {
        return key;
    }

}
