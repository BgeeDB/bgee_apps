package org.bgee.pipeline.expression;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallParams;
import org.bgee.model.dao.api.ontologycommon.RelationDAO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.pipeline.BgeeDBUtils;
import org.bgee.pipeline.MySQLDAOUser;

/**
 * Class used to delete or update conflicting no-expression calls in Bgee and associated 
 * raw data.
 * <p> 
 * A no-expression call is considered to be conflicting when there exists 
 * an expression call for the same gene, in the same anatomical structure/developmental 
 * stage, or in a child anatomical structure/developmental stage. 
 * <p>
 * i) If all the data types are conflicting, the no-expression call is deleted: for instance, 
 * a no-expression call produced by Affymetrix data, while there exists a corresponding 
 * expression call produced by techniques including Affymetrix data; ii) If only 
 * some data types are conflicting, the no-expression call is updated to set the corresponding 
 * data types to 'no data': for instance, if a no-expression call was produced 
 * by Affymetrix data and RNA-Seq data, while there exists a corresponding 
 * expression call produced by Affymetrix data, the no-expression call will be updated 
 * to keep unchanged the RNA-Seq data type, but to change the Affymetrix data type to 'no data'.
 * <p>
 * Corresponding raw data are also updated to set to null their no-expression ID 
 * and to set their reason for exclusion to 'no-expression conflict'.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class FilterNoExprCalls extends MySQLDAOUser {
    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(FilterNoExprCalls.class.getName());
    
    /**
     * Default constructor using default {@code MySQLDAOManager}.
     */
    public FilterNoExprCalls() {
        this(null);
    }
    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by 
     * this object to perform queries to the database.
     * 
     * @param manager   the {@code MySQLDAOManager} to use.
     */
    public FilterNoExprCalls(MySQLDAOManager manager) {
        super(manager);
    }
    
    public void filterNoExpressionCalls(List<String> speciesIds, boolean globalNoExpression) 
        throws IllegalArgumentException{
        log.entry(speciesIds, globalNoExpression);
        
        List<String> speciesIdsToUse = BgeeDBUtils.checkAndGetSpeciesIds(speciesIds, 
                this.getSpeciesDAO());
        
        for (String speciesId: speciesIdsToUse) {
            Set<String> speciesFilter = new HashSet<String>();
            speciesFilter.add(speciesId);
            
            //get the reflexive/direct/indirect is_a/part_of relations between stages 
            //and between anat entities for this species
            this.getRelationDAO().setAttributes(RelationDAO.Attribute.SOURCEID, 
                    RelationDAO.Attribute.TARGETID);
            List<RelationTO> anatEntityRelTOs = this.getRelationDAO().getAnatEntityRelations(
                    speciesFilter, EnumSet.of(RelationType.ISA_PARTOF), null).getAllTOs();
            List<RelationTO> stageRelTOs = this.getRelationDAO().getStageRelations(
                    speciesFilter, null).getAllTOs();
            
            //get the global expression calls for this species
            ExpressionCallParams params = new ExpressionCallParams();
            params.addAllSpeciesIds(speciesIds);
            params.setIncludeSubstructures(true);
            List<ExpressionCallTO> exprTOs = 
                    this.getExpressionCallDAO().getExpressionCalls(params).getAllTOs();
            
            //now, the no-expression calls, global or not depending on argument
            NoExpressionCallParams noExprParams = new NoExpressionCallParams();
            noExprParams.addAllSpeciesIds(speciesIds);
            noExprParams.setIncludeParentStructures(globalNoExpression);
            List<NoExpressionCallTO> noExprTOs = 
                    this.getNoExpressionCallDAO().getNoExpressionCalls(noExprParams).getAllTOs();
        }
        
        log.exit();
    }
}
