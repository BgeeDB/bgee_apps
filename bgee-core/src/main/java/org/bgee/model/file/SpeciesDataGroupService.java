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
     * Delegates to {@link #loadAllSpeciesDataGroup(boolean, boolean)} with the two {@code boolean}s
     * set to {@code true}.
     * 
     * @return  A {@code List} containing all {@code SpeciesDataGroup}s, in order of preference, 
     *          with member {@code Species} in order of preference, with all download files populated. 
     * @throws DAOException                 If an error occurred while accessing a {@code DAO}.
     * @throws QueryInterruptedException    If a query to a {@code DAO} was intentionally interrupted.
     * @throws IllegalStateException        If the {@code DownloadFileService} and {@code SpeciesService} 
     *                                      obtained at instantiation do not return consistent information 
     *                                      related to {@code SpeciesDataGroup}s.
     */ 
    public List<SpeciesDataGroup> loadAllSpeciesDataGroup() 
            throws DAOException, QueryInterruptedException, IllegalStateException {
        log.traceEntry();
        return log.traceExit(this.loadSpeciesDataGroup(true, true));
    }
    /**
     * Loads all {@code SpeciesDataGroup}s. {@code SpeciesDataGroup}s are returned in preferred order for display,
     * and member {@code Species} are ordered based on the preferred display order.
     * {@code withFilesRelatedToExprCalls} and {@code withFilesRelatedToProcessedExprValues}
     * cannot be both {@code false}, otherwise an {@code IllegalArgumentException} is thrown.
     *
     * @param withFilesRelatedToExprCalls              A {@code boolean} defining if information about files
     *                                                 related to expression calls should be populated in the returned
     *                                                 {@code SpeciesDataGroup}s.
     * @param withFilesRelatedToProcessedExprValues    A {@code boolean} defining if information about files
     *                                                 related to processed expression values should be populated
     *                                                 in the returned {@code SpeciesDataGroup}s.
     * @return                                         A {@code List} containing all {@code SpeciesDataGroup}s,
     *                                                 in order of preference, with member {@code Species}
     *                                                 in order of preference.
     * @throws DAOException                    If an error occurred while accessing a {@code DAO}.
     * @throws IllegalArgumentException        If both {@code withFilesRelatedToExprCalls} and
     *                                         {@code withFilesRelatedToProcessedExprValues} are {@code false}.
     * @throws IllegalStateException           If the {@code DownloadFileService} and {@code SpeciesService}
     *                                         obtained at instantiation do not return consistent information
     *                                         related to {@code SpeciesDataGroup}s.
     * @throws QueryInterruptedException       If a query to a {@code DAO} was intentionally interrupted.
     */
    //TODO: allow to retrieve either single or multiple species data groups
    //TODO: allow to provide a species ID list
    public List<SpeciesDataGroup> loadSpeciesDataGroup(boolean withFilesRelatedToExprCalls,
            boolean withFilesRelatedToProcessedExprValues) throws DAOException, IllegalArgumentException,
    IllegalStateException, QueryInterruptedException {
        log.traceEntry("{}, {}", withFilesRelatedToExprCalls, withFilesRelatedToProcessedExprValues);

        if (!withFilesRelatedToExprCalls && !withFilesRelatedToProcessedExprValues) {
            throw log.throwing(new IllegalArgumentException("At least one type of download file must be requested"));
        }
        final Map<Integer, Set<SpeciesDownloadFile>> groupIdToDownloadFilesMap = buildDownloadFileMap(
                this.getServiceFactory().getDownloadFileService().getDownloadFiles(
                        EnumSet.allOf(SpeciesDownloadFile.Category.class).stream()
                                .filter(c -> withFilesRelatedToExprCalls && c.isRelatedToExprCallFile() ||
                                    withFilesRelatedToProcessedExprValues && c.isRelatedToProcessedExprValueFile())
                                .collect(Collectors.toCollection(() -> EnumSet.noneOf(SpeciesDownloadFile.Category.class)))));
        
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
        return log.traceExit(getDaoManager().getSpeciesDataGroupDAO()
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
        log.traceEntry("{}, {}", speciesToDataGroupRs, species);
        
        final Map<Integer, Species> speciesMap = species.stream()
                .collect(Collectors.toMap(Entity::getId, Function.identity()));
        
        return log.traceExit(speciesToDataGroupRs.stream().collect(Collectors.groupingBy(
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
    private static Map<Integer, Set<SpeciesDownloadFile>> buildDownloadFileMap(
            Collection<SpeciesDownloadFile> downloadFiles) {
        log.traceEntry("{}", downloadFiles);
        return log.traceExit(downloadFiles.stream()
                .collect(Collectors.groupingBy(SpeciesDownloadFile::getSpeciesDataGroupId, Collectors.toSet())));
    }

    /**
     * Helper method to build a {@link SpeciesDataGroup} from a {@code SpeciesDataGroupTO}
     * and the the {@code List} of associated {@code Species} and {@code DownloadFile}
     * @param groupTO   the {@code SpeciesDataGroupTO}
     * @param species   the {@code List} of associated {@code Species}
     * @param files     the {@code Set} of associated {@code SpeciesDownloadFile}
     * @return          a (newly allocated) {@code SpeciesDataGroup}
     */
    private static SpeciesDataGroup newSpeciesDataGroup(SpeciesDataGroupDAO.SpeciesDataGroupTO groupTO, 
            List<Species> species, Set<SpeciesDownloadFile> files) {
        log.traceEntry("{}, {}, {}", groupTO, species, files);
        return log.traceExit(newSpeciesDataGroup(groupTO.getId(), groupTO.getName(), groupTO.getDescription(), 
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
     * @param files         the {@code Set} of associated {@code SpeciesDownloadFile}s
     * @return a (newly allocated) {@code SpeciesDataGroup}
     */
    private static SpeciesDataGroup newSpeciesDataGroup(Integer id, String name, String description, 
            List<Species> species, Set<SpeciesDownloadFile> files) {
        log.traceEntry("{}, {}, {}, {}, {}", id, name, description, species, files);
//        files = new HashSet<DownloadFile>();
//        files.add(new DownloadFile("path", "my_name", DownloadFile.Category.AFFY_ANNOT, 100L, id));
        return log.traceExit(new SpeciesDataGroup(id, name, description, species, files));
    }


}
