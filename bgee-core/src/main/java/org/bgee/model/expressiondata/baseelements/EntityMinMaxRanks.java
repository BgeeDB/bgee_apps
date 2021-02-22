package org.bgee.model.expressiondata.baseelements;

import java.math.BigDecimal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A class to store the min and maximum expression ranks in an entity
 * (such as an {@code AnatEntity}, or a {@code Gene}).
 *
 * @author Frederic Bastian
 * @see ExpressionLevelCategory
 * @since Bgee 14 Feb. 2019
 * @version Bgee 14 Feb. 2019
 *
 * @param <T>   The type of the entity the min. and max ranks were retrieved for.
 */
//T does not extend Entity on purpose, because Gene does not extend Entity
public class EntityMinMaxRanks<T> {
    private final static Logger log = LogManager.getLogger(EntityMinMaxRanks.class.getName());

    private final BigDecimal minRank;
    private final BigDecimal maxRank;
    private final T entityConsidered;

    /**
     * @param minRank               See {@link #getMinRank()}
     * @param maxRank               See {@link #getMaxRank()}
     */
    public EntityMinMaxRanks(BigDecimal minRank, BigDecimal maxRank) {
        this(minRank, maxRank, null);
    }
    /**
     * @param minRank               See {@link #getMinRank()}
     * @param maxRank               See {@link #getMaxRank()}
     * @param entityConsidered      See {@link #getEntityConsidered()}
     */
    public EntityMinMaxRanks(BigDecimal minRank, BigDecimal maxRank, T entityConsidered) {
        if (minRank == null || maxRank == null) {
            throw log.throwing(new IllegalArgumentException("All ranks must be provided"));
        }
        if (minRank.compareTo(maxRank) > 0) {
            throw log.throwing(new IllegalArgumentException("Min. rank greater than max rank"));
        }
        this.minRank = minRank;
        this.maxRank = maxRank;
        this.entityConsidered = entityConsidered;
    }

    /**
     * @param rank  A {@code BigDecimal} to check whether its value is between {@link #getMinRank()}
     *              and {@link #getMaxRank()}, included.
     * @return      A {@code boolean} that is {@code false} if {@code rank} < {@link #getMinRank()}
     *              or {@code rank} > {@link #getMaxRank()}.
     * throws IllegalArgumentException  If {@code rank} is {@code null}.
     */
    public boolean isInRange(BigDecimal rank) {
        log.entry(rank);
        if (rank == null) {
            throw log.throwing(new IllegalArgumentException("The rank must be provided"));
        }
        if (rank.compareTo(this.getMinRank()) < 0 || rank.compareTo(this.getMaxRank()) > 0) {
            return log.traceExit(false);
        }
        return log.traceExit(true);
    }

    /**
     * @return  A {@code BigDecimal} that is the minimum expression rank in the entity considered.
     *          Cannot be {@code null}.
     */
    public BigDecimal getMinRank() {
        return minRank;
    }
    /**
     * @return  A {@code BigDecimal} that is the max expression rank in the entity considered.
     *          Cannot be {@code null}.
     */
    public BigDecimal getMaxRank() {
        return maxRank;
    }
    /**
     * @return  The entity {@code T} which the min. and max ranks were retrieved for.
     *          Can be {@code null} if this information was not necessary.
     */
    public T getEntityConsidered() {
        return entityConsidered;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((entityConsidered == null) ? 0 : entityConsidered.hashCode());
        result = prime * result + ((maxRank == null) ? 0 : maxRank.hashCode());
        result = prime * result + ((minRank == null) ? 0 : minRank.hashCode());
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
        EntityMinMaxRanks<?> other = (EntityMinMaxRanks<?>) obj;
        if (entityConsidered == null) {
            if (other.entityConsidered != null) {
                return false;
            }
        } else if (!entityConsidered.equals(other.entityConsidered)) {
            return false;
        }
        if (maxRank == null) {
            if (other.maxRank != null) {
                return false;
            }
        } else if (!maxRank.equals(other.maxRank)) {
            return false;
        }
        if (minRank == null) {
            if (other.minRank != null) {
                return false;
            }
        } else if (!minRank.equals(other.minRank)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EntityMinMaxRanks [minRank=").append(minRank)
               .append(", maxRank=").append(maxRank)
               .append(", entityConsidered=").append(entityConsidered)
               .append("]");
        return builder.toString();
    }
}
