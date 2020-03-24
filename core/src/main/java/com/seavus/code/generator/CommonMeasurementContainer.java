package com.seavus.code.generator;

import com.seavus.code.generator.exceptions.KeyNotFoundException;
import com.seavus.code.generator.model.Measurement;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class CommonMeasurementContainer<T extends Measurement> {
    public final T root;
    private Map<Class, Measurement> classesCache = new HashMap<>();

    public CommonMeasurementContainer(T root) {
        this.root = root;
    }

    public T getRoot() {
        return root;
    }

    public <R extends Measurement> R get(Class<R> clazz) {
        return clazz.cast(classesCache.computeIfAbsent(clazz, aClass -> findInTree(root, clazz::isInstance)));
    }

    public Optional<Measurement> findByInternalKey(String key) {
        return Optional.ofNullable(findInTree(root, concept -> key.equals(concept.getKey())));
    }

    public Measurement findByInternalKeyOrThrow(String value) {
        return findByInternalKey(value).orElseThrow(() -> new KeyNotFoundException("Concept with internal code not found : " + value));
    }

    private Measurement findInTree(Measurement localRoot, Predicate<Measurement> testFunction) {
        if (testFunction.test(localRoot)) {
            return localRoot;
        }
        for (Measurement childMeasurement : localRoot.getChildren()) {
            Measurement result = findInTree(childMeasurement, testFunction);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

}
