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
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.keyword.KeywordDAO;
import org.bgee.model.dao.api.keyword.KeywordDAO.EntityToKeywordTO;
import org.bgee.model.dao.api.keyword.KeywordDAO.EntityToKeywordTOResultSet;
/**
 * The service for accessing keywords.
 * 
 * @author Philippe Moret
 * @since Bgee 13
 */
public class KeywordService extends Service {

    private final static Logger log = LogManager.getLogger(KeywordService.class.getName());

    /**
     * @param serviceFactory            The {@code ServiceFactory} to be used to obtain {@code Service}s 
     *                                  and {@code DAOManager}.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    public KeywordService(ServiceFactory serviceFactory) {
        super(serviceFactory);
    }
	
	/**
	 * @return     A {@code Map} where keys are {@code String}s representing species Ids, 
     *             and values are {@code Set}s of {@code String}s that are the associated keywords.
     *             For species, keywords represent alternative names.
	 * @see #getKeywordForSpecies(Collection)
	 */
	public Map<String, Set<String>> getKeywordForAllSpecies() {
		log.entry();
		return log.exit(getKeywordForSpecies(null));
	}
	/**
	 * Gets a {@code Map} of keywords for a given set of species. For species, keywords 
	 * represent alternative names.
	 * @param speciesIds   A {@code Collection} of {@code String}s that are IDs of species 
	 *                     for which to return the keywords.
	 * @return             A {@code Map} where keys are {@code String}s representing species Ids, 
	 *                     and values are {@code Set}s of {@code String}s that are the associated keywords.
	 */
	public Map<String, Set<String>> getKeywordForSpecies(Collection<String> speciesIds) {
		log.entry(speciesIds);
		KeywordDAO dao = this.getDaoManager().getKeywordDAO();
        Map<String, String> keywordMap = dao.getKeywordsRelatedToSpecies(speciesIds)
                .stream().collect(Collectors.toMap(EntityTO::getId, KeywordDAO.KeywordTO::getName));
		
        EntityToKeywordTOResultSet results = dao.getKeywordToSpecies(speciesIds);
		
		return log.exit(results.stream().collect(groupingBy(
				EntityToKeywordTO::getEntityId,
				mapping(t -> keywordMap.get(t.getKeywordId()), toSet()))));
	}
}
