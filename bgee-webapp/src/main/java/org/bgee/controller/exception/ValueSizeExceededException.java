package org.bgee.controller.exception;

import org.bgee.controller.URLParameters;
/**
 * {@code Exception} thrown when the value of a parameter has exceeded its max allowed length.
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Nov. 2015
 * @see org.bgee.controller.RequestParameters
 * @since Bgee 13 Nov. 2015
 *
 */
public class ValueSizeExceededException extends URLParameterException {
    private static final long serialVersionUID = 47853627220232301L;

    /**
     * @param parameter The {@code URLParameters.Parameter} that was assigned incorrect values, 
     *                  which was the source of this {@code ValueSizeExceededException}.
     */
    public ValueSizeExceededException(URLParameters.Parameter<?> parameter) {
        super(parameter);
    }
}
