package org.bgee.view;

/**
 * Interface defining the methods that views related to topAnat must implemented. 
 * 
 * @author  Frederic Bastian
 * @version Bgee 13 Jul 2015
 * @since   Bgee 13
 */
public interface TopAnatDisplay {
    /**
     * Display the topAnat home page.
     */
    public void displayTopAnatHomePage();
    
    /**
     * Display the topAnat waiting page.
     */
    public void displayTopAnatWaitingPage();
    
    /**
     * Display the topAnat page with displayed results.
     */
    public void displayTopAnatResultPage();
}
