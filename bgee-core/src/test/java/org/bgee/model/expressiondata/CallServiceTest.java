package org.bgee.model.expressiondata;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;
import org.bgee.model.dao.api.expressiondata.CallDAOFilter;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTOResultSet;
import org.bgee.model.dao.api.file.DownloadFileDAO.DownloadFileTO;
import org.bgee.model.dao.api.file.DownloadFileDAO.DownloadFileTOResultSet;
import org.junit.Test;

/**
 * Unit tests for {@link CallService}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Nov. 2015
 * @since Bgee 13 Nov. 2015
 */
public class CallServiceTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(CallServiceTest.class.getName());
    @Override
    protected Logger getLogger() {
        return log;
    } 
    
    /**
     * Test the method {@link CallService#loadExpressionCalls(String, ExpressionCallFilter, 
     * Collection, LinkedHashMap)}.
     */
    @Test
    public void shoudLoadExpressionCalls() {
        //First test for one gene, no substructures no sub-stages. 
        //Retrieving geneId, anatEntityId, stageId, and data qualities, ordered by mean rank. 
        DAOManager manager = mock(DAOManager.class);
        ExpressionCallDAO dao = mock(ExpressionCallDAO.class);
        when(manager.getExpressionCallDAO()).thenReturn(dao);
        
        LinkedHashMap<ExpressionCallDAO.OrderingAttribute, DAO.Direction> orderingAttrs = 
                new LinkedHashMap<>();
        orderingAttrs.put(ExpressionCallDAO.OrderingAttribute.MEAN_RANK, DAO.Direction.DESC);
        
        ExpressionCallTOResultSet resultSetMock = getMockResultSet(ExpressionCallTOResultSet.class, 
                Arrays.asList(
                    new ExpressionCallTO(null, "geneId1", "anatEntityId1", "stageId1", 
                        null, CallTO.DataState.LOWQUALITY, null, CallTO.DataState.HIGHQUALITY, 
                        null, CallTO.DataState.LOWQUALITY, null, CallTO.DataState.LOWQUALITY, null, 
                        null, null, null, null, null), 
                    new ExpressionCallTO(null, "geneId1", "anatEntityId1", "stageId2", 
                            null, CallTO.DataState.LOWQUALITY, null, CallTO.DataState.HIGHQUALITY, 
                            null, CallTO.DataState.LOWQUALITY, null, CallTO.DataState.LOWQUALITY, null, 
                            null, null, null, null, null)));
        
        when(dao.getExpressionCalls(
                //CallDAOFilters
                collectionEq(Arrays.asList(
                    new CallDAOFilter(null, Arrays.asList("speciesId1"), null))
                ), 
                //CallTOs
                collectionEq(new HashSet<ExpressionCallTO>()),
                //propagation
                eq(false), eq(false), 
                //genes
                eq(Arrays.asList("geneId1")), 
                //orthology
                eq(null), 
                //attributes
                collectionEq(EnumSet.allOf(ExpressionCallDAO.Attribute.class).stream()
                        .filter(attr -> attr != ExpressionCallDAO.Attribute.ID && 
                                        !attr.isPropagationAttribute() && 
                                        !attr.isRankAttribute())
                        .collect(Collectors.toSet())), 
                eq(orderingAttrs)))
        
        .thenReturn(resultSetMock);
    }
}
