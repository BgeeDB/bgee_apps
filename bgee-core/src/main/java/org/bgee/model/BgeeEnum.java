package org.bgee.model;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class provides convenient methods for Bgee {@code Enum}s.
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 13, Aug. 2016
 * @since   Bgee 13, Nov. 2015
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
     * Convert the {@code String} representation corresponding to an {@code BgeeEnumField} 
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
     * @throws IllegalArgumentException If {@code representation} does not correspond 
     *                                  to any element of {@code enumField}.
     */
    public static final <T extends Enum<T> & BgeeEnumField> T convert(
            Class<T> enumClass, String representation) {
        log.traceEntry("{}, {}", enumClass, representation);
        
        if (representation == null) {
            return log.traceExit((T) null);
        }
        for (T element: enumClass.getEnumConstants()) {
            if (element.getStringRepresentation().toLowerCase().equals(representation.toLowerCase()) || 
                    element.name().toLowerCase().equals(representation.toLowerCase())) {
                return log.traceExit(element);
            }
        }
        throw log.throwing(new IllegalArgumentException("\"" + representation + 
                "\" does not correspond to any element of " + enumClass.getName()));
    }
    
    /**
     * Convert a {@code Collection} of {@code String}s into a {@code Set} of {@code BgeeEnumField}s, 
     * using the method {@link BgeeEnum#convert(Class, String)}.
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
     * @param <T> The type of {@code BgeeEnumField}
     */
    public static final <T extends Enum<T> & BgeeEnumField> EnumSet<T> 
        convertStringSetToEnumSet(Class<T> enumClass, Collection<String> representations) {
        log.traceEntry("{}", representations);

        if (representations == null || representations.isEmpty()) {
            return log.traceExit((EnumSet<T>) null);
        }

        Set<String> filteredRepresentations = new HashSet<>(representations);
        EnumSet<T> enumSet = EnumSet.noneOf(enumClass);
        for (String repr: filteredRepresentations) {
            T convertedRep = convert(enumClass, repr);
            enumSet.add(convertedRep);
        }
        return log.traceExit(enumSet);
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
     * @param <T>   The type of {@code BgeeEnumField}
     */
    public static final <T extends Enum<T> & BgeeEnumField> Set<String> 
        convertEnumSetToStringSet(Set<T> enums) {
        log.traceEntry("{}", enums);
        
        if (enums == null || enums.isEmpty()) {
            return log.traceExit((Set<String>) null);
        }

        Set<String> stringSet = new HashSet<String>();
        for (T bgeeEnum: enums) {
            if (bgeeEnum != null) {
                stringSet.add(bgeeEnum.getStringRepresentation());
            }
        }
        return log.traceExit(stringSet);
    }
    
    /**
     * Defining whether {@code representation} is a representation of 
     * an element of the {@code enumClass}.
     * 
     * @param enumClass         A {@code Class<T>} that is the type of {@code BgeeEnumField}.
     * @param representation    A {@code String} that is the representation to be checked.
     * @return                  The {@code boolean} defining whether {@code representation} is a 
     *                          representation of an element of the {@code enumClass}.
     */
    public static final <T extends Enum<T> & BgeeEnumField> boolean isInEnum(
            Class<T> enumClass, String representation) {
        log.traceEntry("{}, {}", enumClass, representation);
        String lowCaseRepresentation = representation.toLowerCase(Locale.ENGLISH);
        for (T bgeeEnum: EnumSet.allOf(enumClass)) {
            if (bgeeEnum.getStringRepresentation().toLowerCase(Locale.ENGLISH).equals(lowCaseRepresentation) || 
                bgeeEnum.name().toLowerCase(Locale.ENGLISH).equals(lowCaseRepresentation)) {
                return log.traceExit(true);
            }
        }
        return log.traceExit(false);
    }
    
    /**
     * Defining whether each element of {@code representations} are a representation of 
     * an element of the {@code enumClass}.
     * 
     * @param enumClass         A {@code Class<T>} that is the type of {@code BgeeEnumField}.
     * @param representations   A {@code Collection} of {@code String}s that
     *                          are representations to be checked.
     * @return                  The {@code boolean} defining whether each element of 
     *                          {@code representations} are a representation of 
     *                          an element of the {@code enumClass}.
     * @param <T> The type of {@code BgeeEnumField}
     */
    public static final <T extends Enum<T> & BgeeEnumField> boolean areAllInEnum(
            Class<T> enumClass, Collection<String> representations) {
        log.traceEntry("{}, {}", enumClass, representations);
        if (representations == null) {
            return log.traceExit(true);
        }
        Set<String> filteredRepresentations = new HashSet<>(representations);
        for (String representation: filteredRepresentations) {
            if (!BgeeEnum.isInEnum(enumClass, representation)) {
                return log.traceExit(false);
            }
        }
        return log.traceExit(true);
    }

    public static final <T extends Enum<T>> Set<EnumSet<T>> getAllPossibleEnumCombinations(
            Class<T> enumClass, Collection<T> enums) {
        log.traceEntry("{}", enums);
        if (enums == null || enums.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Some values must be provided."));
        }
        EnumSet<T> filteredEnums = EnumSet.copyOf(enums);
        Set<EnumSet<T>> combinations = new HashSet<>();
        //we provide the class as argument so we're safe for the cast
        @SuppressWarnings("unchecked")
        T[] enumArr = filteredEnums.toArray((T[]) Array.newInstance(enumClass, filteredEnums.size()));
        final int n = enumArr.length;

        for (int i = 0; i < Math.pow(2, n); i++) {
            String bin = Integer.toBinaryString(i);
            while (bin.length() < n) {
                bin = "0" + bin;
            }
            EnumSet<T> combination = EnumSet.noneOf(enumClass);
            char[] chars = bin.toCharArray();
            for (int j = 0; j < n; j++) {
                if (chars[j] == '1') {
                    combination.add(enumArr[j]);
                }
            }
            //We don't want the combination where nothing is considered
            if (!combination.isEmpty()) {
                combinations.add(combination);
            }
        }
        return log.traceExit(combinations);
    }
}
