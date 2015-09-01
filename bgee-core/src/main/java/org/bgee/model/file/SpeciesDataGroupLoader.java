package org.bgee.model.file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesLoader;

import java.util.*;

/**
 * This is the loader for {@link SpeciesDataGroup}.
 *
 * @author Philippe Moret
 */
public class SpeciesDataGroupLoader {

    private final static Logger log = LogManager.getLogger(SpeciesDataGroupLoader.class.getName());

    private SpeciesDataGroupDAO speciesGroupDAO;

    private DownloadFileLoader downloadFileLoader;

    private SpeciesLoader speciesLoader;

    /**
     * Load all {@code SpeciesDataGroup}
     * @return the {@code Set} containing all {@code SpeciesDataGroup}
     */
    public Set<SpeciesDataGroup> loadAllSpeciesDataGroup() {
        log.entry();
        
        DAOResultSet<SpeciesDataGroupDAO.SpeciesDataGroupTO> speciesGroups = speciesGroupDAO.getAllSpeciesDataGroup();
        Map<String, List<DownloadFile>> downloadFiles = buildDownloadFileMap(downloadFileLoader.getAllDownloadFiles());
        Map<String, Species> species = buildSpeciesIdMap(speciesLoader.loadSpeciesInDataGroups());
        Map<String, List<Species>> groupToSpeciesMap = buildGroupToSpeciesMap(
                speciesGroupDAO.getAllSpeciesToDataGroup(), species);

        Set<SpeciesDataGroup> result = new HashSet<SpeciesDataGroup>();

        SpeciesDataGroupDAO.SpeciesDataGroupTO to;
        while (speciesGroups.next()) {
            to = speciesGroups.getTO();
            result.add(newSpeciesDataGroup(to,
                    groupToSpeciesMap.get(to.getId()), downloadFiles.get(to.getId())));
        }

        return log.exit(result);
    }

    /**
     * Build a map from datagroup to species
     * @param list the list of {@code SpeciesToDataGroupMemberTO}
     * @param speciesMap the map from data group id to {@code Species}
     * @return
     */
    private static Map<String, List<Species>> buildGroupToSpeciesMap(
            DAOResultSet<SpeciesDataGroupDAO.SpeciesToDataGroupTO> speciesToDataGroupRs, Map<String, Species> speciesMap) {
        log.entry(speciesToDataGroupRs, speciesMap);
        
        Map<String, List<Species>> result = new HashMap<>();

        while(speciesToDataGroupRs.next()) {
            SpeciesDataGroupDAO.SpeciesToDataGroupTO e = speciesToDataGroupRs.getTO();
            String group = e.getGroupId();
            Species species = speciesMap.get(e.getSpeciesId());
            List<Species> members = result.get(group);
            if (members == null) {
                members = new ArrayList<>();
                result.put(group, members);
            }
            members.add(species);
        }
        return log.exit(result);
    }

    /**
     * Builds a map from speciesId (as {@code String} to {@link Species} from a collection of {@code Species}
     * @param species the source collection of {@code Species}
     * @return the map
     */
    private static Map<String, Species> buildSpeciesIdMap(Collection<Species> species) {
        log.entry(species);
        Map<String, Species> speciesMap = new HashMap<>();
        for (Species s : species) {
            speciesMap.put(s.getId(), s);
        }
        return log.exit(speciesMap);
    }

    /**
     * Build a map from species data group ids (as {@code String} to the {@code List} of {@code DownloadFile} 
     * available for that data group.
     * 
     * @param downloadFileList the source list of {@code DownloadFile}
     * @return the map
     */
    private static Map<String, List<DownloadFile>> buildDownloadFileMap(List<DownloadFile> downloadFileList) {
        log.entry(downloadFileList);
        Map<String, List<DownloadFile>> downloadFiles = new HashMap<>();
        for (DownloadFile file : downloadFileList) {
            String group = file.getSpeciesDataGroupId();
            List<DownloadFile> list = downloadFiles.get(group);
            if (list == null) {
                list = new ArrayList<>();
                downloadFiles.put(group, list);
            }
            list.add(file);
        }
        return log.exit(downloadFiles);
    }

    /**
     * Helper method to build a {@link SpeciesDataGroup} from a {@code SpeciesDataGroupTO}
     * and the the {@code List} of associated {@code Species} and {@code DownloadFile}
     * @param groupTO the {@code SpeciesDataGroupTO}
     * @param species the {@code List} of associated {@code Species}
     * @param files the {@code List} of associated {@code DownloadFile}
     * @return a (newly allocated) {@code SpeciesDataGroup}
     */
    private static SpeciesDataGroup newSpeciesDataGroup(SpeciesDataGroupDAO.SpeciesDataGroupTO groupTO, 
            List<Species> species, List<DownloadFile> files) {
        log.entry(groupTO, species, files);
        return log.exit(newSpeciesDataGroup(groupTO.getId(), groupTO.getName(), groupTO.getDescription(), 
                species, files));
    }

    /**
     * Helper method to build a {@link SpeciesDataGroup} from its id, name and description
     * and the the {@code List} of associated {@code Species} and {@code DownloadFile}. 
     * 
     * @param id the id of the {@code SpeciesDataGroup}
     * @param name the name of the {@code SpeciesDataGroup}
     * @param description the description of the {@code SpeciesDataGroup}
     * @param species the {@code List} of associated {@code Species}
     * @param files the {@code List} of associated {@code DownloadFile}
     * @return a (newly allocated) {@code SpeciesDataGroup}
     */
    private static SpeciesDataGroup newSpeciesDataGroup(String id, String name, String description, List<Species>
            species, List<DownloadFile> files) {
        log.entry(id, name, description, species, files);
        return log.exit(new SpeciesDataGroup(id, name, description, species, files));
    }


}
