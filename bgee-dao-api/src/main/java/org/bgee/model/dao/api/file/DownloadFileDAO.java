package org.bgee.model.dao.api.file;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;

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
    enum Attribute implements DAO.Attribute {
        ID,
        NAME,
        DESCRIPTION,
        PATH,
        FILE_SIZE,
        CATEGORY,
        SPECIES_DATA_GROUP_ID
    }

    /**
     * The {@code DAOResultSet} specific to {@code DownloadFileTO}
     */
    interface DownloadFileTOResultSet extends DAOResultSet<DownloadFileTO> {

    }

    /**
     * The {@code TransferObject} representing a Download File.
     */
    //TODO: standardize javadoc
    final class DownloadFileTO extends EntityTO {

        private static final long serialVersionUID = 19171223459721L;

        /**
         * The class' logger.
         */
        private final static Logger log = LogManager.getLogger(DownloadFileTO.class.getName());

        /**
         * The path of the file
         */
        private final String path;

        /**
         * The size of the file (in bytes)
         */
        private final Long size;

        /**
         * The category of the file, as a {@link CategoryEnum}.
         */
        private final CategoryEnum category;

        /**
         * The id of this file's species data group,
         * see {@link SpeciesDataGroupDAO}
         */
        private final String speciesDataGroupId;

        /**
         * The constructor providing all fields
         * @param id The id of the download file
         * @param name The name of the download file
         * @param description The description of the download file
         * @param path The path of the download file
         * @param size The size of the download file
         * @param category The category of the download file
         * @param speciesDataGroupId The id of this file's species data group
         */
        public DownloadFileTO(String id, String name, String description, String path, Long size,
                              CategoryEnum category, String speciesDataGroupId){
            super(id, name, description);
            this.category = category;
            this.size = size;
            this.path = path;
            this.speciesDataGroupId = speciesDataGroupId;
        }

        /**
         * Gets the path of the download file, relative to download files root directory. 
         * @return The path of the download file
         */
        public String getPath() {
            return path;
        }

        /**
         * Gets the size of the file (in bytes).
         * @return The size of the file (in bytes).
         */
        public Long getSize() {
            return size;
        }

        /**
         * Get the category of the file
         * @return The category of the file
         */
        public CategoryEnum getCategory() {
            return category;
        }

        /**
         * Get the species data group id
         * @return the species data group id
         */
        public String getSpeciesDataGroupId() {
            return speciesDataGroupId;
        }

        @Override
        public String toString() {
            return "DownloadFile[id="+getId()
                    +", name="+getName()
                    +", description=" +getDescription()
                    +", category="+category+""
                    +", path="+path
                    +", size="+size
                    +", speciesDataGroupId="+speciesDataGroupId;
        }

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
                log.entry();
                return log.exit(EntityTO.convert(CategoryEnum.class, stringRepresentation));
            }

            @Override
            public String toString() {
                return getStringRepresentation();
            }
        }
    }

    /**
     * Get all download files
     * @return A {@link org.bgee.model.dao.api.file.DownloadFileDAO.DownloadFileTOResultSet} containing the results
     * as {@code DownloadFileTO}s
     * @throws DAOException
     */
    DownloadFileTOResultSet getAllDownloadFiles() throws DAOException;

    /**
     * Insert the provided download files into the Bgee database, represented as
     * a {@code Collection} of {@code DownloadFileTO}s.
     * 
     * @param files                     A {@code Collection} of {@code DownloadFileTO}s to be
     *                                  inserted into the database.
     * @throws IllegalArgumentException If {@code files} is empty or null.
     * @throws DAOException             If an error occurred while trying
     *                                  to insert {@code files}.
     */
    public int insertDownloadFiles(Collection<DownloadFileTO> files)
            throws DAOException, IllegalArgumentException;
}
