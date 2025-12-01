package org.bgee.model.expressiondata.call;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.ExpressionLevelInfo;
import org.bgee.model.expressiondata.baseelements.ExpressionLevelInfo2;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.gene.Gene;
import org.bgee.model.Service;

public class ExpressionCallOTF {

    //XXX: Do we really need to keep self and descendant observation???
//    private final Map<DataType, ExpressionObservation> selfObservation;
//    private final Map<DataType, Set<ExpressionObservation>> directDescendantObservations;
    private final Map<DataType, ExpressionObservation> propagatedObservation;
    private final Gene gene;
    private final Condition2 condition;
    private final SummaryQuality summaryQuality;
    private final ExpressionSummary exprSummary;
    private final ExpressionLevelInfo2 exprLevelInfo;
    //XXX to discuss how to represent best descendant info (useful for Harry lazy loading)
    // It could be an ExpressionCallOTF without the Maps Map<DataType, ExpressionObservation>
    private final ExpressionLevelInfo bestDescendantExprLevelInfo;
    private final Condition2 bestDescendantCondition;
    
    public static enum OrderingAttribute implements Service.OrderingAttribute{
        EXPRESSION_SCORE, RANK, P_VALUE
    }

    private static final Logger log = LogManager.getLogger(ExpressionCallOTF.class.getName());
    

    public ExpressionCallOTF(Gene gene, Condition2 condition, Map<DataType, ExpressionObservation> propagatedObservation, SummaryQuality summaryQuality,
            ExpressionSummary exprSummary, ExpressionLevelInfo2 exprLevelInfo,
            ExpressionLevelInfo bestDescendantExprLevelInfo, Condition2 bestDescendantCondition) {
        this.gene = gene;
        this.condition = condition;
//        this.selfObservation = selfObservation;
//        this.directDescendantObservations = directDescendantObservations;
        this.propagatedObservation = propagatedObservation;
        this.summaryQuality = summaryQuality;
        this.exprSummary = exprSummary;
        this.exprLevelInfo = exprLevelInfo;
        this.bestDescendantExprLevelInfo = bestDescendantExprLevelInfo;
        this.bestDescendantCondition = bestDescendantCondition;
    }
//    public Map<DataType, ExpressionObservation> getSelfObservation() {
//        return selfObservation;
//    }
//    public Map<DataType, Set<ExpressionObservation>> getDirectDescendantObservations() {
//        return directDescendantObservations;
//    }
    public Map<DataType, ExpressionObservation> getPropagatedObservation() {
        return propagatedObservation;
    }
    public Gene getGene() {
        return gene;
    }
    public Condition2 getCondition() {
        return condition;
    }
    public SummaryQuality getSummaryQuality() {
        return summaryQuality;
    }
    public ExpressionSummary getExprSummary() {
        return exprSummary;
    }
    public ExpressionLevelInfo2 getExprLevelInfo() {
        return exprLevelInfo;
    }
    @Override
    public int hashCode() {
        return Objects.hash(condition, exprLevelInfo, exprSummary, gene,
                propagatedObservation, summaryQuality);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExpressionCallOTF other = (ExpressionCallOTF) obj;
        return Objects.equals(condition, other.condition)
                && Objects.equals(exprLevelInfo, other.exprLevelInfo) && exprSummary == other.exprSummary
                && Objects.equals(gene, other.gene)
                && Objects.equals(propagatedObservation, other.propagatedObservation)
                && summaryQuality == other.summaryQuality;
    }
    @Override
    public String toString() {
        return "ExpressionCallOTF [propagatedObservation=" + propagatedObservation + ", gene=" + gene
                + ", condition=" + condition + ", summaryQuality=" + summaryQuality + ", exprSummary=" + exprSummary
                + ", exprLevelInfo=" + exprLevelInfo + "]";
    }

//    private static Map<DataType, ExpressionObservation> propagateExpression(
//            Map<DataType, ExpressionObservation> selfObservation,
//            Map<DataType, Set<ExpressionObservation>> directDescendantObservations) {
//        if (selfObservation.keySet() != directDescendantObservations.keySet()) {
//            throw log.throwing(new IllegalArgumentException("Self and descendant dataTypes should be the same"));
//        }
//        Map<DataType, ExpressionObservation> propagatedCalls = new HashMap<>();
//        for(DataType dt : selfObservation.keySet()) {
//            ExpressionObservation self = selfObservation.get(dt);
//            Set<ExpressionObservation> descendants = directDescendantObservations.get(dt);
//            BigDecimal descPValues = BigDecimal.ZERO;
//            Integer descNumObs= 0;
//            BigDecimal descWeight = BigDecimal.ZERO;
//            BigDecimal descRank = BigDecimal.ZERO;
//            BigDecimal selfWeight = self.getWeight();
//            for (ExpressionObservation descendant : descendants) {
//                descPValues = descPValues.add(descendant.getpValue().multiply(descendant.getWeight()));
//                descNumObs += descendant.getNumberObservation();
//                descRank = descRank.add(descendant.getRank().multiply(descendant.getWeight()));
//                descWeight = descWeight.add(descendant.getWeight());
//            }
//            propagatedCalls.put(dt, new ExpressionObservation(
//                    descPValues.add(self.getpValue()).divide(descWeight.add(selfWeight)),
//                    descNumObs + self.getNumberObservation(),
//                    descRank.add(self.getRank()).divide(descWeight.add(selfWeight)),
//                    descWeight.add(selfWeight)));
//        }
//        return propagatedCalls ;
//    }
}
