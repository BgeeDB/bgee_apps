package org.bgee.pipeline.similarity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.Utils;
import org.supercsv.cellprocessor.constraint.NotNull;

/**
 * Class related to the use, verification, and insertion into the database 
 * of the annotations of similarity between Uberon terms (similarity in the sense 
 * of the term in the HOM ontology HOM:0000000).
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class SimilarityAnnotation {
    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = 
            LogManager.getLogger(SimilarityAnnotation.class.getName());
    /**
     * An {@code int} that is the index of the column containing the taxon IDs 
     * in the similarity annotation file.
     */
    private final static int TAXONCOLUMNINDEX = 9;
    /**
     * An {@code int} that is the total number of columns in the similarity 
     * annotation file.
     */
    private final static int COLUMNCOUNT = 14;
    
    /**
     * Extract from the similarity annotation file the list of all taxon IDs used. 
     * 
     * @param annotFile A {@code String} that is the path to the similarity annotation file.
     * @return          A {@code Set} of {@code String}s that contains all IDs 
     *                  of the taxa used in the annotation file.
     * @throws FileNotFoundException    If {@code annotFile} could not be found.
     * @throws IOException              If {@code annotFile} could not be read.
     */
    public Set<String> extractTaxonIds(String annotFile) 
            throws FileNotFoundException, IOException {
        log.entry(annotFile);
        
        Set<String> taxonIds = new HashSet<String>(Utils.parseColumnAsString(
                annotFile, TAXONCOLUMNINDEX, COLUMNCOUNT, new NotNull()));
        
        if (taxonIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("The annotation file " +
                    annotFile + " did not contain any valid taxon ID"));
        }
        
        return log.exit(taxonIds);
    }
}
