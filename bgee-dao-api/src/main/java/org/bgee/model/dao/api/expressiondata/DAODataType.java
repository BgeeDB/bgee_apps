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
 * </ul>
 * 
 * @author Freeric Bastian
 * @version Bgee 14 Mar. 2017
 * @since Bgee 14 Mar. 2017
 */
public enum DAODataType implements EnumDAOField {
    AFFYMETRIX("affymetrix"), EST("est"), IN_SITU("in situ"), RNA_SEQ("rna-seq");
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
        return log.exit(TransferObject.convert(DAODataType.class, representation));
    }

    /**
     * See {@link #getStringRepresentation()}
     */
    private final String stringRepresentation;

    /**
     * Constructor providing the {@code String} representation of this {@code DataType}.
     * 
     * @param stringRepresentation  A {@code String} corresponding to this {@code DataType}.
     */
    private DAODataType(String stringRepresentation) {
        this.stringRepresentation = stringRepresentation;
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
