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

public class SpeciesDataGroup extends NamedEntity {

    private static final Logger log = LogManager.getLogger(SpeciesDataGroup.class.getName());

    private final List<Species> members;

    private final List<DownloadFile> downloadFiles;

    public SpeciesDataGroup(String name, String description, String id, List<Species> members, List<DownloadFile> downloadFiles) {
        super(id,name,description);
        this.downloadFiles = Collections.unmodifiableList(new ArrayList<>(downloadFiles));
        log.entry();
        for (Species s: members) {
            if (s == null) {
                throw log.throwing(new IllegalArgumentException("SpeciesDataGroup cannot contains null members"));
            }
        }
        this.members = Collections.unmodifiableList(new ArrayList<>(members));
        log.exit();
    }

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
