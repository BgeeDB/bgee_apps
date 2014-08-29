package org.bgee.model.dao.mysql.expressiondata;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTOResultSet;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.junit.Test;


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

    /**
     * Test the select method {@link MySQLExpressionCallDAO#getAllExpressionCalls()}.
     */
    @Test
    public void shouldGetAllExpression() throws SQLException {
        log.entry();
        
        this.useSelectDB();

        MySQLExpressionCallDAO dao = new MySQLExpressionCallDAO(this.getMySQLDAOManager());
        dao.setAttributes(Arrays.asList(ExpressionCallDAO.Attribute.values()));

        dao.setAttributes(Arrays.asList(ExpressionCallDAO.Attribute.ID, 
                ExpressionCallDAO.Attribute.GENEID, 
                ExpressionCallDAO.Attribute.DEVSTAGEID, 
                ExpressionCallDAO.Attribute.ANATENTITYID, 
                ExpressionCallDAO.Attribute.AFFYMETRIXDATA, 
                ExpressionCallDAO.Attribute.ESTDATA, 
                ExpressionCallDAO.Attribute.INSITUDATA,
                // Remove RELAXEDINSITUDATA because not already in database
                //ExpressionCallDAO.Attribute.RELAXEDINSITUDATA, 
                ExpressionCallDAO.Attribute.RNASEQDATA
                // Remove INCLUDESUBSTRUCTURES and INCLUDESUBSTAGES because not data from DB
                //ExpressionCallDAO.Attribute.INCLUDESUBSTRUCTURES, 
                //ExpressionCallDAO.Attribute.INCLUDESUBSTAGES,
                // Remove ORIGINOFLINE because we test get expression call on expression table
                //ExpressionCallDAO.Attribute.ORIGINOFLINE
                ));
        // Generate result with the method
        Set<String> speciesId = new HashSet<String>();
        ExpressionCallTOResultSet methResults = dao.getAllExpressionCalls(speciesId);

        // Generate manually expected result
        List<ExpressionCallTO> expectedExprCalls = Arrays.asList(
                new ExpressionCallTO("1","ID3", "Anat_id1", "Stage_id1", DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, DataState.HIGHQUALITY, false, false),
                new ExpressionCallTO("2","ID1", "Anat_id6", "Stage_id6", DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, false, false),
                new ExpressionCallTO("3","ID1", "Anat_id6", "Stage_id7", DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, false, false),
                new ExpressionCallTO("4","ID2", "Anat_id2", "Stage_id18", DataState.HIGHQUALITY,DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.HIGHQUALITY, false, false),
                new ExpressionCallTO("5","ID1", "Anat_id7", "Stage_id10", DataState.LOWQUALITY,DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.NODATA, DataState.LOWQUALITY, false, false),
                new ExpressionCallTO("6","ID2", "Anat_id11", "Stage_id12",DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, DataState.NODATA, DataState.HIGHQUALITY, false, false),
                new ExpressionCallTO("7","ID2", "Anat_id11", "Stage_id13",DataState.NODATA,DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, DataState.NODATA, false, false),
                new ExpressionCallTO("8","ID3", "Anat_id3", "Stage_id1", DataState.NODATA, DataState.HIGHQUALITY,DataState.LOWQUALITY, DataState.NODATA, DataState.NODATA, false, false),
                new ExpressionCallTO("9","ID2", "Anat_id1", "Stage_id9", DataState.HIGHQUALITY, DataState.LOWQUALITY,DataState.NODATA, DataState.NODATA, DataState.HIGHQUALITY, false, false)); 
        
        while (methResults.next()) {
            boolean found = false;
            ExpressionCallTO methExprCall = methResults.getTO();
            for (ExpressionCallTO expExprCall: expectedExprCalls) {
                log.trace("Comparing {} to {}", methExprCall.getId(), expExprCall.getId());
                if (TOComparator.areExpressionCallTOsEqual(methExprCall, expExprCall)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                log.debug("No equivalent expressionTO found for {}", methExprCall.toString());
                throw log.throwing(new AssertionError("Incorrect generated TO"));
            }
        }
        methResults.close();

        speciesId.add("11");
        speciesId.add("41");    // species that do not exist
        methResults = dao.getAllExpressionCalls(speciesId);

        //Generate manually expected result
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO("2","ID1", "Anat_id6", "Stage_id6", DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, false, false),
                new ExpressionCallTO("3","ID1", "Anat_id6", "Stage_id7", DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, false, false),
                new ExpressionCallTO("5","ID1", "Anat_id7", "Stage_id10", DataState.LOWQUALITY,DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.NODATA, DataState.LOWQUALITY, false, false)); 

        while (methResults.next()) {
            boolean found = false;
            ExpressionCallTO methExprCall = methResults.getTO();
            for (ExpressionCallTO expExprCall: expectedExprCalls) {
                log.trace("Comparing {} to {}", methExprCall.getId(), expExprCall.getId());
                if (TOComparator.areExpressionCallTOsEqual(methExprCall, expExprCall)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                log.debug("No equivalent expressionTO found for {}", methExprCall.toString());
                throw log.throwing(new AssertionError("Incorrect generated TO"));
            }
        }
        methResults.close();
    }
}
