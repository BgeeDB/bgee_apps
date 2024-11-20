package org.bgee.model.anatdev;

import org.bgee.model.NamedEntity;
import org.bgee.model.expressiondata.call.ConditionParameterValue;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataConditionParameterValue;
import org.bgee.model.ontology.OntologyElement;

/**
 * Class describing anatomical entities.
 * 
 * @author Frederic Bastian
 * @version Bgee 13.1
 *
 */
public class AnatEntity extends NamedEntity<String>
implements OntologyElement<AnatEntity, String>, ConditionParameterValue, RawDataConditionParameterValue {
    private final Boolean isCellType;

    /**
     * Constructor providing the ID of this {@code AnatEntity}. 
     * {@code id} cannot be blank, otherwise an {@code IllegalArgumentException} is thrown. 
     * 
     * @param id    A {@code String} representing the ID of this {@code AnatEntity}.
     * @throws IllegalArgumentException     if {@code id} is blank. 
     */
    public AnatEntity(String id) throws IllegalArgumentException {
        this(id, null, null);
    }
    /**
     * Constructor providing the ID, name, and description corresponding to this {@code AnatEntity}. 
     * {@code id} cannot be blank, otherwise an {@code IllegalArgumentException} is thrown. 
     * 
     * @param id            A {@code String} representing the ID of this {@code AnatEntity}.
     * @param name          A {@code String} representing the name of this {@code AnatEntity}.
     * @param description   A {@code String} representing the description of this {@code AnatEntity}.
     * @throws IllegalArgumentException     if {@code id} is blank. 
     */
    public AnatEntity(String id, String name, String description) {
        this(id, name, description, null);
    }
    /**
     * Constructor providing the ID, name, and description corresponding to this {@code AnatEntity}. 
     * {@code id} cannot be blank, otherwise an {@code IllegalArgumentException} is thrown. 
     * 
     * @param id            A {@code String} representing the ID of this {@code AnatEntity}.
     * @param name          A {@code String} representing the name of this {@code AnatEntity}.
     * @param description   A {@code String} representing the description of this {@code AnatEntity}.
     * @param description   A {@code Boolean} indicating whether this {@code AnatEntity} is a cell type.
     * @throws IllegalArgumentException     if {@code id} is blank. 
     */
    public AnatEntity(String id, String name, String description, Boolean isCellType) {
        super(id, name, description);
        this.isCellType = isCellType;
    }

    public Boolean getIsCellType() {
        return isCellType;
    }
}