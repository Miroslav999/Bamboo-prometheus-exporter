package atlas.plugin.promexporter.jobaction;

import atlas.plugin.promexporter.metric.MetricCollector;

import com.atlassian.bamboo.chains.StageExecution;
import com.atlassian.bamboo.chains.plugins.PreJobAction;
import com.atlassian.bamboo.v2.build.BuildContext;

public class RegisterPreJobAction implements PreJobAction {

    @Override
    public void execute(StageExecution arg0, BuildContext arg1) {
        System.out.println();
        Long ml = System.currentTimeMillis();
        int startTime = ml.intValue() / 1000;
        String projectName = arg1.getProjectName();
        String planName = arg1.getPlanName().split("-")[1];
        String jobName = arg1.getShortName();
        String number = String.valueOf(arg1.getBuildNumber());
        MetricCollector.JOB_START_TIME.labels(projectName, planName, jobName
                ).set(startTime);
    }

}
