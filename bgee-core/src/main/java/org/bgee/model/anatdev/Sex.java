package org.bgee.model.anatdev;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeEnum.BgeeEnumField;
import org.bgee.model.BgeeEnum;
import org.bgee.model.NamedEntity;
import org.bgee.model.ontology.OntologyElement;

/**
 * Class describing sexes.
 * 
 * @author Julien Wollbrett
 * @version Bgee 15.0
 *
 */
public class Sex extends NamedEntity<String> implements OntologyElement<Sex, String> {
    private final static Logger log = LogManager.getLogger(Sex.class.getName());

    /**
     * Constructor providing the ID of this {@code Sex}. 
     * {@code id} cannot be blank, otherwise an {@code IllegalArgumentException} is thrown. 
     * 
     * @param id    A {@code String} representing the ID of this {@code Sex}.
     * @throws IllegalArgumentException     if {@code id} is blank. 
     **/
    public Sex(String id) {
        super(id, id, null);
        Set <String> allowedSexRepresentations = SexEnum.getAllowedRepresentations();
        if(!allowedSexRepresentations.contains(id)) {
            throw log.throwing(new IllegalArgumentException("Can only use values from ["
                    + allowedSexRepresentations + "] to create a Sex. [" + id 
                    + "] is not allowed."));
        }
    }
    
    public enum SexEnum implements BgeeEnumField{
        MALE("male"), FEMALE("female"), HERMAPHRODITE("hermaphrodite"), ANY("any");
        
        private final String representation;
        
        private SexEnum(String representation) {
            this.representation = representation;
        }

        @Override
        public String getStringRepresentation() {
            return this.representation;
        }
        
        public static Set<String> getAllowedRepresentations() {
            return EnumSet.allOf(SexEnum.class).stream().map(s -> s.getStringRepresentation())
                    .collect(Collectors.toSet());
        }
        
        public static SexEnum convertToSexEnum(String representation) {
            log.traceEntry("{}", representation);
            return log.traceExit(BgeeEnum.convert(SexEnum.class, representation));
        }
    }
}