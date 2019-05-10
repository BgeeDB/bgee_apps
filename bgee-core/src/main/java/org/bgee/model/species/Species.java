package org.bgee.model.species;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bgee.model.NamedEntity;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.source.Source;

/**
 * Class allowing to describe species used in Bgee.
 * 
 * @author  Frederic Bastian
 * @author  Philippe Moret
 * @author  Valentine Rech de Laval
 * @version Bgee 14, May 2019
 * @since   Bgee 13, Mar. 2013
 */
public class Species extends NamedEntity<Integer> {
    
	/** @see #getGenus() */
	private final String genus;
	
    /** @see #getSpeciesName() */
    private final String speciesName;
    
    /** @see #getGenomeVersion() */
    private final String genomeVersion;
    /**
     * @see #getGenomeSource()
     */
    private final Source genomeSource;
	
    /**@see #getDataTypesByDataSourcesForData() */
    private Map<Source, Set<DataType>> dataTypesByDataSourcesForData;

    /**@see #getDataTypesByDataSourcesForAnnotation() */
    private Map<Source, Set<DataType>> dataTypesByDataSourcesForAnnotation;

    private final Integer parentTaxonId;
    
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
        this(id, null, null, null, null, null, null, null, null, null, null);
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
            String genomeVersion, Source genomeSource, Integer parentTaxonId, Map<Source, Set<DataType>> dataTypesByDataSourcesForData, 
            Map<Source, Set<DataType>> dataTypesByDataSourcesForAnnotation, Integer preferredDisplayOrder)
                throws IllegalArgumentException {
        super(id, name, description);
        this.genus = genus;
        this.speciesName = speciesName;
        this.genomeVersion = genomeVersion;
        this.genomeSource = genomeSource;
        this.parentTaxonId = parentTaxonId;
        this.dataTypesByDataSourcesForData = dataTypesByDataSourcesForData == null ? 
                null: Collections.unmodifiableMap(new HashMap<>(dataTypesByDataSourcesForData));
        this.dataTypesByDataSourcesForAnnotation = dataTypesByDataSourcesForAnnotation == null ? 
                null: Collections.unmodifiableMap(new HashMap<>(dataTypesByDataSourcesForAnnotation));
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
                .append(", genomeVersion=").append(genomeVersion)
                .append(", dataTypesByDataSourcesForData=").append(dataTypesByDataSourcesForData)
                .append(", dataTypesByDataSourcesForAnnotation=").append(dataTypesByDataSourcesForAnnotation)
                .append(", parentTaxonId=").append(parentTaxonId)
                .append(", preferredDisplayOrder=").append(preferredDisplayOrder).append("]");
        return builder.toString();
    }
}
