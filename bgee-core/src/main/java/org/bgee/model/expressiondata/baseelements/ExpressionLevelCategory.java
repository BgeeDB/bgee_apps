package org.bgee.model.expressiondata.baseelements;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * {@code enum} defining expression level categories, based on the comparison of an expression rank
 * to the min. and max ranks of an entity (of a {@code Gene}, or in a {@code Condition}).
 * 
 * @author Frederic Bastian
 * @since Bgee 14 Feb. 2019
 * @see QualitativeExpressionLevel
 * @version Bgee 14 Feb. 2019
 */
public enum ExpressionLevelCategory {
    //Note: order is from high expression to low expression on purpose,
    //used in method getExpressionLevelCategory
    HIGH, MEDIUM, LOW, ABSENT;

    private final static Logger log = LogManager.getLogger(ExpressionLevelCategory.class.getName());

    /**
     * A {@code BigDecimal} that is the minimum difference between a minimum rank
     * and a maximum rank to start considering different expression levels
     * from ranks. If the difference is smaller, all expression levels are considered {@code HIGH}.
     * As of Bgee 14, set to {@code 100}.
     */
    public static final BigDecimal MIN_RANK_DIFF_FOR_LEVELS = new BigDecimal("100");
    private static ExpressionLevelCategory[] LEVELS_WITHOUT_ABSENT =
            Arrays.stream(ExpressionLevelCategory.values())
            .filter(c -> !ABSENT.equals(c))
            .toArray(l -> new ExpressionLevelCategory[l]);
    public static ExpressionLevelCategory[] valuesWithoutAbsent() {
        return LEVELS_WITHOUT_ABSENT;
    }

    public static ExpressionLevelCategory getExpressionLevelCategory(
            EntityMinMaxRanks<?> relativeEntityMinMaxRanks, BigDecimal rank) throws IllegalArgumentException {
        log.entry(relativeEntityMinMaxRanks, rank);
        if (relativeEntityMinMaxRanks == null) {
            throw log.throwing(new IllegalArgumentException(
                    "The min./max ranks for the entity considered must be provided"));
        }
        if (rank == null) {
            throw log.throwing(new IllegalArgumentException(
                    "The rank to be compared to min./max ranks must be provided"));
        }
        BigDecimal minRank = relativeEntityMinMaxRanks.getMinRank();
        BigDecimal maxRank = relativeEntityMinMaxRanks.getMaxRank();
        if (!relativeEntityMinMaxRanks.isInRange(rank)) {
            throw log.throwing(new IllegalArgumentException("Inconsistent rank, min rank: "
                    + minRank + ", max rank: " + maxRank + ", rank: " + rank));
        }
        //Get the level threshold.
        BigDecimal diff = maxRank.subtract(minRank);
        //First, if maxRank - minRank <= MIN_RANK_DIFF_FOR_LEVELS,
        //everything is considered highest expression
        if (diff.compareTo(MIN_RANK_DIFF_FOR_LEVELS) <= 0) {
            return log.exit(ExpressionLevelCategory.valuesWithoutAbsent()[0]);
        }
        //Otherwise, we compute the threshold and levels
        int levelCount = ExpressionLevelCategory.valuesWithoutAbsent().length;
        BigDecimal levelCountDec = new BigDecimal(levelCount);
        int scale = Math.max(rank.scale(), Math.max(minRank.scale(), maxRank.scale()));
        BigDecimal threshold = diff.divide(levelCountDec, scale, RoundingMode.HALF_UP);
        log.trace("Level threshold: {}", threshold);
        for (int i = 0 ; i < levelCount; i++) {
            ExpressionLevelCategory level = ExpressionLevelCategory.valuesWithoutAbsent()[i];
            //No need to evaluate if it is the last level that is being iterated.
            //Plus, we don't want to evaluate the last level because of rouding error
            //on the levelMax computation
            if (i == levelCount - 1) {
                return log.exit(level);
            }
            BigDecimal levelMax = minRank.add(threshold.multiply(new BigDecimal(i + 1)));
            log.trace("Rank max for level {}: {}", level, levelMax);
            if (rank.compareTo(levelMax) <= 0) {
                return log.exit(level);
            }
        }
        throw log.throwing(new AssertionError("A level should always be assigned"));
    }
}