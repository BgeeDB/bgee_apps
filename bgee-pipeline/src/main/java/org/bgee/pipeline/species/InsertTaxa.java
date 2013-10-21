package org.bgee.pipeline.species;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class allows to insert species used in Bgee, and all their ancestor taxa. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class InsertTaxa {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(InsertTaxa.class.getName());
    /**
     * A {@code String} representing the default location of the file containing 
     * taxonomy data.
     */
    public static final String TAXONOMYFILEURL = "ftp://ftp.ncbi.nlm.nih.gov/pub/taxonomy/taxdmp.zip";
    
    /**
     * {@code main} method which the following parameters should be provided to: 
     * <ul>
     * <li>First entry in {@code args}: path to the file containing the species used 
     * in Bgee, as a TSV file. First line should be a header line, first column should 
     * contain the NCBI taxonomy ID of the species to use (for instance, {@code 9606} 
     * for human). One ID per line. <strong>required</strong>.
     * <li>Second entry in {@code args}: URL to download the NCBI taxonomy data.  
     * <strong>optional</strong>, default value is {@link #TAXONOMYFILEURL}.
     * </ul>
     * @param args  An {@code Array} of {@code String}s containing parameters. 
     * @throws IllegalArgumentException If the required parameters are not provided, 
     *                                  or does not allow to obtain the required data.
     * @throws IOException 
     */
    public static void main(String[] args) throws IllegalArgumentException, IOException {
        log.entry((Object[]) args);
        
        //try to get the species file
        /*try {
            File speciesFile = new File(args[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw log.throwing(new IllegalArgumentException("The parameters did not contain " +
            		"the path to the species file"));
        } catch (Exception e) {
            throw log.throwing(new IllegalArgumentException("An error occurred when trying " +
            		"to access the species file", e));
        }*/
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(TAXONOMYFILEURL);
            InputStream inputStream = ftpClient.retrieveFileStream(TAXONOMYFILEURL);
        } finally {
            ftpClient.disconnect();
        }
        
        log.exit();
    }
}
