package com.seavus.code.generator.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seavus.code.generator.utils.model.MeasureTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MeasuresTemplateLoader {
    private final File file;

    public MeasuresTemplateLoader(File file) {
        this.file = file;
    }

    public MeasureTemplate load() throws IOException {
        InputStream stream = new FileInputStream(file);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(stream, MeasureTemplate.class);
    }
}
