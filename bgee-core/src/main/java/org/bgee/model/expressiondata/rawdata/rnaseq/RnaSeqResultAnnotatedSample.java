package org.bgee.model.expressiondata.rawdata.rnaseq;

import java.math.BigDecimal;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeEnum;
import org.bgee.model.BgeeEnum.BgeeEnumField;
import org.bgee.model.expressiondata.rawdata.baseelements.RawCall;
import org.bgee.model.expressiondata.rawdata.baseelements.RawCallSource;

/**
 * Class describing rna seq annotated sample results
 * @author Julien Wollbrett
 * @version Bgee 15, Nov. 2022
 *
 */
public class RnaSeqResultAnnotatedSample implements RawCallSource<RnaSeqLibraryAnnotatedSample> {

    /**
     * An {@code enum} used to define the different RNA-Seq abundance units allowed in the 
     * Bgee database. Enum types available: 
     * <ul>
     * <li>{@code CPM}: counts per million
     * <li>{@code TPM}: transcripts per million
     * </ul>
     * 
     * @author Julien Wollbrett
     * @version Bgee 15, Nov. 2022
     */
    public enum AbundanceUnit implements BgeeEnumField {
        CPM("cpm"), TPM("tpm");
    
        /**
         * Convert the {@code String} representation of an abundace unit (for instance, 
         * retrieved from a database) into an {@code AbundanceUnit}. This method compares 
         * {@code representation} to the value returned by {@link #getStringRepresentation()}, 
         * as well as to the value returned by {@link Enum#name()}, for each 
         * {@code AbundanceUnit}.
         * 
         * @param representation    A {@code String} representing an exclusion reason.
         * @return                  A {@code ExclusionReason} corresponding to 
         *                          {@code representation}.
         * @throws IllegalArgumentException If {@code representation} does not correspond 
         *                                  to any {@code ExclusionReason}.
         */
        public static final AbundanceUnit convertToAbundanceUnit(String representation) {
            log.traceEntry("{}",representation);
            return log.traceExit(BgeeEnum.convert(AbundanceUnit.class, representation));
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
         *                              this {@code AbundanceUnit}.
         */
        private AbundanceUnit(String stringRepresentation) {
            this.stringRepresentation = stringRepresentation;
        }
    
        /**
         * @return  A {@code String} that is the representation for this {@code AbundanceUnit}, 
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

    private final static Logger log = LogManager
            .getLogger(RnaSeqResultAnnotatedSample.class.getName());

    private final RnaSeqLibraryAnnotatedSample annotatedSample;
    private final RawCall rawCall;
    private final AbundanceUnit abundanceUnit;
    private final BigDecimal abundance;
    private final BigDecimal rawRank;
    private final BigDecimal readCounts;
    private final BigDecimal UMIsCounts;
    private final BigDecimal zScore;

    public RnaSeqResultAnnotatedSample (RawCall rawCall, RnaSeqLibraryAnnotatedSample annotatedSample,
            AbundanceUnit abundanceUnit, BigDecimal abundance, BigDecimal rawRank,
            BigDecimal readCounts, BigDecimal UMISCounts, BigDecimal zscore) {
        this.rawCall = rawCall;
        this.annotatedSample = annotatedSample;
        this.abundanceUnit = abundanceUnit;
        this.abundance = abundance;
        this.rawRank = rawRank;
        this.readCounts = readCounts;
        this.UMIsCounts = UMISCounts;
        this.zScore = zscore;
    }

    @Override
    public RnaSeqLibraryAnnotatedSample getAssay() {
        return annotatedSample;
    }

    public RawCall getRawCall() {
        return rawCall;
    }

    public AbundanceUnit getAbundanceUnit() {
        return abundanceUnit;
    }

    public BigDecimal getAbundance() {
        return abundance;
    }

    public BigDecimal getRawRank() {
        return rawRank;
    }

    public BigDecimal getReadCounts() {
        return readCounts;
    }

    public BigDecimal getUMIsCounts() {
        return UMIsCounts;
    }

    public BigDecimal getzScore() {
        return zScore;
    }

    @Override
    public int hashCode() {
        return Objects.hash(UMIsCounts, abundance, abundanceUnit, annotatedSample, rawCall, rawRank, readCounts,
                zScore);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RnaSeqResultAnnotatedSample other = (RnaSeqResultAnnotatedSample) obj;
        return Objects.equals(UMIsCounts, other.UMIsCounts) && Objects.equals(abundance, other.abundance)
                && abundanceUnit == other.abundanceUnit && Objects.equals(annotatedSample, other.annotatedSample)
                && Objects.equals(rawCall, other.rawCall) && Objects.equals(rawRank, other.rawRank)
                && Objects.equals(readCounts, other.readCounts) && Objects.equals(zScore, other.zScore);
    }

    @Override
    public String toString() {
        return "RnaSeqResultAnnotatedSample [annotatedSample=" + annotatedSample + ", rawCall=" + rawCall
                + ", abundanceUnit=" + abundanceUnit + ", abundance=" + abundance + ", rawRank=" + rawRank
                + ", readCounts=" + readCounts + ", UMIsCounts=" + UMIsCounts + ", zScore=" + zScore + "]";
    }

}
