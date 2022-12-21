package org.bgee.model.expressiondata.baseelements;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Entity;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.Sex;
import org.bgee.model.anatdev.Strain;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataSex;

/**
 * This class allows to mimic an {@code Enum} defining condition parameters. We need it
 * over an {@code Enum} as we want to define generic types.
 * <p>
 * The constructors of this class and sub-classes are private. Some static public attributes
 * are provided to obtain specific implementations, in order to mimic an {@code enum}:
 * <ul>
 * <li>{@link #ANAT_ENTITY_CELL_TYPE}
 * <li>{@link #DEV_STAGE}
 * <li>{@link #SEX}
 * <li>{@link #STRAIN}
 * </ul>
 *
 * @author Frederic Bastian
 * @version Bgee 15.0, Dec. 2022
 * @version Bgee 15.0, Dec. 2022
 * @param <T>   The type of object returned from a {@code Condition2} object when calling
 *              the method {@code BaseCondition2#getCondParamValue(ConditionParameter)}
 *              using this {@code ConditionParameter}.
 * @param <U>   The type of object returned from a {@code RawDataCondition} object when calling
 *              the method {@code BaseCondition2#getCondParamValue(ConditionParameter)}
 *              using this {@code ConditionParameter}.
 */
public abstract class ConditionParameter<T extends Entity<?>, U> {
    private final static Logger log = LogManager.getLogger(ConditionParameter.class.getName());

    public static class AnatEntityCondParam extends ConditionParameter<AnatEntity, AnatEntity> {
        private AnatEntityCondParam() {
            super(AnatEntity.class, AnatEntity.class, (o) -> o.getId(), (o) -> o.getId(),
                    "anat_entity", "Anat. entity");
        }
    }
    public static class DevStageCondParam extends ConditionParameter<DevStage, DevStage> {
        private DevStageCondParam() {
            super(DevStage.class, DevStage.class,
                    (o) -> o.getId(), (o) -> o.getId(),
                    "dev_stage", "Dev. stage");
        }
    }
    public static class SexCondParam extends ConditionParameter<Sex, RawDataSex> {
        private SexCondParam() {
            super(Sex.class, RawDataSex.class,
                    (o) -> o.getId(), (o) -> o.getStringRepresentation(),
                    "sex", "Sex");
        }
    }
    public static class StrainCondParam extends ConditionParameter<Strain, String> {
        private StrainCondParam() {
            super(Strain.class, String.class,
                    (o) -> o.getId(), (o) -> o,
                    "strain", "Strain");
        }
    }


    //XXX: this one might be simply call ANAT_ENTITY, or ANAT_STRUCTURE_CELL_TYPE
    public static final AnatEntityCondParam ANAT_ENTITY_CELL_TYPE = new AnatEntityCondParam();
    public static final DevStageCondParam   DEV_STAGE             = new DevStageCondParam();
    public static final SexCondParam        SEX                   = new SexCondParam();
    public static final StrainCondParam     STRAIN                = new StrainCondParam();
    private final static LinkedHashSet<ConditionParameter<?, ?>> ALL_OF =
            new LinkedHashSet<>(Arrays.asList(
                    ANAT_ENTITY_CELL_TYPE,
                    DEV_STAGE,
                    SEX,
                    STRAIN));
    private final static Set<LinkedHashSet<ConditionParameter<?, ?>>> ALL_COND_PARAM_COMBINATIONS =
          getAllPossibleCombinations(ALL_OF);


    /**
     * @return  The returned {@code LinkedHashSet} can be safely modified.
     */
    public static final LinkedHashSet<ConditionParameter<?, ?>> allOf() {
        log.traceEntry();
        //defensive copying, we don't want to return an unmodifiable Set
        return log.traceExit(new LinkedHashSet<>(ALL_OF));
    }
    public static final LinkedHashSet<ConditionParameter<?, ?>> noneOf() {
        log.traceEntry();
        return log.traceExit(new LinkedHashSet<>());
    }
    /**
     * Creates a {@code LinkedHashSet} containing the same elements as in the provided
     * {@code Collection} (if any). The order of the elements in the returned
     * {@code LinkedHashSet} is guaranteed to be always the same, whatever the order
     * in the provided {@code Collection}, and at any call to this method. This is to reproduce
     * the behavior of {@code EnumSet.copyOf}.
     *
     * @param c The {@code Collection} from which to initialize this {@code LinkedHashSet}.
     * @return  A {@code LinkedHashSet} initialized from the given {@code Collection}.
     * @throws NullPointerException     if {@code c} is {@code null}
     *                                  or contains a {@code null} element.
     */
    public static final LinkedHashSet<ConditionParameter<?, ?>> copyOf(
            Collection<ConditionParameter<?, ?>> c) {
        log.traceEntry("{}", c);
        //to mimic the behavior of EnumSet.copyOf
        if (c == null) {
            throw log.throwing(new NullPointerException(
                    "The provided Collection cannot be null"));
        }
        if (c.contains(null)) {
            throw log.throwing(new NullPointerException(
                    "The provided Collection cannot contain null elements"));
        }
        //We want the order to be guaranteed, so we iterate ALL_OF
        return log.traceExit(ALL_OF.stream()
                .filter(param -> c.contains(param))
                .collect(Collectors.toCollection(() -> new LinkedHashSet<>())));
    }

    public static final Set<LinkedHashSet<ConditionParameter<?, ?>>> getAllPossibleCombinations() {
        //defensive copying
        return ALL_COND_PARAM_COMBINATIONS.stream()
                .map(s -> copyOf(s))
                .collect(Collectors.toSet());
    }
    public static final Set<LinkedHashSet<ConditionParameter<?, ?>>> getAllPossibleCombinations(
            Collection<ConditionParameter<?, ?>> condParams) {
        log.traceEntry("{}", condParams);
        if (condParams == null || condParams.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Some values must be provided."));
        }
        if (condParams.contains(null)) {
            //Set.copyOf refuses null values
            throw log.throwing(new IllegalArgumentException("No value can be null."));
        }
        //copyOf will filter redundant values
        LinkedHashSet<ConditionParameter<?, ?>> filteredParams = copyOf(condParams);
        Set<LinkedHashSet<ConditionParameter<?, ?>>> combinations = new HashSet<>();

        ConditionParameter<?, ?>[] arr = filteredParams.toArray((ConditionParameter<?, ?>[])
                Array.newInstance(ConditionParameter.class, filteredParams.size()));
        final int n = arr.length;

        for (int i = 0; i < Math.pow(2, n); i++) {
            String bin = Integer.toBinaryString(i);
            while (bin.length() < n) {
                bin = "0" + bin;
            }
            LinkedHashSet<ConditionParameter<?, ?>> combination = noneOf();
            char[] chars = bin.toCharArray();
            for (int j = 0; j < n; j++) {
                if (chars[j] == '1') {
                    combination.add(arr[j]);
                }
            }
            //We don't want the combination where nothing is considered
            if (!combination.isEmpty()) {
                //we want a predictable iteration order,
                //but the order is guaranteed by the fact that we used copyOf
                //at the beginning
                combinations.add(combination);
            }
        }
        return log.traceExit(combinations);
    }


    private final Class<T> condValueType;
    private final Class<U> rawDataCondValueType;
    //We could still add two other generic types if we wanted to specify
    //a type of ID different from String
    private final Function<T, String> condValueIdFun;
    private final Function<U, String> rawDataCondValueIdFun;
    private final String stringRepresentation;
    private final String displayName;

    private ConditionParameter(Class<T> condValueType, Class<U> rawDataCondValueType,
            Function<T, String> condValueIdFun, Function<U, String> rawDataCondValueIdFun,
            String stringRepresentation, String displayName) {
        this.condValueType = condValueType;
        this.rawDataCondValueType = rawDataCondValueType;
        this.condValueIdFun = condValueIdFun;
        this.rawDataCondValueIdFun = rawDataCondValueIdFun;
        this.stringRepresentation = stringRepresentation;
        this.displayName = displayName;
    }

    public Class<T> getCondValueType() {
        return condValueType;
    }
    public Class<U> getRawDataCondValueType() {
        return rawDataCondValueType;
    }
    public Function<T, String> getCondValueIdFun() {
        return condValueIdFun;
    }
    public Function<U, String> getRawDataCondValueIdFun() {
        return rawDataCondValueIdFun;
    }
    public String getStringRepresentation() {
        return stringRepresentation;
    }
    public String getDisplayName() {
        return displayName;
    }



    //****************************
    //No need for hashCode/equals, there will be only one instance of each of these classes,
    //their constructor is private and they will be retrieved only through
    //the public static final attributes of this class
    //****************************
}