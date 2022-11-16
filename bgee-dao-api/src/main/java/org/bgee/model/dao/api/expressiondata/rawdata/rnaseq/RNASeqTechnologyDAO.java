package org.bgee.model.dao.api.expressiondata.rawdata.rnaseq;

import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;

public interface RNASeqTechnologyDAO extends DAO<RNASeqTechnologyDAO.Attribute>{

    /**
     * {@code Enum} used to define the attributes to populate in the {@code RNASeqTechnologyTO}s
     * obtained from this {@code RNASeqTechnologyDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link RNASeqTechnologyTO#getId()}.
     * <li>{@code NAME}: corresponds to {@link RNASeqTechnologyTO#getName()}.
     * <li>{@code SINGLE_CELL}: corresponds to {@link RNASeqTechnologyTO#getIsSingleCell()}
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        ID("rnaSeqTechnologyId"), NAME("rnaSeqTechnologyName"),
        IS_SINGLE_CELL("rnaSeqTechnologyIsSingleCell");

        /**
         * A {@code String} that is the corresponding field name in {@code ESTTO} class.
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

    public RNASeqTechnologyTOResultSet getRNASeqTechnologies(Collection<RNASeqTechnologyDAO.Attribute> attrs);
    
    public RNASeqTechnologyTOResultSet getRNASeqTechnologies(Boolean isSingleCell,
            Collection<String> technologyName, Collection<RNASeqTechnologyDAO.Attribute> attrs);

    /**
     * {@code DAOResultSet} for {@code RNASeqExperimentTO}s
     * 
     * @author  Julien Wollbrett
     * @version Bgee 15, Nov. 2022
     * @since   Bgee 15
     */
    public interface RNASeqTechnologyTOResultSet extends DAOResultSet<RNASeqTechnologyTO> {
    }

    /**
     * {@code TransferObject} for RNA-Seq technologies.
     * 
     * @author Julien Wollbrett
     * @version Bgee 15, Nov. 2022
     * @since Bgee 15
     */
    public final class RNASeqTechnologyTO extends EntityTO<Integer> {

        private static final long serialVersionUID = 6341814808553725495L;

        private final String name;
        private final boolean isSingleCell;

        public RNASeqTechnologyTO(Integer id, String name, boolean isSingleCell) {
            super(id);
            this.name = name;
            this.isSingleCell = isSingleCell;
        }

        public String getName() {
            return name;
        }

        public boolean isSingleCell() {
            return isSingleCell;
        }

    }

}
