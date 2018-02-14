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

import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@link Service} to obtain {@link DownloadFile}s. 
 * Users should use the {@link org.bgee.model.ServiceFactory} to obtain {@code DownloadFileService}s.
 *
 * @author Philippe Moret
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
     * Gets all available {@code DownloadFile}.
     *
     * @return a {@code List} of {@code DownloadFile}
     * @throws DAOException                 If an error occurred while accessing a {@code DAO}.
     * @throws QueryInterruptedException    If a query to a {@code DAO} was intentionally interrupted.
     */
    public List<DownloadFile> getAllDownloadFiles() throws DAOException, QueryInterruptedException {
        log.entry();
        return log.exit(getDaoManager().getDownloadFileDAO().getAllDownloadFiles().stream()
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
        log.entry(downloadFileTO);
        if (downloadFileTO == null) {
            return log.exit(null);
        }
        return log.exit(new DownloadFile(downloadFileTO.getPath(),
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
        log.entry(daoEnum);

        switch (daoEnum) {
            case EXPR_CALLS_COMPLETE:
                return log.exit(DownloadFile.CategoryEnum.EXPR_CALLS_COMPLETE);
            case EXPR_CALLS_SIMPLE:
                return log.exit(DownloadFile.CategoryEnum.EXPR_CALLS_SIMPLE);
            case DIFF_EXPR_ANAT_SIMPLE:
                return log.exit(DownloadFile.CategoryEnum.DIFF_EXPR_ANAT_SIMPLE);
            case DIFF_EXPR_ANAT_COMPLETE:
                return log.exit(DownloadFile.CategoryEnum.DIFF_EXPR_ANAT_COMPLETE);
            case DIFF_EXPR_DEV_COMPLETE:
                return log.exit(DownloadFile.CategoryEnum.DIFF_EXPR_DEV_COMPLETE);
            case DIFF_EXPR_DEV_SIMPLE:
                return log.exit(DownloadFile.CategoryEnum.DIFF_EXPR_DEV_SIMPLE);
            case ORTHOLOG:
                return log.exit(DownloadFile.CategoryEnum.ORTHOLOG);
            case AFFY_ANNOT:
                return log.exit(DownloadFile.CategoryEnum.AFFY_ANNOT);
            case AFFY_DATA:
                return log.exit(DownloadFile.CategoryEnum.AFFY_DATA);
            case RNASEQ_ANNOT:
                return log.exit(DownloadFile.CategoryEnum.RNASEQ_ANNOT);
            case RNASEQ_DATA:
                return log.exit(DownloadFile.CategoryEnum.RNASEQ_DATA);
            default:
                throw log.throwing(new IllegalArgumentException("Category not supported: " + daoEnum));
        }
    }

    private static CallService.Attribute mapDAOCondParamToServiceCondParam(
            ConditionDAO.Attribute daoEnum) {
        log.entry(daoEnum);

        switch (daoEnum) {
            case ANAT_ENTITY_ID:
                return log.exit(CallService.Attribute.ANAT_ENTITY_ID);
            case STAGE_ID:
                return log.exit(CallService.Attribute.DEV_STAGE_ID);
            default:
                throw log.throwing(new IllegalArgumentException("Condition parameter not supported: " + daoEnum));
        }
    }
}
