package atlas.plugin.promexporter.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import atlas.plugin.promexporter.bambootask.PrometheusTask;
import atlas.plugin.promexporter.metric.MetricCollector;
import atlas.plugin.promexporter.metric.Parameter;

import com.atlassian.bamboo.build.CustomBuildProcessorServer;
import com.atlassian.bamboo.results.tests.TestResults;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.v2.build.BuildContext;

public class BuildProcessorServerExport implements CustomBuildProcessorServer {

    public static final String MASTER_BRANCH = "master";
    public static final String DELIMETER = ",";
    public static final String PATTERN = "\\(.*?\\)";
    public static final String TASK_KEY = "prom.atlas.plugins.bamboo-prom-exporter:test";

    private BuildContext buildContext;
    private MetricCollector metricCollector;

    public BuildProcessorServerExport(MetricCollector metricCollector) {
        this.metricCollector = metricCollector;
    }

    private static final Logger LOGGER = LoggerFactory
            .getLogger(BuildProcessorServerExport.class);

    @Override
    public BuildContext call() throws InterruptedException, Exception {
        LOGGER.info("Call method call");
        
        Long ml = System.currentTimeMillis();
        int endTime = ml.intValue() / 1000;
        String projectName = buildContext.getProjectName();
        String planName = buildContext.getPlanName().split("-")[1];
        String jobName = buildContext.getShortName();
        String number = String.valueOf(buildContext.getBuildNumber());
        MetricCollector.JOB_END_TIME.labels(projectName, planName, jobName
                ).set(endTime);
        
        TaskResult task = getTaskExporter(buildContext);

        if (task == null) {
            LOGGER.info("Task is null");
            return null;
        }

        Map<String, String> configs = getConfigMap(buildContext, task);

        if (configs == null) {
            LOGGER.info("Config is null");
            return null;
        }

        String currentPlanName = buildContext.getPlanName().split("-")[1]
                .trim();

        String requiredPlanName = configs.get(PrometheusTask.PLAN_NAME);

        if (!requiredPlanName.equals(currentPlanName)) {
            return null;
        }

        Set<String> branches = new HashSet<>(Arrays.asList(configs.get(
                PrometheusTask.BRANCHES_KEY).split(DELIMETER)));

        String currentBranch = Optional
                .ofNullable(buildContext.getBuildResult())
                .map(d -> d.getCustomBuildData())
                .map(e -> e.get("planRepository.branch"))
                .orElseThrow(
                        () -> new RuntimeException("Current branch not found"));

        String testType = configs.get(PrometheusTask.KEY_TEST_TYPE);

        int border = 0;

        try {
            border = Integer.valueOf(configs.get(PrometheusTask.BORDER));
        } catch (Exception e) {
            LOGGER.info("Integer.valueOf was failed");
        }

        if (MASTER_BRANCH.equals(currentBranch)
                || branches.contains(currentBranch)) {

            collectStatisticsFromSuccessfulTests(buildContext.getBuildResult()
                    .getSuccessfulTestResults(), currentBranch, testType,
                    requiredPlanName, border);

            collectStatisticsFromFailedTest(buildContext.getBuildResult().getFailedTestResults(), currentBranch,
                    testType, requiredPlanName);

        }

        return buildContext;
    }

    @Override
    public void init(BuildContext arg0) {
        this.buildContext = arg0;
    }

    @Nullable
    private Map<String, String> getConfigMap(BuildContext context,
            TaskResult taskResult) {
        return context
                .getBuildDefinition()
                .getTaskDefinitions()
                .stream()
                .filter(task -> taskResult.getTaskIdentifier().getPluginKey()
                        .equals(task.getPluginKey())).findFirst()
                .map(res -> res.getConfiguration()).orElse(null);
    }

    @Nullable
    private TaskResult getTaskExporter(BuildContext buildContext) {
        return buildContext
                .getBuildResult()
                .getTaskResults()
                .stream()
                .filter(taskResult -> taskResult.getTaskIdentifier()
                        .getPluginKey().equals(TASK_KEY)).findFirst()
                .orElse(null);
    }

    private String getClassName(String str) {
        return getStringByTemplate(PATTERN, str);
    }

    private String getTestName(String str) {
        return str.replaceAll(PATTERN, "");
    }

    @Nullable
    private String getStringByTemplate(String pattern, String str) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(str);
        if (m.find()) {
            return m.group().subSequence(1, m.group().length() - 1).toString();
        } else {
            return null;
        }
    }

    private void collectStatisticsFromSuccessfulTests(Collection<TestResults> testResults,
            String currentBranch, String testType, String planName, int border) {

        for (TestResults testResult : testResults){
            
            String className = Optional.ofNullable(
                    getClassName(testResult.getActualMethodName())).orElse(
                    "UnknownClassName");

            String testName = Optional.ofNullable(
                    getTestName(testResult.getActualMethodName())).orElse(
                    "UnknownTestName");

//            String className = testResult.getClassName();
//
//            String testName = testResult.getActualMethodName();
            
            Long duration = testResult.getDurationMs() / 1000;

            Map<Parameter, String> marks = genereteMapWithParameters(planName,
                    currentBranch, buildContext.getShortName(), className,
                    testName, testType);

            metricCollector.removeByLabels(MetricCollector.DURATION_TESTS, marks);
            
            //XXX FAILED_TESTS
            metricCollector.removeByLabels(MetricCollector.FAILED_TESTS, marks);


            if (duration < border) {
                continue;
            }

            logTestResult(className, testName, testType,
                    buildContext.getBuildNumber(), buildContext.getShortName(),
                    planName, currentBranch, duration);

            metricCollector.setLabels(marks, duration);
        }
        
    }

    private void logTestResult(String className, String testName,
            String testType, int buildNumber, String jobName, String planName,
            String currentBranch, long duration) {

        String metric = Stream
                .of(className, testName, testType, buildNumber, jobName,
                        planName, currentBranch, String.valueOf(duration))
                .map(Object::toString).collect(Collectors.joining("-"));

        LOGGER.info("Test = " + metric);
    }

    private Map<Parameter, String> genereteMapWithParameters(String planName,
            String currentBranch, String job, String className,
            String testName, String testType) {

        Map<Parameter, String> marks = new HashMap<>();
        marks.put(Parameter.PLAN, planName);
        marks.put(Parameter.JOB, job);
        marks.put(Parameter.BRANCH, currentBranch);
        marks.put(Parameter.CLASS_NAME, className);
        marks.put(Parameter.TEST_NAME, testName);
        marks.put(Parameter.TEST_TYPE, testType);
        return marks;
    }

  //XXX FAILED_TESTS
    private void collectStatisticsFromFailedTest(Collection<TestResults> testResults,
            String currentBranch, String testType, String planName) {

        testResults.forEach(failedTest -> {

            String className = Optional.ofNullable(
                    getClassName(failedTest.getActualMethodName())).orElse(
                    "UnknownClassName");

            String testName = Optional.ofNullable(
                    getTestName(failedTest.getActualMethodName())).orElse(
                    "UnknownTestName");
//                String className = failedTest.getClassName();
//
//                String testName = failedTest.getActualMethodName();
                
                String stack = failedTest.getErrors().get(0).getContent();
                
                LOGGER.info("Error Message = " + stack);

                String errorType = stack.substring(0, stack.indexOf(':'));

                MetricCollector.FAILED_TESTS.labels(className, testName,
                        testType, currentBranch, buildContext.getShortName(),
                        planName, errorType).inc();
            });
    }
}
