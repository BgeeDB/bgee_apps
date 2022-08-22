package org.bgee.model.dao.api.expressiondata.rawdata.rnaseq;

import java.math.BigDecimal;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataAnnotatedTO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryDAO.RNASeqLibraryTO;

/**
 * {@code DAO} for {@link RNASeqLibraryAnnotatedSampleTO}s.
 * 
 * @author Julien Wollbrett
 * @version Bgee 15
 * @see RNASeqLibraryTO
 * @since Bgee 15
 */
public interface RNASeqLibraryAnnotatedSampleDAO extends DAO<RNASeqLibraryAnnotatedSampleDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code RNASeqLibraryTO}s
     * obtained from this {@code RNASeqLibraryDAO}.
     * <ul>
     * <li>{@code LIBRARY_ANNOTATED_ID}: corresponds to {@link RNASeqLibraryTO#getLibraryAnnotatedSampleId()}.
     * <li>{@code LIBRARY_ID}: corresponds to {@link RNASeqLibraryTO#getLibraryId()}.
     * <li>{@code CONDITION_ID}: corresponds to {@link RNASeqLibraryTO#getConditionId()}.

//XXX should maybe move all these columns in a table RNASeqProtocol
     * <li>{@code STRAND_SELECTION}: corresponds to {@link RNASeqLibraryTO#getStrandSelection()}.
     * <li>{@code CELL_COMPARTMENT}: corresponds to {@link RNASeqLibraryTO#getCellCompartment()}.
     * <li>{@code SEQUENCED_TRANSCRIPT_PART}: corresponds to {@link RNASeqLibraryTO#getSequencedTranscriptPart()}.
     * <li>{@code FRAGMENTATION}: corresponds to {@link RNASeqLibraryTO#Fragmentation()}.
     * <li>{@code POPULATION_CAPTURE_ID}: corresponds to {@link RNASeqLibraryTO#getPopulationCaptureId()}.
     * <li>{@code GENOTYPE_ID}: corresponds to {@link RNASeqLibraryTO#getGenotypeId()}.
     * <li>{@code BARCODE}: corresponds to {@link RNASeqLibraryTO#getBarcode()}.
     * <li>{@code ABUNDANCE_UNIT}: corresponds to {@link RNASeqLibraryTO#getAbundanceUnit()}.

     * <li>{@code MEAN_REF_INTERGENIC_DISCTRIBUTION}: corresponds to {@link RNASeqLibraryTO#getMeanRefIntergenicDistribution()}.
     * <li>{@code SD_REF_INTERGENIC_DISCTRIBUTION}: corresponds to {@link RNASeqLibraryTO#getSdRefIntergenicDistribution()}.
     * <li>{@code TMM_FACTOR}: corresponds to {@link RNASeqLibraryTO#getTmmFactor()}.
     * <li>{@code ABUNDANCE_THRESHOLD}: corresponds to {@link RNASeqLibraryTO#getTpmThreshold()}.
     * <li>{@code ALL_GENES_PERCENT_PRESENT}: corresponds to {@link RNASeqLibraryTO#getAllGenesPercentPresent()}.
     * <li>{@code PROTEIN_CODING_GENES_PERCENT_PRESENT}: corresponds to {@link RNASeqLibraryTO#getProteinCodingGenesPercentPresent()}.
     * <li>{@code INTERGENIC_REGION_PERCENT_PRESENT}: corresponds to {@link RNASeqLibraryTO#getIntergenicRegionsPercentPresent()}.
     * <li>{@code PVALUE_THRESHOLD}: corresponds to {@link RNASeqLibraryTO#getPValueThreshold()}.
     * <li>{@code ALL_READ_COUNT}: corresponds to {@link RNASeqLibraryTO#getAllReadCount()}.
     * <li>{@code ALL_UMIS_COUNT}: corresponds to {@link RNASeqLibraryTO#getAllUmisCount()}.
     * <li>{@code MAPPED_READ_COUNT}: corresponds to {@link RNASeqLibraryTO#getMappedReadCount()}.
     * <li>{@code MAPPED_UMIS_COUNT}: corresponds to {@link RNASeqLibraryTO#getMappedUmisCount()}.
     * <li>{@code MIN_READ_LENGTH}: corresponds to {@link RNASeqLibraryTO#getMinReadLength()}.
     * <li>{@code MAX_READ_LENGTH}: corresponds to {@link RNASeqLibraryTO#getMaxReadLength()}.
     * <li>{@code LIBRARY_TYPE}: corresponds to {@link RNASeqLibraryTO#getLibraryType()}.
     * <li>{@code MAX_RANK}: corresponds to {@link RNASeqLibraryTO#getMaxRank()}.
     * <li>{@code DISTINCT_RANK_COUNT}: corresponds to {@link RNASeqLibraryTO#getDistinctRankCount()}.
     * <li>{@code MULTIPLE_INDIVIDUAL_SAMPLE}: corresponds to {@link RNASeqLibraryTO#getMultipleIndividualSample()}.
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        ID("rnaSeqLibraryAnnotatedSampleId"), RNASEQ_LIBRARY_ID("rnaSeqLibraryId"),
        CONDITION_ID("conditionId"), STRAND_SELECTION("strandSelection"), 
        CELL_COMPARTMENT("cellCompartment"), SEQUENCED_TRANSCRIPT_PART("sequencedTranscriptPart"),
        FRAGMENTATION("fragmentation"), POPULATION_CAPTURE_ID("rnaSeqPopulationCaptureId"),
        GENOTYPE_ID("genotypeId"), BARCODE("barcode"), ABUNDANCE_UNIT("abundanceUnit"),
        MEAN_REF_INTERGENIC_DISCTRIBUTION("meanReferenceIntergenicDistribution"),
        SD_REF_INTERGENIC_DISCTRIBUTION("sdReferenceIntergenicDistribution"), TMM_FACTOR("tmmFactor"),
        ABUNDANCE_THRESHOLD("abundanceThreshold"), ALL_GENES_PERCENT_PRESENT("allGenesPercentPresent"),
        PROTEIN_CODING_GENES_PERCENT_PRESENT("proteinCodingGenesPercentPresent"),
        INTERGENIC_REGION_PERCENT_PRESENT("intergenicRegionsPercentPresent"),
        PVALUE_THRESHOLD("pValueThreshold"), ALL_READ_COUNT("allReadsCount"),
        ALL_UMIS_COUNT("allUMIsCount"), MAPPED_READ_COUNT("mappedReadsCount"),
        MAPPED_UMIS_COUNT("mappedUMIsCount"), MIN_READ_LENGTH("minReadLength"),
        MAX_READ_LENGTH("maxReadLength"), LIBRARY_TYPE("libraryType"), MAX_RANK("libraryMaxRank"),
        DISTINCT_RANK_COUNT("libraryDistinctRankCount"),
        MULTIPLE_INDIVIDUAL_SAMPLE("multipleLibraryIndividualSample");
        
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
    }

    /**
     * Retrieve from a data source a set of {@code RNASeqLibraryAnnotatedSampleTO}s,  
     * corresponding to the RNA-Seq library with the ID {@code libraryId}, 
     * {@code null} if none could be found.  
     * 
     * @param libraryIds        A {@code {@link Collection} of {@code String} representing the IDs 
     *                          of the RNA-Seq library to retrieve from the data source.
     * @param attributes        A {@code Collection} of {@code Attribute}s to specify the information
     *                          to retrieve from the annotated library.
     * @return  A {@code RNASeqLibraryAnnotatedSampleTOResultSet}, encapsulating all the data 
     *          related to the RNA-Seq annotated library retrieved from the data source, 
     *          or {@code null} if none could be found. 
     * @throws DAOException     If an error occurred when accessing the data source.
     */
    public RNASeqLibraryAnnotatedSampleTOResultSet getRnaSeqLibraryAnnotatedSampleFromLibraryIds(
            Collection<String> libraryIds, Collection<Attribute> attributes) throws DAOException;

    /**
     * Retrieve from a data source a set of {@code RNASeqLibraryAnnotatedSampleTO}s,  
     * corresponding to the annotated RNA-Seq libraries with the experiment IDs {@code experimentIds}, 
     * {@code null} if none could be found.  
     * 
     * @param experimentIds     A {@code {@link Collection} of {@code String} representing the IDs 
     *                          of the RNA-Seq experiments of the annotated libraries to retrieve 
     *                          from the data source.
     * @param attributes        A {@code Collection} of {@code Attribute}s to specify the information
     *                          to retrieve from the annotated library.
     * @return  A {@code RNASeqLibraryAnnotatedSampleTOResultSet}, encapsulating all the data 
     *          related to the RNA-Seq annotated libraries retrieved from the data source, 
     *          or {@code null} if none could be found. 
     * @throws DAOException     If an error occurred when accessing the data source.
     */
    public RNASeqLibraryAnnotatedSampleTOResultSet getRnaSeqLibraryAnnotatedSampleFromExperimentIds(
            Collection<String> experimentIds, Collection<Attribute> attributes) throws DAOException;

    /**
     * Retrieve from a data source a set of {@code RNASeqLibraryAnnotatedSampleTO}s,  
     * corresponding to the annotated RNA-Seq libraries with selected species IDs
     * and raw condition parameters
     * {@code null} if none could be found.  
     * 
     * @param rawDataFilter     A {@code DAORawDataFilter} allowing to specify which library to
     *                          retrieve.
     * @param attributes        A {@code Collection} of {@code Attribute}s to specify the information
     *                          to retrieve from the library.
     * @return  A {@code RNASeqLibraryTOResultSet}, encapsulating all the data 
     *          related to the annotated RNA-Seq libraries retrieved from the data source, 
     *          or {@code null} if none could be found. 
     * @throws DAOException     If an error occurred when accessing the data source.
     */
    public RNASeqLibraryAnnotatedSampleTOResultSet getRnaSeqLibraryAnnotatedSampleFromRawDataFilter(
            DAORawDataFilter rawDataFilter, Collection<Attribute> attributes) 
            throws DAOException;

    /**
     * Retrieve from a data source a set of {@code RNASeqLibraryAnnotatedSampleTO}s,  
     * corresponding to the annotated RNA-Seq libraries with selected library IDs, experiment IDs,
     * species IDs, and raw condition parameters
     * {@code null} if none could be found.  
     * 
     * @param libraryIds        A {@code {@link Collection} of {@code String} representing the IDs 
     *                          of the RNA-Seq library of the annotated libraries to retrieve from
     *                          the data source. 
     * @param experimentIds     A {@code {@link Collection} of {@code String} representing the IDs 
     *                          of the RNA-Seq experiments of the annotated libraries to retrieve
     *                          from the data source.
     * @param rawDataFilter     A {@code DAORawDataFilter} allowing to specify which annotated 
     *                          libraries to retrieve.
     * @param attributes        A {@code Collection} of {@code Attribute}s to specify the information
     *                          to retrieve from the library.
     *                          
     * @return  A {@code RNASeqLibraryAnnotatedSampleTOResultSet}, encapsulating all the data 
     *          related to the annotated RNA-Seq libraries retrieved from the data source, 
     *          or {@code null} if none could be found. 
     * @throws DAOException     If an error occurred when accessing the data source.
     */
    public RNASeqLibraryAnnotatedSampleTOResultSet getRnaSeqLibraryAnnotatedSamples(
            Collection<String> libraryIds, Collection<String> experimentIds,
            DAORawDataFilter rawDataFilter, Collection<Attribute> attributes) 
            throws DAOException;

    /**
     * {@code DAOResultSet} for {@code RNASeqExperimentTO}s
     * 
     * @author  Julien Wollbrett
     * @version Bgee 15, Aug. 2022
     * @since   Bgee 15
     */
    public interface RNASeqLibraryAnnotatedSampleTOResultSet extends DAOResultSet<RNASeqLibraryAnnotatedSampleTO> {
    }

    /**
     * {@code TransferObject} for RNA-Seq libraries annotated samples.
     * 
     * @author Julien Wollbrett
     * @version Bgee 15
     * @since Bgee 15
     */
    public final class RNASeqLibraryAnnotatedSampleTO extends EntityTO<Integer> implements RawDataAnnotatedTO {

        private static final long serialVersionUID = 5975028551851785246L;
        private final static Logger log = LogManager.getLogger(
                RNASeqLibraryAnnotatedSampleTO.class.getName());

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

        /**
         * @author Julien Wollbrett
         * @version Bgee 15
         * @since Bgee 15
         */
        public enum AbundanceUnit implements EnumDAOField {
            TPM("tpm"), CPM("cpm");

            /**
             * See {@link #getStringRepresentation()}
             */
            private final String stringRepresentation;
            /**
             * Constructor providing the {@code String} representation of this {@code AbundanceUnit}.
             *
             * @param stringRepresentation  A {@code String} corresponding to this {@code AbundanceUnit}.
             */
            private AbundanceUnit(String stringRepresentation) {
                this.stringRepresentation = stringRepresentation;
            }

            /**
             * Convert the {@code String} representation of a abundance unit (for instance,
             * retrieved from a database) into a {@code AbundanceUnit}. This method compares
             * {@code representation} to the value returned by {@link #getStringRepresentation()},
             * as well as to the value returned by {@link Enum#name()}, for each {@code AbundanceUnit}.
             *
             * @param representation    A {@code String} representing an abundance unit.
             * @return                  A {@code AbundanceUnit} corresponding to {@code representation}.
             * @throws IllegalArgumentException If {@code representation} does not correspond to any {@code AbundanceUnit}.
             */
            public static final AbundanceUnit convertToAbundanceUnit(String representation) {
                log.traceEntry("{}", representation);
                return log.traceExit(TransferObject.convert(AbundanceUnit.class, representation));
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
        
        private final String libraryId;
        private final Integer conditionId;
        private final StrandSelection strandSelection;
        private final CellCompartment cellCompartment;
        private final SequencedTrancriptPart sequencedTranscriptPart;
        private final Integer fragmentation;
        private final Integer populationCaptureId;
        private final Integer genotypeId;
        private final String barcode;
        private final AbundanceUnit abundanceUnit;
        private final BigDecimal meanRefIntergenicDistribution;
        private final BigDecimal sdRefIntergenicDistribution;
        /**
         * A {@code BigDecimal} that is the normalization factor from TMM.
         */
        private final BigDecimal tmmFactor;
        /**
         * A {@code BigDecimal} representing the abudance threshold, above which genes are considered as "present".
         */
        private final BigDecimal abundanceThreshold;
        /**
         * A {@code BigDecimal} representing the percentage of genes 
         * flagged as "present" in this library (values from 0 to 100). 
         */
        private final BigDecimal allGenesPercentPresent;
        /**
         * A {@code BigDecimal} representing the percentage of protein-coding genes 
         * flagged as "present" in this library (values from 0 to 100). 
         */
        private final BigDecimal proteinCodingGenesPercentPresent;
        /**
         * A {@code BigDecimal} representing the percentage of intergenic regions  
         * flagged as "present" in this library (values from 0 to 100). 
         */
        private final BigDecimal intergenicRegionsPercentPresent;

        private final BigDecimal pValueThreshold;
        /**
         * An {@code int} representing the count of reads present in this library.
         */
        private final Integer allReadCount;
        /**
         * An {@code int} representing the count of UMIs present in this library.
         */
        private final Integer allUMIsCount;
        /**
         * An {@code int} representing the count of reads mapped to anything.
         */
        private final Integer mappedReadCount;
        /**
         * An {@code int} representing the count of UMIs mapped to anything.
         */
        private final Integer mappedUMIsCount;
        /**
         * An {@code int} representing the minimum length in bases of reads aligned in this library.
         */
        private final Integer minReadLength;
        /**
         * An {@code int} representing the maximum length in bases of reads aligned in this library.
         */
        private final Integer maxReadLength;
        /**
         * A {@code LibraryType} representing the type of this library.
         */
        private final LibraryType libraryType;
        private final BigDecimal maxRank;
        private final Integer distinctRankCount;
        private final Boolean multipleLibraryIndividualSample;

        /**
         * Default constructor. 
         */
        public RNASeqLibraryAnnotatedSampleTO(Integer libraryAnnotatedSampleId,String libraryId, Integer conditionId, 
                StrandSelection strandSelection, CellCompartment cellCompartment,
                SequencedTrancriptPart seqTranscriptPart, Integer fragmentation,
                Integer populationCaptureId, Integer genotypeId, String barcode, AbundanceUnit unit,
                BigDecimal meanRefIntergenicDistribution, BigDecimal sdRefIntergenicDistribution,
                BigDecimal tmmFactor, BigDecimal abundanceThreshold, BigDecimal allGenesPercentPresent,
                BigDecimal proteinCodingGenesPercentPresent, BigDecimal intergenicRegionsPercentPresent,
                BigDecimal  pValueThreshold, Integer allReadCount, Integer allUMIsCount,
                Integer mappedReadCount, Integer mappedUMIsCount, Integer minReadLength,
                Integer maxReadLength, LibraryType libType, BigDecimal maxRank, 
                Integer distinctRankCount, Boolean multipleLibraryIndividualSample) {
            super(libraryAnnotatedSampleId);
            this.libraryId = libraryId;
            this.conditionId = conditionId;
            this.strandSelection = strandSelection;
            this.cellCompartment = cellCompartment;
            this.sequencedTranscriptPart = seqTranscriptPart;
            this.fragmentation = fragmentation;
            this.populationCaptureId = populationCaptureId;
            this.genotypeId = genotypeId;
            this.barcode = barcode;
            this.abundanceUnit = unit;
            this.meanRefIntergenicDistribution = meanRefIntergenicDistribution;
            this.sdRefIntergenicDistribution = sdRefIntergenicDistribution;
            this.tmmFactor = tmmFactor;
            this.abundanceThreshold = abundanceThreshold;
            this.allGenesPercentPresent = allGenesPercentPresent;
            this.proteinCodingGenesPercentPresent = proteinCodingGenesPercentPresent;
            this.intergenicRegionsPercentPresent = intergenicRegionsPercentPresent;
            this.pValueThreshold = pValueThreshold;
            this.allReadCount = allReadCount;
            this.allUMIsCount = allUMIsCount;
            this.mappedReadCount = mappedReadCount;
            this.mappedUMIsCount = mappedUMIsCount;
            this.minReadLength = minReadLength;
            this.maxReadLength = maxReadLength;
            this.libraryType = libType;
            this.maxRank = maxRank;
            this.distinctRankCount = distinctRankCount;
            this.multipleLibraryIndividualSample = multipleLibraryIndividualSample;
        }
        
        public String getLibraryId() {
            return libraryId;
        }
        @Override
        public Integer getConditionId() {
            return conditionId;
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
        public Integer getGenotypeId() {
            return genotypeId;
        }
        public String getBarcode() {
            return barcode;
        }
        public AbundanceUnit getAbundanceUnit() {
            return abundanceUnit;
        }
        public BigDecimal getMeanRefIntergenicDistribution() {
            return meanRefIntergenicDistribution;
        }
        public BigDecimal getSdRefIntergenicDistribution() {
            return sdRefIntergenicDistribution;
        }
        public BigDecimal getTmmFactor() {
            return tmmFactor;
        }
        public BigDecimal getAbundanceThreshold() {
            return abundanceThreshold;
        }
        public BigDecimal getAllGenesPercentPresent() {
            return allGenesPercentPresent;
        }
        public BigDecimal getProteinCodingGenesPercentPresent() {
            return proteinCodingGenesPercentPresent;
        }
        public BigDecimal getIntergenicRegionsPercentPresent() {
            return intergenicRegionsPercentPresent;
        }
        public BigDecimal getpValueThreshold() {
            return pValueThreshold;
        }
        public Integer getAllReadCount() {
            return allReadCount;
        }
        public Integer getAllUMIsCount() {
            return allUMIsCount;
        }
        public Integer getMappedReadCount() {
            return mappedReadCount;
        }
        public Integer getMappedUMIsCount() {
            return mappedUMIsCount;
        }
        public Integer getMinReadLength() {
            return minReadLength;
        }
        public Integer getMaxReadLength() {
            return maxReadLength;
        }
        public LibraryType getLibraryType() {
            return libraryType;
        }
        public BigDecimal getMaxRank() {
            return maxRank;
        }
        public Integer getDistinctRankCount() {
            return distinctRankCount;
        }
        public boolean isMultipleLibraryIndividualSample() {
            return multipleLibraryIndividualSample;
        }

        @Override
        public String toString() {
            return "RNASeqLibraryAnnotatedSampleTO [libraryId=" + libraryId + ", conditionId=" + conditionId
                    + ", strandSelection=" + strandSelection + ", cellCompartment=" + cellCompartment
                    + ", sequencedTranscriptPart=" + sequencedTranscriptPart + ", fragmentation=" + fragmentation
                    + ", populationCaptureId=" + populationCaptureId + ", genotypeId=" + genotypeId + ", barcode="
                    + barcode + ", abundanceUnit=" + abundanceUnit + ", meanRefIntergenicDistribution="
                    + meanRefIntergenicDistribution + ", sdRefIntergenicDistribution=" + sdRefIntergenicDistribution
                    + ", tmmFactor=" + tmmFactor + ", abundanceThreshold=" + abundanceThreshold
                    + ", allGenesPercentPresent=" + allGenesPercentPresent + ", proteinCodingGenesPercentPresent="
                    + proteinCodingGenesPercentPresent + ", intergenicRegionsPercentPresent="
                    + intergenicRegionsPercentPresent + ", pValueThreshold=" + pValueThreshold + ", allReadCount="
                    + allReadCount + ", allUMIsCount=" + allUMIsCount + ", mappedReadCount=" + mappedReadCount
                    + ", mappedUMIsCount=" + mappedUMIsCount + ", minReadLength=" + minReadLength + ", maxReadLength="
                    + maxReadLength + ", libraryType=" + libraryType + ", maxRank=" + maxRank + ", distinctRankCount="
                    + distinctRankCount + ", multipleLibraryIndividualSample=" + multipleLibraryIndividualSample + "]";
        }
    }
}
