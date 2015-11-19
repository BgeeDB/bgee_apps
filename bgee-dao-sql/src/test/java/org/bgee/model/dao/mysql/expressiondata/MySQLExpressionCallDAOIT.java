package org.bgee.model.dao.mysql.expressiondata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.CallDAOFilter;
import org.bgee.model.dao.api.expressiondata.DAOConditionFilter;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO.OriginOfLine;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.GlobalExpressionToExpressionTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.expressiondata.MySQLExpressionCallDAO.MySQLExpressionCallTOResultSet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


/**
 * Integration tests for {@link MySQLExpressionCallDAO}, performed on a real MySQL database. 
 * See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for 
 * important information.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.expressiondata.ExpressionCallDAO
 * @since Bgee 13
 */
public class MySQLExpressionCallDAOIT extends MySQLITAncestor {

    private final static Logger log = 
            LogManager.getLogger(MySQLExpressionCallDAOIT.class.getName());

    public MySQLExpressionCallDAOIT() {
        super();
    }
    
    @Override
    protected Logger getLogger() {
        return log;
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    /**
     * Unit test for {@link MySQLExpressionCallDAO#getExpressionCalls(Collection, 
     * Collection, String, Collection, LinkedHashMap)}, when not needing a GROUP BY clause.
     */
    @Test
    public void shouldGetExpressionCallsNoSubStages() throws SQLException {
        
        this.useSelectDB();
        MySQLExpressionCallDAO dao = new MySQLExpressionCallDAO(this.getMySQLDAOManager());

        //now, filter with conditions, data types, gene IDs, species IDs, etc
        //First, without substructures
        Collection<CallDAOFilter> filters = Arrays.asList(
                new CallDAOFilter(Arrays.asList("ID1", "ID2", "ID3"), 
                        Arrays.asList("11", "31"), 
                        Arrays.asList(new DAOConditionFilter(
                                Arrays.asList("Anat_id1", "Anat_id3"), 
                                Arrays.asList("Stage_id1")), 
                                new DAOConditionFilter(Arrays.asList("Anat_id6"), null))
                        ), 
                new CallDAOFilter(Arrays.asList("ID2"), 
                        null, 
                        Arrays.asList(new DAOConditionFilter(
                                Arrays.asList("Anat_id11", "Anat_id1"), 
                                Arrays.asList("Stage_id12", "Stage_id13", "Stage_id9", "Stage_id2")), 
                                new DAOConditionFilter(null, Arrays.asList("Stage_id18")))
                        ));
        
        Collection<ExpressionCallTO> callTOFilters = Arrays.asList(
                new ExpressionCallTO(null, null, 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY), 
                new ExpressionCallTO(DataState.HIGHQUALITY, null, null, null), 
                new ExpressionCallTO(null, null, 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        OriginOfLine.SELF, OriginOfLine.SELF, true), 
                new ExpressionCallTO(null, null, DataState.HIGHQUALITY, null), 
                new ExpressionCallTO(null, null, null, DataState.LOWQUALITY, 
                        OriginOfLine.SELF, OriginOfLine.SELF, true)
            );
        List<ExpressionCallTO> orderedExpectedExprCalls = Arrays.asList(
                //calls retrieved thanks to first filter
                new ExpressionCallTO("1", "ID3", "Anat_id1", "Stage_id1", DataState.LOWQUALITY, 
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("2", "ID1", "Anat_id6", "Stage_id6", DataState.LOWQUALITY,
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY,
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("3", "ID1", "Anat_id6", "Stage_id7", DataState.NODATA, 
                        DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY,
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                //retrieved thanks to second filter
                new ExpressionCallTO("4", "ID2", "Anat_id2", "Stage_id18", DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("6", "ID2", "Anat_id11", "Stage_id12", DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("9", "ID2", "Anat_id1", "Stage_id9", DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("10", "ID1", "Anat_id6", "Stage_id8", DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true));
        //No ordering requested, put in a Set
        Set<ExpressionCallTO> unorderedExpectedExprCalls = new HashSet<>(orderedExpectedExprCalls);
        // Compare
        MySQLExpressionCallTOResultSet rs = 
                (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(filters, callTOFilters, 
                        false, false, null, null, null, null);
        //no ordering requested, put results in a Set
        Set<ExpressionCallTO> unorderedExpressions = new HashSet<>(rs.getAllTOs());
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + unorderedExpectedExprCalls 
                + ", but was: " + unorderedExpressions, 
                TOComparator.areTOCollectionsEqual(unorderedExpectedExprCalls, unorderedExpressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());

        //TODO: test with ordering
        
        //now, without substructures
        callTOFilters = Arrays.asList(
                new ExpressionCallTO(null, null, 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        OriginOfLine.DESCENT, OriginOfLine.SELF, false), 
                new ExpressionCallTO(DataState.HIGHQUALITY, null, null, null), 
                new ExpressionCallTO(null, null, 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        OriginOfLine.SELF, OriginOfLine.SELF, true), 
                new ExpressionCallTO(null, null, DataState.HIGHQUALITY, null), 
                new ExpressionCallTO(null, null, null, DataState.LOWQUALITY, 
                        OriginOfLine.SELF, OriginOfLine.SELF, true));
        orderedExpectedExprCalls = Arrays.asList(
                //first filter
                new ExpressionCallTO("1", "ID3", "Anat_id1", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("9", "ID3", "Anat_id3", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("12", "ID3", "Anat_id6", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("2", "ID1", "Anat_id6", "Stage_id6", 
                        DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("3", "ID1", "Anat_id6", "Stage_id7", 
                        DataState.NODATA, DataState.NODATA, DataState.NODATA, 
                        DataState.LOWQUALITY, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                //second filter
                new ExpressionCallTO("4", "ID2", "Anat_id2", "Stage_id18",
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("6", "ID2", "Anat_id11", "Stage_id12",
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("16", "ID2", "Anat_id3", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("17", "ID2", "Anat_id4", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("18", "ID2", "Anat_id5", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("19", "ID2", "Anat_id9", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("20", "ID2", "Anat_id10", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("21", "ID2", "Anat_id11", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("22", "ID1", "Anat_id6", "Stage_id8", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                        DataState.NODATA, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true));
        //No ordering requested, put in a Set
        unorderedExpectedExprCalls = new HashSet<>(orderedExpectedExprCalls);
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(filters, callTOFilters, 
                        true, false, null, null, null, null);
        //no ordering requested, put results in a Set
        unorderedExpressions = new HashSet<>(rs.getAllTOs());
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + unorderedExpectedExprCalls 
                + ", but was: " + unorderedExpressions, 
                TOComparator.areTOCollectionsEqual(unorderedExpectedExprCalls, unorderedExpressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
    }
    
    /**
     * Unit test for {@link MySQLExpressionCallDAO#getExpressionCalls(Collection, 
     * Collection, String, Collection, LinkedHashMap)}, when including substages, 
     * but avoiding to request or filter on some specific parameters triggering the use 
     * of a GROUP BY clause.
     */
    @Test
    public void shouldGetExpressionCallsNoGroupBy() throws SQLException {
        
        this.useSelectDB();
        //to be sure there is no GROUP BY used, we change the gene count limit from the properties, 
        //so that a LIMIT feature would be used if a GROUP BY is needed. This will allow 
        //to detect any incorrect use of GROUP BY.
        Properties newProps = DAOManager.getDefaultProperties();
        newProps.setProperty(MySQLDAOManager.EXPR_PROPAGATION_GENE_COUNT_KEY, "1");
        try {
            MySQLDAOManager manager = this.getMySQLDAOManager(newProps);
            MySQLExpressionCallDAO dao = new MySQLExpressionCallDAO(manager);
        
            //First, retrieve everything for species 11 and 21
            CallDAOFilter filter = new CallDAOFilter(null, 
                    Arrays.asList("11", "21", "41"), // 41 = species Id that does not exist
                    null);
            // Generate manually expected result
            List<ExpressionCallTO> orderedExpectedExprCalls = Arrays.asList(
                    new ExpressionCallTO("ID1__Anat_id6__Stage_id6", "ID1", "Anat_id6", "Stage_id6", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID1__Anat_id6__Stage_id5", "ID1", "Anat_id6", "Stage_id5", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID1__Anat_id6__Stage_id1", "ID1", "Anat_id6", "Stage_id1", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID1__Anat_id6__Stage_id7", "ID1", "Anat_id6", "Stage_id7", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID1__Anat_id6__Stage_id8", "ID1", "Anat_id6", "Stage_id8", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID1__Anat_id1__Stage_id8", "ID1", "Anat_id1", "Stage_id8", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID1__Anat_id1__Stage_id7", "ID1", "Anat_id1", "Stage_id7", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID1__Anat_id1__Stage_id5", "ID1", "Anat_id1", "Stage_id5", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID1__Anat_id1__Stage_id1", "ID1", "Anat_id1", "Stage_id1", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID1__Anat_id7__Stage_id10", "ID1", "Anat_id7", "Stage_id10", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID1__Anat_id7__Stage_id1", "ID1", "Anat_id7", "Stage_id1", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID2__Anat_id1__Stage_id1", "ID2", "Anat_id1", "Stage_id1", 
                            null, null, null, null, true, true, null, null, null), 
                    new ExpressionCallTO("ID2__Anat_id1__Stage_id2", "ID2", "Anat_id1", "Stage_id2",
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID2__Anat_id11__Stage_id1", "ID2", "Anat_id11", "Stage_id1", 
                            null, null, null, null, true, true, null, null, null), 
                    new ExpressionCallTO("ID2__Anat_id11__Stage_id10", "ID2", "Anat_id11", "Stage_id10", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID2__Anat_id11__Stage_id12", "ID2", "Anat_id11", "Stage_id12",
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID2__Anat_id11__Stage_id13", "ID2", "Anat_id11", "Stage_id13", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID2__Anat_id11__Stage_id14", "ID2", "Anat_id11", "Stage_id14", 
                            null, null, null, null, true, true, null, null, null), 
                    new ExpressionCallTO("ID2__Anat_id11__Stage_id18", "ID2", "Anat_id11", "Stage_id18",
                            null, null, null, null, true, true, null, null, null), 
                    new ExpressionCallTO("ID2__Anat_id2__Stage_id1", "ID2", "Anat_id2", "Stage_id1", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID2__Anat_id2__Stage_id14", "ID2", "Anat_id2", "Stage_id14", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID2__Anat_id2__Stage_id18", "ID2", "Anat_id2", "Stage_id18", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID2__Anat_id3__Stage_id18", "ID2", "Anat_id3", "Stage_id18", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID2__Anat_id3__Stage_id14", "ID2", "Anat_id3", "Stage_id14", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID2__Anat_id3__Stage_id1", "ID2", "Anat_id3", "Stage_id1", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID2__Anat_id4__Stage_id18", "ID2", "Anat_id4", "Stage_id18", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID2__Anat_id4__Stage_id14", "ID2", "Anat_id4", "Stage_id14", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID2__Anat_id4__Stage_id1", "ID2", "Anat_id4", "Stage_id1", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID2__Anat_id5__Stage_id18", "ID2", "Anat_id5", "Stage_id18", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID2__Anat_id5__Stage_id14", "ID2", "Anat_id5", "Stage_id14", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID2__Anat_id5__Stage_id1", "ID2", "Anat_id5", "Stage_id1", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID2__Anat_id9__Stage_id18", "ID2", "Anat_id9", "Stage_id18", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID2__Anat_id9__Stage_id14", "ID2", "Anat_id9", "Stage_id14", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID2__Anat_id9__Stage_id1", "ID2", "Anat_id9", "Stage_id1", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID2__Anat_id10__Stage_id18", "ID2", "Anat_id10", "Stage_id18", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID2__Anat_id10__Stage_id14", "ID2", "Anat_id10", "Stage_id14", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID2__Anat_id10__Stage_id1", "ID2", "Anat_id10", "Stage_id1", 
                            null, null, null, null, true, true, null, null, null));
            //No ordering requested, put in a Set
            Set<ExpressionCallTO> unorderedExpectedExprCalls = new HashSet<>(orderedExpectedExprCalls);
            // Compare. 
            MySQLExpressionCallTOResultSet rs = 
                    (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(Arrays.asList(filter), 
                            null, true, true, null, null, 
                            Arrays.asList(ExpressionCallDAO.Attribute.ID, ExpressionCallDAO.Attribute.GENE_ID, 
                                    ExpressionCallDAO.Attribute.ANAT_ENTITY_ID, ExpressionCallDAO.Attribute.STAGE_ID, 
                                    ExpressionCallDAO.Attribute.INCLUDE_SUBSTRUCTURES, 
                                    ExpressionCallDAO.Attribute.INCLUDE_SUBSTAGES), 
                            null);
            //no ordering requested, put results in a Set
            Set<ExpressionCallTO> unorderedExpressions = new HashSet<>(rs.getAllTOs());
            assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + unorderedExpectedExprCalls 
                    + ", but was: " + unorderedExpressions, 
                    TOComparator.areTOCollectionsEqual(unorderedExpectedExprCalls, unorderedExpressions));
            assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
            assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
            
            //TODO: test with some ordering
            
            
            //now, filter with conditions, data types, gene IDs, species IDs, etc
            Collection<CallDAOFilter> filters = Arrays.asList(
                    new CallDAOFilter(Arrays.asList("ID1", "ID100"), //ID100 does not exist
                            Arrays.asList("11", "21", "41"), // 41 = species Id that does not exist
                            Arrays.asList(new DAOConditionFilter(
                                    Arrays.asList("Anat_id6", "Anat_id1"), 
                                    Arrays.asList("Stage_id6", "Stage_id7")), 
                                    //This filter should select no call
                                    new DAOConditionFilter(
                                            Arrays.asList("Anat_id600", "Anat_id100"), 
                                            Arrays.asList("Stage_id600", "Stage_id700")))), 
                    new CallDAOFilter(Arrays.asList("ID2"), 
                            Arrays.asList("21"), 
                            Arrays.asList(new DAOConditionFilter(Arrays.asList("Anat_id11"), null))));
            Collection<ExpressionCallTO> callTOFilters = Arrays.asList(
                    new ExpressionCallTO(null, null, DataState.HIGHQUALITY, null), 
                    new ExpressionCallTO(null, null, null, DataState.HIGHQUALITY), 
                    new ExpressionCallTO(DataState.HIGHQUALITY, null, null, null), 
                    new ExpressionCallTO(null, null, DataState.HIGHQUALITY, null)
                );
            orderedExpectedExprCalls = Arrays.asList(
                    //first CallFilter should retrieve those: 
                    new ExpressionCallTO("ID1__Anat_id6__Stage_id6", "ID1", "Anat_id6", "Stage_id6", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID1__Anat_id6__Stage_id7", "ID1", "Anat_id6", "Stage_id7", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID1__Anat_id1__Stage_id7", "ID1", "Anat_id1", "Stage_id7", 
                            null, null, null, null, true, true, null, null, null),
                    
                    //second CallFilter should retrieve those: 
                    new ExpressionCallTO("ID2__Anat_id11__Stage_id1", "ID2", "Anat_id11", "Stage_id1", 
                            null, null, null, null, true, true, null, null, null), 
                    new ExpressionCallTO("ID2__Anat_id11__Stage_id10", "ID2", "Anat_id11", "Stage_id10",  
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID2__Anat_id11__Stage_id12", "ID2", "Anat_id11", "Stage_id12", 
                            null, null, null, null, true, true, null, null, null),
                    new ExpressionCallTO("ID2__Anat_id11__Stage_id14", "ID2", "Anat_id11", "Stage_id14",  
                            null, null, null, null, true, true, null, null, null), 
                    new ExpressionCallTO("ID2__Anat_id11__Stage_id18", "ID2", "Anat_id11", "Stage_id18", 
                            null, null, null, null, true, true, null, null, null));
            //No ordering requested, put in a Set
            unorderedExpectedExprCalls = new HashSet<>(orderedExpectedExprCalls);
            // Compare. 
            rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(filters, callTOFilters, 
                    true, true, null, null, 
                    Arrays.asList(ExpressionCallDAO.Attribute.ID, ExpressionCallDAO.Attribute.GENE_ID, 
                            ExpressionCallDAO.Attribute.ANAT_ENTITY_ID, ExpressionCallDAO.Attribute.STAGE_ID, 
                            ExpressionCallDAO.Attribute.INCLUDE_SUBSTRUCTURES, 
                            ExpressionCallDAO.Attribute.INCLUDE_SUBSTAGES), 
                    null);
            //no ordering requested, put results in a Set
            unorderedExpressions = new HashSet<>(rs.getAllTOs());
            assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + unorderedExpectedExprCalls 
                    + ", but was: " + unorderedExpressions, 
                    TOComparator.areTOCollectionsEqual(unorderedExpectedExprCalls, unorderedExpressions));
            assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
            assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
            
            //TODO: test with ordering
        } finally {
            //restore default parameters
            this.getMySQLDAOManager(DAOManager.getDefaultProperties());
        }
    }
    
    /**
     * Unit test for {@link MySQLExpressionCallDAO#getExpressionCalls(Collection, 
     * Collection, String, Collection, LinkedHashMap)}, when needing a GROUP BY clause 
     * (because including substages, or requesting or filtering on some specific parameters), 
     * without using the LIMIT feature.
     */
    @Test
    public void shouldGetExpressionCallsGroupByNoLimitFeature() throws SQLException {
        
        this.useSelectDB();
        MySQLExpressionCallDAO dao = new MySQLExpressionCallDAO(this.getMySQLDAOManager());
        
        //First, retrieve everything for species 11 and 21, including substructures
        CallDAOFilter filter = new CallDAOFilter(null, 
                Arrays.asList("11", "21", "41"), // 41 = species Id that does not exist
                null);
        // Generate manually expected result
        List<ExpressionCallTO> orderedExpectedExprCalls = Arrays.asList(
                new ExpressionCallTO("ID1__Anat_id6__Stage_id6", "ID1", "Anat_id6", "Stage_id6", 
                        DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, true, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id5", "ID1", "Anat_id6", "Stage_id5", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, true, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id1", "ID1", "Anat_id6", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, true, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id7", "ID1", "Anat_id6", "Stage_id7", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, 
                        true, true, OriginOfLine.SELF, OriginOfLine.BOTH, true),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id8", "ID1", "Anat_id6", "Stage_id8", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        true, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id8", "ID1", "Anat_id1", "Stage_id8", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        true, true, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id7", "ID1", "Anat_id1", "Stage_id7", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        true, true, OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id5", "ID1", "Anat_id1", "Stage_id5", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        true, true, OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id1", "ID1", "Anat_id1", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        true, true, OriginOfLine.BOTH, OriginOfLine.BOTH, true),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id10", "ID1", "Anat_id7", "Stage_id10", 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, true, true, OriginOfLine.BOTH, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id1", "ID1", "Anat_id7", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, true, true, OriginOfLine.BOTH, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id1__Stage_id1", "ID2", "Anat_id1", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.HIGHQUALITY, true, true,
                        OriginOfLine.BOTH, OriginOfLine.BOTH, false), 
                new ExpressionCallTO("ID2__Anat_id1__Stage_id2", "ID2", "Anat_id1", "Stage_id2",
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.NODATA, true, true, 
                        OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id1", "ID2", "Anat_id11", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.BOTH, OriginOfLine.DESCENT, false), 
                new ExpressionCallTO("ID2__Anat_id11__Stage_id10", "ID2", "Anat_id11", "Stage_id10", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id12", "ID2", "Anat_id11", "Stage_id12",
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id13", "ID2", "Anat_id11", "Stage_id13", 
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.NODATA, true, true, 
                        OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id14", "ID2", "Anat_id11", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false), 
                new ExpressionCallTO("ID2__Anat_id11__Stage_id18", "ID2", "Anat_id11", "Stage_id18",
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.SELF, false), 
                new ExpressionCallTO("ID2__Anat_id2__Stage_id1", "ID2", "Anat_id2", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id2__Stage_id14", "ID2", "Anat_id2", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id2__Stage_id18", "ID2", "Anat_id2", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID2__Anat_id3__Stage_id18", "ID2", "Anat_id3", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("ID2__Anat_id3__Stage_id14", "ID2", "Anat_id3", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true,
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id3__Stage_id1", "ID2", "Anat_id3", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id4__Stage_id18", "ID2", "Anat_id4", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("ID2__Anat_id4__Stage_id14", "ID2", "Anat_id4", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id4__Stage_id1", "ID2", "Anat_id4", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true,
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id5__Stage_id18", "ID2", "Anat_id5", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("ID2__Anat_id5__Stage_id14", "ID2", "Anat_id5", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id5__Stage_id1", "ID2", "Anat_id5", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id9__Stage_id18", "ID2", "Anat_id9", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("ID2__Anat_id9__Stage_id14", "ID2", "Anat_id9", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id9__Stage_id1", "ID2", "Anat_id9", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id10__Stage_id18", "ID2", "Anat_id10", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("ID2__Anat_id10__Stage_id14", "ID2", "Anat_id10", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id10__Stage_id1", "ID2", "Anat_id10", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false));
        //No ordering requested, put in a Set
        Set<ExpressionCallTO> unorderedExpectedExprCalls = new HashSet<>(orderedExpectedExprCalls);
        // Compare. Provide a global gene ID filtering to avoid to trigger 
        // the use of the LIMIT feature
        MySQLExpressionCallTOResultSet rs = 
                (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(Arrays.asList(filter), 
                        null, true, true, Arrays.asList("ID1", "ID2", "ID100"), null, null, null);
        //no ordering requested, put results in a Set
        Set<ExpressionCallTO> unorderedExpressions = new HashSet<>(rs.getAllTOs());
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + unorderedExpectedExprCalls 
                + ", but was: " + unorderedExpressions, 
                TOComparator.areTOCollectionsEqual(unorderedExpectedExprCalls, unorderedExpressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
        
        //TODO: test with some ordering
        
        //now Request only anatEntityId and Affy data
        orderedExpectedExprCalls = Arrays.asList(
                new ExpressionCallTO(null, null, "Anat_id6", null,  
                        DataState.HIGHQUALITY, null, null, null, null, null, null, null, null),
                new ExpressionCallTO(null, null, "Anat_id1", null,  
                        DataState.HIGHQUALITY, null, null, null, null, null, null, null, null),
                new ExpressionCallTO(null, null, "Anat_id7", null,  
                        DataState.LOWQUALITY, null, null, null, null, null, null, null, null), 
                new ExpressionCallTO(null, null, "Anat_id11", null,  
                        DataState.HIGHQUALITY, null, null, null, null, null, null, null, null), 
                new ExpressionCallTO(null, null, "Anat_id2", null,  
                        DataState.HIGHQUALITY, null, null, null, null, null, null, null, null), 
                new ExpressionCallTO(null, null, "Anat_id3", null,  
                        DataState.HIGHQUALITY, null, null, null, null, null, null, null, null), 
                new ExpressionCallTO(null, null, "Anat_id4", null,  
                        DataState.HIGHQUALITY, null, null, null, null, null, null, null, null), 
                new ExpressionCallTO(null, null, "Anat_id5", null,  
                        DataState.HIGHQUALITY, null, null, null, null, null, null, null, null), 
                new ExpressionCallTO(null, null, "Anat_id9", null,  
                        DataState.HIGHQUALITY, null, null, null, null, null, null, null, null), 
                new ExpressionCallTO(null, null, "Anat_id10", null,  
                        DataState.HIGHQUALITY, null, null, null, null, null, null, null, null));
        //No ordering requested, put in a Set
        unorderedExpectedExprCalls = new HashSet<>(orderedExpectedExprCalls);
        // Compare. Provide a global gene ID filtering to avoid to trigger 
        // the use of the LIMIT feature
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(Arrays.asList(filter), 
                null, true, true, Arrays.asList("ID1", "ID2", "ID100"), null, Arrays.asList(
                        ExpressionCallDAO.Attribute.ANAT_ENTITY_ID, 
                        ExpressionCallDAO.Attribute.AFFYMETRIX_DATA), 
                null);
        //no ordering requested, put results in a Set
        unorderedExpressions = new HashSet<>(rs.getAllTOs());
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + unorderedExpectedExprCalls 
                + ", but was: " + unorderedExpressions, 
                TOComparator.areTOCollectionsEqual(unorderedExpectedExprCalls, unorderedExpressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
        
        //TODO: test with some ordering
        
        //Now, a GROUP BY needed because of the attributes requested, not because of includeSubStages.
        orderedExpectedExprCalls = Arrays.asList(
                new ExpressionCallTO(null, "ID1", "Anat_id6", null, 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                
                new ExpressionCallTO(null, "ID2", "Anat_id1", null, 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.HIGHQUALITY, true, false,
                        OriginOfLine.BOTH, OriginOfLine.SELF, true));
        
        //now, filter with conditions, data types, gene IDs, species IDs, etc
        Collection<CallDAOFilter> filters = Arrays.asList(
                new CallDAOFilter(Arrays.asList("ID1", "ID2"), //ID100 does not exist
                        Arrays.asList("11", "21", "41"), // 41 = species Id that does not exist
                        Arrays.asList(new DAOConditionFilter(
                                Arrays.asList("Anat_id1", "Anat_id6"), null))
            ));
        Collection<ExpressionCallTO> callTOFilters = Arrays.asList(
                new ExpressionCallTO(DataState.HIGHQUALITY, null, null, 
                        DataState.LOWQUALITY, OriginOfLine.SELF, 
                        OriginOfLine.BOTH, true), 
                new ExpressionCallTO(DataState.HIGHQUALITY, null, null, 
                        DataState.HIGHQUALITY, OriginOfLine.BOTH, 
                        OriginOfLine.BOTH, true)
            );
        //No ordering requested, put in a Set
        unorderedExpectedExprCalls = new HashSet<>(orderedExpectedExprCalls);
        // Compare. Provide a global gene ID filtering to avoid to trigger 
        // the use of the LIMIT feature
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(filters, callTOFilters, 
                true, false, Arrays.asList("ID1", "ID2", "ID100"), null, 
                EnumSet.allOf(ExpressionCallDAO.Attribute.class).stream()
                    .filter(attr -> !attr.equals(ExpressionCallDAO.Attribute.ID) && 
                            !attr.equals(ExpressionCallDAO.Attribute.STAGE_ID) && !attr.isRankAttribute())
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(ExpressionCallDAO.Attribute.class))), 
                null);
        //no ordering requested, put results in a Set
        unorderedExpressions = new HashSet<>(rs.getAllTOs());
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + unorderedExpectedExprCalls 
                + ", but was: " + unorderedExpressions, 
                TOComparator.areTOCollectionsEqual(unorderedExpectedExprCalls, unorderedExpressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
        
        
        filters = Arrays.asList(
                new CallDAOFilter(Arrays.asList("ID1", "ID100"), //ID100 does not exist
                          Arrays.asList("11", "21", "41"), // 41 = species Id that does not exist
                          Arrays.asList(new DAOConditionFilter(
                                 Arrays.asList("Anat_id6", "Anat_id1"), 
                                 Arrays.asList("Stage_id6", "Stage_id7")), 
                                 //This filter should select no call
                                 new DAOConditionFilter(
                                         Arrays.asList("Anat_id600", "Anat_id100"), 
                                         Arrays.asList("Stage_id600", "Stage_id700")))
                         ), 
                 new CallDAOFilter(Arrays.asList("ID2"), 
                         Arrays.asList("21"), 
                         Arrays.asList(new DAOConditionFilter(null, Arrays.asList("Stage_id1")))
                         ));
        callTOFilters = Arrays.asList(
                new ExpressionCallTO(DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        OriginOfLine.BOTH, OriginOfLine.DESCENT, false), 
                new ExpressionCallTO(DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                //This CallTO doesn't allow to retrieve any call
                new ExpressionCallTO(DataState.HIGHQUALITY, null, null, null, 
                        OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO(DataState.HIGHQUALITY, null, null, 
                        DataState.LOWQUALITY, OriginOfLine.SELF, 
                        OriginOfLine.BOTH, true), 
                new ExpressionCallTO(DataState.HIGHQUALITY, null, null, 
                       DataState.LOWQUALITY, OriginOfLine.SELF, 
                       OriginOfLine.SELF, true), 
               new ExpressionCallTO(null, null, DataState.HIGHQUALITY, null, 
                       OriginOfLine.DESCENT, OriginOfLine.SELF, false), 
               new ExpressionCallTO(null, null, DataState.HIGHQUALITY, null, 
                       OriginOfLine.SELF, OriginOfLine.SELF, true),
               //This CallTO doesn't allow to retrieve any call
               new ExpressionCallTO(null, null, null, DataState.HIGHQUALITY)
            );
        orderedExpectedExprCalls = Arrays.asList(
                //first CallFilter should retrieve those: 
                new ExpressionCallTO("ID1__Anat_id6__Stage_id6", "ID1", "Anat_id6", "Stage_id6", 
                        DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, true, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id7", "ID1", "Anat_id6", "Stage_id7", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, 
                        true, true, OriginOfLine.SELF, OriginOfLine.BOTH, true),
                
                //second CallFilter should retrieve those: 
                new ExpressionCallTO("ID2__Anat_id11__Stage_id1", "ID2", "Anat_id11", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.BOTH, OriginOfLine.DESCENT, false), 
                new ExpressionCallTO("ID2__Anat_id3__Stage_id1", "ID2", "Anat_id3", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id4__Stage_id1", "ID2", "Anat_id4", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true,
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id5__Stage_id1", "ID2", "Anat_id5", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id9__Stage_id1", "ID2", "Anat_id9", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id10__Stage_id1", "ID2", "Anat_id10", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id1__Stage_id1", "ID2", "Anat_id1", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.BOTH, OriginOfLine.BOTH, false),
                new ExpressionCallTO("ID2__Anat_id2__Stage_id1", "ID2", "Anat_id2", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.SELF, OriginOfLine.DESCENT, false));
        //No ordering requested, put in a Set
        unorderedExpectedExprCalls = new HashSet<>(orderedExpectedExprCalls);
        // Compare. Provide a global gene ID filtering to avoid to trigger 
        // the use of the LIMIT feature
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(filters, callTOFilters, 
                true, true, Arrays.asList("ID1", "ID2", "ID100"), null, null, null);
        //no ordering requested, put results in a Set
        unorderedExpressions = new HashSet<>(rs.getAllTOs());
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + unorderedExpectedExprCalls 
                + ", but was: " + unorderedExpressions, 
                TOComparator.areTOCollectionsEqual(unorderedExpectedExprCalls, unorderedExpressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
        
        //TODO: test with ordering
    }
    
    /**
     * Unit test for {@link MySQLExpressionCallDAO#getExpressionCalls(Collection, 
     * Collection, String, Collection, LinkedHashMap)}, when needing a GROUP BY clause 
     * (because including substages, or requesting or filtering on some specific parameters), 
     * and the use of the LIMIT feature.
     */
    @Test
    public void shouldGetExpressionCallsWithLimitFeature() throws SQLException {
        
        this.useSelectDB();

        //to test the LIMIT feature used when propagating expression calls on-the-fly 
        //with a GROUP BY needed, we change the gene count limit from the properties.
        Properties newProps = DAOManager.getDefaultProperties();
        newProps.setProperty(MySQLDAOManager.EXPR_PROPAGATION_GENE_COUNT_KEY, "1");
        try {
            MySQLDAOManager manager = this.getMySQLDAOManager(newProps);
            MySQLExpressionCallDAO dao = new MySQLExpressionCallDAO(manager);
            

            //First, retrieve everything for species 11 and 21
            CallDAOFilter filter = new CallDAOFilter(null, 
                    Arrays.asList("11", "21", "41"), // 41 = species Id that does not exist
                    null);
            // Generate manually expected result
            List<ExpressionCallTO> orderedExpectedExprCalls = Arrays.asList(
                new ExpressionCallTO("ID1__Anat_id6__Stage_id6", "ID1", "Anat_id6", "Stage_id6", 
                        DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, true, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id5", "ID1", "Anat_id6", "Stage_id5", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, true, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id1", "ID1", "Anat_id6", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, true, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id7", "ID1", "Anat_id6", "Stage_id7", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, 
                        true, true, OriginOfLine.SELF, OriginOfLine.BOTH, true),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id8", "ID1", "Anat_id6", "Stage_id8", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        true, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id8", "ID1", "Anat_id1", "Stage_id8", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        true, true, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id7", "ID1", "Anat_id1", "Stage_id7", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        true, true, OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id5", "ID1", "Anat_id1", "Stage_id5", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        true, true, OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id1", "ID1", "Anat_id1", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        true, true, OriginOfLine.BOTH, OriginOfLine.BOTH, true),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id10", "ID1", "Anat_id7", "Stage_id10", 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, true, true, OriginOfLine.BOTH, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id1", "ID1", "Anat_id7", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, true, true, OriginOfLine.BOTH, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id1__Stage_id1", "ID2", "Anat_id1", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.HIGHQUALITY, true, true,
                        OriginOfLine.BOTH, OriginOfLine.BOTH, false), 
                new ExpressionCallTO("ID2__Anat_id1__Stage_id2", "ID2", "Anat_id1", "Stage_id2",
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.NODATA, true, true, 
                        OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id1", "ID2", "Anat_id11", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.BOTH, OriginOfLine.DESCENT, false), 
                new ExpressionCallTO("ID2__Anat_id11__Stage_id10", "ID2", "Anat_id11", "Stage_id10", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id12", "ID2", "Anat_id11", "Stage_id12",
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id13", "ID2", "Anat_id11", "Stage_id13", 
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.NODATA, true, true, 
                        OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id14", "ID2", "Anat_id11", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false), 
                new ExpressionCallTO("ID2__Anat_id11__Stage_id18", "ID2", "Anat_id11", "Stage_id18",
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.SELF, false), 
                new ExpressionCallTO("ID2__Anat_id2__Stage_id1", "ID2", "Anat_id2", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id2__Stage_id14", "ID2", "Anat_id2", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id2__Stage_id18", "ID2", "Anat_id2", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID2__Anat_id3__Stage_id18", "ID2", "Anat_id3", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("ID2__Anat_id3__Stage_id14", "ID2", "Anat_id3", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true,
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id3__Stage_id1", "ID2", "Anat_id3", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id4__Stage_id18", "ID2", "Anat_id4", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("ID2__Anat_id4__Stage_id14", "ID2", "Anat_id4", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id4__Stage_id1", "ID2", "Anat_id4", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true,
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id5__Stage_id18", "ID2", "Anat_id5", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("ID2__Anat_id5__Stage_id14", "ID2", "Anat_id5", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id5__Stage_id1", "ID2", "Anat_id5", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id9__Stage_id18", "ID2", "Anat_id9", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("ID2__Anat_id9__Stage_id14", "ID2", "Anat_id9", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id9__Stage_id1", "ID2", "Anat_id9", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id10__Stage_id18", "ID2", "Anat_id10", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("ID2__Anat_id10__Stage_id14", "ID2", "Anat_id10", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id10__Stage_id1", "ID2", "Anat_id10", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false));
            //No ordering requested, put in a Set
            Set<ExpressionCallTO> unorderedExpectedExprCalls = new HashSet<>(orderedExpectedExprCalls);
            // Compare
            MySQLExpressionCallTOResultSet rs = 
                    (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(Arrays.asList(filter), 
                            null, true, true, null, null, null, null);
            //no ordering requested, put results in a Set
            Set<ExpressionCallTO> unorderedExpressions = new HashSet<>(rs.getAllTOs());
            assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + unorderedExpectedExprCalls 
                    + ", but was: " + unorderedExpressions, 
                    TOComparator.areTOCollectionsEqual(unorderedExpectedExprCalls, unorderedExpressions));
            assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
            assertTrue("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
            
            //TODO: test with some ordering
            
            //now with some filtering of duplicates. Request only anatEntityId and Affy data
            orderedExpectedExprCalls = Arrays.asList(
                new ExpressionCallTO(null, null, "Anat_id6", null,  
                        DataState.HIGHQUALITY, null, null, null, null, null, null, null, null),
                new ExpressionCallTO(null, null, "Anat_id1", null,  
                        DataState.HIGHQUALITY, null, null, null, null, null, null, null, null),
                new ExpressionCallTO(null, null, "Anat_id7", null,  
                        DataState.LOWQUALITY, null, null, null, null, null, null, null, null), 
                new ExpressionCallTO(null, null, "Anat_id11", null,  
                        DataState.HIGHQUALITY, null, null, null, null, null, null, null, null), 
                new ExpressionCallTO(null, null, "Anat_id2", null,  
                        DataState.HIGHQUALITY, null, null, null, null, null, null, null, null), 
                new ExpressionCallTO(null, null, "Anat_id3", null,  
                        DataState.HIGHQUALITY, null, null, null, null, null, null, null, null), 
                new ExpressionCallTO(null, null, "Anat_id4", null,  
                        DataState.HIGHQUALITY, null, null, null, null, null, null, null, null), 
                new ExpressionCallTO(null, null, "Anat_id5", null,  
                        DataState.HIGHQUALITY, null, null, null, null, null, null, null, null), 
                new ExpressionCallTO(null, null, "Anat_id9", null,  
                        DataState.HIGHQUALITY, null, null, null, null, null, null, null, null), 
                new ExpressionCallTO(null, null, "Anat_id10", null,  
                        DataState.HIGHQUALITY, null, null, null, null, null, null, null, null));
            //No ordering requested, put in a Set
            unorderedExpectedExprCalls = new HashSet<>(orderedExpectedExprCalls);
            // Compare
            rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(Arrays.asList(filter), 
                            null, true, true, null, null, Arrays.asList(
                                    ExpressionCallDAO.Attribute.ANAT_ENTITY_ID, 
                                    ExpressionCallDAO.Attribute.AFFYMETRIX_DATA), 
                            null);
            //no ordering requested, put results in a Set
            unorderedExpressions = new HashSet<>(rs.getAllTOs());
            assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + unorderedExpectedExprCalls 
                    + ", but was: " + unorderedExpressions, 
                    TOComparator.areTOCollectionsEqual(unorderedExpectedExprCalls, unorderedExpressions));
            assertTrue("Incorrect filtering of duplicates", rs.isFilterDuplicates());
            assertTrue("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
            
            //TODO: test with some ordering
            
            //Now, a GROUP BY needed because of the attributes requested, not because of includeSubStages.
            orderedExpectedExprCalls = Arrays.asList(
                    new ExpressionCallTO(null, "ID1", "Anat_id6", null, 
                            DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                            DataState.LOWQUALITY, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                    
                    new ExpressionCallTO(null, "ID2", "Anat_id1", null, 
                            DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                            DataState.HIGHQUALITY, true, false,
                            OriginOfLine.BOTH, OriginOfLine.SELF, true));
            
            //now, filter with conditions, data types, gene IDs, species IDs, etc
            Collection<CallDAOFilter> filters = Arrays.asList(
                    new CallDAOFilter(Arrays.asList("ID1", "ID2", "ID:100"), //ID100 does not exist
                            Arrays.asList("11", "21", "41"), // 41 = species Id that does not exist
                            Arrays.asList(new DAOConditionFilter(
                                    Arrays.asList("Anat_id1", "Anat_id6"), null))
                ));
            Collection<ExpressionCallTO> callTOFilters = Arrays.asList(
                    new ExpressionCallTO(DataState.HIGHQUALITY, null, null, 
                            DataState.LOWQUALITY, OriginOfLine.SELF, 
                            OriginOfLine.BOTH, true), 
                    new ExpressionCallTO(DataState.HIGHQUALITY, null, null, 
                            DataState.HIGHQUALITY, OriginOfLine.BOTH, 
                            OriginOfLine.BOTH, true)
                    
                );
            //No ordering requested, put in a Set
            unorderedExpectedExprCalls = new HashSet<>(orderedExpectedExprCalls);
            // Compare. 
            rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(filters, callTOFilters, 
                    true, false, null, null, 
                    EnumSet.allOf(ExpressionCallDAO.Attribute.class).stream()
                        .filter(attr -> !attr.equals(ExpressionCallDAO.Attribute.ID) && 
                                !attr.equals(ExpressionCallDAO.Attribute.STAGE_ID))
                        .collect(Collectors.toCollection(() -> EnumSet.noneOf(ExpressionCallDAO.Attribute.class))), 
                    null);
            //no ordering requested, put results in a Set
            unorderedExpressions = new HashSet<>(rs.getAllTOs());
            assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + unorderedExpectedExprCalls 
                    + ", but was: " + unorderedExpressions, 
                    TOComparator.areTOCollectionsEqual(unorderedExpectedExprCalls, unorderedExpressions));
            assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
            //XXX: The LIMIT feature is actually only activated when includeSubStages is true, 
            //but this might change in the future, so we keep the test here.
            //assertTrue("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
            assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
            
            //now, filter with conditions, data types, gene IDs, species IDs, etc
            filters = Arrays.asList(
                    new CallDAOFilter(Arrays.asList("ID1", "ID100"), //ID100 does not exist
                            Arrays.asList("11", "21", "41"), // 41 = species Id that does not exist
                            Arrays.asList(new DAOConditionFilter(
                                    Arrays.asList("Anat_id6", "Anat_id1"), 
                                    Arrays.asList("Stage_id6", "Stage_id7")), 
                                    //This filter should select no call
                                    new DAOConditionFilter(
                                            Arrays.asList("Anat_id600", "Anat_id100"), 
                                            Arrays.asList("Stage_id600", "Stage_id700")))
                            ), 
                    new CallDAOFilter(Arrays.asList("ID2"), 
                            Arrays.asList("21"), 
                            Arrays.asList(new DAOConditionFilter(null, Arrays.asList("Stage_id1")))
                            ));
            callTOFilters = Arrays.asList(
                    new ExpressionCallTO(DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                            DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                            OriginOfLine.BOTH, OriginOfLine.DESCENT, false), 
                    new ExpressionCallTO(DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                            DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                            OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                    //This CallTO doesn't allow to retrieve any call
                    new ExpressionCallTO(DataState.HIGHQUALITY, null, null, null, 
                            OriginOfLine.SELF, OriginOfLine.SELF, true), 
                    new ExpressionCallTO(DataState.HIGHQUALITY, null, null, 
                            DataState.LOWQUALITY, OriginOfLine.SELF, 
                            OriginOfLine.BOTH, true), 
                    new ExpressionCallTO(DataState.HIGHQUALITY, null, null, 
                            DataState.LOWQUALITY, OriginOfLine.SELF, 
                            OriginOfLine.SELF, true), 
                    new ExpressionCallTO(null, null, DataState.HIGHQUALITY, null, 
                            OriginOfLine.DESCENT, OriginOfLine.SELF, false), 
                    new ExpressionCallTO(null, null, DataState.HIGHQUALITY, null, 
                            OriginOfLine.SELF, OriginOfLine.SELF, true),
                    //This CallTO doesn't allow to retrieve any call
                    new ExpressionCallTO(null, null, null, DataState.HIGHQUALITY)
                );
            orderedExpectedExprCalls = Arrays.asList(
                    //first CallFilter should retrieve those: 
                    new ExpressionCallTO("ID1__Anat_id6__Stage_id6", "ID1", "Anat_id6", "Stage_id6", 
                            DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                            DataState.LOWQUALITY, true, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                    new ExpressionCallTO("ID1__Anat_id6__Stage_id7", "ID1", "Anat_id6", "Stage_id7", 
                            DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, 
                            true, true, OriginOfLine.SELF, OriginOfLine.BOTH, true),
                    
                    //second CallFilter should retrieve those: 
                    new ExpressionCallTO("ID2__Anat_id11__Stage_id1", "ID2", "Anat_id11", "Stage_id1", 
                            DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                            DataState.HIGHQUALITY, true, true, 
                            OriginOfLine.BOTH, OriginOfLine.DESCENT, false), 
                    new ExpressionCallTO("ID2__Anat_id3__Stage_id1", "ID2", "Anat_id3", "Stage_id1", 
                            DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                            DataState.HIGHQUALITY, true, true, 
                            OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                    new ExpressionCallTO("ID2__Anat_id4__Stage_id1", "ID2", "Anat_id4", "Stage_id1", 
                            DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                            DataState.HIGHQUALITY, true, true,
                            OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                    new ExpressionCallTO("ID2__Anat_id5__Stage_id1", "ID2", "Anat_id5", "Stage_id1", 
                            DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                            DataState.HIGHQUALITY, true, true, 
                            OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                    new ExpressionCallTO("ID2__Anat_id9__Stage_id1", "ID2", "Anat_id9", "Stage_id1", 
                            DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                            DataState.HIGHQUALITY, true, true, 
                            OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                    new ExpressionCallTO("ID2__Anat_id10__Stage_id1", "ID2", "Anat_id10", "Stage_id1", 
                            DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                            DataState.HIGHQUALITY, true, true, 
                            OriginOfLine.DESCENT, OriginOfLine.DESCENT, false), 
                    new ExpressionCallTO("ID2__Anat_id1__Stage_id1", "ID2", "Anat_id1", "Stage_id1", 
                            DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                            DataState.HIGHQUALITY, true, true, 
                            OriginOfLine.BOTH, OriginOfLine.BOTH, false), 
                    new ExpressionCallTO("ID2__Anat_id2__Stage_id1", "ID2", "Anat_id2", "Stage_id1", 
                            DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                            DataState.HIGHQUALITY, true, true, 
                            OriginOfLine.SELF, OriginOfLine.DESCENT, false));
            //No ordering requested, put in a Set
            unorderedExpectedExprCalls = new HashSet<>(orderedExpectedExprCalls);
            // Compare
            rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(filters, callTOFilters, 
                            true, true, null, null, null, null);
            //no ordering requested, put results in a Set
            unorderedExpressions = new HashSet<>(rs.getAllTOs());
            assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + unorderedExpectedExprCalls 
                    + ", but was: " + unorderedExpressions, 
                    TOComparator.areTOCollectionsEqual(unorderedExpectedExprCalls, unorderedExpressions));
            assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
            assertTrue("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
            
            //TODO: test with ordering
            
        } finally {
            //restore default parameters
            this.getMySQLDAOManager(DAOManager.getDefaultProperties());
        }
    }

    
    /**
     * Unit test for {@link MySQLExpressionCallDAO#getExpressionCalls(Collection, 
     * Collection, String, Collection, LinkedHashMap)}, when needing a GROUP BY clause 
     * (because including substages, or requesting or filtering on some specific parameters), 
     * and the LIMIT feature, to test the use of the filtering feature.
     */
    @Test
    public void testExpressionCallLimitFiltering() throws SQLException {
            
        
        this.useSelectDB();
        
        //to test the LIMIT feature used when propagating expression calls on-the-fly 
        //with a GROUP BY needed, we change the gene count limit from the properties.
        Properties newProps = DAOManager.getDefaultProperties();
        newProps.setProperty(MySQLDAOManager.EXPR_PROPAGATION_GENE_COUNT_KEY, "1");
        try {
            MySQLDAOManager manager = this.getMySQLDAOManager(newProps);
            MySQLExpressionCallDAO dao = new MySQLExpressionCallDAO(manager);
            
            
            //First, retrieve everything for species 11 and 21
            CallDAOFilter filter = new CallDAOFilter(
                    Arrays.asList("ID1", "ID100"), //ID100 is a fake gene ID meant to trigger 
                                                   //the use of the LIMIT feature
                    Arrays.asList("11", "21", "41"), // 41 = species Id that does not exist
                    Arrays.asList(new DAOConditionFilter(
                            Arrays.asList("Anat_id6", "Anat_id1"), Arrays.asList("Stage_id8"))));
            // Generate manually expected result
            List<ExpressionCallTO> orderedExpectedExprCalls = Arrays.asList(
                new ExpressionCallTO(null, null, "Anat_id6", "Stage_id8", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        true, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO(null, null, "Anat_id1", "Stage_id8", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        true, true, OriginOfLine.DESCENT, OriginOfLine.SELF, false));
            //No ordering requested, put in a Set
            Set<ExpressionCallTO> unorderedExpectedExprCalls = new HashSet<>(orderedExpectedExprCalls);
            // Compare
            MySQLExpressionCallTOResultSet rs = 
                    (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(Arrays.asList(filter), 
                            null, true, true, null, null, 
                            EnumSet.allOf(ExpressionCallDAO.Attribute.class).stream()
                                .filter(attr -> !attr.equals(ExpressionCallDAO.Attribute.ID) && 
                                        !attr.equals(ExpressionCallDAO.Attribute.GENE_ID))
                                .collect(Collectors.toCollection(() -> 
                                         EnumSet.noneOf(ExpressionCallDAO.Attribute.class))), 
                            null);
            //no ordering requested, put results in a Set
            Set<ExpressionCallTO> unorderedExpressions = new HashSet<>(rs.getAllTOs());
            assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + unorderedExpectedExprCalls 
                    + ", but was: " + unorderedExpressions, 
                    TOComparator.areTOCollectionsEqual(unorderedExpectedExprCalls, unorderedExpressions));
            //the filtering should be activated, because if we don't retrieve a gene ID or an expression ID, 
            //then the iterations of the LIMIT sub-query could return redundant results
            assertTrue("Incorrect filtering of duplicates", rs.isFilterDuplicates());
            assertTrue("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
            
            //TODO: test with some ordering
            
            
            orderedExpectedExprCalls = Arrays.asList(
                    new ExpressionCallTO(null, null, "Anat_id6", null, 
                            DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                            DataState.LOWQUALITY, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                    
                    new ExpressionCallTO(null, null, "Anat_id1", null, 
                            DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                            DataState.HIGHQUALITY, true, false,
                            OriginOfLine.BOTH, OriginOfLine.SELF, true));
            
            //now, filter with conditions, data types, gene IDs, species IDs, etc
            Collection<CallDAOFilter> filters = Arrays.asList(
                    new CallDAOFilter(Arrays.asList("ID1", "ID2", "ID:100"), //ID100 does not exist
                            Arrays.asList("11", "21", "41"), // 41 = species Id that does not exist
                            Arrays.asList(new DAOConditionFilter(
                                    Arrays.asList("Anat_id1", "Anat_id6"), null))
                            ));
            Collection<ExpressionCallTO> callTOFilters = Arrays.asList(
                    new ExpressionCallTO(DataState.HIGHQUALITY, null, null, 
                            DataState.LOWQUALITY, OriginOfLine.SELF, 
                            OriginOfLine.BOTH, true), 
                    new ExpressionCallTO(DataState.HIGHQUALITY, null, null, 
                            DataState.HIGHQUALITY, OriginOfLine.BOTH, 
                            OriginOfLine.BOTH, true)
                    );
            //No ordering requested, put in a Set
            unorderedExpectedExprCalls = new HashSet<>(orderedExpectedExprCalls);
            // Compare. 
            rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(filters, callTOFilters, 
                    true, false, null, null, 
                    EnumSet.allOf(ExpressionCallDAO.Attribute.class).stream()
                    .filter(attr -> !attr.equals(ExpressionCallDAO.Attribute.ID) && 
                            !attr.equals(ExpressionCallDAO.Attribute.GENE_ID) &&
                            !attr.equals(ExpressionCallDAO.Attribute.STAGE_ID))
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(ExpressionCallDAO.Attribute.class))), 
                    null);
            //no ordering requested, put results in a Set
            unorderedExpressions = new HashSet<>(rs.getAllTOs());
            assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + unorderedExpectedExprCalls 
                    + ", but was: " + unorderedExpressions, 
                    TOComparator.areTOCollectionsEqual(unorderedExpectedExprCalls, unorderedExpressions));
            //XXX: The LIMIT feature is actually only activated when includeSubStages is true, 
            //but this might change in the future, so we keep the test here.
            //assertTrue("Incorrect filtering of duplicates", rs.isFilterDuplicates());
            //assertTrue("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
            assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
            assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
            
        } finally {
            //restore default parameters
            this.getMySQLDAOManager(DAOManager.getDefaultProperties());
        }
    }
    
    /**
     * Test the method {@link MySQLExpressionCallDAO#getExpressionCalls(Collection, 
     * Collection, String, Collection, LinkedHashMap)}, to see if it correctly reproduces 
     * previously existing behavior. 
     */
    @Test
    public void regressionTestGetExpressionCalls() throws SQLException {
        
        this.useSelectDB();

        // On expression table 
        MySQLExpressionCallDAO dao = new MySQLExpressionCallDAO(this.getMySQLDAOManager());

        // No filtering, no ordering
        // Generate manually expected result
        Set<ExpressionCallTO> expectedExprCalls = new HashSet<>(Arrays.asList(
                new ExpressionCallTO("1", "ID3", "Anat_id1", "Stage_id1", DataState.LOWQUALITY, 
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("2", "ID1", "Anat_id6", "Stage_id6", DataState.LOWQUALITY,
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY,
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("3", "ID1", "Anat_id6", "Stage_id7", DataState.NODATA, 
                        DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY,
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("4", "ID2", "Anat_id2", "Stage_id18", DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("5", "ID1", "Anat_id7", "Stage_id10", DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("6", "ID2", "Anat_id11", "Stage_id12", DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("7", "ID2", "Anat_id11", "Stage_id13", DataState.NODATA, 
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("8", "ID3", "Anat_id3", "Stage_id1", DataState.NODATA, 
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA,
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("9", "ID2", "Anat_id1", "Stage_id9", DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("10", "ID1", "Anat_id6", "Stage_id8", DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("11", "ID2", "Anat_id1", "Stage_id2", DataState.NODATA, 
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("12", "ID1", "Anat_id1", "Stage_id1", DataState.NODATA, 
                        DataState.NODATA, DataState.LOWQUALITY, DataState.NODATA, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true)));

        // Compare
        MySQLExpressionCallTOResultSet rs = 
                (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(null, null, false, false, 
                        null, null, null, null);
        //no ordering requested, put results in a Set
        Set<ExpressionCallTO> expressions = new HashSet<>(rs.getAllTOs());
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls 
                + ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());

        // Same test but with only two attributes
        // Generate manually expected result
        expectedExprCalls = new HashSet<>(Arrays.asList(
                new ExpressionCallTO(null, "ID1", null, null, null, null, null, null, null, null, 
                        null, null, true),
                new ExpressionCallTO(null, "ID2", null, null, null, null, null, null, null, null, 
                        null, null, true),
                new ExpressionCallTO(null, "ID3", null, null, null, null, null, null, null, null, 
                        null, null, true)));

        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(null, null, false, false, null, null, 
                        Arrays.asList(ExpressionCallDAO.Attribute.GENE_ID, 
                                ExpressionCallDAO.Attribute.OBSERVED_DATA), 
                        null);
        //no ordering requested, put results in a Set
        expressions = new HashSet<>(rs.getAllTOs());
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls 
                + ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
        
        // With speciesIds but not include substructures 
        // Generate parameters
        CallDAOFilter filter = new CallDAOFilter(null, 
                Arrays.asList("11", "41"), // 41 = species Id that does not exist)
                null);
        // Generate manually expected result
        expectedExprCalls = new HashSet<>(Arrays.asList(
                new ExpressionCallTO("2","ID1", "Anat_id6", "Stage_id6", DataState.LOWQUALITY, 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("3","ID1", "Anat_id6", "Stage_id7", DataState.NODATA, 
                        DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("5","ID1", "Anat_id7", "Stage_id10", DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("10", "ID1", "Anat_id6", "Stage_id8", DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("12", "ID1", "Anat_id1", "Stage_id1", DataState.NODATA, 
                        DataState.NODATA, DataState.LOWQUALITY, DataState.NODATA, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true)));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(Arrays.asList(filter), null,  
                false, false, null, null, null, null);
        //no ordering requested, put results in a Set
        expressions = new HashSet<>(rs.getAllTOs());
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls 
                + ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());       

        // On global expression table 
        // With speciesIds (11 and 41) and include substructures 
        // Generate parameters
        filter = new CallDAOFilter(null, 
                Arrays.asList("11", "41"), // 41 = species Id that does not exist)
                null);
        // Generate manually expected result
        expectedExprCalls = new HashSet<>(Arrays.asList(
                new ExpressionCallTO("2", "ID1", "Anat_id6", "Stage_id6", DataState.LOWQUALITY, 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("3", "ID1", "Anat_id6", "Stage_id7", DataState.NODATA, 
                        DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, 
                        true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("5", "ID1", "Anat_id7", "Stage_id10", DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        true, false, OriginOfLine.BOTH, OriginOfLine.SELF, true),
                new ExpressionCallTO("22", "ID1", "Anat_id6", "Stage_id8", DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("24", "ID1", "Anat_id1", "Stage_id8", DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("26", "ID1", "Anat_id1", "Stage_id1", DataState.NODATA, 
                        DataState.NODATA, DataState.LOWQUALITY, DataState.NODATA, 
                        true, false, OriginOfLine.SELF, OriginOfLine.SELF, true)));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(Arrays.asList(filter), null, 
                true, false, null, null, null, null);
        //no ordering requested, put results in a Set
        expressions = new HashSet<>(rs.getAllTOs());
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls 
                + ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature()); 

        // Same test but with only two attributes
        // Generate parameters
        filter = new CallDAOFilter(null, 
                Arrays.asList("11", "41"), // 41 = species Id that does not exist)
                null);
        // Generate manually expected result
        expectedExprCalls = new HashSet<>(Arrays.asList(
                new ExpressionCallTO(null, "ID1", null, null, null, null, null, null, 
                        null, null, null, null, true)));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(Arrays.asList(filter), null, 
                true, false, null, null, 
                Arrays.asList(ExpressionCallDAO.Attribute.GENE_ID, 
                        ExpressionCallDAO.Attribute.OBSERVED_DATA), 
                null);
        //no ordering requested, put results in a Set
        expressions = new HashSet<>(rs.getAllTOs());
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls 
                + ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature()); 
        
        // Without species filter but include substructures, all attributes
        // Generate parameters
        filter = new CallDAOFilter(null, null, null);
        //Generate manually expected result
        expectedExprCalls = new HashSet<>(Arrays.asList(
                new ExpressionCallTO("1", "ID3", "Anat_id1", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("2", "ID1", "Anat_id6", "Stage_id6", 
                        DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("3", "ID1", "Anat_id6", "Stage_id7", 
                        DataState.NODATA, DataState.NODATA, DataState.NODATA, 
                        DataState.LOWQUALITY, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("4", "ID2", "Anat_id2", "Stage_id18",
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("5", "ID1", "Anat_id7", "Stage_id10", 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, true, false, OriginOfLine.BOTH, OriginOfLine.SELF, true),
                new ExpressionCallTO("6", "ID2", "Anat_id11", "Stage_id12",
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("7", "ID2", "Anat_id11", "Stage_id13",
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.NODATA, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("8", "ID3", "Anat_id2", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("9", "ID3", "Anat_id3", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("10", "ID3", "Anat_id4", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("11", "ID3", "Anat_id5", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("12", "ID3", "Anat_id6", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("13", "ID3", "Anat_id9", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("14", "ID3", "Anat_id10", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("15", "ID3", "Anat_id11", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("16", "ID2", "Anat_id3", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("17", "ID2", "Anat_id4", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("18", "ID2", "Anat_id5", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("19", "ID2", "Anat_id9", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("20", "ID2", "Anat_id10", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("21", "ID2", "Anat_id11", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("22", "ID1", "Anat_id6", "Stage_id8", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                        DataState.NODATA, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("23", "ID2", "Anat_id1", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("24", "ID1", "Anat_id1", "Stage_id8", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                        DataState.NODATA, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("25", "ID2", "Anat_id1", "Stage_id2", 
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.NODATA, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("26", "ID1", "Anat_id1", "Stage_id1", DataState.NODATA, 
                        DataState.NODATA, DataState.LOWQUALITY, DataState.NODATA, 
                        true, false, OriginOfLine.SELF, OriginOfLine.SELF, true)));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(Arrays.asList(filter), null,  
                true, false, null, null, null, null);
        //no ordering requested, put results in a Set
        expressions = new HashSet<>(rs.getAllTOs());
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls 
                + ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature()); 
        
        // Test get only GENE_ID without species filter and without including substructures
        filter = new CallDAOFilter(null, null, null);
        expectedExprCalls = new HashSet<>(Arrays.asList(
                new ExpressionCallTO(null, "ID1", null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO(null, "ID2", null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO(null, "ID3", null, null, null, null, null, null, null, null, null, null, null)));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(Arrays.asList(filter), null, 
                true, false, null, null, Arrays.asList(ExpressionCallDAO.Attribute.GENE_ID), null);
        //no ordering requested, put results in a Set
        expressions = new HashSet<>(rs.getAllTOs());
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls 
                + ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature()); 

        // Test get only INCLUDE_SUBSTRUCTURES without species filter and without including substructures
        filter = new CallDAOFilter(null, null, null);
        expectedExprCalls = new HashSet<>(Arrays.asList(
                new ExpressionCallTO(null, null, null, null, null, null, null, null, false, null, 
                        null, null, null)));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(Arrays.asList(filter), null, 
                false, false, null, null, Arrays.asList(ExpressionCallDAO.Attribute.INCLUDE_SUBSTRUCTURES), null);
        //no ordering requested, put results in a Set
        expressions = new HashSet<>(rs.getAllTOs());
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls 
                + ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature()); 

        // Test get only ID without species filter and including substructures
        filter = new CallDAOFilter(null, null, null);
        expectedExprCalls = new HashSet<>(Arrays.asList(
                new ExpressionCallTO("1", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("2", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("3", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("4", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("5", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("6", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("7", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("8", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("9", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("10", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("11", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("12", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("13", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("14", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("15", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("16", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("17", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("18", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("19", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("20", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("21", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("22", null, null, null, null, null, null, null, null, null, null, null, null), 
                new ExpressionCallTO("23", null, null, null, null, null, null, null, null, null, null, null, null), 
                new ExpressionCallTO("24", null, null, null, null, null, null, null, null, null, null, null, null), 
                new ExpressionCallTO("25", null, null, null, null, null, null, null, null, null, null, null, null), 
                new ExpressionCallTO("26", null, null, null, null, null, null, null, null, null, null, null, null)));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(Arrays.asList(filter), null, 
                true, false, null, null, Arrays.asList(ExpressionCallDAO.Attribute.ID), null);
        //no ordering requested, put results in a Set
        expressions = new HashSet<>(rs.getAllTOs());
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls 
                + ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature()); 
        
        // Test get INCLUDE_SUBSTRUCTURES (and STAGE_ID) without OriginOfLine including substructures
        filter = new CallDAOFilter(null, null, null);
        expectedExprCalls = new HashSet<>(Arrays.asList(
                new ExpressionCallTO(null, null, null, "Stage_id1", 
                        null, null, null, null, true, null, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id2", 
                        null, null, null, null, true, null, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id6", 
                        null, null, null, null, true, null, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id7", 
                        null, null, null, null, true, null, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id8", 
                        null, null, null, null, true, null, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id18",
                        null, null, null, null, true, null, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id10", 
                        null, null, null, null, true, null, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id12",
                        null, null, null, null, true, null, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id13",
                        null, null, null, null, true, null, null, null, null)));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(Arrays.asList(filter), null, 
                true, false, null, null, Arrays.asList(ExpressionCallDAO.Attribute.STAGE_ID, 
                        ExpressionCallDAO.Attribute.INCLUDE_SUBSTRUCTURES), null);
        //no ordering requested, put results in a Set
        expressions = new HashSet<>(rs.getAllTOs());
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls 
                + ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature()); 
    }
    
    /**
     * Test the method {@link MySQLExpressionCallDAO#getExpressionCalls(Collection, 
     * Collection, String, Collection, LinkedHashMap)} when including data from sub-stages, 
     * to see if it correctly reproduces already existing behavior. 
     */
    @Test
    public void regressionTestGetExpressionCallsIncludeSubStages() throws SQLException {
        
        this.useSelectDB();

        //to test the LIMIT feature used when propagating expression calls on-the-fly 
        //with a GROUP BY needed, we change the gene count limit from the properties.
        Properties newProps = DAOManager.getDefaultProperties();
        newProps.setProperty(MySQLDAOManager.EXPR_PROPAGATION_GENE_COUNT_KEY, "2");
        try {
        MySQLDAOManager manager = this.getMySQLDAOManager(newProps);
        
        MySQLExpressionCallDAO dao = new MySQLExpressionCallDAO(manager);
        
        Set<String> allGeneIds = new HashSet<>(Arrays.asList("ID1", "ID2", "ID3", "ID4"));
        


        // First, we test with no GROUP BY needed (we only request attributes not needing a GROUP BY)
        // Without speciesIds and not include organ substructures
        // Generate parameters
        CallDAOFilter filter = new CallDAOFilter(null, null, null);
        // Generate manually expected result. No ordering requested, put in a Set
        Set<ExpressionCallTO> expectedExprCalls = new HashSet<>(Arrays.asList(
                new ExpressionCallTO("ID1__Anat_id1__Stage_id1", "ID1", "Anat_id1", "Stage_id1",
                        null, null, null, null, false, true, OriginOfLine.SELF, null, null),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id1", "ID1", "Anat_id6", "Stage_id1",
                        null, null, null, null, false, true, OriginOfLine.SELF, null, null),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id5", "ID1", "Anat_id6", "Stage_id5", 
                        null, null, null, null, false, true, OriginOfLine.SELF, null, null),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id6", "ID1", "Anat_id6", "Stage_id6", 
                        null, null, null, null, false, true, OriginOfLine.SELF, null, null),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id7", "ID1", "Anat_id6", "Stage_id7", 
                        null, null, null, null, false, true, OriginOfLine.SELF, null, null),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id8", "ID1", "Anat_id6", "Stage_id8", 
                        null, null, null, null, false, true, OriginOfLine.SELF, null, null),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id1", "ID1", "Anat_id7", "Stage_id1", 
                        null, null, null, null, false, true, OriginOfLine.SELF, null, null),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id10", "ID1", "Anat_id7", "Stage_id10", 
                        null, null, null, null, false, true, OriginOfLine.SELF, null, null),
                new ExpressionCallTO("ID2__Anat_id1__Stage_id1", "ID2", "Anat_id1", "Stage_id1", 
                        null, null, null, null, false, true, OriginOfLine.SELF, null, null), 
                new ExpressionCallTO("ID2__Anat_id1__Stage_id2", "ID2", "Anat_id1", "Stage_id2", 
                        null, null, null, null, false, true, OriginOfLine.SELF, null, null), 
                new ExpressionCallTO("ID2__Anat_id1__Stage_id5", "ID2", "Anat_id1", "Stage_id5", 
                        null, null, null, null, false, true, OriginOfLine.SELF, null, null), 
                new ExpressionCallTO("ID2__Anat_id1__Stage_id7", "ID2", "Anat_id1", "Stage_id7", 
                        null, null, null, null, false, true, OriginOfLine.SELF, null, null), 
                new ExpressionCallTO("ID2__Anat_id1__Stage_id9", "ID2", "Anat_id1", "Stage_id9", 
                        null, null, null, null, false, true, OriginOfLine.SELF, null, null), 
                new ExpressionCallTO("ID2__Anat_id2__Stage_id1", "ID2", "Anat_id2", "Stage_id1", 
                        null, null, null, null, false, true, OriginOfLine.SELF, null, null),
                new ExpressionCallTO("ID2__Anat_id2__Stage_id14", "ID2", "Anat_id2", "Stage_id14", 
                        null, null, null, null, false, true, OriginOfLine.SELF, null, null),
                new ExpressionCallTO("ID2__Anat_id2__Stage_id18", "ID2", "Anat_id2", "Stage_id18", 
                        null, null, null, null, false, true, OriginOfLine.SELF, null, null),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id1", "ID2", "Anat_id11", "Stage_id1", 
                        null, null, null, null, false, true, OriginOfLine.SELF, null, null),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id10", "ID2", "Anat_id11", "Stage_id10", 
                        null, null, null, null, false, true, OriginOfLine.SELF, null, null),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id12", "ID2", "Anat_id11", "Stage_id12", 
                        null, null, null, null, false, true, OriginOfLine.SELF, null, null),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id13", "ID2", "Anat_id11", "Stage_id13", 
                        null, null, null, null, false, true, OriginOfLine.SELF, null, null),
                new ExpressionCallTO("ID3__Anat_id1__Stage_id1", "ID3", "Anat_id1", "Stage_id1", 
                        null, null, null, null, false, true, OriginOfLine.SELF, null, null),
                new ExpressionCallTO("ID3__Anat_id3__Stage_id1", "ID3", "Anat_id3", "Stage_id1", 
                        null, null, null, null, false, true, OriginOfLine.SELF, null, null)));
        // Compare
        MySQLExpressionCallTOResultSet rs = 
                (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(Arrays.asList(filter), 
                        null, false, true, null, null, 
                        Arrays.asList(ExpressionCallDAO.Attribute.ID, 
                                ExpressionCallDAO.Attribute.GENE_ID, 
                                ExpressionCallDAO.Attribute.STAGE_ID, 
                                ExpressionCallDAO.Attribute.ANAT_ENTITY_ID, 
                                ExpressionCallDAO.Attribute.INCLUDE_SUBSTRUCTURES, 
                                ExpressionCallDAO.Attribute.INCLUDE_SUBSTAGES, 
                                ExpressionCallDAO.Attribute.ANAT_ORIGIN_OF_LINE), 
                        null);
        //no ordering requested, put results in a Set
        Set<ExpressionCallTO> expressions = new HashSet<>(rs.getAllTOs());
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls 
                + ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
        
        
        // GROUP BY needed but no LIMIT clause
        dao = new MySQLExpressionCallDAO(this.getMySQLDAOManager(DAOManager.getDefaultProperties()));
        //need to restrict the gene IDs to use to get no LIMIT clause
        filter = new CallDAOFilter(allGeneIds, null, null);
        // Generate manually expected result. No ordering requested, put in a Set
        expectedExprCalls = new HashSet<>(Arrays.asList(
                new ExpressionCallTO("ID1__Anat_id1__Stage_id1", "ID1", "Anat_id1", "Stage_id1",
                        DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, 
                        DataState.NODATA, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id1", "ID1", "Anat_id6", "Stage_id1",
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id5", "ID1", "Anat_id6", "Stage_id5", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id6", "ID1", "Anat_id6", "Stage_id6", 
                        DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id7", "ID1", "Anat_id6", "Stage_id7", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.BOTH, true),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id8", "ID1", "Anat_id6", "Stage_id8", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                        DataState.NODATA, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id1", "ID1", "Anat_id7", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id10", "ID1", "Anat_id7", "Stage_id10", 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID2__Anat_id1__Stage_id1", "ID2", "Anat_id1", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false), 
                new ExpressionCallTO("ID2__Anat_id1__Stage_id2", "ID2", "Anat_id1", "Stage_id2", 
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.NODATA, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true), 
                new ExpressionCallTO("ID2__Anat_id1__Stage_id5", "ID2", "Anat_id1", "Stage_id5", 
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false), 
                new ExpressionCallTO("ID2__Anat_id1__Stage_id7", "ID2", "Anat_id1", "Stage_id7", 
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false), 
                new ExpressionCallTO("ID2__Anat_id1__Stage_id9", "ID2", "Anat_id1", "Stage_id9", 
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true), 
                new ExpressionCallTO("ID2__Anat_id2__Stage_id1", "ID2", "Anat_id2", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id2__Stage_id14", "ID2", "Anat_id2", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id2__Stage_id18", "ID2", "Anat_id2", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id1", "ID2", "Anat_id11", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id10", "ID2", "Anat_id11", "Stage_id10", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id12", "ID2", "Anat_id11", "Stage_id12", 
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id13", "ID2", "Anat_id11", "Stage_id13", 
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.NODATA, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID3__Anat_id1__Stage_id1", "ID3", "Anat_id1", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID3__Anat_id3__Stage_id1", "ID3", "Anat_id3", "Stage_id1", 
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.NODATA, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true)));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(Arrays.asList(filter), null, 
                        false, true, null, null, null, null);
        //no ordering requested, put results in a Set
        expressions = new HashSet<>(rs.getAllTOs());
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls 
                + ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());

        
        // GROUP BY needed, and LIMIT clause
        dao = new MySQLExpressionCallDAO(this.getMySQLDAOManager(newProps));
        filter = new CallDAOFilter(null, null, null);
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(Arrays.asList(filter), null, 
                        false, true, null, null, null, null);
        //no ordering requested, put results in a Set
        expressions = new HashSet<>(rs.getAllTOs());
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls 
                + ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertTrue("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());

        
        
        // With speciesIds but not include substructures, first, with no GROUP BY needed
        // Generate parameters
        filter = new CallDAOFilter(null, 
                Arrays.asList("11", "41"), // 41 = species Id that does not exist
                null);
        // Generate manually expected result
        expectedExprCalls = new HashSet<>(Arrays.asList(
                new ExpressionCallTO("ID1__Anat_id1__Stage_id1", "ID1", "Anat_id1", "Stage_id1",
                        null, null, null, null, false, true, OriginOfLine.SELF, null, null),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id6","ID1", "Anat_id6", "Stage_id6", 
                        null, null, null, null, false, true, OriginOfLine.SELF, null, null),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id5","ID1", "Anat_id6", "Stage_id5", 
                        null, null, null, null, false, true, OriginOfLine.SELF, null, null),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id1","ID1", "Anat_id6", "Stage_id1", 
                        null, null, null, null, false, true, OriginOfLine.SELF, null, null),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id7","ID1", "Anat_id6", "Stage_id7", 
                        null, null, null, null, false, true, OriginOfLine.SELF, null, null),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id8","ID1", "Anat_id6", "Stage_id8", 
                        null, null, null, null, false, true, OriginOfLine.SELF, null, null),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id10","ID1", "Anat_id7", "Stage_id10", 
                        null, null, null, null, false, true, OriginOfLine.SELF, null, null),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id1","ID1", "Anat_id7", "Stage_id1", 
                        null, null, null, null, false, true, OriginOfLine.SELF, null, null)));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(Arrays.asList(filter), null, 
                false, true, null, null, 
                Arrays.asList(ExpressionCallDAO.Attribute.ID, 
                        ExpressionCallDAO.Attribute.GENE_ID, 
                        ExpressionCallDAO.Attribute.STAGE_ID, 
                        ExpressionCallDAO.Attribute.ANAT_ENTITY_ID, 
                        ExpressionCallDAO.Attribute.INCLUDE_SUBSTRUCTURES, 
                        ExpressionCallDAO.Attribute.INCLUDE_SUBSTAGES, 
                        ExpressionCallDAO.Attribute.ANAT_ORIGIN_OF_LINE), 
                null);
        //no ordering requested, put results in a Set
        expressions = new HashSet<>(rs.getAllTOs());
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls 
                + ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());

        
        // GROUP BY needed but no LIMIT clause
        dao = new MySQLExpressionCallDAO(this.getMySQLDAOManager(DAOManager.getDefaultProperties()));
        //need to restrict the gene IDs to get no LIMIT clause
        filter = new CallDAOFilter(allGeneIds, 
                Arrays.asList("11", "41"), // 41 = species Id that does not exist
                null);
        // Generate manually expected result
        expectedExprCalls = new HashSet<>(Arrays.asList(
                new ExpressionCallTO("ID1__Anat_id1__Stage_id1", "ID1", "Anat_id1", "Stage_id1",
                        DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, 
                        DataState.NODATA, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id6","ID1", "Anat_id6", "Stage_id6", 
                        DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id5","ID1", "Anat_id6", "Stage_id5", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id1","ID1", "Anat_id6", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id7","ID1", "Anat_id6", "Stage_id7", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, 
                        false, true, OriginOfLine.SELF, OriginOfLine.BOTH, true),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id8","ID1", "Anat_id6", "Stage_id8", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id10","ID1", "Anat_id7", "Stage_id10", 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id1","ID1", "Anat_id7", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false)));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(Arrays.asList(filter), null,  
                        false, true, null, null, null, null);
        //no ordering requested, put results in a Set
        expressions = new HashSet<>(rs.getAllTOs());
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls 
                + ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());

        
        // GROUP BY needed, and LIMIT clause
        dao = new MySQLExpressionCallDAO(this.getMySQLDAOManager(newProps));
        filter = new CallDAOFilter(null, 
                Arrays.asList("11", "41"), // 41 = species Id that does not exist
                null);
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(Arrays.asList(filter), null, 
                        false, true, null, null, null, null);
        //no ordering requested, put results in a Set
        expressions = new HashSet<>(rs.getAllTOs());
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls 
                + ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertTrue("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
        
        
        

        // On global expression table 
        // With speciesIds and include substructures. No GROUP BY needed
        dao = new MySQLExpressionCallDAO(this.getMySQLDAOManager(DAOManager.getDefaultProperties()));
        //need to restrict the gene IDs to get no LIMIT clause
        filter = new CallDAOFilter(allGeneIds, 
                Arrays.asList("11", "21", "41"), // 41 = species Id that does not exist
                null);
        // Generate manually expected result
        expectedExprCalls = new HashSet<>(Arrays.asList(
                new ExpressionCallTO("ID1__Anat_id6__Stage_id6", "ID1", "Anat_id6", "Stage_id6", 
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id5", "ID1", "Anat_id6", "Stage_id5", 
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id1", "ID1", "Anat_id6", "Stage_id1", 
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id7", "ID1", "Anat_id6", "Stage_id7", 
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id8", "ID1", "Anat_id6", "Stage_id8", 
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id8", "ID1", "Anat_id1", "Stage_id8", 
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id7", "ID1", "Anat_id1", "Stage_id7", 
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id5", "ID1", "Anat_id1", "Stage_id5", 
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id1", "ID1", "Anat_id1", "Stage_id1", 
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id10", "ID1", "Anat_id7", "Stage_id10", 
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id1", "ID1", "Anat_id7", "Stage_id1", 
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID2__Anat_id1__Stage_id1", "ID2", "Anat_id1", "Stage_id1", 
                        null, null, null, null, true, true, null, null, null), 
                new ExpressionCallTO("ID2__Anat_id1__Stage_id2", "ID2", "Anat_id1", "Stage_id2",
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id1", "ID2", "Anat_id11", "Stage_id1", 
                        null, null, null, null, true, true, null, null, null), 
                new ExpressionCallTO("ID2__Anat_id11__Stage_id10", "ID2", "Anat_id11", "Stage_id10", 
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id12", "ID2", "Anat_id11", "Stage_id12",
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id13", "ID2", "Anat_id11", "Stage_id13", 
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id14", "ID2", "Anat_id11", "Stage_id14", 
                        null, null, null, null, true, true, null, null, null), 
                new ExpressionCallTO("ID2__Anat_id11__Stage_id18", "ID2", "Anat_id11", "Stage_id18",
                        null, null, null, null, true, true, null, null, null), 
                new ExpressionCallTO("ID2__Anat_id2__Stage_id1", "ID2", "Anat_id2", "Stage_id1", 
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID2__Anat_id2__Stage_id14", "ID2", "Anat_id2", "Stage_id14", 
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID2__Anat_id2__Stage_id18", "ID2", "Anat_id2", "Stage_id18", 
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID2__Anat_id3__Stage_id18", "ID2", "Anat_id3", "Stage_id18", 
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID2__Anat_id3__Stage_id14", "ID2", "Anat_id3", "Stage_id14", 
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID2__Anat_id3__Stage_id1", "ID2", "Anat_id3", "Stage_id1", 
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID2__Anat_id4__Stage_id18", "ID2", "Anat_id4", "Stage_id18", 
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID2__Anat_id4__Stage_id14", "ID2", "Anat_id4", "Stage_id14", 
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID2__Anat_id4__Stage_id1", "ID2", "Anat_id4", "Stage_id1", 
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID2__Anat_id5__Stage_id18", "ID2", "Anat_id5", "Stage_id18", 
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID2__Anat_id5__Stage_id14", "ID2", "Anat_id5", "Stage_id14", 
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID2__Anat_id5__Stage_id1", "ID2", "Anat_id5", "Stage_id1", 
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID2__Anat_id9__Stage_id18", "ID2", "Anat_id9", "Stage_id18", 
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID2__Anat_id9__Stage_id14", "ID2", "Anat_id9", "Stage_id14", 
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID2__Anat_id9__Stage_id1", "ID2", "Anat_id9", "Stage_id1", 
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID2__Anat_id10__Stage_id18", "ID2", "Anat_id10", "Stage_id18", 
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID2__Anat_id10__Stage_id14", "ID2", "Anat_id10", "Stage_id14", 
                        null, null, null, null, true, true, null, null, null),
                new ExpressionCallTO("ID2__Anat_id10__Stage_id1", "ID2", "Anat_id10", "Stage_id1", 
                        null, null, null, null, true, true, null, null, null)));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(Arrays.asList(filter), null, 
                        true, true, null, null, 
                        Arrays.asList(ExpressionCallDAO.Attribute.ID, 
                                ExpressionCallDAO.Attribute.GENE_ID, 
                                ExpressionCallDAO.Attribute.STAGE_ID, 
                                ExpressionCallDAO.Attribute.ANAT_ENTITY_ID, 
                                ExpressionCallDAO.Attribute.INCLUDE_SUBSTRUCTURES, 
                                ExpressionCallDAO.Attribute.INCLUDE_SUBSTAGES), 
                        null);
        //no ordering requested, put results in a Set
        expressions = new HashSet<>(rs.getAllTOs());
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls 
                + ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());

         
        // With speciesIds and include substructures. GROUP BY needed, by no LIMIT clause
        dao = new MySQLExpressionCallDAO(this.getMySQLDAOManager(DAOManager.getDefaultProperties()));
        //need to restrict the gene IDs to get no LIMIT clause
        filter = new CallDAOFilter(allGeneIds, 
                Arrays.asList("11", "21", "41"), // 41 = species Id that does not exist
                null);
        // Generate manually expected result
        expectedExprCalls = new HashSet<>(Arrays.asList(
                new ExpressionCallTO("ID1__Anat_id6__Stage_id6", "ID1", "Anat_id6", "Stage_id6", 
                        DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, true, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id5", "ID1", "Anat_id6", "Stage_id5", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, true, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id1", "ID1", "Anat_id6", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, true, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id7", "ID1", "Anat_id6", "Stage_id7", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, 
                        true, true, OriginOfLine.SELF, OriginOfLine.BOTH, true),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id8", "ID1", "Anat_id6", "Stage_id8", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        true, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id8", "ID1", "Anat_id1", "Stage_id8", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        true, true, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id7", "ID1", "Anat_id1", "Stage_id7", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        true, true, OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id5", "ID1", "Anat_id1", "Stage_id5", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        true, true, OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id1", "ID1", "Anat_id1", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        true, true, OriginOfLine.BOTH, OriginOfLine.BOTH, true),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id10", "ID1", "Anat_id7", "Stage_id10", 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, true, true, OriginOfLine.BOTH, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id1", "ID1", "Anat_id7", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, true, true, OriginOfLine.BOTH, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id1__Stage_id1", "ID2", "Anat_id1", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.HIGHQUALITY, true, true,
                        OriginOfLine.BOTH, OriginOfLine.BOTH, false), 
                new ExpressionCallTO("ID2__Anat_id1__Stage_id2", "ID2", "Anat_id1", "Stage_id2",
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.NODATA, true, true, 
                        OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id1", "ID2", "Anat_id11", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.BOTH, OriginOfLine.DESCENT, false), 
                new ExpressionCallTO("ID2__Anat_id11__Stage_id10", "ID2", "Anat_id11", "Stage_id10", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id12", "ID2", "Anat_id11", "Stage_id12",
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id13", "ID2", "Anat_id11", "Stage_id13", 
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.NODATA, true, true, 
                        OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id14", "ID2", "Anat_id11", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false), 
                new ExpressionCallTO("ID2__Anat_id11__Stage_id18", "ID2", "Anat_id11", "Stage_id18",
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.SELF, false), 
                new ExpressionCallTO("ID2__Anat_id2__Stage_id1", "ID2", "Anat_id2", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id2__Stage_id14", "ID2", "Anat_id2", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id2__Stage_id18", "ID2", "Anat_id2", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID2__Anat_id3__Stage_id18", "ID2", "Anat_id3", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("ID2__Anat_id3__Stage_id14", "ID2", "Anat_id3", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true,
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id3__Stage_id1", "ID2", "Anat_id3", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id4__Stage_id18", "ID2", "Anat_id4", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("ID2__Anat_id4__Stage_id14", "ID2", "Anat_id4", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id4__Stage_id1", "ID2", "Anat_id4", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true,
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id5__Stage_id18", "ID2", "Anat_id5", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("ID2__Anat_id5__Stage_id14", "ID2", "Anat_id5", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id5__Stage_id1", "ID2", "Anat_id5", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id9__Stage_id18", "ID2", "Anat_id9", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("ID2__Anat_id9__Stage_id14", "ID2", "Anat_id9", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id9__Stage_id1", "ID2", "Anat_id9", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id10__Stage_id18", "ID2", "Anat_id10", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("ID2__Anat_id10__Stage_id14", "ID2", "Anat_id10", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id10__Stage_id1", "ID2", "Anat_id10", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false)));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(Arrays.asList(filter), null, 
                        true, true, null, null, null, null);
        //no ordering requested, put results in a Set
        expressions = new HashSet<>(rs.getAllTOs());
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls 
                + ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
        
        
        // With speciesIds and include substructures. GROUP BY needed, and LIMIT clause
        dao = new MySQLExpressionCallDAO(this.getMySQLDAOManager(newProps));
        filter = new CallDAOFilter(null, 
                Arrays.asList("11", "21", "41"), // 41 = species Id that does not exist
                null);
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(Arrays.asList(filter), null, 
                        true, true, null, null, null, null);
        //no ordering requested, put results in a Set
        expressions = new HashSet<>(rs.getAllTOs());
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls 
                + ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertTrue("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
        
        // Test get only GENE_ID without species filter and without including substructures, 
        // but with including sub-stages
        filter = new CallDAOFilter(null, null, null);
        expectedExprCalls = new HashSet<>(Arrays.asList(
                new ExpressionCallTO(null, "ID1", null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO(null, "ID2", null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO(null, "ID3", null, null, null, null, null, null, null, null, null, null, null)));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(Arrays.asList(filter), null, 
                        false, true, null, null, Arrays.asList(ExpressionCallDAO.Attribute.GENE_ID), null);
        //no ordering requested, put results in a Set
        expressions = new HashSet<>(rs.getAllTOs());
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls 
                + ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());

        // Test get only INCLUDE_SUBSTRUCTURES without species filter and without including substructures, 
        // but with including sub-stages
        expectedExprCalls = new HashSet<>(Arrays.asList(
                new ExpressionCallTO(null, null, null, null, null, null, null, null, false, null, null, null, null)));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(Arrays.asList(filter), null, 
                        false, true, null, null, Arrays.asList(ExpressionCallDAO.Attribute.INCLUDE_SUBSTRUCTURES), null);
        //no ordering requested, put results in a Set
        expressions = new HashSet<>(rs.getAllTOs());
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls 
                + ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
        
       // Test get only INCLUDE_SUBSTAGES without species filter and without including substructures, 
        // but with including sub-stages
        expectedExprCalls = new HashSet<>(Arrays.asList(
                new ExpressionCallTO(null, null, null, null, null, null, null, null, null, true, null, null, null)));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(Arrays.asList(filter), null, 
                        false, true, null, null, Arrays.asList(ExpressionCallDAO.Attribute.INCLUDE_SUBSTAGES), null);
        //no ordering requested, put results in a Set
        expressions = new HashSet<>(rs.getAllTOs());
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls 
                + ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());

        // Test get only ID with species filter and including substructures and sub-stages
        filter = new CallDAOFilter(null, 
                Arrays.asList("11", "41"), // 41 = species Id that does not exist
                null);
        expectedExprCalls = new HashSet<>(Arrays.asList(
                new ExpressionCallTO("ID1__Anat_id6__Stage_id1", null, null, null, null, null, 
                        null, null, null, null, null, null, null),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id5", null, null, null, null, null, 
                        null, null, null, null, null, null, null),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id6", null, null, null, null, null, 
                        null, null, null, null, null, null, null),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id7", null, null, null, null, null, 
                        null, null, null, null, null, null, null),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id8", null, null, null, null, null, 
                        null, null, null, null, null, null, null),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id1", null, null, null, null, null, 
                        null, null, null, null, null, null, null),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id5", null, null, null, null, null, 
                        null, null, null, null, null, null, null),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id7", null, null, null, null, null, 
                        null, null, null, null, null, null, null),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id8", null, null, null, null, null, 
                        null, null, null, null, null, null, null),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id1", null, null, null, null, null, 
                        null, null, null, null, null, null, null),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id10", null, null, null, null, null, 
                        null, null, null, null, null, null, null)));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(Arrays.asList(filter), null, 
                        true, true, null, null, Arrays.asList(ExpressionCallDAO.Attribute.ID), null);
        //no ordering requested, put results in a Set
        expressions = new HashSet<>(rs.getAllTOs());
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls 
                + ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
        
        // Test get INCLUDE_SUBSTAGES (and STAGE_ID) without OriginOfLine including sub-stages 
        // and sub-structures and species filter
        filter = new CallDAOFilter(null, 
                Arrays.asList("11", "41"), // 41 = species Id that does not exist
                null);
        expectedExprCalls = new HashSet<>(Arrays.asList(
                new ExpressionCallTO(null, null, null, "Stage_id1", 
                null, null, null, null, null, true, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id5", 
                        null, null, null, null, null, true, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id6", 
                        null, null, null, null, null, true, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id7", 
                        null, null, null, null, null, true, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id8",
                        null, null, null, null, null, true, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id10", 
                        null, null, null, null, null, true, null, null, null)));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(Arrays.asList(filter), null, 
                        true, true, null, null, 
                        Arrays.asList(ExpressionCallDAO.Attribute.STAGE_ID, 
                                ExpressionCallDAO.Attribute.INCLUDE_SUBSTAGES), 
                        null);
        //no ordering requested, put results in a Set
        expressions = new HashSet<>(rs.getAllTOs());
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls 
                + ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
        } finally {
            //restore default parameters
            this.getMySQLDAOManager(DAOManager.getDefaultProperties());
        }
    }

    /**
     * Test the select method {@link MySQLExpressionCallDAO#getExpressionCalls(ExpressionCallParams)}.
     */
    @Test
    @Deprecated
    public void shouldGetExpressionCallsDeprecated() throws SQLException {
        
        this.useSelectDB();

        // On expression table 
        MySQLExpressionCallDAO dao = new MySQLExpressionCallDAO(this.getMySQLDAOManager());

        // Without speciesIds and not include substructures
        // Generate parameters
        Set<String> speciesIds = new HashSet<String>();
        ExpressionCallParams params = new ExpressionCallParams();
        params.addAllSpeciesIds(speciesIds);
        params.setIncludeSubstructures(false);
        // Generate manually expected result
        List<ExpressionCallTO> expectedExprCalls = Arrays.asList(
                new ExpressionCallTO("1", "ID3", "Anat_id1", "Stage_id1", DataState.LOWQUALITY, 
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("2", "ID1", "Anat_id6", "Stage_id6", DataState.LOWQUALITY,
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY,
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("3", "ID1", "Anat_id6", "Stage_id7", DataState.NODATA, 
                        DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY,
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("4", "ID2", "Anat_id2", "Stage_id18", DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("5", "ID1", "Anat_id7", "Stage_id10", DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("6", "ID2", "Anat_id11", "Stage_id12", DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("7", "ID2", "Anat_id11", "Stage_id13", DataState.NODATA, 
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("8", "ID3", "Anat_id3", "Stage_id1", DataState.NODATA, 
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA,
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("9", "ID2", "Anat_id1", "Stage_id9", DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("10", "ID1", "Anat_id6", "Stage_id8", DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("11", "ID2", "Anat_id1", "Stage_id2", DataState.NODATA, 
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("12", "ID1", "Anat_id1", "Stage_id1", DataState.NODATA, 
                        DataState.NODATA, DataState.LOWQUALITY, DataState.NODATA, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true));

        // Compare
        MySQLExpressionCallTOResultSet rs = 
                (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        List<ExpressionCallTO> expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());

        // Same test but with only two attributes
        // Generate parameters
        dao.setAttributes(
                ExpressionCallDAO.Attribute.GENE_ID, ExpressionCallDAO.Attribute.OBSERVED_DATA);
        // Generate manually expected result
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO(null, "ID1", null, null, null, null, null, null, null, null, 
                        null, null, true),
                new ExpressionCallTO(null, "ID2", null, null, null, null, null, null, null, null, 
                        null, null, true),
                new ExpressionCallTO(null, "ID3", null, null, null, null, null, null, null, null, 
                        null, null, true));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
        
        // With speciesIds but not include substructures 
        dao.clearAttributes();
        // Generate parameters
        params.addAllSpeciesIds(Arrays.asList("11", "41")); // 41 = species Id that does not exist
        // Generate manually expected result
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO("2","ID1", "Anat_id6", "Stage_id6", DataState.LOWQUALITY, 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("3","ID1", "Anat_id6", "Stage_id7", DataState.NODATA, 
                        DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("5","ID1", "Anat_id7", "Stage_id10", DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("10", "ID1", "Anat_id6", "Stage_id8", DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("12", "ID1", "Anat_id1", "Stage_id1", DataState.NODATA, 
                        DataState.NODATA, DataState.LOWQUALITY, DataState.NODATA, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());        

        // On global expression table 
        // With speciesIds (11 and 41) and include substructures 
        // Generate parameters
        params.setIncludeSubstructures(true);
        // Generate manually expected result
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO("2", "ID1", "Anat_id6", "Stage_id6", DataState.LOWQUALITY, 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("3", "ID1", "Anat_id6", "Stage_id7", DataState.NODATA, 
                        DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, 
                        true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("5", "ID1", "Anat_id7", "Stage_id10", DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        true, false, OriginOfLine.BOTH, OriginOfLine.SELF, true),
                new ExpressionCallTO("22", "ID1", "Anat_id6", "Stage_id8", DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("24", "ID1", "Anat_id1", "Stage_id8", DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("26", "ID1", "Anat_id1", "Stage_id1", DataState.NODATA, 
                        DataState.NODATA, DataState.LOWQUALITY, DataState.NODATA, 
                        true, false, OriginOfLine.SELF, OriginOfLine.SELF, true));

        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls + 
                ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());

        // Same test but with only two attributes
        // Generate parameters
        dao.setAttributes(
                ExpressionCallDAO.Attribute.GENE_ID, ExpressionCallDAO.Attribute.OBSERVED_DATA);
        // Generate manually expected result
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO(null, "ID1", null, null, null, null, null, null, 
                        null, null, null, null, true),
                new ExpressionCallTO(null, "ID1", null, null, null, null, null, null, 
                        null, null, null, null, false));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
        
        // Without species filter but include substructures
        dao.clearAttributes();
        // Generate parameters
        params.clearSpeciesIds();
        //Generate manually expected result
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO("1", "ID3", "Anat_id1", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("2", "ID1", "Anat_id6", "Stage_id6", 
                        DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("3", "ID1", "Anat_id6", "Stage_id7", 
                        DataState.NODATA, DataState.NODATA, DataState.NODATA, 
                        DataState.LOWQUALITY, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("4", "ID2", "Anat_id2", "Stage_id18",
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("5", "ID1", "Anat_id7", "Stage_id10", 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, true, false, OriginOfLine.BOTH, OriginOfLine.SELF, true),
                new ExpressionCallTO("6", "ID2", "Anat_id11", "Stage_id12",
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("7", "ID2", "Anat_id11", "Stage_id13",
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.NODATA, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("8", "ID3", "Anat_id2", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("9", "ID3", "Anat_id3", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("10", "ID3", "Anat_id4", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("11", "ID3", "Anat_id5", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("12", "ID3", "Anat_id6", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("13", "ID3", "Anat_id9", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("14", "ID3", "Anat_id10", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("15", "ID3", "Anat_id11", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("16", "ID2", "Anat_id3", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("17", "ID2", "Anat_id4", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("18", "ID2", "Anat_id5", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("19", "ID2", "Anat_id9", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("20", "ID2", "Anat_id10", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("21", "ID2", "Anat_id11", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("22", "ID1", "Anat_id6", "Stage_id8", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                        DataState.NODATA, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("23", "ID2", "Anat_id1", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("24", "ID1", "Anat_id1", "Stage_id8", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                        DataState.NODATA, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("25", "ID2", "Anat_id1", "Stage_id2", 
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.NODATA, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("26", "ID1", "Anat_id1", "Stage_id1", DataState.NODATA, 
                        DataState.NODATA, DataState.LOWQUALITY, DataState.NODATA, 
                        true, false, OriginOfLine.SELF, OriginOfLine.SELF, true));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
        
        // Test get only GENE_ID without species filter and without including substructures
        dao.clearAttributes();
        dao.setAttributes(Arrays.asList(ExpressionCallDAO.Attribute.GENE_ID));
        params.clearSpeciesIds();
        params.setIncludeSubstructures(false);
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO(null, "ID1", null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO(null, "ID2", null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO(null, "ID3", null, null, null, null, null, null, null, null, null, null, null));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());

        // Test get only GENE_ID without species filter and without including substructures
        dao.clearAttributes();
        dao.setAttributes(Arrays.asList(ExpressionCallDAO.Attribute.INCLUDE_SUBSTRUCTURES));
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO(null, null, null, null, null, null, null, null, false, null, null, null, null));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());

        // Test get only ID without species filter and including substructures
        dao.clearAttributes();
        dao.setAttributes(Arrays.asList(ExpressionCallDAO.Attribute.ID));
        params.setIncludeSubstructures(true);
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO("1", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("2", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("3", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("4", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("5", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("6", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("7", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("8", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("9", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("10", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("11", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("12", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("13", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("14", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("15", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("16", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("17", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("18", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("19", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("20", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("21", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("22", null, null, null, null, null, null, null, null, null, null, null, null), 
                new ExpressionCallTO("23", null, null, null, null, null, null, null, null, null, null, null, null), 
                new ExpressionCallTO("24", null, null, null, null, null, null, null, null, null, null, null, null), 
                new ExpressionCallTO("25", null, null, null, null, null, null, null, null, null, null, null, null), 
                new ExpressionCallTO("26", null, null, null, null, null, null, null, null, null, null, null, null));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
        
        // Test get INCLUDE_SUBSTRUCTURES (and STAGE_ID) without OriginOfLine including substructures
        dao.clearAttributes();
        dao.setAttributes(
                ExpressionCallDAO.Attribute.STAGE_ID, ExpressionCallDAO.Attribute.INCLUDE_SUBSTRUCTURES);
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO(null, null, null, "Stage_id1", 
                        null, null, null, null, true, null, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id2", 
                        null, null, null, null, true, null, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id6", 
                        null, null, null, null, true, null, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id7", 
                        null, null, null, null, true, null, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id8", 
                        null, null, null, null, true, null, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id18",
                        null, null, null, null, true, null, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id10", 
                        null, null, null, null, true, null, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id12",
                        null, null, null, null, true, null, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id13",
                        null, null, null, null, true, null, null, null, null));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
    }

    /**
     * Test the select method {@link MySQLExpressionCallDAO#getExpressionCalls(ExpressionCallParams)} 
     * when including data from sub-stages.
     */
    @Test
    @Deprecated
    public void shouldGetExpressionCallsIncludeSubStagesDeprecated() throws SQLException {
        
        this.useSelectDB();

        //to test the LIMIT feature used when propagating expression calls on-the-fly, 
        //we change the gene count limit from the properties.
        Properties newProps = DAOManager.getDefaultProperties();
        newProps.setProperty(MySQLDAOManager.EXPR_PROPAGATION_GENE_COUNT_KEY, "2");
        try {
        MySQLDAOManager manager = this.getMySQLDAOManager(newProps);
        
        MySQLExpressionCallDAO dao = new MySQLExpressionCallDAO(manager);

        // On expression table 
        // Without speciesIds and not include organ substructures
        // Generate parameters
        Set<String> speciesIds = new HashSet<String>();
        ExpressionCallParams params = new ExpressionCallParams();
        params.addAllSpeciesIds(speciesIds);
        params.setIncludeSubstructures(false);
        params.setIncludeSubStages(true);
        // Generate manually expected result
        List<ExpressionCallTO> expectedExprCalls = Arrays.asList(
                new ExpressionCallTO("ID1__Anat_id1__Stage_id1", "ID1", "Anat_id1", "Stage_id1",
                        DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, 
                        DataState.NODATA, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id1", "ID1", "Anat_id6", "Stage_id1",
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id5", "ID1", "Anat_id6", "Stage_id5", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id6", "ID1", "Anat_id6", "Stage_id6", 
                        DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id7", "ID1", "Anat_id6", "Stage_id7", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.BOTH, true),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id8", "ID1", "Anat_id6", "Stage_id8", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                        DataState.NODATA, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id1", "ID1", "Anat_id7", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id10", "ID1", "Anat_id7", "Stage_id10", 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID2__Anat_id1__Stage_id1", "ID2", "Anat_id1", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false), 
                new ExpressionCallTO("ID2__Anat_id1__Stage_id2", "ID2", "Anat_id1", "Stage_id2", 
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.NODATA, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true), 
                new ExpressionCallTO("ID2__Anat_id1__Stage_id5", "ID2", "Anat_id1", "Stage_id5", 
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false), 
                new ExpressionCallTO("ID2__Anat_id1__Stage_id7", "ID2", "Anat_id1", "Stage_id7", 
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false), 
                new ExpressionCallTO("ID2__Anat_id1__Stage_id9", "ID2", "Anat_id1", "Stage_id9", 
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true), 
                new ExpressionCallTO("ID2__Anat_id2__Stage_id1", "ID2", "Anat_id2", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id2__Stage_id14", "ID2", "Anat_id2", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id2__Stage_id18", "ID2", "Anat_id2", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id1", "ID2", "Anat_id11", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id10", "ID2", "Anat_id11", "Stage_id10", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id12", "ID2", "Anat_id11", "Stage_id12", 
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id13", "ID2", "Anat_id11", "Stage_id13", 
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.NODATA, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID3__Anat_id1__Stage_id1", "ID3", "Anat_id1", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID3__Anat_id3__Stage_id1", "ID3", "Anat_id3", "Stage_id1", 
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.NODATA, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true));
        // Compare
        MySQLExpressionCallTOResultSet rs = 
                (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        List<ExpressionCallTO> expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected " + expectedExprCalls + 
                ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertTrue("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());

        // With speciesIds but not include substructures 
        // Generate parameters
        params.addAllSpeciesIds(Arrays.asList("11", "41")); // 41 = species Id that does not exist
        // Generate manually expected result
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO("ID1__Anat_id1__Stage_id1", "ID1", "Anat_id1", "Stage_id1",
                        DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, 
                        DataState.NODATA, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id6","ID1", "Anat_id6", "Stage_id6", 
                        DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id5","ID1", "Anat_id6", "Stage_id5", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id1","ID1", "Anat_id6", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id7","ID1", "Anat_id6", "Stage_id7", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, 
                        false, true, OriginOfLine.SELF, OriginOfLine.BOTH, true),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id8","ID1", "Anat_id6", "Stage_id8", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id10","ID1", "Anat_id7", "Stage_id10", 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id1","ID1", "Anat_id7", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false)); 
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls + 
                ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertTrue("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());

        // On global expression table 
        // With speciesIds and include substructures 
        // Generate parameters
        params.setIncludeSubstructures(true);
        params.clearSpeciesIds();
        params.addAllSpeciesIds(Arrays.asList("11", "21", "41")); // 41 = species Id that does not exist
        // Generate manually expected result
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO("ID1__Anat_id6__Stage_id6", "ID1", "Anat_id6", "Stage_id6", 
                        DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, true, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id5", "ID1", "Anat_id6", "Stage_id5", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, true, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id1", "ID1", "Anat_id6", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, true, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id7", "ID1", "Anat_id6", "Stage_id7", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, 
                        true, true, OriginOfLine.SELF, OriginOfLine.BOTH, true),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id8", "ID1", "Anat_id6", "Stage_id8", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        true, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id8", "ID1", "Anat_id1", "Stage_id8", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        true, true, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id7", "ID1", "Anat_id1", "Stage_id7", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        true, true, OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id5", "ID1", "Anat_id1", "Stage_id5", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        true, true, OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id1", "ID1", "Anat_id1", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        true, true, OriginOfLine.BOTH, OriginOfLine.BOTH, true),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id10", "ID1", "Anat_id7", "Stage_id10", 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, true, true, OriginOfLine.BOTH, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id1", "ID1", "Anat_id7", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, true, true, OriginOfLine.BOTH, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id1__Stage_id1", "ID2", "Anat_id1", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.HIGHQUALITY, true, true,
                        OriginOfLine.BOTH, OriginOfLine.BOTH, false), 
                new ExpressionCallTO("ID2__Anat_id1__Stage_id2", "ID2", "Anat_id1", "Stage_id2",
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.NODATA, true, true, 
                        OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id1", "ID2", "Anat_id11", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.BOTH, OriginOfLine.DESCENT, false), 
                new ExpressionCallTO("ID2__Anat_id11__Stage_id10", "ID2", "Anat_id11", "Stage_id10", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id12", "ID2", "Anat_id11", "Stage_id12",
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id13", "ID2", "Anat_id11", "Stage_id13", 
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.NODATA, true, true, 
                        OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id14", "ID2", "Anat_id11", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false), 
                new ExpressionCallTO("ID2__Anat_id11__Stage_id18", "ID2", "Anat_id11", "Stage_id18",
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.SELF, false), 
                new ExpressionCallTO("ID2__Anat_id2__Stage_id1", "ID2", "Anat_id2", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id2__Stage_id14", "ID2", "Anat_id2", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id2__Stage_id18", "ID2", "Anat_id2", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID2__Anat_id3__Stage_id18", "ID2", "Anat_id3", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("ID2__Anat_id3__Stage_id14", "ID2", "Anat_id3", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true,
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id3__Stage_id1", "ID2", "Anat_id3", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id4__Stage_id18", "ID2", "Anat_id4", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("ID2__Anat_id4__Stage_id14", "ID2", "Anat_id4", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id4__Stage_id1", "ID2", "Anat_id4", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true,
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id5__Stage_id18", "ID2", "Anat_id5", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("ID2__Anat_id5__Stage_id14", "ID2", "Anat_id5", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id5__Stage_id1", "ID2", "Anat_id5", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id9__Stage_id18", "ID2", "Anat_id9", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("ID2__Anat_id9__Stage_id14", "ID2", "Anat_id9", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id9__Stage_id1", "ID2", "Anat_id9", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id10__Stage_id18", "ID2", "Anat_id10", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("ID2__Anat_id10__Stage_id14", "ID2", "Anat_id10", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id10__Stage_id1", "ID2", "Anat_id10", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls + 
                ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertTrue("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
        
        // Test get only GENE_ID without species filter and without including substructures, 
        // but with including sub-stages
        dao.clearAttributes();
        dao.setAttributes(Arrays.asList(ExpressionCallDAO.Attribute.GENE_ID));
        params.clearSpeciesIds();
        params.setIncludeSubstructures(false);
        params.setIncludeSubStages(true);
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO(null, "ID1", null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO(null, "ID2", null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO(null, "ID3", null, null, null, null, null, null, null, null, null, null, null));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertTrue("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());

        // Test get only INCLUDE_SUBSTRUCTURES without species filter and without including substructures, 
        // but with including sub-stages
        dao.clearAttributes();
        dao.setAttributes(Arrays.asList(ExpressionCallDAO.Attribute.INCLUDE_SUBSTRUCTURES));
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO(null, null, null, null, null, null, null, null, false, null, null, null, null));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertTrue("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertTrue("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
        
       // Test get only INCLUDE_SUBSTAGES without species filter and without including substructures, 
        // but with including sub-stages
        dao.clearAttributes();
        dao.setAttributes(Arrays.asList(ExpressionCallDAO.Attribute.INCLUDE_SUBSTAGES));
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO(null, null, null, null, null, null, null, null, null, true, null, null, null));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertTrue("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertTrue("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());

        // Test get only ID with species filter and including substructures and sub-stages
        dao.clearAttributes();
        dao.setAttributes(Arrays.asList(ExpressionCallDAO.Attribute.ID));
        params = new ExpressionCallParams();
        params.addAllSpeciesIds(Arrays.asList("11", "41")); // 41 = species Id that does not exist
        params.setIncludeSubstructures(true);
        params.setIncludeSubStages(true);
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO("ID1__Anat_id6__Stage_id1", null, null, null, null, null, 
                        null, null, null, null, null, null, null),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id5", null, null, null, null, null, 
                        null, null, null, null, null, null, null),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id6", null, null, null, null, null, 
                        null, null, null, null, null, null, null),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id7", null, null, null, null, null, 
                        null, null, null, null, null, null, null),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id8", null, null, null, null, null, 
                        null, null, null, null, null, null, null),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id1", null, null, null, null, null, 
                        null, null, null, null, null, null, null),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id5", null, null, null, null, null, 
                        null, null, null, null, null, null, null),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id7", null, null, null, null, null, 
                        null, null, null, null, null, null, null),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id8", null, null, null, null, null, 
                        null, null, null, null, null, null, null),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id1", null, null, null, null, null, 
                        null, null, null, null, null, null, null),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id10", null, null, null, null, null, 
                        null, null, null, null, null, null, null));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls + 
                ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertTrue("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
        
        // Test get INCLUDE_SUBSTAGES (and STAGE_ID) without OriginOfLine including sub-stages
        dao.clearAttributes();
        dao.setAttributes(
                ExpressionCallDAO.Attribute.STAGE_ID, ExpressionCallDAO.Attribute.INCLUDE_SUBSTAGES);
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO(null, null, null, "Stage_id1", 
                null, null, null, null, null, true, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id5", 
                        null, null, null, null, null, true, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id6", 
                        null, null, null, null, null, true, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id7", 
                        null, null, null, null, null, true, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id8",
                        null, null, null, null, null, true, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id10", 
                        null, null, null, null, null, true, null, null, null));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertTrue("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertTrue("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
        } finally {
            //restore default parameters
            this.getMySQLDAOManager(DAOManager.getDefaultProperties());
        }
    }
    
    /**
     * Test the get max method {@link MySQLExpressionCallDAO#getMaxExpressionCallId()}.
     */
    @Test
    public void shouldGetMaxExpressionCallId() throws SQLException {

        // Check on database with calls
        this.useSelectDB();

        MySQLExpressionCallDAO dao = new MySQLExpressionCallDAO(this.getMySQLDAOManager());

        // Generate manually expected result for expression table
        assertEquals("Max expression ID incorrectly retrieved", 12, 
                dao.getMaxExpressionCallId(false));

        // Generate manually expected result for global expression table
        assertEquals("Max expression ID incorrectly retrieved", 26, 
                dao.getMaxExpressionCallId(true));

        // Check on database without calls
        this.useEmptyDB();
        
        try {
            // Generate manually expected result for expression table
            assertEquals("Max expression ID incorrectly retrieved", 0, 
                    dao.getMaxExpressionCallId(false));

            // Generate manually expected result for global expression table
            assertEquals("Max expression ID incorrectly retrieved", 0, 
                    dao.getMaxExpressionCallId(true));
        } finally {
            this.emptyAndUseDefaultDB();
        }
    }

    /**
     * Test the select method {@link MySQLExpressionCallDAO#insertExpressionCalls()}.
     */
    @Test
    public void shouldInsertExpressionCalls() throws SQLException {
        
        this.useEmptyDB();
        //create a Collection of ExpressionCallTO to be inserted
        Collection<ExpressionCallTO> exprCallTOs = Arrays.asList(
                new ExpressionCallTO("1", "ID3", "Anat_id1", "Stage_id1", DataState.LOWQUALITY,
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("7", "ID2", "Anat_id11", "Stage_id13", DataState.NODATA,
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("16", "ID2", "Anat_id3", "Stage_id18", DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("19", "ID2", "Anat_id10", "Stage_id18", DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("20", "ID2", "Anat_id8", "Stage_id18", DataState.NODATA, 
                        DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        true, false, OriginOfLine.BOTH, OriginOfLine.SELF, false),
                new ExpressionCallTO("21", "ID2", "Anat_id11", "Stage_id18", DataState.LOWQUALITY, 
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false));

        try {
            MySQLExpressionCallDAO dao = new MySQLExpressionCallDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows inserted", 6, 
                    dao.insertExpressionCalls(exprCallTOs));
            
            //we manually verify the insertion, as we do not want to rely on other methods 
            //that are tested elsewhere.
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from expression where " +
                      "expressionId = ? and geneId = ? and anatEntityId = ? and stageId = ? " +
                      "and estData = ? and affymetrixData = ? and inSituData = ? and rnaSeqData = ?")) {
                
                stmt.setString(1, "1");
                stmt.setString(2, "ID3");
                stmt.setString(3, "Anat_id1");
                stmt.setString(4, "Stage_id1");
                stmt.setString(5, "no data");
                stmt.setString(6, "poor quality");
                stmt.setString(7, "high quality");
                stmt.setString(8, "high quality");
                assertTrue("ExpressionCallTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setString(1, "7");
                stmt.setString(2, "ID2");
                stmt.setString(3, "Anat_id11");
                stmt.setString(4, "Stage_id13");
                stmt.setString(5, "high quality");
                stmt.setString(6, "no data");
                stmt.setString(7, "poor quality");
                stmt.setString(8, "no data");
                assertTrue("ExpressionCallTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setString(1, "16");
                stmt.setString(2, "ID2");
                stmt.setString(3, "Anat_id3");
                stmt.setString(4, "Stage_id18");
                stmt.setString(5, "high quality");
                stmt.setString(6, "high quality");
                stmt.setString(7, "high quality");
                stmt.setString(8, "high quality");
                assertTrue("ExpressionCallTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }

            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from globalExpression where " +
                      "globalExpressionId = ? and geneId = ? and anatEntityId = ? and stageId = ? " +
                      "and estData = ? and affymetrixData = ? and inSituData = ? and rnaSeqData = ? " +
                      "and originOfLine = ?")) {
                stmt.setString(1, "19");
                stmt.setString(2, "ID2");
                stmt.setString(3, "Anat_id10");
                stmt.setString(4, "Stage_id18");
                stmt.setString(5, "high quality");
                stmt.setString(6, "high quality");
                stmt.setString(7, "high quality");
                stmt.setString(8, "high quality");
                stmt.setString(9, "self");
                assertTrue("ExpressionCallTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());

                stmt.setString(1, "20");
                stmt.setString(2, "ID2");
                stmt.setString(3, "Anat_id8");
                stmt.setString(4, "Stage_id18");
                stmt.setString(5, "poor quality");
                stmt.setString(6, "no data");
                stmt.setString(7, "high quality");
                stmt.setString(8, "high quality");
                stmt.setString(9, "both");
                assertTrue("ExpressionCallTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());

                stmt.setString(1, "21");
                stmt.setString(2, "ID2");
                stmt.setString(3, "Anat_id11");
                stmt.setString(4, "Stage_id18");
                stmt.setString(5, "no data");
                stmt.setString(6, "poor quality");
                stmt.setString(7, "high quality");
                stmt.setString(8, "poor quality");
                stmt.setString(9, "descent");
                assertTrue("ExpressionCallTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
            
            this.thrown.expect(IllegalArgumentException.class);
            dao.insertExpressionCalls(new HashSet<ExpressionCallTO>());
        } finally {
            this.emptyAndUseDefaultDB();
        }
    }   

    /**
     * Test the select method {@link MySQLExpressionCallDAO#insertGlobalExpressionToExpression()}.
     */
    @Test
    public void shouldInsertGlobalExpressionToExpression() throws SQLException {
        
        this.useEmptyDB();

        //create a Collection of ExpressionCallTO to be inserted
        Collection<GlobalExpressionToExpressionTO> globalExprToExprTOs = Arrays.asList(
                new GlobalExpressionToExpressionTO("1","10"),
                new GlobalExpressionToExpressionTO("1","1"),
                new GlobalExpressionToExpressionTO("2","14"));

        try {
            MySQLExpressionCallDAO dao = new MySQLExpressionCallDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows inserted", 3, 
                    dao.insertGlobalExpressionToExpression(globalExprToExprTOs));
            
            //we manually verify the insertion, as we do not want to rely on other methods 
            //that are tested elsewhere.
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from globalExpressionToExpression where " +
                      "expressionId = ? and globalExpressionId = ?")) {
                
                stmt.setString(1, "1");
                stmt.setString(2, "10");
                assertTrue("GlobalExpressionToExpressionTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setString(1, "1");
                stmt.setString(2, "1");
                assertTrue("GlobalExpressionToExpressionTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setString(1, "2");
                stmt.setString(2, "14");
                assertTrue("GlobalExpressionToExpressionTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
            
            this.thrown.expect(IllegalArgumentException.class);
            dao.insertGlobalExpressionToExpression(new HashSet<GlobalExpressionToExpressionTO>());
        } finally {
            this.emptyAndUseDefaultDB();
        }
    }    
}
