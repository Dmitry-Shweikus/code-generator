package com.seavus.code.generator.generator;

import com.seavus.code.generator.utils.model.MeasureTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateMerger {
    private final MeasureTemplate template;

    public TemplateMerger(MeasureTemplate template) {
        this.template = template;
    }

    public void merge() {
        Map<String, List<MeasureTemplate>> childTemplates = new HashMap<>();
        //create map by name
        for (MeasureTemplate child : template.getChildren()) {
            List<MeasureTemplate> measureTemplates = childTemplates.get(child.getName());
            if (measureTemplates == null) {
                measureTemplates = new ArrayList<>();
            }
            measureTemplates.add(child);
            childTemplates.put(child.getName(), measureTemplates);
        }
        //merge
        if (childTemplates.size() < template.getChildren().size()) {
            template.getChildren().clear();
            for (List<MeasureTemplate> measureTemplates : childTemplates.values()) {
                MeasureTemplate first = measureTemplates.remove(0);
                if (!measureTemplates.isEmpty()) {
                    //there is something to merge
                    for (MeasureTemplate other : measureTemplates) {
                        first.getChildren().addAll(other.getChildren());
                    }
                    //merge children recursively
                    new TemplateMerger(first).merge();
                }
                template.getChildren().add(first);
            }
        }
    }
}
