package org.bgee.pipeline.expression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallParams;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTOResultSet;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTOResultSet;
import org.bgee.model.dao.mysql.expressiondata.MySQLExpressionCallDAO.MySQLExpressionCallTOResultSet;
import org.bgee.model.dao.mysql.expressiondata.MySQLNoExpressionCallDAO.MySQLNoExpressionCallTOResultSet;
import org.bgee.pipeline.TestAncestor;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration tests for {@link CallUser}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class CallUserTest extends TestAncestor {
    
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(CallUserTest.class.getName());
    
    /**
     * Default Constructor. 
     */
    public CallUserTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Reinit the {@link #mockDAOManager} before each test.
     */
    @Before
    public void initMockDAOManager() {
        mockDAOManager  = new MockDAOManager();
    }
    
    /**
     * A {@code MockDAOManager} to initialize {@link FakeCallUser}, and to be accessible 
     * from tests.
     */
    private MockDAOManager mockDAOManager;

    /**
     * Anonymous class extending {@code CallUser}, which is abstract, 
     * to test its methods with no overloading from real subclasses. 
     */
    class FakeCallUser extends CallUser {
        protected FakeCallUser() {
            super(mockDAOManager);
        }
    }
    


    /**
     * Test {@link CallUser#getExpressionCallsByGeneId(Set, ExpressionCallDAO)}.
     */
    @Test
    public void shouldGetExpressionCallsByGeneId() {
        CallUser callUser = new FakeCallUser();
        
        Map<String, Set<ExpressionCallTO>> expectedMap = 
                new HashMap<String, Set<ExpressionCallTO>>();
        Set<ExpressionCallTO> Id1ExprSet = new HashSet<ExpressionCallTO>();
        Id1ExprSet.addAll(Arrays.asList(
                new ExpressionCallTO("1", "ID1", "Anat_id4", "Stage_id6", 
                        DataState.NODATA, DataState.LOWQUALITY, 
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        false, false, ExpressionCallTO.OriginOfLine.SELF, 
                        ExpressionCallTO.OriginOfLine.SELF, true),
                        new ExpressionCallTO("2", "ID1", "Anat_id5", "Stage_id6", 
                                DataState.HIGHQUALITY, DataState.NODATA, 
                                DataState.NODATA, DataState.LOWQUALITY, 
                                false, false, ExpressionCallTO.OriginOfLine.SELF, 
                                ExpressionCallTO.OriginOfLine.SELF, true),
                                new ExpressionCallTO("3", "ID1", "Anat_id3", "Stage_id1", 
                                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                                        DataState.NODATA, DataState.LOWQUALITY, 
                                        false, false, ExpressionCallTO.OriginOfLine.SELF, 
                                        ExpressionCallTO.OriginOfLine.SELF, true)));
        expectedMap.put("ID1", Id1ExprSet);
        
        Set<ExpressionCallTO> Id2ExprSet = new HashSet<ExpressionCallTO>();
        Id2ExprSet.addAll(Arrays.asList(
                new ExpressionCallTO("4", "ID2", "Anat_id4", "Stage_id7", 
                        DataState.LOWQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, DataState.NODATA, 
                        false, false, ExpressionCallTO.OriginOfLine.SELF, 
                        ExpressionCallTO.OriginOfLine.SELF, true),
                        new ExpressionCallTO("5", "ID2", "Anat_id1", "Stage_id7", 
                                DataState.NODATA, DataState.HIGHQUALITY, 
                                DataState.LOWQUALITY, DataState.LOWQUALITY, 
                                false, false, ExpressionCallTO.OriginOfLine.SELF, 
                                ExpressionCallTO.OriginOfLine.SELF, true)));
        expectedMap.put("ID2", Id2ExprSet);
        
        List<ExpressionCallTO> allTOs = new ArrayList<ExpressionCallTO>(Id1ExprSet);
        allTOs.addAll(Id2ExprSet);
        
        ExpressionCallTOResultSet mockExprResultSet = this.createMockDAOResultSet(
                allTOs, MySQLExpressionCallTOResultSet.class);
        ExpressionCallParams params = new ExpressionCallParams();
        params.addAllSpeciesIds(Arrays.asList("11", "21"));
        when(mockDAOManager.mockExpressionCallDAO.getExpressionCalls(
                (ExpressionCallParams) TestAncestor.valueCallParamEq(params))).thenReturn(mockExprResultSet);
        
        Map<String, List<ExpressionCallTO>> returnedMap = callUser.getExpressionCallsByGeneId(
                new HashSet<String>(Arrays.asList("11", "21")));
        
        assertEquals("Maps not equal, different sizes", expectedMap.size() , returnedMap.size());
        
        for(String id : expectedMap.keySet()) {
            if(returnedMap.containsKey(id)) {
                assertTrue("Incorrect map generated: different values for " + id, 
                        TOComparator.areTOCollectionsEqual(expectedMap.get(id), returnedMap.get(id)));
            } else {
                throw new AssertionError("Incorrect map generated: missing expected id " + id);
            }
        }
    }
    
    /**
     * Test {@link BgeeDBUtils#getNoExpressionCallsByGeneId(Set, NoExpressionCallDAO)}.
     */
    @Test
    public void shouldGetNoExpressionCallsByGeneId() {
        CallUser callUser = new FakeCallUser();
        
            Map<String, Set<NoExpressionCallTO>> expectedMap = 
                    new HashMap<String, Set<NoExpressionCallTO>>();
            Set<NoExpressionCallTO> Id1NoExprSet = new HashSet<NoExpressionCallTO>();
            Id1NoExprSet.addAll(Arrays.asList(
                    new NoExpressionCallTO("2", "ID1", "Anat_id3", "Stage_id1", 
                            DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                            DataState.LOWQUALITY, false, NoExpressionCallTO.OriginOfLine.SELF),
                            new NoExpressionCallTO("3", "ID1", "Anat_id4", "Stage_id3", 
                                    DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                                    DataState.LOWQUALITY, false, NoExpressionCallTO.OriginOfLine.SELF),
                                    new NoExpressionCallTO("5", "ID1", "Anat_id5", "Stage_id3", 
                                            DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                                            DataState.LOWQUALITY, false, NoExpressionCallTO.OriginOfLine.SELF)));
            expectedMap.put("ID1", Id1NoExprSet);
            
            Set<NoExpressionCallTO> Id2NoExprSet = new HashSet<NoExpressionCallTO>();
            Id2NoExprSet.add(new NoExpressionCallTO("4", "ID2", "Anat_id4", "Stage_id3", 
                    DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                    DataState.NODATA, false, NoExpressionCallTO.OriginOfLine.SELF));
            expectedMap.put("ID2", Id2NoExprSet);
            
            Set<NoExpressionCallTO> Id3NoExprSet = new HashSet<NoExpressionCallTO>();
            Id3NoExprSet.add(new NoExpressionCallTO("1", "ID3", "Anat_id1", "Stage_id6", 
                    DataState.NODATA, DataState.LOWQUALITY, DataState.HIGHQUALITY, 
                    DataState.LOWQUALITY, false, NoExpressionCallTO.OriginOfLine.SELF));
            expectedMap.put("ID3", Id3NoExprSet);
            
            List<NoExpressionCallTO> allTOs = new ArrayList<NoExpressionCallTO>(Id1NoExprSet);
            allTOs.addAll(Id2NoExprSet);
            allTOs.addAll(Id3NoExprSet);
            
            NoExpressionCallTOResultSet mockNoExprResultSet = this.createMockDAOResultSet(
                    allTOs, MySQLNoExpressionCallTOResultSet.class);
            NoExpressionCallParams params = new NoExpressionCallParams();
            params.addAllSpeciesIds(Arrays.asList("11", "21"));
            when(mockDAOManager.mockNoExpressionCallDAO.getNoExpressionCalls(
                    (NoExpressionCallParams) TestAncestor.valueCallParamEq(params))).
                    thenReturn(mockNoExprResultSet);
            
            Map<String, List<NoExpressionCallTO>> returnedMap = callUser.getNoExpressionCallsByGeneId(
                    new HashSet<String>(Arrays.asList("11", "21")));
            
            assertEquals("Maps not equal, different sizes", expectedMap.size() , returnedMap.size());
            
            for(String id : expectedMap.keySet()) {
                if(returnedMap.containsKey(id)) {
                    assertTrue("Incorrect map generated: different values for " + id, 
                            TOComparator.areTOCollectionsEqual(expectedMap.get(id), returnedMap.get(id)));
                } else {
                    throw new AssertionError("Incorrect map generated: missing expected id " + id);
                }
            }
    }

    /**
     * Test the method {@link CallUser#groupAndOrderByGeneAnatEntityStage(Collection)}.
     */
    @Test
    public void shouldGroupAndOrderByGeneAnatEntityStage() {
        CallUser callUser = new FakeCallUser();
        
        Collection<CallTO> callsToGroup = Arrays.asList(
                (CallTO) new ExpressionCallTO(null, "gene9", "organ1", "stage1", 
                        null, null, null, null, true, null, ExpressionCallTO.OriginOfLine.DESCENT, 
                        null, null), 
                (CallTO) new ExpressionCallTO(null, "gene2", "organ2", "stage1", 
                        null, null, null, null, null, null, null, null, null), 
                (CallTO) new NoExpressionCallTO(null, "gene1", "organ2", "stage2", 
                        null, null, null, null, true, NoExpressionCallTO.OriginOfLine.BOTH), 
                (CallTO) new ExpressionCallTO(null, "gene9", "organ2", "stage2", 
                       null, null, null, null, null, null, null, null, null),
                (CallTO) new NoExpressionCallTO(null, "gene3", "organ1", "stage1", 
                       null, null, null, null, true, NoExpressionCallTO.OriginOfLine.SELF), 
                (CallTO) new ExpressionCallTO(null, "gene2", "organ2", "stage1", 
                      null, null, null, null, null, null, null, null, null), 
                (CallTO) new ExpressionCallTO(null, "gene3", "organ1", "stage1", 
                      null, null, null, null, true, null, ExpressionCallTO.OriginOfLine.BOTH, 
                      null, null), 
                (CallTO) new ExpressionCallTO(null, "gene9", "organ2", "stage1", 
                      null, null, null, null, true, null, ExpressionCallTO.OriginOfLine.SELF, 
                      null, null),  
                (CallTO) new ExpressionCallTO(null, "gene3", "organ1", "stage1", 
                      null, null, null, null, false, null, ExpressionCallTO.OriginOfLine.SELF, 
                      null, null)
                );
        
        Map<CallTO, Collection<CallTO>> expectedMap = 
                new LinkedHashMap<CallTO, Collection<CallTO>>();
        
        expectedMap.put(new ExpressionCallTO(null, "gene1", "organ2", "stage2", 
                null, null, null, null, null, null, null, null, null), 
            Arrays.asList((CallTO) new NoExpressionCallTO(null, "gene1", "organ2", "stage2", 
                null, null, null, null, true, NoExpressionCallTO.OriginOfLine.BOTH)));

        expectedMap.put(new ExpressionCallTO(null, "gene2", "organ2", "stage1", 
                null, null, null, null, null, null, null, null, null), 
            Arrays.asList((CallTO) new ExpressionCallTO(null, "gene2", "organ2", "stage1", 
                null, null, null, null, null, null, null, null, null), 
                          (CallTO) new ExpressionCallTO(null, "gene2", "organ2", "stage1", 
                null, null, null, null, null, null, null, null, null)));

        expectedMap.put(new ExpressionCallTO(null, "gene3", "organ1", "stage1", 
                null, null, null, null, null, null, null, null, null), 
            Arrays.asList((CallTO) new ExpressionCallTO(null, "gene3", "organ1", "stage1", 
                null, null, null, null, true, null, ExpressionCallTO.OriginOfLine.BOTH, null, null), 
                          (CallTO) new ExpressionCallTO(null, "gene3", "organ1", "stage1", 
                null, null, null, null, false, null, ExpressionCallTO.OriginOfLine.SELF, null, null), 
                          (CallTO) new NoExpressionCallTO(null, "gene3", "organ1", "stage1", 
                null, null, null, null, true, NoExpressionCallTO.OriginOfLine.SELF)));
        
        expectedMap.put(new ExpressionCallTO(null, "gene9", "organ1", "stage1", 
                    null, null, null, null, null, null, null, null, null), 
                Arrays.asList((CallTO) new ExpressionCallTO(null, "gene9", "organ1", "stage1", 
                    null, null, null, null, true, null, ExpressionCallTO.OriginOfLine.DESCENT, null, null)));
        
        expectedMap.put(new ExpressionCallTO(null, "gene9", "organ2", "stage1", 
                null, null, null, null, null, null, null, null, null), 
            Arrays.asList((CallTO) new ExpressionCallTO(null, "gene9", "organ2", "stage1", 
                null, null, null, null, true, null, ExpressionCallTO.OriginOfLine.SELF, null, null)));
        
        expectedMap.put(new ExpressionCallTO(null, "gene9", "organ2", "stage2", 
                null, null, null, null, null, null, null, null, null), 
            Arrays.asList((CallTO) new ExpressionCallTO(null, "gene9", "organ2", "stage2", 
                null, null, null, null, null, null, null, null, null)));
        
        SortedMap<CallTO, Collection<CallTO>> generatedMap = 
                callUser.groupAndOrderByGeneAnatEntityStage(callsToGroup);
        //check generation and ordering of the keys.
        assertEquals("Incorrect keys generated", new ArrayList<CallTO>(expectedMap.keySet()), 
                new ArrayList<CallTO>(generatedMap.keySet()));
        //now, check each value of each Entry
        for (Entry<CallTO, Collection<CallTO>> expectedEntry: expectedMap.entrySet()) {
            if (!TOComparator.areTOCollectionsEqual(expectedEntry.getValue(), 
                    generatedMap.get(expectedEntry.getKey()))) {
                throw log.throwing(new AssertionError("Incorrect values associated to the key " + 
                        expectedEntry.getKey() + ", expected: " + expectedEntry.getValue() + 
                        ", but was: " + generatedMap.get(expectedEntry.getKey())));
            }
        }
    }
    
    /**
     * Test the method {@link CallUser#isPropagatedOnly(CallTO)}.
     */
    @Test
    public void testIsPropagatedOnly() {
        CallUser callUser = new FakeCallUser();
        
        // Test expression calls
        ExpressionCallTO exprTO = new ExpressionCallTO(  
                null, null, null, null, null, null, null, null, null, null, 
                ExpressionCallTO.OriginOfLine.BOTH, ExpressionCallTO.OriginOfLine.BOTH, true);
        assertFalse("Incorrect boolean returned by isPropagatedOnly", 
                callUser.isPropagatedOnly(exprTO));

        exprTO = new ExpressionCallTO(  
                null, null, null, null, null, null, null, null, null, null, 
                ExpressionCallTO.OriginOfLine.BOTH, ExpressionCallTO.OriginOfLine.BOTH, false);
        assertTrue("Incorrect boolean returned by isPropagatedOnly", 
                callUser.isPropagatedOnly(exprTO));
        
        exprTO = new ExpressionCallTO(  
                null, null, null, null, null, null, null, null, null, null, 
                ExpressionCallTO.OriginOfLine.BOTH, ExpressionCallTO.OriginOfLine.DESCENT, false);
        assertTrue("Incorrect boolean returned by isPropagatedOnly", 
                callUser.isPropagatedOnly(exprTO));
        
        exprTO = new ExpressionCallTO(  
                null, null, null, null, null, null, null, null, null, null, 
                ExpressionCallTO.OriginOfLine.BOTH, ExpressionCallTO.OriginOfLine.SELF, false);
        assertTrue("Incorrect boolean returned by isPropagatedOnly", 
                callUser.isPropagatedOnly(exprTO));

        exprTO = new ExpressionCallTO(  
                null, null, null, null, null, null, null, null, null, null, 
                ExpressionCallTO.OriginOfLine.BOTH, ExpressionCallTO.OriginOfLine.SELF, true);
        assertFalse("Incorrect boolean returned by isPropagatedOnly", 
                callUser.isPropagatedOnly(exprTO));
        
        exprTO = new ExpressionCallTO(  
                null, null, null, null, null, null, null, null, null, null, 
                ExpressionCallTO.OriginOfLine.DESCENT, ExpressionCallTO.OriginOfLine.SELF, false);
        assertTrue("Incorrect boolean returned by isPropagatedOnly", 
                callUser.isPropagatedOnly(exprTO));
        
        exprTO = new ExpressionCallTO(  
                null, null, null, null, null, null, null, null, null, null, 
                ExpressionCallTO.OriginOfLine.SELF, ExpressionCallTO.OriginOfLine.SELF, true);
        assertFalse("Incorrect boolean returned by isPropagatedOnly", 
                callUser.isPropagatedOnly(exprTO));
        
        exprTO = new ExpressionCallTO(  
                null, null, null, null, null, null, null, null, null, null, 
                ExpressionCallTO.OriginOfLine.DESCENT, ExpressionCallTO.OriginOfLine.DESCENT, false);
        assertTrue("Incorrect boolean returned by isPropagatedOnly", 
                callUser.isPropagatedOnly(exprTO));

        // Test no-expression calls
        NoExpressionCallTO noExprTO = new NoExpressionCallTO(  
                null, null, null, null, null,  null, null, null, null,
                NoExpressionCallTO.OriginOfLine.SELF);
        assertFalse("Incorrect boolean returned by isPropagatedOnly", 
                callUser.isPropagatedOnly(noExprTO));
        
        noExprTO = new NoExpressionCallTO(  
                null, null, null, null, null,  null, null, null, null,
                NoExpressionCallTO.OriginOfLine.BOTH);
        assertFalse("Incorrect boolean returned by isPropagatedOnly", 
                callUser.isPropagatedOnly(noExprTO));

        noExprTO = new NoExpressionCallTO(  
                null, null, null, null, null,  null, null, null, null,
                NoExpressionCallTO.OriginOfLine.PARENT);
        assertTrue("Incorrect boolean returned by isPropagatedOnly", 
                callUser.isPropagatedOnly(noExprTO));
    }
    
    /**
     * Test the method {@link CallUser#isCallWithNoData(CallTO)}.
     */
    @Test
    public void testIsCallWithNoData() {
        CallUser callUser = new FakeCallUser();
        
        // Expression call
        // Empty call: all DataStates set to null
        ExpressionCallTO exprTO = new ExpressionCallTO(  
                null, null, null, null, null, null, null, null, null, null, null, null, null);
        assertTrue("Incorrect boolean returned by isPropagatedOnly", 
                callUser.isCallWithNoData(exprTO));

        // Empty call: DataStates set to null or to NODATA
        exprTO = new ExpressionCallTO(null, null, null, null, 
                DataState.NODATA, null, null, null, null, null, null, null, null);
        assertTrue("Incorrect boolean returned by isPropagatedOnly", 
                callUser.isCallWithNoData(exprTO));

        // Call not empty without null in DataStates
        exprTO = new ExpressionCallTO(null, null, null, null, 
                DataState.NODATA, DataState.LOWQUALITY, 
                DataState.LOWQUALITY, DataState.LOWQUALITY, null, null, null, null, null);
        assertFalse("Incorrect boolean returned by isPropagatedOnly", 
                callUser.isCallWithNoData(exprTO));

        // Call not empty with some null in DataStates
        exprTO = new ExpressionCallTO(null, null, null, null, 
                DataState.HIGHQUALITY, DataState.NODATA, 
                null, DataState.HIGHQUALITY, null, null, null, null, null);
        assertFalse("Incorrect boolean returned by isPropagatedOnly", 
                callUser.isCallWithNoData(exprTO));
        
        // No-expression call
        // Empty call: all DataStates set to null
        NoExpressionCallTO noExprTO = new NoExpressionCallTO(  
                null, null, null, null, null, null, null, null, null, null);
        assertTrue("Incorrect boolean returned by isPropagatedOnly", 
                callUser.isCallWithNoData(noExprTO));

        // Empty call: DataStates set to null or to NODATA
        noExprTO = new NoExpressionCallTO(null, null, null, null, 
                null, null, null, DataState.NODATA, null, null);
        assertTrue("Incorrect boolean returned by isPropagatedOnly", 
                callUser.isCallWithNoData(noExprTO));

        // Call not empty without null in DataStates
        noExprTO = new NoExpressionCallTO(null, null, null, null, 
                DataState.NODATA, DataState.HIGHQUALITY, 
                DataState.NODATA, DataState.NODATA, null, null);
        assertFalse("Incorrect boolean returned by isPropagatedOnly", 
                callUser.isCallWithNoData(noExprTO));

        // Call not empty with some null in DataStates
        noExprTO = new NoExpressionCallTO(null, null, null, null, 
                DataState.HIGHQUALITY, DataState.NODATA, 
                null, DataState.HIGHQUALITY, null, null);
        assertFalse("Incorrect boolean returned by isPropagatedOnly", 
                callUser.isCallWithNoData(noExprTO));

    }
    
    /**
     * Test the method {@link CallUser#generateGlobalExpressionTOs(List, Map, boolean)} 
     * with the boolean argument set to {@code true}.
     */
    @Test
    public void shouldGenerateGlobalExpressionTOsToAnatomy() {
        CallUser callUser = new FakeCallUser();
        
        ExpressionCallTO callTO1 = new ExpressionCallTO("4", "geneId", "childAnatId1", "stageId",
                DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, 
                DataState.NODATA, false, false, ExpressionCallTO.OriginOfLine.SELF, 
                ExpressionCallTO.OriginOfLine.SELF, null);
        ExpressionCallTO callTO2 = new ExpressionCallTO("10", "geneId", "childAnatId2", "stageId",
                DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                DataState.NODATA, false, false, ExpressionCallTO.OriginOfLine.SELF, 
                ExpressionCallTO.OriginOfLine.SELF, null);
        ExpressionCallTO callTO3 = new ExpressionCallTO("11", "geneId", "parentAnatId", "stageId",
                DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                DataState.NODATA, false, false, ExpressionCallTO.OriginOfLine.SELF, 
                ExpressionCallTO.OriginOfLine.SELF, null);
        ExpressionCallTO callTO4 = new ExpressionCallTO("12", "geneId", "otherAnatId", "stageId",
                DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                DataState.NODATA, false, false, ExpressionCallTO.OriginOfLine.SELF, 
                ExpressionCallTO.OriginOfLine.SELF, null);
        List<ExpressionCallTO> basicCalls = Arrays.asList(callTO1, callTO2, callTO3, callTO4);
        Map<String, Set<String>> anatRelations = new HashMap<String, Set<String>>();
        anatRelations.put("parentAnatId", new HashSet<String>(Arrays.asList("parentAnatId")));
        anatRelations.put("parentAnatId2", new HashSet<String>(Arrays.asList("parentAnatId2")));
        anatRelations.put("otherAnatId", new HashSet<String>(Arrays.asList("otherAnatId")));
        anatRelations.put("whatever", new HashSet<String>(Arrays.asList("whatever")));
        anatRelations.put("childAnatId1", new HashSet<String>(
                Arrays.asList("childAnatId1", "parentAnatId")));
        anatRelations.put("childAnatId2", new HashSet<String>(
                Arrays.asList("childAnatId2", "parentAnatId", "parentAnatId2")));
        
        Map<ExpressionCallTO, Set<ExpressionCallTO>> expectedMap = 
                new HashMap<ExpressionCallTO, Set<ExpressionCallTO>>();
        ExpressionCallTO propagatedTO1 = new ExpressionCallTO(
                null, "geneId", "parentAnatId", "stageId", 
                DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.NODATA,
                false, false, ExpressionCallTO.OriginOfLine.SELF, 
                ExpressionCallTO.OriginOfLine.SELF, null);
        ExpressionCallTO propagatedTO2 = new ExpressionCallTO(
                null, "geneId", "parentAnatId2", "stageId", 
                DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.NODATA,
                false, false, ExpressionCallTO.OriginOfLine.SELF, 
                ExpressionCallTO.OriginOfLine.SELF, null);
        ExpressionCallTO propagatedTO3 = new ExpressionCallTO(
                null, "geneId", "otherAnatId", "stageId", 
                DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.NODATA,
                false, false, ExpressionCallTO.OriginOfLine.SELF, 
                ExpressionCallTO.OriginOfLine.SELF, null);
        ExpressionCallTO propagatedTO4 = new ExpressionCallTO(
                null, "geneId", "childAnatId1", "stageId", 
                DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.NODATA,
                false, false, ExpressionCallTO.OriginOfLine.SELF, 
                ExpressionCallTO.OriginOfLine.SELF, null);
        ExpressionCallTO propagatedTO5 = new ExpressionCallTO(
                null, "geneId", "childAnatId2", "stageId", 
                DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.NODATA,
                false, false, ExpressionCallTO.OriginOfLine.SELF, 
                ExpressionCallTO.OriginOfLine.SELF, null);
        //propagated call from children and from self
        expectedMap.put(propagatedTO1, new HashSet<ExpressionCallTO>(Arrays.asList(
                callTO1, callTO2, callTO3)));
        //propagated call from child only
        expectedMap.put(propagatedTO2, new HashSet<ExpressionCallTO>(Arrays.asList(
                callTO2)));
        //propagated calls from self only
        expectedMap.put(propagatedTO3, new HashSet<ExpressionCallTO>(Arrays.asList(
                callTO4)));
        expectedMap.put(propagatedTO4, new HashSet<ExpressionCallTO>(Arrays.asList(
                callTO1)));
        expectedMap.put(propagatedTO5, new HashSet<ExpressionCallTO>(Arrays.asList(
                callTO2)));
        Map<ExpressionCallTO, Set<ExpressionCallTO>> generatedMap = 
                callUser.groupExpressionCallTOsByPropagatedCalls(basicCalls, anatRelations, true);
        assertTrue("Incorrect generated calls, expected: " + expectedMap.keySet() + 
                ", but was: " + generatedMap.keySet(), 
                TOComparator.areTOCollectionsEqual(expectedMap.keySet(), generatedMap.keySet()));
        //we check the whole Map with the regular equals method, this is enough to check 
        //values
        assertEquals("Incorrect generated Map", expectedMap, generatedMap);
    }
    
    /**
     * Test the method {@link CallUser#generateGlobalExpressionTOs(List, Map, boolean)} 
     * with the boolean argument set to {@code false}.
     */
    @Test
    public void shouldGenerateGlobalExpressionTOsToStages() {
        CallUser callUser = new FakeCallUser();
        
        ExpressionCallTO callTO1 = new ExpressionCallTO("4", "geneId", "anatEntityId", "childStageId1",
                DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, 
                DataState.NODATA, false, false, ExpressionCallTO.OriginOfLine.SELF, 
                ExpressionCallTO.OriginOfLine.SELF, null);
        ExpressionCallTO callTO2 = new ExpressionCallTO("10", "geneId", "anatEntityId", "childStageId2",
                DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                DataState.NODATA, false, false, ExpressionCallTO.OriginOfLine.SELF, 
                ExpressionCallTO.OriginOfLine.SELF, null);
        ExpressionCallTO callTO3 = new ExpressionCallTO("11", "geneId", "anatEntityId", "parentStageId",
                DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                DataState.NODATA, false, false, ExpressionCallTO.OriginOfLine.SELF, 
                ExpressionCallTO.OriginOfLine.SELF, null);
        ExpressionCallTO callTO4 = new ExpressionCallTO("12", "geneId", "anatEntityId", "otherStageId",
                DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                DataState.NODATA, false, false, ExpressionCallTO.OriginOfLine.SELF, 
                ExpressionCallTO.OriginOfLine.SELF, null);
        List<ExpressionCallTO> basicCalls = Arrays.asList(callTO1, callTO2, callTO3, callTO4);
        Map<String, Set<String>> anatRelations = new HashMap<String, Set<String>>();
        anatRelations.put("parentStageId", new HashSet<String>(Arrays.asList("parentStageId")));
        anatRelations.put("parentStageId2", new HashSet<String>(Arrays.asList("parentStageId2")));
        anatRelations.put("otherStageId", new HashSet<String>(Arrays.asList("otherStageId")));
        anatRelations.put("whatever", new HashSet<String>(Arrays.asList("whatever")));
        anatRelations.put("childStageId1", new HashSet<String>(
                Arrays.asList("childStageId1", "parentStageId")));
        anatRelations.put("childStageId2", new HashSet<String>(
                Arrays.asList("childStageId2", "parentStageId", "parentStageId2")));
        
        Map<ExpressionCallTO, Set<ExpressionCallTO>> expectedMap = 
                new HashMap<ExpressionCallTO, Set<ExpressionCallTO>>();
        ExpressionCallTO propagatedTO1 = new ExpressionCallTO(
                null, "geneId", "anatEntityId", "parentStageId", 
                DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.NODATA,
                false, false, ExpressionCallTO.OriginOfLine.SELF, 
                ExpressionCallTO.OriginOfLine.SELF, null);
        ExpressionCallTO propagatedTO2 = new ExpressionCallTO(
                null, "geneId", "anatEntityId", "parentStageId2", 
                DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.NODATA,
                false, false, ExpressionCallTO.OriginOfLine.SELF, 
                ExpressionCallTO.OriginOfLine.SELF, null);
        ExpressionCallTO propagatedTO3 = new ExpressionCallTO(
                null, "geneId", "anatEntityId", "otherStageId", 
                DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.NODATA,
                false, false, ExpressionCallTO.OriginOfLine.SELF, 
                ExpressionCallTO.OriginOfLine.SELF, null);
        ExpressionCallTO propagatedTO4 = new ExpressionCallTO(
                null, "geneId", "anatEntityId", "childStageId1", 
                DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.NODATA,
                false, false, ExpressionCallTO.OriginOfLine.SELF, 
                ExpressionCallTO.OriginOfLine.SELF, null);
        ExpressionCallTO propagatedTO5 = new ExpressionCallTO(
                null, "geneId", "anatEntityId", "childStageId2", 
                DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.NODATA,
                false, false, ExpressionCallTO.OriginOfLine.SELF, 
                ExpressionCallTO.OriginOfLine.SELF, null);
        //propagated call from children and from self
        expectedMap.put(propagatedTO1, new HashSet<ExpressionCallTO>(Arrays.asList(
                callTO1, callTO2, callTO3)));
        //propagated call from child only
        expectedMap.put(propagatedTO2, new HashSet<ExpressionCallTO>(Arrays.asList(
                callTO2)));
        //propagated calls from self only
        expectedMap.put(propagatedTO3, new HashSet<ExpressionCallTO>(Arrays.asList(
                callTO4)));
        expectedMap.put(propagatedTO4, new HashSet<ExpressionCallTO>(Arrays.asList(
                callTO1)));
        expectedMap.put(propagatedTO5, new HashSet<ExpressionCallTO>(Arrays.asList(
                callTO2)));
        Map<ExpressionCallTO, Set<ExpressionCallTO>> generatedMap = 
                callUser.groupExpressionCallTOsByPropagatedCalls(basicCalls, anatRelations, false);
        assertTrue("Incorrect generated calls, expected: " + expectedMap.keySet() + 
                ", but was: " + generatedMap.keySet(), 
                TOComparator.areTOCollectionsEqual(expectedMap.keySet(), generatedMap.keySet()));
        //we check the whole Map with the regular equals method, this is enough to check 
        //values
        assertEquals("Incorrect generated Map", expectedMap, generatedMap);
    }
    
    /**
     * Test the method {@link CallUser#updateGlobalExpressions(Map, boolean, boolean)}.
     */
    @Test
    public void shouldUpdateGlobalExpressions() {
        CallUser callUser = new FakeCallUser();
        
        //***************** propagating anatomy ********************
        Map<ExpressionCallTO, Set<ExpressionCallTO>> toUpate = 
                new HashMap<ExpressionCallTO, Set<ExpressionCallTO>>();
        ExpressionCallTO globalCallTO = new ExpressionCallTO(null, "geneId", 
                "parentAnatId", "stageId",
                DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.NODATA,
                false, false, ExpressionCallTO.OriginOfLine.SELF, 
                ExpressionCallTO.OriginOfLine.SELF, null);
        Set<ExpressionCallTO> basicCalls = new HashSet<ExpressionCallTO>(Arrays.asList(
                    new ExpressionCallTO("4", "geneId", "childAnatId1", "stageId",
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, 
                        DataState.NODATA, false, true, ExpressionCallTO.OriginOfLine.SELF, 
                        ExpressionCallTO.OriginOfLine.SELF, null), 
                    new ExpressionCallTO("10", "geneId", "childAnatId2", "stageId",
                            DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                            DataState.NODATA, false, true, ExpressionCallTO.OriginOfLine.SELF, 
                            ExpressionCallTO.OriginOfLine.DESCENT, null)));
        
        toUpate.put(globalCallTO, basicCalls);
        ExpressionCallTO expectedGlobalCallTO = new ExpressionCallTO("1", "geneId", 
                "parentAnatId", "stageId",
                DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA,
                true, true, ExpressionCallTO.OriginOfLine.DESCENT, 
                ExpressionCallTO.OriginOfLine.BOTH, false);
        Map<ExpressionCallTO, Set<String>> updatedMap = callUser.updateGlobalExpressions(
                toUpate, true, false);
        ExpressionCallTO upatedGlobalCallTO = updatedMap.keySet().iterator().next();
        assertEquals("Incorrect Map size", 1, updatedMap.size());
        assertTrue("Incorrect TO generated, expected: " + expectedGlobalCallTO + ", but was: " + 
                upatedGlobalCallTO, 
                TOComparator.areTOsEqual(expectedGlobalCallTO, upatedGlobalCallTO));
        assertEquals("Incorrect associated IDs", new HashSet<String>(Arrays.asList("4", "10")), 
                updatedMap.values().iterator().next());
        assertTrue("Provided Map was not emptied", toUpate.isEmpty());
        
        
        ExpressionCallTO selfTO = new ExpressionCallTO("12", "geneId", "parentAnatId", "stageId",
                DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                DataState.LOWQUALITY, false, true, ExpressionCallTO.OriginOfLine.SELF, 
                ExpressionCallTO.OriginOfLine.SELF, null);
        basicCalls.add(selfTO);
        toUpate.put(expectedGlobalCallTO, basicCalls);
        expectedGlobalCallTO = new ExpressionCallTO("1", "geneId", 
                "parentAnatId", "stageId",
                DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY,
                true, true, ExpressionCallTO.OriginOfLine.BOTH, 
                ExpressionCallTO.OriginOfLine.BOTH, true);
        updatedMap = callUser.updateGlobalExpressions(
                toUpate, true, false);
        upatedGlobalCallTO = updatedMap.keySet().iterator().next();
        assertEquals("Incorrect Map size", 1, updatedMap.size());
        assertTrue("Incorrect TO generated, expected: " + expectedGlobalCallTO + ", but was: " + 
                upatedGlobalCallTO, 
                TOComparator.areTOsEqual(expectedGlobalCallTO, upatedGlobalCallTO));
        assertEquals("Incorrect associated IDs", new HashSet<String>(Arrays.asList("4", "10", "12")), 
                updatedMap.values().iterator().next());
        assertTrue("Provided Map was not emptied", toUpate.isEmpty());
        
        
        basicCalls.clear();
        basicCalls.add(selfTO);
        toUpate.put(expectedGlobalCallTO, basicCalls);
        expectedGlobalCallTO = new ExpressionCallTO("1", "geneId", 
                "parentAnatId", "stageId",
                DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY,
                true, true, ExpressionCallTO.OriginOfLine.SELF, 
                ExpressionCallTO.OriginOfLine.SELF, true);
        updatedMap = callUser.updateGlobalExpressions(
                toUpate, true, false);
        upatedGlobalCallTO = updatedMap.keySet().iterator().next();
        assertEquals("Incorrect Map size", 1, updatedMap.size());
        assertTrue("Incorrect TO generated, expected: " + expectedGlobalCallTO + ", but was: " + 
                upatedGlobalCallTO, 
                TOComparator.areTOsEqual(expectedGlobalCallTO, upatedGlobalCallTO));
        assertEquals("Incorrect associated IDs", new HashSet<String>(Arrays.asList("12")), 
                updatedMap.values().iterator().next());
        assertTrue("Provided Map was not emptied", toUpate.isEmpty());
        

        //***************** propagating stages ********************
        globalCallTO = new ExpressionCallTO("myID", "geneId", "anatId", "parentStageId",
                DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.NODATA,
                true, false, ExpressionCallTO.OriginOfLine.SELF, 
                ExpressionCallTO.OriginOfLine.SELF, null);
        basicCalls = new HashSet<ExpressionCallTO>(Arrays.asList(
                    new ExpressionCallTO("4", "geneId", "anatId", "stageId1",
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, 
                        DataState.NODATA, true, false, ExpressionCallTO.OriginOfLine.DESCENT, 
                        ExpressionCallTO.OriginOfLine.SELF, null), 
                    new ExpressionCallTO("10", "geneId", "anatId", "stageId2",
                            DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                            DataState.NODATA, true, false, ExpressionCallTO.OriginOfLine.SELF, 
                            ExpressionCallTO.OriginOfLine.SELF, null)));
        
        toUpate.put(globalCallTO, basicCalls);
        expectedGlobalCallTO = new ExpressionCallTO("myID", "geneId", "anatId", "parentStageId",
                DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA,
                true, true, ExpressionCallTO.OriginOfLine.BOTH, 
                ExpressionCallTO.OriginOfLine.DESCENT, false);
        updatedMap = callUser.updateGlobalExpressions(
                toUpate, false, true);
        upatedGlobalCallTO = updatedMap.keySet().iterator().next();
        assertEquals("Incorrect Map size", 1, updatedMap.size());
        assertTrue("Incorrect TO generated, expected: " + expectedGlobalCallTO + ", but was: " + 
                upatedGlobalCallTO, 
                TOComparator.areTOsEqual(expectedGlobalCallTO, upatedGlobalCallTO));
        assertEquals("Incorrect associated IDs", new HashSet<String>(Arrays.asList("4", "10")), 
                updatedMap.values().iterator().next());
        assertTrue("Provided Map was not emptied", toUpate.isEmpty());
        
        
        basicCalls.add(new ExpressionCallTO("12", "geneId", "anatId", "parentStageId",
                            DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                            DataState.NODATA, true, false, ExpressionCallTO.OriginOfLine.SELF, 
                            ExpressionCallTO.OriginOfLine.SELF, null));
        toUpate.put(globalCallTO, basicCalls);
        expectedGlobalCallTO = new ExpressionCallTO("myID", "geneId", "anatId", "parentStageId",
                DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA,
                true, true, ExpressionCallTO.OriginOfLine.BOTH, 
                ExpressionCallTO.OriginOfLine.BOTH, true);
        updatedMap = callUser.updateGlobalExpressions(
                toUpate, false, true);
        upatedGlobalCallTO = updatedMap.keySet().iterator().next();
        assertEquals("Incorrect Map size", 1, updatedMap.size());
        assertTrue("Incorrect TO generated, expected: " + expectedGlobalCallTO + ", but was: " + 
                upatedGlobalCallTO, 
                TOComparator.areTOsEqual(expectedGlobalCallTO, upatedGlobalCallTO));
        assertEquals("Incorrect associated IDs", new HashSet<String>(Arrays.asList("4", "10", "12")), 
                updatedMap.values().iterator().next());
        assertTrue("Provided Map was not emptied", toUpate.isEmpty());
        
        
        basicCalls.clear();
        basicCalls.add(new ExpressionCallTO("12", "geneId", "anatId", "parentStageId",
                DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                DataState.NODATA, false, false, ExpressionCallTO.OriginOfLine.SELF, 
                ExpressionCallTO.OriginOfLine.SELF, null));
        basicCalls.add(new ExpressionCallTO("13", "geneId", "anatId", "stageId1",
                DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                DataState.NODATA, false, false, ExpressionCallTO.OriginOfLine.SELF, 
                ExpressionCallTO.OriginOfLine.SELF, null));
        toUpate.put(globalCallTO, basicCalls);
        expectedGlobalCallTO = new ExpressionCallTO("myID", "geneId", "anatId", "parentStageId",
                DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA,
                false, true, ExpressionCallTO.OriginOfLine.SELF, 
                ExpressionCallTO.OriginOfLine.BOTH, true);
        updatedMap = callUser.updateGlobalExpressions(
                toUpate, false, true);
        upatedGlobalCallTO = updatedMap.keySet().iterator().next();
        assertEquals("Incorrect Map size", 1, updatedMap.size());
        assertTrue("Incorrect TO generated, expected: " + expectedGlobalCallTO + ", but was: " + 
                upatedGlobalCallTO, 
                TOComparator.areTOsEqual(expectedGlobalCallTO, upatedGlobalCallTO));
        assertEquals("Incorrect associated IDs", new HashSet<String>(Arrays.asList("12", "13")), 
                updatedMap.values().iterator().next());
        assertTrue("Provided Map was not emptied", toUpate.isEmpty());
        
        
        globalCallTO = new ExpressionCallTO("myID", "geneId", "anatId", "parentStageId",
                DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.NODATA,
                false, false, ExpressionCallTO.OriginOfLine.SELF, 
                ExpressionCallTO.OriginOfLine.SELF, null);
        
        basicCalls.clear();
        basicCalls.add(new ExpressionCallTO("12", "geneId", "anatId", "parentStageId",
                DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                DataState.NODATA, true, false, ExpressionCallTO.OriginOfLine.DESCENT, 
                ExpressionCallTO.OriginOfLine.SELF, null));
        toUpate.put(globalCallTO, basicCalls);
        expectedGlobalCallTO = new ExpressionCallTO("myID", "geneId", "anatId", "parentStageId",
                DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA,
                true, true, ExpressionCallTO.OriginOfLine.DESCENT, 
                ExpressionCallTO.OriginOfLine.SELF, false);
        updatedMap = callUser.updateGlobalExpressions(
                toUpate, false, true);
        upatedGlobalCallTO = updatedMap.keySet().iterator().next();
        assertEquals("Incorrect Map size", 1, updatedMap.size());
        assertTrue("Incorrect TO generated, expected: " + expectedGlobalCallTO + ", but was: " + 
                upatedGlobalCallTO, 
                TOComparator.areTOsEqual(expectedGlobalCallTO, upatedGlobalCallTO));
        assertEquals("Incorrect associated IDs", new HashSet<String>(Arrays.asList("12")), 
                updatedMap.values().iterator().next());
        
        
        globalCallTO = new ExpressionCallTO("myID", "geneId", "anatId", "parentStageId",
                DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.NODATA,
                true, false, ExpressionCallTO.OriginOfLine.BOTH, 
                ExpressionCallTO.OriginOfLine.SELF, null);
        
        basicCalls = new HashSet<ExpressionCallTO>(Arrays.asList(
                new ExpressionCallTO("4", "geneId", "anatId", "stageId1",
                    DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, 
                    DataState.NODATA, true, false, ExpressionCallTO.OriginOfLine.BOTH, 
                    ExpressionCallTO.OriginOfLine.SELF, null), 
                new ExpressionCallTO("10", "geneId", "anatId", "stageId2",
                        DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                        DataState.NODATA, true, false, ExpressionCallTO.OriginOfLine.BOTH, 
                        ExpressionCallTO.OriginOfLine.SELF, null)));
        basicCalls.add(new ExpressionCallTO("12", "geneId", "anatId", "parentStageId",
                DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                DataState.NODATA, true, false, ExpressionCallTO.OriginOfLine.BOTH, 
                ExpressionCallTO.OriginOfLine.SELF, null));
        toUpate.put(globalCallTO, basicCalls);
        expectedGlobalCallTO = new ExpressionCallTO("myID", "geneId", "anatId", "parentStageId",
                DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA,
                true, true, ExpressionCallTO.OriginOfLine.BOTH, 
                ExpressionCallTO.OriginOfLine.BOTH, true);
        updatedMap = callUser.updateGlobalExpressions(
                toUpate, false, true);
        upatedGlobalCallTO = updatedMap.keySet().iterator().next();
        assertEquals("Incorrect Map size", 1, updatedMap.size());
        assertTrue("Incorrect TO generated, expected: " + expectedGlobalCallTO + ", but was: " + 
                upatedGlobalCallTO, 
                TOComparator.areTOsEqual(expectedGlobalCallTO, upatedGlobalCallTO));
        assertEquals("Incorrect associated IDs", new HashSet<String>(Arrays.asList("4", "10", "12")), 
                updatedMap.values().iterator().next());
        assertTrue("Provided Map was not emptied", toUpate.isEmpty());
        
        

        //***************** propagating stages  and anatomy ********************
        globalCallTO = new ExpressionCallTO("myID", "geneId", "parentAnatId", "parentStageId",
                DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.NODATA,
                false, false, null, null, null);
        
        basicCalls = new HashSet<ExpressionCallTO>(Arrays.asList(
                new ExpressionCallTO("4", "geneId", "parentAnatId", "stageId1",
                    DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, 
                    DataState.NODATA, false, false, ExpressionCallTO.OriginOfLine.SELF, 
                    ExpressionCallTO.OriginOfLine.SELF, null), 
                new ExpressionCallTO("10", "geneId", "anatId1", "parentStageId",
                        DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                        DataState.NODATA, false, false, ExpressionCallTO.OriginOfLine.SELF, 
                        ExpressionCallTO.OriginOfLine.SELF, null)));
        toUpate.put(globalCallTO, basicCalls);
        expectedGlobalCallTO = new ExpressionCallTO("myID", "geneId", "parentAnatId", "parentStageId",
                DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA,
                true, true, ExpressionCallTO.OriginOfLine.BOTH, 
                ExpressionCallTO.OriginOfLine.BOTH, false);
        updatedMap = callUser.updateGlobalExpressions(
                toUpate, true, true);
        upatedGlobalCallTO = updatedMap.keySet().iterator().next();
        assertEquals("Incorrect Map size", 1, updatedMap.size());
        assertTrue("Incorrect TO generated, expected: " + expectedGlobalCallTO + ", but was: " + 
                upatedGlobalCallTO, 
                TOComparator.areTOsEqual(expectedGlobalCallTO, upatedGlobalCallTO));
        assertEquals("Incorrect associated IDs", new HashSet<String>(Arrays.asList("4", "10")), 
                updatedMap.values().iterator().next());
        assertTrue("Provided Map was not emptied", toUpate.isEmpty());
    }
    
    /**
     * Test the method {@link CallUser#getBestDataState(DataState, DataState)}.
     */
    @Test
    public void shouldGetBestDataState() {
        CallUser callUser = new FakeCallUser();

        assertEquals("Incorrect DataState", DataState.HIGHQUALITY, 
                callUser.getBestDataState(DataState.HIGHQUALITY, DataState.HIGHQUALITY));
        assertEquals("Incorrect DataState", DataState.HIGHQUALITY, 
                callUser.getBestDataState(DataState.HIGHQUALITY, DataState.LOWQUALITY));
        assertEquals("Incorrect DataState", DataState.HIGHQUALITY, 
                callUser.getBestDataState(DataState.HIGHQUALITY, DataState.NODATA));

        assertEquals("Incorrect DataState", DataState.HIGHQUALITY, 
                callUser.getBestDataState(DataState.LOWQUALITY, DataState.HIGHQUALITY));
        assertEquals("Incorrect DataState", DataState.LOWQUALITY, 
                callUser.getBestDataState(DataState.LOWQUALITY, DataState.LOWQUALITY));
        assertEquals("Incorrect DataState", DataState.LOWQUALITY, 
                callUser.getBestDataState(DataState.LOWQUALITY, DataState.NODATA));

        assertEquals("Incorrect DataState", DataState.HIGHQUALITY, 
                callUser.getBestDataState(DataState.NODATA, DataState.HIGHQUALITY));
        assertEquals("Incorrect DataState", DataState.LOWQUALITY, 
                callUser.getBestDataState(DataState.NODATA, DataState.LOWQUALITY));
        assertEquals("Incorrect DataState", DataState.NODATA, 
                callUser.getBestDataState(DataState.NODATA, DataState.NODATA));
    }
}
