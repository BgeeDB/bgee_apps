package org.bgee.model.file;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.CommonService;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.exception.QueryInterruptedException;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryDAO;
import org.bgee.model.dao.api.file.DownloadFileDAO;
import org.bgee.model.dao.api.file.DownloadFileDAO.DownloadFileTO;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.call.CallService;
import org.bgee.model.species.Species;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@link Service} to obtain {@link DownloadFile}s. 
 * Users should use the {@link org.bgee.model.ServiceFactory} to obtain {@code DownloadFileService}s.
 *
 * @author Philippe Moret
 * @author Frederic Bastian
 * @version Bgee 15.2, May 2024
 */
public class DownloadFileService extends CommonService {

    private static final Logger log = LogManager.getLogger(DownloadFileService.class.getName());

    /**
     * @param serviceFactory            The {@code ServiceFactory} to be used to obtain {@code Service}s 
     *                                  and {@code DAOManager}.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    public DownloadFileService(ServiceFactory serviceFactory) {
        super(serviceFactory);
    }

    /**
     * Gets available {@code DownloadFile}s.
     *
     * @param categories    A {@code Collection} of {@code DownloadFile.Category}s specifying
     *                      the categories for which download files should be retrieved. If {@code null}
     *                      or empty, all download files are retrieved.
     * @return              a {@code List} of {@code DownloadFile}
     * @throws DAOException                 If an error occurred while accessing a {@code DAO}.
     * @throws QueryInterruptedException    If a query to a {@code DAO} was intentionally interrupted.
     */
    public List<SpeciesDownloadFile> getDownloadFiles(Collection<SpeciesDownloadFile.Category> categories)
            throws DAOException, QueryInterruptedException {
        log.traceEntry("{}", categories);
        EnumSet<DownloadFileTO.CategoryEnum> cats = categories == null || categories.isEmpty()? null:
            categories.stream().map(cat -> convertServiceCategoryToDAOCategory(cat))
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(DownloadFileTO.CategoryEnum.class)));
        return log.traceExit(getDaoManager().getDownloadFileDAO().getDownloadFiles(cats).stream()
                .map(DownloadFileService::mapFromTO)
                .collect(Collectors.toList()));
    }

    public List<ExperimentDownloadFile> loadExperimentDownloadFiles(String experimentId, DataType dataType) {
        log.traceEntry("{}, {}", experimentId, dataType);
        if (StringUtils.isBlank(experimentId)) {
            throw log.throwing(new IllegalArgumentException("An experimentId must be provided"));
        }
        if (dataType == null) {
            throw log.throwing(new IllegalArgumentException("An DataType must be provided"));
        }
        //We have no experiment download files for data types other than AFFYMETRIX, RNA_SEQ, SC_RNA_SEQ,
        //so we return an empty list.
        if (!dataType.equals(DataType.AFFYMETRIX) && !dataType.equals(DataType.RNA_SEQ)
                && !dataType.equals(DataType.SC_RNA_SEQ)) {
            return log.traceExit(List.of());
        }

        DAORawDataFilter filter = new DAORawDataFilter(Set.of(experimentId), null, null, null);
        DAODataType daoDataType = convertDataTypeToDAODataType(dataType);
        //For RNA-Seq conditions, only RNA_SEQ is used, not SC_RNA_SEQ
        if (daoDataType.equals(DAODataType.SC_RNA_SEQ)) {
            daoDataType = DAODataType.RNA_SEQ;
        }

        //Now, we always need to retrieve the species part of the experiment.
        //In order to retrieve the species, we query the related conditions
        RawDataConditionDAO condDAO = this.getDaoManager().getRawDataConditionDAO();
        Set<Integer> speciesIds = condDAO.getRawDataConditionsLinkedToDataType(
                    Set.of(filter),
                    daoDataType,
                    dataType.equals(DataType.AFFYMETRIX)? null: (dataType.equals(DataType.SC_RNA_SEQ)? true: false),
                    EnumSet.of(RawDataConditionDAO.Attribute.SPECIES_ID)
                ).stream()
                .map(condTO -> condTO.getSpeciesId())
                .collect(Collectors.toSet());
        Set<Species> species = this.getServiceFactory().getSpeciesService().loadSpeciesByIds(speciesIds, false);

        if (dataType.equals(DataType.AFFYMETRIX)) {
            return log.traceExit(species.stream()
                    .map(s -> new ExperimentDownloadFile(
                            this.getServiceFactory().getBgeeProperties().getDownloadAffyProcExprValueFilesRootDirectory() +
                                    s.getSpeciesFullNameWithoutSpace() + "/",
                            s.getSpeciesFullNameWithoutSpace() +
                                    "_Affymetrix_probesets_" + experimentId + ".tar.gz",
                            "Affymetrix processed expression values for species " + s.getScientificName() +
                                    " in experiment " + experimentId,
                            //Since we don't store yet this info to database, we don't have access to the file size
                            0L,
                            ExperimentDownloadFile.Category.ANNOTATED_SAMPLES,
                            dataType,
                            false,
                            s)
                    ).collect(Collectors.toList()));
        }

        assert dataType.equals(DataType.RNA_SEQ) || dataType.equals(DataType.SC_RNA_SEQ);

        //Bulk or single-cell RNA-Seq experiments can contain multiple species and multiple "data types"
        //(e.g., for SC_RNA_SEQ, can contain both full-length and droplet-based data).
        //We need to query the data source to get the required information.

        //to know if we have full-length and/or droplet-based data
        RNASeqLibraryDAO libDAO = this.getDaoManager().getRnaSeqLibraryDAO();
        Set<Boolean> multiplexings =
                //for now we don't have multiplexed data for bulk RNA-Seq
                dataType.equals(DataType.RNA_SEQ)?
                        Set.of(false):
                        //otherwise we query the data source
                        libDAO.getRnaSeqLibrary(
                                Set.of(filter),
                                true,
                                0L, 100000,
                                EnumSet.of(RNASeqLibraryDAO.Attribute.SAMPLE_MULTIPLEXING))
                        .stream().map(lib -> lib.getSampleMultiplexing())
                        .collect(Collectors.toSet());
        assert multiplexings.size() == 1 || dataType.equals(DataType.SC_RNA_SEQ):
            "For bulk RNA-Seq we for now never have different types of multiplexing in an experiment";


        return log.traceExit(species.stream().flatMap(spe ->

            multiplexings.stream().flatMap(multiplexing -> {

                if (dataType.equals(DataType.RNA_SEQ)) {
                    assert multiplexing == false;
                    String path = this.getServiceFactory().getBgeeProperties()
                            .getDownloadRNASeqProcExprValueFilesRootDirectory()
                            + spe.getSpeciesFullNameWithoutSpace() + "/";
                    String fileName = spe.getSpeciesFullNameWithoutSpace()
                            + "_RNA-Seq_read_counts_TPM_" + experimentId + ".tsv.gz";

                    return Stream.of(new ExperimentDownloadFile(
                            path,
                            fileName,
                            "Processed expression values for bulk RNA-Seq for species " +
                            spe.getScientificName() + " in experiment " + experimentId,
                            //Since we don't store yet this info to database, we don't have access to the file size
                            0L,
                            ExperimentDownloadFile.Category.ANNOTATED_SAMPLES,
                            dataType,
                            false,
                            spe));

                } else if (dataType.equals(DataType.SC_RNA_SEQ)) {

                    return EnumSet.allOf(ExperimentDownloadFile.Category.class).stream().map(cat -> {

                        String path = this.getServiceFactory().getBgeeProperties().getDownloadRootDirectory() +
                                (cat.equals(ExperimentDownloadFile.Category.ANNOTATED_SAMPLES)?
                                        "processed_expr_values/": "h5ad/") +
                                (multiplexing? "droplet_based/": "full_length/") +
                                spe.getSpeciesFullNameWithoutSpace() + "/";
                        String fileName = spe.getSpeciesFullNameWithoutSpace() +
                                (cat.equals(ExperimentDownloadFile.Category.ANNOTATED_SAMPLES)?
                                        (multiplexing?
                                                "_Droplet-Based_SC_RNA-Seq_read_counts_CPM_" + experimentId + ".tsv.gz":
                                                "_Full-Length_SC_RNA-Seq_read_counts_TPM_" + experimentId + ".tsv.gz"):

                                        (multiplexing?
                                                "_" + experimentId + "_droplet_based.h5ad":
                                                "_" + experimentId + "_full_length.h5ad"));
                        String fileTitle = (cat.equals(ExperimentDownloadFile.Category.ANNOTATED_SAMPLES)?
                                "Processed expression values": "Data in H5AD") + " for " +
                                (multiplexing? "droplet-based data": "full-length data") + " for species " +
                                spe.getScientificName() + " in experiment " + experimentId;
                        return new ExperimentDownloadFile(
                                path,
                                fileName,
                                fileTitle,
                                //Since we don't store yet this info to database, we don't have access to the file size
                                0L,
                                cat,
                                dataType,
                                multiplexing,
                                spe);
                    });

                }
                throw log.throwing(new IllegalStateException("Unsupported DataType " + dataType));
            })
        ).collect(Collectors.toList()));
    }

    /**
     * Maps {@link DownloadFileDAO.DownloadFileTO} to a {@link DownloadFile}.
     *
     * @param downloadFileTO The {@link DownloadFileDAO.DownloadFileTO} to map.
     * @return The mapped  {@link DownloadFile}.
     */
    private static SpeciesDownloadFile mapFromTO(DownloadFileDAO.DownloadFileTO downloadFileTO) {
        log.traceEntry("{}", downloadFileTO);
        if (downloadFileTO == null) {
            return log.traceExit((SpeciesDownloadFile) null);
        }
        return log.traceExit(new SpeciesDownloadFile(downloadFileTO.getPath(),
                downloadFileTO.getName(),
                null,
                downloadFileTO.getSize(),
                mapDAOCategoryToServiceCategory(downloadFileTO.getCategory()),
                downloadFileTO.getSpeciesDataGroupId(),
                downloadFileTO.getConditionParameters().stream()
                    .map(p -> mapDAOCondParamToServiceCondParam(p))
                    .collect(Collectors.toSet())));
    }

    private static SpeciesDownloadFile.Category mapDAOCategoryToServiceCategory(
            DownloadFileTO.CategoryEnum daoEnum) {
        log.traceEntry("{}", daoEnum);

        switch (daoEnum) {
            case EXPR_CALLS_COMPLETE:
                return log.traceExit(SpeciesDownloadFile.Category.EXPR_CALLS_COMPLETE);
            case EXPR_CALLS_SIMPLE:
                return log.traceExit(SpeciesDownloadFile.Category.EXPR_CALLS_SIMPLE);
            case DIFF_EXPR_ANAT_SIMPLE:
                return log.traceExit(SpeciesDownloadFile.Category.DIFF_EXPR_ANAT_SIMPLE);
            case DIFF_EXPR_ANAT_COMPLETE:
                return log.traceExit(SpeciesDownloadFile.Category.DIFF_EXPR_ANAT_COMPLETE);
            case DIFF_EXPR_DEV_COMPLETE:
                return log.traceExit(SpeciesDownloadFile.Category.DIFF_EXPR_DEV_COMPLETE);
            case DIFF_EXPR_DEV_SIMPLE:
                return log.traceExit(SpeciesDownloadFile.Category.DIFF_EXPR_DEV_SIMPLE);
            case ORTHOLOG:
                return log.traceExit(SpeciesDownloadFile.Category.ORTHOLOG);
            case AFFY_ANNOT:
                return log.traceExit(SpeciesDownloadFile.Category.AFFY_ANNOT);
            case AFFY_DATA:
                return log.traceExit(SpeciesDownloadFile.Category.AFFY_DATA);
            case RNASEQ_ANNOT:
                return log.traceExit(SpeciesDownloadFile.Category.RNASEQ_ANNOT);
            case RNASEQ_DATA:
                return log.traceExit(SpeciesDownloadFile.Category.RNASEQ_DATA);
            case FULL_LENGTH_ANNOT:
                return log.traceExit(SpeciesDownloadFile.Category.FULL_LENGTH_ANNOT);
            case FULL_LENGTH_DATA:
                return log.traceExit(SpeciesDownloadFile.Category.FULL_LENGTH_DATA);
            case FULL_LENGTH_H5AD:
                return log.traceExit(SpeciesDownloadFile.Category.FULL_LENGTH_H5AD);
            case DROPLET_BASED_ANNOT:
                return log.traceExit(SpeciesDownloadFile.Category.DROPLET_BASED_ANNOT);
            case DROPLET_BASED_DATA:
                return log.traceExit(SpeciesDownloadFile.Category.DROPLET_BASED_DATA);
            case DROPLET_BASED_H5AD:
                return log.traceExit(SpeciesDownloadFile.Category.DROPLET_BASED_H5AD);
            default:
                throw log.throwing(new IllegalArgumentException("Category not supported: " + daoEnum));
        }
    }
    private static DownloadFileTO.CategoryEnum convertServiceCategoryToDAOCategory(
            SpeciesDownloadFile.Category serviceEnum) {
        log.traceEntry("{}", serviceEnum);

        switch (serviceEnum) {
            case EXPR_CALLS_COMPLETE:
                return log.traceExit(DownloadFileTO.CategoryEnum.EXPR_CALLS_COMPLETE);
            case EXPR_CALLS_SIMPLE:
                return log.traceExit(DownloadFileTO.CategoryEnum.EXPR_CALLS_SIMPLE);
            case DIFF_EXPR_ANAT_SIMPLE:
                return log.traceExit(DownloadFileTO.CategoryEnum.DIFF_EXPR_ANAT_SIMPLE);
            case DIFF_EXPR_ANAT_COMPLETE:
                return log.traceExit(DownloadFileTO.CategoryEnum.DIFF_EXPR_ANAT_COMPLETE);
            case DIFF_EXPR_DEV_COMPLETE:
                return log.traceExit(DownloadFileTO.CategoryEnum.DIFF_EXPR_DEV_COMPLETE);
            case DIFF_EXPR_DEV_SIMPLE:
                return log.traceExit(DownloadFileTO.CategoryEnum.DIFF_EXPR_DEV_SIMPLE);
            case ORTHOLOG:
                return log.traceExit(DownloadFileTO.CategoryEnum.ORTHOLOG);
            case AFFY_ANNOT:
                return log.traceExit(DownloadFileTO.CategoryEnum.AFFY_ANNOT);
            case AFFY_DATA:
                return log.traceExit(DownloadFileTO.CategoryEnum.AFFY_DATA);
            case RNASEQ_ANNOT:
                return log.traceExit(DownloadFileTO.CategoryEnum.RNASEQ_ANNOT);
            case RNASEQ_DATA:
                return log.traceExit(DownloadFileTO.CategoryEnum.RNASEQ_DATA);
            case FULL_LENGTH_ANNOT:
                return log.traceExit(DownloadFileTO.CategoryEnum.FULL_LENGTH_ANNOT);
            case FULL_LENGTH_DATA:
                return log.traceExit(DownloadFileTO.CategoryEnum.FULL_LENGTH_DATA);
            case FULL_LENGTH_H5AD:
                return log.traceExit(DownloadFileTO.CategoryEnum.FULL_LENGTH_H5AD);
            case DROPLET_BASED_ANNOT:
                return log.traceExit(DownloadFileTO.CategoryEnum.DROPLET_BASED_ANNOT);
            case DROPLET_BASED_DATA:
                return log.traceExit(DownloadFileTO.CategoryEnum.DROPLET_BASED_DATA);
            case DROPLET_BASED_H5AD:
                return log.traceExit(DownloadFileTO.CategoryEnum.DROPLET_BASED_H5AD);
            default:
                throw log.throwing(new IllegalArgumentException("Category not supported: " + serviceEnum));
        }
    }

    private static CallService.Attribute mapDAOCondParamToServiceCondParam(
            ConditionDAO.Attribute daoEnum) {
        log.traceEntry("{}", daoEnum);

        switch (daoEnum) {
            case ANAT_ENTITY_ID:
                return log.traceExit(CallService.Attribute.ANAT_ENTITY_ID);
            case STAGE_ID:
                return log.traceExit(CallService.Attribute.DEV_STAGE_ID);
            case CELL_TYPE_ID:
                return log.traceExit(CallService.Attribute.CELL_TYPE_ID);
            case SEX_ID:
                return log.traceExit(CallService.Attribute.SEX_ID);
            case STRAIN_ID:
                return log.traceExit(CallService.Attribute.STRAIN_ID);
            default:
                throw log.throwing(new IllegalArgumentException("Condition parameter not supported: " + daoEnum));
        }
    }
}
