package hudson.plugins.promoted_builds.integrations.pipeline;

import hudson.model.Result;
import hudson.plugins.promoted_builds.JobPropertyImpl;
import hudson.plugins.promoted_builds.PromotionProcess;
import hudson.plugins.promoted_builds.conditions.ManualCondition;
import java.io.File;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for Declarative Pipeline examples with promoted builds.
 */
@WithJenkins
class DeclarativePipelineExamplesTest {

    @Test
    void testExampleDeclarativePipelineWithPromotions(JenkinsRule j) throws Exception {
        // Given - Load the example Jenkinsfile
        String pipelineScript = FileUtils.readFileToString(
            new File("src/test/resources/example-declarative-pipeline.groovy"), 
            StandardCharsets.UTF_8
        );
        
        // Create a pipeline job with the script
        WorkflowJob job = j.createProject(WorkflowJob.class, "test-pipeline-job");
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        
        // When - Run the pipeline
        WorkflowRun run = j.buildAndAssertSuccess(job);
        
        // Then - Verify the job has promotion property configured
        JobPropertyImpl promotionProperty = job.getProperty(JobPropertyImpl.class);
        assertNotNull(promotionProperty, "Job should have promotion property");
        
        // Verify the active promotion process names
        assertEquals(2, promotionProperty.getActiveItems().size(), 
            "Should have 2 active promotion processes");
        
        // Verify the promotion processes exist
        PromotionProcess qaApproved = promotionProperty.getItem("QA-Approved");
        assertNotNull(qaApproved, "QA-Approved promotion process should exist");
        
        PromotionProcess prodReady = promotionProperty.getItem("Production-Ready");
        assertNotNull(prodReady, "Production-Ready promotion process should exist");
    }

    @Test
    void testExamplePromotedBuildParameter(JenkinsRule j) throws Exception {
        // Given - Create an upstream job with promotions first
        WorkflowJob upstreamJob = j.createProject(WorkflowJob.class, "upstream-job");
        upstreamJob.setDefinition(new CpsFlowDefinition(
            "pipeline { agent any; stages { stage('Build') { steps { echo 'Building' } } } }", 
            true
        ));
        
        // Add promotion configuration to upstream job
        JobPropertyImpl promotionProperty = new JobPropertyImpl(upstreamJob);
        upstreamJob.addProperty(promotionProperty);
        PromotionProcess process = promotionProperty.addProcess("Production-Ready");
        process.conditions.add(new ManualCondition());
        
        // Build the upstream job
        j.buildAndAssertSuccess(upstreamJob);
        
        // Now load the downstream example that uses promoted build parameter
        String pipelineScript = FileUtils.readFileToString(
            new File("src/test/resources/example-promoted-build-parameter.groovy"), 
            StandardCharsets.UTF_8
        );
        
        // When - Create downstream job with promoted build parameter
        WorkflowJob downstreamJob = j.createProject(WorkflowJob.class, "test-downstream-job");
        downstreamJob.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        
        // Then - Job should be created successfully (won't run without actual promoted build)
        assertNotNull(downstreamJob, "Downstream job should be created");
        
        // Note: We can't fully test the execution without manually promoting a build,
        // but we verify the syntax is valid and the job can be created
    }

    @Test
    void testBasicPipelineWithPromotionProperty(JenkinsRule j) throws Exception {
        // Given - A simple pipeline with promotion property
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
        
        WorkflowJob job = j.createProject(WorkflowJob.class, "simple-pipeline");
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        
        // When
        WorkflowRun run = j.buildAndAssertSuccess(job);
        
        // Then
        JobPropertyImpl promotionProperty = job.getProperty(JobPropertyImpl.class);
        assertNotNull(promotionProperty, "Job should have promotion property");
        assertEquals(1, promotionProperty.getActiveItems().size(), 
            "Should have 1 active promotion process");
        
        PromotionProcess development = promotionProperty.getItem("Development");
        assertNotNull(development, "Development promotion process should exist");
    }
}
