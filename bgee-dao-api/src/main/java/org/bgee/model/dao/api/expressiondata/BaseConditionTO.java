package org.bgee.model.dao.api.expressiondata;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.TransferObject;

/**
 * Parent class of all {@code TransferObject}s describing conditions in Bgee.
 *
 * @author Frederic Bastian
 * @version Bgee 14, Sep. 2018
 * @since Bgee 14, Sep. 2018
 */
public abstract class BaseConditionTO extends EntityTO<Integer> {
    private final static Logger log = LogManager.getLogger(BaseConditionTO.class.getName());
    private static final long serialVersionUID = 7627889095686120298L;

    /**
     * {@code EnumDAOField} representing the different sex info that can be used in Bgee.
     *
     * @author Frederic Bastian
     * @version Bgee 14 Sep. 2018
     * @since Bgee 14 Sep. 2018
     */
    public enum Sex implements EnumDAOField {
        NOT_ANNOTATED("not annotated"), HERMAPHRODITE("hermaphrodite"), FEMALE("female"), MALE("male"),
        MIXED("mixed"), NA("NA");

        /**
         * See {@link #getStringRepresentation()}
         */
        private final String stringRepresentation;
        /**
         * Constructor providing the {@code String} representation of this {@code Sex}.
         *
         * @param stringRepresentation  A {@code String} corresponding to this {@code Sex}.
         */
        private Sex(String stringRepresentation) {
            this.stringRepresentation = stringRepresentation;
        }

        /**
         * Convert the {@code String} representation of a sex (for instance,
         * retrieved from a database) into a {@code Sex}. This method compares
         * {@code representation} to the value returned by {@link #getStringRepresentation()},
         * as well as to the value returned by {@link Enum#name()}, for each {@code Sex}.
         *
         * @param representation    A {@code String} representing a sex.
         * @return                  A {@code Sex} corresponding to {@code representation}.
         * @throws IllegalArgumentException If {@code representation} does not correspond to any {@code Sex}.
         */
        public static final Sex convertToSex(String representation) {
            log.entry(representation);
            return log.traceExit(TransferObject.convert(Sex.class, representation));
        }

        @Override
        public String getStringRepresentation() {
            return this.stringRepresentation;
        }
        @Override
        public String toString() {
            return this.getStringRepresentation();
        }
    }


    private final String anatEntityId;
    private final String stageId;
    private final Sex sex;
    private final String strain;
    private final Integer speciesId;

    protected BaseConditionTO(Integer id, String anatEntityId, String stageId, Sex sex, String strain,
            Integer speciesId) {
        super(id);
        this.anatEntityId = anatEntityId;
        this.stageId = stageId;
        this.sex = sex;
        this.strain = strain;
        this.speciesId = speciesId;
    }

    /**
     * @return  The {@code String} that is the Uberon anatomical entity ID.
     */
    public String getAnatEntityId() {
        return anatEntityId;
    }
    /**
     * @return  The {@code String} that is the Uberon stage ID.
     */
    public String getStageId() {
        return stageId;
    }
    /**
     * @return  {@code Sex} of this condition.
     */
    public Sex getSex() {
        return sex;
    }
    /**
     * @return  A {@code String} corresponding to the strain of this condition.
     */
    public String getStrain() {
        return strain;
    }
    /**
     * @return  The {@code String} that is the NCBI species taxon ID.
     */
    public Integer getSpeciesId() {
        return speciesId;
    }
}