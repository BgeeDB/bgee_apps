package org.bgee.view;

/**
 * Interface defining methods to be implemented by views related to Resources.
 * 
 * @author  Julien Wollbrett
 * @version Bgee 14, May 2019
 * @since   Bgee 14, May 2019
 */

public interface ResourceDisplay {
    
    /**
     * Displays the 'R Packages' page.
     */
    void displayRPackages();
    
    /**
     * Displays the 'annotations' page.
     */
    void displayAnnotations();
    
    /**
     * Displays the 'ontologies' page.
     */
    void displayOntologies();
    
    /**
     * Displays the 'source_code' page.
     */
    void displaySourceCode();

}
