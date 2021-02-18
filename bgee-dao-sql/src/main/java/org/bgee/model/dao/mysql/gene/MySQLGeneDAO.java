package org.bgee.model.dao.mysql.gene;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;

/**
 * A {@code GeneDAO} for MySQL.
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Apr. 2019
 * @see org.bgee.model.dao.api.gene.GeneDAO.GeneTO
 * @see org.bgee.model.dao.api.gene.GeneDAO.GeneBioTypeTO
 * @since   Bgee 13, May 2014
 */
public class MySQLGeneDAO extends MySQLDAO<GeneDAO.Attribute> implements GeneDAO {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(MySQLGeneDAO.class.getName());

    private static final String GENE_TABLE_NAME = "gene";
    /**
     * A {@code String} that is the field name for Bgee internal gene IDs.
     */
    public static final String BGEE_GENE_ID = "bgeeGeneId";

    /**
     * A {@code Map} of column name to their corresponding {@code Attribute}.
     */
    private static final Map<String, GeneDAO.Attribute> columnToAttributesMap;

    static {
        columnToAttributesMap = new HashMap<>();
        columnToAttributesMap.put(BGEE_GENE_ID, GeneDAO.Attribute.ID);
        columnToAttributesMap.put("geneId", GeneDAO.Attribute.ENSEMBL_ID);
        columnToAttributesMap.put("geneName", GeneDAO.Attribute.NAME);
        columnToAttributesMap.put("geneDescription", GeneDAO.Attribute.DESCRIPTION);
        columnToAttributesMap.put("speciesId", GeneDAO.Attribute.SPECIES_ID);
        columnToAttributesMap.put("geneBioTypeId", GeneDAO.Attribute.GENE_BIO_TYPE_ID);
        columnToAttributesMap.put("OMAParentNodeId", GeneDAO.Attribute.OMA_PARENT_NODE_ID);
        columnToAttributesMap.put("ensemblGene", GeneDAO.Attribute.ENSEMBL_GENE);
        columnToAttributesMap.put("geneMappedToGeneIdCount", GeneDAO.Attribute.GENE_MAPPED_TO_SAME_GENE_ID_COUNT);
    }

    /**
     * Constructor providing the {@code MySQLDAOManager} that this
     * {@code MySQLDAO} will use to obtain {@code BgeeConnection}s.
     *
     * @param manager
     *            The {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException
     *             If {@code manager} is {@code null}.
     */
    public MySQLGeneDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public GeneTOResultSet getAllGenes() throws DAOException {
        log.traceEntry();
        return log.traceExit(getGenes(null, null, null));
    }

    @Override
    public GeneTOResultSet getGenesBySpeciesIds(Collection<Integer> speciesIds) throws DAOException {
        log.entry(speciesIds);
        return log.traceExit(this.getGenes(convertSpeciesIdsTOMap(speciesIds), null, null));
    }

    @Override
    public GeneTOResultSet getGenesWithDataBySpeciesIds(Collection<Integer> speciesIds) throws DAOException {
        log.entry(speciesIds);
        return log.traceExit(this.getGenes(convertSpeciesIdsTOMap(speciesIds), null, true));
    }

    @Override
    public GeneTOResultSet getGenesByEnsemblGeneIds(Collection<String> ensemblGeneIds) throws DAOException {
        log.entry(ensemblGeneIds);
        Map<Integer, Set<String>> speToGeneMap = new HashMap<>();
        //sanity checks on geneIds will be performed by the getGenes method
        speToGeneMap.put(null, ensemblGeneIds == null? null: new HashSet<>(ensemblGeneIds));
        return log.traceExit(getGenes(speToGeneMap, null, null));
    }
    
    @Override
    public GeneTOResultSet getGenesByIds(Collection<Integer> geneIds) throws DAOException {
        log.entry(geneIds);
        return log.traceExit(getGenes(null, geneIds, null));
    }

    @Override
    public GeneTOResultSet getGenesBySpeciesAndGeneIds(Map<Integer, Set<String>> speciesIdToGeneIds)
            throws DAOException {
        log.entry(speciesIdToGeneIds);
        return log.traceExit(this.getGenes(speciesIdToGeneIds, null, null));
    }

    @Override
    public GeneTOResultSet getGenesByBgeeIds(Collection<Integer> bgeeGeneIds) throws DAOException {
        log.entry(bgeeGeneIds);
        return log.traceExit(this.getGenes(null, bgeeGeneIds, null));
    }

    private GeneTOResultSet getGenes(Map<Integer, Set<String>> speciesIdToGeneIds,
            Collection<Integer> bgeeGeneIds, Boolean withExprData) throws DAOException {
        log.entry(speciesIdToGeneIds, bgeeGeneIds, withExprData);

        if (speciesIdToGeneIds != null &&
                speciesIdToGeneIds.containsKey(null) && speciesIdToGeneIds.size() != 1) {
            throw log.throwing(new IllegalArgumentException(
                    "If a null species ID is provided, it should be the only Entry in the Map."));
        }

        //need a LinkedHashMap for consistent setting of the query parameters.
        //Copy it with Collectors.toMap to create copies of the gene ID Sets.
        LinkedHashMap<Integer, Set<String>> clonedSpeciesIdToGeneIds = speciesIdToGeneIds == null?
                new LinkedHashMap<>(): speciesIdToGeneIds.entrySet().stream()
                //eliminate entries with no species IDs and no gene IDs
                .filter(e -> e.getKey() != null || e.getValue() != null && !e.getValue().isEmpty())
                //sort the the species IDs for maximizing chances of cache hits.
                //Gene IDs in the Set values will be sorted by the method BgeePreparedStatement.setIntegers
                .sorted(Comparator.comparing(e -> e.getKey(),
                        //null species ID is allowed for method such as getGenesByIds,
                        //but in that case it should be the only entry in the Map
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toMap(
                        e -> {
                            if (e.getKey() != null && e.getKey() <= 0) {
                                throw log.throwing(new IllegalArgumentException(
                                        "No species ID can be less than 1"));
                            }
                            return e.getKey();
                        },
                        e -> {
                            if (e.getValue() == null) {
                                return new HashSet<>();
                            }
                            if (e.getValue().stream().anyMatch(geneId -> StringUtils.isBlank(geneId))) {
                                throw log.throwing(new IllegalArgumentException("No gene ID can be null"));
                            }
                            return new HashSet<>(e.getValue());
                        },
                        (v1, v2) -> {throw new IllegalStateException("Impossible to have duplicated keys.");},
                        () -> new LinkedHashMap<>()));
        Set<Integer> clonedBgeeGeneIds = bgeeGeneIds == null? new HashSet<>(): new HashSet<>(bgeeGeneIds);

        String sql = generateSelectClause(GENE_TABLE_NAME, columnToAttributesMap, true, this.getAttributes());

        sql += " FROM " + GENE_TABLE_NAME;

        if (!clonedSpeciesIdToGeneIds.isEmpty() || !clonedBgeeGeneIds.isEmpty() ||
                Boolean.TRUE.equals(withExprData)) {
            sql += " WHERE ";
        }
        if (clonedSpeciesIdToGeneIds.values().stream().anyMatch(geneIds -> !geneIds.isEmpty())) {
            sql += clonedSpeciesIdToGeneIds.entrySet().stream().map(
                    e -> {
                        String where = "";
                        //a null species ID key is allowed for methods such as getGenesbyIds,
                        //but it should be the only entry in the Map then.
                        if (e.getKey() != null) {
                            where += GENE_TABLE_NAME + ".speciesId = ?";
                            if (!e.getValue().isEmpty()) {
                                where += " AND ";
                            }
                        }
                        if (!e.getValue().isEmpty()) {
                            where += GENE_TABLE_NAME + ".geneId IN ("
                                     + BgeePreparedStatement.generateParameterizedQueryString(
                                            e.getValue().size()) + ")";
                        }
                        return where;
                    }).collect(Collectors.joining(" OR ", "(", ") "));
        } else if (!clonedSpeciesIdToGeneIds.isEmpty()) {
            sql += GENE_TABLE_NAME + ".speciesId IN ("
                   + BgeePreparedStatement.generateParameterizedQueryString(
                           clonedSpeciesIdToGeneIds.size())
                   + ") ";
        }

        if (!clonedBgeeGeneIds.isEmpty()) {
            if (!clonedSpeciesIdToGeneIds.isEmpty()) {
                sql += " AND ";
            }
            sql += GENE_TABLE_NAME + ".bgeeGeneId IN ("
                   + BgeePreparedStatement.generateParameterizedQueryString(clonedBgeeGeneIds.size())
                   + ")";
        }
        if (Boolean.TRUE.equals(withExprData)) {
            if (!clonedSpeciesIdToGeneIds.isEmpty() || !clonedBgeeGeneIds.isEmpty()) {
                sql += " AND ";
            }
            sql += "EXISTS (SELECT 1 FROM expression WHERE expression.bgeeGeneId = "
                   + GENE_TABLE_NAME + ".bgeeGeneId)";
        }

        // we don't use a try-with-resource, because we return a pointer to the results,
        // not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            int offsetParamIndex = 1;
            for (Entry<Integer, Set<String>> speToGenes: clonedSpeciesIdToGeneIds.entrySet()) {
                if (speToGenes.getKey() != null) {
                    stmt.setInt(offsetParamIndex, speToGenes.getKey());
                    offsetParamIndex++;
                }
                if (!speToGenes.getValue().isEmpty()) {
                    stmt.setStrings(offsetParamIndex, speToGenes.getValue(), true);
                    offsetParamIndex += speToGenes.getValue().size();
                }
            }
            if (!clonedBgeeGeneIds.isEmpty()) {
                stmt.setIntegers(offsetParamIndex, clonedBgeeGeneIds, true);
                offsetParamIndex += clonedBgeeGeneIds.size();
            }

            return log.traceExit(new MySQLGeneTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    public GeneBioTypeTOResultSet getGeneBioTypes() {
        log.traceEntry();

        String sql = "SELECT * FROM geneBioType";
        // we don't use a try-with-resource, because we return a pointer to the results,
        // not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            return log.traceExit(new MySQLGeneBioTypeTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    // ***************************************************************************
    // METHODS NOT PART OF THE bgee-dao-api, USED BY THE PIPELINE AND NOT MEANT
    // TO BE EXPOSED TO THE PUBLIC API.
    // ***************************************************************************

    @Override
    public int updateGenes(Collection<GeneTO> genes, Collection<GeneDAO.Attribute> attributesToUpdate)
            throws DAOException, IllegalArgumentException {
        log.entry(genes, attributesToUpdate);

        if (genes == null || genes.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No gene is given, then no gene is updated"));
        }
        if (attributesToUpdate == null || attributesToUpdate.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No attribute is given, then no gene is updated"));
        }
        // if
        // (attributesToUpdate.contains(GeneDAO.Attribute.ANCESTRAL_OMA_NODE_ID)
        // ||
        // attributesToUpdate.contains(GeneDAO.Attribute.ANCESTRAL_OMA_TAXON_ID))
        // {
        // throw log.throwing(new IllegalArgumentException(
        // "'Ancestral OMA' attributes are not store in database, then no gene
        // is updated"));
        // }

        int geneUpdatedCount = 0;
        // Construct sql query according to currents attributes
        StringBuilder sql = new StringBuilder();
        Map<GeneDAO.Attribute, String> attrToColumnMap = columnToAttributesMap.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getValue(), e -> e.getKey()));

        for (GeneDAO.Attribute attribute : attributesToUpdate) {
            String col = attrToColumnMap.get(attribute);
            if (col == null) {
                throw log.throwing(new IllegalStateException("Unsupported Attribute: " + attribute));
            }
            if (sql.length() == 0) {
                sql.append("UPDATE gene SET ");
            } else {
                sql.append(", ");
            }
            sql.append(col + " = ?");
        }
        sql.append(" WHERE bgeeGeneId = ?");

        try (BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql.toString())) {
            for (GeneTO gene : genes) {
                int i = 1;
                for (GeneDAO.Attribute attribute : attributesToUpdate) {
                    if (attribute.equals(GeneDAO.Attribute.NAME)) {
                        stmt.setString(i++, gene.getName());
                    } else if (attribute.equals(GeneDAO.Attribute.DESCRIPTION)) {
                        stmt.setString(i++, gene.getDescription());
                    } else if (attribute.equals(GeneDAO.Attribute.SPECIES_ID)) {
                        stmt.setInt(i++, gene.getSpeciesId());
                    } else if (attribute.equals(GeneDAO.Attribute.GENE_BIO_TYPE_ID)) {
                        stmt.setInt(i++, gene.getGeneBioTypeId());
                    } else if (attribute.equals(GeneDAO.Attribute.OMA_PARENT_NODE_ID)) {
                        stmt.setInt(i++, gene.getOMAParentNodeId());
                    } else if (attribute.equals(GeneDAO.Attribute.ENSEMBL_GENE)) {
                        stmt.setBoolean(i++, gene.isEnsemblGene());
                    }
                }
                stmt.setInt(i, gene.getId());
                geneUpdatedCount += stmt.executeUpdate();
                stmt.clearParameters();
            }
            return log.traceExit(geneUpdatedCount);
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    private Map<Integer, Set<String>> convertSpeciesIdsTOMap(Collection<Integer> speciesIds) {
        log.entry(speciesIds);
        return log.traceExit(speciesIds == null? null: speciesIds.stream()
                .collect(Collectors.toMap(id -> {
                    if (id == null || id <= 0) {
                        throw log.throwing(new IllegalStateException(
                                "No species ID can be null or less than 1."));
                    }
                    return id;
                }, id -> new HashSet<>(), (v1, v2) -> v1)));
    }

    /**
     * A {@code MySQLDAOResultSet} specific to {@code GeneTO}.
     *
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public class MySQLGeneTOResultSet extends MySQLDAOResultSet<GeneTO> implements GeneTOResultSet {

        /**
         * Delegates to
         * {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
         * super constructor.
         *
         * @param statement
         *            The first {@code BgeePreparedStatement} to execute a query
         *            on.
         */
        private MySQLGeneTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected GeneTO getNewTO() {
            log.traceEntry();
            String geneId = null, geneName = null, geneDescription = null;
            Integer id = null, speciesId = null, geneBioTypeId = null, OMAParentNodeId = null,
                    geneMappedToGeneIdCount = null;
            Boolean ensemblGene = null;
            // Get results
            for (Entry<Integer, String> column : this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("bgeeGeneId")) {
                        id = this.getCurrentResultSet().getInt(column.getKey());

                    } else if (column.getValue().equals("geneId")) {
                        geneId = this.getCurrentResultSet().getString(column.getKey());

                    } else if (column.getValue().equals("geneName")) {
                        geneName = this.getCurrentResultSet().getString(column.getKey());

                    } else if (column.getValue().equals("geneDescription")) {
                        geneDescription = this.getCurrentResultSet().getString(column.getKey());

                    } else if (column.getValue().equals("speciesId")) {
                        speciesId = this.getCurrentResultSet().getInt(column.getKey());

                    } else if (column.getValue().equals("geneBioTypeId")) {
                        geneBioTypeId = this.getCurrentResultSet().getInt(column.getKey());

                    } else if (column.getValue().equals("OMAParentNodeId")) {
                        OMAParentNodeId = this.getCurrentResultSet().getInt(column.getKey());

                    } else if (column.getValue().equals("ensemblGene")) {
                        ensemblGene = this.getCurrentResultSet().getBoolean(column.getKey());

                    } else if (column.getValue().equals("geneMappedToGeneIdCount")) {
                        geneMappedToGeneIdCount = this.getCurrentResultSet().getInt(column.getKey());

                    } else if (column.getValue().equals("matchLength") || column.getValue().equals("termMatch") ||
                            column.getValue().equals("speciesDisplayOrder")) {
                        //nothing here, these columns are retrieved solely to fix issue#173
                    } else {
                        throw log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            // Set GeneTO
            return log.traceExit(new GeneTO(id, geneId, geneName, geneDescription, speciesId, geneBioTypeId, OMAParentNodeId,
                    ensemblGene, geneMappedToGeneIdCount));
        }
    }

    /**
     * A {@code MySQLDAOResultSet} specific to {@code GeneBioTypeTO}.
     * 
     * @author Frederic Bastian
     * @version Bgee 14 Sep. 2018
     * @since Bgee 14 Sep. 2018
     */
    public class MySQLGeneBioTypeTOResultSet extends MySQLDAOResultSet<GeneBioTypeTO> implements GeneBioTypeTOResultSet {

        /**
         * Delegates to
         * {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
         * super constructor.
         * 
         * @param statement     The first {@code BgeePreparedStatement} to execute a query on.
         */
        private MySQLGeneBioTypeTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected GeneBioTypeTO getNewTO() {
            log.traceEntry();
            String name = null;
            Integer id = null;
            // Get results
            for (Entry<Integer, String> column : this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("geneBioTypeId")) {
                        id = this.getCurrentResultSet().getInt(column.getKey());

                    } else if (column.getValue().equals("geneBioTypeName")) {
                        name = this.getCurrentResultSet().getString(column.getKey());

                    } else {
                        throw log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            // Set GeneBioTypeTO
            return log.traceExit(new GeneBioTypeTO(id, name));
        }
    }
}
