package com.jayway.trumpet.server.rest;

import com.jayway.trumpet.server.boot.ForceClientUpgradeConfig;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static java.lang.String.format;

/**
 * Servlet filter that checks that the clients supplies a valid Api-Key header.
 */
public class ForceClientUpgradeFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(ForceClientUpgradeFilter.class);

    private static final String CLIENT_TYPE = "X-CLIENT-PLATFORM";
    private static final String CLIENT_VERSION = "X-CLIENT-VERSION";
    private static final String ERROR_CONTENT_TYPE = "application/json";
    private static final String ERROR_CONTENT_ENCODING = "UTF-8";
    private static final int BAD_REQUEST = 400;
    private static final String ANDROID = "android";
    private static final String DEFAULT_VERSION = "0.0.0";

    private final ForceClientUpgradeConfig config;

    public ForceClientUpgradeFilter(ForceClientUpgradeConfig config) {
        this.config = config;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String version = Optional.ofNullable(request.getHeader(CLIENT_VERSION)).orElse(DEFAULT_VERSION).toLowerCase().trim();
        if (config.isForceUpgradingOfOldClientsEnabled() && !version.isEmpty()) {
            version = StringUtils.countMatches(version, ".") == 2 ? version : DEFAULT_VERSION; // For backward compatibility with older clients which sends version in another format
            ArtifactVersion clientMinVersion = new DefaultArtifactVersion(version);
            String clientType = Optional.ofNullable(request.getHeader(CLIENT_TYPE)).orElse(ANDROID).toLowerCase().trim();
            final ArtifactVersion minRequiredVersion;
            switch (clientType) {
                case ANDROID:
                    minRequiredVersion = new DefaultArtifactVersion(config.androidMinVersion());
                    break;
                default:
                    minRequiredVersion = new DefaultArtifactVersion(DEFAULT_VERSION);
            }

            if (clientMinVersion.compareTo(minRequiredVersion) < 0) {
                ((HttpServletResponse) servletResponse).setStatus(BAD_REQUEST);
                servletResponse.setContentType(ERROR_CONTENT_TYPE);
                servletResponse.setCharacterEncoding(ERROR_CONTENT_ENCODING);
                servletResponse.getOutputStream().print(format("{ \"minRequiredVersion\" : \"%s\", \"upgradeRequired\" : true }", minRequiredVersion.toString()));
                return;
            }

        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
    }
}
