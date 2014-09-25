package com.jayway.trumpet.server.rest;

import org.slf4j.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.list;


public class LoggingFilter implements Filter {

    private final Logger logger;
    private final boolean enabled;


    public LoggingFilter(boolean enabled, Logger logger) {
        this.enabled = enabled;
        this.logger = logger;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if(!enabled) return;

        StringBuilder sb = new StringBuilder();

        logResourceUrl((HttpServletRequest) request, sb);
        logHeaders((HttpServletRequest) request, sb);
        logParameters((HttpServletRequest) request, sb);

        logger.debug(sb.toString());

        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}


    private void logResourceUrl(HttpServletRequest request, StringBuilder sb){

        String time = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME);

        sb.append("----------------------------------------------------------\n");
        sb.append(time + " " + request.getMethod() + " " + request.getRequestURI()).append("\n");
        sb.append("----------------------------------------------------------\n");
    }

    private void logHeaders(HttpServletRequest request, StringBuilder sb){
        List<String> headerNames = list(request.getHeaderNames());

        if(headerNames.isEmpty()){
            return;
        }
        sb.append("Headers: \n");
        for (String headerName : headerNames) {
            sb.append("    ");
            sb.append(headerName);
            sb.append(" = ");
            sb.append(request.getHeader(headerName));
            sb.append("\n");
        }
    }

    private void logParameters(HttpServletRequest request, StringBuilder sb){
        Map<String, String[]> parameterMap = request.getParameterMap();
        if(parameterMap.isEmpty()){
            return;
        }
        sb.append("Parameters: \n");
        for (Map.Entry<String, String[]> parameter : parameterMap.entrySet()) {
            sb.append("    ");
            sb.append(parameter.getKey());
            sb.append(" = ");
            sb.append(asList(parameter.getValue()).stream().collect(Collectors.joining(", ", "[", "]")));
            sb.append("\n");
        }
    }
}
