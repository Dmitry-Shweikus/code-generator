package com.seavus.code.generator.utils.model;

import java.util.ArrayList;
import java.util.List;

public class MeasureTemplate {
    private String name;
    private String key;
    private Deprecation deprecation;
    private List<MeasureTemplate> children = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<MeasureTemplate> getChildren() {
        return children;
    }

    public void setChildren(List<MeasureTemplate> children) {
        this.children = children;
    }

    public Deprecation getDeprecation() {
        return deprecation;
    }

    public void setDeprecation(Deprecation deprecation) {
        this.deprecation = deprecation;
    }
}
