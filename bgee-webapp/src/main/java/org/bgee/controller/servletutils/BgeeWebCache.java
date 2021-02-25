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
import net.sf.ehcache.constructs.web.PageInfo;
import net.sf.ehcache.constructs.web.filter.CachingFilter;
import net.sf.ehcache.constructs.web.filter.FilterNonReentrantException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.controller.URLParameters;
import org.bgee.controller.exception.MultipleValuesNotAllowedException;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.controller.exception.RequestParametersNotFoundException;
import org.bgee.controller.exception.RequestParametersNotStorableException;
import org.bgee.controller.exception.InvalidFormatException;

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
 * To use ehcache, the ehcache configuration xml file has to be present in
 * the resources folder
 * 
 * 
 * @author Mathieu Seppey
 * @version Bgee 13, Aug 2014
 * @see javax.servlet.Filter
 * @see net.sf.ehcache.constructs.web.filter.CachingFilter
 * @see net.sf.ehcache.constructs.web.PageInfo
 * @see org.bgee.controller.RequestParameters
 * @see org.bgee.controller.FrontController
 * @see #calculateKey(HttpServletRequest)
 * @see BgeeProperties#getWebpagesCacheConfigFileName()
 * @since Bgee 13
 */
public class BgeeWebCache extends CachingFilter
{

    private final static Logger log = LogManager.getLogger(BgeeWebCache.class.getName());

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
        log.entry(httpRequest);
        // Return the key. Note : the order of parameters does not matter
        return log.exit(DigestUtils.sha1Hex(httpRequest.getParameterMap().toString() 
                + httpRequest.getMethod()));
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
        log.entry(request, response, chain);
        BgeeProperties prop = BgeeProperties.getBgeeProperties();
        try {
            if (new RequestParameters(request,
                    new URLParameters(),prop, true, "&")
            .isACacheableRequest()){
                // Cacheble, forward it to the super class
                super.doFilter(request, response, chain);
            } else {
                // Not cachable, leave the caching process
                chain.doFilter(request, response);
            }
        } catch (RequestParametersNotFoundException | InvalidFormatException
                | RequestParametersNotStorableException
                | MultipleValuesNotAllowedException | PageNotFoundException e) {
            // If an Exception is thrown by the RequestParameter, call the next element in chain
            // and leave the caching process. The corresponding error page will be displayed and
            // nothing will be kept in cache.
            chain.doFilter(request, response);
        }  
        finally{
            // Remove the bgee properties instance from the pool
            BgeeProperties.removeFromBgeePropertiesPool();
        }
        log.exit();
    }

    /**
     * Gets the {@code CacheManager} for this {@code CachingFilter}.
     * It creates a cache based on the config xml file declared in 
     * {@code BgeeProperties#getWebpagesCacheConfigFileName()}
     * that has to be in the resources folder.
     */
    @Override
    protected CacheManager getCacheManager() {
        log.entry();
        return log.exit(CacheManager.create(BgeeWebCache.class
                .getClassLoader().getResource(BgeeProperties.getBgeeProperties()
                        .getWebpagesCacheConfigFileName())));
    }

    // Overridden because CachingFilter put a null body to the returned page when the
    // response is not 200.. and get rid of our nice 404 custom page. 
    // This method re throws the exception, so the normal chain produces the page again.
    // TODO : it works better than nothing because the 404 error page is displayed correctly
    // when the filter is on, but...
    // - it is a super inefficient way to solve the problem, because ehcache still put our wrong page
    // in cache with a null body and then the webapp is called again without cache to produce
    // the correct 404 error page.
    // However, it seems impossible to override CachingFilter#buildPageInfo to get rid of this
    // behavior because of the internal class VisitLog that is private and important to manage
    // the access to the cache.
    // - shouldn't be better to let the ehcache behavior happen normally so the 404 are
    // cached and in case of DOS on a non existing adresse it will help. And use redirection to
    // display error pages, which seems to be what CachingFilter expects us to do.
    @Override
    protected PageInfo buildPageInfo(final HttpServletRequest request,
            final HttpServletResponse response, final FilterChain chain)
                    throws Exception {
        log.entry(request, response, chain);
        PageInfo returnedPageInfo  = super.buildPageInfo(request, response, chain);
        if(! returnedPageInfo.isOk()){
            throw new PageNotFoundException();
        }
        return log.exit(returnedPageInfo);
    }
}
