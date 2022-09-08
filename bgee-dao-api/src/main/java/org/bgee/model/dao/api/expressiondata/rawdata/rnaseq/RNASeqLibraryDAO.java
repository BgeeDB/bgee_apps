package org.bgee.model.dao.api.expressiondata.rawdata.rnaseq;

import java.util.Collection;
import java.util.EnumSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.TransferObject;
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
     * <li>{@code PLATFORM_NAME}: corresponds to {@link RNASeqLibraryTO#getPlatformId()}.
     * <li>{@code SAMPLE_MULTIPLEXING}: corresponds to {@link RNASeqLibraryTO#getSampleMultiplexing()}.
     * <li>{@code LIBRARY_MULTIPLEXING}: corresponds to {@link RNASeqLibraryTO#getLibraryMultiplexing()}.
//XXX should maybe move all these columns in a table RNASeqProtocol
     * <li>{@code STRAND_SELECTION}: corresponds to {@link RNASeqLibraryTO#getStrandSelection()}.
     * <li>{@code CELL_COMPARTMENT}: corresponds to {@link RNASeqLibraryTO#getCellCompartment()}.
     * <li>{@code SEQUENCED_TRANSCRIPT_PART}: corresponds to {@link RNASeqLibraryTO#getSequencedTranscriptPart()}.
     * <li>{@code FRAGMENTATION}: corresponds to {@link RNASeqLibraryTO#Fragmentation()}.
     * <li>{@code POPULATION_CAPTURE_ID}: corresponds to {@link RNASeqLibraryTO#getPopulationCaptureId()}.
     * <li>{@code LIBRARY_TYPE}: corresponds to {@link RNASeqLibraryTO#getLibraryType()}.
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        ID("rnaSeqLibraryId"), EXPERIMENT_ID("rnaSeqExperimentId"), PLATFORM_ID("rnaSeqPlatformName"),
        SAMPLE_MULTIPLEXING("sampleMultiplexing"), LIBRARY_MULTIPLEXING("libraryMultiplexing"),
        MULTIPLE_ANNOTATED_SAMPLES("multipleLibraryAnnotatedSample"), STRAND_SELECTION("strandSelection"), 
        CELL_COMPARTMENT("cellCompartment"), SEQUENCED_TRANSCRIPT_PART("sequencedTranscriptPart"),
        FRAGMENTATION("fragmentation"), POPULATION_CAPTURE_ID("rnaSeqPopulationCaptureId"),
        LIBRARY_TYPE("libraryType");

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
     * @param experimentIds     A {@code {@link Collection} of {@code String} representing the IDs
     *                          of the RNA-Seq experiments of the libraries to retrieve from the data
     *                          source.
     * @param libraryIds        A {@code {@link Collection} of {@code String} representing the IDs
     *                          of the RNA-Seq library to retrieve from the data source.
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
    public RNASeqLibraryTOResultSet getRnaSeqLibraries(Collection<String> experimentIds,
            Collection<String> libraryIds, DAORawDataFilter rawDataFilter,
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
        private final static Logger log = LogManager.getLogger(RNASeqLibraryTO.class.getName());

        /**
         * <ul>
         * <li>{@code NA}: info not used for pseudo-mapping of reads
         * <li>{@code SINGLE_READ}: single-read library type
         * <li>{@code PAIRED_END}: paired-end library type
         * </ul>
         * @author Frederic Bastian
         * @version Bgee 14
         * @since Bgee 14
         */
        public enum LibraryType implements EnumDAOField {
            NA("NA"), SINGLE_READ("single"), PAIRED_END("paired");

            /**
             * See {@link #getStringRepresentation()}
             */
            private final String stringRepresentation;
            /**
             * Constructor providing the {@code String} representation of this {@code LibraryType}.
             *
             * @param stringRepresentation  A {@code String} corresponding to this {@code LibraryType}.
             */
            private LibraryType(String stringRepresentation) {
                this.stringRepresentation = stringRepresentation;
            }

            /**
             * Convert the {@code String} representation of a library type (for instance,
             * retrieved from a database) into a {@code LibraryType}. This method compares
             * {@code representation} to the value returned by {@link #getStringRepresentation()},
             * as well as to the value returned by {@link Enum#name()}, for each {@code LibraryType}.
             *
             * @param representation    A {@code String} representing a library type.
             * @return                  A {@code LibraryType} corresponding to {@code representation}.
             * @throws IllegalArgumentException If {@code representation} does not correspond to any {@code LibraryType}.
             */
            public static final LibraryType convertToLibraryType(String representation) {
                log.traceEntry("{}", representation);
                return log.traceExit(TransferObject.convert(LibraryType.class, representation));
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
         * The strand selection available for RNA-Seq libraries.
         *
         * @author Frederic Bastian
         * @version Bgee 14
         * @since Bgee 14
         */
        public enum StrandSelection implements EnumDAOField {
            NA("NA"), FORWARD("forward"), REVERSE("reverse"), UNSTRANDED("unstranded");

            /**
             * See {@link #getStringRepresentation()}
             */
            private final String stringRepresentation;
            /**
             * Constructor providing the {@code String} representation of this {@code StrandSelection}.
             *
             * @param stringRepresentation  A {@code String} corresponding to this {@code StrandSelection}.
             */
            private StrandSelection(String stringRepresentation) {
                this.stringRepresentation = stringRepresentation;
            }

            /**
             * Convert the {@code String} representation of a library orientation (for instance,
             * retrieved from a database) into a {@code StrandSelection}. This method compares
             * {@code representation} to the value returned by {@link #getStringRepresentation()},
             * as well as to the value returned by {@link Enum#name()}, for each {@code StrandSelection}.
             *
             * @param representation    A {@code String} representing a strand selection.
             * @return                  A {@code StrandSelection} corresponding to {@code representation}.
             * @throws IllegalArgumentException If {@code representation} does not correspond to any {@code StrandSelection}.
             */
            public static final StrandSelection convertToStrandSelection(String representation) {
                log.traceEntry("{}", representation);
                return log.traceExit(TransferObject.convert(StrandSelection.class, representation));
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
         * @author Julien Wollbrett
         * @version Bgee 15
         * @since Bgee 15
         */
        public enum CellCompartment implements EnumDAOField {
            NA("NA"), NUCLEUS("nucleus"), CELL("cell");

            /**
             * See {@link #getStringRepresentation()}
             */
            private final String stringRepresentation;
            /**
             * Constructor providing the {@code String} representation of this {@code CellCompartment}.
             *
             * @param stringRepresentation  A {@code String} corresponding to this {@code CellCompartment}.
             */
            private CellCompartment(String stringRepresentation) {
                this.stringRepresentation = stringRepresentation;
            }

            /**
             * Convert the {@code String} representation of a cell compartment (for instance,
             * retrieved from a database) into a {@code CellCompartment}. This method compares
             * {@code representation} to the value returned by {@link #getStringRepresentation()},
             * as well as to the value returned by {@link Enum#name()}, for each {@code CellCompartment}.
             *
             * @param representation    A {@code String} representing a cell compartment.
             * @return                  A {@code CellCompartment} corresponding to {@code representation}.
             * @throws IllegalArgumentException If {@code representation} does not correspond to any {@code CellCompartment}.
             */
            public static final CellCompartment convertToCellCompartment(String representation) {
                log.traceEntry("{}", representation);
                return log.traceExit(TransferObject.convert(CellCompartment.class, representation));
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
         * @author Julien Wollbrett
         * @version Bgee 15
         * @since Bgee 15
         */
        public enum SequencedTrancriptPart implements EnumDAOField {
            NA("NA"), THREE_PRIME("3prime"), FIVE_PRIME("5prime"), FULL_LENGTH("full_length");

            /**
             * See {@link #getStringRepresentation()}
             */
            private final String stringRepresentation;
            /**
             * Constructor providing the {@code String} representation of this {@code SequencedTrancriptPart}.
             *
             * @param stringRepresentation  A {@code String} corresponding to this {@code SequencedTrancriptPart}.
             */
            private SequencedTrancriptPart(String stringRepresentation) {
                this.stringRepresentation = stringRepresentation;
            }

            /**
             * Convert the {@code String} representation of a sequenced transcript part(for instance,
             * retrieved from a database) into a {@code SequencedTrancriptPart}. This method compares
             * {@code representation} to the value returned by {@link #getStringRepresentation()},
             * as well as to the value returned by {@link Enum#name()}, for each {@code SequencedTrancriptPart}.
             *
             * @param representation    A {@code String} representing a sequenced transcript part.
             * @return                  A {@code SequencedTrancriptPart} corresponding to {@code representation}.
             * @throws IllegalArgumentException If {@code representation} does not correspond to any {@code SequencedTrancriptPart}.
             */
            public static final SequencedTrancriptPart convertToSequencedTranscriptPart(
                    String representation) {
                log.traceEntry("{}", representation);
                return log.traceExit(TransferObject.convert(SequencedTrancriptPart.class, 
                        representation));
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

        private final String rnaSeqExperimentId;
        /**
         * A {@code String} representing the ID of the platform used 
         * to generate this RNA-Seq library.
         */
        private final String platformId;

        private final boolean sampleMultiplexing;
        private final boolean libraryMultiplexing;
        private final boolean multipleLibraryAnnotatedSamples;
        private final StrandSelection strandSelection;
        private final CellCompartment cellCompartment;
        private final SequencedTrancriptPart sequencedTranscriptPart;
        private final Integer fragmentation;
        private final Integer populationCaptureId;
        private final LibraryType libraryType;

        public RNASeqLibraryTO(String rnaSeqLibraryId, String rnaSeqExperimentId, String platformId,
                boolean sampleMultiplexing, boolean libraryMultiplexing, boolean multipleLibraryAnnotatedSamples, StrandSelection strandSelection,
                CellCompartment cellCompartment, SequencedTrancriptPart seqTranscriptPart, Integer fragmentation,
                Integer populationCaptureId, LibraryType libType) {
            super(rnaSeqLibraryId);
            this.rnaSeqExperimentId = rnaSeqExperimentId;
            this.platformId = platformId;
            this.sampleMultiplexing = sampleMultiplexing;
            this.libraryMultiplexing = libraryMultiplexing;
            this.multipleLibraryAnnotatedSamples = multipleLibraryAnnotatedSamples;
            this.strandSelection = strandSelection;
            this.cellCompartment = cellCompartment;
            this.sequencedTranscriptPart = seqTranscriptPart;
            this.fragmentation = fragmentation;
            this.populationCaptureId = populationCaptureId;
            this.libraryType = libType;
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
        public boolean isMultipleLibraryAnnotatedSamples() {
            return multipleLibraryAnnotatedSamples;
        }
        public boolean isLibraryMultiplexing() {
            return libraryMultiplexing;
        }
        public StrandSelection getStrandSelection() {
            return strandSelection;
        }
        public CellCompartment getCellCompartment() {
            return cellCompartment;
        }
        public SequencedTrancriptPart getSequencedTranscriptPart() {
            return sequencedTranscriptPart;
        }
        public Integer getFragmentation() {
            return fragmentation;
        }
        public Integer getPopulationCaptureId() {
            return populationCaptureId;
        }
        public LibraryType getLibraryType() {
            return libraryType;
        }

        @Override
        public String toString() {
            return "RNASeqLibraryTO [rnaSeqExperimentId=" + rnaSeqExperimentId + ", platformId=" + platformId
                    + ", sampleMultiplexing=" + sampleMultiplexing + ", libraryMultiplexing=" + libraryMultiplexing
                    + ", multipleLibraryAnnotatedSamples=" + multipleLibraryAnnotatedSamples + ", strandSelection="
                    + strandSelection + ", cellCompartment=" + cellCompartment + ", sequencedTranscriptPart="
                    + sequencedTranscriptPart + ", fragmentation=" + fragmentation + ", populationCaptureId="
                    + populationCaptureId + ", libraryType=" + libraryType + "]";
        }

    }
}
