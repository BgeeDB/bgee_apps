package org.bgee.controller;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Entity;
import org.bgee.model.ServiceFactory;
import org.bgee.model.file.SpeciesDataGroup;
import org.bgee.model.species.Species;
import org.bgee.view.SpeciesDisplay;
import org.bgee.view.ViewFactory;

/**
 * Controller handling requests relative to species.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 14, July 2019
 * @since   Bgee 13, Nov 2015
 */
public class CommandSpecies extends CommandParent {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(CommandSpecies.class.getName());
    
    /**
     * Constructor providing necessary dependencies. 
     * 
     * @param response          A {@code HttpServletResponse} that will be used 
     *                          to display the page to the client
     * @param requestParameters The {@code RequestParameters} that handles 
     *                          the parameters of the current request.
     * @param prop              A {@code BgeeProperties} instance that contains 
     *                          the properties to use.
     * @param viewFactory       A {@code ViewFactory} providing the views of the appropriate 
     *                          display type.
     * @param serviceFactory    A {@code ServiceFactory} that provides bgee services.
     */
    public CommandSpecies(HttpServletResponse response, RequestParameters requestParameters,
            BgeeProperties prop, ViewFactory viewFactory, ServiceFactory serviceFactory) {
        super(response, requestParameters, prop, viewFactory, serviceFactory);
    }

    @Override
    public void processRequest() throws Exception {
        log.traceEntry();
        
        SpeciesDisplay display = this.viewFactory.getSpeciesDisplay();
        
        // Get submitted species ID
        Integer speciesId = this.requestParameters.getSpeciesId();
        if (speciesId != null) {
            final Set<Species> speciesSet = this.serviceFactory.getSpeciesService()
                    .loadSpeciesByIds(Collections.singleton(speciesId), true);
            if (speciesSet.isEmpty()) {
                throw log.throwing(new IllegalStateException(
                        "A SpeciesService did not allow to obtain any Species."));
            }
            assert speciesSet.size() == 1;
            
            Species sp = speciesSet.iterator().next();
            
            List<SpeciesDataGroup> groups = serviceFactory.getSpeciesDataGroupService().loadAllSpeciesDataGroup();
            if (groups.isEmpty()) {
                throw log.throwing(new IllegalStateException("A SpeciesDataGroupService did not allow "
                        + "to obtain any SpeciesDataGroup."));
            }

            Set<SpeciesDataGroup> speciesDataGroups = groups.stream()
                    .filter(g -> g.isSingleSpecies() && g.getMembers().contains(sp))
                    .collect(Collectors.toSet());
            assert speciesDataGroups.size() == 1;

            display.displaySpecies(sp, speciesDataGroups.iterator().next());
            
            log.traceExit(); return;
        }

        // Get submitted species IDs
        Collection<Integer> submittedSpeciesIds = this.requestParameters.getSpeciesList();

        if (submittedSpeciesIds != null && !submittedSpeciesIds.isEmpty()) {
            // Load detected species
            Set<Species> species = this.serviceFactory.getSpeciesService().
                    loadSpeciesByIds(submittedSpeciesIds, true);
            if (species.isEmpty()) {
                throw log.throwing(new IllegalStateException(
                        "A SpeciesService did not allow to obtain any Species."));
            }
            display.sendSpeciesResponse(
                    species.stream()
                            //filter species with no data (insertion error in Bgee 13 database)
                            .filter(s -> !s.getDataTypesByDataSourcesForData().isEmpty())
                            //order species by ID for consistent rendering
                            //XXX: should we add ordering attributes to SpeciesService?
                            .sorted(Comparator.comparing(Entity::getId))
                            .collect(Collectors.toList())
            );
            log.traceExit(); return;
        }

        Set<Species> species = this.serviceFactory.getSpeciesService().loadSpeciesByIds(null, false);
        display.displaySpeciesHomePage(species.stream()
                .sorted(Comparator.comparing(Species::getPreferredDisplayOrder))
                .collect(Collectors.toList()));

        log.traceExit();
    }
}
