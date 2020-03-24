package com.seavus.code.generator.servicesample.controllers;

import com.seavus.code.generator.impl.MeasuresContainer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @GetMapping("/container")
    public MeasuresContainer getConceptContainer(){
        return new MeasuresContainer();
    }
}
