package org.bgee.controller.servletutils;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.constructs.blocking.LockTimeoutException;
import net.sf.ehcache.constructs.web.AlreadyCommittedException;
import net.sf.ehcache.constructs.web.AlreadyGzippedException;
import net.sf.ehcache.constructs.web.filter.CachingFilter;
import net.sf.ehcache.constructs.web.filter.FilterNonReentrantException;

import org.apache.commons.codec.digest.DigestUtils;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.controller.URLParameters;
import org.bgee.controller.exception.MultipleValuesNotAllowedException;
import org.bgee.controller.exception.RequestParametersNotFoundException;
import org.bgee.controller.exception.RequestParametersNotStorableException;
import org.bgee.controller.exception.WrongFormatException;

/**
 * This class is a {@code Filter} that manages Web pages caching, thus it has to be placed
 * as a filter before the main controller.
 * It extends {@code CachingFilter} that caches the Web pages into a ehcache storage.
 * It stores the content of the {@code HttpServletResponse} in the form of a {@code PageInfo} 
 * associated with a key generated using the parameters of the request.
 * If a page is not in cache, the filter will allow the next step in the chain to be
 * processed normally and cache the result.
 * If the page is in cache, the filter will recreate and return a {@code HttpServletResponse} 
 * based on the cached {@code PageInfo} without any call further into the chain, i.e. the
 * {@code FrontController}
 * 
 * Not all request can be cached, for example pages with login information should never be
 * kept. Thus a call to {@code RequestParameters#isACacheableRequest} is made before putting
 * any request in cache.
 * 
 * @author Mathieu Seppey
 * @version Bgee 13, Aug 2014
 * @see javax.servlet.Filter
 * @see net.sf.ehcache.constructs.web.filter.CachingFilter
 * @see net.sf.ehcache.constructs.web.PageInfo
 * @see org.bgee.controller.RequestParameters
 * @see org.bgee.controller.FrontController
 * @see #calculateKey(HttpServletRequest)
 * @since Bgee 13
 */
public class BgeeWebCache extends CachingFilter
{

    /**
     * Compute a key for the current request. Same parameters and the same http method
     * will generate the same key.
     * 
     * @param httpRequest   the {@code HttpServletRequest}being currently processed
     * @return              a {@code String}corresponding to the key
     */
    @Override
    protected String calculateKey(HttpServletRequest httpRequest)
    {   
        // Return the key. Note : the order of parameters does not matter
        return DigestUtils.sha1Hex(httpRequest.getParameterMap().toString() 
                + httpRequest.getMethod());
    }

    /**
     * Perform the filtering for a request. It overrides the parent method because not all pages
     * should be cached, for example those with login information. Consequently, it uses a
     * {@code RequestParameters} to assess whether the request is cacheable.
     * When it is the case, the {@link CachingFilter#doFilter} behavior takes place, and when it
     * is not the case, the request is directly forwarded to the next element in the chain
     * 
     * @param request   the {@code HttpServletRequest} being currently processed
     * @param response  the {@code HttpServletResponse} to send responses to the client 
     *                  (would be wrapped if cached)
     * @param chain     the {@code FilterChain} used to invoke the next entity in the servlet
     *                  filter chain. if the page should not be cache
     */
    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) 
            throws AlreadyCommittedException, AlreadyGzippedException, FilterNonReentrantException,
            LockTimeoutException, IOException, ServletException, Exception
    {
        // Call the protected that actually does the job, with a RequestParameters
        doFilter(request, response, chain, 
                new RequestParameters(request,new URLParameters(),BgeeProperties.getBgeeProperties()));
    }

    /**
     * Perform the filtering for a request. This protected method allows the injection of a 
     * {@code RequestParameters}. It should be called only internally by the overridden
     * method {@link #doFilter(HttpServletRequest, HttpServletResponse, FilterChain)} or
     * for unit test purpose.
     * 
     * @param request           the {@code HttpServletRequest} being currently processed
     * @param response          the {@code HttpServletResponse} to send responses to the client 
     *                          (would be wrapped if cached)
     * @param chain             the {@code FilterChain} used to invoke the next entity in the servlet
     *                          filter chain. if the page should not be cache
     * @param requestParameter  A {@code RequestParamters} that is used to check whether the request
     *                          is cacheable 
     */
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, 
            FilterChain chain, RequestParameters requestParameter) 
                    throws AlreadyCommittedException, AlreadyGzippedException, FilterNonReentrantException,
                    LockTimeoutException, IOException, ServletException, Exception
    {
        try {
            if (requestParameter.isACacheableRequest()) {
                // Cacheble, forward it to the super class
                super.doFilter(request, response, chain);
            } else {
                // Not cachable, leave the caching process
                chain.doFilter(request, response);
            }
        } catch (RequestParametersNotFoundException | WrongFormatException | RequestParametersNotStorableException
                | MultipleValuesNotAllowedException e) {
            // If an Exception is thrown by the RequestParameter, call the next element in chain
            // and leave the caching process. The corresponding error page will be displayed and
            // nothing will be kept in cache.
            chain.doFilter(request, response);
        }  
    }

    @Override
    protected CacheManager getCacheManager() {
        return CacheManager.getInstance();
    }
}
