package org.bgee.model.expressiondata.rawdata.baseelements;

import java.math.BigDecimal;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeEnum;
import org.bgee.model.BgeeEnum.BgeeEnumField;
import org.bgee.model.dao.api.expressiondata.call.CallDAO.CallTO.DataState;
import org.bgee.model.gene.Gene;

public class RawCall {
    private final static Logger log = LogManager.getLogger(RawCall.class.getName());
    /**
     * An {@code enum} used to define the different exclusion reasons allowed in the 
     * Bgee database. Enum types available: 
     * <ul>
     * <li>{@code NOT_EXCLUDED}.
     * <li>{@code PRE_FILTERING}: probesets always seen as {@code ABSENT} or {@code MARGINAL} 
     * over the whole dataset are removed.
     * <li>{@code UNDEFINED}: only {@code UNDEFINED} call have been seen.
     * <li>{@code NO_EXPRESSION_CONFLICT}: a call of absence of expression has been removed because of 
     * expression in some child condition. Note that, as of Bgee 14, we haven't removed this reason for exclusion,
     * but we don't use it anymore, as we might want to take into account noExpression in parent conditions
     * for generating a global expression calls, where there is expression in a child condition.
     * </ul>
     * 
     * @author Valentine Rech de Laval
     * @author Frederic Bastian
     * @version Bgee 14
     * @since Bgee 13
     */
    public enum ExclusionReason implements BgeeEnumField {
        NOT_EXCLUDED("not excluded"), PRE_FILTERING("pre-filtering"),
        UNDEFINED("undefined"), NO_EXPRESSION_CONFLICT("noExpression conflict"),
        ABSENT_NOT_RELIABLE("absent call not reliable");
    
        /**
         * Convert the {@code String} representation of a exclusion reason (for instance, 
         * retrieved from a database) into a {@code ExclusionReason}. This method compares 
         * {@code representation} to the value returned by {@link #getStringRepresentation()}, 
         * as well as to the value returned by {@link Enum#name()}, for each 
         * {@code ExclusionType}.
         * 
         * @param representation    A {@code String} representing an exclusion reason.
         * @return                  A {@code ExclusionReason} corresponding to 
         *                          {@code representation}.
         * @throws IllegalArgumentException If {@code representation} does not correspond 
         *                                  to any {@code ExclusionReason}.
         */
        public static final ExclusionReason convertToExclusionReason(String representation) {
            log.traceEntry("{}",representation);
            return log.traceExit(BgeeEnum.convert(ExclusionReason.class, representation));
        }
    
        /**
         * See {@link #getStringRepresentation()}
         */
        private final String stringRepresentation;
    
        /**
         * Constructor providing the {@code String} representation 
         * of this {@code ExclusionReason}.
         * 
         * @param stringRepresentation  A {@code String} corresponding to 
         *                              this {@code ExclusionReason}.
         */
        private ExclusionReason(String stringRepresentation) {
            this.stringRepresentation = stringRepresentation;
        }
    
        /**
         * @return  A {@code String} that is the representation for this {@code ExclusionReason}, 
         *          for instance to be used in a database.
         */
        public String getStringRepresentation() {
            return this.stringRepresentation;
        }
    
        @Override
        public String toString() {
            return this.getStringRepresentation();
        }
    }

    //**************************************
    // ATTRIBUTES
    //**************************************
    /**
     * A {@code Gene} to this raw call.
     */
    private final Gene gene;
    
    /**
     * A {@code BigDecimal} defining the pValue of this call.
     */
    private final BigDecimal pValue;
    
    /**
     * A {@code DataState} defining the contribution of data type to the generation of this raw call.
     */
    private final DataState expressionConfidence;

    /**
     * An {@code ExclusionReason} defining the reason of exclusion of this raw call.
     */
    private final ExclusionReason exclusionReason;

    public RawCall(Gene gene, BigDecimal pValue, DataState expressionConfidence,
            ExclusionReason exclusionReason) {
        super();
        this.pValue = pValue;
        this.gene = gene;
        this.expressionConfidence = expressionConfidence;
        this.exclusionReason = exclusionReason;
    }

    //**************************************
    // GETTERS
    //**************************************
    /**
     * @return  A {@code Gene} representing the gene associated to this raw call.
     */
    public Gene getGene() {
        return this.gene;
    }
    
    /**
     * @return  A {@code BigDecimal} defining the pValue of this raw call.
     */
    public BigDecimal getPValue() {
        return this.pValue;
    }

    /**
     * @return  the {@code DataState} defining the contribution of data type to the generation of
     * this raw call.
     */
    public DataState getExpressionConfidence() {
        return this.expressionConfidence;
    }

    /**
     * @return  the {@code ExclusionReason} defining the reason of exclusion of this raw call.
     */
    public ExclusionReason getExclusionReason() {
        return this.exclusionReason;
    }

    @Override
    public int hashCode() {
        return Objects.hash(exclusionReason, expressionConfidence, gene, pValue);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RawCall other = (RawCall) obj;
        return exclusionReason == other.exclusionReason && expressionConfidence == other.expressionConfidence
                && Objects.equals(gene, other.gene) && Objects.equals(pValue, other.pValue);
    }

    @Override
    public String toString() {
        return "RawCall [gene=" + gene + ", pValue=" + pValue + ", expressionConfidence="
                + expressionConfidence + ", exclusionReason=" + exclusionReason + "]";
    }
}