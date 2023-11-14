package org.bgee.model.species;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgee.model.NamedEntity;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.source.Source;

/**
 * Class allowing to describe species used in Bgee.
 * 
 * @author  Frederic Bastian
 * @author  Philippe Moret
 * @author  Valentine Rech de Laval
 * @version Bgee 15, Oct. 2021
 * @since   Bgee 13, Mar. 2013
 */
public class Species extends NamedEntity<Integer> {
    
	/** @see #getGenus() */
	private final String genus;
	
    /** @see #getSpeciesName() */
    private final String speciesName;
    
    /** @see #getGenomeVersion() */
    private final String genomeVersion;

    /** @see #getSpeciesFullNameWithoutSpace() */
    private final String speciesFullNameWithoutSpace;

    /**
     * @see #getGenomeSource()
     */
    private final Source genomeSource;
	
    /**@see #getDataTypesByDataSourcesForData() */
    private final Map<Source, Set<DataType>> dataTypesByDataSourcesForData;
    private final Map<DataType, Set<Source>> dataSourcesForDataByDataTypes;

    /**@see #getDataTypesByDataSourcesForAnnotation() */
    private final Map<Source, Set<DataType>> dataTypesByDataSourcesForAnnotation;
    private final Map<DataType, Set<Source>> dataSourcesForAnnotationByDataTypes;

    private final Integer parentTaxonId;

    private final Integer genomeSpeciesId;
    
    /**@see #getPreferredDisplayOrder() */
    private final Integer preferredDisplayOrder;
    
    /**
     * Constructor providing the {@code id} of this {@code Species}.
     * This {@code id} cannot be blank,
     * otherwise an {@code IllegalArgumentException} will be thrown.
     *
     * @param id    An {@code Integer} representing the ID of this object.
     * @throws IllegalArgumentException if {@code id} is blank.
     */
    public Species(Integer id) throws IllegalArgumentException {
        this(id, null, null, null, null, null, null, null, null, null, null, null);
    }
    /**
     * Constructor of {@code Species}.
     * 
     * @param id            An {@code Integer} representing the ID of this {@code Species}. 
     *                      Cannot be blank.
     * @param name          A {@code String} representing the (common) name of this {@code Species}.
     * @param description   A {@code String} description of this {@code Species}.
     * @param genus         A {@code String} representing the genus of this {@code Species} 
     *                      (e.g., "Homo" for human).
     * @param speciesName   A {@code String} representing the species name of this 
     *                      {@code Species} (e.g., "sapiens" for human).
     * @param genomeVersion A {@code String} representing the genome version used for 
     *                      this {@code Species}.
     * @param parentTaxonId An {@code Integer} representing the ID of the parent taxon of this species.
     * @param dataTypesByDataSourcesForData         A {@code Map} where keys are {@code Source}s 
     *                                              corresponding to data sources, the associated values 
     *                                              being a {@code Set} of {@code DataType}s corresponding
     *                                              to data types of raw data of this species.
     * @param dataTypesByDataSourcesForAnnotation   A {@code Map} where keys are {@code Source}s
     *                                              corresponding to data sources, the associated values 
     *                                              being a {@code Set} of {@code DataType}s corresponding
     *                                              to data types of annotation data of this data source.
     * @param preferredDisplayOrder                 An {@code Integer} allowing to sort {@code Species}
     *                                              in preferred display order.
     */
    public Species(Integer id, String name, String description, String genus, String speciesName,
            String genomeVersion, Source genomeSource, Integer genomeSpeciesId, Integer parentTaxonId,
            Map<Source, Set<DataType>> dataTypesByDataSourcesForData, 
            Map<Source, Set<DataType>> dataTypesByDataSourcesForAnnotation, Integer preferredDisplayOrder)
                throws IllegalArgumentException {
        super(id, name, description);
        this.genus = genus;
        this.speciesName = speciesName;
        this.speciesFullNameWithoutSpace = (new StringBuilder()).append(this.genus)
                .append("_").append(this.speciesName).toString().replace(" ", "_");
        this.genomeVersion = genomeVersion;
        this.genomeSource = genomeSource;
        //In the data source, '0' means that we used the correct genome
        if (genomeSpeciesId != null && genomeSpeciesId == 0) {
            this.genomeSpeciesId = id;
        } else {
            this.genomeSpeciesId = genomeSpeciesId;
        }
        this.parentTaxonId = parentTaxonId;
        this.dataTypesByDataSourcesForData = Collections.unmodifiableMap(
                dataTypesByDataSourcesForData == null? new HashMap<>():
                    new HashMap<>(dataTypesByDataSourcesForData));
        this.dataTypesByDataSourcesForAnnotation = Collections.unmodifiableMap(
                dataTypesByDataSourcesForAnnotation == null? new HashMap<>():
                    new HashMap<>(dataTypesByDataSourcesForAnnotation));
        //We also inverse key/value in the Maps.
        this.dataSourcesForDataByDataTypes = Collections.unmodifiableMap(
                this.dataTypesByDataSourcesForData.entrySet().stream()
                //transform the Entry<Source, Set<DataType>> into several Entry<DataType, Source>
                .flatMap(e -> e.getValue().stream().map(t -> new AbstractMap.SimpleEntry<>(t, e.getKey())))
                //collect the Entry<DataType, Source> into a Map<DataType, Set<Source>>
                .collect(Collectors.toMap(e -> e.getKey(), e -> new HashSet<>(Arrays.asList(e.getValue())), 
                        (s1, s2) -> {s1.addAll(s2); return s1;})));
        this.dataSourcesForAnnotationByDataTypes = Collections.unmodifiableMap(
                this.dataTypesByDataSourcesForAnnotation.entrySet().stream()
                //transform the Entry<Source, Set<DataType>> into several Entry<DataType, Source>
                .flatMap(e -> e.getValue().stream().map(t -> new AbstractMap.SimpleEntry<>(t, e.getKey())))
                //collect the Entry<DataType, Source> into a Map<DataType, Set<Source>>
                .collect(Collectors.toMap(e -> e.getKey(), e -> new HashSet<>(Arrays.asList(e.getValue())), 
                        (s1, s2) -> {s1.addAll(s2); return s1;})));
        this.preferredDisplayOrder = preferredDisplayOrder;
    }

    /**
     * @return A {@code String} representing the genus of the species (e.g., "Homo" for human).
     */
    public String getGenus() {
    	return this.genus;
    }

    /**
     * @return  A {@code String} representing the species name 
     *          of this {@code Species} (e.g., "sapiens" for human).
     */
    public String getSpeciesName() {
    	return this.speciesName;
    }

    /**
     * @return  A {@code String} representing the concatenation of genus
     *          and species name of this {@code Species}. The concatenation is
     *          done using an underscore and spaces in the resulting {@code String} are
     *          replaced by underscore too (e.g., "Homo_sapiens" for human,
     *          Canis_lupus_familiaris for dog).
     */
    public String getSpeciesFullNameWithoutSpace() {
        return this.speciesFullNameWithoutSpace;
    }
    
    /**
     * @return A {@code String} representing the genome version used for this {@code Species}
     */
    public String getGenomeVersion() {
        return this.genomeVersion;
    }
    /**
     * @return  The {@code Source} for the genome of this species.
     */
    public Source getGenomeSource() {
        return genomeSource;
    }
    /**
     * @return  The {@code Integer} that is the ID of the species of the genome used.
     *          In Bgee, when the genome of a species is not available, we sometimes use
     *          the genome of a closely related species.
     */
    public Integer getGenomeSpeciesId() {
        return genomeSpeciesId;
    }
    
    /**
     * @return  A {@code String} representing the species common name 
     *          (e.g., "human" for Homo sapiens).
     */
    @Override
    //method overridden to provide a more accurate javadoc
    public String getName() {
        return super.getName();
    }
    
    /**
     * @return  A {@code String} that is the scientific name of this {@code Species}, 
     *          for instance, "Homo sapiens" for human. 
     */
    public String getScientificName() {
        return this.getGenus() + " " + this.getSpeciesName();
    }

    /**
     * @return  A {@code String} containing a short representation of the name 
     *          (e.g., "H. sapiens" for Homo sapiens).
     */
    public String getShortName() {
    	if (genus == null || speciesName == null) return "";
    	return genus.toUpperCase().charAt(0) +". "+speciesName;
    }
    
    /**
     * @return An {@code Integer} representing the ID of the parent Taxon of this species
     */
    public Integer getParentTaxonId() {
        return this.parentTaxonId;
    }

	
    /**
     * @return  A {@code Map} where keys are {@code Source}s corresponding to data sources,
     *          the associated values being a {@code Set} of {@code DataType}s corresponding to 
     *          data types of raw data in this species. Is {@code null} if this information 
     *          was not requested.
     */
    //XXX: good candidate for using Java 8 Optional, null if not requested, 
    //empty if requested but no data types
	public Map<Source, Set<DataType>> getDataTypesByDataSourcesForData() {
        return dataTypesByDataSourcesForData;
    }

    /**
     * @return  A {@code Map} where keys are {@code Source}s corresponding to data sources,
     *          the associated values being a {@code Set} of {@code DataType}s corresponding to 
     *          data types of annotation data in this species. Is {@code null} if this information 
     *          was not requested.
     */
    //XXX: good candidate for using Java 8 Optional, null if not requested, 
    //empty if requested but no data types
    public Map<Source, Set<DataType>> getDataTypesByDataSourcesForAnnotation() {
        return dataTypesByDataSourcesForAnnotation;
    }

    public Map<DataType, Set<Source>> getDataSourcesForDataByDataTypes() {
        return dataSourcesForDataByDataTypes;
    }
    public Map<DataType, Set<Source>> getDataSourcesForAnnotationByDataTypes() {
        return dataSourcesForAnnotationByDataTypes;
    }

    /**
     * @return An {@code Integer} allowing to sort {@code Species in preferred display order.
     */
    public Integer getPreferredDisplayOrder() {
        return preferredDisplayOrder;
    }

    //we based hashCode/equals of NamedEntity on methods from parent class Entity:
    //only the ID matter for NamedEntity, as for Entity.

	@Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Species [").append(super.toString()).append(", genus=").append(genus)
                .append(", speciesName=").append(speciesName)
                .append(", speciesFullNameWithoutSpace=").append(speciesFullNameWithoutSpace)
                .append(", genomeVersion=").append(genomeVersion)
                .append(", genomeSource=").append(genomeSource)
                .append(", genomeSpeciesId=").append(genomeSpeciesId)
                .append(", parentTaxonId=").append(parentTaxonId)
                .append(", dataTypesByDataSourcesForData=").append(dataTypesByDataSourcesForData)
                .append(", dataTypesByDataSourcesForAnnotation=").append(dataTypesByDataSourcesForAnnotation)
                .append(", preferredDisplayOrder=").append(preferredDisplayOrder).append("]");
        return builder.toString();
    }
}
