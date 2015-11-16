package org.bgee.controller.exception;

/**
 * An exception thrown when a stored {@code RequestParameters} object is requested based
 * on an indexed key value, but no {@code RequestParameters} object can be retrieved 
 * using that key. 
 * 
 * @author 	Frederic Bastian
 * @version Bgee 13, Nov. 2015
 * @see 	org.bgee.controller.RequestParameters
 * @see 	org.bgee.controller.URLParameters#DATA
 * @since 	Bgee 11
 *
 */
public class RequestParametersNotFoundException extends Exception implements ControllerException {
    private static final long serialVersionUID = -4828837007674742138L;
    
    /**
     * @see #getKey()
     */
    private final String key;
    
    /**
     * @param key   A {@code String} that is the key for which it was not possible 
     *              to retrieve the associated {@code RequestParameters} object.
     */
    public RequestParametersNotFoundException(String key) {
        super();
        this.key = key;
    }
    
    /**
     * @return  A {@code String} that is the key for which it was not possible 
     *          to retrieve the associated {@code RequestParameters} object.
     */
    public String getKey() {
        return this.key;
    }

    @Override
    public String getMessage() {
        return "It was not possible to retrieve any parameters stored associated to the key " 
                + this.key;
    }
}
