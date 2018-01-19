package org.bgee.model.file;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bgee.model.BgeeEnum;
import org.bgee.model.BgeeEnum.BgeeEnumField;

/**
 * A file (available for download), providing information such as size, category.
 *
 * @author  Philippe Moret
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Apr. 2017
 * @since   Bgee 13
 */
//TODO: sanity checks
public class DownloadFile {

	/**
	 * This enum contains all the different categories of files:
	 * <ul>
     *   <li>{@code EXPR_CALLS_SIMPLE} a simple expression calls file</li>
     *   <li>{@code EXPR_CALLS_COMPLETE} a complete expression calls file</li>
     *   <li>{@code DIFF_EXPR_ANAT_SIMPLE} a simple differential expression across anatomy file</li>
     *   <li>{@code DIFF_EXPR_ANAT_COMPLETE} a complete differential expression across anatomy file</li>
     *   <li>{@code DIFF_EXPR_DEV_COMPLETE} a complete differential expression across developmental stages file</li>
     *   <li>{@code DIFF_EXPR_DEV_SIMPLE}a simple differential expression across developmental stages file</li>
     *   <li>{@code ORTHOLOG} corresponds to an orthologies file</li>
     *   <li>{@code AFFY_ANNOT} corresponds to an Affymetrix annoations file</li>
     *   <li>{@code AFFY_DATA} corresponds to an Affymetrix signal intensities file</li>
     *   <li>{@code RNASEQ_ANNOT} corresponds to RNA-Seq annotations file</li>
     *   <li>{@code RNASEQ_DATA} corresponds toRNA-Seq data file</li>
	 * </ul>
	 *
	 * @author  Philippe Moret
	 * @author  Valentine Rech de Laval
	 * @version Bgee 14, Apr. 2017
	 * @since   Bgee 13
	 */
    public enum CategoryEnum implements BgeeEnumField {
        EXPR_CALLS_SIMPLE("expr_simple", false),
        EXPR_CALLS_COMPLETE("expr_advanced", false),
        DIFF_EXPR_ANAT_SIMPLE("diff_expr_anatomy_simple", true),
        //TODO: harmonize use of advanced/complete. Use "advanced" in the text name.
        DIFF_EXPR_ANAT_COMPLETE("diff_expr_anatomy_complete", true),
        DIFF_EXPR_DEV_COMPLETE("diff_expr_dev_complete", true),
        DIFF_EXPR_DEV_SIMPLE("diff_expr_dev_simple", true),
        ORTHOLOG("ortholog", false),
        AFFY_ANNOT("affy_annot", false),
        AFFY_DATA("affy_data", false),
        RNASEQ_ANNOT("rnaseq_annot", false),
        RNASEQ_DATA("rnaseq_data", false);

        /**
         * A {@code String} that is the string representation.
         */
        private final String stringRepresentation;

        /**
         * A {@code boolean} defining whether the file is a differential expression file.
         */
        private final boolean isDiffExpr;

        /**
         * Constructor with 2-params.
         *
         * @param stringRepresentation  A {@code String} that is the string representation.
         * @param isDiffExpr            A {@code boolean} defining whether the file is
         *                              a differential expression file.
         */
        CategoryEnum(String stringRepresentation, boolean isDiffExpr) {
            this.stringRepresentation = stringRepresentation;
            this.isDiffExpr = isDiffExpr;
        }

        @Override
        public String getStringRepresentation() {
            return this.stringRepresentation;
        }

        /**
         * Convert the {@code String} representation of a category (for instance,
         * retrieved from request) into a {@code CategoryEnum}.
         * Operation performed by calling {@link BgeeEnum#convert(Class, String)} with
         * {@code CategoryEnum} as the {@code Class} argument, and {@code representation}
         * as the {@code String} argument.
         *
         * @param representation            A {@code String} representing a data quality.
         * @return                          A {@code CategoryEnum} corresponding
         *                                  to {@code representation}.
         * @throws IllegalArgumentException If {@code representation} does not correspond
         *                                  to any {@code CategoryEnum}.
         * @see #convert(Class, String)
         */
        public static final CategoryEnum convertToCategoryEnum(String representation) {
            return BgeeEnum.convert(CategoryEnum.class, representation);
        }

        /**
         * @return {@code true} if the file category is a differential expression.
         */
        public boolean isDiffExpr() {
            return this.isDiffExpr;
        }

        @Override
        public String toString() {
            return this.getStringRepresentation();
        }
    }

    /**
     * This enum contains all the different condition parameters of files:
     * <ul>
     *   <li>{@code ANAT_ENTITY} corresponds to the anatomical entity parameter</li>
     *   <li>{@code DEV_STAGE} corresponds to the developmental stage parameter</li>
     * </ul>
     *
     * @author  Valentine Rech de Laval
     * @version Bgee 14, Apr. 2017
     * @since   Bgee 14, Apr. 2017
     */
    // TODO: should we use CallService.Attribute?
    // TODO: Yes
    public enum ConditionParameter implements BgeeEnumField {
        ANAT_ENTITY("anatomicalEntity"), DEV_STAGE("developmentalStage");

        /** The string representation */
        private final String stringRepresentation;

        /**
         * Constructor with 1-param
         *
         * @param stringRepresentation A {@code String} corresponding to this {@code ConditionParameter}.
         */
        ConditionParameter(String stringRepresentation) {
            this.stringRepresentation = stringRepresentation;
        }

        @Override
        public String getStringRepresentation() {
            return this.stringRepresentation;
        }
        
        /**
         * Convert the {@code String} representation of a condition parameter (for instance,
         * retrieved from request) into a {@code ConditionParameter}.
         * Operation performed by calling {@link BgeeEnum#convert(Class, String)} with
         * {@code ConditionParameter} as the {@code Class} argument, and {@code representation}
         * as the {@code String} argument.
         *
         * @param representation            A {@code String} representing a data quality.
         * @return                          A {@code ConditionParameter} corresponding
         *                                  to {@code representation}.
         * @throws IllegalArgumentException If {@code representation} does not correspond
         *                                  to any {@code ConditionParameter}.
         * @see #convert(Class, String)
         */
        public static final ConditionParameter convertToConditionParameter(String representation) {
            return BgeeEnum.convert(ConditionParameter.class, representation);
        }

        @Override
        public String toString() {
            return this.getStringRepresentation();
        }
    }

    /**
     * See {@link #getPath()}.
     */
    private final String path;

    /**
     * See {@link #getName()}.
     */
    private final String name;

    /**
     * See {@link #getCategory()}.
     */
    private final CategoryEnum category;

    /**
     * See {@link #getSpeciesDataGroupId()}.
     */
    private final Integer speciesDataGroupId;

    /**
     * See {@link #getSize()}.
     */
    private final long size;

    /**
     * See {@link #getConditionParameters()}.
     */
    private final Set<ConditionParameter> conditionParameters;

    /**
     * The constructor provides all values except condition parameters to create a {@code DownloadFile}.
     * <p>
     * No argument can be null or blank.
     *
     * @param path                 A {@code String} representing the path of the containing the file.
     * @param name                 A {@code String} containing the file name. Might be {@code /} 
     *                             by convention if the file represents a directory.
     * @param category             A {@code CategoryEnum} that is the category of the file.
     * @param size                 A {@code Long} representing the file size in bytes.
     * @param speciesDataGroupId   A {@code Integer} representing the species data group that owns this file.
     * @throws IllegalArgumentException If any of the argument is {@code null}.
     */
    public DownloadFile(String path, String name, CategoryEnum category, Long size, Integer speciesDataGroupId){
        this(path, name, category, size, speciesDataGroupId, null);
    }

    /**
     * The constructor provides all values to create a {@code DownloadFile}.
     * <p>
     * Only {@code conditionParameters} can be null or blank.
     *
     * @param path                 A {@code String} representing the path of the containing the file.
     * @param name                 A {@code String} containing the file name. Might be {@code /}
     *                             by convention if the file represents a directory.
     * @param category             A {@code CategoryEnum} that is the category of the file.
     * @param size                 A {@code Long} representing the file size in bytes.
     * @param speciesDataGroupId   A {@code Integer} representing the species data group that owns this file.
     * @param conditionParameters  A {@code Set} of {@code ConditionParameter} thats are the
     *                             condition parameters used to generate this file.
     * @throws IllegalArgumentException If any of the argument is {@code null}.
     */
    public DownloadFile(String path, String name, CategoryEnum category, Long size,
            Integer speciesDataGroupId, Collection<ConditionParameter> conditionParameters) {
        if (StringUtils.isBlank(path) || StringUtils.isBlank(name) || category == null || 
                speciesDataGroupId == null) {
            throw new IllegalArgumentException("No argument can be null or blank.");
        }
        this.path = path;
        this.name = name;
        this.size = size;
        this.category = category;
        this.speciesDataGroupId = speciesDataGroupId;
        this.conditionParameters = Collections.unmodifiableSet(conditionParameters == null?
                new HashSet<>(): new HashSet<>(conditionParameters));
    }

    /**
     * Get the path, relative to download files root directory.
     *
     * @return A {@code String} containing the path where the actual file is found.
     */
    public String getPath() {
        return path;
    }

    /**
     * Get the file name.
     *
     * @return A {@code String} containing the file name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the file category.
     *
     * @return A {@code CategoryEnum} representing the file's category.
     */
    public CategoryEnum getCategory() {
        return category;
    }

    /**
     * Gets the species data group id.
     *
     * @return An {@code Integer} representation of the species data group id.
     */
    public Integer getSpeciesDataGroupId() {
        return speciesDataGroupId;
    }

    
    /**
     * Gets the condition parameters.
     *
     * @return  A {@code Set} of {@code ConditionParameter} that are the
     *          condition parameters used to generate this file.
     */
    public Set<ConditionParameter> getConditionParameters() {
        return conditionParameters;
    }

    /**
     * Gets the size.
     *
     * @return A {@code Long} representing the size of the file in bytes.
     */
    public Long getSize() {
        return size;
    }

    /**
     * Define whether the file is a differential expression file.
     *
     * @return {@code true} if the file is a differential expression file.
     */
    public boolean isDiffExpr(){
        return category.isDiffExpr();
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((category == null) ? 0 : category.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + (int) (size ^ (size >>> 32));
        result = prime * result + ((speciesDataGroupId == null) ? 0 : speciesDataGroupId.hashCode());
        result = prime * result + ((conditionParameters == null) ? 0 : conditionParameters.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "DownloadFile [path=" + path + ", name=" + name + ", category=" + category + ", speciesDataGroupId="
		        + speciesDataGroupId + ", size=" + size + ", conditionParameters=" + conditionParameters + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DownloadFile other = (DownloadFile) obj;
		if (category != other.category) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (path == null) {
			if (other.path != null) {
				return false;
			}
		} else if (!path.equals(other.path)) {
			return false;
		}
		if (size != other.size) {
			return false;
		}
		if (speciesDataGroupId == null) {
			if (other.speciesDataGroupId != null) {
				return false;
			}
		} else if (!speciesDataGroupId.equals(other.speciesDataGroupId)) {
			return false;
		}
		if (conditionParameters == null) {
		    if (other.conditionParameters != null) {
		        return false;
		    }
		} else if (!conditionParameters.equals(other.conditionParameters)) {
		    return false;
		}
		return true;
	}
}
