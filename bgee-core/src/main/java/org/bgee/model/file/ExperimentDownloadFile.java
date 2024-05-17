package org.bgee.model.file;

import java.util.Objects;

import org.bgee.model.BgeeEnum;
import org.bgee.model.BgeeEnum.BgeeEnumField;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.species.Species;

/**
 * {@code DownloadFile} providing data related to an {@code ExperimentWithDataDownload}.
 *
 * @author Frederic Bastian
 * @version Bgee 15.2, May 2024
 * @since Bgee 15.2, May 2024
 */
public class ExperimentDownloadFile extends DownloadFile {
    /**
     * <ul>
     * <li>{@code ANNOTATED_SAMPLES}: corresponds to data related to annotated samples, e.g.,
     * data in bulk RNA-Seq, or data in droplet-based scRNA-Seq grouped by cell population.
     * <li>{@code INDIVIDUAL_SAMPLES}: corresponds to data related to individual samples, e.g.,
     * each cell in a droplet-based scRNA-Seq experiment. Of note, even for full-length scRNA-Seq data,
     * even though the annotated and individual samples are the same
     * (one cell is equivalent to one annotated sample), we provide this information as both categories,
     * in both formats.
     * </ul>
     *
     * @author Frederic Bastian
     * @version Bgee 15.2, May 2024
     * @since Bgee 15.2, May 2024
     */
    public enum Category implements BgeeEnumField {
        ANNOTATED_SAMPLES("annotated_samples"), INDIVIDUAL_SAMPLES("individual_samples");

        /**
         * A {@code String} that is the string representation.
         */
        private final String stringRepresentation;

        Category(String stringRepresentation) {
            this.stringRepresentation = stringRepresentation;
        }

        @Override
        public String getStringRepresentation() {
            return this.stringRepresentation;
        }
        @Override
        public String toString() {
            return this.getStringRepresentation();
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
    }


    private final Category category;
    private final DataType dataType;
    private final boolean sampleMultiplexing;
    private final Species species;

    /**
     * @param path                  A {@code String} representing the path containing the file.
     * @param fileName              A {@code String} representing the file name. Might be {@code /}
     *                              by convention if the file represents a directory.
     * @param title                 A {@code String} representing a user friendly title for the download file.
     * @param size                  A {@code Long} representing the file size in bytes.
     * @param category              A {@code Category} that is the category of the file.
     * @param dataType              The {@code DataType} associated to this download file.
     * @param isDropletBased        A {@code boolean} that is true if the download file for {@code dataType}
     *                              is related to droplet-based data.
     * @param species               The {@code Species} this download file contains data for.
     */
    public ExperimentDownloadFile(String path, String fileName, String title, Long size,
            Category category, DataType dataType, boolean sampleMultiplexing, Species species) {
        super(path, fileName, title, size);
        if (category == null) {
            throw new IllegalArgumentException("Category must be provided");
        }
        if (dataType == null) {
            throw new IllegalArgumentException("DataType must be provided");
        }
        if (species == null) {
            throw new IllegalArgumentException("Species must be provided");
        }
        this.category = category;
        this.dataType = dataType;
        this.sampleMultiplexing = sampleMultiplexing;
        this.species = species;
    }

    public Category getCategory() {
        return category;
    }
    public DataType getDataType() {
        return dataType;
    }
    public boolean isSampleMultiplexing() {
        return sampleMultiplexing;
    }
    public Species getSpecies() {
        return species;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(category, dataType, sampleMultiplexing, species);
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
        ExperimentDownloadFile other = (ExperimentDownloadFile) obj;
        return category == other.category && dataType == other.dataType
                && sampleMultiplexing == other.sampleMultiplexing && Objects.equals(species, other.species);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ExperimentDownloadFile [")
               .append("category=").append(category)
               .append(", dataType=").append(dataType)
               .append(", sampleMultiplexing=").append(sampleMultiplexing)
               .append(", species=").append(species)
               .append(", getPath()=").append(getPath())
               .append(", getFileName()=").append(getFileName())
               .append(", getTitle()=").append(getTitle())
               .append(", getSize()=").append(getSize())
               .append("]");
        return builder.toString();
    }
}