package org.bgee.view;

/**
 * Interface that defines the methods a display for the download category, i.e. page=download
 * has to implements
 * @author  Mathieu Seppey
 * @version Bgee 13 Aug 2014
 * @since   Bgee 13
 */
public interface DownloadDisplay {
    
	/**
	 * Display the download page.
	 */
	public void displayDownloadPage();
	
	/**
     * Display the documentation page.
	 */
	public void displayDocumentation();
}
