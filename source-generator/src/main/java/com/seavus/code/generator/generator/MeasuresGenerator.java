package com.seavus.code.generator.generator;

import com.seavus.code.generator.CommonMeasurementContainer;
import com.seavus.code.generator.generator.exceptions.MeasureGeneratorException;
import com.seavus.code.generator.generator.model.GeneratedMeasureModel;
import com.seavus.code.generator.model.Measurement;
import com.seavus.code.generator.utils.MeasuresTemplateLoader;
import com.seavus.code.generator.utils.model.MeasureTemplate;
import com.seavus.code.generator.utils.model.Deprecation;
import com.squareup.javapoet.*;
import org.apache.maven.plugin.logging.Log;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MeasuresGenerator {
    private GeneratorNameUtils generatorNameUtils = new GeneratorNameUtils();

    private final File sourceRootFolder;
    private final File jsonConfigsFolder;
    private final String basePackage;
    private final Log log;
    private int measuresGeneratedCount;
    private final Map<String, GeneratedMeasureModel> generatedClasses = new HashMap<>();

    public MeasuresGenerator(File sourceRootFolder, File jsonConfigsFolder, String basePackage, Log log) {
        this.sourceRootFolder = sourceRootFolder;
        this.jsonConfigsFolder = jsonConfigsFolder;
        this.basePackage = basePackage;
        this.log = log;
    }


    public void generate() throws IOException {
        GeneratedMeasureModel root = generateRoot();
        addDeprecationInfo();
        writeMeasureClasses();
        log.info("Total measures generated : " + measuresGeneratedCount);
        generateContainer(root);
    }

    private void addDeprecationInfo() {
        for (GeneratedMeasureModel generatedMeasureModel : generatedClasses.values()) {
            Deprecation deprecation = generatedMeasureModel.getMeasureTemplate().getDeprecation();
            if (deprecation != null) {
                TypeSpec.Builder builder = generatedMeasureModel.getBuilder();
                builder.addAnnotation(Deprecated.class);
                builder.addJavadoc("@deprecated");
                List<String> links = deprecation.getLinks();
                if (links != null && !links.isEmpty()) {
                    builder.addJavadoc("\nuse ");
                    for (String link : links) {
                        GeneratedMeasureModel linkToModel = generatedClasses.get(link);
                        if (linkToModel == null) {
                            String errorTemplate = "Error generate deprecation javadoc for class %s (%s). Key \"%s\" for deprecation link not found";
                            String errorMsg = String.format(errorTemplate, generatedMeasureModel.getFullClassName(),
                                    generatedMeasureModel.getMeasureTemplate().getKey(),
                                    link);
                            throw new MeasureGeneratorException(errorMsg);
                        }
                        builder.addJavadoc("{@link $N} ", linkToModel.getFullClassName());
                    }
                    builder.addJavadoc("\n");
                }
                if (deprecation.getComment() != null && deprecation.getComment().trim().length() > 0) {
                    builder.addJavadoc(deprecation.getComment());
                }
                log.info("Measures " + generatedMeasureModel.getFullClassName() + " is marked as deprecated");
            }
        }
    }

    private void writeMeasureClasses() throws IOException {
        for (GeneratedMeasureModel generatedMeasureModel : generatedClasses.values()) {
            TypeSpec conceptClass = generatedMeasureModel.getBuilder().build();
            JavaFile javaFile = JavaFile.builder(generatedMeasureModel.getPackageName(), conceptClass).build();
            javaFile.writeTo(sourceRootFolder);
        }
    }

    private List<MeasureTemplate> getTemplates() throws IOException {
        List<MeasureTemplate> list = new ArrayList<>();
        //Note for developer :
        //for some unknown reasons maven build fails when we use lambdas
        //So we use inner classes instead lambdas
        List<Path> allPatches = Files.walk(jsonConfigsFolder.toPath())
                .filter(new Predicate<Path>() {
                    @Override
                    public boolean test(Path path) {
                        File file = path.toFile();
                        return file.isFile() && file.getName().toLowerCase().endsWith(".json");
                    }
                })
                .collect(Collectors.toList());
        for (Path path : allPatches) {
            File file = path.toFile();
            log.info("File " + file + " added to processing");
            MeasuresTemplateLoader loader = new MeasuresTemplateLoader(file);
            list.add(loader.load());
        }
        return list;
    }

    private GeneratedMeasureModel generateClasses(MeasureTemplate template, String packageName) throws IOException {
        String className = generatorNameUtils.toClassName(template.getName());
        ClassName superConceptClass = ClassName.get(Measurement.class);

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("super($S)", template.getKey())
                .build();

        TypeSpec.Builder builder = TypeSpec.classBuilder(className)
                .superclass(superConceptClass)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(constructor);

        MethodSpec.Builder getChildren = MethodSpec.methodBuilder("getChildren")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(List.class, Measurement.class))
                .addStatement("$T list = new $T<>()", ParameterizedTypeName.get(List.class, Measurement.class), ArrayList.class);

        boolean hasDeprecatedChildren = false;
        for (MeasureTemplate childTemplate : template.getChildren()) {
            String childPackageName = packageName;
            if (!childTemplate.getChildren().isEmpty()) {
                childPackageName += "." + generatorNameUtils.toPackage(childTemplate.getName());
            }
            generateClasses(childTemplate, childPackageName);
            ClassName childClassName = ClassName.get(childPackageName, generatorNameUtils.toClassName(childTemplate.getName()));
            FieldSpec.Builder fieldBuilder = FieldSpec.builder(childClassName, generatorNameUtils.toFieldName(childTemplate.getName()))
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .initializer("new $T()", childClassName);
            if (childTemplate.getDeprecation() != null){
                fieldBuilder.addAnnotation(getSuppressWarnings());
            }
            FieldSpec childField = fieldBuilder.build();
            builder.addField(fieldBuilder.build());

            getChildren.addStatement("list.add($N)", childField.name);
            hasDeprecatedChildren |= childTemplate.getDeprecation() != null;
        }

        if (!template.getChildren().isEmpty()) {
            getChildren.addStatement("return list");
            if (hasDeprecatedChildren) {
                getChildren.addAnnotation(getSuppressWarnings());
            }
            builder.addMethod(getChildren.build());
        }

        measuresGeneratedCount++;
        GeneratedMeasureModel generatedMeasureModel = new GeneratedMeasureModel(builder, template, className, packageName);
        checkKeyNotExist(generatedMeasureModel);
        generatedClasses.put(template.getKey(), generatedMeasureModel);
        return generatedMeasureModel;
    }

    private AnnotationSpec getSuppressWarnings() {
        return AnnotationSpec
                .builder(SuppressWarnings.class)
                .addMember("value", "$S","deprecation")
                .build();
    }

    private void checkKeyNotExist(GeneratedMeasureModel generatedMeasureModel) {
        GeneratedMeasureModel oldKey = generatedClasses.get(generatedMeasureModel.getMeasureTemplate().getKey());
        if (oldKey != null) {
            String format = "Error generate class %s (key = %s). Key already in use for class %s";
            String msg = String.format(format, generatedMeasureModel.getFullClassName(),
                    generatedMeasureModel.getMeasureTemplate().getKey(),
                    oldKey.getFullClassName());
            throw new MeasureGeneratorException(msg);
        }
    }

    private void generateContainer(GeneratedMeasureModel root) throws IOException {

        ClassName rootClassName = ClassName.get(root.getPackageName(), root.getClassName());
        ParameterizedTypeName superClass = ParameterizedTypeName.get(ClassName.get(CommonMeasurementContainer.class), rootClassName);
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("super(new $T())", rootClassName)
                .build();

        TypeSpec.Builder builder = TypeSpec.classBuilder("MeasuresContainer")
                .superclass(superClass)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(constructor);

        TypeSpec measuresContainerClass = builder.build();
        JavaFile javaFile = JavaFile.builder(basePackage, measuresContainerClass).build();
        javaFile.writeTo(sourceRootFolder);
    }

    private GeneratedMeasureModel generateRoot() throws IOException {
        MeasureTemplate rootTemplate = new MeasureTemplate();
        rootTemplate.setName("Root");
        rootTemplate.setKey("root");
        rootTemplate.getChildren().addAll(getTemplates());
        new TemplateMerger(rootTemplate).merge();
        return generateClasses(rootTemplate, basePackage);
    }
}
