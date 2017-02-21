package org.apereo.cas.web.report;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlets.MetricsServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.endpoint.mvc.AbstractNamedMvcEndpoint;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.Properties;

/**
 * This is {@link MetricsController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class MetricsController extends AbstractNamedMvcEndpoint {

    private Properties initParameters = new Properties();

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("metrics")
    private MetricRegistry metrics;

    public MetricsController() {
        super("casmetrics", "/metrics", true, true);
    }

    /**
     * Handle.
     *
     * @param request  the request
     * @param response the response
     * @throws Exception the exception
     */
    @GetMapping
    public void handle(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final MetricsServlet servlet = new MetricsServlet(this.metrics);
        servlet.init(new DelegatingServletConfig());
        servlet.service(request, response);
    }

    private ServletContext getServletContext() {
        if (applicationContext instanceof WebApplicationContext) {
            final WebApplicationContext webCtx = (WebApplicationContext) applicationContext;
            return webCtx.getServletContext();
        }
        throw new IllegalArgumentException("Cant determine the web application context");
    }

    private class DelegatingServletConfig implements ServletConfig {
        private DelegatingServletConfig() {
        }

        @Override
        public String getServletName() {
            return MetricsController.this.getClass().getSimpleName();
        }

        @Override
        public ServletContext getServletContext() {
            return MetricsController.this.getServletContext();
        }

        @Override
        public String getInitParameter(final String paramName) {
            return MetricsController.this.initParameters.getProperty(paramName);
        }

        @Override
        public Enumeration getInitParameterNames() {
            return MetricsController.this.initParameters.keys();
        }
    }
}
