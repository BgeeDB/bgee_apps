package org.bgee.model.dao.mysql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;

/**
 * Parent class of all MySQL DAOs of this module.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 01
 *
 * @param <T>   The type of {@code DAO.Attribute} that can be used with this {@code DAO}, 
 *              to define what attributes should be populated in the {@code TransferObject}s 
 *              obtained from this {@code DAO}.
 */
public abstract class MySQLDAO<T extends Enum<?> & DAO.Attribute> implements DAO<T> {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLDAO.class.getName());


    /**
     * A {@code String} that is the name of the MySQL table storing OMA hierarchical groups 
     * (corresponds to {@link 
     * org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalGroupTO HierarchicalGroupTO}).
     */
    public final static String HIERARCHICAL_GROUP_TABLE_NAME = "OMAHierarchicalGroup";
    /**
     * A {@code String} that is the name of the MySQL table storing genes (corresponds to 
     * {@link org.bgee.model.dao.api.gene.GeneDAO.GeneTO GeneTO}).
     */
    public final static String GENE_TABLE_NAME = "gene";
    /**
     * A {@code String} that is the name of the MySQL table storing Gene Ontology terms 
     * (corresponds to {@link org.bgee.model.dao.api.gene.GeneOntologyDAO.GeneOntologyTO 
     * GeneOntologyTO}).
     */
    public final static String GO_TERM_TABLE_NAME = "geneOntologyTerm";
    /**
     * A {@code String} that is the name of the MySQL table storing relations between 
     * Gene Ontology terms.
     * @see #GO_TERM_TABLE_NAME
     */
    public final static String GO_REL_TABLE_NAME = "geneOntologyRelation";
    /**
     * A {@code String} that is the name of the MySQL table storing species (corresponds to 
     * {@link org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO SpeciesTO}).
     */
    public final static String SPECIES_TABLE_NAME = "species";
    /**
     * A {@code String} that is the name of the MySQL table storing taxa (corresponds to 
     * {@link org.bgee.model.dao.api.species.TaxonDAO.TaxonTO TaxonTO}).
     */
    public final static String TAXON_TABLE_NAME = "taxon";
    /**
     * A {@code String} that is the name of the MySQL table storing species (corresponds to 
     * {@link org.bgee.model.dao.api.source.SourceDAO.SourceTO SourceTO}).
     */
    public final static String SOURCE_TABLE_NAME = "dataSource";
    /**
     * An {@code int} that is the maximum number of rows that can be inserted in a 
     * single INSERT or UPDATE statements.
     */
    protected final static int MAX_UPDATE_COUNT = 10000;
    
    /**
     * A {@code Set} of {@code DAO.Attribute}s specifying the attributes to retrieve 
     * from the data source in order to build {@code TransferObject}s associated to 
     * this {@code DAO}.
     */
    private final Set<T> attributes;
    
    /**
     * The {@code MySQLDAOManager} used by this {@code MySQLDAO} to obtain 
     * {@code BgeeConnection}s.
     */
    private final MySQLDAOManager manager;
    
    /**
     * Default constructor private, should not be used, a {@code MySQLDAOManager} 
     * should always be provided, see {@link #MySQLDAO(MySQLDAOManager)}.
     */
    @SuppressWarnings("unused")
    private MySQLDAO() {
        this(null);
    }
    /**
     * Default constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * @param manager   the {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException If {@code manager} is {@code null}.
     */
    public MySQLDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        //attributes could be an EnumSet, but we would need to provide the class 
        //of T to the constructor in order to get the type...
        this.attributes = new HashSet<T>();
        if (manager == null) {
            throw log.throwing(new IllegalArgumentException("The MySQLDAOManager " +
                    "cannot be null"));
        }
        this.manager = manager;
    }
    
    /**
     * @return  The {@code MySQLDAOManager} used by this {@code MySQLDAO} to obtain 
     *          {@code BgeeConnection}s.
     */
    protected MySQLDAOManager getManager() {
        return this.manager;
    }

    @Override
    public void setAttributes(Collection<T> attributes) {
        log.entry(attributes);
        this.clearAttributes();
        if (attributes != null) {
            this.attributes.addAll(attributes);
        }
        log.exit();
    }
    
    /*
     * (non-Javadoc)
     * suppress warning because this method is robust to heap pollution, it only depends 
     * on the fact that the array will contain {@code T} elements, not on the fact 
     * that it is an array of {@code T}; we can add the @SafeVarargs annotation. 
     * See http://docs.oracle.com/javase/7/docs/technotes/guides/language/non-reifiable-varargs.html
     */
    @SafeVarargs
    @Override
    public final void setAttributes(T... attributes) {
        log.entry((Object[]) attributes);
        Set<T> newAttributes = new HashSet<T>();
        for (int i = 0; i < attributes.length; i++) {
            newAttributes.add(attributes[i]);
        }
        this.setAttributes(newAttributes);
        log.exit();
    }
    
    @Override
    public void clearAttributes() {
        log.entry();
        this.attributes.clear();
        log.exit();
    }
    
    @Override
    public Collection<T> getAttributes() {
        log.entry();
        Set<T> attributeCopy = new HashSet<T>(attributes) ;
        return log.exit(attributeCopy);
    }
    
    @Override
    public <O extends TransferObject> List<O> getAllTOs(DAOResultSet<O> resultSet) 
        throws DAOException {
        log.entry(resultSet);
        List<O> allTOs = new ArrayList<O>();
        while (resultSet.next()) {
            allTOs.add(resultSet.getTO());
        }
        resultSet.close();
        return log.exit(allTOs);
    }
    
    
    
    /**
     * Returns the label corresponding to the provided {@code Attribute}.
     * <p>
     * In most cases, this corresponds to the column name of a MySQL table. But this is different 
     * from a {@code select_expr} (see {@link #getSQLExpr(Enum)}): a {@code select_expr} 
     * could for instance include a table name (table_name.column_name), or a complex 
     * SQL expression (for instance, COUNT). 
     * <p>
     * This label is used by the method {@link #getSelectClause(Collection)}, 
     * to relabel the {@code select_expr} corresponding to {@code attribute}, using an AS clause, 
     * but also by the {@link DAOResultSet#getTO()} methods implemented 
     * by {@code MySQLSQOResultSet}s, to retrieve data from a {@code ResultSet}.
     * <p>
     * Remark: this method was created to avoid defining a {@code getLabel} method in 
     * the {@code SpeciesDAO.Attribute} {@code Enum}, that would have been MySQL specific. 
     * And {@code Enum}s cannot be extended (poor language design). This method should be 
     * static, but Java does not allow abstract static methods (poor language design again).
     * 
     * @param attribute     An {@code Attribute} which we want the label name.
     * @return              A {@code String} that is the label name.
     * @see #getSQLExpr(Enum)
     * @see #getSelectClause(Collection)
     * @see DAOResultSet#getTO()
     * 
     */
    protected abstract String getLabel(T attribute);
    
    /**
     * Return the SQL expression corresponding to {@code attribute}. This 
     * SQL expression can then be used to build the SELECT clause of a MySQL query 
     * (see {@link #getSelectClause(Collection)}), or the {@code conditional_expr}s 
     * in a {@code join_condition} (see {@link #getTableReferences(Collection)}). 
     * Therefore, <strong>it should not include any {@code AS} clause to relabel it</strong>. 
     * Relabeling is the responsibility of {@link #getSelectClause(Collection)}.
     * <p>
     * It can simply correspond to the name of a column in a table, but it can also correspond 
     * to complex statements (for instance, COUNT using DISTINCT).
     * <p>
     * Remark: this method was created to avoid defining a {@code getSQLExpr} method in 
     * the {@code SpeciesDAO.Attribute} {@code Enum}, that would have been MySQL specific. 
     * And {@code Enum}s cannot be extended (poor language design). This method should be 
     * static, but Java does not allow abstract static methods (poor language design again).
     * 
     * @param attribute An {@code Attribute} for which we want the associated 
     *                  SQL expression.
     * @return          A {@code String} that is the SQL expression, and 
     *                  that never includes any {@code AS} clause.
     * @see #getSelectClause(Collection)
     * @see #getTableReferences(Collection)
     */
    protected abstract String getSQLExpr(T attribute);
    
    /**
     * Returns the SELECT clause, beginning of a MySQL query (the {@code select_expr}s 
     * defining data to retrieve), built depending on the requested {@code attributes}. 
     * This method allows to build the SQL statements used by this {@code MySQLDAO}. 
     * <p>
     * This method will call {@link #getSQLExpr(Enum)} for each {@code Attribute}, and 
     * will relabel the SQL expressions obtained with AS clauses, making use of the value 
     * returned by {@link #getLabel(Enum)}. 
     * <p>
     * If {@code attributes} is {@code null} or empty, all data available from 
     * {@code table_references} will be retrieved (see {@link #getTableReferences(Collection)}).
     * 
     * @param attributes    A {@code Collection} of {@code Attribute}s to build 
     *                      the SELECT clause, defining data to retrieve.
     * @return              A {@code String} that is the SELECT clause. 
     * @throw UnsupportedOperationException If no SELECT request done by the 
     *                                      {@code MySQLDAO}. 
     * @see #getSQLExpr(Enum)
     * @see #getTableReferences(Collection)
     */
    protected final String getSelectClause(Collection<T> attributes) {
        log.entry(attributes);
        
        if (attributes == null || attributes.size() == 0) {
            return log.exit("*");
        }
        StringBuilder selectExpr = new StringBuilder();
        for (T attribute: attributes) {
            if (selectExpr.length() != 0) {
                selectExpr.append(", ");
            }
            selectExpr.append(this.getSQLExpr(attribute));
            selectExpr.append(" AS ");
            selectExpr.append(this.getLabel(attribute));
        }
        
        return log.exit(selectExpr.toString());
    }
}
