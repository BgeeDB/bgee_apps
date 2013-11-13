package org.bgee.pipeline;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

/**
 * This class allows to perform operations that are widely used during execution 
 * of the Bgee pipeline. For instance, unzipping an archive, or removing 
 * a directory.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class Utils {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(Utils.class.getName());

    
    /**
     * Get IDs of species from the TSV file named {@code speciesFile}.
     * The IDs are {@code Integer} corresponding to the NCBI taxonomy ID (e.g., 9606 
     * for human). The first line should be a header line, the first column should 
     * contain the IDs, and the second column be present only for human readability.
     * 
     * @param speciesFile   A {@code String} that is the path to the TSV file 
     *                      containing the list of species IDs.
     * @return              A {@code Set} of {Integer}s that are the NCBI IDs 
     *                      of the species present in {@code speciesFile}.
     * @throws FileNotFoundException    If {@code speciesFile} could not be found.
     * @throws IOException              If {@code speciesFile} could not be read.
     * @throws IllegalArgumentException If the file located at {@code speciesFile} 
     *                                  did not allow to obtain any valid species ID.
     */
    public static Set<Integer> getTaxonIds(String speciesFile) throws IllegalArgumentException, 
        FileNotFoundException, IOException {
        log.entry(speciesFile);
        Set<Integer> speciesIds = new HashSet<Integer>();
        
        try (ICsvMapReader mapReader = new CsvMapReader(
                new FileReader(speciesFile), CsvPreference.TAB_PREFERENCE)) {
            mapReader.getHeader(true); 
            //define our own headers, because only the first column is used
            String columnName = "speciesId";
            String[] headers = new String[] {columnName, null};
            //constrain the first column to be not-null, unique, and parse it to Integer.
            //we don't care about the second column
            final CellProcessor[] processors = new CellProcessor[] {
                    new NotNull(new UniqueHashCode(new ParseInt())), null};
            Map<String, Object> speciesMap;
            while( (speciesMap = mapReader.read(headers, processors)) != null ) {
                    speciesIds.add((Integer) speciesMap.get(columnName));
            }
        }
        
        if (speciesIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("The species file " +
                    speciesFile + " did not contain any valid species ID"));
        }
        
        return log.exit(speciesIds);
    }
}
