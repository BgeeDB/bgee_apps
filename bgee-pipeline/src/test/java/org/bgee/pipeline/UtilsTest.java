package org.bgee.pipeline;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.util.CsvContext;

/**
 * Unit tests for {@link Utils}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class UtilsTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(UtilsTest.class.getName());

    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();

    /**
     * Default Constructor. 
     */
    public UtilsTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test {@link Utils#standardizeCSVFileColumnCount(File, File, CsvPreference)}.
     * @throws IOException
     */
    @Test
    public void shouldStandardizeCSVFileColumnCount() throws IOException {
        File originalFile = new File(
                this.getClass().getResource("/utils/tsvVariableColumns.tsv").getFile());
        File standardizedFile = testFolder.newFile("standard");
        Utils.standardizeCSVFileColumnCount(originalFile, standardizedFile, 
                Utils.TSVCOMMENTED);
        //now we read the standardized file and count number of columns: 
        //it should be 5 in all lines
        try (ICsvListReader listReader = new CsvListReader(new FileReader(standardizedFile), 
                Utils.TSVCOMMENTED)) {
            int rowCount = 0;
            while( (listReader.read()) != null ) {
                rowCount++;
                assertEquals("Incorrect number of columns at line " + listReader.getLineNumber(), 
                        5, listReader.length());
            }
            //a comment in the original file should have been skipped, so that we have 
            //6 lines instead of 7.
            assertEquals("Incorrect number of lines in standardized file", 6, rowCount);
        }
    }
    
    /**
     * Tests {@link Utils.parseColumnAsString(String, String, CellProcessor)}.
     */
    @Test
    public void shouldParseColumnAsString() throws IllegalArgumentException, 
        FileNotFoundException, IOException {
        CellProcessor processor = mock(CellProcessor.class);
        when(processor.execute(anyObject(), any(CsvContext.class))).thenAnswer(
            new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    Object[] args = invocation.getArguments();
                    return args[0];
                }
        });
        
        List<String> values = Utils.parseColumnAsString(
                this.getClass().getResource("/utils/tsvTestFile.tsv").getFile(), 
                "column2", processor);
        //the column to read in the second line is empty, it is in purpose 
        //for a regression test
        assertEquals("Incorrect values returned", 2, values.size());
        assertEquals("Incorrect values returned", "b1", values.get(0));
        assertEquals("Incorrect values returned", "b3", values.get(1));
        verify(processor, times(3)).execute(anyObject(), any(CsvContext.class));
        
        //an IllegalArgumentException should be thrown if no column with the provided 
        //name could be found.
        try {
            Utils.parseColumnAsString(
                    this.getClass().getResource("/utils/tsvTestFile.tsv").getFile(), 
                    "fakeColumn", processor);
            //if we reach this point, test failed
            throw new AssertionError("No IllegalArgumentException was thrown when " +
            		"providing a non-existing column name");
        } catch(IllegalArgumentException e) {
            //test passed
        }
    }
    
    /**
     * Tests {@link Utils.parseColumnAsInteger(String, String, CellProcessor)}.
     */
    @Test
    public void shouldParseColumnAsInteger() throws IllegalArgumentException, 
        FileNotFoundException, IOException {
        CellProcessor processor = mock(CellProcessor.class);
        when(processor.execute(anyObject(), any(CsvContext.class))).thenAnswer(
            new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    Object[] args = invocation.getArguments();
                    return args[0];
                }
        });
        
        List<Integer> values = Utils.parseColumnAsInteger(
                this.getClass().getResource("/utils/tsvTestFile.tsv").getFile(), 
                "column3", processor);
        assertEquals("Incorrect values returned", 3, values.size());
        assertEquals("Incorrect values returned", 3, (int) values.get(0));
        assertEquals("Incorrect values returned", 2, (int) values.get(1));
        assertEquals("Incorrect values returned", 1, (int) values.get(2));
        verify(processor, times(3)).execute(anyObject(), any(CsvContext.class));
        
        //an IllegalArgumentException should be thrown if no column with the provided 
        //name could be found.
        try {
            Utils.parseColumnAsString(
                    this.getClass().getResource("/utils/tsvTestFile.tsv").getFile(), 
                    "fakeColumn", processor);
            //if we reach this point, test failed
            throw new AssertionError("No IllegalArgumentException was thrown when " +
                    "providing a non-existing column name");
        } catch(IllegalArgumentException e) {
            //test passed
        }
        
        //we check if everything works fine even if we provide a ParseInt CellProcessor 
        //(regression test)
        Utils.parseColumnAsInteger(
                this.getClass().getResource("/utils/tsvTestFile.tsv").getFile(), 
                "column3", new ParseInt());
    }
    
    /**
     * Test the method {@link AnnotationCommon#localizeColumn(String[], List)}.
     */
    @Test
    public void shouldLocalizeColumn() {
        String[] header = new String[3];
        header[0] = "col1";
        header[1] = "col2";
        header[2] = "col3";
        assertEquals("Incorrect column index returned", 2, 
                Utils.localizeColumn(header, Arrays.asList("col3", "col1")));
        assertEquals("Incorrect column index returned", 0, 
                Utils.localizeColumn(header, Arrays.asList("col1", "col3")));
        assertEquals("Incorrect column index returned", 1, 
                Utils.localizeColumn(header, Arrays.asList("noMatch1", "col2")));
        assertEquals("Incorrect column index returned", -1, 
                Utils.localizeColumn(header, Arrays.asList("noMatch1", "noMatch2")));
    }
    
    /**
     * Test {@link Utils.FmtMultipleStringValues#execute(Object, CsvContext)}
     */
    @Test
    public void shouldExecuteFmtMultipleStringValues() {
        CsvContext mockContext = mock(CsvContext.class);  
        //to test that next processor in the chain is called
        CellProcessor next = mock(CellProcessor.class);
        
        String expectedValue = " abcd  123  " + Utils.VALUE_SEPARATORS.get(0) + " dfgd2" 
                + Utils.VALUE_SEPARATORS.get(0) + "qwe rty";
        //The next processor will simply pass the value it received.
        //Note that if the test fails, the assertEquals will not be able to display 
        //the "actual" result, because the next CellProcessor would have returned null.
        when(next.execute(expectedValue, mockContext)).thenReturn(expectedValue);
        assertEquals("Incorrect separated-value string generated.", 
                expectedValue, 
               new Utils.FmtMultipleStringValues(next).execute(
                       Arrays.asList(" abcd  123  ", " dfgd2", "qwe rty"), mockContext));
        //verification a bit useless, if the test succeeded this processor has 
        //to have returned a value
        verify(next).execute(expectedValue, mockContext);
        
        next = mock(CellProcessor.class);
        expectedValue = " abcd  123  ";
        //The next processor will simply pass the value it received.
        //Note that if the test fails, the assertEquals will not be able to display 
        //the "actual" result, because the next CellProcessor would have returned null.
        when(next.execute(expectedValue, mockContext)).thenReturn(expectedValue);
        assertEquals("Incorrect separated-value string generated.", 
                expectedValue, 
               new Utils.FmtMultipleStringValues(next).execute(Arrays.asList(" abcd  123  "), 
                       mockContext));
        //verification a bit useless, if the test succeeded this processor has 
        //to have returned a value
        verify(next).execute(expectedValue, mockContext);
        
        try {
            new Utils.FmtMultipleStringValues().execute(new ArrayList<String>(), 
                    mockContext);
            throw log.throwing(new AssertionError("An exception should have been thrown "
                    + "when using an empty List"));
        } catch (Exception e) {
            //test successful
        }
        try {
            new Utils.FmtMultipleStringValues().execute(Arrays.asList("fsdfdfs", "", "fdfds"), 
                    mockContext);
            throw log.throwing(new AssertionError("An exception should have been thrown "
                    + "when using Blank elements"));
        } catch (Exception e) {
            //test successful
        }
        try {
            new Utils.FmtMultipleStringValues().execute(Arrays.asList("fsdfdfs", null, "fdfds"), 
                    mockContext);
            throw log.throwing(new AssertionError("An exception should have been thrown "
                    + "when using Blank elements"));
        } catch (Exception e) {
            //test successful
        }

        try {
            new Utils.FmtMultipleStringValues().execute(Arrays.asList(1, 2, 3), 
                    mockContext);
            throw log.throwing(new AssertionError("An exception should have been thrown "
                    + "when using non-String elements"));
        } catch (Exception e) {
            //test successful
        }
    }
    
    /**
     * Test {@link Utils#formatMultipleValuesToString(List)}.
     */
    @Test
    public void shouldFormatMultipleValuesToString() {
        assertEquals("Incorrect separated-value string generated.", 
               " abcd  123  " + Utils.VALUE_SEPARATORS.get(0) + " dfgd2" 
                        + Utils.VALUE_SEPARATORS.get(0) + "qwe rty", 
               Utils.formatMultipleValuesToString(
                       Arrays.asList(" abcd  123  ", " dfgd2", "qwe rty")));
        assertEquals("Incorrect separated-value string generated.", 
                " abcd  123  ", 
                Utils.formatMultipleValuesToString(Arrays.asList(" abcd  123  ")));
        
        try {
            Utils.formatMultipleValuesToString(new ArrayList<String>());
            throw log.throwing(new AssertionError("An exception should have been thrown "
                    + "when using an empty List"));
        } catch (Exception e) {
            //test successful
        }
        try {
            Utils.formatMultipleValuesToString(Arrays.asList("fsdfdfs", null, "fdfds"));
            throw log.throwing(new AssertionError("An exception should have been thrown "
                    + "when using Blank elements"));
        } catch (Exception e) {
            //test successful
        }
    }
}
