package com.seavus.code.generator.generator.model;

import com.seavus.code.generator.utils.model.MeasureTemplate;
import com.squareup.javapoet.TypeSpec;

public class GeneratedMeasureModel {
    private final TypeSpec.Builder builder;
    private final MeasureTemplate measureTemplate;
    private final String className;
    private final String packageName;

    public GeneratedMeasureModel(TypeSpec.Builder builder, MeasureTemplate measureTemplate, String className, String packageName) {
        this.builder = builder;
        this.measureTemplate = measureTemplate;
        this.className = className;
        this.packageName = packageName;
    }

    public String getFullClassName(){
        return packageName + "." + className;
    }

    public TypeSpec.Builder getBuilder() {
        return builder;
    }

    public MeasureTemplate getMeasureTemplate() {
        return measureTemplate;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }
}
