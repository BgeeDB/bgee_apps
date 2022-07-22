package org.bgee.controller.servletutils;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The default Tomcat {@code org.apache.catalina.filters.CorsFilter CorsFilter} does not work properly
 * to handle POST CORS queries (the header {@code Access-Control-Allow-Origin} is returned for GET queries,
 * not for POST queries), we had to implement our own {@code Filter} based on
 * https://stackoverflow.com/a/38364552/1768736.
 *
 * @author Frederic Bastian
 * @version Bgee 15 Oct. 2021
 * @since Bgee 15 Oct. 2021
 */
public class SimpleCORSFilter implements Filter {
    private final static Logger log = LogManager.getLogger(SimpleCORSFilter.class.getName());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        log.traceEntry("{}, {}, {}", request, response, chain);

        HttpServletResponse res = (HttpServletResponse) response;
        res.setHeader("Access-Control-Allow-Origin", "*");
        res.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        res.setHeader("Access-Control-Max-Age", "3600");
        res.setHeader("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type,"
                + "Access-Control-Request-Method, Access-Control-Request-Headers");
        chain.doFilter(request, res);

        log.traceExit();
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }
    @Override
    public void init(FilterConfig arg0) throws ServletException {
        // TODO Auto-generated method stub
    }
}