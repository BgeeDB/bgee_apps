package org.bgee.model.keyword;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.keyword.KeywordDAO;
import org.bgee.model.dao.api.keyword.KeywordDAO.EntityToKeywordTO;
import org.bgee.model.species.SpeciesService;
/**
 * The service for accessing keywords.
 * 
 * @author Philippe Moret
 * @since Bgee 13
 */
public class KeywordService extends Service {

	/** This class' logger */
    private final static Logger log = LogManager.getLogger(KeywordService.class.getName());

	/** The {@code DAOManager} for this service */
	private final DAOManager daoManager;

	/** The {@code SpeciesService} for this service */
	private final SpeciesService speciesService;
	
	/**
	 * Constructs a {@code KeywordService}.
	 * @param manager The {@code DAOManager} to be used by this service.
	 */
	public KeywordService(DAOManager manager, SpeciesService speciesService) {
		this.daoManager = manager;
		this.speciesService = speciesService;
	}
	
	/**
	 * Constructs a {@code KeywordService}, attempts to get a {@code DAOManager} 
	 * through {@link DAOManager#getDAOManager()}.
	 */
	@SuppressWarnings("unused")
	public KeywordService() {
		this(DAOManager.getDAOManager(), null);
	}
	
	/**
	 * @return A {@code Map} of species Id to the {@code Set} of associated keywords
	 */
	public Map<String, Set<String>> getKeywordForAllSpecies() {
		log.entry();
		return log.exit(getKeywordForSpecies(speciesService.loadAllSpeciesId()));
	}
	/**
	 * Gets a {@code Map} of keywords for a given set of species
	 * @param  speciesIds A {@code Collection} of species id for which to return the keywords.
	 * @return A {@code Map} of species Id to the {@code Set} of associated keywords
	 */
	public Map<String, Set<String>> getKeywordForSpecies(Collection<String> speciesIds) {
		log.entry();
		KeywordDAO dao = daoManager.getKeywordDAO();
        Map<String, String> keywordMap = dao.getKeywordsRelatedToSpecies(speciesIds).stream()
		                                    .collect(Collectors.toMap(EntityTO::getId, KeywordDAO.KeywordTO::getName));
		
		DAOResultSet<EntityToKeywordTO> results = dao.getKeywordToSpecies(speciesIds);
		
		return log.exit(results.stream().collect(groupingBy(
				EntityToKeywordTO::getEntityId,
				mapping(t -> keywordMap.get(t.getKeywordId()), toSet()))));
	
	}


}
