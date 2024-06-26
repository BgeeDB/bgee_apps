package org.bgee.model.expressiondata.baseelements;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeEnum;
import org.bgee.model.BgeeEnum.BgeeEnumField;

/**
 * An {@code enum} defining the expression data types used in Bgee:
 * <ul>
 * <li>{@code AFFYMETRIX}: microarray Affymetrix.
 * <li>{@code EST}: Expressed Sequence Tag.
 * <li>{@code IN_SITU}: <em>in situ</em> hybridization data.
 * <li>{@code RELAXED_IN_SITU}: use of <em>in situ</em> hybridization data 
 * to infer more information about absence of expression: the inference 
 * considers expression patterns described by <em>in situ</em> data as complete. 
 * It is indeed usual for authors of <em>in situ</em> hybridizations to report 
 * only localizations of expression, implicitly stating absence of expression 
 * in all other tissues. When <em>in situ</em> data are available for a gene, 
 * this data type considered that absence of expression is assumed in any organ existing 
 * at the developmental stage studied in the <em>in situ</em>, with no report of 
 * expression by any data type, in the organ itself, or any substructure. 
 * <li>{@code SC_RNA_SEQ}: Full length single cell RNA-Seq data.
 * <li>{@code RNA_SEQ}: RNA-Seq data.
 * </ul>
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Sept. 2015
 * @since Bgee 13
 */
//TODO: why don't we have a "ALL" data type?? This would be much cleaner than having to provide "null" 
//everywhere...
public enum DataType implements BgeeEnumField {
    AFFYMETRIX("Affymetrix", true, null, true), EST("EST", false, null, false),
    IN_SITU("in situ hybridization", true, null, true),
    //Note: It is important to keep SC_RNA_SEQ before RNA_SEQ until we fix the issue of retrieving
    // experiment information consisting in different datatype (e.g both RNA_SEQ and SC_RNA_SEQ) as
    // it defines libraries from which datatype will be retrieved. For now if an experiment has both
    // bulk and single cell libraries then single cell libraries are retrieved.
    SC_RNA_SEQ("single-cell RNA-Seq", false, true, true), RNA_SEQ("RNA-Seq", true, false, true);

    private final static Logger log = LogManager.getLogger(DataType.class.getName());

    private final static Set<EnumSet<DataType>> ALL_DATA_TYPE_COMBINATIONS = 
            getAllPossibleDataTypeCombinations(EnumSet.allOf(DataType.class));
    
    private final String representation;
    private final boolean trustedForAbsentCalls;
    private final Boolean singleCell;
    private final boolean withExperiments;

    //Important to have the distinction "null" singleCell and true/false singleCell.
    //Because that would mess up with some internal code if we were using "false" instead of "null"
    //when not appropriate.
    private DataType(String representation, boolean trustedForAbsentCalls, Boolean singleCell,
            boolean withExperiments) {
        this.representation = representation;
        this.trustedForAbsentCalls = trustedForAbsentCalls;
        this.singleCell = singleCell;
        this.withExperiments = withExperiments;
    }

    @Override
    public String getStringRepresentation() {
        return this.representation;
    }
    /**
     * @return  A {@code boolean} that is {@code true} if this {@code DataType} can be used
     *          for generating ABSENT expression calls, {@code false} otherwise.
     */
    public boolean isTrustedForAbsentCalls() {
        return this.trustedForAbsentCalls;
    }
    /**
     * @return  A {@code Boolean} that is {@code null} if there is no notion of "single-cell data"
     *          for this {@code DataType}, {@code false} if there is a notion of "single-cell data"
     *          with the related technology but the data are produced as bulk, {@code true}
     *          if this {@code DataType} produces single-cell data.
     */
    //Important to have the distinction "null" singleCell and true/false singleCell.
    //Because that would mess up with some internal code if we were using "false" instead of "null"
    //when not appropriate.
    public Boolean getSingleCell() {
        return this.singleCell;
    }
    /**
     * @return  A {@code boolean} indicating whether this {@code DataType} has a concept of experiments,
     *          or only of assays.
     */
    public boolean isWithExperiments() {
        return this.withExperiments;
    }
    
    /**
     * Convert the {@code Collection} of {@code String}s that are string representations of data types
     * into a {@code Set} of {@code DataType}s.
     * Operation performed by calling {@link BgeeEnum#convertStringSetToEnumSet(Class, Collection)}
     * with {@code DataType} as the {@code Class} argument, and {@code representation} 
     * as the {@code String} argument.
     * 
     * @param representations           A {@code Collection} of {@code String}s that are string
     *                                  representations of data types.
     * @return                          A {@code Set} of {@code DataType}s corresponding 
     *                                  to {@code representations}.
     * @throws IllegalArgumentException If a representation does not correspond 
     *                                  to any {@code DataType}.
     */
    public static final EnumSet<DataType> convertToDataTypeSet(Collection<String> representations) {
        return BgeeEnum.convertStringSetToEnumSet(DataType.class, representations);
    }

    public static final Set<EnumSet<DataType>> getAllPossibleDataTypeCombinations() {
        log.traceEntry();
        //defensive copying
        return log.traceExit(ALL_DATA_TYPE_COMBINATIONS.stream()
                .map(s -> EnumSet.copyOf(s))
                .collect(Collectors.toSet()));
    }
    public static final Set<EnumSet<DataType>> getAllPossibleDataTypeCombinations(
            Collection<DataType> dataTypes) {
        log.traceEntry("{}", dataTypes);
        if (dataTypes == null || dataTypes.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Some data types must be provided."));
        }
        return log.traceExit(BgeeEnum.getAllPossibleEnumCombinations(DataType.class, dataTypes));
    }
    public static EnumSet<DataType> findCombinationWithGreatestOverlap(
            Collection<EnumSet<DataType>> dataTypeCombinations, Collection<DataType> dataTypeCombination) {
        log.traceEntry("{}, {}", dataTypeCombinations, dataTypeCombination);

        Set<EnumSet<DataType>> clonedDataTypeCombinations = new HashSet<>(dataTypeCombinations);
        EnumSet<DataType> clonedDataTypeCombination = EnumSet.copyOf(dataTypeCombination);

        int bestDataTypeMatchCount = 0;
        EnumSet<DataType>  mostMatchedCombination = null;
        for (EnumSet<DataType> comb: clonedDataTypeCombinations) {
            if (clonedDataTypeCombination.equals(comb)) {
                return log.traceExit(EnumSet.copyOf(comb));
            }
            if (!clonedDataTypeCombination.containsAll(comb)) {
                continue;
            }
            EnumSet<DataType> clonedComb = EnumSet.copyOf(comb);
            clonedComb.retainAll(clonedDataTypeCombination);
            int dataTypeMatchCount = clonedComb.size();
            if (dataTypeMatchCount > bestDataTypeMatchCount) {
                bestDataTypeMatchCount = dataTypeMatchCount;
                mostMatchedCombination = comb;
            }
        }
        if (mostMatchedCombination == null) {
            return log.traceExit((EnumSet<DataType>) null);
        }
        return log.traceExit(EnumSet.copyOf(mostMatchedCombination));
    }
}
