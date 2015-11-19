package org.bgee.view;

import org.bgee.controller.exception.InvalidFormatException;
import org.bgee.controller.exception.InvalidRequestException;
import org.bgee.controller.exception.MultipleValuesNotAllowedException;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.controller.exception.RequestParametersNotFoundException;
import org.bgee.controller.exception.RequestParametersNotStorableException;
import org.bgee.controller.exception.RequestSizeExceededException;
import org.bgee.controller.exception.ValueSizeExceededException;

/**
 * Interface defining the methods to be implemented by views to display error messages.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Nov. 2015
 * @since   Bgee 13
 */
public interface ErrorDisplay {

    //******************************
    // CONTROLLER EXCEPTION TYPES
    //******************************
    /**
     * Display an error message following an {@link InvalidFormatException}.
     * @param e     The {@link InvalidFormatException} to display details about.
     */
    public void displayControllerException(InvalidFormatException e);
    /**
     * Display an error message following an {@link InvalidFormatException}.
     * @param e     The {@link InvalidFormatException} to display details about.
     */
    public void displayControllerException(InvalidRequestException e);
    /**
     * Display an error message following an {@link InvalidFormatException}.
     * @param e     The {@link InvalidFormatException} to display details about.
     */
    public void displayControllerException(MultipleValuesNotAllowedException e);
    /**
     * Display an error message following an {@link InvalidFormatException}.
     * @param e     The {@link InvalidFormatException} to display details about.
     */
    public void displayControllerException(RequestSizeExceededException e);
    /**
     * Display an error message following an {@link InvalidFormatException}.
     * @param e     The {@link InvalidFormatException} to display details about.
     */
    public void displayControllerException(ValueSizeExceededException e);

    /**
     * Display an error message following an {@link InvalidFormatException}.
     * @param e     The {@link InvalidFormatException} to display details about.
     */
    public void displayControllerException(PageNotFoundException e);
    /**
     * Display an error message following an {@link InvalidFormatException}.
     * @param e     The {@link InvalidFormatException} to display details about.
     */
    public void displayControllerException(RequestParametersNotFoundException e);
    /**
     * Display an error message following an {@link InvalidFormatException}.
     * @param e     The {@link InvalidFormatException} to display details about.
     */
    public void displayControllerException(RequestParametersNotStorableException e);
    
    //******************************
    // OTHER EXCEPTION TYPES
    //******************************
    /**
     * Display an error message when a requested operation is not supported 
     * by the requested view.
     */
    public void displayUnsupportedOperationException();
    /**
     * Display the output expected in the case of a HTTP error 503.
     */
    public void displayServiceUnavailable();
    /**
     * Display an error message if an unexpected error occurred.
     */
    public void displayUnexpectedError();
}
