package org.bgee.model.file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.NamedEntity;
import org.bgee.model.species.Species;

import java.util.*;

/**
 * Describes a group of one or several species.
 * @author Philippe Moret
 * @author Frederic Bastian
 * @version Bgee 15.2, May 2024
 * @since Bgee 13
 * @see SpeciesDownloadFile
 */
//TODO: change this class to not expose an ID, they are not stable between releases
public class SpeciesDataGroup extends NamedEntity<Integer> {
    private static final Logger log = LogManager.getLogger(SpeciesDataGroup.class.getName());

    /**
     * @see #getMembers()
     */
    private final List<Species> members;
	/**
	 * @see #getDownloadFiles()
	 */
    private final Set<SpeciesDownloadFile> downloadFiles;

    public SpeciesDataGroup(Integer id, String name, String description, List<Species> members, 
            Set<SpeciesDownloadFile> downloadFiles) {
        super(id,name,description);
        
        if (downloadFiles == null || downloadFiles.isEmpty() || 
                downloadFiles.stream().anyMatch(Objects::isNull)) {
            throw log.throwing(new IllegalArgumentException(
                    "SpeciesDataGroup must be provided with non-null SpeciesDownloadFiles."));
        }
        this.downloadFiles = Collections.unmodifiableSet(new HashSet<>(downloadFiles));
        
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
    public List<Species> getMembers() {
        return members;
    }
    /**
     * @return true if the group contains less than 2 species, false otherwise
     */
    public boolean isSingleSpecies() {
        return members.size() < 2;
    }
    /**
     * @return true if the group contains more than 1 species, false otherwise
     */
    public boolean isMultipleSpecies() {
        return members.size() > 1;
    }
    /**
     * @return The {@code Set} of {@link SpeciesDownloadFile} associated to this group.
     */
	public Set<SpeciesDownloadFile> getDownloadFiles() {
		return downloadFiles;
	}
	

	//XXX: NamedEntity hashCode/equals usually only relies on their ID
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(downloadFiles, members);
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        SpeciesDataGroup other = (SpeciesDataGroup) obj;
        return Objects.equals(downloadFiles, other.downloadFiles) && Objects.equals(members, other.members);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SpeciesDataGroup [")
               .append("getId()=").append(getId())
               .append(", getName()=").append(getName())
               .append(", getDescription()=").append(getDescription())
               .append(", members=").append(members)
               .append(", downloadFiles=").append(downloadFiles)
               .append("]");
        return builder.toString();
    }
}
