package org.bgee.model.expressiondata.baseelements;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class providing a qualitative expression level information for an {@code ExpressionCall}.
 * This information is computed by comparing the expression rank of the {@code ExpressionCall}
 * to the min. and max expression ranks of an entity. So, for a same {@code ExpressionCall},
 * we can compute several {@code QualitativeExpressionLevel}s, for instance one relative
 * to the anatomical entity (by comparing the rank of the call to the min. and max ranks
 * in the anatomical entity, considering any gene expressed in it), and another one
 * relative to the gene (by comparing to the min. and max ranks of the gene,
 * from any anatomical structure where it is expressed).
 * <p>
 * As an example, if a gene has the following expression calls:
 * <pre>
 * Gene ID  Anat. entity ID Expression call Expression rank
 * Gene1    AnatEntity1     EXPRESSED       2500
 * Gene1    AnatEntity2     EXPRESSED       10000
 * Gene1    AnatEntity3     EXPRESSED       22500
 * </pre>
 * And the following genes are expressed in {@code AnatEntity1}:
 * <pre>
 * Gene ID  Anat. entity ID Expression call Expression rank
 * Gene2    AnatEntity1     EXPRESSED       1000
 * Gene1    AnatEntity1     EXPRESSED       2500
 * Gene3    AnatEntity1     EXPRESSED       4000
 * </pre>
 * As a result:
 * <ul>
 * <li>The min. and max ranks in {@code AnatEntity1} are respectively {@code 1000} and {@code 4000}.
 * The expression level category for {@code Gene1} in {@code AnatEntity1},
 * relative to the expression levels in {@code AnatEntity1}, is {@code MEDIUM}
 * (threshold for HIGH: 2000; threshold for MEDIUM: 3000).
 * <li>The min. and max ranks for {@code gene1} are respectively {@code 2500} and {@code 22500}.
 * The expression level category for {@code Gene1} in {@code AnatEntity1},
 * relative to the expression levels of {@code Gene1}, is {@code HIGH}
 * (threshold for HIGH: 9167; threshold for MEDIUM: 15833).
 * </ul>
 * 
 * @author Frederic Bastian
 * @since Bgee 14 Feb. 2019
 * @version Bgee 14 Feb. 2019
 *
 * @param <T>   The type of the entity the min. and max ranks were retrieved for.
 */
public class QualitativeExpressionLevel<T> {
    private final static Logger log = LogManager.getLogger(QualitativeExpressionLevel.class.getName());

    private final ExpressionLevelCategory expressionLevelCategory;
    private final EntityMinMaxRanks<T> relativeEntityMinMaxRanks;

    /**
     * @param relativeEntityMinMaxRanks See {@link #getRelativeEntityMinMaxRanks()}.
     * @param rank                      See {@link #getRank()}
     */
    public QualitativeExpressionLevel(ExpressionLevelCategory exprLevelCat, 
            EntityMinMaxRanks<T> relativeEntityMinMaxRanks) {
        if (exprLevelCat == null) {
            throw log.throwing(new IllegalArgumentException(
                    "The ExpressionLevelCategory must be provided"));
        }
        this.expressionLevelCategory = exprLevelCat;
        this.relativeEntityMinMaxRanks = relativeEntityMinMaxRanks;
    }

    /**
     * @return  {@code ExpressionLevelCategory} of an expression call. If the category
     *          is not {@code ABSENT}, it reflects the expression rank of the call relative to
     *          the min. and max ranks of an entity (returned by
     *          {@link #getRelativeEntityMinMaxRanks()}). If the difference between
     *          the min. and the max rank is less than {@link #MIN_RANK_DIFF_FOR_CATEGORIES},
     *          the highest {@code ExpressionLevelCategory} is always returned.
     */
    public ExpressionLevelCategory getExpressionLevelCategory() {
        return expressionLevelCategory;
    }
    /**
     * @return  The {@code EntityMinMaxRanks} providing the min./max rank information of an entity,
     *          that allowed to classify an expression rank into an {@code ExpressionLevelCategory}.
     *          Can be {@code null} if it was not needed to store this information, or if
     *          the {@code ExpressionLevelCategory} of the call is {@code ABSENT}.
     */
    public EntityMinMaxRanks<T> getRelativeEntityMinMaxRanks() {
        return relativeEntityMinMaxRanks;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((expressionLevelCategory == null) ? 0 : expressionLevelCategory.hashCode());
        result = prime * result + ((relativeEntityMinMaxRanks == null) ? 0 : relativeEntityMinMaxRanks.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        QualitativeExpressionLevel<?> other = (QualitativeExpressionLevel<?>) obj;
        if (expressionLevelCategory != other.expressionLevelCategory) {
            return false;
        }
        if (relativeEntityMinMaxRanks == null) {
            if (other.relativeEntityMinMaxRanks != null) {
                return false;
            }
        } else if (!relativeEntityMinMaxRanks.equals(other.relativeEntityMinMaxRanks)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("QualitativeExpressionLevel [expressionLevelCategory=")
               .append(expressionLevelCategory)
               .append(", relativeEntityMinMaxRanks=").append(relativeEntityMinMaxRanks)
               .append("]");
        return builder.toString();
    }
}