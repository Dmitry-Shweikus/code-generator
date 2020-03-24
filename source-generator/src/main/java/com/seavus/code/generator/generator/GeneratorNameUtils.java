package com.seavus.code.generator.generator;

public class GeneratorNameUtils {
    public String toPackage(String name) {
        return name.toLowerCase();
    }

    public String toClassName(String name) {
        String firstLetter = name.substring(0, 1);
        return firstLetter.toUpperCase() + name.substring(1);
    }

    public String toFieldName(String name) {
        String firstLetter = name.substring(0, 1);
        return firstLetter.toLowerCase() + name.substring(1);
    }
}
