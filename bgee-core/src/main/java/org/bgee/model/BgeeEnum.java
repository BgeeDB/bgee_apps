package org.bgee.model;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class provides convenient methods for Bgee {@code Enum}s.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Nov 2015
 * @since   Bgee 13, Nov 2015
 */
public abstract class BgeeEnum {
    
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(BgeeEnum.class.getName());

    /**
     * An interface that must be implemented by {@code Enum}s to be able to get string 
     * representation using {@link #convert(Class, String)}
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 13, Nov 2015
     * @since   Bgee 13, Nov 2015
     */
    public interface BgeeEnumField {
        /**
         * @return  A {@code String} corresponding to this {@code BgeeEnumField} element.
         */
        public String getStringRepresentation();
    }
    
    /**
     * Convert the {@code String} representation corresponding to an {@link BgeeEnumField} 
     * (for instance, retrieved from request) into the proper {@code Enum} element.
     * <p>
     * This method compares {@code representation} to the value returned by 
     * {@link BgeeEnumField#getStringRepresentation()}, as well as to the value 
     * returned by {@link Enum#name()}, for each {@code Enum} element corresponding to 
     * {@code enumField}.
     * 
     * @param enumClass                 The {@code Class} that is the {@code Enum} class 
     *                                  implementing {@code BgeeEnumField}, for which we want 
     *                                  to find an element corresponding to {@code representation}.
     * @param representation            A {@code String} representing an element of {@code enumField}.
     * @return                          An element of the {@code Enum} class {@code enumField}, 
     *                                  corresponding to {@code representation}. Can be {@code null}
     *                                  if {@code representation} is {@code null}.
     * @throw IllegalArgumentException  If {@code representation} does not correspond 
     *                                  to any element of {@code enumField}.
     */
    public static final <T extends Enum<T> & BgeeEnumField> T convert(
            Class<T> enumClass, String representation) {
        log.entry(enumClass, representation);
        
        if (representation == null) {
            return log.exit(null);
        }
        for (T element: enumClass.getEnumConstants()) {
            if (element.getStringRepresentation().equals(representation) || 
                    element.name().equals(representation)) {
                return log.exit(element);
            }
        }
        throw log.throwing(new IllegalArgumentException("\"" + representation + 
                "\" does not correspond to any element of " + enumClass.getName()));
    }
    
    /**
     * Convert a {@code Collection} of {@code String}s into a {@code Set} of {@code BgeeEnumField}s, 
     * using the method {@link BgeeEnum#convert()}.
     * 
     * @param enumClass         The {@code Class} that is the {@code Enum} class 
     *                          implementing {@code BgeeEnumField}, for which we want 
     *                          to find an element corresponding to {@code representation}.
     * @param representations   A {@code Collection} of {@code String}s to be converted.
     * @return                  The {@code Set} of {@code BgeeEnumField}s that are the 
     *                          representation of the {@code BgeeEnumField}s contained in 
     *                          {@code enums}. Can be {@code null} if {@code representations} is
     *                          {@code null}. An element can be {@code null} if 
     *                          {@code representations} has an element {@code null}.
     * @param T The type of {@code BgeeEnumField}
     */
    public static final <T extends Enum<T> & BgeeEnumField> Set<T> 
        convertStringSetToEnumSet(Class<T> enumClass, Collection<String> representations) {
        log.entry(representations);

        if (representations == null || representations.isEmpty()) {
            return log.exit(null);
        }

        Set<String> filteredRepresentations = new HashSet<>(representations);
        Set<T> enumSet = new HashSet<>();
        for (String repr: filteredRepresentations) {
            T convertedRep = convert(enumClass, repr);
            enumSet.add(convertedRep);
        }
        return log.exit(enumSet);
    }

    /**
     * Convert a {@code Set} of {@code BgeeEnumField}s into a {@code Set} of {@code String}s, 
     * using the method {@link BgeeEnumField#getStringRepresentation()}.
     * Generic method to avoid cast compilation errors (for instance, {@code cannot 
     * convert from List<DataType> to List<BgeeEnumField>}).
     * 
     * @param enums A {@code Set} of {@code BgeeEnumField}s to be converted.
     * @return      A {@code Set} of {@code String}s that are the representation of 
     *              the {@code BgeeEnumField}s contained in {@code enums}. Can be {@code null} if 
     *              {@code enums} is {@code null}. An element can be {@code null} if
     *              {@code enums} has an element {@code null}.
     * @param T The type of {@code BgeeEnumField}
     */
    public static final <T extends Enum<T> & BgeeEnumField> Set<String> 
        convertEnumSetToStringSet(Set<T> enums) {
        log.entry(enums);
        
        if (enums == null || enums.isEmpty()) {
            return log.exit(null);
        }

        Set<String> stringSet = new HashSet<String>();
        for (T bgeeEnum: enums) {
            if (bgeeEnum != null) {
                stringSet.add(bgeeEnum.getStringRepresentation());
            }
        }
        return log.exit(stringSet);
    }
    
    /**
     * @param enumClass
     * @param representation
     * @return
     */
    public static final <T extends Enum<T> & BgeeEnumField> boolean isInEnum(
            Class<T> enumClass, String representation) {
        log.entry(enumClass, representation);
        for (BgeeEnumField bgeeEnum: EnumSet.allOf(enumClass)) {
            if (bgeeEnum.getStringRepresentation().equals(representation)) {
                return log.exit(true);
            }
        }
        return log.exit(false);
    }
    
    /**
     * @param enumClass
     * @param representations
     * @return
     * @param T The type of {@code BgeeEnumField}
     */
    public static final <T extends Enum<T> & BgeeEnumField> boolean areAllInEnum(
            Class<T> enumClass, Collection<String> representations) {
        log.entry(enumClass, representations);
        if (representations == null) {
            return log.exit(true);
        }
        Set<String> filteredRepresentations = new HashSet<>(representations);
        for (String representation: filteredRepresentations) {
            if (!BgeeEnum.isInEnum(enumClass, representation)) {
                return log.exit(false);
            }
        }
        return log.exit(true);
    }
}
