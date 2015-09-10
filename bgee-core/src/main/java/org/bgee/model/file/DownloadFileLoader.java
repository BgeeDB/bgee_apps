package org.bgee.model.file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
        log.entry();
        return log.exit(downloadFileDAO.getAllDownloadFiles().stream()
                .map(DownloadFileLoader::mapFromTO)
                .collect(Collectors.toList()));
    }


    /**
     * Maps {@link DownloadFileDAO.DownloadFileTO} to a {@link DownloadFile}.
     *
     * @param downloadFileTO The {@link DownloadFileDAO.DownloadFileTO} to map.
     * @return The mapped  {@link DownloadFile}.
     */
    public static DownloadFile mapFromTO(DownloadFileDAO.DownloadFileTO downloadFileTO) {
        log.entry(downloadFileTO);
        if (downloadFileTO == null) {
            return log.exit(null);
        }
        return log.exit(new DownloadFile(downloadFileTO.getPath(),
                downloadFileTO.getName(),
                downloadFileTO.getCategory().getStringRepresentation(),
                downloadFileTO.getSize(),
                downloadFileTO.getSpeciesDataGroupId()));
    }


}
