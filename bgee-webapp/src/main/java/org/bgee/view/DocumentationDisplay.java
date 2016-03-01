package org.bgee.view;

/**
 * Interface that defines methods displaying the documentation category, i.e. page=documentation
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Feb 2016
 * @since   Bgee 13
 */
public interface DocumentationDisplay {
    
    /**
     * Display the documentation home page.
     */
    public void displayDocumentationHomePage();

    /**
     * Display the documentation about expression call download files.
     */
    public void displayCallDownloadFileDocumentation();
    /**
     * Display the documentation about reference expression download files.
     */
    public void displayRefExprDownloadFileDocumentation();
    
    /**
     * Display the documentation about TopAnat.
     */
    public void displayTopAnatDocumentation();
    
    /**
     * Display the documentation about how to access to Bgee data.
     */
    public void displayHowToAccessDataDocumentation();
    
}
