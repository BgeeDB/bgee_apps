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
	
	/**
	 * Constructs a {@code KeywordService}.
	 * @param manager The {@code DAOManager} to be used by this service.
	 */
	public KeywordService(DAOManager manager) {
		this.daoManager = manager;
	}
	
	/**
	 * Default constructor private on purpose, a {@code DAOManager} 
	 * and a {@code SpeciesService} must be provided. 
	 */
	//XXX: see notes about SpeciesDataGroupService#SpeciesDataGroupService(). 
	//Notably, if we call DAOManager#getDAOManager() here, we might not be using 
	//the same DAOManager as the ServiceFactory
	@SuppressWarnings("unused")
	private KeywordService() {
		this(null);
	}
	
	/**
	 * @return     A {@code Map} where keys are {@code String}s representing species Ids, 
     *             and values are {@code Set}s of {@code String}s that are the associated keywords.
	 * @see #getKeywordForAllSpecies(Collection)
	 */
	public Map<String, Set<String>> getKeywordForAllSpecies() {
		log.entry();
		return log.exit(getKeywordForSpecies(null));
	}
	/**
	 * Gets a {@code Map} of keywords for a given set of species
	 * @param speciesIds   A {@code Collection} of {@code String}s that are IDs of species 
	 *                     for which to return the keywords.
	 * @return             A {@code Map} where keys are {@code String}s representing species Ids, 
	 *                     and values are {@code Set}s of {@code String}s that are the associated keywords.
	 */
	public Map<String, Set<String>> getKeywordForSpecies(Collection<String> speciesIds) {
		log.entry();
		KeywordDAO dao = daoManager.getKeywordDAO();
        Map<String, String> keywordMap = dao.getKeywordsRelatedToSpecies(speciesIds)
                .stream().collect(Collectors.toMap(EntityTO::getId, KeywordDAO.KeywordTO::getName));
		
		DAOResultSet<EntityToKeywordTO> results = dao.getKeywordToSpecies(speciesIds);
		
		return log.exit(results.stream().collect(groupingBy(
				EntityToKeywordTO::getEntityId,
				mapping(t -> keywordMap.get(t.getKeywordId()), toSet()))));
	}
}
