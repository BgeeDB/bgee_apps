package org.bgee.model.dao.api.file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;

/**
 * The DAO interface for DownloadFile objects.
 * @author Philippe Moret
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
        private final long size;

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
        public DownloadFileTO(String id, String name, String description, String path, String size,
                              CategoryEnum category, String speciesDataGroupId){
            super(id, name, description);
            this.category = category;
            this.size = Long.parseLong(size);
            this.path = path;
            this.speciesDataGroupId = speciesDataGroupId;
        }

        /**
         * Gets the path of the download file
         * @return The path of the download file
         */
        public String getPath() {
            return path;
        }

        /**
         * Gets the size of the file (in bytes).
         * @return The size of the file (in bytes).
         */
        public long getSize() {
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
         * Represents the category of a downloadFile:
         * <ul>
         *     <li>{@code EXPR_CALLS} correponds to expression calls file (single species)</li>
         *     <li>{@code DIFF_EXPR_CALLS_ANAT} corresponds to diff expression calls file across anatomy</li>
         *     <li>{@code DIFF_EXPR_CALLS_STAGES} corresponds to diff expression calls across stages</li>
         *     <li>{@code ORTHOLOGS} corresponds to orthologies file</li>
         * </ul>
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
            AFFY_ROOT("affy_root"),
            RNASEQ_ANNOT("rnaseq_annot"),
            RNASEQ_DATA("rnaseq_data"),
            RNASEQ_ROOT("rnaseq_root");

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

}
