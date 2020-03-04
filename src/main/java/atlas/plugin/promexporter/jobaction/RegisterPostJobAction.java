package atlas.plugin.promexporter.jobaction;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import atlas.plugin.promexporter.metric.JobCollector;
import atlas.plugin.promexporter.metric.MetricCollector;
import atlas.plugin.promexporter.service.BuildProcessorServerExport;

import com.atlassian.bamboo.build.Job;
import com.atlassian.bamboo.chains.StageExecution;
import com.atlassian.bamboo.chains.plugins.PostJobAction;
import com.atlassian.bamboo.resultsummary.BuildResultsSummary;
import com.atlassian.bamboo.resultsummary.tests.TestResultsSummary;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.v2.build.BuildContext;

public class RegisterPostJobAction implements PostJobAction {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(RegisterPostJobAction.class);

    private JobCollector jobCollector;

    public RegisterPostJobAction(JobCollector jobCollector) {
        this.jobCollector = jobCollector;
    }

    @Override
    public void execute(StageExecution stageExecution, Job job,
            BuildResultsSummary buildResultsSummary) {

        if (!jobCollector.isStarted()) {
            return;
        }

        BuildContext buildContext = stageExecution
                .getBuilds()
                .stream()
                .filter(buildExecution -> buildExecution.getBuildContext()
                        .getPlanName().equals(job.getName())).findFirst().get()
                .getBuildContext();

        TaskDefinition taskDefinition = buildContext
                .getRuntimeTaskDefinitions()
                .stream()
                .filter(taskDef -> taskDef.getPluginKey().equals(
                        BuildProcessorServerExport.PROM_TASK_KEY)).findFirst()
                .orElse(null);

        if (taskDefinition == null) {
            return;
        }

        String jobBranch = buildResultsSummary.getCustomBuildData().get(
                "planRepository.1.branchName");

        if (jobCollector.getRequiredBranchName().equals(jobBranch)) {
            TestResultsSummary testResultsSummary = buildResultsSummary
                    .getTestResultsSummary();

            atlas.plugin.promexporter.metric.Job jobWithStat = jobCollector
                    .getJobs().get(job.getName());

            // if (testResultsSummary.hasFailedTestResults()
            // || testResultsSummary.hasSuccessfulTestResults()) {

            Long endTime = TimeUnit.MILLISECONDS.toSeconds(System
                    .currentTimeMillis());

            jobWithStat.setEndTime(endTime);

            Long prDuration = TimeUnit.MILLISECONDS
                    .toSeconds(buildResultsSummary.getProcessingDuration());

            jobWithStat.setProcessingDuration(prDuration);

            Long qDuration = TimeUnit.MILLISECONDS
                    .toSeconds(buildResultsSummary.getQueueDuration());

            jobWithStat.setQueueDuration(qDuration);

            if (testResultsSummary.hasFailedTestResults()
                    || testResultsSummary.hasSuccessfulTestResults()) {
                jobWithStat.setTestNumber(testResultsSummary
                        .getFailedTestCaseCount()
                        + testResultsSummary.getSuccessfulTestCaseCount()
                        + testResultsSummary.getIgnoredTestCaseCount());
            }
            
            jobWithStat.setFinished(true);

            LOGGER.info("Job finished : " + jobWithStat.toString());

            // } else {
            // jobCollector.finish();
            // LOGGER.info("Breaken job: " + jobWithStat.toString());
            // }
        }

        if (jobCollector.isFinished()) {
            collectJobStatistics(buildResultsSummary);
            jobCollector.getJobs().clear();
            jobCollector.finish();
            LOGGER.info("Record finished successful");
        }
    }

    private void collectJobStatistics(BuildResultsSummary buildResultsSummary) {
        jobCollector
                .getJobs()
                .entrySet()
                .forEach(
                        job -> {

                            MetricCollector.JOB_PROCESSING_DURATION.labels(
                                    job.getValue().getProjectName(),
                                    job.getValue().getPlanName(),
                                    job.getValue().getJobName()).set(
                                    job.getValue().getProcessingDuration());
                            MetricCollector.JOB_QUEUE_DURATION.labels(
                                    job.getValue().getProjectName(),
                                    job.getValue().getPlanName(),
                                    job.getValue().getJobName()).set(
                                    job.getValue().getQueueDuration());
                            MetricCollector.JOB_START_TIME.labels(
                                    job.getValue().getProjectName(),
                                    job.getValue().getPlanName(),
                                    job.getValue().getJobName()).set(
                                    job.getValue().getStartTime());
                            MetricCollector.JOB_END_TIME.labels(
                                    job.getValue().getProjectName(),
                                    job.getValue().getPlanName(),
                                    job.getValue().getJobName()).set(
                                    job.getValue().getEndTime());

                            MetricCollector.TEST_NUMBER.labels(
                                    job.getValue().getProjectName(),
                                    job.getValue().getPlanName(),
                                    job.getValue().getJobName()).set(
                                    job.getValue().getTestNumber());

                        });
    }

}
