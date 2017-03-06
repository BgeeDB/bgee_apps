package org.bgee.pipeline.expression;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.pipeline.BgeeDBUtils;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.expression.CallUser;

/**
 * Compare time execution to get expression calls on-the-fly or with SQL query.
 * 
 * @author Valentine Rech de Laval
 */
//FIXME: to remove?
public class ExecutionTime extends CallUser {
//
//    /**
//     * {@code Logger} of the class. 
//     */
//    private final static Logger log = LogManager.getLogger(ExecutionTime.class.getName());
//
//    /**
//     * Default constructor. 
//     */
//    public ExecutionTime() {
//        this(null);
//    }
//
//    /**
//     * Constructor providing the {@code MySQLDAOManager} that will be used by 
//     * this object to perform queries to the database. This is useful for unit testing.
//     * 
//     * @param manager   the {@code MySQLDAOManager} to use.
//     */
//    public ExecutionTime(MySQLDAOManager manager) {
//        super(manager);
//    }
//
//    /**
//     * Main method to compare execution times to get expression calls using SQL query or generating 
//     * them on-the-fly. Parameter that must be provided in {@code args} is the list of NCBI 
//     * species IDs (for instance, {@code 9606} for human) that will be used to compare methods. 
//     * 
//     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
//     */
//    public static void main(String[] args) {
//        log.entry((Object[]) args);
//
//        int expectedArgLengthSingleSpecies = 1; // species list
//        if (args.length != expectedArgLengthSingleSpecies) {
//            throw log.throwing(new IllegalArgumentException(
//                    "Incorrect number of arguments provided, expected " + 
//                    expectedArgLengthSingleSpecies + " arguments, " + args.length + " provided."));
//        }
//
//        List<Integer> speciesIds = CommandRunner.parseListArgumentAsInt(args[0]);
//
//        ExecutionTime myApp = new ExecutionTime();
//        myApp.calculateExecutionTime(speciesIds);
//
//        log.exit();
//    }
//
//    private void calculateExecutionTime (List<Integer> speciesIds) {
//        log.entry(speciesIds);
//
//        Set<Integer> setSpecies = new HashSet<>(
//                BgeeDBUtils.checkAndGetSpeciesIds(speciesIds, this.getSpeciesDAO()));
//
////        // No propagation
////        long startTime = System.nanoTime();
////        this.getExpressionCallsBySql(setSpecies, false, false);
////        long endTime = System.nanoTime();
////        long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
////        log.trace("Get expression without propagation took: {} ms", duration / 1000000);
////
////
//        // SQL propagation of substructures only
//        long startTime = System.nanoTime();
//        Collection<ExpressionCallTO> sqlResult = 
//                this.getExpressionCallsBySql(setSpecies, true, false);
//        long endTime = System.nanoTime();
//        long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
//        log.info("Get global expression calls (globlalExpression table) took: {} ms",
//                duration / 1000000);
//        // On-the-fly propagation of substructures only
//        startTime = System.nanoTime();
//        Collection<ExpressionCallTO> onTheFlyResult = 
//                this.getExpressionCallsByApp(setSpecies, true, false);
//        endTime = System.nanoTime();
//        duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
//        log.info("Get basic expression calls then propagate to substructures on-the-fly took: {} ms", 
//                duration / 1000000);
//
//        if (!TOComparator.areTOCollectionsEqual(sqlResult, onTheFlyResult, false)) {
//            throw log.throwing(new IllegalStateException("Collection are not equals: sql result: " + 
//                    sqlResult + "on-the-fly result: " + onTheFlyResult));
//        }
//
////        // SQL propagation of substages only
////        startTime = System.nanoTime();
////        sqlResult = this.getExpressionCallsBySql(setSpecies, false, true);
////        endTime = System.nanoTime();
////        duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
////        log.info("Get expression with substage SQL propagation only took: {} ms", 
////                duration / 1000000);
////        // On-the-fly propagation of substages only
////        startTime = System.nanoTime();
////        onTheFlyResult = this.getExpressionCallsByApp(setSpecies, false, true);
////        endTime = System.nanoTime();
////        duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
////        log.info("Get expression with substage on-the-fly propagation took: {} ms", 
////                duration / 1000000);
////
////        if (TOComparator.areTOCollectionsEqual(sqlResult, onTheFlyResult, false)) {
////            throw log.throwing(new IllegalStateException("Collection are not equals: sql result: " + 
////                    sqlResult + "on-the-fly result: " + onTheFlyResult));
////        }
//
//        // SQL propagation of substructures and substages
////        startTime = System.nanoTime();
////        sqlResult = this.getExpressionCallsBySql(setSpecies, true, true);
////        endTime = System.nanoTime();
////        duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
////        log.info("Get expression with substructure and substage SQL propagation took: {} ms",
////                duration / 1000000);
//        // On-the-fly propagation of substructures and substages
//        startTime = System.nanoTime();
//        onTheFlyResult = this.getExpressionCallsByApp(setSpecies, true, true);
//        endTime = System.nanoTime();
//        duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
//        log.info("Get basic expression calls then propagate to substructures and substages on-the-fly took: {} ms", 
//                duration / 1000000);
//
////        if (TOComparator.areTOCollectionsEqual(sqlResult, onTheFlyResult, false)) {
////            throw log.throwing(new IllegalStateException("Collection are not equals: sql result: " + 
////                    sqlResult + "on-the-fly result: " + onTheFlyResult));
////        }
//
//        log.exit();
//    }
//
//    private Collection<ExpressionCallTO> getExpressionCallsByApp(Set<Integer> speciesIds, 
//            boolean isIncludeSubstructures, boolean isIncludeSubStages) {
//        log.entry(speciesIds, isIncludeSubstructures, isIncludeSubStages);
//
//        LinkedHashMap<String, List<ExpressionCallTO>> exprTOs = 
//                this.getExpressionCallsByGeneId(speciesIds);
//
//        Map<String, Set<String>> anatEntityParentsFromChildren = null;
//        Map<String, Set<String>> stageParentsFromChildren = null;
//
//        if (isIncludeSubstructures) {
//            anatEntityParentsFromChildren = BgeeDBUtils.getAnatEntityParentsFromChildren(
//                    speciesIds, this.getRelationDAO());
//        } 
//        if (isIncludeSubStages) {
//            stageParentsFromChildren = BgeeDBUtils.getStageParentsFromChildren(
//                    speciesIds, this.getRelationDAO());
//        }
//
//        Set<ExpressionCallTO> returnExprTOs = new HashSet<ExpressionCallTO>();
//
//        Iterator<Entry<String, List<ExpressionCallTO>>> exprTOIterator = 
//                exprTOs.entrySet().iterator();
//        while (exprTOIterator.hasNext()) {
//            Entry<String, List<ExpressionCallTO>> entry = exprTOIterator.next();
//
//            if (isIncludeSubstructures && isIncludeSubStages) {
//                Map<ExpressionCallTO, Set<String>> exprCallPropagatedInAnatEntityParents = 
//                        this.updateGlobalExpressions(
//                                this.groupExpressionCallTOsByPropagatedCalls(
//                                        entry.getValue(), anatEntityParentsFromChildren, true),
//                                        true, false);
//                returnExprTOs = this.updateGlobalExpressions(
//                        this.groupExpressionCallTOsByPropagatedCalls(
//                                exprCallPropagatedInAnatEntityParents.keySet(), 
//                                stageParentsFromChildren, false),
//                                false, true).keySet();
//            } else if (isIncludeSubstructures) {
//                returnExprTOs = this.updateGlobalExpressions(
//                        this.groupExpressionCallTOsByPropagatedCalls(
//                                entry.getValue(), anatEntityParentsFromChildren, true),
//                                true, false).keySet();
//            } else if (isIncludeSubStages) {
//                returnExprTOs = this.updateGlobalExpressions(
//                        this.groupExpressionCallTOsByPropagatedCalls(
//                                entry.getValue(), stageParentsFromChildren, false),
//                                false, true).keySet();
//            }
//        }
//
//        return log.exit(returnExprTOs);
//    }
//
//    private Collection<ExpressionCallTO> getExpressionCallsBySql(Set<Integer> speciesIds, 
//            boolean isIncludeSubstructures, boolean isIncludeSubStages) {
//        log.entry(speciesIds, isIncludeSubstructures, isIncludeSubStages);
//
//        ExpressionCallDAO dao = this.getExpressionCallDAO();
//        dao.setAttributes(EnumSet.complementOf(EnumSet.of(ExpressionCallDAO.Attribute.ID, 
//                ExpressionCallDAO.Attribute.OBSERVED_DATA, 
//                ExpressionCallDAO.Attribute.STAGE_ORIGIN_OF_LINE)));
//
//        ExpressionCallParams params = new ExpressionCallParams();
//        params.addAllSpeciesIds(speciesIds);
//
//        params.setIncludeSubstructures(isIncludeSubstructures);
//        params.setIncludeSubStages(isIncludeSubStages);        
//        List<ExpressionCallTO> exprTOs = dao.getExpressionCalls(params).getAllTOs();
//
//        return log.exit(exprTOs);
//    }
}
