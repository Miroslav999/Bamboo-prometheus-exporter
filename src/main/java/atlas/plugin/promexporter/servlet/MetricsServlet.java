package atlas.plugin.promexporter.servlet;

import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.exporter.common.TextFormat;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import atlas.plugin.promexporter.manager.PromSettingsManager;
import atlas.plugin.promexporter.metric.MetricCollector;
import atlas.plugin.promexporter.metric.Parameter;
import atlas.plugin.promexporter.service.ConnectionService;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MetricsServlet extends MainHttpServlet {
    private static final long serialVersionUID = 1944150902679688099L;
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MetricsServlet.class);
    
    private static final String CLEAR_PARAM = "clear";
    private final MetricCollector metricCollector;
    private ConnectionService connectionService;
    private final String VALUE = "Value";
    private GsonBuilder gsonMapBuilder = new GsonBuilder();
    
    public MetricsServlet(MetricCollector metricCollector,PromSettingsManager promSettingsManager) {
        this.metricCollector = metricCollector;
        this.connectionService = new ConnectionService(promSettingsManager);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Gson gsonObject = gsonMapBuilder.create();

        Map<String, String[]> requestParameters = req.getParameterMap();

        List<Sample> sampleStorage = Arrays.asList(
                MetricCollector.DURATION_TESTS.collect().get(0)).get(0).samples;

        if (requestParameters.size() > 0) {
            Map<Parameter, String> parameters = convertHttpParamToMap(requestParameters);
            sampleStorage = sampleStorage
                    .stream()
                    .filter(sample -> metricCollector.isLabelMatches(sample,
                            parameters)).collect(Collectors.toList());
        }

        Object[] objs = sampleStorage.stream().map(sample -> {

            Map<String, String> map = new HashMap<>();

            for (int i = 0; i < sample.labelNames.size(); i++) {

                map.put(sample.labelNames.get(i), sample.labelValues.get(i));

            }

            double value = sample.value;

            map.put(VALUE, String.valueOf(value));

            return map;
        }).toArray();

        String contentResp = gsonObject.toJson(objs);

        resp.setContentType("application/json");

        resp.setHeader("Access-Control-Allow-Origin", "*");

        resp.getWriter().print(contentResp);

        resp.getWriter().flush();
    }
    
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        LOGGER.info("Prometheus: doDelete " + req.getParameterMap().toString());

        if (Boolean.TRUE.toString().equals(req.getParameter(CLEAR_PARAM))) {
            MetricCollector.DURATION_TESTS.clear();
            return;
        }

        Map<String, String[]> requestParameters = req.getParameterMap();
        
        int statusCode = 400;
        
        if (requestParameters.size() > 0) {

            Map<Parameter, String> labels = convertHttpParamToMap(requestParameters);

            statusCode = connectionService.removeMetricsFromPrometheus(labels);
            
            if (statusCode == ConnectionService.STATUS_OK){
                metricCollector.removeByLabels(MetricCollector.DURATION_TESTS, labels);

            }; 
        }
        
        resp.setStatus(statusCode);
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType(TextFormat.CONTENT_TYPE_004);

    }
}
