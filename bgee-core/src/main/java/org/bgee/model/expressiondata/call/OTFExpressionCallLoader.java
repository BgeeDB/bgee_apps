package org.bgee.model.expressiondata.call;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.CommonService;
import org.bgee.model.ServiceFactory;
import org.bgee.model.expressiondata.rawdata.baseelements.RawCall;
import org.bgee.model.expressiondata.rawdata.baseelements.RawCallSource;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCondition;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataContainer;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataDataType;
import org.bgee.model.gene.Gene;

public class OTFExpressionCallLoader extends CommonService {
    private static final Logger log = LogManager.getLogger(OTFExpressionCallLoader.class.getName());

    /**
     * A {@code BigDecimal} representing the minimum value that can take an expression score.
     */
    public final static BigDecimal EXPRESSION_SCORE_MIN_VALUE = new BigDecimal("0.01");
    /**
     * A {@code BigDecimal} representing the maximum value that can take an expression score.
     */
    public final static BigDecimal EXPRESSION_SCORE_MAX_VALUE = new BigDecimal("100");
    private final static BigDecimal ZERO_BIGDECIMAL = new BigDecimal("0");
    private final static BigDecimal ABOVE_ZERO_BIGDECIMAL = new BigDecimal("0.000000000000000000000000000001");
    private final static BigDecimal MIN_FDR_BIGDECIMAL = new BigDecimal("0.00000000000001");

    protected OTFExpressionCallLoader() {
        this(null);
    }
    protected OTFExpressionCallLoader(ServiceFactory serviceFactory) {
        super(serviceFactory);
    }

    public List<OTFExpressionCall> loadOTFExpressionCalls(Gene gene, ConditionGraph conditionGraph,
            Map<RawDataCondition, Condition> rawDataCondsToConds,
            Map<RawDataDataType<?, ?>, RawDataContainer<?, ?>> rawDataContainers) {

        //For faster computation, we want to retrieve the raw data per Condition,
        //keeping info of data type
        
                
                

        //Retrieve roots of conditionGraph
        Set<Condition> rootConditions = conditionGraph.getRootConditions();
        if (rootConditions.size() != 1) {
            throw log.throwing(new IllegalStateException("Incorrect number of roots: "
                  + rootConditions.size()));
        }
        //Retrieve ordered List of Conditions for DFS
        List<Condition> conds = conditionGraph.getDeepFirstOrderedConditions(rootConditions.iterator().next());

        return null;
    }

    static Map<Condition, Map<RawDataDataType<?, ?>, List<RawCall>>> transformToRawDataPerCondition(
            Map<RawDataCondition, Condition> rawDataCondsToConds,
            Map<RawDataDataType<?, ?>, RawDataContainer<?, ?>> rawDataContainers) {
        log.traceEntry("{}, {}", rawDataCondsToConds, rawDataContainers);
        //For faster computation, we want to retrieve the raw data per Condition,
        //keeping info of data type
        Map<Condition, Map<RawDataDataType<?, ?>, List<RawCall>>> condToData =
                //Start doing the grouping for each data type
                rawDataContainers.entrySet().stream()
                //here, obtain an Entry<RawDataDataType<?, ?>,  Map<Condition, List<RawCall>>>
                .map(e -> {
                    Map<Condition, List<RawCall>> condToCalls = e.getValue().getCalls()
                            .stream().collect(Collectors.toMap(
                                    rawCallSource -> rawDataCondsToConds.get(
                                            rawCallSource.getAssay().getAnnotation().getRawDataCondition()),
                                    //Use a List because RawCall implements hashCode/equals
                                    //and maybe it was a bad idea, we don't want to "loose" calls
                                    rawCallSource -> {
                                        //We need a mutable List, so we don't use List.of()
                                        List<RawCall> calls = new ArrayList<>();
                                        calls.add(rawCallSource.getRawCall());
                                        return calls;
                                    },
                                    (v1, v2) -> {v1.addAll(v2); return v1;}));
                    return new AbstractMap.SimpleEntry<>(e.getKey(), condToCalls);
                })
                //Here obtain a Stream of Entry<Condition, Entry<RawDataDataType<?, ?>, List<RawCall>>>
                .flatMap(e -> e.getValue().entrySet().stream()
                    .map(ePerCond -> new AbstractMap.SimpleEntry<>(
                            ePerCond.getKey(),
                            new AbstractMap.SimpleEntry<>(e.getKey(), ePerCond.getValue())))
                )
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> {
                            //We need a mutable Map, so we don't use Map.of()
                            Map<RawDataDataType<?, ?>, List<RawCall>> dataTypeToCalls = new HashMap<>();
                            dataTypeToCalls.put(e.getValue().getKey(), e.getValue().getValue());
                            return dataTypeToCalls;
                        },
                        (map1, map2) -> {map1.putAll(map2); return map1;}
                        ));
        return log.traceExit(condToData);
    }

    static BigDecimal computeExpressionScore(BigDecimal rank, BigDecimal maxRank) {
        log.traceEntry("{}, {}", rank, maxRank);
        if (maxRank == null) {
            throw log.throwing(new IllegalArgumentException("Max rank must be provided"));
        }
        if (rank == null) {
            log.debug("Rank is null, cannot compute expression score");
            return log.traceExit((BigDecimal) null);
        }
        if (rank.compareTo(new BigDecimal("0")) <= 0 || maxRank.compareTo(new BigDecimal("0")) <= 0) {
            throw log.throwing(new IllegalArgumentException("Rank and max rank cannot be less than or equal to 0"));
        }
        if (rank.compareTo(maxRank) > 0) {
            throw log.throwing(new IllegalArgumentException("Rank cannot be greater than maxRank. Rank: " + rank
                    + " - maxRank: " + maxRank));
        }

        BigDecimal invertedRank = maxRank.add(new BigDecimal("1")).subtract(rank);
        BigDecimal expressionScore = invertedRank.multiply(new BigDecimal("100")).divide(maxRank, 5, RoundingMode.HALF_UP);
        //We want expression score to be at least greater than EXPRESSION_SCORE_MIN_VALUE
        if (expressionScore.compareTo(EXPRESSION_SCORE_MIN_VALUE) < 0) {
            expressionScore = EXPRESSION_SCORE_MIN_VALUE;
        }
        if (expressionScore.compareTo(EXPRESSION_SCORE_MAX_VALUE) > 0) {
            log.warn("Expression score should always be lower or equals to " + EXPRESSION_SCORE_MAX_VALUE
                    + ". The value was " + expressionScore + "and was then manually updated to "
                    + EXPRESSION_SCORE_MAX_VALUE + ".");
            expressionScore = EXPRESSION_SCORE_MAX_VALUE;
        }
        return log.traceExit(expressionScore);
    }

    static BigDecimal computeFDRCorrectedPValue(List<BigDecimal> pValues) {
        log.traceEntry("{}", pValues);

        int m = pValues.size();
        Double[] pValuesDouble = 
                pValues.stream()
                .map(p -> p.compareTo(ZERO_BIGDECIMAL) == 0 ? ABOVE_ZERO_BIGDECIMAL : p)
                .map(p -> p.doubleValue())
                .toArray(length -> new Double[length]);
        double[] adjustedPValues = new double[m];

        Arrays.sort(pValuesDouble);
        // iterate through all p-values:  largest to smallest
        for (int i = m - 1; i >= 0; i--) {
            if (i == m - 1) {
                adjustedPValues[i] = pValuesDouble[i];
            } else {
                double unadjustedPvalue = pValuesDouble[i];
                int divideByM = i + 1;
                double left = adjustedPValues[i + 1];
                double right = (m / (double) divideByM) * unadjustedPvalue;
                adjustedPValues[i] = Math.min(left, right);
            }
        }
        //Find the smallest corrected p-value
        BigDecimal fdr = BigDecimal.valueOf(Arrays.stream(adjustedPValues).min().getAsDouble());
        //If the FDR is less than MIN_FDR_BIGDECIMAL, change it to MIN_FDR_BIGDECIMAL
        //(in order to avoid having fields in the globalExpression table with too  much precision)
        if (fdr.compareTo(MIN_FDR_BIGDECIMAL) < 0) {
            fdr = MIN_FDR_BIGDECIMAL;
        }
        return log.traceExit(fdr);
    }

    static BigDecimal computeMedian(List<BigDecimal> pValues) {
        log.traceEntry("{}", pValues);

        
        return null;
    }
}
