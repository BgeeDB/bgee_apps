package org.bgee.model.topanat.exception;

/**
 * An {@code Exception} launched when a TopAnat analysis has failed in R 
 * due to unknown reason. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Dec. 2015
 * @since Bgee 13 Dec. 2015
 */
public class RAnalysisException extends Exception {

    private static final long serialVersionUID = 8275905684305500715L;
    
    public RAnalysisException(String message, Exception cause) {
        super(message, cause);
    }

}
