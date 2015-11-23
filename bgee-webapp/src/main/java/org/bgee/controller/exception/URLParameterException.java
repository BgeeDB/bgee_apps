package org.bgee.controller.exception;

import org.bgee.controller.URLParameters;

/**
 * Super class of all {@code Exception}s related to the use of incorrect values for  
 * {@link org.bgee.controller.URLParameters.Parameter}s. 
 * 
 * @author  Frederic Bastian
 * @version Bgee 13 Nov. 2015
 * @since   Bgee 13 Nov. 2015
 */
public abstract class URLParameterException extends RuntimeException implements ControllerException {
    private static final long serialVersionUID = 7822967418537921742L;
    
    /**
     * @see #getParameter()
     */
    private final URLParameters.Parameter<?> urlParameter;

    /**
     * @param parameter The {@code URLParameters.Parameter} that was assigned incorrect values, 
     *                  which was the source of this {@code URLParameterException}.
     */
    protected URLParameterException(URLParameters.Parameter<?> parameter) {
        super();
        this.urlParameter = parameter;
    }
    /**
     * @param parameter The {@code URLParameters.Parameter} that was assigned incorrect values, 
     *                  which was the source of this {@code URLParameterException}.
     * @param e         Underlying exception thrown.
     */
    protected URLParameterException(URLParameters.Parameter<?> parameter, Throwable e) {
        super("Incorrect parameter: " + parameter.getName(), e);
        this.urlParameter = parameter;
    }
    
    /**
     * @return  The {@code URLParameters.Parameter} that was assigned incorrect values, 
     *          which was the source of this {@code URLParameterException}.
     */
    public URLParameters.Parameter<?> getURLParameter() {
        return urlParameter;
    }
    
    @Override
    public String getMessage() {
        return "Incorrect parameter: " + this.getURLParameter().getName();
    }
}
