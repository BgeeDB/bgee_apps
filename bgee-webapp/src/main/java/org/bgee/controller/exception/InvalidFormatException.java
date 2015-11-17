package org.bgee.controller.exception;

import org.bgee.controller.URLParameters;

/**
 * This {@code Exception} is thrown when an value with a wrong format is provided
 * to a {@link org.bgee.controller.URLParameters.Parameter}.
 * <p>
 * It extends a {@code RuntimeException} for convenience, as any such errors should be 
 * catch by the {@code FrontController} anyway, and methods throwing this exception 
 * are largely used through all the application for very basic operations.
 * 
 * @author  Mathieu Seppey
 * @author  Frederic Bastian
 * @version Bgee 13, Nov. 2015
 * @see     org.bgee.controller.RequestParameters
 * @since   Bgee 13
 */
public class InvalidFormatException extends URLParameterException {
    private static final long serialVersionUID = -8910679982751616114L;
    
    /**
     * @param parameter The {@code URLParameters.Parameter} that was assigned incorrect values, 
     *                  which was the source of this {@code InvalidFormatException}.
     */
    public InvalidFormatException(URLParameters.Parameter<?> parameter) {
        super(parameter);
    }

    /**
     * @param parameter The {@code URLParameters.Parameter} that was assigned incorrect values, 
     *                  which was the source of this {@code InvalidFormatException}.
     * @param e         Underlying exception thrown.
     */
    public InvalidFormatException(URLParameters.Parameter<?> parameter, Throwable e) {
        super(parameter, e);
    }
}
