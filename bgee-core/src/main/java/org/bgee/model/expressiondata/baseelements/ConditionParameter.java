package org.bgee.model.expressiondata.baseelements;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.Sex;
import org.bgee.model.anatdev.Sex.SexEnum;
import org.bgee.model.anatdev.Strain;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.expressiondata.call.ConditionParameterValue;
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
public abstract class ConditionParameter<T extends ConditionParameterValue, U> {
    private final static Logger log = LogManager.getLogger(ConditionParameter.class.getName());

    public static class AnatEntityCondParam extends ConditionParameter<AnatEntity, AnatEntity> {
        private AnatEntityCondParam() {
            super(AnatEntity.class, new AnatEntity(ConditionDAO.ANAT_ENTITY_ROOT_ID), AnatEntity.class,
                    (o) -> o.getId(), (o) -> o.getId(), null,
                    "anatEntity", "Anat. entity", "anat_entity",
                    "anat_entity_id", "filter_anat_entity_id", "discard_anat_entity_and_children_id",
                    "anatEntity.id", "anatEntity.id", "anat_entity_descendant",
                    true, true, 1);
        }
    }
    public static class CellTypeCondParam extends ConditionParameter<AnatEntity, AnatEntity> {
        private CellTypeCondParam() {
            super(AnatEntity.class, new AnatEntity(ConditionDAO.CELL_TYPE_ROOT_ID), AnatEntity.class,
                    (o) -> o.getId(), (o) -> o.getId(), null,
                    "cellType", "Cell type", "cell_type",
                    "cell_type_id", "filter_cell_type_id", null,
                    "cellType.id", "cellType.id",
                    "cell_type_descendant", true, true, 1);
        }
    }
    public static class DevStageCondParam extends ConditionParameter<DevStage, DevStage> {
        private DevStageCondParam() {
            super(DevStage.class, new DevStage(ConditionDAO.DEV_STAGE_ROOT_ID), DevStage.class,
                    (o) -> o.getId(), (o) -> o.getId(), null,
                    "devStage", "Developmental and life stage", "dev_stage",
                    "stage_id", "filter_stage_id", null,
                    "devStage.id", "devStage.id", "stage_descendant",
                    true, true, 2);
        }
    }
    public static class SexCondParam extends ConditionParameter<Sex, RawDataSex> {
        private SexCondParam() {
            super(Sex.class, new Sex(SexEnum.ANY.getStringRepresentation()), RawDataSex.class,
                    (o) -> o.getId(), (o) -> o.getStringRepresentation(),
                    EnumSet.allOf(SexEnum.class)
                    .stream()
                    .map(e -> e.name())
                    .collect(Collectors.toSet()),
                    "sex", "Sex", "sex", "sex", "filter_sex", null,
                    "sex.id", "sex", null,
                    false, false, 3);
        }
    }
    //XXX: This String for raw data strain is the only reason why we cannot constraint this ConditionParameter
    //to be ConditionParameter<T extends ConditionAttribute, U extends RawDataConditionAttribute>
    public static class StrainCondParam extends ConditionParameter<Strain, String> {
        private StrainCondParam() {
            super(Strain.class, new Strain(ConditionDAO.STRAIN_ROOT_ID), String.class,
                    (o) -> o.getId(), (o) -> o,
                    //Debatable whether we should provide all strains here
                    null,
                    "strain", "Strain", "strain", "strain", "filter_strain", null,
                    "strain.id", "strain", null,
                    false, false, 4);
        }
    }


    //XXX: this one might be simply call ANAT_ENTITY, or ANAT_STRUCTURE_CELL_TYPE
    public static final AnatEntityCondParam ANAT_ENTITY = new AnatEntityCondParam();
    public static final CellTypeCondParam   CELL_TYPE   = new CellTypeCondParam();
    public static final DevStageCondParam   DEV_STAGE   = new DevStageCondParam();
    public static final SexCondParam        SEX         = new SexCondParam();
    public static final StrainCondParam     STRAIN      = new StrainCondParam();
    private final static LinkedHashSet<ConditionParameter<?, ?>> ALL_OF =
            new LinkedHashSet<>(Arrays.asList(
                    ANAT_ENTITY,
                    CELL_TYPE,
                    DEV_STAGE,
                    SEX,
                    STRAIN));
    private final static Set<LinkedHashSet<ConditionParameter<? extends ConditionParameterValue, ?>>> ALL_COND_PARAM_COMBINATIONS =
          getAllPossibleCombinations(ALL_OF);
    private final static LinkedHashMap<Integer, LinkedHashSet<ConditionParameter<?, ?>>> GROUP_ID_TO_COND_PARAMS =
            ALL_OF.stream().collect(Collectors.toMap(
                    cp -> cp.getGroupId(),
                    cp -> new LinkedHashSet<>(Set.of(cp)),
                    (v1, v2) -> {v1.addAll(v2); return v1;},
                    () -> new LinkedHashMap<>()));


    public static final LinkedHashMap<Integer, LinkedHashSet<ConditionParameter<?, ?>>> getGroupIdToCondParams() {
        log.traceEntry();
        //defensive copying
        return log.traceExit(GROUP_ID_TO_COND_PARAMS.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> new LinkedHashSet<>(e.getValue()),
                        (v1, v2) -> {throw log.throwing(new IllegalStateException("no key collision possible"));},
                        () -> new LinkedHashMap<>())));
    }
    /**
     * @return  The returned {@code LinkedHashSet} can be safely modified.
     */
    public static final LinkedHashSet<ConditionParameter<? extends ConditionParameterValue, ?>> allOf() {
        log.traceEntry();
        //defensive copying, we don't want to return an unmodifiable Set
        return log.traceExit(new LinkedHashSet<>(ALL_OF));
    }
    public static final LinkedHashSet<ConditionParameter<? extends ConditionParameterValue, ?>> noneOf() {
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
    public static final LinkedHashSet<ConditionParameter<? extends ConditionParameterValue, ?>> copyOf(
            Collection<ConditionParameter<?, ?>> c) {
        log.traceEntry("{}", c);
        //to mimic the behavior of EnumSet.copyOf
        if (c == null) {
            throw log.throwing(new NullPointerException(
                    "The provided Collection cannot be null"));
        }
        //We cannot call .contains(null) on Collections not accepting null values
        if (c.stream().anyMatch(e -> e == null)) {
            throw log.throwing(new NullPointerException(
                    "The provided Collection cannot contain null elements"));
        }
        //We want the order to be guaranteed, so we iterate ALL_OF
        return log.traceExit(ALL_OF.stream()
                .filter(param -> c.contains(param))
                .collect(Collectors.toCollection(() -> new LinkedHashSet<>())));
    }
    public static final LinkedHashSet<ConditionParameter<? extends ConditionParameterValue, ?>> getCondParams(
            Collection<ConditionParameter<?, ?>> c) {
        log.traceEntry("{}", c);
        if (c == null || c.isEmpty()) {
            return log.traceExit(allOf());
        }
        return log.traceExit(copyOf(c));
    }

    public static final Set<LinkedHashSet<ConditionParameter<? extends ConditionParameterValue, ?>>> getAllPossibleCombinations() {
        //defensive copying
        return ALL_COND_PARAM_COMBINATIONS.stream()
                .map(s -> copyOf(s))
                .collect(Collectors.toSet());
    }
    public static final Set<LinkedHashSet<ConditionParameter<? extends ConditionParameterValue, ?>>> getAllPossibleCombinations(
            Collection<ConditionParameter<?, ?>> condParams) {
        log.traceEntry("{}", condParams);
        if (condParams == null || condParams.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Some values must be provided."));
        }
        //We cannot call .contains(null) on Collections not accepting null values
        if (condParams.stream().anyMatch(e -> e == null)) {
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
    private final T condValueRoot;
    private final Class<U> rawDataCondValueType;

    //We could still add two other generic types if we wanted to specify
    //a type of ID different from String
    private final Function<T, String> condValueIdFun;
    private final Function<U, String> rawDataCondValueIdFun;
    //see javadoc getAllPossibleValueIds
    private final Set<String> allPossibleCondValueIds;

    private final String attributeName;
    private final String displayName;
    private final String parameterName;
    private final String requestParameterName;
    private final String requestFilterParameterName;
    private final String requestDiscardParameterName;
    //Corresponds to the attributes in Condition2 transformed into JSON, e.g., "sex.id"
    //for class Sex stored in the attribute 'sex' in Condition2, and the Sex class has an 'id' attribute
    private final String condTargetAttributeName;
    //Corresponds to the attributes in RawDataCondition transformed into JSON, e.g., "sex" or "strain"
    //corresponding to those attributes in RawDataCondition and that are Strings.
    //For other objects such as anatEntity, it will be "anatEntity.id" since they are classes
    //with an 'id' attribute
    private final String rawDataCondTargetAttributeName;
    private final String requestDescendantParameterName;
    private final boolean informativeId;
    private final boolean withRequestableDescendants;
    //Some CondtionParameters are related and can be grouped together,
    //such as ANAT_ENTITY and CELL_TYPE. They will have the same groupId.
    private final int groupId;

    private ConditionParameter(Class<T> condValueType, T condValueRoot, Class<U> rawDataCondValueType,
            Function<T, String> condValueIdFun, Function<U, String> rawDataCondValueIdFun,
            Set<String> allPossibleCondValueIds,
            String attributeName, String displayName, String parameterName,
            String requestParameterName, String requestFilterParameterName,
            String requestDiscardParameterName,
            String condTargetAttributeName, String rawDataCondTargetAttributeName,
            String requestDescendantParameterName,
            boolean informativeId, boolean withRequestableDescendants,
            int groupId) {
        this.condValueType = condValueType;
        this.condValueRoot = condValueRoot;
        this.rawDataCondValueType = rawDataCondValueType;
        this.condValueIdFun = condValueIdFun;
        this.rawDataCondValueIdFun = rawDataCondValueIdFun;
        this.allPossibleCondValueIds = allPossibleCondValueIds == null? null:
            Collections.unmodifiableSet(allPossibleCondValueIds);
        this.attributeName = attributeName;
        this.displayName = displayName;
        this.parameterName = parameterName;
        this.requestParameterName = requestParameterName;
        this.requestFilterParameterName = requestFilterParameterName;
        this.requestDiscardParameterName = requestDiscardParameterName;
        this.condTargetAttributeName = condTargetAttributeName;
        this.rawDataCondTargetAttributeName = rawDataCondTargetAttributeName;
        this.requestDescendantParameterName = requestDescendantParameterName;
        this.informativeId = informativeId;
        this.withRequestableDescendants = withRequestableDescendants;
        this.groupId = groupId;
    }

    public Class<T> getCondValueType() {
        return condValueType;
    }
    public T getCondValueRoot() {
        return condValueRoot;
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
    /**
     * If in a {@code Condition2}, the objects associated with this {@code ConditionParameter}
     * only accepts a restrictive set of IDs (e.g., {@code ConditionParameter.SEX} for class {@code Sex}
     * in attribute {@code Condition2#getSex()}), return them. Otherwise, return {@code null}.
     *
     * @return  A {@code Set} of {@code String}s that are all the possible IDs that can be accepted
     *          associated with this {@code ConditionParameter}, {@code null} if there is no restriction.
     */
    public Set<String> getAllPossibleCondValueIds() {
        return allPossibleCondValueIds;
    }
    public String getAttributeName() {
        return attributeName;
    }
    public String getDisplayName() {
        return displayName;
    }
    public String getParameterName() {
        return parameterName;
    }
    public String getRequestParameterName() {
        return requestParameterName;
    }
    public String getRequestFilterParameterName() {
        return requestFilterParameterName;
    }
    public String getRequestDiscardParameterName() {
        return requestDiscardParameterName;
    }
    public String getCondTargetAttributeName() {
        return condTargetAttributeName;
    }
    public String getRawDataCondTargetAttributeName() {
        return rawDataCondTargetAttributeName;
    }
    public String getRequestDescendantParameterName() {
        return requestDescendantParameterName;
    }
    public boolean isInformativeId() {
        return informativeId;
    }
    public boolean isWithRequestableDescendants() {
        return withRequestableDescendants;
    }
    public int getGroupId() {
        return groupId;
    }


    @Override
    public String toString() {
        return this.getAttributeName();
    }

    //****************************
    //No need for hashCode/equals, there will be only one instance of each of these classes,
    //their constructor is private and they will be retrieved only through
    //the public static final attributes of this class
    //****************************
}