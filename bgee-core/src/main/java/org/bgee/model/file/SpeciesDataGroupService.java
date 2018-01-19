package org.bgee.model.file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Entity;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.exception.QueryInterruptedException;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO.SpeciesToDataGroupTOResultSet;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO.SpeciesToGroupOrderingAttribute;
import org.bgee.model.species.Species;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A {@link Service} to obtain {@link SpeciesDataGroup} objects. 
 * Users should use the {@link org.bgee.model.ServiceFactory} to obtain {@code SpeciesDataGroupService}s.
 *
 * @author  Philippe Moret
 * @author  Frederic bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 13, July 2016
 * @since   Bgee 13
 */
public class SpeciesDataGroupService extends Service {

    private final static Logger log = LogManager.getLogger(SpeciesDataGroupService.class.getName());

    /**
     * @param serviceFactory            The {@code ServiceFactory} to be used to obtain {@code Service}s 
     *                                  and {@code DAOManager}.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    public SpeciesDataGroupService(ServiceFactory serviceFactory) {
        super(serviceFactory);
    }

    /**
     * Loads all {@code SpeciesDataGroup}s. {@code SpeciesDataGroup}s are returned 
     * in preferred order for display, and member {@code Species} are ordered 
     * based on their taxonomic distance to the human lineage (from species closest to human, 
     * to farthest from human, e.g.: human, chimpanzee, mouse, zebrafish).
     * 
     * @return  A {@code List} containing all {@code SpeciesDataGroup}s, in order of preference, 
     *          with member {@code Species} ordered based on their taxonomic distance to human. 
     * @throws DAOException                 If an error occurred while accessing a {@code DAO}.
     * @throws QueryInterruptedException    If a query to a {@code DAO} was intentionally interrupted.
     * @throws IllegalStateException        If the {@code DownloadFileService} and {@code SpeciesService} 
     *                                      obtained at instantiation do not return consistent information 
     *                                      related to {@code SpeciesDataGroup}s.
     */ 
    public List<SpeciesDataGroup> loadAllSpeciesDataGroup() 
            throws DAOException, QueryInterruptedException, IllegalStateException {
        log.entry();
        
        final Map<Integer, Set<DownloadFile>> groupIdToDownloadFilesMap = 
                buildDownloadFileMap(this.getServiceFactory().getDownloadFileService().getAllDownloadFiles());
        
        LinkedHashMap<SpeciesToGroupOrderingAttribute, DAO.Direction> orderAttrs = new LinkedHashMap<>();
        orderAttrs.put(SpeciesToGroupOrderingAttribute.DATA_GROUP_ID, DAO.Direction.ASC);
        orderAttrs.put(SpeciesToGroupOrderingAttribute.DISTANCE_TO_SPECIES, DAO.Direction.ASC);
        final Map<Integer, List<Species>> groupIdToSpeciesMap = buildSpeciesMap(
                getDaoManager().getSpeciesDataGroupDAO().getAllSpeciesToDataGroup(orderAttrs), 
                this.getServiceFactory().getSpeciesService().loadSpeciesInDataGroups(false));
        
        if (groupIdToSpeciesMap.size() != groupIdToDownloadFilesMap.size()) {
            throw log.throwing(new IllegalStateException("The number of data groups "
                    + "associated to download files is different from the number of groups "
                    + "associated to species."));
        }
        
        LinkedHashMap<SpeciesDataGroupDAO.OrderingAttribute, DAO.Direction> orderAttrs2 = new LinkedHashMap<>();
        orderAttrs2.put(SpeciesDataGroupDAO.OrderingAttribute.PREFERRED_ORDER, DAO.Direction.ASC);
        return log.exit(getDaoManager().getSpeciesDataGroupDAO()
                .getAllSpeciesDataGroup(null, orderAttrs2).stream()
                .map(e -> newSpeciesDataGroup(e, groupIdToSpeciesMap.get(e.getId()), 
                        groupIdToDownloadFilesMap.get(e.getId())))
                 .collect(Collectors.toList()));
    }

    /**
     * Build a map from data group IDs to the species they contain, in preferred order.
     * 
     * @param speciesToDataGroupRs  A {@code SpeciesToDataGroupTOResultSet} to acquire 
     *                              {@code SpeciesToDataGroupMemberTO}s from, in preferred order.
     * @param species               A {@code Collection} containing all {@code Species} part of 
     *                              a data group.
     * @return                      A {@code Map} where keys are {@code String}s corresponding to 
     *                              data group IDs, associated to the {@code List} of {@code Species} 
     *                              they contain, in preferred order, as value. 
     */
    private static Map<Integer, List<Species>> buildSpeciesMap(
            SpeciesToDataGroupTOResultSet speciesToDataGroupRs, Collection<Species> species) {
        log.entry(speciesToDataGroupRs, species);
        
        final Map<Integer, Species> speciesMap = species.stream()
                .collect(Collectors.toMap(Entity::getId, Function.identity()));
        
        return log.exit(speciesToDataGroupRs.stream().collect(Collectors.groupingBy(
                //group the SpeciesToDataGroupTOs by groupId
                SpeciesDataGroupDAO.SpeciesToDataGroupTO::getGroupId, 
                //make the value associated to a groupId to be the all the Species associated to 
                //this groupId (species retrieved from speciesMap, using the speciesId field 
                //of the SpeciesToDataGroupTOs)
                Collectors.mapping(e -> speciesMap.get(e.getSpeciesId()), Collectors.toList()))));
    }

    /**
     * Build a map from species data group IDs to the {@code DownloadFile}s they contain, 
     * in preferred order. 
     * 
     * @param downloadFiles A {@code Collection} containing the {@code DownloadFile}s  
     *                      part of a data group.
     * @return              A {@code Map} where keys are {@code String}s corresponding to data group IDs, 
     *                      associated to the {@code Set} of {@code DownloadFile} they contain as value. 
     */
    private static Map<Integer, Set<DownloadFile>> buildDownloadFileMap(
            Collection<DownloadFile> downloadFiles) {
        log.entry(downloadFiles);
        return log.exit(downloadFiles.stream()
                .collect(Collectors.groupingBy(DownloadFile::getSpeciesDataGroupId, Collectors.toSet())));
    }

    /**
     * Helper method to build a {@link SpeciesDataGroup} from a {@code SpeciesDataGroupTO}
     * and the the {@code List} of associated {@code Species} and {@code DownloadFile}
     * @param groupTO   the {@code SpeciesDataGroupTO}
     * @param species   the {@code List} of associated {@code Species}
     * @param files     the {@code Set} of associated {@code DownloadFile}
     * @return          a (newly allocated) {@code SpeciesDataGroup}
     */
    private static SpeciesDataGroup newSpeciesDataGroup(SpeciesDataGroupDAO.SpeciesDataGroupTO groupTO, 
            List<Species> species, Set<DownloadFile> files) {
        log.entry(groupTO, species, files);
        return log.exit(newSpeciesDataGroup(groupTO.getId(), groupTO.getName(), groupTO.getDescription(), 
                species, files));
    }

    /**
     * Helper method to build a {@link SpeciesDataGroup} from its id, name and description
     * and associated {@code Species} and {@code DownloadFile}s. 
     * 
     * @param id            the id of the {@code SpeciesDataGroup}
     * @param name          the name of the {@code SpeciesDataGroup}
     * @param description   the description of the {@code SpeciesDataGroup}
     * @param species       the {@code List} of associated {@code Species}
     * @param files         the {@code Set} of associated {@code DownloadFile}s
     * @return a (newly allocated) {@code SpeciesDataGroup}
     */
    private static SpeciesDataGroup newSpeciesDataGroup(Integer id, String name, String description, 
            List<Species> species, Set<DownloadFile> files) {
        log.entry(id, name, description, species, files);
        return log.exit(new SpeciesDataGroup(id, name, description, species, files));
    }


}
