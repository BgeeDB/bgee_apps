package org.bgee.view;

/**
 * Interface that defines the methods a display for the general category (i.e. page= nothing 
 * that corresponds to another category) has to implements
 * 
 * @author  Mathieu Seppey
 * @author Frederic Bastian
 * @version Bgee 13 Aug 2014
 * @since   Bgee 1
 */
public interface GeneralDisplay {
    /**
     * Launch the display of the "about" page, using the {@code HttpServletResponse} 
     * provided at instantiation.
     */
    public void displayAbout();
    
    /**
     * Launch the display of the home page, using the {@code HttpServletResponse} 
     * provided at instantiation.
     */
    public void displayHomePage();
}
