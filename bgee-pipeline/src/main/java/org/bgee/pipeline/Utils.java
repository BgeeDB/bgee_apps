package org.bgee.pipeline;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.comment.CommentStartsWith;
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
     * A {@code String} that is the name of the column to get taxon IDs from, 
     * in the file containing the taxa used in Bgee.
     */
    public final static String TAXONCOLUMNNAME = "taxon ID";

    
    /**
     * Get IDs of taxa from the TSV file named {@code taxonFile}.
     * The IDs are {@code Integer}s corresponding to the NCBI ID, for instance, 
     * "9606" for human. The first line should be a header line, defining a column 
     * to get IDs from, named exactly "taxon ID" (other columns are optional 
     * and will be ignored).
     * 
     * @param taxonFile     A {@code String} that is the path to the TSV file 
     *                      containing the list of taxon IDs.
     * @return              A {@code Set} of {Integer}s that are the NCBI IDs 
     *                      of the taxa present in {@code taxonFile}.
     * @throws FileNotFoundException    If {@code taxonFile} could not be found.
     * @throws IOException              If {@code taxonFile} could not be read.
     * @throws IllegalArgumentException If the file located at {@code taxonFile} 
     *                                  did not allow to obtain any valid taxon ID.
     */
    public Set<Integer> getTaxonIds(String taxonFile) throws IllegalArgumentException, 
        FileNotFoundException, IOException {
        log.entry(taxonFile);
        
        CellProcessor processor = new NotNull(new UniqueHashCode());
        Set<Integer> taxonIds = new HashSet<Integer>(this.parseColumnAsInteger(taxonFile, 
                TAXONCOLUMNNAME, processor));
        
        if (taxonIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("The taxon file " +
                    taxonFile + " did not contain any valid taxon ID"));
        }
        
        return log.exit(taxonIds);
    }
    
    /**
     * Parse {@code tsvFile} and retrieve the values in the column named  
     * {@code columnName} (case-insensitive), as {@code String}s. Comment lines 
     * starting with "//" are allowed in {@code tsvFile}. 
     * <p>
     * This method returned the values retrieved from the specified column in 
     * the order they were read from the file.
     * 
     * @param tsvFile           A {@code String} that is the path to a TSV file 
     *                          to parse.
     * @param columnName        A {@code String} that is the name of the column  
     *                          to parse. The comparison to find the column is 
     *                          case-insensitive.
     * @return  A {@code List} of {@code String}s that the value in the column specified, 
     *          in the order they were read from {@code tsvFiles}.
     * @throws IllegalArgumentException If {@code columnName} was not found 
     *                                  in the headers of {@code tsvFile}.
     * @throws FileNotFoundException    If {@code tsvFile} could not be found.
     * @throws IOException              If {@code tsvFile} could not be read.
     * @see #parseColumnAsString(String, String, CellProcessor)
     */
    public List<String> parseColumnAsString(String tsvFile, String columnName) 
            throws IllegalArgumentException, FileNotFoundException, IOException {
        log.entry(tsvFile, columnName);
        
        return log.exit(this.parseColumnAsString(tsvFile, columnName, null));
    }
    
    /**
     * Parse {@code tsvFile} and retrieve the values in the column named  
     * {@code columnName} (case insensitive), as {@code String}s. To define how 
     * the column should be processed, a {@code CellProcessor} is provided. 
     * Comment lines starting with "//" are allowed in {@code tsvFile}. 
     * <p>
     * This method returned the values retrieved from the specified column in 
     * the order they were read from the file.
     * 
     * @param tsvFile           A {@code String} that is the path to a TSV file 
     *                          to parse.
     * @param columnName        A {@code String} that is the name of the column  
     *                          to parse. The comparison to find the column is 
     *                          case-insensitive.
     * @param columnProcessor   A {@code CellProcessor} defining how to parse 
     *                          the specify column.
     * @return  A {@code List} of {@code String}s that the value in the column specified, 
     *          in the order they were read from {@code tsvFiles}.
     * @throws IllegalArgumentException If {@code columnName} was not found 
     *                                  in the headers of {@code tsvFile}.
     * @throws FileNotFoundException    If {@code tsvFile} could not be found.
     * @throws IOException              If {@code tsvFile} could not be read.
     */
    public List<String> parseColumnAsString(String tsvFile, String columnName, 
            CellProcessor columnProcessor) throws IllegalArgumentException, 
            FileNotFoundException, IOException {
        log.entry(tsvFile, columnName, columnProcessor);
        
        List<String> values = new ArrayList<String>();
        CsvPreference tsvWithComments = 
                new CsvPreference.Builder(CsvPreference.TAB_PREFERENCE).
                skipComments(new CommentStartsWith("//")).build();
        
        try (ICsvMapReader mapReader = new CsvMapReader(
                new FileReader(tsvFile), tsvWithComments)) {
            //TODO: DRY
            String[] headers = mapReader.getHeader(true); 
            String[] usedHeaders = new String[headers.length];
            CellProcessor[] processors = new CellProcessor[headers.length];
            boolean columnFound = false;
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].equalsIgnoreCase(columnName)) {
                    usedHeaders[i] = headers[i];
                    processors[i]  = columnProcessor;
                    columnFound = true;
                } else {
                    usedHeaders[i] = null;
                    processors[i]  = null;
                }
            }
            if (!columnFound) {
                throw log.throwing(new IllegalArgumentException(columnName + 
                        " could not be found in the headers: " + Arrays.toString(headers)));
            }
            Map<String, Object> rowMap;
            while( (rowMap = mapReader.read(headers, processors)) != null ) {
                values.add((String) rowMap.get(columnName));
            }
        }
        
        return log.exit(values);
    }
   
    /**
     * Parse {@code tsvFile} and retrieve the values in the column named  
     * {@code columnName} (case-insensitive), as {@code Integer}s. It is not necessary 
     * to provide the {@code ParseInt} {@code CellProcessor}.Comment lines 
     * starting with "//" are allowed in {@code tsvFile}. 
     * <p>
     * This method returned the values retrieved from the specified column in 
     * the order they were read from the file.
     * 
     * @param tsvFile           A {@code String} that is the path to a TSV file 
     *                          to parse.
     * @param columnName        A {@code String} that is the name of the column  
     *                          to parse. The comparison to find the column is 
     *                          case-insensitive.
     * @return  A {@code List} of {@code Integer}s that the value in the column specified, 
     *          in the order they were read from {@code tsvFiles}.
     * @throws IllegalArgumentException If {@code columnName} was not found 
     *                                  in the headers of {@code tsvFile}.
     * @throws FileNotFoundException    If {@code tsvFile} could not be found.
     * @throws IOException              If {@code tsvFile} could not be read.
     * @see #parseColumnAsString(String, String, CellProcessor)
     */
    public List<Integer> parseColumnAsInteger(String tsvFile, String columnName) 
            throws IllegalArgumentException, FileNotFoundException, IOException {
        log.entry(tsvFile, columnName);
        
        return log.exit(this.parseColumnAsInteger(tsvFile, columnName, null));
    }
    
    /**
     * Parse {@code tsvFile} and retrieve the values in the column named  
     * {@code columnName} (case insensitive), as {@code Integer}s. To define how 
     * the column should be processed, a {@code CellProcessor} is provided. 
     * It is not necessary to provide the {@code ParseInt} {@code CellProcessor}.
     * Comment lines starting with "//" are allowed in {@code tsvFile}. 
     * <p>
     * This method returned the values retrieved from the specified column in 
     * the order they were read from the file.
     * 
     * @param tsvFile           A {@code String} that is the path to a TSV file 
     *                          to parse.
     * @param columnName        A {@code String} that is the name of the column  
     *                          to parse. The comparison to find the column is 
     *                          case-insensitive.
     * @param columnProcessor   A {@code CellProcessor} defining how to parse 
     *                          the specify column.
     * @return  A {@code List} of {@code Integer}s that the value in the column specified, 
     *          in the order they were read from {@code tsvFiles}.
     * @throws IllegalArgumentException If {@code columnName} was not found 
     *                                  in the headers of {@code tsvFile}.
     * @throws FileNotFoundException    If {@code tsvFile} could not be found.
     * @throws IOException              If {@code tsvFile} could not be read.
     */
    public List<Integer> parseColumnAsInteger(String tsvFile, String columnName, 
            CellProcessor columnProcessor) throws IllegalArgumentException, 
            FileNotFoundException, IOException {
        log.entry(tsvFile, columnName, columnProcessor);
        
        List<Integer> values = new ArrayList<Integer>();
        CsvPreference tsvWithComments = 
                new CsvPreference.Builder(CsvPreference.TAB_PREFERENCE).
                skipComments(new CommentStartsWith("//")).build();
        
        try (ICsvMapReader mapReader = new CsvMapReader(
                new FileReader(tsvFile), tsvWithComments)) {
            //TODO: DRY
            String[] headers = mapReader.getHeader(true); 
            String[] usedHeaders = new String[headers.length];
            CellProcessor[] processors = new CellProcessor[headers.length];
            boolean columnFound = false;
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].equalsIgnoreCase(columnName)) {
                    usedHeaders[i] = headers[i];
                    processors[i]  = columnProcessor;
                    columnFound = true;
                } else {
                    usedHeaders[i] = null;
                    processors[i]  = null;
                }
            }
            if (!columnFound) {
                throw log.throwing(new IllegalArgumentException(columnName + 
                        " could not be found in the headers: " + Arrays.toString(headers)));
            }
            Map<String, Object> rowMap;
            while( (rowMap = mapReader.read(headers, processors)) != null ) {
                Object value = rowMap.get(columnName);
                Integer valueToAdd = null;
                if (value instanceof String) {
                    valueToAdd = Integer.parseInt((String) value);
                } else {
                    valueToAdd = (Integer) value;
                }
                values.add(valueToAdd);
            }
        }
        
        return log.exit(values);
    }
}
