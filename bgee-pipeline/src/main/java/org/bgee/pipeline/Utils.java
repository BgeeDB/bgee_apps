package org.bgee.pipeline;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.comment.CommentStartsWith;
import org.supercsv.exception.SuperCsvCellProcessorException;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CsvContext;

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
     * An unmodifiable {@code List} of {@code String}s that are the allowed separators 
     * between values in cells potentially containing multiple values, 
     * in preferred order of use. 
     * @see ParseMultipleStringValues
     * @see #multipleValuesToString(List)
     */
    public final static List<String> VALUE_SEPARATORS = 
            Collections.unmodifiableList(Arrays.asList("|", ","));

    /**
     * A {@code CellProcessorAdaptor} converting a {@code List} of {@code String}s 
     * into a {@code String} where elements in the {@code List} are separated 
     * by a separator. The separator used is the first element in 
     * {@link SimilarityAnnotationUtils#VALUE_SEPARATOR}. If you want to convert 
     * a separated-values {@code String} into a {@code List} of {@code String}s, 
     * see {@link SimilarityAnnotationUtils.ParseMultipleStringValues}.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Apr. 2015
     * @since Bgee 13
     */
    public static class FmtMultipleStringValues extends CellProcessorAdaptor {
        
        /**
         * Default constructor, no other {@code CellProcessor} in the chain.
         */
        public FmtMultipleStringValues() {
            super();
        }
        /**
         * Constructor allowing other processors to be chained 
         * after {@code FmtMultipleValuesCell}.
         * @param next  A {@code CellProcessor} that is the next to be called. 
         */
        public FmtMultipleStringValues(CellProcessor next) {
            super(next);
        }
        
        @Override
        public Object execute(Object value, CsvContext context) 
                throws SuperCsvCellProcessorException {
            log.entry(value, context); 
            
            //throws an Exception if the input is null, as all CellProcessors usually do.
            validateInputNotNull(value, context); 
            
            if (!(value instanceof List)) {
                throw log.throwing(new SuperCsvCellProcessorException(
                        "A List of Strings must be provided, incorrect value: " 
                        + value + " of type " + value.getClass().getSimpleName(), 
                        context, this));
            }
            List<?> valueList = (List<?>) value;
            if (valueList.isEmpty()) {
                throw log.throwing(new SuperCsvCellProcessorException(
                        "The provided List cannot be empty", 
                        context, this));
            }
            
            String multipleValuesString = "";
            for (Object valueElement: valueList) {
                if (!(valueElement instanceof String)) {
                    throw log.throwing(new SuperCsvCellProcessorException(
                            "A List of Strings must be provided, incorrect value element: " 
                            + valueElement + " of type " + valueElement.getClass().getSimpleName(), 
                            context, this));
                }
                if (StringUtils.isBlank((String) valueElement)) {
                    throw log.throwing(new SuperCsvCellProcessorException(
                            "The provided List cannot contain blank values", 
                            context, this));
                }
                
                if (!multipleValuesString.isEmpty()) {
                    multipleValuesString += VALUE_SEPARATORS.get(0);
                }
                multipleValuesString += valueElement;
            }
            
            //passes result to next processor in the chain
            return log.exit(next.execute(multipleValuesString, context));
        }
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
    public static List<String> parseColumnAsString(String tsvFile, String columnName) 
            throws IllegalArgumentException, FileNotFoundException, IOException {
        log.entry(tsvFile, columnName);
        
        return log.exit(parseColumnAsString(tsvFile, columnName, null));
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
    public static List<String> parseColumnAsString(String tsvFile, String columnName, 
            CellProcessor columnProcessor) throws IllegalArgumentException, 
            FileNotFoundException, IOException {
        log.entry(tsvFile, columnName, columnProcessor);
        return log.exit(parseColumnAsString(tsvFile, Arrays.asList(columnName), 
                columnProcessor));
    }
    
    /**
     * Parse {@code tsvFile} and retrieve the values in the column whose name is present in   
     * {@code columnNames} (case insensitive), as {@code String}s. To define how 
     * the column should be processed, a {@code CellProcessor} is provided. 
     * Comment lines starting with "//" are allowed in {@code tsvFile}. 
     * <p>
     * This method returned the values retrieved from the specified column in 
     * the order they were read from the file.
     * 
     * @param tsvFile           A {@code String} that is the path to a TSV file 
     *                          to parse.
     * @param columnNames       A {@code List} of {@code String}s that are the potential names 
     *                          of the column to parse. These {@code String}s are ordered 
     *                          by order of preference of use. The comparison to find 
     *                          the column is case-insensitive.
     * @param columnProcessor   A {@code CellProcessor} defining how to parse 
     *                          the specify column.
     * @return  A {@code List} of {@code String}s that the value in the column specified, 
     *          in the order they were read from {@code tsvFiles}.
     * @throws IllegalArgumentException If {@code columnName} was not found 
     *                                  in the headers of {@code tsvFile}.
     * @throws FileNotFoundException    If {@code tsvFile} could not be found.
     * @throws IOException              If {@code tsvFile} could not be read.
     */
    public static List<String> parseColumnAsString(String tsvFile, List<String> columnNames, 
            CellProcessor columnProcessor) throws IllegalArgumentException, 
            FileNotFoundException, IOException {
        log.entry(tsvFile, columnNames, columnProcessor);
        return log.exit(parseColumn(tsvFile, columnNames, columnProcessor, 
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
    public static List<Integer> parseColumnAsInteger(String tsvFile, String columnName) 
            throws IllegalArgumentException, FileNotFoundException, IOException {
        log.entry(tsvFile, columnName);
        
        return log.exit(parseColumnAsInteger(tsvFile, columnName, null));
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
    public static List<Integer> parseColumnAsInteger(String tsvFile, String columnName, 
            CellProcessor columnProcessor) throws IllegalArgumentException, 
            FileNotFoundException, IOException {
        log.entry(tsvFile, columnName, columnProcessor);
        
        return log.exit(parseColumnAsInteger(tsvFile, Arrays.asList(columnName), 
                columnProcessor));
    }

    /**
     * Parse {@code tsvFile} and retrieve the values in the column whose name is present  
     * in {@code columnName}s (case insensitive), as {@code Integer}s. To define how 
     * the column should be processed, a {@code CellProcessor} is provided. 
     * It is not necessary to provide the {@code ParseInt} {@code CellProcessor}.
     * Comment lines starting with "//" are allowed in {@code tsvFile}. 
     * <p>
     * This method returned the values retrieved from the specified column in 
     * the order they were read from the file.
     * 
     * @param tsvFile           A {@code String} that is the path to a TSV file 
     *                          to parse.
     * @param columnNames       A {@code List} of {@code String}s that are the potential names 
     *                          of the column to parse. These {@code String}s are ordered 
     *                          by order of preference of use. The comparison to find 
     *                          the column is case-insensitive.
     * @param columnProcessor   A {@code CellProcessor} defining how to parse 
     *                          the specify column.
     * @return  A {@code List} of {@code Integer}s that the value in the column specified, 
     *          in the order they were read from {@code tsvFiles}.
     * @throws IllegalArgumentException If {@code columnName} was not found 
     *                                  in the headers of {@code tsvFile}.
     * @throws FileNotFoundException    If {@code tsvFile} could not be found.
     * @throws IOException              If {@code tsvFile} could not be read.
     */
    public static List<Integer> parseColumnAsInteger(String tsvFile, List<String> columnNames, 
            CellProcessor columnProcessor) throws IllegalArgumentException, 
            FileNotFoundException, IOException {
        log.entry(tsvFile, columnNames, columnProcessor);
        
        return log.exit(parseColumn(tsvFile, columnNames, columnProcessor, 
                Integer.class));
    }
    
    /**
     * Parse {@code tsvFile} and retrieve the values in the column whose name matches   
     * one of the {@code columnNames} (case insensitive), with the class type {@code cls}. 
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
     * @param columnNames        A {@code List} of {@code String}s that are the potential names 
     *                          of the column to parse. These {@code String}s are ordered 
     *                          by order of preference of use. The comparison to find 
     *                          the column is case-insensitive.
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
    private static <T> List<T> parseColumn(String tsvFile, List<String> columnNames, 
            CellProcessor columnProcessor, Class<T> cls) throws FileNotFoundException, 
            IOException {
        log.entry(tsvFile, columnNames, columnProcessor, cls);
        
        try (ICsvListReader listReader = new CsvListReader(
                new FileReader(tsvFile), TSVCOMMENTED)) {
            //find the index of the column with name columnName
            String[] headers = listReader.getHeader(true);
            int columnIndex = Utils.localizeColumn(headers, columnNames);
            if (columnIndex == -1) {
                throw log.throwing(new IllegalArgumentException("Any column names " +
                        columnNames + " could be found in the file " + 
                        tsvFile + " - headers: " + Arrays.toString(headers)));
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
    public static boolean containsWhiteSpace(String toTest) {
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
     * in {@code allowedColumnNames}, then -1 is returned. The comparisons are case 
     * insensitive.
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
                if (columnName.equalsIgnoreCase(header[i])) {
                    columnIndex = i;
                    break columnLoop;
                } 
            }
        }
         
         return log.exit(columnIndex);
    }
}
