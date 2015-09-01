package org.bgee.model.file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.helper.DAOResultSetHelper;
import org.bgee.model.dao.api.file.DownloadFileDAO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The loader for {@link DownloadFile}
 *
 * @author Philippe Moret
 */
public class DownloadFileLoader {

    private static final Logger log = LogManager.getLogger(DownloadFileLoader.class.getName());

    private DownloadFileDAO downloadFileDAO;

    /**
     * Gets all available {@code DownloadFile}
     *
     * @return a {@code List} of {@code DownloadFile}
     */
    public List<DownloadFile> getAllDownloadFiles() {
        return  DAOResultSetHelper.mapToList(downloadFileDAO.getAllDownloadFiles(), DownloadFileLoader::mapFromTO);
    }


    /**
     * Maps {@link DownloadFileDAO.DownloadFileTO} to a {@link DownloadFile}.
     *
     * @param downloadFileTO The {@link DownloadFileDAO.DownloadFileTO} to map.
     * @return The mapped  {@link DownloadFile}.
     */
    public static DownloadFile mapFromTO(DownloadFileDAO.DownloadFileTO downloadFileTO) {
        if (downloadFileTO == null) {
            return null;
        }
        return new DownloadFile(downloadFileTO.getPath(),
                downloadFileTO.getName(),
                downloadFileTO.getCategory().getStringRepresentation(),
                downloadFileTO.getSize(),
                downloadFileTO.getSpeciesDataGroupId());
    }


}
