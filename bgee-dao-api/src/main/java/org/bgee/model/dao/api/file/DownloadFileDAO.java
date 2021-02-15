package org.bgee.model.dao.api.file;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.NamedEntityTO;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;

/**
 * The DAO interface for DownloadFile objects.
 * 
 * @author Philippe Moret
 * @author Valentine Rech de Laval
 * @version Bgee 13 Sept. 2015
 * @since Bgee 13
 */
public interface DownloadFileDAO extends DAO<DownloadFileDAO.Attribute> {

    /**
     * The available attributes for @{code DownloadFileTO}
     * <ul>
     *     <li>{@code ID} corresponds to {@link DownloadFileTO#getId()}</li>
     *     <li>{@code NAME} corresponds to {@link DownloadFileTO#getName()}}</li>
     *     <li>{@code DESCRIPTION} corresponds to {@link DownloadFileTO#getDescription()}</li>
     *     <li>{@code PATH} corresponds to {@link DownloadFileTO#getPath()}}</li>
     *     <li>{@code FILE_SIZE} corresponds to {@link DownloadFileTO#getSize()}}</li>
     *     <li>{@code CATEGORY} corresponds to {@link DownloadFileTO#getCategory()}</li>
     *     <li>{@code SPECIES_DATA_GROUP_ID} corresponds to {@link DownloadFileTO#getSpeciesDataGroupId()}}</li>
     * </ul>
     */
    //TODO: javadoc for CONDITION_PARAMETERS
    public enum Attribute implements DAO.Attribute {
        ID, NAME, DESCRIPTION, PATH, FILE_SIZE, CATEGORY, SPECIES_DATA_GROUP_ID, CONDITION_PARAMETERS
    }

    /**
     * Get all download files.
     * <p>
     * The download files are retrieved and returned as a {@code DownloadFileTOResultSet}.
     * It is the responsibility of the caller to close this {@code DAOResultSet} once results
     * are retrieved.
     *
     * @return  A {@code DownloadFileTOResultSet} containing all download files from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public DownloadFileTOResultSet getAllDownloadFiles() throws DAOException;

    /**
     * Insert the provided download files into the Bgee database, represented as
     * a {@code Collection} of {@code DownloadFileTO}s.
     *
     * @param files                     A {@code Collection} of {@code DownloadFileTO}s to be
     *                                  inserted into the database.
     * @throws IllegalArgumentException If {@code files} is empty or null.
     * @throws DAOException             If an error occurred while trying to insert {@code files}.
     */
    public int insertDownloadFiles(Collection<DownloadFileTO> files)
            throws DAOException, IllegalArgumentException;

    /**
     * The {@code DAOResultSet} specific to {@code DownloadFileTO}.
     */
    public interface DownloadFileTOResultSet extends DAOResultSet<DownloadFileTO> {
    }

    /**
     * {@code NamedEntityTO} representing a download file in the Bgee database.
     */
    public final class DownloadFileTO extends NamedEntityTO<Integer> {

        private static final long serialVersionUID = 19171223459721L;

        /**
         * The class' logger.
         */
        private final static Logger log = LogManager.getLogger(DownloadFileTO.class.getName());

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
         * @author Philippe Moret
         * @version Bgee 13
         * @since Bgee 13
         *
         */
        public enum CategoryEnum implements TransferObject.EnumDAOField {
            EXPR_CALLS_SIMPLE("expr_simple"),
            EXPR_CALLS_COMPLETE("expr_complete"),
            DIFF_EXPR_ANAT_SIMPLE("diff_expr_anatomy_simple"),
            DIFF_EXPR_ANAT_COMPLETE("diff_expr_anatomy_complete"),
            DIFF_EXPR_DEV_COMPLETE("diff_expr_dev_complete"),
            DIFF_EXPR_DEV_SIMPLE("diff_expr_dev_simple"),
            ORTHOLOG("ortholog"),
            AFFY_ANNOT("affy_annot"),
            AFFY_DATA("affy_data"),
            RNASEQ_ANNOT("rnaseq_annot"),
            RNASEQ_DATA("rnaseq_data");

            /**
             * Constructor
             * @param stringRepresentation the {@code String} representation of the enum.
             */
            CategoryEnum(String stringRepresentation) {
                this.stringRepresentation = stringRepresentation;
            }

            /**
             * The {@code String} representation of the enum.
             */
            private String stringRepresentation;

            @Override
            public String getStringRepresentation() {
                return stringRepresentation;
            }

            /**
             * Return the mapped {@link DownloadFileDAO.DownloadFileTO.CategoryEnum} from a string representation.
             * @param stringRepresentation A string representation
             * @return The corresponding {@link DownloadFileDAO.DownloadFileTO.CategoryEnum}
             * @see org.bgee.model.dao.api.TransferObject.EnumDAOField#convert(Class, String)
             */
            public static CategoryEnum convertToCategoryEnum(String stringRepresentation){
                log.entry(stringRepresentation);
                return log.traceExit(EntityTO.convert(CategoryEnum.class, stringRepresentation));
            }

            @Override
            public String toString() {
                return getStringRepresentation();
            }
        }

        /**
         * A {@code String} that is the path of the download file.
         */
        private final String path;

        /**
         * A {@code Long} that is the size of the file (in bytes).
         */
        private final Long size;

        /**
         * A {@code CategoryEnum} that is the category of the file.
         */
        private final CategoryEnum category;

        /**
         * An {@code Integer} that is the ID of this file's species data group,
         * see {@link SpeciesDataGroupDAO}
         */
        private final Integer speciesDataGroupId;

        /**
         * A {@code Set} of {@code ConditionParameter}s that are the condition parameters
         * used to generate this file.
         */
        private final Set<ConditionDAO.Attribute> conditionParams;

        /**
         * The constructor providing all fields
         *
         * @param id                    An {@code Integer} that is the ID of this file.
         * @param name                  A {@code String} that is the name of this file.
         * @param description           A {@code String} that is the description of this file.
         * @param path                  A {@code String} that is the path of this file.
         * @param size                  A {@code Long} that is the size of this file.
         * @param category              A {@code CategoryEnum} that is the category of this file.
         * @param speciesDataGroupId    An {@code Integer} that is the ID of this file's species data group.
         * @param conditionParams       A {@code Collection} of {@code ConditionDAO.Attribute}s that are
         *                              the condition parameters used to generate this file.
         */
        public DownloadFileTO(Integer id, String name, String description, String path, Long size,
                              CategoryEnum category, Integer speciesDataGroupId,
                              Collection<ConditionDAO.Attribute> conditionParams){
            super(id, name, description);
            this.category = category;
            this.size = size;
            this.path = path;
            this.speciesDataGroupId = speciesDataGroupId;
            this.conditionParams = Collections.unmodifiableSet(
                    conditionParams == null? new HashSet<>(): new HashSet<>(conditionParams));
        }

        /**
         * Gets the path of the download file, relative to download files root directory.
         *
         * @return  The {@code String} that is the path of the download file.
         */
        public String getPath() {
            return path;
        }

        /**
         * Gets the size of the file (in bytes).
         *
         * @return  The {@code Long} that is the size of the file (in bytes).
         */
        public Long getSize() {
            return size;
        }

        /**
         * Get the category of the file
         *
         * @return The {@code CategoryEnum} that is the category of the file.
         */
        public CategoryEnum getCategory() {
            return category;
        }

        /**
         * Get the species data group ID.
         *
         * @return  The {@code Integer} that is the species data group ID.
         */
        public Integer getSpeciesDataGroupId() {
            return speciesDataGroupId;
        }

        /**
         * Get the condition parameters used to generate this file.
         *
         * @return  The {@code Collection} of {@code ConditionDAO.Attribute}s that are
         *          the condition parameters used to generate this file.
         */
        public Set<ConditionDAO.Attribute> getConditionParameters() {
            return conditionParams;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("DownloadFileTO [getId()=").append(getId())
                    .append(", getName()=").append(getName())
                    .append(", getDescription()=").append(getDescription())
                    .append(", getPath()=").append(getPath())
                    .append(", getSize()=").append(getSize())
                    .append(", getCategory()=").append(getCategory())
                    .append(", getSpeciesDataGroupId()=").append(getSpeciesDataGroupId())
                    .append(", getConditionParameters()=").append(getConditionParameters())
                    .append("]");
            return builder.toString();
        }
    }
}
