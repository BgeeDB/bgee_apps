package org.bgee.view.csv;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.CommandRPackage;
import org.bgee.controller.RequestParameters;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationStatus;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.ontology.ElementRelation;
import org.bgee.model.species.Species;
import org.bgee.view.RPackageDisplay;
import org.bgee.view.ViewFactory;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.io.ICsvMapWriter;

public class CsvRPackageDisplay extends CsvParentDisplay implements RPackageDisplay {

	private final static Logger log = LogManager.getLogger(CsvSpeciesDisplay.class.getName());

	protected CsvRPackageDisplay(HttpServletResponse response, RequestParameters requestParameters, BgeeProperties prop,
			ViewFactory factory, Delimiter delimiter) throws IllegalArgumentException, IOException {
		super(response, requestParameters, prop, factory, delimiter);
	}

	@Override
	public void displayCalls(List<String> attrs, Stream<ExpressionCall> callsStream) {
		try (final ICsvMapWriter mapWriter = new CsvMapWriter(this.getOut(), this.csvPref)) {
			String[] header = attrs.stream().map(attr -> attr.toString()).toArray(String[]::new);
			this.startDisplay();
			mapWriter.writeHeader(header);
			for (ExpressionCall call : callsStream.collect(Collectors.toSet())) {
				int columnNumber = 0;
				final Map<String, Object> speMap = new HashMap<String, Object>();
				while (columnNumber < attrs.size()) {
					switch (attrs.get(columnNumber)) {
					case CommandRPackage.CALLS_GENE_ID_PARAM:
						speMap.put(header[columnNumber], call.getGene().getEnsemblGeneId());
						columnNumber++;
						break;
					case CommandRPackage.CALLS_ANAT_ENTITY_ID_PARAM:
						speMap.put(header[columnNumber], call.getCondition().getAnatEntityId());
						columnNumber++;
						break;
					case CommandRPackage.CALLS_DEV_STAGE_PARAM:
						speMap.put(header[columnNumber], call.getCondition().getDevStageId());
						columnNumber++;
						break;
					case CommandRPackage.CALLS_DATA_QUALITY_PARAM:
						speMap.put(header[columnNumber], call.getSummaryQuality());
						columnNumber++;
						break;
					default:
						throw log.throwing(new IllegalStateException("Unknow Attribut " + attrs.get(columnNumber)));
					}
				}
				mapWriter.write(speMap, header);
			}
			mapWriter.flush();
			this.endDisplay();
		} catch (IOException e) {
			log.catching(e);
			throw log.throwing(new IllegalStateException("Cannot write CSV response", e));
		}

	}

	@Override
	public void displayAnatEntities(List<String> attrs, Stream<AnatEntity> anatEntitiesStream) {
		log.entry(attrs, anatEntitiesStream);
		String[] header = attrs.stream().map(attr -> attr.toString()).toArray(String[]::new);
		try (final ICsvMapWriter mapWriter = new CsvMapWriter(this.getOut(), this.csvPref)) {
			this.startDisplay();
			mapWriter.writeHeader(header);
			for (AnatEntity anatEntity : anatEntitiesStream.collect(Collectors.toSet())) {
				int columnNumber = 0;
				final Map<String, Object> speMap = new HashMap<String, Object>();
				while (columnNumber < attrs.size()) {
					switch (attrs.get(columnNumber)) {
					case CommandRPackage.AE_ID_PARAM:
						speMap.put(header[columnNumber], anatEntity.getId());
						columnNumber++;
						break;
					case CommandRPackage.AE_NAME_PARAM:
						speMap.put(header[columnNumber], anatEntity.getName());
						columnNumber++;
						break;
					case CommandRPackage.AE_DESCRIPTION_PARAM:
						speMap.put(header[columnNumber], anatEntity.getDescription());
						columnNumber++;
						break;
					default:
						throw log.throwing(new IllegalStateException("Unknow Attribut " + attrs.get(columnNumber)));
					}
				}
				mapWriter.write(speMap, header);
			}
			mapWriter.flush();
			this.endDisplay();
		} catch (IOException e) {
			log.catching(e);
			throw log.throwing(new IllegalStateException("Cannot write CSV response", e));
		}

	}

	public void displaySpecies(List<String> attrs, List<Species> speciesList) {
		log.entry(attrs, speciesList);
		String[] header = attrs.stream().map(attr -> attr.toString()).toArray(String[]::new);

		try (final ICsvMapWriter mapWriter = new CsvMapWriter(this.getOut(), this.csvPref)) {
			this.startDisplay();
			mapWriter.writeHeader(header);
			for (Species species : speciesList) {
				int columnNumber = 0;
				final Map<String, Object> speMap = new HashMap<String, Object>();
				while (columnNumber < attrs.size()) {
					switch (attrs.get(columnNumber)) {
					case CommandRPackage.SPECIES_ID_PARAM:
						speMap.put(header[columnNumber], species.getId());
						columnNumber++;
						break;
					case CommandRPackage.SPECIES_GENUS_PARAM:
						speMap.put(header[columnNumber], species.getGenus());
						columnNumber++;
						break;
					case CommandRPackage.SPECIES_NAME_PARAM:
						speMap.put(header[columnNumber], species.getSpeciesName());
						columnNumber++;
						break;
					case CommandRPackage.SPECIES_COMMON_NAME_PARAM:
						speMap.put(header[columnNumber], species.getName());
						columnNumber++;
						break;
					case CommandRPackage.SPECIES_AFFYMETRIX_PARAM:
						if (species.getDataTypesByDataSourcesForData().values().stream().flatMap(dt -> dt.stream())
								.filter(dt -> dt.equals(DataType.AFFYMETRIX)).collect(Collectors.toSet()).size() > 0) {
							speMap.put(header[columnNumber], "T");
							columnNumber++;
							break;
						}
						speMap.put(header[columnNumber], "F");
						columnNumber++;
						break;
					case CommandRPackage.SPECIES_EST_PARAM:
						if (species.getDataTypesByDataSourcesForData().values().stream().flatMap(dt -> dt.stream())
								.filter(dt -> dt.equals(DataType.EST)).collect(Collectors.toSet()).size() > 0) {
							speMap.put(header[columnNumber], "T");
							columnNumber++;
							break;
						}
						speMap.put(header[columnNumber], "F");
						columnNumber++;
						break;
					case CommandRPackage.SPECIES_IN_SITU_PARAM:
						if (species.getDataTypesByDataSourcesForData().values().stream().flatMap(dt -> dt.stream())
								.filter(dt -> dt.equals(DataType.IN_SITU)).collect(Collectors.toSet()).size() > 0) {
							speMap.put(header[columnNumber], "T");
							columnNumber++;
							break;
						}
						speMap.put(header[columnNumber], "F");
						columnNumber++;
						break;

					case CommandRPackage.SPECIES_RNA_SEQ_PARAM:
						if (species.getDataTypesByDataSourcesForData().values().stream().flatMap(dt -> dt.stream())
								.filter(dt -> dt.equals(DataType.RNA_SEQ)).collect(Collectors.toSet()).size() > 0) {
							speMap.put(header[columnNumber], "T");
							columnNumber++;
							break;
						}
						speMap.put(header[columnNumber], "F");
						columnNumber++;
						break;
					default:
						throw log.throwing(new IllegalStateException("Unknow Attribut " + attrs.get(columnNumber)));
					}
				}
				mapWriter.write(speMap, header);
			}
			mapWriter.flush();
			this.endDisplay();
		} catch (IOException e) {
			log.catching(e);
			throw log.throwing(new IllegalStateException("Cannot write CSV response", e));
		}
	}

	@Override
	public void displayAERelations(List<String> attrs, Set<ElementRelation<String>> elementRelations) {
		log.entry(attrs, elementRelations);
		String[] header = attrs.stream().map(attr -> attr.toString()).toArray(String[]::new);
		try (final ICsvMapWriter mapWriter = new CsvMapWriter(this.getOut(), this.csvPref)) {
			this.startDisplay();
			mapWriter.writeHeader(header);
			for(ElementRelation<String> elementRelation : elementRelations){
				final Map<String, Object> speMap = new HashMap<String, Object>();
				int columnNumber = 0;
				while (columnNumber < attrs.size()) {
					switch (attrs.get(columnNumber)) {
					case CommandRPackage.RELATIONS_SOURCE_PARAM:
						speMap.put(header[columnNumber], elementRelation.getSourceId());
						columnNumber++;
						break;
					case CommandRPackage.RELATIONS_TARGET_PARAM:
						speMap.put(header[columnNumber], elementRelation.getTargetId());
						columnNumber++;
						break;
					case CommandRPackage.RELATION_TYPE_PARAM:
						speMap.put(header[columnNumber], elementRelation.getRelationType());
						columnNumber++;
						break;
					case CommandRPackage.RELATION_STATUS_PARAM:
						speMap.put(header[columnNumber], elementRelation.getRelationStatus());
						columnNumber++;
						break;
					default:
						throw log.throwing(new IllegalStateException("Unknow Attribut " + attrs.get(columnNumber)));
					}
				}
				mapWriter.write(speMap, header);
			}
			
//			for (String sourceId : anatEntityRelations.keySet()) {
//				Set<String> targetIds = anatEntityRelations.get(sourceId);
//				if( targetIds != null){
//					for(String targetId : targetIds){
//						int columnNumber = 0;
//						final Map<String, Object> speMap = new HashMap<String, Object>();
//						while (columnNumber < attrs.size()) {
//							switch (attrs.get(columnNumber)) {
//							case CommandRPackage.RELATIONS_SOURCE_PARAM:
//								speMap.put(header[columnNumber], sourceId);
//								columnNumber++;
//								break;
//							case CommandRPackage.RELATIONS_TARGET_PARAM:
//								speMap.put(header[columnNumber], targetId);
//								columnNumber++;
//								break;
//							case CommandRPackage.RELATION_TYPE_PARAM:
//								speMap.put(header[columnNumber], RelationType.ISA_PARTOF.getStringRepresentation());
//								columnNumber++;
//								break;
//							case CommandRPackage.RELATION_STATUS_PARAM:
//								speMap.put(header[columnNumber], RelationStatus.DIRECT.getStringRepresentation());
//								columnNumber++;
//								break;
//							default:
//								throw log.throwing(new IllegalStateException("Unknow Attribut " + attrs.get(columnNumber)));
//							}
//						}
//						mapWriter.write(speMap, header);
//					}
//					
//				}
//			}
			mapWriter.flush();
			this.endDisplay();
		} catch (IOException e) {
			log.catching(e);
			throw log.throwing(new IllegalStateException("Cannot write CSV response", e));
		}

	}
}
