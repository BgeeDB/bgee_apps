package org.bgee.controller.exception;

/**
 * {@code Exception} thrown when the overall size of a request exceeds a defined threshold, 
 * for security reason.
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Nov. 2015
 * @see org.bgee.controller.RequestParameters
 * @since Bgee 13 Nov. 2015
 *
 */
public class RequestSizeExceededException extends RuntimeException implements ControllerException {
    private static final long serialVersionUID = 4098699391535542998L;

    /**
     * Default constructor.
     */
    //No other information to be provided, we do not provide the maximum allowed length, 
    //and we cannot always know the real offending length (if we stop iterating the parameters 
    //of a query).
    public RequestSizeExceededException() {
        super();
    }
}
