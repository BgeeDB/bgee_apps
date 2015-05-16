package org.bgee.view;

/**
 * Interface that defines the methods a display for the download category, i.e. page=download
 * has to implements
 * @author  Mathieu Seppey
 * @author  Valentine Rech de Laval
 * @version Bgee 13 May 2014
 * @since   Bgee 13
 */
public interface DownloadDisplay {
    
	/**
	 * Display the download page.
	 */
	public void displayDownloadHomePage();
	
    /**
     * Display the download page of processed raw data files.
     */
    public void displayProcessedRawDataDownloadPage();

    /**
     * Display the download page of gene expression call files.
     */
    public void displayGeneExpressionCallDownloadPage();

}
