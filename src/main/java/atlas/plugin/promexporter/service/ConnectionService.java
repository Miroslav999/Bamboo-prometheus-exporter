package atlas.plugin.promexporter.service;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import atlas.plugin.promexporter.manager.PromSettingsManager;
import atlas.plugin.promexporter.metric.Parameter;

public class ConnectionService {

    public static final int STATUS_OK = 204; 
    
    private final String PATH = "/api/v1/admin/tsdb/delete_series";
    private final String PARAMETERS = "{%s}";
    private final PromSettingsManager promSettingsManager;
    

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ConnectionService.class);

    public ConnectionService(PromSettingsManager promSettingsManager) {
        this.promSettingsManager = promSettingsManager;
    }

    public int removeMetricsFromPrometheus(Map<Parameter, String> parameters) {
        
        int statusCode = 400;
        
        if (promSettingsManager.getUrl() == null){
            return statusCode;
        }
        
        String url = promSettingsManager.getUrl();

        HttpClient client = new HttpClient();

        PostMethod method = new PostMethod(url + PATH);
        method.setRequestHeader("Content-Type",
                "application/x-www-form-urlencoded");

        String value = getPath(parameters);

        method.setParameter("match[]", value);

        LOGGER.info("Prometheus. The url of the request : " + url
                + " | parameters: " + value);
        try {
             statusCode = client.executeMethod(method);

            LOGGER.info("Prometheus. The status code of the request: "
                    + statusCode);

            if (method.getResponseBody()!=null){
                byte[] responseBody = method.getResponseBody();

                LOGGER.info("Prometheus. The body of the request: "
                        + new String(responseBody));
            }

        } catch (HttpException e) {
            e.printStackTrace();
            statusCode = 400;
        } catch (IOException e) {
            e.printStackTrace();
            statusCode = 400;
        } finally {
            method.releaseConnection();
        }
        return statusCode;
    }

    private String getPath(Map<Parameter, String> parameters) {
        StringBuilder url = new StringBuilder();
        parameters.forEach((k, v) -> {
            url.append(k.getLabelName());
            url.append("=");
            url.append("\"" + v + "\",");
        });
        return String.format(PARAMETERS, url.toString());
    }

}
