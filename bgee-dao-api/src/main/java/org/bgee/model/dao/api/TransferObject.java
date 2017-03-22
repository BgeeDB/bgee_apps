package org.bgee.model.dao.api;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * {@code TransferObject}s are used to communicate between the
 * DAO layer and the business/model layer. 
 * <p>
 * {@code TransferObject}s should be immutable. 
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since Bgee 01
 */
public abstract class TransferObject implements Serializable {
    
    private static final long serialVersionUID = 3679182128027053390L;

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(TransferObject.class.getName());

    
    /**
     * An interface that must be implemented by {@code Enum}s representing 
     * a field in the data source. To be used along with {@link #convert(Class, String)}
     * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @since Bgee 13
     */
    public interface EnumDAOField {
        /**
         * @return  A {@code String} corresponding to this {@code EnumDAOField} element, 
         *          to be used in the data source.
         */
        public String getStringRepresentation();
    }
    
    /**
     * Convert the {@code String} representation corresponding to an {@link EnumDAOField} 
     * (for instance, retrieved from a data source) into the proper {@code Enum} element. 
     * This method compares {@code representation} to the value returned by 
     * {@link EnumDAOField#getStringRepresentation()}, as well as to the value 
     * returned by {@link Enum#name()}, for each {@code Enum} element corresponing to 
     * {@code enumField}. 
     * .
     * @param enumField         The {@code Class} that is the {@code Enum} class 
     *                          implementing {@code EnumDAOField}, for which we want 
     *                          to find an element corresponding to {@code representation}.
     * @param representation    A {@code String} representing an element of {@code enumField}.
     * @return  An element of the {@code Enum} class {@code enumField}, 
     *          corresponding to {@code representation}.
     * @throws IllegalArgumentException If {@code representation} does not correspond 
     *                                  to any element of {@code enumField}.
     */
    public static final <T extends Enum<T> & EnumDAOField> T convert(Class<T> enumField,
            String representation) {
        log.entry(enumField, representation);
        
        if (representation == null) {
            return log.exit(null);
        }
        for (T element: enumField.getEnumConstants()) {
            if (element.getStringRepresentation().equals(representation) || 
                    element.name().equals(representation)) {
                return log.exit(element);
            }
        }
        throw log.throwing(new IllegalArgumentException("\"" + representation + 
                "\" does not correspond to any element of " + enumField.getName()));
    }
    
    /**
     * Convert a {@code Set} of {@code EnumDAOField}s into a {@code Set} of {@code String}s, 
     * using the method {@link EnumDAOField#getStringRepresentation()}.
     * Generic method to avoid cast compilation errors (for instance, {@code cannot 
     * convert from List<RelationType> to List<EnumDAOField>}).
     * @param enums A {@code Set} of {@code EnumDAOField}s to be converted.
     * @return      A {@code Set} of {@code String}s that are the representation of 
     *              the {@code EnumDAOField}s contained in {@code enums}.
     * @param <T>   The type of {@code EnumDAOField}
     * 
     */
    protected static final <T extends Enum<T> & EnumDAOField> Set<String> 
        convertEnumSetToStringSet(Set<T> enums) {
        log.entry(enums);
        Set<String> stringSet = new HashSet<String>();
        for (T enumDAOField: enums) {
            stringSet.add(enumDAOField.getStringRepresentation());
        }
        return log.exit(stringSet);
    }
}
