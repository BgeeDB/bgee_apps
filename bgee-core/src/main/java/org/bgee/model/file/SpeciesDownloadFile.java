package org.bgee.model.file;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.bgee.model.BgeeEnum;
import org.bgee.model.BgeeEnum.BgeeEnumField;
import org.bgee.model.expressiondata.call.CallService;

/**
 * {@code DownloadFile} providing all data for a species for a given {@code Category} of information.
 *
 * @author Frederic Bastian
 * @version Bgee 15.2, May 2024
 * @since Bgee 15.2, May 2024
 */
public class SpeciesDownloadFile extends DownloadFile {

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
    public enum Category implements BgeeEnumField {
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

        Category(String stringRepresentation, boolean relatedToExprCallFile,
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
         * retrieved from request) into a {@code Category}.
         * Operation performed by calling {@link BgeeEnum#convert(Class, String)} with
         * {@code Category} as the {@code Class} argument, and {@code representation}
         * as the {@code String} argument.
         *
         * @param representation            A {@code String} representing the category.
         * @return                          A {@code Category} corresponding to {@code representation}.
         * @throws IllegalArgumentException If {@code representation} does not correspond
         *                                  to any {@code Category}.
         * @see #convert(Class, String)
         */
        public static final Category convertToCategory(String representation) {
            return BgeeEnum.convert(Category.class, representation);
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
     * See {@link #getCategory()}.
     */
    private final Category category;
    /**
     * See {@link #getSpeciesDataGroupId()}.
     */
    private final Integer speciesDataGroupId;
    /**
     * See {@link #getConditionParameters()}.
     */
    private final Set<CallService.Attribute> conditionParameters;


    /**
     * @param path                  A {@code String} representing the path containing the file.
     * @param fileName              A {@code String} representing the file name. Might be {@code /}
     *                              by convention if the file represents a directory.
     * @param title                 A {@code String} representing a user friendly title for the download file.
     * @param size                  A {@code Long} representing the file size in bytes.
     * @param category              A {@code Category} that is the category of the file.
     * @param speciesDataGroupId    A {@code Integer} representing the species data group that owns this file.
     * @param conditionParameters   A {@code Collection} of {@code CallService.Attribute}s that are condition parameters.
     * @throws IllegalArgumentException If {@code path} or {@code fileName} are {@code null} or empty,
     *                                  or if {@code size} is less than 0.
     */
    public SpeciesDownloadFile(String path, String fileName, String title, Long size,
            Category category, Integer speciesDataGroupId) {
         this(path, fileName, title, size, category, speciesDataGroupId, null);
     }
    /**
     * @param path                  A {@code String} representing the path containing the file.
     * @param fileName              A {@code String} representing the file name. Might be {@code /}
     *                              by convention if the file represents a directory.
     * @param title                 A {@code String} representing a user friendly title for the download file.
     * @param size                  A {@code Long} representing the file size in bytes.
     * @param category              A {@code Category} that is the category of the file.
     * @param speciesDataGroupId    A {@code Integer} representing the species data group that owns this file.
     * @param conditionParameters   A {@code Collection} of {@code CallService.Attribute}s that are condition parameters.
     *                              Can be {@code null} if the download file is not related to a selection
     *                              of condition parameters (e.g., annotations or expression values
     *                              for a specific data type.
     * @throws IllegalArgumentException If {@code path} or {@code fileName} are {@code null} or empty,
     *                                  or if {@code size} is less than 0, or if {@code conditionParameters}
     *                                  contains {@code CallService.Attribute}s that are not condition parameters.
     */
    public SpeciesDownloadFile(String path, String fileName, String title, Long size,
            Category category, Integer speciesDataGroupId,
            Collection<CallService.Attribute> conditionParameters) {
         super(path, fileName, title, size);
         if (conditionParameters != null && conditionParameters.stream().anyMatch(c -> !c.isConditionParameter())) {
             throw new IllegalArgumentException("Not a condition parameter");
         }
         this.category = category;
         this.speciesDataGroupId = speciesDataGroupId;
         this.conditionParameters = Collections.unmodifiableSet(conditionParameters == null?
                 new HashSet<>(): new HashSet<>(conditionParameters));
     }


    /**
     * Gets the file category.
     *
     * @return A {@code Category} representing the file's category.
     */
    public Category getCategory() {
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

    public boolean isRelatedToExprCallFile(){
        return category.isRelatedToExprCallFile();
    }
    public boolean isRelatedToProcessedExprValueFile(){
        return category.isRelatedToProcessedExprValueFile();
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(category, conditionParameters, speciesDataGroupId);
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        SpeciesDownloadFile other = (SpeciesDownloadFile) obj;
        return category == other.category && Objects.equals(conditionParameters, other.conditionParameters)
                && Objects.equals(speciesDataGroupId, other.speciesDataGroupId);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SpeciesDownloadFile [")
               .append("category=").append(category)
               .append(", speciesDataGroupId=").append(speciesDataGroupId)
               .append(", conditionParameters=").append(conditionParameters)
               .append(", getPath()=").append(getPath())
               .append(", getFileName()=").append(getFileName())
               .append(", getTitle()=").append(getTitle())
               .append(", getSize()=").append(getSize())
               .append("]");
        return builder.toString();
    }
}
