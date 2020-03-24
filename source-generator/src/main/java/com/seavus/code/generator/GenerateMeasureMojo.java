package com.seavus.code.generator;

import com.seavus.code.generator.generator.MeasuresGenerator;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;


@Mojo(name = "generate-measures")
public class GenerateMeasureMojo
        extends AbstractMojo {
    @Parameter(required = true, defaultValue = "${project.build.directory}")
    private File outputDirectory;

    @Parameter(defaultValue = "${project}")
    private MavenProject project;

    @Parameter( property = "directory", required = true)
    private String directory;

    @Parameter( property = "basePackage", required = true)
    private String basePackage;

    public void execute() throws MojoExecutionException {
        File outputSourceDirectory = outputDirectory.toPath().resolve("generated-sources").resolve("measures").toFile();
        outputSourceDirectory.mkdirs();
        project.addCompileSourceRoot(outputSourceDirectory.getPath());

        for (Resource resource : project.getResources()) {
            File jsonFilesDirectory = new File(resource.getDirectory()).toPath().resolve(directory).toFile();
            if (jsonFilesDirectory.exists()){
                try {
                    MeasuresGenerator generator = new MeasuresGenerator(outputSourceDirectory, jsonFilesDirectory, basePackage, getLog());
                    generator.generate();
                } catch (IOException e) {
                    throw new MojoExecutionException("Error generate measures sources", e);
                }
            }
        }


    }
}
