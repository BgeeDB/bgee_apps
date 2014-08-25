package org.bgee.view;

/**
 * Interface that defines the methods that all displays have to implements
 * @author  Mathieu Seppey
 * @version Bgee 13 Aug 2014
 * @since   Bgee 1
 */
public interface DisplayParentInterface 
{		
    public void emptyDisplay();

    public void startDisplay(String page, String title);

    public void displayBgeeMenu();
}
