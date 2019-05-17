package org.bgee.controller;

import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.model.ServiceFactory;
import org.bgee.view.ViewFactory;

import javax.servlet.http.HttpServletResponse;

public class CommandRedirect extends CommandParent {
    
    public CommandRedirect(HttpServletResponse response, RequestParameters requestParameters, BgeeProperties prop,
                           ViewFactory viewFactory, ServiceFactory serviceFactory) {
        super(response, requestParameters, prop, viewFactory, serviceFactory);
    }

    @Override
    public void processRequest() throws Exception {
        
        if (this.requestParameters.isPostFormSubmit()) {
            //if this is not an AJAX request, and the data are submitted by POST method, 
            //then we need to redirect the user (to avoid an annoying message when pressing 'back', 
            //and also to try to put all requested parameters into the URL if they are not too long, 
            //otherwise, and ID allowing to retrieve the parameters will be added to the URL 
            //(see RequestParameters#getRequestURI()))

            //get the requested URI, trying to put all parameters in the URL
            //get the URI without URLencoding it, because it will be done by the method 
            //<code>encodeRedirectURL</code> of the <code>HttpServletResponse</code>

            //encodeRedirectURL is supposed to be the way of properly redirecting users, 
            //but it actually does not encode \n, so, forget it... we provide an url already URL encoded
            //and we do not care about sessionid passed by URL anyway.
            //so finally, we do not use this.requestParameters.getRequestURL(false) anymore
            this.requestParameters.setPostFormSubmit(false);
            this.response.setStatus(HttpServletResponse.SC_SEE_OTHER);
            this.response.addHeader("Location", this.requestParameters.getRequestURL());
            this.response.getWriter().close();
        } else {
            throw new PageNotFoundException("Wrong parameters");
        }
    }
}
