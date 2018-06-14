package org.bgee.model.dao.mysql.file;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;

/**
 * The MySQL implementation of the {@link SpeciesDataGroupDAO} interface.
 *
 * @author Philippe Moret
 * @author Valentine Rech de Laval
 * @author Frederic Bastian
 * @version Bgee 13 Oct. 2015
 * @since Bgee 13
 */
public class MySQLSpeciesDataGroupDAO extends MySQLDAO<SpeciesDataGroupDAO.Attribute> 
implements SpeciesDataGroupDAO {

    /**
     * The {@code Logger} of this class
     */
    private static final Logger log = LogManager.getLogger(MySQLSpeciesDataGroupDAO.class.getName());
    
    /**
     * A {@code String} that is the ID of the human species in the database. 
     */
    private static final String HUMAN_SPECIES_ID = "9606";

    /**
     * The underlying MySQL table name
     */
    private static final String SPECIES_DATAGROUP_TABLE = "speciesDataGroup";

    /**
     * A {@code Map} of column name to their corresponding {@code Attribute}.
     */
    private static final Map<String, SpeciesDataGroupDAO.Attribute> columnToAttributesMap;

    static {
        columnToAttributesMap = new HashMap<>();
        columnToAttributesMap.put("speciesDataGroupId", SpeciesDataGroupDAO.Attribute.ID);
        columnToAttributesMap.put("speciesDataGroupName", SpeciesDataGroupDAO.Attribute.NAME);
        columnToAttributesMap.put("speciesDataGroupDescription", SpeciesDataGroupDAO.Attribute.DESCRIPTION);
        columnToAttributesMap.put("speciesDataGroupOrder", SpeciesDataGroupDAO.Attribute.PREFERRED_ORDER);
    }

    /**
     * Finds the {@link org.bgee.model.dao.api.file.SpeciesDataGroupDAO.Attribute} from a column name.
     *
     * @param columnName The column name.
     * @return The {@link org.bgee.model.dao.api.file.SpeciesDataGroupDAO.Attribute} corresponding to the column name
     * @throws IllegalArgumentException If the columnName doesn't match any attributes.
     */
    private static SpeciesDataGroupDAO.Attribute getAttributeByColumnName(String columnName) {
        log.entry(columnName);
        SpeciesDataGroupDAO.Attribute attribute = columnToAttributesMap.get(columnName);
        if (attribute == null) {
            throw log.throwing(new IllegalArgumentException("Unknown column name : " + columnName));
        } 
        return attribute;
    }

    /**
     * A {@code String} that is the ID of the species that the ordering 
     * of the {@code SpeciesToDatGroupTO}s will be based on, 
     * when {@link SpeciesToGroupOrderingAttribute#DISTANCE_TO_SPECIES} is used.
     * @see SpeciesToGroupOrderingAttribute#DISTANCE_TO_SPECIES
     */
    private final String speciesId;
    
    /**
     * Default constructor providing the {@code MySQLDAOManager} to use, 
     * and targeting by default the human species, when ordering of {@code SpeciesToDataGroupTO}s 
     * is requested, based on the taxonomic distance to a species (see 
     * {@link #MySQLSpeciesDataGroupDAO(MySQLDAOManager, String)}).
     *
     * @param manager   The {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException If {@code manager} is {@code null}.
     * @see #MySQLSpeciesDataGroupDAO(MySQLDAOManager, String)
     */
    public MySQLSpeciesDataGroupDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        this(manager, HUMAN_SPECIES_ID);
    }

    /**
     * Constructor providing the {@code MySQLDAOManager} to use and the ID of the species 
     * to target, when ordering of {@code SpeciesToDataGroupTO}s is requested, using 
     * {@link SpeciesToGroupOrderingAttribute#DISTANCE_TO_SPECIES}.
     *
     * @param manager   The {@code MySQLDAOManager} to use.
     * @param speciesId A {@code String} that is the ID of the species that the ordering
     *                  of the {@code SpeciesToDataGroupTO}s will be based on, 
     *                  when {@link SpeciesToGroupOrderingAttribute#DISTANCE_TO_SPECIES} 
     *                  is used. 
     * @throws IllegalArgumentException If {@code manager} is {@code null}, or {@code speciesId} 
     *                                  is blank.
     * @see SpeciesToGroupOrderingAttribute#DISTANCE_TO_SPECIES
     */
    public MySQLSpeciesDataGroupDAO(MySQLDAOManager manager, String speciesId) throws IllegalArgumentException {
        super(manager);
        if (StringUtils.isBlank(speciesId)) {
            throw log.throwing(
                    new IllegalArgumentException("The provided species ID cannot be blank"));
        }
        this.speciesId = speciesId;
    }


    @Override
    public SpeciesDataGroupTOResultSet getAllSpeciesDataGroup(
        Collection<SpeciesDataGroupDAO.Attribute> attributes, 
        LinkedHashMap<SpeciesDataGroupDAO.OrderingAttribute, SpeciesDataGroupDAO.Direction> orderingAttributes) 
                throws DAOException {
        log.entry(attributes, orderingAttributes);
        
        final Set<SpeciesDataGroupDAO.Attribute> clonedAttrs = attributes == null? null: new HashSet<>(attributes);
        final LinkedHashMap<SpeciesDataGroupDAO.OrderingAttribute, DAO.Direction> clonedOrderingAttrs = 
                orderingAttributes == null? null: new LinkedHashMap<>(orderingAttributes);
        //Fix issue #173
        if (clonedAttrs != null && !clonedAttrs.isEmpty() &&
                clonedOrderingAttrs != null &&
                clonedOrderingAttrs.containsKey(SpeciesDataGroupDAO.OrderingAttribute.PREFERRED_ORDER)) {
            clonedAttrs.add(SpeciesDataGroupDAO.Attribute.PREFERRED_ORDER);
        }

        //for now we still use the setAttributes method to be able to use generateSelectAllStatement, 
        //but this method will soon disappear, signature of generateSelectAllStatement should change.
        this.setAttributes(clonedAttrs);
        
        String sql = generateSelectAllStatement(SPECIES_DATAGROUP_TABLE, columnToAttributesMap, false);
        
        if (clonedOrderingAttrs != null && !clonedOrderingAttrs.isEmpty()) {
            //TODO: for now, there is only one OrderingAttribute in this DAO, 
            //so we manage it in a quick and dirty way, but we should create methods 
            //similar to generateSelectAllStatement, for ordering results. 
            
            //Check that we still have only one OrderingAttribute
            if (clonedOrderingAttrs.size() > 1 || 
                    !clonedOrderingAttrs.containsKey(SpeciesDataGroupDAO.OrderingAttribute.PREFERRED_ORDER)) {
                throw new UnrecognizedColumnException("Unsupported OrderingAttributes: " 
                        + clonedOrderingAttrs);
            }
            
            //The OrderingAttribute uses a field defines in Attributes, 
            //so we can use columnToAttributesMap. 
            sql += " ORDER BY " + columnToAttributesMap.entrySet().stream()
                    .filter(e -> e.getValue() == SpeciesDataGroupDAO.Attribute.PREFERRED_ORDER)
                    .map(e -> e.getKey()).findFirst().get();
            if (clonedOrderingAttrs.values().iterator().next() == DAO.Direction.DESC) {
                sql += " DESC";
            }
        }
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            return log.exit(new MySQLSpeciesDataGroupTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public int insertSpeciesDataGroups(Collection<SpeciesDataGroupTO> groupTOs)
            throws DAOException, IllegalArgumentException {
        log.entry(groupTOs);
        
        if (groupTOs == null || groupTOs.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No species data groups is given, then no group is inserted"));
        }
        
        StringBuilder sql = new StringBuilder(); 
        sql.append("INSERT INTO speciesDataGroup")  
           .append("(speciesDataGroupId, speciesDataGroupName, speciesDataGroupDescription, ")
           .append("speciesDataGroupOrder) VALUES ");
        for (int i = 0; i < groupTOs.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("(?, ?, ?, ?) ");
        }
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sql.toString())) {
            int paramIndex = 1;
            for (SpeciesDataGroupTO groupTO: groupTOs) {
                stmt.setInt(paramIndex, groupTO.getId());
                paramIndex++;
                stmt.setString(paramIndex, groupTO.getName());
                paramIndex++;
                stmt.setString(paramIndex, groupTO.getDescription());
                paramIndex++;
                stmt.setInt(paramIndex, groupTO.getPreferredOrder());
                paramIndex++;
            }
            
            return log.exit(stmt.executeUpdate());
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    /**
     * The MySQL implementation of {@code SpeciesDataGroupTOResultSet}
     * @author Philippe Moret
     * @version Bgee 13
     * @since Bgee 13
     */
    class MySQLSpeciesDataGroupTOResultSet extends MySQLDAOResultSet<SpeciesDataGroupTO> implements
            SpeciesDataGroupTOResultSet {

        private MySQLSpeciesDataGroupTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected SpeciesDataGroupTO getNewTO() throws DAOException {
            log.entry();
            final ResultSet currentResultSet = this.getCurrentResultSet();
            try {
                String name = null, description = null;
                Integer id = null, preferredOrder = null;
                for (String colName : getColumnLabels().values()) {
                    SpeciesDataGroupDAO.Attribute attr = getAttributeByColumnName(colName);
                    switch (attr) {
                        case ID:
                            id = currentResultSet.getInt(colName);
                            break;
                        case DESCRIPTION:
                            description = currentResultSet.getString(colName);
                            break;
                        case NAME:
                            name = currentResultSet.getString(colName);
                            break;
                        case PREFERRED_ORDER:
                            preferredOrder = currentResultSet.getInt(colName);
                            break;
                        default:
                            throw log.throwing(new UnrecognizedColumnException(colName));
                    }
                }
                return log.exit(new SpeciesDataGroupTO(id, name, description, preferredOrder));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }

    /**
     * The MySQL implementation of the {@code DAOResultSet} interface for {@code SpeciesToDataGroupTO}
     * @author Philippe Moret
     * @version Bgee 13
     * @since Bgee 13
     */
    class MySQLSpeciesToDataGroupTOResultSet extends MySQLDAOResultSet<SpeciesToDataGroupTO>
            implements SpeciesToDataGroupTOResultSet {

        /**
         * Construct a result set from the {@code BgeePreparedStatement}
         *
         * @param stmt the {@code BgeePreparedStatement}
         */
        private MySQLSpeciesToDataGroupTOResultSet(BgeePreparedStatement stmt) {
            super(stmt);
        }

        @Override
        protected SpeciesToDataGroupTO getNewTO() throws DAOException {
            log.entry();
            try {
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Integer speciesId = null, groupId = null;

                for (Map.Entry<Integer, String> col : this.getColumnLabels().entrySet()) {
                    String columnName = col.getValue();
                    Integer currentValue = currentResultSet.getInt(columnName);
                    switch (columnName) {
                        case "speciesId":
                            speciesId = currentValue;
                            break;
                        case "speciesDataGroupId":
                            groupId = currentValue;
                            break;
                        case "distToSpe":
                            //nothing here, this attribute is retrieved solely to be used in ORDER BY clause
                            //(fix for issue #173)
                            break;
                        default:
                            log.throwing(new UnrecognizedColumnException(columnName));
                    }
                }
                return log.exit(new SpeciesToDataGroupTO(speciesId,groupId));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }

    @Override
    public SpeciesToDataGroupTOResultSet getAllSpeciesToDataGroup(
            LinkedHashMap<SpeciesToGroupOrderingAttribute, DAO.Direction> orderingAttributes) {
        log.entry(orderingAttributes);
        
        final LinkedHashMap<SpeciesToGroupOrderingAttribute, DAO.Direction> clonedOrderingAttrs = 
                orderingAttributes == null? null: new LinkedHashMap<>(orderingAttributes);
        
        StringBuilder sb = new StringBuilder("SELECT t1.*");
        //Fix issue#173
        //Note: SpeciesToGroupOrderingAttribute.DATA_GROUP_ID and speciesId is already covered by the clause 't1.*'
        if (clonedOrderingAttrs != null && clonedOrderingAttrs.containsKey(SpeciesToGroupOrderingAttribute.DISTANCE_TO_SPECIES)) {
            sb.append(", ")
              //Here is a subquery to identify the level of the least common ancestor
              //between the targeted species and the current species.
              .append("(SELECT t6.taxonLevel FROM taxon AS t6 ")
              .append("WHERE t6.taxonLeftBound <= LEAST(t3.taxonLeftBound, tSpeciesBounds.tSpeciesLeftBound) ")
              .append("AND t6.taxonRightBound >= GREATEST(t3.taxonRightBound, tSpeciesBounds.tSpeciesRightBound) ")
              .append("ORDER BY t6.taxonLevel DESC LIMIT 1) ")
              .append("AS distToSpe");
        }
        sb.append(" FROM speciesToDataGroup AS t1 ");
        
        if (clonedOrderingAttrs != null && !clonedOrderingAttrs.isEmpty()) {
            //If DISTANCE_TO_SPECIES ordering requested, we make joins to get 
            //the left and right bounds of the taxon which the species in data groups belong to.
            //XXX: what would be good would be to rather use a stored procedure, 
            //to have a first query checking that the targeted species exists, 
            //to throw a SIGNAL if it doesn't. 
            if (clonedOrderingAttrs.containsKey(SpeciesToGroupOrderingAttribute.DISTANCE_TO_SPECIES)) {
                sb.append("INNER JOIN species AS t2 ON t1.speciesId = t2.speciesId ")
                  .append("INNER JOIN taxon AS t3 ON t2.taxonId = t3.taxonId ");
                //we also make a join to a fake table to directly have access 
                //to the left and right bounds for the targeted taxon 
                //(will avoid many subqueries in the ORDER BY clause)
                sb.append("INNER JOIN ")
                  .append("(SELECT taxonLeftBound AS tSpeciesLeftBound, taxonRightBound AS tSpeciesRightBound ")
                  .append("FROM taxon AS t4 INNER JOIN species AS t5 ON t4.taxonId = t5.taxonId ")
                  .append("WHERE t5.speciesId = ?) AS tSpeciesBounds ");
            }
            
            sb.append("ORDER BY ");
            int i = 0;
            for (Entry<SpeciesToGroupOrderingAttribute, DAO.Direction> attr: clonedOrderingAttrs.entrySet()) {
                if (i > 0) {
                    sb.append(", "); 
                }
                switch(attr.getKey()) {
                case DATA_GROUP_ID: 
                    sb.append("t1.speciesDataGroupId ");
                    if (attr.getValue() == Direction.DESC) {
                        sb.append("DESC ");
                    }
                    break;
                case DISTANCE_TO_SPECIES:
                    sb.append("distToSpe ");
                    //the greatest the level of the common ancestor, the closest the species 
                    //is from the targeted species. So, if it is requested to order in ascending 
                    //taxonomic distance, then we need to order by descending taxon level.
                    if (attr.getValue() == null || attr.getValue() == Direction.ASC) {
                        sb.append("DESC ");
                    }
                    break;
                default: 
                    throw new UnrecognizedColumnException("Ordering attribute not supported: "
                            + attr.getKey());
                }
                i++;
            }

            //for cases when several species have a same common ancestor, 
            //we also order by speciesId, to have consistent ordering. We add it at the end, 
            //so that other ordering attributes have precedence. 
            if (clonedOrderingAttrs.containsKey(SpeciesToGroupOrderingAttribute.DISTANCE_TO_SPECIES)) {
                sb.append(", t1.speciesId");
            }
        }
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sb.toString());
            if (clonedOrderingAttrs != null && 
                    clonedOrderingAttrs.containsKey(SpeciesToGroupOrderingAttribute.DISTANCE_TO_SPECIES)) {
                stmt.setString(1, this.speciesId);
            }
            return log.exit(new MySQLSpeciesToDataGroupTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    @Override
    public int insertSpeciesToDataGroup(Collection<SpeciesToDataGroupTO> mappingTOs)
            throws DAOException, IllegalArgumentException {
        log.entry(mappingTOs);
        
        if (mappingTOs == null || mappingTOs.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No species data groups to species mappings is given, then no mapping is inserted"));
        }
        
        StringBuilder sql = new StringBuilder(); 
        sql.append("INSERT INTO speciesToDataGroup(speciesDataGroupId, speciesId) VALUES ");
        for (int i = 0; i < mappingTOs.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("(?, ?) ");
        }
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sql.toString())) {
            int paramIndex = 1;
            for (SpeciesToDataGroupTO mappingTO: mappingTOs) {
                stmt.setInt(paramIndex, mappingTO.getGroupId());
                paramIndex++;
                stmt.setInt(paramIndex, mappingTO.getSpeciesId());
                paramIndex++;
            }
            
            return log.exit(stmt.executeUpdate());
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

}
