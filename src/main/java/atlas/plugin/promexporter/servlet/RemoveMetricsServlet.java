package atlas.plugin.promexporter.servlet;

import io.prometheus.client.exporter.common.TextFormat;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import atlas.plugin.promexporter.manager.PromSettingsManager;
import atlas.plugin.promexporter.metric.MetricCollector;
import atlas.plugin.promexporter.metric.Parameter;
import atlas.plugin.promexporter.service.ConnectionService;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.webresource.api.assembler.PageBuilderService;

@Scanned
public class RemoveMetricsServlet extends MainHttpServlet {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(RemoveMetricsServlet.class);
    private static final long serialVersionUID = -3610132083554159547L;
    
    private static final String CLEAR_PARAM = "clear";
    private final MetricCollector metricCollector;
    private ConnectionService connectionService;

    @ComponentImport
    private final TemplateRenderer templateRenderer;
    @ComponentImport
    private PageBuilderService pageBuilderService;

    @Inject
    public RemoveMetricsServlet(MetricCollector metricCollector,
            TemplateRenderer templateRenderer,
            PageBuilderService pageBuilderService,
            PromSettingsManager promSettingsManager) {
        this.metricCollector = metricCollector;
        this.templateRenderer = templateRenderer;
        this.pageBuilderService = pageBuilderService;
        connectionService = new ConnectionService(promSettingsManager);
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
                metricCollector.removeByLabels(labels);

            }; 
        }
        
        resp.setStatus(statusCode);
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType(TextFormat.CONTENT_TYPE_004);

    }

    @Override
    protected void doGet(final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse) throws IOException {

        pageBuilderService
                .assembler()
                .resources()
                .requireWebResource(
                        "prom.atlas.plugins.bamboo-prom-exporter:bootstrap-resources");
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
        httpServletResponse.setContentType("text/html");
        httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
        templateRenderer.render("metrics/metrics.vm",
                httpServletResponse.getWriter());

    }

}
