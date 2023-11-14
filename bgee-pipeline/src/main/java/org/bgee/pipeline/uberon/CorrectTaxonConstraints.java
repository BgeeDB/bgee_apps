package org.bgee.pipeline.uberon;

import java.io.File;
import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.TaxonConstraintDAO.TaxonConstraintTO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.pipeline.MySQLDAOUser;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

/**
 * Class dedicated to the correction of mismatch between Uberon and the Bgee
 * RDB. One mismatch corresponds to one missing anatomical entity for one
 * species. This mismatch correction is a short term correction. It could be
 * interesting to understand the origin of these mismatches and directly correct
 * them during taxon constraints generation.
 * Example of command line usage for this task: 
     *   {@code java -Dbgee.dao.jdbc.username={ROOT_MYSQL_USERNAME} -Dbgee.dao.jdbc.password={ROOT_MYSQL_PASS} 
     *   -Dbgee.dao.jdbc.driver.names=com.mysql.cj.jdbc.Driver
     *   -Dbgee.dao.jdbc.url=jdbc:mysql://{SERVER_NAME}:3306/bgee_v{BGEE_VERSION}  
     *   -jar bgee-pipeline-{BGEE_VERSION}-with-dependencies.jar CorrectTaxonConstraints fromRDB}
 * 
 * @author Julien Wollbrett
 * @version Bgee 14
 * @since Bgee 14
 */
//TODO: to remove
public class CorrectTaxonConstraints extends MySQLDAOUser {

	private final static Logger log = LogManager.getLogger(CorrectTaxonConstraints.class.getName());

	private Set<TaxonConstraintTO<String>> anatEntityTaxonConstraintTOs;
	private Set<TaxonConstraintTO<Integer>> anatRelTaxonConstraintTOs;

	public CorrectTaxonConstraints() {
		this(null);
	}

	/**
	 * Constructor providing the {@code MySQLDAOManager} that will be used by
	 * this object to perform queries to the database.
	 *
	 * @param manager
	 *            the {@code MySQLDAOManager} to use.
	 */
	public CorrectTaxonConstraints(MySQLDAOManager manager) {
		super(manager);

		this.anatEntityTaxonConstraintTOs = new HashSet<TaxonConstraintTO<String>>();
		this.anatRelTaxonConstraintTOs = new HashSet<TaxonConstraintTO<Integer>>();
	}

	public static void main(String[] args) throws Exception {
		CorrectTaxonConstraints correctTC = new CorrectTaxonConstraints();
		Set<Mismatch> mismatches = new HashSet<>();
		if (args.length < 1 || args.length > 2) {
			log.error(new IllegalStateException(
					"You must provide 1 or 2 arguments. First argument corresponds to the action. Actions can be :\n"
							+ "\t- fromRDB : correct taxon constraints by querying the RDB\n"
							+ "\t- fromTSV : correct taxon constraints using a TSV file. With this action you must add"
							+ " a 2nd argument correponding to TSV file path."));
		}
		if (args[0].equals("fromTSV") && args.length > 1) {
			log.debug("Load mismatches from TSV file");
			mismatches = correctTC.loadMismatchesFromTSV(new File(args[1]));
		} else if (args[0].equals("fromRDB") && args.length == 1) {
			log.debug("Load mismatches from Bgee RDB");
			mismatches = correctTC.loadMismatchesFromRDB();
		} else {
			log.error(new IllegalStateException(
					"First argument corresponds to the action. Actions can " + "be \"fromRDB\" or \"fromTSV\".\n"
							+ "\t- fromRDB : correct taxon constraints by querying the RDB\n"
							+ "\t- fromTSV : correct taxon constraints using a TSV file. With this action you must add"
							+ " a 2nd argument correponding to TSV file path."));
		}
		correctTC.persistMismatches(mismatches);

	}

	private Set<Mismatch> loadMismatchesFromRDB() {
		log.info(
				"start querying RDB in order to find taxon constraints mismatches. This step is extremely long.");
		Set<Mismatch> mismatches = new HashSet<>();
		String sql = "SELECT t1.*, GROUP_CONCAT(DISTINCT t2.speciesId ORDER BY t2.speciesId) AS relationTaxonConstraints, "
				+ "GROUP_CONCAT(DISTINCT t3.speciesId ORDER BY t3.speciesId) AS sourceTaxonContraints, "
				+ "GROUP_CONCAT(DISTINCT t4.speciesId ORDER BY t4.speciesId) AS targetTaxonConstraints "
				+ "FROM anatEntityRelation AS t1 INNER JOIN anatEntityRelationTaxonConstraint "
				+ "AS t2 ON t1.anatEntityRelationId = t2.anatEntityRelationId "
				+ "INNER JOIN anatEntityTaxonConstraint AS t3 ON t1.anatEntitySourceId = t3.anatEntityId "
				+ "INNER JOIN anatEntityTaxonConstraint AS t4 ON t1.anatEntityTargetId = t4.anatEntityId "
				+ "WHERE NOT EXISTS (SELECT 1 FROM anatEntityTaxonConstraint AS t5 WHERE t5.anatEntityId = t1.anatEntitySourceId "
				+ "AND (t5.speciesId IS NULL OR t2.speciesId IS NULL OR t5.speciesId = t2.speciesId)) "
				+ "OR NOT EXISTS (SELECT 1 FROM anatEntityTaxonConstraint AS t5 WHERE t5.anatEntityId = t1.anatEntityTargetId "
				+ "AND (t5.speciesId IS NULL OR t2.speciesId IS NULL OR t5.speciesId = t2.speciesId)) "
				+ "GROUP BY t1.anatEntityRelationId;";
		log.info("query : " + sql);
		try {
			BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
			ResultSet resultSet = stmt.getRealPreparedStatement().executeQuery();
			log.info("finish querying RDB");
			while (resultSet.next()) {
				Mismatch mismatch = new Mismatch(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3),
						resultSet.getString(4), resultSet.getString(5), constraintsToSetInteger(resultSet.getString(6)),
						constraintsToSetInteger(resultSet.getString(7)),
						constraintsToSetInteger(resultSet.getString(8)));
				mismatches.add(mismatch);
			}
			log.info("finish querying RDB. " + mismatches.size() + " mismatches have been found.");
		} catch (SQLException e) {
			throw log.throwing(new DAOException(e));
		}
		return log.traceExit(mismatches);
	}

	private Set<Mismatch> loadMismatchesFromTSV(File file) throws Exception {
		Set<Mismatch> mismatches = new HashSet<>();
		ICsvMapReader mapReader = null;
		try {
			log.info("start using TSV file to find mismatches.");
			mapReader = new CsvMapReader(new FileReader(file), CsvPreference.TAB_PREFERENCE);
			final String[] header = mapReader.getHeader(true);
			final CellProcessor[] processors = getProcessors();
			Map<String, Object> customerMap;
			while ((customerMap = mapReader.read(header, processors)) != null) {
				mismatches.add(new Mismatch(Integer.decode(customerMap.get(Mismatch.AE_REL_ID).toString()),
						customerMap.get(Mismatch.AE_SOURCE_ID).toString(),
						customerMap.get(Mismatch.AE_TARGET_ID).toString(),
						customerMap.get(Mismatch.REL_TYPE).toString(), customerMap.get(Mismatch.REL_STATUS).toString(),
						constraintsToSetInteger(customerMap.get(Mismatch.REL_TAXON_CSTRTS).toString()),
						constraintsToSetInteger(customerMap.get(Mismatch.SOURCE_TAXON_CSTRTS).toString()),
						constraintsToSetInteger(customerMap.get(Mismatch.TARGET_TAXON_CSTRTS).toString())));
			}
			log.info("finish retrieve mismatches form TSV file. " + mismatches.size() + " mismatches have been found.");
		} finally {
			if (mapReader != null) {
				mapReader.close();
			}
		}
		return log.traceExit(mismatches);
	}

	private static CellProcessor[] getProcessors() {
		final CellProcessor[] processors = new CellProcessor[] { new ParseInt(), new NotNull(), new NotNull(),
				new NotNull(), new NotNull(), new NotNull(), new NotNull(), new NotNull(), };
		return processors;
	}

	private Set<Integer> constraintsToSetInteger(String constraints) {
		Set<Integer> relationTaxonConstraints = new HashSet<Integer>();
		if (constraints != null) {
			relationTaxonConstraints.addAll(Arrays.asList(constraints.split(",")).stream()
					.filter(e -> !e.equals("NULL")).map(Integer::parseInt).collect(Collectors.toSet()));
		}
		return log.traceExit(relationTaxonConstraints);
	}

	private void persistMismatches(Set<Mismatch> mismatches) {

		for (Mismatch mismatch : mismatches) {
			Set<Integer> relTC = mismatch.getRelationTaxonConstraints();
			Set<Integer> sourceTC = mismatch.getSourceTaxonContraints();
			Set<Integer> targetTC = mismatch.getTargetTaxonConstraints();
			for (Integer relSpecies : relTC) {
				if (sourceTC.contains(relSpecies) && !targetTC.contains(relSpecies)) {
					this.anatEntityTaxonConstraintTOs
							.add(new TaxonConstraintTO<String>(mismatch.getAnatEntityTargetId(), relSpecies));
				} else if (targetTC.contains(relSpecies) && !sourceTC.contains(relSpecies)) {
					this.anatEntityTaxonConstraintTOs
							.add(new TaxonConstraintTO<String>(mismatch.getAnatEntitySourceId(), relSpecies));
				} else if (!targetTC.contains(relSpecies) && !sourceTC.contains(relSpecies)) {
					this.anatRelTaxonConstraintTOs
							.add(new TaxonConstraintTO<Integer>(mismatch.getAnatEntityRelationId(), relSpecies));
				} else {
					log.debug("No problems with this relation.");
				}
			}
		}
		log.info("{} anatomical entities taxon constraints will be added.", anatEntityTaxonConstraintTOs.size());
		log.info( "{} anatomical relations taxon constraints will be deleted.", anatRelTaxonConstraintTOs.size());
		log.info("Start correction of mismatches in the RDB.");
		// add missing AnatEntityTaxonConstraint TOs AND remove wrong AnatEntityRelationTaxonConstraints
		try {
			this.startTransaction();
			if(anatEntityTaxonConstraintTOs.size() > 0){
				log.info("List of missing anatEntityTaxonConstraintTOs that will be inserted : {}",anatEntityTaxonConstraintTOs);
				this.getTaxonConstraintDAO().insertAnatEntityTaxonConstraints(anatEntityTaxonConstraintTOs);
			}
			if(anatRelTaxonConstraintTOs.size() > 0){
				log.warn(
						"there is currently no implemented method allowing to delete anat entity relation taxon constraints."
								+ " Please delete them manually using following mysql commands.\n");
				for (TaxonConstraintTO<Integer> aeRTC : anatRelTaxonConstraintTOs) {
					log.warn("DELETE FROM anatEntityRelationTaxonConstraint WHERE anatEntityRelationId = "
							+ "{} AND speciesId = {};", aeRTC.getEntityId(), aeRTC.getSpeciesId());
				}
			}
			this.commit();
			log.info("Finish correction of mismatches in the RDB.");
		} finally {
			this.closeDAO();
		}

		log.traceExit();
	}
}

final class Mismatch {

    private Integer anatEntityRelationId;
    private String anatEntitySourceId;
    private String anatEntityTargetId;
    private String relationType;
    private String relationStatus;
    private Set<Integer> relationTaxonConstraints;
    private Set<Integer> sourceTaxonContraints;
    private Set<Integer> targetTaxonConstraints;

    public final static String AE_REL_ID = "anatEntityRelationId";
    public final static String AE_SOURCE_ID = "anatEntitySourceId";
    public final static String AE_TARGET_ID = "anatEntityTargetId";
    public final static String REL_TYPE = "relationType";
    public final static String REL_STATUS = "relationStatus";
    public final static String REL_TAXON_CSTRTS = "relationTaxonConstraints";
    public final static String SOURCE_TAXON_CSTRTS = "sourceTaxonContraints";
    public final static String TARGET_TAXON_CSTRTS = "targetTaxonConstraints";

    public int getAnatEntityRelationId() {
        return anatEntityRelationId;
    }

    public String getAnatEntitySourceId() {
        return anatEntitySourceId;
    }

    public String getAnatEntityTargetId() {
        return anatEntityTargetId;
    }

    public String getRelationType() {
        return relationType;
    }

    public String getRelationStatus() {
        return relationStatus;
    }

    public Set<Integer> getRelationTaxonConstraints() {
        return relationTaxonConstraints;
    }

    public Set<Integer> getSourceTaxonContraints() {
        return sourceTaxonContraints;
    }

    public Set<Integer> getTargetTaxonConstraints() {
        return targetTaxonConstraints;
    }

    public Mismatch() {
    }

    public Mismatch(int anatEntityRelationId, String anatEntitySourceId, String anatEntityTargetId, String relationType,
            String relationStatus, Set<Integer> relationTaxonConstraints, Set<Integer> sourceTaxonContraints,
            Set<Integer> targetTaxonConstraints) {
        this.anatEntityRelationId = anatEntityRelationId;
        this.anatEntitySourceId = anatEntitySourceId;
        this.anatEntityTargetId = anatEntityTargetId;
        this.relationType = relationType;
        this.relationStatus = relationStatus;
        this.relationTaxonConstraints = relationTaxonConstraints;
        this.sourceTaxonContraints = sourceTaxonContraints;
        this.targetTaxonConstraints = targetTaxonConstraints;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((anatEntityRelationId == null) ? 0 : anatEntityRelationId.hashCode());
        result = prime * result + ((anatEntitySourceId == null) ? 0 : anatEntitySourceId.hashCode());
        result = prime * result + ((anatEntityTargetId == null) ? 0 : anatEntityTargetId.hashCode());
        result = prime * result + ((relationType == null) ? 0 : relationType.hashCode());
        result = prime * result + ((relationStatus == null) ? 0 : relationStatus.hashCode());
        result = prime * result + ((relationTaxonConstraints == null) ? 0 : relationTaxonConstraints.hashCode());
        result = prime * result + ((sourceTaxonContraints == null) ? 0 : sourceTaxonContraints.hashCode());
        result = prime * result + ((targetTaxonConstraints == null) ? 0 : targetTaxonConstraints.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Mismatch other = (Mismatch) obj;
        if (anatEntityRelationId == null) {
            if (other.anatEntityRelationId != null) {
                return false;
            }
        } else if (!anatEntityRelationId.equals(other.anatEntityRelationId)) {
            return false;
        }
        if (anatEntitySourceId == null) {
            if (other.anatEntitySourceId != null) {
                return false;
            }
        } else if (!anatEntitySourceId.equals(other.anatEntitySourceId)) {
            return false;
        }
        if (anatEntityTargetId == null) {
            if (other.anatEntityTargetId != null) {
                return false;
            }
        } else if (!anatEntityTargetId.equals(other.anatEntityTargetId)) {
            return false;
        }
        if (relationType == null) {
            if (other.relationType != null) {
                return false;
            }
        } else if (!relationType.equals(other.relationType)) {
            return false;
        }
        if (relationStatus == null) {
            if (other.relationStatus != null) {
                return false;
            }
        } else if (!relationStatus.equals(other.relationStatus)) {
            return false;
        }
        if (relationTaxonConstraints == null) {
            if (other.relationTaxonConstraints != null) {
                return false;
            }
        } else if (!relationTaxonConstraints.equals(other.relationTaxonConstraints)) {
            return false;
        }
        if (sourceTaxonContraints == null) {
            if (other.sourceTaxonContraints != null) {
                return false;
            }
        } else if (!sourceTaxonContraints.equals(other.sourceTaxonContraints)) {
            return false;
        }
        if (targetTaxonConstraints == null) {
            if (other.targetTaxonConstraints != null) {
                return false;
            }
        } else if (!targetTaxonConstraints.equals(other.targetTaxonConstraints)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Mismatch [").append("anatEntityRelationId=").append(anatEntityRelationId)
                .append(", anatEntitySourceId=").append(anatEntitySourceId).append(", anatEntityTargetId=")
                .append(anatEntityTargetId).append(", relationType=").append(relationType).append(", relationStatus=")
                .append(relationStatus).append(", relationTaxonConstraints=").append(relationTaxonConstraints)
                .append(", sourceTaxonContraints=").append(sourceTaxonContraints).append(", targetTaxonConstraints=")
                .append(targetTaxonConstraints).append("]");
        return builder.toString();
    }

}
