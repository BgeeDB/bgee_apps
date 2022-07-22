package org.bgee.model.file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.exception.QueryInterruptedException;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.file.DownloadFileDAO;
import org.bgee.model.dao.api.file.DownloadFileDAO.DownloadFileTO;
import org.bgee.model.expressiondata.CallService;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@link Service} to obtain {@link DownloadFile}s. 
 * Users should use the {@link org.bgee.model.ServiceFactory} to obtain {@code DownloadFileService}s.
 *
 * @author Philippe Moret
 * @author Frederic Bastian
 * @version Bgee 15, Oct. 2021
 */
public class DownloadFileService extends Service {

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
     * @param categories    A {@code Collection} of {@code DownloadFile.CategoryEnum}s specifying
     *                      the categories for which download files should be retrieved. If {@code null}
     *                      or empty, all download files are retrieved.
     * @return              a {@code List} of {@code DownloadFile}
     * @throws DAOException                 If an error occurred while accessing a {@code DAO}.
     * @throws QueryInterruptedException    If a query to a {@code DAO} was intentionally interrupted.
     */
    public List<DownloadFile> getDownloadFiles(Collection<DownloadFile.CategoryEnum> categories)
            throws DAOException, QueryInterruptedException {
        log.traceEntry("{}", categories);
        EnumSet<DownloadFileTO.CategoryEnum> cats = categories == null || categories.isEmpty()? null:
            categories.stream().map(cat -> convertServiceCategoryToDAOCategory(cat))
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(DownloadFileTO.CategoryEnum.class)));
        return log.traceExit(getDaoManager().getDownloadFileDAO().getDownloadFiles(cats).stream()
                .map(DownloadFileService::mapFromTO)
                .collect(Collectors.toList()));
    }


    /**
     * Maps {@link DownloadFileDAO.DownloadFileTO} to a {@link DownloadFile}.
     *
     * @param downloadFileTO The {@link DownloadFileDAO.DownloadFileTO} to map.
     * @return The mapped  {@link DownloadFile}.
     */
    private static DownloadFile mapFromTO(DownloadFileDAO.DownloadFileTO downloadFileTO) {
        log.traceEntry("{}", downloadFileTO);
        if (downloadFileTO == null) {
            return log.traceExit((DownloadFile) null);
        }
        return log.traceExit(new DownloadFile(downloadFileTO.getPath(),
                downloadFileTO.getName(),
                mapDAOCategoryToServiceCategory(downloadFileTO.getCategory()),
                downloadFileTO.getSize(),
                downloadFileTO.getSpeciesDataGroupId(),
                downloadFileTO.getConditionParameters().stream()
                    .map(p -> mapDAOCondParamToServiceCondParam(p))
                    .collect(Collectors.toSet())));
    }

    private static DownloadFile.CategoryEnum mapDAOCategoryToServiceCategory(
            DownloadFileTO.CategoryEnum daoEnum) {
        log.traceEntry("{}", daoEnum);

        switch (daoEnum) {
            case EXPR_CALLS_COMPLETE:
                return log.traceExit(DownloadFile.CategoryEnum.EXPR_CALLS_COMPLETE);
            case EXPR_CALLS_SIMPLE:
                return log.traceExit(DownloadFile.CategoryEnum.EXPR_CALLS_SIMPLE);
            case DIFF_EXPR_ANAT_SIMPLE:
                return log.traceExit(DownloadFile.CategoryEnum.DIFF_EXPR_ANAT_SIMPLE);
            case DIFF_EXPR_ANAT_COMPLETE:
                return log.traceExit(DownloadFile.CategoryEnum.DIFF_EXPR_ANAT_COMPLETE);
            case DIFF_EXPR_DEV_COMPLETE:
                return log.traceExit(DownloadFile.CategoryEnum.DIFF_EXPR_DEV_COMPLETE);
            case DIFF_EXPR_DEV_SIMPLE:
                return log.traceExit(DownloadFile.CategoryEnum.DIFF_EXPR_DEV_SIMPLE);
            case ORTHOLOG:
                return log.traceExit(DownloadFile.CategoryEnum.ORTHOLOG);
            case AFFY_ANNOT:
                return log.traceExit(DownloadFile.CategoryEnum.AFFY_ANNOT);
            case AFFY_DATA:
                return log.traceExit(DownloadFile.CategoryEnum.AFFY_DATA);
            case RNASEQ_ANNOT:
                return log.traceExit(DownloadFile.CategoryEnum.RNASEQ_ANNOT);
            case RNASEQ_DATA:
                return log.traceExit(DownloadFile.CategoryEnum.RNASEQ_DATA);
            case FULL_LENGTH_ANNOT:
                return log.traceExit(DownloadFile.CategoryEnum.FULL_LENGTH_ANNOT);
            case FULL_LENGTH_DATA:
                return log.traceExit(DownloadFile.CategoryEnum.FULL_LENGTH_DATA);
            default:
                throw log.throwing(new IllegalArgumentException("Category not supported: " + daoEnum));
        }
    }
    private static DownloadFileTO.CategoryEnum convertServiceCategoryToDAOCategory(
            DownloadFile.CategoryEnum serviceEnum) {
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
