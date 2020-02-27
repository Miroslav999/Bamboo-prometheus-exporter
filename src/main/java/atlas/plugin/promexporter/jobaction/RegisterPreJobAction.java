package atlas.plugin.promexporter.jobaction;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import atlas.plugin.promexporter.metric.Job;
import atlas.plugin.promexporter.metric.JobCollector;
import atlas.plugin.promexporter.service.BuildProcessorServerExport;

import com.atlassian.bamboo.chains.StageExecution;
import com.atlassian.bamboo.chains.plugins.PreJobAction;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.v2.build.BuildContext;

public class RegisterPreJobAction implements PreJobAction {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(RegisterPreJobAction.class);

    private JobCollector jobCollector;

    public RegisterPreJobAction(JobCollector jobCollector) {
        this.jobCollector = jobCollector;
    }

    @Override
    public void execute(StageExecution stageExecution, BuildContext buildContext) {

        TaskDefinition taskDefinition = stageExecution
                .getBuilds()
                .get(0)
                .getBuildContext()
                .getRuntimeTaskDefinitions()
                .stream()
                .filter(taskDef -> taskDef.getPluginKey().equals(
                        BuildProcessorServerExport.PROM_TASK_KEY)).findFirst()
                .orElse(null);

        if (taskDefinition == null) {
            return;
        }
        String branch = stageExecution.getBuilds().get(0).getBuildContext()
                .getParentBuildContext().getShortName();

        if (taskDefinition.getConfiguration().get("planName").equals(branch)){
            branch = "master";
        }
        
        if (taskDefinition != null && jobCollector.isStarted()
                && jobCollector.getRequiredBranchName().equals(branch) ) {
            
            Long startTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
            
            Job job = new Job();

            job.setJobName(buildContext.getShortName());

            job.setPlanName(buildContext.getPlanName().split("-")[1]);

            job.setProjectName(buildContext.getProjectName());

            job.setStartTime(startTime);

            jobCollector.getJobs().put(buildContext.getPlanName(), job);

            LOGGER.info("RegisterPreJobAction: " + jobCollector.toString());
        }

    }
}
