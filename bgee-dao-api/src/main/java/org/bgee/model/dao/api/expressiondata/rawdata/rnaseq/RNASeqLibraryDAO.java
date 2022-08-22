package org.bgee.model.dao.api.expressiondata.rawdata.rnaseq;

import java.util.Collection;
import java.util.EnumSet;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataAssayDAO.AssayPartOfExpTO;

/**
 * {@code DAO} for {@link RNASeqLibraryTO}s.
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @author Julien Wollbrett
 * @version Bgee 15
 * @see RNASeqLibraryTO
 * @since Bgee 12
 */
public interface RNASeqLibraryDAO extends DAO<RNASeqLibraryDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code RNASeqLibraryTO}s
     * obtained from this {@code RNASeqLibraryDAO}.
     * <ul>
// columns from rnaSeqLibrary table
     * <li>{@code ID}: corresponds to {@link RNASeqLibraryTO#getId()}.
     * <li>{@code EXPERIMENT_ID}: corresponds to {@link RNASeqLibraryTO#getExperimentId()}.
     * <li>{@code PLATFORM_ID}: corresponds to {@link RNASeqLibraryTO#getPlatformId()}.
     * <li>{@code SAMPLE_MULTIPLEXING}: corresponds to {@link RNASeqLibraryTO#getSampleMultiplexing()}.
     * <li>{@code LIBRARY_MULTIPLEXING}: corresponds to {@link RNASeqLibraryTO#getLibraryMultiplexing()}.
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        ID("rnaSeqLibraryId"), EXPERIMENT_ID("rnaSeqExperimentId"), PLATFORM_ID("rnaSeqPlatformId"),
        SAMPLE_MULTIPLEXING("sampleMultiplexing"), LIBRARY_MULTIPLEXING("libraryMultiplexing");

        /**
         * A {@code String} that is the corresponding field name in {@code ESTTO} class.
         * @see {@link Attribute#getTOFieldName()}
         */
        private final String fieldName;

        private Attribute(String fieldName) {
            this.fieldName = fieldName;
        }
        @Override
        public String getTOFieldName() {
            return this.fieldName;
        }
        public EnumSet<Attribute> getRnaSeqLibraryField() {
            return EnumSet.of(ID, EXPERIMENT_ID, PLATFORM_ID, SAMPLE_MULTIPLEXING,
                    LIBRARY_MULTIPLEXING);
        }
        public EnumSet<Attribute> getRnaSeqLibraryAnnotatedField() {
            return EnumSet.complementOf(getRnaSeqLibraryAnnotatedField());
        }
    }

    /**
     * Retrieve from a data source a set of {@code RNASeqLibraryTO}s,
     * corresponding to the RNA-Seq library with the ID {@code libraryId}, 
     * {@code null} if none could be found.
     * 
     * @param libraryIds        A {@code {@link Collection} of {@code String} representing the IDs
     *                          of the RNA-Seq library to retrieve from the data source.
     * @param attributes        A {@code Collection} of {@code Attribute}s to specify the information
     *                          to retrieve from the library.
     * @return	A {@code RNASeqLibraryTO}, encapsulating all the data
     * 			related to the RNA-Seq library retrieved from the data source,
     * 			or {@code null} if none could be found. 
     * @throws DAOException 	If an error occurred when accessing the data source.
     */
    public RNASeqLibraryTOResultSet getRnaSeqLibraryFromIds(Collection<String> libraryIds,
            Collection<Attribute> attributes) throws DAOException;

    /**
     * Retrieve from a data source a set of {@code RNASeqLibraryTO}s,
     * corresponding to the RNA-Seq library with the experiment IDs {@code experimentIds},
     * {@code null} if none could be found.
     * 
     * @param experimentIds     A {@code {@link Collection} of {@code String} representing the IDs
     *                          of the RNA-Seq experiments of the libraries to retrieve from the data
     *                          source.
     * @param attributes        A {@code Collection} of {@code Attribute}s to specify the information
     *                          to retrieve from the library.
     * @return  A {@code RNASeqLibraryTO}, encapsulating all the data
     *          related to the RNA-Seq library retrieved from the data source,
     *          or {@code null} if none could be found.
     * @throws DAOException     If an error occurred when accessing the data source.
     */
    public RNASeqLibraryTOResultSet getRnaSeqLibraryFromExperimentIds(Collection<String> experimentIds,
            Collection<Attribute> attributes) throws DAOException;

    /**
     * Retrieve from a data source a set of {@code RNASeqLibraryTO}s,
     * corresponding to the RNA-Seq library with with selected species IDs, gene IDs
     * and raw condition parameters
     * {@code null} if none could be found.
     * 
     * @param rawDataFilter     A {@code DAORawDataFilter} allowing to specify which library to
     *                          retrieve.
     * @param attributes        A {@code Collection} of {@code Attribute}s to specify the information
     *                          to retrieve from the library.
     * @return  A {@code RNASeqLibraryTO}, encapsulating all the data
     *          related to the RNA-Seq library retrieved from the data source,
     *          or {@code null} if none could be found. 
     * @throws DAOException     If an error occurred when accessing the data source.
     */
    public RNASeqLibraryTOResultSet getRnaSeqLibraryFromRawDataFilter(DAORawDataFilter rawDataFilter,
            Collection<Attribute> attributes)
            throws DAOException;

    /**
     * Retrieve from a data source a set of {@code RNASeqLibraryTO}s,
     * corresponding to the RNA-Seq library with selected library IDs, species IDs, gene IDs
     * and raw condition parameters
     * {@code null} if none could be found.
     * 
     * @param libraryIds        A {@code {@link Collection} of {@code String} representing the IDs
     *                          of the RNA-Seq library to retrieve from the data source.
     * @param experimentIds     A {@code {@link Collection} of {@code String} representing the IDs
     *                          of the RNA-Seq experiments of the libraries to retrieve from the data
     *                          source.
     * @param rawDataFilter     A {@code DAORawDataFilter} allowing to specify which library to
     *                          retrieve.
     * @param attributes        A {@code Collection} of {@code Attribute}s to specify the information
     *                          to retrieve from the library.
     * 
     * @return  A {@code RNASeqLibraryTO}, encapsulating all the data
     *          related to the RNA-Seq library retrieved from the data source,
     *          or {@code null} if none could be found.
     * @throws DAOException     If an error occurred when accessing the data source.
     */
    public RNASeqLibraryTOResultSet getRnaSeqLibraries(Collection<String> libraryIds,
            Collection<String> experimentIds, DAORawDataFilter rawDataFilter,
            Collection<Attribute> attributes)
            throws DAOException;

    /**
     * {@code DAOResultSet} for {@code RNASeqExperimentTO}s
     * 
     * @author  Julien Wollbrett
     * @version Bgee 15, Aug. 2022
     * @since   Bgee 15
     */
    public interface RNASeqLibraryTOResultSet extends DAOResultSet<RNASeqLibraryTO> {
    }

    /**
     * {@code TransferObject} for RNA-Seq libraries.
     * 
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 14
     * @since Bgee 12
     */
    public final class RNASeqLibraryTO extends EntityTO<String>
    implements AssayPartOfExpTO<String, String> {
        private static final long serialVersionUID = -6303846733657736568L;

        private final String rnaSeqExperimentId;
        /**
         * A {@code String} representing the ID of the platform used 
         * to generate this RNA-Seq library.
         */
        private final String platformId;

        private final boolean sampleMultiplexing;
        private final boolean libraryMultiplexing;

        public RNASeqLibraryTO(String rnaSeqLibraryId, String rnaSeqExperimentId, String platformId,
                boolean sampleMultiplexing, boolean libraryMultiplexing) {
            super(rnaSeqLibraryId);
            this.rnaSeqExperimentId = rnaSeqExperimentId;
            this.platformId = platformId;
            this.sampleMultiplexing = sampleMultiplexing;
            this.libraryMultiplexing = libraryMultiplexing;
        }

        @Override
        public String getExperimentId() {
            return this.rnaSeqExperimentId;
        }
        public String getPlatformId() {
            return platformId;
        }
        public boolean isSampleMultiplexing() {
            return sampleMultiplexing;
        }
        public boolean isLibraryMultiplexing() {
            return libraryMultiplexing;
        }

        @Override
        public String toString() {
            return "RNASeqLibraryTO [rnaSeqExperimentId=" + rnaSeqExperimentId + ", platformId=" + platformId
                    + ", sampleMultiplexing=" + sampleMultiplexing + ", libraryMultiplexing=" + libraryMultiplexing
                    + "]";
        }
    }
}
