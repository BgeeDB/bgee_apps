package org.bgee.pipeline;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.comment.CommentStartsWith;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
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
     * An unmodifiable {@code List} of {@code String}s that are the potential names 
     * of columns containing taxon IDs, in the files containing the taxa or species used in Bgee.
     * We allow multiple values because maybe this is inconsistent in the various 
     * annotation files. These values are ordered by order of preference of use.
     */
    public static final List<String> TAXON_COL_NAMES = Collections.unmodifiableList(
            Arrays.asList("taxon ID", "species ID", "taxonID", "speciesID"));
    
    /**
     * A {@code CsvPreference} used to parse TSV files allowing commented line, 
     * starting with "//".
     */
    public final static CsvPreference TSVCOMMENTED = 
            new CsvPreference.Builder(CsvPreference.TAB_PREFERENCE).
            skipComments(new CommentStartsWith("//")).build();
    /**
     * A {@code String} corresponding to {@code System.getProperty("line.separator")}.
     * CR stands for Carriage Return.
     */
    public final static String CR = System.getProperty("line.separator");

    
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
        
        CellProcessor processor = new NotNull();
        Set<Integer> taxonIds = new HashSet<Integer>(this.parseColumnAsInteger(taxonFile, 
                TAXON_COL_NAMES, processor));
        
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
        return log.exit(this.parseColumn(tsvFile, columnName, columnProcessor, 
                String.class));
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
        
        return log.exit(this.parseColumn(tsvFile, columnName, columnProcessor, 
                Integer.class));
    }
    
    /**
     * Parse {@code tsvFile} and retrieve the values in the column named  
     * {@code columnName} (case insensitive), with the class type{@code cls}. 
     * As of Bgee 13, only {@code Integer.class} and {@code String.class} are allowed.
     * To define how the column should be processed, a {@code CellProcessor} 
     * is provided. {@code null} values are discarded (unless the {@code CellProcessor} 
     * provided included {@code NotNull}, in which case an exception would be thrown). 
     * It is not necessary to provide the {@code ParseInt} {@code CellProcessor} 
     * if {@code cls} is {@code Integer.class}.
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
     * @param cls               A {@code Class<T>} that in which class type we want 
     *                          to retrieve the results.
     * @return  A {@code List} of {@code T}s that the value in the column specified, 
     *          in the order they were read from {@code tsvFiles}.
     * @throws IllegalArgumentException If {@code columnName} was not found 
     *                                  in the headers of {@code tsvFile}.
     * @throws FileNotFoundException    If {@code tsvFile} could not be found.
     * @throws IOException              If {@code tsvFile} could not be read.
     */
    private <T> List<T> parseColumn(String tsvFile, String columnName, 
            CellProcessor columnProcessor, Class<T> cls) throws FileNotFoundException, 
            IOException {
        log.entry(tsvFile, columnName, columnProcessor, cls);
        
        try (ICsvListReader listReader = new CsvListReader(
                new FileReader(tsvFile), TSVCOMMENTED)) {
            //find the index of the column with name columnName
            String[] headers = listReader.getHeader(true);
            int columnIndex = -1;
            for (int i = 0; i < headers.length; i++) {
                if (headers[i] != null && headers[i].equalsIgnoreCase(columnName)) {
                    columnIndex = i;
                    break;
                } 
            }
            if (columnIndex == -1) {
                throw log.throwing(new IllegalArgumentException(columnName + 
                        " could not be found in the headers: " + Arrays.toString(headers)));
            }
            
            List<T> values = new ArrayList<T>();
            while((listReader.read()) != null) {
                if (columnIndex >= listReader.length()) {
                    //here we should check if columnProcessor includes a NotNull 
                    //CellProcessor, but it is too complicated
                    continue;
                } 
                
                CellProcessor[] processors = new CellProcessor[listReader.length()];
                for (int i = 0; i < listReader.length(); i++) {
                    if (i == columnIndex) {
                        processors[i] = columnProcessor;
                    } else {
                        processors[i] = new Optional();
                    }
                }
                Object value = listReader.executeProcessors(processors).get(columnIndex);
                if (value == null) {
                    continue;
                } else if (value instanceof String) {
                    if (Integer.class.isAssignableFrom(cls)) {
                        value = Integer.parseInt((String) value);
                    } 
                } else if (!cls.isInstance(value)) {
                    throw log.throwing(new IllegalArgumentException("Value " + value + 
                            " from class " + value.getClass() + " is not castable " +
                            "to neither String nor Integer.")); 
                } 
                values.add(cls.cast(value));
            }
            return log.exit(values);
        }
    }
    
    /**
     * Checks whether {@code toTest} contains any white space characters, as defined 
     * by the method {@code java.lang.Character.isWhitespace}.
     * 
     * @param toTest    The {@code String} to search for white space.
     * @return          {@code true} if {@code toTest} contained any white space 
     *                  characters, {@code false} otherwise.
     */
    public boolean containsWhiteSpace(String toTest) {
        log.entry(toTest);
        if(toTest != null){
            for(int i = 0; i < toTest.length(); i++){
                if(Character.isWhitespace(toTest.charAt(i))){
                    return log.exit(true);
                }
            }
        }
        return log.exit(false);
    }
    

    
    /**
     * Determine, among {@code header}, the index of the element having an equal value 
     * in {@code allowedColumnNames}. If several elements in {@code header} have 
     * an equal value in {@code allowedColumnNames}, then the element whose matching value 
     * has the lowest index in {@code allowedColumnNames} is considered. Index returned 
     * starts from 0. If no element could be found in {@code header} with a matching value 
     * in {@code allowedColumnNames}, then -1 is returned. 
     * <p>
     * The aim is to localize, in the header of a TSV file, the column with the preferred 
     * name, when several column names are allowed to describe a same type of data, 
     * and a header includes several of these allowed column names. For instance, 
     * in some annotation files, the column name "anatEntityId" is sometimes used 
     * to refer to Uberon anatomical entities. But, in other files, "anatEntityId" 
     * represents IDs from anatomical ontologies used before we moved to Uberon, 
     * and they also include a column named "UberonId", referring to Uberon anatomical 
     * entities. In that case, the allowed names to refer to Uberon entities will be, 
     * in order of preference, "UberonId", then "anatEntityId". This will allow 
     * to correctly localize the column "UberonId" in files also including a column 
     * "anatEntityId", and to correctly localize the column "anatEntityId" in files 
     * where it is the only valid column. 
     * 
     * @param header                An {@code Array} of {@code String}s representing 
     *                              the tokenized header of a TSV file. This is how 
     *                              headers are returned by the {@code Super CSV} library 
     *                              that we use to read/write TSV files.
     * @param allowedColumnNames    A {@code List} of {@code String}s representing 
     *                              the potential column names allowed for the data 
     *                              we are looking for, in order of preference.  
     * @return                      an {@code int} that is the index of the preferred 
     *                              matching element in {@code header} (index starts from 0). 
     *                              If no elements in {@code header} match an element 
     *                              in {@code allowedColumnNames}, then -1 is returned. 
     */
    public static int localizeColumn(String[] header, List<String> allowedColumnNames) {
        log.entry(header, allowedColumnNames);
        
        int columnIndex = -1;
        //iterate potential column names in order of preference
        columnLoop: for (String columnName: allowedColumnNames) {
            for (int i = 0; i < header.length; i++) {
                if (columnName.equals(header[i])) {
                    columnIndex = i;
                    break columnLoop;
                } 
            }
        }
         
         return log.exit(columnIndex);
    }
}
