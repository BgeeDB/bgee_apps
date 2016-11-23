package org.bgee.view;

import java.util.List;

import org.bgee.model.file.SpeciesDataGroup;

/**
 * Interface that defines the methods a display for the general category (i.e. page= nothing 
 * that corresponds to another category) has to implements
 * 
 * @author  Mathieu Seppey
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Oct. 2016
 * @since   Bgee 1
 */
public interface GeneralDisplay {
    
    /**
     * Launch the display of the home page, using the {@code HttpServletResponse} 
     * provided at instantiation.
     * @param groups A {@code List} of single species datagroups displayed on the home page.
     * @throws IllegalArgumentException If {@code groups} contains multi-species data groups.
     */
    public void displayHomePage(List<SpeciesDataGroup> groups) throws IllegalArgumentException;
    /**
     * Launch a 204 response "No content" (request successfully processed but no content to return).
     */
    public void respondSuccessNoContent();
}
