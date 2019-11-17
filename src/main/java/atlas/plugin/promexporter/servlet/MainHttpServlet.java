package atlas.plugin.promexporter.servlet;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServlet;

import atlas.plugin.promexporter.exception.NotFoundException;
import atlas.plugin.promexporter.metric.Parameter;

public class MainHttpServlet extends HttpServlet{
    private static final long serialVersionUID = -2305934486905473709L;
    private final String NOT_FOUND_PARAM_MSG = "The parameter %s is not found";
    private final String ILLIGAL_ARG_MSG = "The parameter %s is not set to a value";
    
    protected Map<Parameter, String> convertHttpParamToMap(Map<String, String[]> requestParameters){
        return requestParameters
                .entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                entry -> Parameter
                                        .getParameter(entry.getKey())
                                        .orElseThrow(
                                                () -> new NotFoundException(
                                                        String.format(
                                                                NOT_FOUND_PARAM_MSG,
                                                                entry.getKey()))),
                                entry -> Stream
                                        .of(entry.getValue())
                                        .findFirst()
                                        .orElseThrow(
                                                () -> new IllegalArgumentException(
                                                        String.format(
                                                                ILLIGAL_ARG_MSG,
                                                                entry.getKey())))));
        
    }
    
}
