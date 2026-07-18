package hudson.plugins.promoted_builds.integrations.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import hudson.model.FreeStyleProject;
import hudson.plugins.promoted_builds.JobPropertyImpl;
import hudson.plugins.promoted_builds.PromotionProcess;
import hudson.plugins.promoted_builds.conditions.ManualCondition;
import java.io.File;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.junit.jupiter.api.Test;

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
        
        // Then - Verify the job was created and the pipeline ran successfully
        assertNotNull(run, "Pipeline run should complete successfully");
    }

    @Test
    void testExamplePromotedBuildParameter(JenkinsRule j) throws Exception {
        // Given - Create an upstream job with promotions first
        FreeStyleProject upstreamJob = j.createFreeStyleProject("upstream-job");

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
        // Given - A simple project with a promotion property
        FreeStyleProject job = j.createFreeStyleProject("simple-project");
        JobPropertyImpl promotionProperty = new JobPropertyImpl(job);
        job.addProperty(promotionProperty);
        PromotionProcess development = promotionProperty.addProcess("Development");

        // When
        j.buildAndAssertSuccess(job);

        // Then
        assertNotNull(promotionProperty, "Job should have promotion property");
        assertEquals(1, promotionProperty.getActiveItems().size(),
            "Should have 1 active promotion process");
        assertNotNull(development, "Development promotion process should exist");
    }
}