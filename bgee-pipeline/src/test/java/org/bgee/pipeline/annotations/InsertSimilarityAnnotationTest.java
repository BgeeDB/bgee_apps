package org.bgee.pipeline.annotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.anatdev.mapping.RawSimilarityAnnotationDAO.RawSimilarityAnnotationTO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SimAnnotToAnatEntityTO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SummarySimilarityAnnotationTO;
import org.bgee.pipeline.TestAncestor;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.obolibrary.oboformat.parser.OBOFormatParserException;

/**
 * Tests for {@link InsertSimilarityAnnotation}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Apr. 2015
 * @since Bgee 13
 */
public class InsertSimilarityAnnotationTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(InsertSimilarityAnnotationTest.class.getName());
    
    /**
     * Default Constructor. 
     */
    public InsertSimilarityAnnotationTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test {@link InsertSimilarityAnnotation#insert(String, String)}.
     */
    //warnings raised because of the mockito argument captor, discard
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void shouldInsertSimilarityAnnotation() throws OBOFormatParserException, IOException, 
        IllegalArgumentException, ParseException {
        MockDAOManager mockManager = new MockDAOManager();
        InsertSimilarityAnnotation insert = new InsertSimilarityAnnotation(mockManager);
        
        Set<SummarySimilarityAnnotationTO> expectedSummaryTOs = 
                new HashSet<SummarySimilarityAnnotationTO>();
        Set<SimAnnotToAnatEntityTO> expectedSimAnnotToAnatEntityTOs = 
                new HashSet<SimAnnotToAnatEntityTO>();
        Set<RawSimilarityAnnotationTO> expectedRawTOs = 
                new HashSet<RawSimilarityAnnotationTO>();
        SimpleDateFormat sdf = new SimpleDateFormat(SimilarityAnnotationUtils.DATE_FORMAT);
        
        expectedSummaryTOs.add(new SummarySimilarityAnnotationTO(
                1, 2759, false, "CIO:0000003"));
        expectedSimAnnotToAnatEntityTOs.add(new SimAnnotToAnatEntityTO(1, "CL:0000000"));
        expectedRawTOs.add(new RawSimilarityAnnotationTO(1, false, "ECO:0000033", 
                "CIO:0000003", "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21")));
        
        expectedSummaryTOs.add(new SummarySimilarityAnnotationTO(
                2, 33208, false, "CIO:0000019"));
        expectedSimAnnotToAnatEntityTOs.add(new SimAnnotToAnatEntityTO(2, "CL:0000015"));
        expectedRawTOs.add(new RawSimilarityAnnotationTO(2, false, "ECO:0000205", 
                "CIO:0000004", "DOI:10.1002/bies.950161213", "ref title 2", 
                "supporting text 2", "bgee", "ANN", sdf.parse("2013-08-29")));
        expectedRawTOs.add(new RawSimilarityAnnotationTO(2, false, "ECO:0000205", 
                "CIO:0000005", "ISBN:978-0198566694", "ref title 3", 
                "supporting text 3", "bgee", "ANN", sdf.parse("2013-08-29")));
        
        expectedSummaryTOs.add(new SummarySimilarityAnnotationTO(
                3, 7742, true, "CIO:0000004"));
        expectedSimAnnotToAnatEntityTOs.add(new SimAnnotToAnatEntityTO(3, "CL:0000037"));
        expectedRawTOs.add(new RawSimilarityAnnotationTO(3, true, "ECO:0000067", 
                "CIO:0000004", "DOI:10.1146/annurev.cellbio.22.010605.093317", "ref title 4", 
                "supporting text 4", "bgee", "ANN", sdf.parse("2013-07-01")));
        
        expectedSummaryTOs.add(new SummarySimilarityAnnotationTO(
                4, 7742, true, "CIO:0000004"));
        expectedSimAnnotToAnatEntityTOs.add(new SimAnnotToAnatEntityTO(4, "CL:0000037"));
        expectedSimAnnotToAnatEntityTOs.add(new SimAnnotToAnatEntityTO(4, "UBERON:0000001"));
        expectedSimAnnotToAnatEntityTOs.add(new SimAnnotToAnatEntityTO(4, "UBERON:0000007"));
        expectedRawTOs.add(new RawSimilarityAnnotationTO(4, true, "ECO:0000067", 
                "CIO:0000004", "DOI:10.1146/annurev.cellbio.22.010605.093317", "ref title 7", 
                "supporting text 7", "bgee", "ANN", sdf.parse("2013-07-01")));
        
        expectedSummaryTOs.add(new SummarySimilarityAnnotationTO(
                5, 33213, false, "CIO:0000010"));
        expectedSimAnnotToAnatEntityTOs.add(new SimAnnotToAnatEntityTO(5, "UBERON:0001245"));
        expectedRawTOs.add(new RawSimilarityAnnotationTO(5, false, "ECO:0000355", 
                "CIO:0000004", "http://dpc.uba.uva.nl/ctz/vol73/nr01/art01", "ref title 16", 
                "supporting text 16", "bgee", "ANN", sdf.parse("2013-10-08")));
        expectedRawTOs.add(new RawSimilarityAnnotationTO(5, true, "ECO:0000033", 
                "CIO:0000004", "http://dpc.uba.uva.nl/ctz/vol73/nr01/art01", "ref title 16", 
                "supporting text 16", "bgee", "ANN", sdf.parse("2013-10-08")));
        
        expectedSummaryTOs.add(new SummarySimilarityAnnotationTO(
                6, 7776, true, "CIO:0000003"));
        expectedSimAnnotToAnatEntityTOs.add(new SimAnnotToAnatEntityTO(6, "UBERON:0010207"));
        expectedRawTOs.add(new RawSimilarityAnnotationTO(6, true, "ECO:0000034", 
                "CIO:0000003", "ISBN:978-0030223693", "ref title 8", 
                "supporting text 8", "bgee", "ANN", sdf.parse("2013-09-05")));
        
        expectedSummaryTOs.add(new SummarySimilarityAnnotationTO(
                7, 7778, false, "CIO:0000003"));
        expectedSimAnnotToAnatEntityTOs.add(new SimAnnotToAnatEntityTO(7, "UBERON:0010207"));
        expectedRawTOs.add(new RawSimilarityAnnotationTO(7, false, "ECO:0000034", 
                "CIO:0000003", "ISBN:978-0030223693", "ref title 8", 
                "supporting text 8", "bgee", "ANN", sdf.parse("2013-09-05")));
        

        insert.insert(this.getClass().getResource(
                "/similarity_annotations/test_insert_raw_annotations.tsv").getFile(), 
                this.getClass().getResource(
                "/similarity_annotations/test_insert_summary_annotations.tsv").getFile());
        
        ArgumentCaptor<Collection> summaryTOsArg = ArgumentCaptor.forClass(Collection.class);
        verify(mockManager.mockSummarySimilarityAnnotationDAO).
            insertSummarySimilarityAnnotations(summaryTOsArg.capture());
        if (!TOComparator.areTOCollectionsEqual(expectedSummaryTOs, summaryTOsArg.getValue())) {
            throw new AssertionError("Incorrect summary similarity annotations inserted, " +
                    "expected " + expectedSummaryTOs + ", but was " + summaryTOsArg.getValue());
        }

        ArgumentCaptor<Collection> annotToAnatEntityTOsArg = 
                ArgumentCaptor.forClass(Collection.class);
        verify(mockManager.mockSummarySimilarityAnnotationDAO).
            insertSimilarityAnnotationsToAnatEntityIds(annotToAnatEntityTOsArg.capture());
        if (!TOComparator.areTOCollectionsEqual(expectedSimAnnotToAnatEntityTOs, annotToAnatEntityTOsArg.getValue())){
            throw new AssertionError("Incorrect annotation ID to entitiy IDs inserted, " +
                    "expected " + expectedSimAnnotToAnatEntityTOs + ", but was " + annotToAnatEntityTOsArg.getValue());
        }
        
        ArgumentCaptor<Collection> rawTOsArg = ArgumentCaptor.forClass(Collection.class);
        verify(mockManager.mockRawSimilarityAnnotationDAO).
            insertRawSimilarityAnnotations(rawTOsArg.capture());
        if (!TOComparator.areTOCollectionsEqual(expectedRawTOs, rawTOsArg.getValue())) {
            throw new AssertionError("Incorrect raw similarity annotations inserted, " +
                    "expected " + expectedRawTOs + ", but was " + rawTOsArg.getValue());
        }
    }
    
}
