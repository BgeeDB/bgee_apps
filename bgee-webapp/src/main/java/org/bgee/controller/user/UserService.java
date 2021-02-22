package org.bgee.controller.user;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.RequestParameters;

/**
 * A service for {@link User}s.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Nov 2016
 * @see User
 * @since Bgee 13 Nov 2016
 */
public class UserService {
    private final static Logger log = LogManager.getLogger(UserService.class.getName());
    
    /**
     * Public 0-arg constructor.
     */
    public UserService() {
        //nothing here for now, it only serves as a factory
    }
    
    /**
     * Create a new {@code User instance}, using information retrieved from {@code request} 
     * and {@code requestParams}.
     * 
     * @param request           The {@code HttpServletRequest} sent by the user.
     * @param requestParams     The {@code RequestParameters} loaded from the request.
     * @return                  A new {@code User} object.
     */
    public User createNewUser(HttpServletRequest request, RequestParameters requestParams) {
        log.entry(request, requestParams);
        return log.traceExit(new User(request, requestParams));
    }
}
