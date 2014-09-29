package com.jayway.trumpet.server.rest;

import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

public class FormParameters {

    public static Map<String, String> getPrefixedParameters(HttpServletRequest request, String prefix) {
        Map<String, String> prefixed = new HashMap<>();

        request.getParameterMap().entrySet().stream()
                .filter(e -> e.getKey().startsWith(prefix))
                .filter(e -> e.getValue().length != 0)
                .forEach(e -> prefixed.put(e.getKey().substring(prefix.length() + 1), e.getValue()[0]));

        return prefixed;
    }
}
