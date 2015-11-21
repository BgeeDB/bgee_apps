package org.bgee.controller.exception;

/**
 * This {@code Exception} is thrown when a {@code RequestParameters} object 
 * could not be serialized and stored in a file, to be later retrieved. 
 * <p>
 * Usually, when parameters of a request are too long to be passed through URL, 
 * a key is generated, and is used i) to name the file where the {@code RequestParameters} object 
 * holding the parameters is serialized, 
 * and ii) to replace some parameters in the URL by this key (to shorten it). 
 * If something goes wrong when trying to generate the key, or to serialize 
 * and store the {@code RequestParameters} object, this {@code RequestParametersNotStorableException} 
 * is thrown.
 * <p>
 * This {@code Exception} can be thrown when trying to obtain an URL 
 * from a {@link org.bgee.controller.RequestParameters} object (see {@code getRequestURL} methods), 
 * as it is at this moment that a key is potentially generated to shorten URL, 
 * and the {@code RequestParameters} object tried to be stored.
 * <p>
 * It extends a {@code RuntimeException} for convenience, because the {@code getRequestURL} 
 * methods are largely used through all the application, and it is really bothersome 
 * to manage such an exception only to obtain a very basic (and short) URL.
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Nov. 2015
 * @see org.bgee.controller.RequestParameters
 * @since Bgee 11
 *
 */
public class RequestParametersNotStorableException extends RuntimeException implements ControllerException {

    private static final long serialVersionUID = -4587049017135736170L;

    /**
     * Constructor with an additional {@code message} argument. 
     * 
     * @param message 		a {@code String} giving details about the exception.
     */
    public RequestParametersNotStorableException(String message) {
        super(message);
    }
}
