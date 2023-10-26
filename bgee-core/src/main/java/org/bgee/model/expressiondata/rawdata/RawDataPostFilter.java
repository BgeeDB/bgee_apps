package org.bgee.model.expressiondata.rawdata;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.rawdata.baseelements.Assay;
import org.bgee.model.expressiondata.rawdata.baseelements.Experiment;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCondition.RawDataSex;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataDataType;
import org.bgee.model.species.Species;
/**
 * A class containing values of all condition parameters used to create filters
 * 
 * @author Julien Wollbrett
 * @version Bgee 15.0, Nov. 2022
 * @since Bgee 15.0, Nov. 2022
 *
 */
public class RawDataPostFilter {
    private final static Logger log = LogManager.getLogger(RawDataPostFilter.class.getName());

    public static RawDataPostFilter merge(RawDataPostFilter f1, RawDataPostFilter f2) {
        log.traceEntry("{}, {}", f1, f2);

        if (f1 == null && f2 == null) {
            throw log.throwing(new IllegalArgumentException("Nothing to merge"));
        }
        if (f1 == null) {
            return log.traceExit(f2);
        }
        if (f2 == null) {
            return log.traceExit(f1);
        }
        if (!f1.getRequestedRawDataDataType().equals(f2.getRequestedRawDataDataType())) {
            throw log.throwing(new IllegalArgumentException(
                    "Not possible to merge RawDataPostFilter for different data types"));
        }
        return log.traceExit(new RawDataPostFilter(
                Stream.concat(f1.getAnatEntities().stream(), f2.getAnatEntities().stream())
                .collect(Collectors.toSet()),
                Stream.concat(f1.getDevStages().stream(), f2.getDevStages().stream())
                .collect(Collectors.toSet()),
                Stream.concat(f1.getCellTypes().stream(), f2.getCellTypes().stream())
                .collect(Collectors.toSet()),
                Stream.concat(f1.getSexes().stream(), f2.getSexes().stream())
                .collect(Collectors.toSet()),
                Stream.concat(f1.getStrains().stream(), f2.getStrains().stream())
                .collect(Collectors.toSet()),
                Stream.concat(f1.getSpecies().stream(), f2.getSpecies().stream())
                .collect(Collectors.toSet()),
                Stream.concat(f1.getExperiments().stream(), f2.getExperiments().stream())
                .collect(Collectors.toSet()),
                Stream.concat(f1.getAssays().stream(), f2.getAssays().stream())
                .collect(Collectors.toCollection(LinkedHashSet::new)),
                f1.getRequestedRawDataDataType()
                ));
    }

    private final Set<AnatEntity> anatEntities;
    private final Set<DevStage> devStages;
    private final Set<AnatEntity> cellTypes;
    private final Set<RawDataSex> sexes;
    private final Set<String> strains;
    private final Set<Species> species;
    private final Set<Experiment<?>> experiments;
    private final Set<Assay> assays;
    private final RawDataDataType<?, ?> requestedRawDataDataType;

    public RawDataPostFilter(RawDataDataType<?, ?> requestedRawDataDataType) {
        this(null, null, null, null, null, null, null, null, requestedRawDataDataType);
    }
    /**
     * 
     * @param anatEntities      A {@code Collection} of {@code AnatEntity} specifying the anatomical
     *                          entities to use in the filtering
     * @param devStages         A {@code Collection} of {@code DevStage} specifying the developmental
     *                          stages to use in the filtering
     * @param cellTypes         A {@code Collection} of {@code AnatEntity} specifying the cell types
     *                          to use in the filtering
     * @param sexes             A {@code Collection} of {@code RawDataSex} specifying the sexes
     *                          to use in the filtering
     * @param strains           A {@code Collection} of {@code String} specifying the strains to use
     *                          in the filtering
     * @param species           A {@code Collection} of {@code Species} specifying the species to use
     *                          in the filtering
     * @param experiments       A {@code Collection} of {@code Experiment}s specifying the experiments to use
     *                          in the filtering
     * @param assays            A {@code Collection} of {@code Assay}s specifying the assays to use
     *                          in the filtering
     * @param requestedDataType A {@code RawDataDataType} corresponding to the data type for which post
     *                          filters have been retrieved.
     */
    public RawDataPostFilter(Collection<AnatEntity> anatEntities, Collection<DevStage> devStages,
            Collection<AnatEntity> cellTypes, Collection<RawDataSex> sexes,
            Collection<String> strains, Collection<Species> species,
            Collection<Experiment<?>> experiments, Collection<Assay> assays,
            RawDataDataType<?, ?> requestedRawDataDataType) {
        if (requestedRawDataDataType == null) {
            throw new IllegalArgumentException("requestedRawDataDataType cannot be null");
        }
        this.anatEntities = Collections.unmodifiableSet(anatEntities == null? new LinkedHashSet<>():
            anatEntities.stream()
            .sorted(Comparator.comparing(ae -> ae.getName()))
            .collect(Collectors.<AnatEntity, LinkedHashSet<AnatEntity>>toCollection(LinkedHashSet::new)));
        this.devStages = Collections.unmodifiableSet(devStages == null? new LinkedHashSet<>():
            devStages.stream()
            .sorted(Comparator.comparing(ds -> ds.getName()))
            .collect(Collectors.<DevStage, LinkedHashSet<DevStage>>toCollection(LinkedHashSet::new)));
        this.cellTypes = Collections.unmodifiableSet(cellTypes == null? new LinkedHashSet<>():
            cellTypes.stream()
            .sorted(Comparator.comparing(ct -> ct.getName()))
            .collect(Collectors.<AnatEntity, LinkedHashSet<AnatEntity>>toCollection(LinkedHashSet::new)));
        this.sexes = Collections.unmodifiableSet(sexes == null? new LinkedHashSet<>():
            sexes.stream()
            .sorted(Comparator.comparing(s -> s.getStringRepresentation()))
            .collect(Collectors.<RawDataSex, LinkedHashSet<RawDataSex>>toCollection(LinkedHashSet::new)));
        this.strains = Collections.unmodifiableSet(strains == null? new LinkedHashSet<>():
            strains.stream().sorted()
            .collect(Collectors.<String, LinkedHashSet<String>>toCollection(LinkedHashSet::new)));
        this.species = Collections.unmodifiableSet(species == null? new LinkedHashSet<>():
            species.stream()
            .sorted(Comparator.comparing(s -> s.getPreferredDisplayOrder()))
            .collect(Collectors.<Species, LinkedHashSet<Species>>toCollection(LinkedHashSet::new)));
        this.experiments = Collections.unmodifiableSet(experiments == null? new LinkedHashSet<>():
            experiments.stream()
            .sorted(Comparator.comparing(e -> e.getId()))
            .collect(Collectors.<Experiment<?>, LinkedHashSet<Experiment<?>>>toCollection(LinkedHashSet::new)));
        this.assays = Collections.unmodifiableSet(assays == null? new LinkedHashSet<>():
            assays.stream()
            .sorted(Comparator.comparing(a -> requestedRawDataDataType.getAssayId(a)))
            .collect(Collectors.<Assay, LinkedHashSet<Assay>>toCollection(LinkedHashSet::new)));
        this.requestedRawDataDataType = requestedRawDataDataType;
    }

    /**
     * @return  A {@code Set} of {@code AnatEntity}s present in a raw data query result.
     *          The underlying instance is a {@code LinkedHashSet},
     *          but returned as a {@code Set} to be unmodifiable.
     */
    public Set<AnatEntity> getAnatEntities() {
        return anatEntities;
    }
    /**
     * @return  A {@code Set} of {@code DevStage}s present in a raw data query result.
     *          The underlying instance is a {@code LinkedHashSet},
     *          but returned as a {@code Set} to be unmodifiable.
     */
    public Set<DevStage> getDevStages() {
        return devStages;
    }
    /**
     * @return  A {@code Set} of {@code AnatEntity}s representing cell types,
     *          present in a raw data query result.
     *          The underlying instance is a {@code LinkedHashSet},
     *          but returned as a {@code Set} to be unmodifiable.
     */
    public Set<AnatEntity> getCellTypes() {
        return cellTypes;
    }
    /**
     * @return  A {@code Set} of {@code RawDataSex}s present in a raw data query result.
     *          The underlying instance is a {@code LinkedHashSet},
     *          but returned as a {@code Set} to be unmodifiable.
     */
    public Set<RawDataSex> getSexes() {
        return sexes;
    }
    /**
     * @return  A {@code Set} of {@code String}s representing strains,
     *          present in a raw data query result.
     *          The underlying instance is a {@code LinkedHashSet},
     *          but returned as a {@code Set} to be unmodifiable.
     */
    public Set<String> getStrains() {
        return strains;
    }
    /**
     * @return  A {@code Set} of {@code Species}s present in a raw data query result.
     *          The underlying instance is a {@code LinkedHashSet},
     *          but returned as a {@code Set} to be unmodifiable.
     */
    public Set<Species> getSpecies() {
        return species;
    }
    /**
     * @return  A {@code Set} of {@code Experiment}s present in a raw data query result.
     *          The underlying instance is a {@code LinkedHashSet},
     *          but returned as a {@code Set} to be unmodifiable.
     */
    public Set<Experiment<?>> getExperiments() {
        return experiments;
    }
    /**
     * @return  A {@code Set} of {@code Assay}s present in a raw data query result.
     *          The underlying instance is a {@code LinkedHashSet},
     *          but returned as a {@code Set} to be unmodifiable.
     */
    public Set<Assay> getAssays() {
        return assays;
    }
    /**
     * @return  The {@code RawDataDataType} for which this {@code RawDataPostFilter} was requested.
     */
    public RawDataDataType<?,?> getRequestedRawDataDataType() {
        return this.requestedRawDataDataType;
    }
    /**
     * @return  The {@code DataType} for which this {@code RawDataPostFilter} was requested.
     */
    public DataType getRequestedDataType() {
        return this.requestedRawDataDataType.getDataType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(anatEntities, cellTypes, devStages, sexes, strains,
                species, experiments, assays, requestedRawDataDataType);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RawDataPostFilter other = (RawDataPostFilter) obj;
        return Objects.equals(anatEntities, other.anatEntities)
                && Objects.equals(cellTypes, other.cellTypes)
                && Objects.equals(devStages, other.devStages)
                && Objects.equals(sexes, other.sexes)
                && Objects.equals(strains, other.strains)
                && Objects.equals(species, other.species)
                && Objects.equals(experiments, other.experiments)
                && Objects.equals(assays, other.assays)
                && Objects.equals(requestedRawDataDataType, other.requestedRawDataDataType);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RawDataPostFilter [anatEntities=").append(anatEntities)
               .append(", devStages=").append(devStages)
               .append(", cellTypes=").append(cellTypes)
               .append(", sexes=").append(sexes)
               .append(", strains=").append(strains)
               .append(", species=").append(species)
               .append(", experiments=").append(experiments)
               .append(", assays=").append(assays)
               .append(", requestedRawDataDataType=").append(requestedRawDataDataType)
               .append("]");
        return builder.toString();
    }
}