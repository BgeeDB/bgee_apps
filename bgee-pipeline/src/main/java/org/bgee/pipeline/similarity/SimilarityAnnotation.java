package org.bgee.pipeline.similarity;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
     * An {@code String} that is the name of the column containing the taxon IDs 
     * in the similarity annotation file.
     */
    private final static String TAXONCOLUMNNAME = "taxon ID";
    /**
     * An {@code int} that is the total number of columns in the similarity 
     * annotation file.
     */
    //private final static int COLUMNCOUNT = 14;
    
    /**
     * Several actions can be launched from this main method, depending on the first 
     * element in {@code args}: 
     * <ul>
     * <li>If the first element in {@code args} is "extractTaxonIds", the action 
     * will be to extract the set of taxon IDs present in a similarity annotation file, 
     * and to write them in another file (with no headers), one ID per line. 
     * Following elements in {@code args} must then be: 
     *   <ol>
     *   <li>path to the similarity annotation file to extract taxon IDs from.
     *   <li>path to the output file where write taxon IDs into, one per line.
     *   </ol>
     * </ul>
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IllegalArgumentException If {@code args} does not contain the proper 
     *                                  parameters or does not allow to obtain 
     *                                  correct information.
     * @throws UnsupportedEncodingException If incorrect encoding was used to write 
     *                                      in output file.
     * @throws FileNotFoundException    If the annotation file provided could not be found.
     * @throws IOException              If the annotation file provided could not be read.
     */
    public static void main(String[] args) throws UnsupportedEncodingException,
        FileNotFoundException, IOException {
        
        log.entry((Object[]) args);
        
        if (args[0].equalsIgnoreCase("extractTaxonIds")) {
            if (args.length != 3) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " + 
                        "3 arguments, " + args.length + " provided."));
            }
            new SimilarityAnnotation().extractTaxonIdsToFile(args[1], args[2]);
        }
        
        log.exit();
    }
    
    /**
     * Extract from the similarity annotation file {@code annotFile} the list 
     * of all taxon IDs used, and write them in {@code outputFile}, one ID per line. 
     * The first line of the annotation file should be a header line, defining 
     * a column to get IDs from, named exactly "taxon ID". The output file will 
     * have no headers. The IDs are supposed to be {@code Integer}s corresponding to 
     * the NCBI ID, for instance, "9606" for human.
     * 
     * @param annotFile     A {@code String} that is the path to the similarity 
     *                      annotation file.
     * @param outputFile    A {@code String} that is the path to the file where 
     *                      to write IDs into.
     * @throws UnsupportedEncodingException If incorrect encoding was used to write 
     *                                      in output file.
     * @throws FileNotFoundException        If {@code annotFile} could not be found.
     * @throws IOException                  If an error occurred while reading from 
     *                                      or writing into files.
     */
    public void extractTaxonIdsToFile(String annotFile, String outputFile) 
            throws UnsupportedEncodingException, FileNotFoundException, IOException {
        log.entry(annotFile, outputFile);
        
        Set<Integer> taxonIds = this.extractTaxonIds(annotFile);
        try(PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outputFile), "utf-8")))) {
            for (int taxonId: taxonIds) {
                writer.println(taxonId);
            }
        }
        
        log.exit();
    }
    
    /**
     * Extract from the similarity annotation file {@code annotFile} the list 
     * of all taxon IDs used. The first line of the file should be a header line, 
     * defining a column to get IDs from, named exactly "taxon ID". The IDs returned 
     * are {@code Integer}s corresponding to the NCBI ID, for instance, "9606" for human.
     * 
     * @param annotFile A {@code String} that is the path to the similarity annotation file.
     * @return          A {@code Set} of {@code Integer}s that contains all IDs 
     *                  of the taxa used in the annotation file.
     * @throws IllegalArgumentException If {@code annotFile} did not allow to obtain 
     *                                  any valid taxon ID.
     * @throws FileNotFoundException    If {@code annotFile} could not be found.
     * @throws IOException              If {@code annotFile} could not be read.
     */
    public Set<Integer> extractTaxonIds(String annotFile) 
            throws IllegalArgumentException, FileNotFoundException, IOException {
        log.entry(annotFile);
        
        Set<Integer> taxonIds = new HashSet<Integer>(new Utils().parseColumnAsInteger(
                annotFile, TAXONCOLUMNNAME, new NotNull()));
        
        if (taxonIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("The annotation file " +
                    annotFile + " did not contain any valid taxon ID"));
        }
        
        return log.exit(taxonIds);
    }
}
