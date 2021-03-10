package org.bgee.model.dao.api.expressiondata;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.TransferObject.EnumDAOField;

/**
 * {@code Enum} listing the data types used in Bgee:
 *
 * <ul>
 * <li>{@code AFFYMETRIX}
 * <li>{@code EST}
 * <li>{@code IN_SITU}
 * <li>{@code RNA_SEQ}
 * <li>{@code FULL_LENGTH}
 * </ul>
 * 
 * @author Freeric Bastian
 * @version Bgee 14 Mar. 2017
 * @since Bgee 14 Mar. 2017
 */
public enum DAODataType implements EnumDAOField {
    AFFYMETRIX("affymetrix", "affymetrix"), EST("est", "est"), IN_SITU("in situ", "inSitu"), 
    RNA_SEQ("rna-seq", "rnaSeq"), FULL_LENGTH("full length single cell RNA-Seq", "scRnaSeqfullLength");
    private final static Logger log = LogManager.getLogger(DAODataType.class.getName());

    /**
     * Convert the {@code String} representation of a data type into a {@code DataType}.
     * Operation performed by calling {@link TransferObject#convert(Class, String)} 
     * with {@code DataType} as the {@code Class} argument, and {@code representation} as 
     * the {@code String} argument.
     * 
     * @param representation    A {@code String} representing a data type.
     * @return                  The {@code DataType} corresponding to {@code representation}.
     * @throws IllegalArgumentException If {@code representation} does not correspond 
     *                                  to any {@code DataType}.
     */
    public static final DAODataType convertToDataType(String representation) {
        log.entry(representation);
        return log.traceExit(TransferObject.convert(DAODataType.class, representation));
    }

    /**
     * See {@link #getStringRepresentation()}
     */
    private final String stringRepresentation;
    /**
     * See {@link #getFieldNamePrefix()}
     */
    private final String fieldNamePrefix;

    /**
     * Constructor providing the {@code String} representation of this {@code DataType}.
     * 
     * @param stringRepresentation  A {@code String} corresponding to this {@code DataType}.
     * @param fieldNamePrefix       A {@code String} that is the prefix of fields related to this {@code DataType}.
     */
    private DAODataType(String stringRepresentation, String fieldNamePrefix) {
        this.stringRepresentation = stringRepresentation;
        this.fieldNamePrefix = fieldNamePrefix;
    }
    @Override
    public String getStringRepresentation() {
        return this.stringRepresentation;
    }
    @Override
    public String toString() {
        return this.getStringRepresentation();
    }
    /**
     * @return  A {@code String} that is the prefix of fields related to this {@code DataType}.
     */
    public String getFieldNamePrefix() {
        return this.fieldNamePrefix;
    }
}
