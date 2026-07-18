package hudson.plugins.promoted_builds.integrations.pipeline;

import java.io.File;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ScmExampleDirectoryTest {

    private static final File EXAMPLE_DIR = new File("src/test/resources/examples/promoted-semo-scm-pipeline");

    @Test
    void exampleDirectoryContainsTheFullSCMBackedDemo() throws Exception {
        assertTrue(new File(EXAMPLE_DIR, "Jenkinsfile").isFile());
        assertTrue(new File(EXAMPLE_DIR, "build.sh").isFile());
        assertTrue(new File(EXAMPLE_DIR, "README.md").isFile());
        assertTrue(new File(EXAMPLE_DIR, "jenkins/job-config.json").isFile());
        assertTrue(new File(EXAMPLE_DIR, "jenkins/promoted-semo-scm-pipeline/config.xml").isFile());
        assertTrue(new File(EXAMPLE_DIR, "jenkins/promoted-semo-scm-pipeline/promotions/np/config.xml").isFile());
        assertTrue(new File(EXAMPLE_DIR, "jenkins/promoted-semo-scm-pipeline/promotions/prod/config.xml").isFile());
        assertTrue(new File(EXAMPLE_DIR, "jenkins/promoted-semo-scm-pipeline/promotions/sbx/config.xml").isFile());

        String readme = FileUtils.readFileToString(new File(EXAMPLE_DIR, "README.md"), StandardCharsets.UTF_8);
        String jobConfig = FileUtils.readFileToString(new File(EXAMPLE_DIR, "jenkins/job-config.json"), StandardCharsets.UTF_8);
        String jobXml = FileUtils.readFileToString(new File(EXAMPLE_DIR, "jenkins/promoted-semo-scm-pipeline/config.xml"), StandardCharsets.UTF_8);

        assertTrue(readme.contains("https://github.com/ap0ught/promoted-builds-plugin"));
        assertTrue(readme.contains("deleted and recreated from the SCM contents"));
        assertTrue(jobConfig.contains("promoted-builds-plugin"));
        assertTrue(jobXml.contains("JobPropertyImpl"));
        assertTrue(jobXml.contains("bash src/test/resources/examples/promoted-semo-scm-pipeline/build.sh"));
    }
}
