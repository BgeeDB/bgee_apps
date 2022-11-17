package org.bgee.model.dao.api.expressiondata.rawdata.microarray;

import java.math.BigDecimal;
import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.exception.DAOException;

/**
 * DAO defining queries using or retrieving {@link AffymetrixChipTypeTO}s. 
 * 
 * @author Julien Wollbrett
 * @version Bgee 15 Nov. 2022
 */
public interface AffymetrixChipTypeDAO extends DAO<AffymetrixChipTypeDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code AffymetrixChipTypeTO}s
     * obtained from this {@code AffymetrixChipTypeDAO}.
     * <ul>
     * <li>{@code CHIP_TYPE_ID}: corresponds to {@link AffymetrixChipTypeTO#getId()}.
     * <li>{@code CHIP_TYPE_NAME}: corresponds to {@link AffymetrixChipTypeTO#getAffymetrixChipTypeName()}.
     * <li>{@code CDF_NAME}: corresponds to {@link AffymetrixChipTypeTO#getCdfName()}.
     * <li>{@code IS_COMPATIBLE}: corresponds to {@link AffymetrixChipTypeTO#isCompatible()}.
     * <li>{@code QUALITY_SCORE_THRESHOLD}: corresponds to {@link AffymetrixChipTypeTO#getQualityScoreThreshold()}.
     * <li>{@code PERCENT_PRESENT_THRESHOLD}: corresponds to {@link AffymetrixChipTypeTO#getPercentPresentThreshold()}.
     * <li>{@code CHIP_TYPE_MAX_RANK}: corresponds to {@link AffymetrixChipTypeTO#getChipTypeMaxRank()}.
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        CHIP_TYPE_ID("chipTypeId"), CHIP_TYPE_NAME("chipTypeName"),
        CDF_NAME("cdfName"), IS_COMPATIBLE("isCompatible"), QUALITY_SCORE_THRESHOLD("qualityScoreThreshold"),
        PERCENT_PRESENT_THRESHOLD("percentPresentThreshold"), CHIP_TYPE_MAX_RANK("chipTypeMaxRank");

        /**
         * A {@code String} that is the corresponding field name in {@code AffymetrixChipTypeTO} class.
         * @see {@link Attribute#getTOFieldName()}
         */
        private final String fieldName;

        private Attribute(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public String getTOFieldName() {
            return this.fieldName;
        }
    }

    /**
     * Allows to retrieve {@code AffymetrixChipTypeTO}s according to the provided chip type IDs
     * <p>
     * The {@code AffymetrixChipTypeTO}s are retrieved and returned as a
     * {@code AffymetrixChipTypeTOResultSet}. It is the responsibility of the caller to close this
     * {@code DAOResultSet} once results are retrieved.
     *
     * @param chipTypeIds       A {@code Collection} of {@code String} allowing to filter which
     *                          chip types to retrieve. If null or empty retrieve all chip types
     * @param attributes        A {@code Collection} of {@code Attribute}s to specify the information
     *                          to retrieve from the data source.
     * @return                  A {@code AffymetrixChipTypeTOResultSet} allowing to retrieve the targeted
     *                          {@code AffymetrixChipTypeTO}s.
     * @throws DAOException     If an error occurred while accessing the data source.
     */
    public AffymetrixChipTypeTOResultSet getAffymetrixChipTypes(Collection<String> chipTypeIds,
            Collection<Attribute> attributes) throws DAOException;

    /**
     * {@code DAOResultSet} for {@code AffymetrixChipTypeTO}s
     * 
     * @author  Julien Wollbrett
     * @version Bgee 15, Nov. 2022
     */
    public interface AffymetrixChipTypeTOResultSet extends DAOResultSet<AffymetrixChipTypeTO> {
    }

    /**
     * {@code TransferObject} for Affymetrix chip types.
     * 
     * @author Julien Wollbrett
     * @version Bgee 15 Nov. 2022
     */
    public final class AffymetrixChipTypeTO extends EntityTO<String> {

        private static final long serialVersionUID = -885779088447205595L;

        private final String affymetrixChipTypeName;
        private final String cdfName;

        private final boolean isCompatible;
        private final BigDecimal qualityScoreThreshold;
        private final BigDecimal percentPresentThreshold;
        private final BigDecimal chipTypeMaxRank;

        /**
         * Default constructor. 
         */
        public AffymetrixChipTypeTO(String affymetrixChipTypeId, String affymetrixChipTypeName, 
                String cdfName, boolean isCompatible, BigDecimal qualityScoreThreshold,
                BigDecimal percentPresentThreshold,
                BigDecimal chipTypeMaxRank) {
            super(affymetrixChipTypeId);
            this.affymetrixChipTypeName = affymetrixChipTypeName;
            this.cdfName = cdfName;
            this.isCompatible = isCompatible;
            this.qualityScoreThreshold = qualityScoreThreshold;
            this.percentPresentThreshold = percentPresentThreshold;
            this.chipTypeMaxRank = chipTypeMaxRank;
        }

        public String getAffymetrixChipTypeName() {
            return affymetrixChipTypeName;
        }
        public String getCdfName() {
            return cdfName;
        }
        public boolean isCompatible() {
            return isCompatible;
        }
        public BigDecimal getQualityScoreThreshold() {
            return qualityScoreThreshold;
        }
        public BigDecimal getPercentPresentThreshold() {
            return percentPresentThreshold;
        }
        public BigDecimal getChipTypeMaxRank() {
            return chipTypeMaxRank;
        }

        @Override
        public String toString() {
            return "AffymetrixChipTypeTO [affymetrixChipTypeName=" + affymetrixChipTypeName + ", cdfName=" + cdfName
                    + ", isCompatible=" + isCompatible + ", qualityScoreThreshold=" + qualityScoreThreshold
                    + ", percentPresentThreshold=" + percentPresentThreshold + ", chipTypeMaxRank=" + chipTypeMaxRank
                    + "]";
        }

        

        
        
    }
}
