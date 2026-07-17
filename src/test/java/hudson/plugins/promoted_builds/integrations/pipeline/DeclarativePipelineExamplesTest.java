package hudson.plugins.promoted_builds.integrations.pipeline;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests for Declarative Pipeline example files with promoted builds.
 */
class DeclarativePipelineExamplesTest {

    @Test
    void testExampleDeclarativePipelineWithPromotions() throws Exception {
        String pipelineScript = FileUtils.readFileToString(
            new File("src/test/resources/example-declarative-pipeline.groovy"),
            StandardCharsets.UTF_8
        );

        assertTrue(pipelineScript.contains("JobPropertyImpl"));
        assertTrue(pipelineScript.contains("QA-Approved"));
        assertTrue(pipelineScript.contains("Production-Ready"));
        assertTrue(pipelineScript.contains("properties(["));
    }

    @Test
    void testExamplePromotedBuildParameter() throws Exception {
        String pipelineScript = FileUtils.readFileToString(
            new File("src/test/resources/example-promoted-build-parameter.groovy"),
            StandardCharsets.UTF_8
        );

        assertTrue(pipelineScript.contains("promotedBuild("));
        assertTrue(pipelineScript.contains("project: 'upstream-job'"));
        assertTrue(pipelineScript.contains("promotion: 'Production-Ready'"));
    }

    @Test
    void testBasicPipelineWithPromotionProperty() {
        String pipelineScript = """
            pipeline {
                agent any
                properties([
                    [$class: 'JobPropertyImpl',
                        activeProcessNames: ['Development'] as Set
                    ]
                ])
                stages {
                    stage('Build') {
                        steps {
                            echo 'Building...'
                        }
                    }
                }
            }
            """;

        assertTrue(pipelineScript.contains("JobPropertyImpl"));
        assertTrue(pipelineScript.contains("activeProcessNames: ['Development'] as Set"));
        assertTrue(pipelineScript.contains("stage('Build')"));
    }
}
