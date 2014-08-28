package org.bgee.model.dao.mysql.expressiondata;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.api.expressiondata.CallTO.DataState;
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

        // Generate result with the method
        MySQLExpressionCallDAO dao = new MySQLExpressionCallDAO(this.getMySQLDAOManager());
        dao.setAttributes(Arrays.asList(ExpressionCallDAO.Attribute.values()));

        ExpressionCallParams params = new ExpressionCallParams();
        //TODO this.referenceCallTO = callTO;
        
        params.addAllGeneIds(Arrays.asList("ID1","ID2"));
        params.addAllAnatEntityIds(Arrays.asList("Anat_id4","Anat_id6","Anat_id2"));
        params.addAllDevStageIds(Arrays.asList("Stage_id7","Stage_id18"));
        params.addAllSpeciesIds(Arrays.asList("21","31"));
        params.setAllDataTypes(false);
        params.setAffymetrixData(DataState.HIGHQUALITY);
        params.setESTData(DataState.NODATA);
        params.setInSituData(DataState.NODATA);
        params.setRNASeqData(DataState.NODATA);
        params.setIncludeSubStages(true);
        params.setIncludeSubstructures(true);
        
        ExpressionCallTOResultSet methResults = dao.getAllExpressionCalls(params);
                
        //TODO Generate manually expected result
        List<ExpressionCallTO> expectedExprCalls = Arrays.asList(
                new ExpressionCallTO("ID1", "Anat_id6", "Stage_id7", DataState.NODATA,
                        DataState.NODATA, DataState.NODATA, DataState.NODATA, 
                        DataState.LOWQUALITY, false, false)); 

        while (methResults.next()) {
            boolean found = false;
            ExpressionCallTO methExprCall = methResults.getTO();
            //TODO Uncomment when expected result will be generated
//            for (ExpressionCallTO expExprCall: expectedExprCalls) {
//                log.trace("Comparing {} to {}", methExprCall, expExprCall);
//                if (TOComparator.areExpressionCallTOsEqual(methExprCall, expExprCall)) {
//                    found = true;
//                    break;
//                }
//            }
//            if (!found) {
//                log.debug("No equivalent gene found for {}", methExprCall);
//                throw log.throwing(new AssertionError("Incorrect generated TO"));
//            }
        }
        methResults.close();
        log.exit();
    }
}
