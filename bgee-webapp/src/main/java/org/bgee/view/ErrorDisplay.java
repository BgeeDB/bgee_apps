package org.bgee.view;

/**
 * Interface defining the methods to be implemented by views to display error messages.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Jul 2015
 * @since   Bgee 13
 */
public interface ErrorDisplay {
    /**
     * Display an error message if no stored request parameters 
     * could be found using the key provided by the user (most likely in the URL). 
     * This display is launched when a <code>RequestParametersNotFoundException</code> is thrown. 
     * 
     * @param key   a <code>String</code> representing the key used when trying 
     *              to retrieve the stored parameters.
     * @see org.bgee.controller.exception.RequestParametersNotFoundException
     */
    public void displayRequestParametersNotFound(String key);
    /**
     * Display an error message when the number of parameters is not correct.
     * @param message   A {@code String} providing more details about the error.
     * @see org.bgee.controller.exception.RequestParametersNotStorableException
     */
    public void displayRequestParametersNotStorable(String message);
    /**
     * Display an error message when a <code>PageNotFoundException</code> is thrown 
     * (basically, a "404 not found"), most likely by a controller 
     * that could not understand a query.
     * 
     * @param message   A {@code String} providing more details about the error.
     * @see org.bgee.controller.exception.PageNotFoundException
     */
    public void displayPageNotFound(String message);
    /**
     * Display an error message when the number of parameters is not correct.
     * @param message   A {@code String} providing more details about the error.
     * @see org.bgee.controller.exception.MultipleValuesNotAllowedException
     */
    public void displayMultipleParametersNotAllowed(String message);
    /**
     * Display an error message when the format of parameters is not correct. 
     * @param message   A {@code String} providing more details about the error.
     * @see org.bgee.controller.exception.WrongFormatException
     */
    public void displayWrongFormat(String message);
    /**
     * Display an error message when a requested operation is not supported 
     * by the requested view.
     * @param message   A {@code String} providing more details about the error.
     */
    public void displayUnsupportedOperationException(String message);
    /**
     * Display the output expected in the case of a HTTP error 503.
     */
    public void displayServiceUnavailable();
    /**
     * Display an error message in an unexpected error occurred.
     */
    public void displayUnexpectedError();
}
