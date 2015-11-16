package org.bgee.controller.exception;

import org.bgee.controller.URLParameters;

/**
 * This {@code Exception} is thrown when more than one value is provided
 * to a {@link org.bgee.controller.URLParameters.Parameter} that does not allow multiple values.
 * <p>
 * It extends a {@code RuntimeException} for convenience, as any such errors should be 
 * catch by the {@code FrontController} anyway, and methods throwing this exception 
 * are largely used through all the application for very basic operations.
 * 
 * @author  Mathieu Seppey
 * @author Frederic Bastian
 * @version Bgee 13, Dec 2014
 * @see     org.bgee.controller.RequestParameters
 * @since   Bgee 13
 */
public class MultipleValuesNotAllowedException extends URLParameterException {

    private static final long serialVersionUID = -7861781334770127365L;

    /**
     * @param parameter The {@code URLParameters.Parameter} that was assigned incorrect values, 
     *                  which was the source of this {@code MultipleValuesNotAllowedException}.
     */
    public MultipleValuesNotAllowedException(URLParameters.Parameter<?> parameter) {
        super(parameter);
    }
}
