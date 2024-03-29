package org.bgee.model.file;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bgee.model.BgeeEnum;
import org.bgee.model.BgeeEnum.BgeeEnumField;
import org.bgee.model.expressiondata.call.CallService;

/**
 * A file (available for download), providing information such as size, category.
 *
 * @author  Philippe Moret
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 15, Oct. 2021
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
        EXPR_CALLS_SIMPLE("expr_simple", true, false),
        EXPR_CALLS_COMPLETE("expr_advanced", true, false),
        DIFF_EXPR_ANAT_SIMPLE("diff_expr_anatomy_simple", true, false),
        //TODO: harmonize use of advanced/complete. Use "advanced" in the text name.
        DIFF_EXPR_ANAT_COMPLETE("diff_expr_anatomy_complete", true, false),
        DIFF_EXPR_DEV_COMPLETE("diff_expr_dev_complete", true, false),
        DIFF_EXPR_DEV_SIMPLE("diff_expr_dev_simple", true, false),
        ORTHOLOG("ortholog", false, true),
        AFFY_ANNOT("affy_annot", false, true),
        AFFY_DATA("affy_data", false, true),
        RNASEQ_ANNOT("rnaseq_annot", false, true),
        RNASEQ_DATA("rnaseq_data", false, true),
        SC_RNA_SEQ_ANNOT("full_length_annot", false, true),
        SC_RNA_SEQ_DATA("full_length_data", false, true);

        /**
         * A {@code String} that is the string representation.
         */
        private final String stringRepresentation;

        private final boolean relatedToExprCallFile;
        private final boolean relatedToProcessedExprValueFile;

        CategoryEnum(String stringRepresentation, boolean relatedToExprCallFile,
                boolean relatedToProcessedExprValueFile) {
            this.stringRepresentation = stringRepresentation;
            this.relatedToExprCallFile = relatedToExprCallFile;
            this.relatedToProcessedExprValueFile = relatedToProcessedExprValueFile;
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

        public boolean isRelatedToExprCallFile() {
            return this.relatedToExprCallFile;
        }
        public boolean isRelatedToProcessedExprValueFile() {
            return this.relatedToProcessedExprValueFile;
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
    private final Set<CallService.Attribute> conditionParameters;

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
     *                             condition parameters used to generate this file. Can be {@code null}
     *                             if not relevant to this {@code DownloadFile}.
     * @throws IllegalArgumentException If any of the argument is {@code null}.
     */
    public DownloadFile(String path, String name, CategoryEnum category, Long size,
            Integer speciesDataGroupId, Collection<CallService.Attribute> conditionParameters) {
        if (StringUtils.isBlank(path) || StringUtils.isBlank(name) || category == null || 
                speciesDataGroupId == null) {
            throw new IllegalArgumentException("No argument can be null or blank.");
        }
        if (conditionParameters != null && conditionParameters.stream().anyMatch(c -> !c.isConditionParameter())) {
            throw new IllegalArgumentException("Not a condition parameter");
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
     * @return  A {@code Set} of {@code CallService.Attribute} that are the
     *          condition parameters used to generate this file. Can be {@code null}
     *          if not relevant to this {@code DownloadFile}.
     */
    public Set<CallService.Attribute> getConditionParameters() {
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

    public boolean isRelatedToExprCallFile(){
        return category.isRelatedToExprCallFile();
    }
    public boolean isRelatedToProcessedExprValueFile(){
        return category.isRelatedToProcessedExprValueFile();
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

    @Override
    public String toString() {
        return "DownloadFile [path=" + path + ", name=" + name + ", category=" + category + ", speciesDataGroupId="
                + speciesDataGroupId + ", size=" + size + ", conditionParameters=" + conditionParameters + "]";
    }
}
