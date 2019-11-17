package atlas.plugin.promexporter.metric;


import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Gauge;

@Component
public class MetricCollector {

    public static final String METRIC_NAME = "duration_test";
    public static final String METRIC_DESCRIPTION = "Duration test";

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

    public void removeByLabels(Map<Parameter, String> parameters) {

        List<MetricFamilySamples> metrics = Arrays.asList(DURATION_TESTS
                .collect().get(0));

         DURATION_TESTS.clear();
        
        List<Sample> listSamples = metrics.get(0)
                .samples
                .stream()
                .filter(sample -> !isLabelMatches(sample, parameters))
                .collect(Collectors.toList());

        listSamples.forEach(sample -> {
                setLabels(sample);
        });
    }

    private void setLabels(Sample sample) {

        String[] labels = sample.labelValues
                .toArray(new String[sample.labelValues.size()]);

        DURATION_TESTS.labels(labels).set(sample.value);
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
