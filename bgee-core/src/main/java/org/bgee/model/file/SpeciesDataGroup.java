package org.bgee.model.file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.NamedEntity;
import org.bgee.model.file.DownloadFile;
import org.bgee.model.species.Species;

import java.util.*;

/**
 * Describes a group of one or several species.
 * @author Philippe Moret
 * @see DownloadFile
 */
//TODO: javadoc, private default constructor, equals/hashCode/toString
public class SpeciesDataGroup extends NamedEntity {

    private static final Logger log = LogManager.getLogger(SpeciesDataGroup.class.getName());

    /**
     * @see #getMembers()
     */
    private final List<Species> members;

    //TODO: should be a Set, there is no concept of order for DownloadFiles.
    private final List<DownloadFile> downloadFiles;

    public SpeciesDataGroup(String id, String name, String description, List<Species> members, 
            List<DownloadFile> downloadFiles) {
        super(id,name,description);
        
        if (downloadFiles == null || downloadFiles.isEmpty() || 
                downloadFiles.stream().anyMatch(Objects::isNull)) {
            throw log.throwing(new IllegalArgumentException(
                    "SpeciesDataGroup must be provided with non-null DownloadFiles."));
        }
        this.downloadFiles = Collections.unmodifiableList(new ArrayList<>(downloadFiles));
        
        if (members == null || members.isEmpty() || 
                members.stream().anyMatch(Objects::isNull)) {
            throw log.throwing(new IllegalArgumentException(
                    "SpeciesDataGroup must be provided with non-null Species."));
        }
        this.members = Collections.unmodifiableList(new ArrayList<>(members));
    }

    /**
     * @return  A {@code List} containing the {@code Species} part of this {@code SpeciesDataGroup}, 
     *          in preferred order. 
     */
    //TODO: rename to species and getSpecies(), it sounds clearer to me. 
    public List<Species> getMembers() {
        return members;
    }

    public boolean isSingleSpecies() {
        return members.size() < 2;
    }

    public boolean isMultipleSpecies() {
        return members.size() > 1;
    }

}
