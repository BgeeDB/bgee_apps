package org.bgee.view;

import java.util.List;

import org.bgee.model.file.SpeciesDataGroup;

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
     * @param groups A {@code List} of single species datagroups displayed on the home page.
     */
    public void displayHomePage(List<SpeciesDataGroup> groups);
}
