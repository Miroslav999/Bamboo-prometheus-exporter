package atlas.plugin.promexporter.metric;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.SimpleCollector;

@Component
public class MetricCollector {

    public static final String METRIC_NAME = "duration_test";
    public static final String METRIC_DESCRIPTION = "Duration test";
    
    public static final String FAILED_TESTS_METRIC_NAME = "fallen_test";
    public static final String FAILED_TESTS_METRIC_DESCRIPTION = "Fallen test";

    private static final List<String> metricsList = Arrays.asList(
            Parameter.CLASS_NAME.getLabelName(),
            Parameter.TEST_NAME.getLabelName(),
            Parameter.TEST_TYPE.getLabelName(),
            Parameter.BRANCH.getLabelName(),
            Parameter.JOB.getLabelName(),
            Parameter.PLAN.getLabelName());

    public static final Gauge DURATION_TESTS = Gauge.build().name(METRIC_NAME)
            .help(METRIC_DESCRIPTION)
            .labelNames(metricsList.toArray(new String[metricsList.size()]))
            .register();
    
    public static final Gauge JOB_QUEUE_DURATION = Gauge.build().name("job_queue_duration")
            .help("Job queue duration")
            .labelNames("ProjectName", "PlanName", "JobName")
            .register();
    
    public static final Gauge JOB_PROCESSING_DURATION = Gauge.build().name("job_processing_duration")
            .help("Job processing duration")
            .labelNames("ProjectName", "PlanName", "JobName")
            .register();
    
    public static final Gauge JOB_START_TIME = Gauge.build().name("job_start_time")
            .help("Job start time")
            .labelNames("ProjectName", "PlanName", "JobName")
            .register();
    
    public static final Gauge JOB_END_TIME = Gauge.build().name("job_end_time")
            .help("Job end time")
            .labelNames("ProjectName", "PlanName", "JobName")
            .register();
    
    public static final Gauge TEST_NUMBER = Gauge.build().name("test_number")
            .help("Test number")
            .labelNames("ProjectName", "PlanName", "JobName")
            .register();
    
    // XXX FAILED_TESTS
    public static final Counter FAILED_TESTS = Counter.build().name(FAILED_TESTS_METRIC_NAME)
            .help(FAILED_TESTS_METRIC_DESCRIPTION)
            .labelNames( Parameter.CLASS_NAME.getLabelName(),
                    Parameter.TEST_NAME.getLabelName(),
                    Parameter.TEST_TYPE.getLabelName(),
                    Parameter.BRANCH.getLabelName(),
                    Parameter.JOB.getLabelName(),
                    Parameter.PLAN.getLabelName(),
                    "Error")
            .register();


    public void removeByLabels(@SuppressWarnings("rawtypes") SimpleCollector collector, Map<Parameter, String> parameters) {

        List<MetricFamilySamples> metrics = Arrays.asList(collector
                .collect().get(0));

        collector.clear();
        
        List<Sample> listSamples = metrics.get(0)
                .samples
                .stream()
                .filter(sample -> !isLabelMatches(sample, parameters))
                .collect(Collectors.toList());

        listSamples.forEach(sample -> {
                setLabels(collector, sample);
        });
    }

    private void setLabels(@SuppressWarnings("rawtypes") SimpleCollector collector, Sample sample) {

        String[] labels = sample.labelValues
                .toArray(new String[sample.labelValues.size()]);

        if (collector instanceof Counter){
            ((Counter.Child) collector.labels(labels)).inc(sample.value);
        }
        if (collector instanceof Gauge){
            ((Gauge.Child) collector.labels(labels)).set(sample.value);
        }
    }

    public boolean isLabelMatches(Sample sample,
            Map<Parameter, String> parameters) {
        
        return parameters.entrySet().stream().allMatch(entry -> {
            
            int labelIndex = getLabelIndex(entry.getKey());
            
            if (labelIndex == -1) {
                return false;
            }
            
            String labelValue = sample.labelValues.get(labelIndex);
            
            return labelValue.equals(entry.getValue());
        });
    }

    private int getLabelIndex(Parameter label) {
        return metricsList.indexOf(label.getLabelName());
    }
    
    public void setLabels(Map<Parameter, String> parameters, double value){
        
        String [] labels = new String[parameters.size()];
        
        parameters.forEach((k, v) -> {
            int index = getLabelIndex(k);
            labels[index] = v;
        });
                
        MetricCollector.DURATION_TESTS.labels(labels).set(value);
    }
 
}
