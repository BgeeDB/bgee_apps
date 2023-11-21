package org.bgee.model.dao.api.source;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.source.SourceToSpeciesDAO.SourceToSpeciesTO.InfoType;

/**
 * DAO defining queries using or retrieving {@link SourceToSpeciesTO}s. 
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 14 Mar. 2017
 * @since   Bgee 13, June 2016
 * @see     SourceToSpeciesTO
 */
public interface SourceToSpeciesDAO extends DAO<SourceToSpeciesDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code SourceToSpeciesTO}s 
     * obtained from this {@code SourceToSpeciesDAO}.
     * <ul>
     * <li>{@code DATASOURCE_ID}: corresponds to {@link SourceToSpeciesTO#getDataSourceId()}.
     * <li>{@code SPECIES_ID}: corresponds to {@link SourceToSpeciesTO#getSpeciesId()}.
     * <li>{@code DATA_TYPE}: corresponds to {@link SourceToSpeciesTO#getDataType()}.
     * <li>{@code INFO_TYPE}: corresponds to {@link SourceToSpeciesTO#getInfoType()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        DATASOURCE_ID, SPECIES_ID, DATA_TYPE, INFO_TYPE;
    }
    
    /**
     * Return all data sources to species used in Bgee from data source.
     * <p>
     * The sources are retrieved and returned as a {@code SourceToSpeciesTOResultSet}. It is the
     * responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @param attributes    A {@code Collection} of {@code SourceToSpeciesDAO.Attribute}s defining
     *                      the attributes to populate in the returned {@code SourceToSpeciesTO}s.
     *                      If {@code null} or empty, all attributes are populated. 
     * @return              A {@code SourceToSpeciesTOResultSet} containing sources to species
     *                      used in Bgee from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public SourceToSpeciesTOResultSet getAllSourceToSpecies(Collection<SourceToSpeciesDAO.Attribute> attributes)
            throws DAOException;

    /**
     * Retrieve data sources to species used in Bgee from data source.
     * 
     * @param dataSourceIds A {@code Collection} of {@code Integer}s that are the IDs of data sources
     *                      allowing to filter the data sources to species.
     *                      If {@code null} or empty, all data sources are used.
     * @param speciesIds    A {@code Collection} of {@code Integer}s that are the IDs of species
     *                      allowing to filter the data sources to species.
     *                      If {@code null} or empty, all species are used.
     * @param dataTypes     A {@code Collection} of {@code DataType}s that are the data types
     *                      allowing to filter the data sources to species.
     *                      If {@code null} or empty, all data types are used.
     * @param infoTypes     A {@code Collection} of {@code InfoType}s that are the information types
     *                      allowing to filter the data sources to species.
     *                      If {@code null} or empty, all information types are used.
     * @param attributes    A {@code Collection} of {@code SourceToSpeciesDAO.Attribute}s defining the
     *                      attributes to populate in the returned {@code SourceToSpeciesTO}s.
     *                      If {@code null} or empty, all attributes are populated. 
     * @return              A {@code SourceToSpeciesTOResultSet} containing sources to species
     *                      used in Bgee from data source, filtered by {@code dataSourceIds},
     *                      {@code speciesIds}, {@code dataTypes} and {@code infoTypes}.
     * @throws DAOException             If an error occurred when accessing the data source.
     * @throws IllegalStateException    If retrieved more than one source.
     */
    public SourceToSpeciesTOResultSet getSourceToSpecies(Collection<Integer> dataSourceIds,
            Collection<Integer> speciesIds, Collection<DAODataType> dataTypes, Collection<InfoType> infoTypes,
            Collection<SourceToSpeciesDAO.Attribute> attributes) throws DAOException;

    /**
     * {@code DAOResultSet} specifics to {@code SourceToSpeciesTO}s
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 13
     * @since   Bgee 13
     */
    public interface SourceToSpeciesTOResultSet extends DAOResultSet<SourceToSpeciesTO> {
    }

    /**
     * {@code TransferObject} representing a source to species of data in the Bgee data source.
     * <p>
     * For information on this {@code TransferObject} and its fields, 
     * see the corresponding class in the {@code bgee-core} module.
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 13, June 2016
     * @since   Bgee 13, June 2016
     */
    public final class SourceToSpeciesTO extends TransferObject {

        private static final long serialVersionUID = 4658714975166874629L;
        private final static Logger log = LogManager.getLogger(SourceToSpeciesTO.class.getName());
        
        /**
         * An {@code Enum} used to define the information type.
         * 
         * <ul>
         * <li>{@code DATA}: the information type is data.
         * <li>{@code ANNOTATION}: the information type is annotation.
         * </ul>
         */
        public enum InfoType implements EnumDAOField {
            DATA("data"), ANNOTATION("annotation");

            /**
             * Convert the {@code String} representation of a data type into a {@code InfoType}.
             * Operation performed by calling {@link TransferObject#convert(Class, String)} 
             * with {@code InfoType} as the {@code Class} argument, and {@code representation} as 
             * the {@code String} argument.
             * 
             * @param representation    A {@code String} representing a information type.
             * @return                  The {@code InfoType} corresponding to {@code representation}.
             * @throws IllegalArgumentException If {@code representation} does not correspond 
             *                                  to any {@code InfoType}.
             */
            public static final InfoType convertToInfoType(String representation) {
                log.traceEntry("{}", representation);
                return log.traceExit(TransferObject.convert(InfoType.class, representation));
            }

            /**
             * See {@link #getStringRepresentation()}
             */
            private final String stringRepresentation;

            /**
             * Constructor providing the {@code String} representation of this {@code InfoType}.
             * 
             * @param stringRepresentation  A {@code String} corresponding to this {@code InfoType}.
             */
            private InfoType(String stringRepresentation) {
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
        }


        /**
         * An {@code Integer} that is the ID of the data source.
         */
        private Integer dataSourceId;

        /**
         * An {@code Integer} that is the ID of the species.
         */
        private Integer speciesId;

        /**
         * A {@code DataType} that is the data type (for instance, affymetrix).
         */
        private DAODataType dataType;

        /**
         * A {@code InfoType} that is the information type (for instance, annotation).
         */
        private InfoType infoType;

        /**
         * Constructor providing the data source ID, the species ID, the data type,
         * and the information type.
         * <p>
         * All of these parameters are optional, so they can be {@code null} when not used.
         * 
         * @param datasourceId  An {@code Integer} that is the ID of the data source.
         * @param speciesId     An {@code Integer} that is the ID of the species.
         * @param dataType      A {@code DAODataType} that is the data type.
         * @param infoType      A {@code InfoType} that is the information type.
         */
        public SourceToSpeciesTO(Integer datasourceId, Integer speciesId, 
                DAODataType dataType, InfoType infoType) {
            this.dataSourceId   = datasourceId;
            this.speciesId      = speciesId;
            this.dataType       = dataType;
            this.infoType       = infoType;
        }
        
        /**
         * @return the {@code String} that is the ID of the data source.
         */
        public Integer getDataSourceId() {
            return dataSourceId;
        }

        /**
         * @return the {@code String} that is the ID of the species.
         */
        public Integer getSpeciesId() {
            return speciesId;
        }

        /**
         * @return the {@code DAODataType} that is the data type.
         */
        public DAODataType getDataType() {
            return dataType;
        }

        /**
         * @return the {@code InfoType} that is the information type.
         */
        public InfoType getInfoType() {
            return infoType;
        }

        @Override
        public String toString() {
            return "Data source ID: " + this.getDataSourceId() + 
                    " - Species ID: " + this.getSpeciesId() + 
                    " - Data type: " + this.getDataType() + 
                    " - Information type: " + this.getInfoType();
        }
    }
}
