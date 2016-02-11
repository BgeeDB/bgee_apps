package org.bgee.model.gene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Generated;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneNameSynonymDAO;
import org.bgee.model.dao.api.gene.GeneNameSynonymDAO.GeneNameSynonymTO;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;

import com.mysql.jdbc.StringUtils;

/**
 * A {@link Service} to obtain {@link Gene} objects. Users should use the
 * {@link ServiceFactory} to obtain {@code GeneService}s.
 * 
 * @author Philippe Moret
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 13 Nov. 2015
 * @since Bgee 13 Sept. 2015
 */
public class GeneService extends Service {

	private static final Logger log = LogManager.getLogger(GeneService.class.getName());

	/**
	 * The {@code SpeciesService} to obtain {@code Species} objects used in
	 * {@code SpeciesDataGroup}.
	 */
	private final SpeciesService speciesService;

	/**
	 * 0-arg constructor that will cause this {@code GeneService} to use the
	 * default {@code DAOManager} returned by {@link DAOManager#getDAOManager()}
	 * .
	 * 
	 * @see #GeneService(DAOManager)
	 */
	public GeneService() {
		this(DAOManager.getDAOManager());
	}

	/**
	 * @param daoManager
	 *            The {@code DAOManager} to be used by this {@code GeneService}
	 *            to obtain {@code DAO}s.
	 * @throws IllegalArgumentException
	 *             If {@code daoManager} is {@code null}.
	 */
	public GeneService(DAOManager daoManager) {
		this(daoManager, null);
	}

	public GeneService(DAOManager daoManager, SpeciesService speciesService) {
		super(daoManager);
		this.speciesService = speciesService;
	}

	/**
	 * Retrieve {@code Gene}s for a given set of species IDs and a given set of
	 * gene IDs.
	 * 
	 * @param geneIds
	 *            A {@code Collection} of {@code String}s that are IDs of genes
	 *            for which to return the {@code Gene}s.
	 * @param speciesIds
	 *            A {@code Collection} of {@code String}s that are IDs of
	 *            species for which to return the {@code Gene}s.
	 * @return A {@code List} of {@code Gene}s that are the {@code Gene}s for
	 *         the given set of species IDs and the given set of gene IDs.
	 */
	public List<Gene> loadGenesByIdsAndSpeciesIds(Collection<String> geneIds, Collection<String> speciesIds) {
		log.entry(geneIds, speciesIds);

		Set<String> filteredGeneIds = geneIds == null ? new HashSet<>() : new HashSet<>(geneIds);
		Set<String> filteredSpeciesIds = speciesIds == null ? new HashSet<>() : new HashSet<>(speciesIds);

		// XXX: shouldn't we return a Stream here?
		return log.exit(getDaoManager().getGeneDAO().getGenesBySpeciesIds(filteredSpeciesIds, filteredGeneIds).stream()
		        .map(GeneService::mapFromTO).collect(Collectors.toList()));
	}

	public Gene loadGeneById(String geneId) {
		log.entry(geneId);
		Set<String> geneIds = new HashSet<>();
		geneIds.add(geneId);
		Set<Gene> result = getDaoManager().getGeneDAO().getGenesByIds(geneIds).stream().map(GeneService::mapFromTO)
		        .collect(Collectors.toSet());

		// there should be exactly one result here.
		if (result == null || result.size() != 1) {
			throw log.throwing(new IllegalStateException("Shoud get 1 element here" + result));
		}
		Gene gene = result.iterator().next();
		Set<String> speciesIds = new HashSet<>();
		assert gene.getSpeciesId() != null;
		speciesIds.add(gene.getSpeciesId());
		Species species = speciesService.loadSpeciesByIds(speciesIds).iterator().next();
		gene.setSpecies(species);
		return log.exit(gene);
	}

	// TODO:implement
	public List<GeneMatch> searchByTerm(final String term) {
		log.entry(term);
		GeneDAO dao = getDaoManager().getGeneDAO();
		List<Gene> matchedGeneList = dao.getGeneBySearchTerm(term, null, 1, 25).stream().map(GeneService::mapFromTO)
		        .collect(Collectors.toList());
		
		Set<String> speciesIds = matchedGeneList.stream().map(Gene::getSpeciesId).collect(Collectors.toSet());
		Map<String, Species> speciesMap = speciesService.loadSpeciesByIds(speciesIds).stream()
				.collect(Collectors.toMap(Species::getId, Function.identity()));
		
		for (Gene g: matchedGeneList) {
			g.setSpecies(speciesMap.get(g.getSpeciesId()));
		}
		
		// seems that Java 8 doesn't support Functions with more than 1 argument.
		final Function<Gene, GeneMatch> f = new Function<Gene, GeneMatch>() {
			@Override
			public GeneMatch apply(Gene gene) {
				return geneMatch(gene, term);
			}
			
		};
		
		
		
		return log.exit(matchedGeneList.stream().map(f).collect(Collectors.toList()));
	}

	private GeneMatch geneMatch(final Gene gene, final String term) {
		log.entry(gene, term);
		// if the gene name or id match there is no synonym
		if (gene.getName().toLowerCase().contains(term.toLowerCase())
		        || gene.getId().toLowerCase().contains(term.toLowerCase())) {
			return log.exit(new GeneMatch(gene, null));
		}

		// otherwise we fetch synonym and find the first match
		List<GeneNameSynonymTO> synonyms = getDaoManager().getGeneNameSynonymDAO().getGeneNameSynonyms(gene.getId())
		        .stream().filter(s -> s.getGeneNameSynonym().toLowerCase().contains(term.toLowerCase()))
		        .collect(Collectors.toList());
		if (synonyms.size() < 1) {
			throw new IllegalStateException("The term should match either the gene id/name "
			        + "or one of its synonyms. Term: " + term + " Gene;" + gene);
		}
		return log.exit(new GeneMatch(gene, synonyms.get(0).getGeneNameSynonym()));
	}


	/**
	 * Maps {@link GeneTO} to a {@link Gene}.
	 * 
	 * @param geneTO
	 *            The {@link GeneTO} to map.
	 * @return The mapped {@link Gene}.
	 */
	private static Gene mapFromTO(GeneTO geneTO) {
		log.entry(geneTO);
		if (geneTO == null) {
			return log.exit(null);
		}

		return log.exit(new Gene(geneTO.getId(), String.valueOf(geneTO.getSpeciesId()), geneTO.getName(),
		        geneTO.getDescription()));
	}
}
