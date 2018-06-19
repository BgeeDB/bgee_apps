package org.bgee.view;

/**
 * Interface that defines the methods a display for the about category, i.e. page=about
 * has to implements
 *
 * @author 	Valentine Rech de Laval
 * @version Bgee 14, June 2018
 * @since   Bgee 13, Mar. 2015
 */
public interface AboutDisplay {

    /**
     * Display the about page.
     */
    public void displayAboutPage();

    /**
     * Display the privacy policy.
     */
    public void displayPrivacyPolicy();
   
}