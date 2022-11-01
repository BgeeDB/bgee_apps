package org.bgee.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.InvalidRequestException;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.Sex;
import org.bgee.model.anatdev.Sex.SexEnum;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;
import org.bgee.view.DataDisplay;
import org.bgee.view.ViewFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

/**
 * Controller that handles requests for the raw data page.
 *
 * @author  Frederic Bastian
 * @version Bgee 15.0, Oct. 2022
 * @since   Bgee 15.0, Oct. 2022
 */
public class CommandData extends CommandParent {
    private final static Logger log = LogManager.getLogger(CommandData.class.getName());

    public static class DataFormDetails {
        private final Species requestedSpecies;
        private final Ontology<DevStage, String> requestedSpeciesDevStageOntology;
        private final List<Sex> requestedSpeciesSexes;
        private final List<AnatEntity> requestedAnatEntitesAndCellTypes;
        private final List<Gene> requestedGenes;

        public DataFormDetails(Species requestedSpecies,
                Ontology<DevStage, String> requestedSpeciesDevStageOntology,
                List<Sex> requestedSpeciesSexes, List<AnatEntity> requestedAnatEntitesAndCellTypes,
                List<Gene> requestedGenes) {

            this.requestedSpecies = requestedSpecies;
            this.requestedSpeciesDevStageOntology = requestedSpeciesDevStageOntology;
            this.requestedSpeciesSexes = Collections.unmodifiableList(requestedSpeciesSexes == null?
                    new ArrayList<>(): new ArrayList<>(requestedSpeciesSexes));
            this.requestedAnatEntitesAndCellTypes = Collections.unmodifiableList(
                    requestedAnatEntitesAndCellTypes == null?
                    new ArrayList<>(): new ArrayList<>(requestedAnatEntitesAndCellTypes));
            this.requestedGenes = Collections.unmodifiableList(requestedGenes == null?
                    new ArrayList<>(): new ArrayList<>(requestedGenes));
        }

        public Species getRequestedSpecies() {
            return requestedSpecies;
        }
        public Ontology<DevStage, String> getRequestedSpeciesDevStageOntology() {
            return requestedSpeciesDevStageOntology;
        }
        public List<Sex> getRequestedSpeciesSexes() {
            return requestedSpeciesSexes;
        }
        public List<AnatEntity> getRequestedAnatEntitesAndCellTypes() {
            return requestedAnatEntitesAndCellTypes;
        }
        public List<Gene> getRequestedGenes() {
            return requestedGenes;
        }

        public boolean containsAnyInformation() {
            return this.getRequestedSpeciesDevStageOntology() != null ||
                    !this.getRequestedSpeciesSexes().isEmpty() ||
                    !this.getRequestedAnatEntitesAndCellTypes().isEmpty() ||
                    !this.getRequestedGenes().isEmpty();
        }
    }

    private final SpeciesService speciesService;

    /**
     * Constructor
     *
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the 
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties to use.
     * @param viewFactory       A {@code ViewFactory} that provides the display type to be used.
     * @param serviceFactory    A {@code ServiceFactory} that provides the services to be used.
     */
    public CommandData(HttpServletResponse response, RequestParameters requestParameters,
                          BgeeProperties prop, ViewFactory viewFactory, ServiceFactory serviceFactory) {
        super(response, requestParameters, prop, viewFactory, serviceFactory);
        this.speciesService = this.serviceFactory.getSpeciesService();
    }

    @Override
    public void processRequest() throws Exception {
        log.traceEntry();

        List<Species> speciesList = null;
        DataFormDetails formDetails = null;

        if (this.requestParameters.isGetSpeciesList()) {
            speciesList = this.loadSpeciesList();
        }

        if (this.requestParameters.isDetailedRequestParameters()) {
            Ontology<DevStage, String> requestedSpeciesDevStageOntology = null;
            List<Sex> requestedSpeciesSexes = null;
            List<AnatEntity> requestedAnatEntitesAndCellTypes = null;
            List<Gene> requestedGenes = null;

            Species requestedSpecies = this.loadRequestedSpecies();
            List<String> requestedGeneIds = this.requestParameters.getGeneIds();

            if (requestedSpecies != null) {
                int speciesId = requestedSpecies.getId();
                requestedSpeciesDevStageOntology = this.loadSpeciesStageOntology(speciesId);
                requestedSpeciesSexes = this.loadSpeciesSexes(speciesId);
                requestedGenes = this.loadRequestedGenes(speciesId, requestedGeneIds);

            } else if (requestedGeneIds != null && !requestedGeneIds.isEmpty()) {
                throw log.throwing(new InvalidRequestException(
                        "A species ID must be provided to query genes"));
            }
            requestedAnatEntitesAndCellTypes = this.loadRequestedAnatEntitesAndCellTypes();

            formDetails = new DataFormDetails(requestedSpecies, requestedSpeciesDevStageOntology,
                    requestedSpeciesSexes, requestedAnatEntitesAndCellTypes, requestedGenes);
        }

        DataDisplay display = viewFactory.getDataDisplay();
        display.displayDataPage(speciesList, formDetails);

        log.traceExit();
    }

    private List<Species> loadSpeciesList() {
        log.traceEntry();
        return log.traceExit(this.speciesService.loadSpeciesByIds(null, false)
                .stream()
                .sorted(Comparator.comparing(Species::getPreferredDisplayOrder))
                .collect(Collectors.toList()));
    }

    private Species loadRequestedSpecies() throws InvalidRequestException {
        log.traceEntry();
        Integer requestedSpeciesId = this.requestParameters.getSpeciesId();
        if (requestedSpeciesId == null) {
            return log.traceExit((Species) null);
        }
        Species species = this.speciesService.loadSpeciesByIds(Set.of(requestedSpeciesId), false)
                .stream().findAny().orElse(null);
        if (species == null) {
            throw log.throwing(new InvalidRequestException("No species corresponding to ID "
                    + requestedSpeciesId));
        }
        return log.traceExit(species);
    }

    private Ontology<DevStage, String> loadSpeciesStageOntology(int speciesId) {
        log.traceEntry("{}", speciesId);

        Set<DevStage> stages = this.serviceFactory.getDevStageService()
                .loadGroupingDevStages(Set.of(speciesId), null);

        return log.traceExit(this.serviceFactory.getOntologyService()
                .getDevStageOntologyFromDevStages(Set.of(speciesId), stages, false, false)
                .getAsSingleSpeciesOntology(speciesId));
    }

    private List<Sex> loadSpeciesSexes(int speciesId) {
        log.traceEntry("{}", speciesId);

        return log.traceExit(this.serviceFactory.getSexService().loadSexesBySpeciesId(speciesId).stream()
                // We filter out the "any". Users will either:
                // * select no sex to retrieve all results, including "mixed" or "NA"
                // * select all sexes (male, female, hermaphrodite) to retrieve all defined information
                // => there will be no possibility to select "other" (for retrieving only annotations
                // such as "mixed" or "NA".
                .filter(sex -> !sex.getId().equalsIgnoreCase(SexEnum.ANY.getStringRepresentation()))
                //Sort by their EnumSex representation for consistent ordering
                .sorted((s1, s2) -> SexEnum.convertToSexEnum(s1.getId()).compareTo(SexEnum.convertToSexEnum(s2.getId())))
                .collect(Collectors.toList()));
    }

    private List<AnatEntity> loadRequestedAnatEntitesAndCellTypes() throws InvalidRequestException {
        log.traceEntry();

        Set<String> anatEntityAndCellTypeIds = new HashSet<>();
        if (this.requestParameters.getAnatEntity() != null) {
            anatEntityAndCellTypeIds.addAll(this.requestParameters.getAnatEntity());
        }
        if (this.requestParameters.getCellType() != null) {
            anatEntityAndCellTypeIds.addAll(this.requestParameters.getCellType());
        }
        if (anatEntityAndCellTypeIds.isEmpty()) {
            return log.traceExit((List<AnatEntity>) null);
        }

        List<AnatEntity> anatEntities = this.serviceFactory.getAnatEntityService()
                .loadAnatEntities(anatEntityAndCellTypeIds, false)
                .sorted(Comparator.comparing(ae -> ae.getName()))
                .collect(Collectors.toList());
        if (anatEntities.size() != anatEntityAndCellTypeIds.size()) {
            Set<String> retrievedIds = anatEntities.stream()
                    .map(ae -> ae.getId())
                    .collect(Collectors.toSet());
            anatEntityAndCellTypeIds.removeAll(retrievedIds);
            throw log.throwing(new InvalidRequestException(
                    "Some anatomical entities or cell types could not be identified: "
                    + anatEntityAndCellTypeIds));
        }

        return log.traceExit(anatEntities);
    }

    private List<Gene> loadRequestedGenes(int speciesId, Collection<String> requestedGeneIds) throws InvalidRequestException {
        log.traceEntry();
        if (requestedGeneIds == null || requestedGeneIds.isEmpty()) {
            return log.traceExit((List<Gene>) null);
        }
        Set<String> clonedGeneIds = new HashSet<>(requestedGeneIds);

        GeneFilter filter = new GeneFilter(speciesId, clonedGeneIds);
        List<Gene> genes = this.serviceFactory.getGeneService().loadGenes(filter)
                .sorted(Comparator.<Gene, String>comparing(g -> g.getName())
                        .thenComparing(Comparator.comparing(g -> g.getGeneId())))
                .collect(Collectors.toList());

        if (genes.size() != clonedGeneIds.size()) {
            Set<String> retrieveGeneIds = genes.stream().map(g -> g.getGeneId())
                    .collect(Collectors.toSet());
            clonedGeneIds.removeAll(retrieveGeneIds);
            throw log.throwing(new InvalidRequestException(
                    "Some genes could not be identified: " + clonedGeneIds));
        }
        return log.traceExit(genes);
    }
}
