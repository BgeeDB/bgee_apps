package org.bgee.pipeline.uberon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.ontologycommon.OntologyUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CsvContext;


/**
 * Class dedicated to correction of XRefs and Equivalent classes of the UBERON ontology.
 * This step is mandatory before inserting the UBERON ontology in the the Bgee database.
 * Initially all these modifications were done manually by modifying the obo and owl
 * ontologies with text editors.
 * The new approach is to reference all modification in a text file and then use information
 * present in this file to automatically modify the ontology. It prevents typos in the ontologies
 * and allows to reuse corrections from previous Bgee release.

 * 
 * @author Julien Wollbrett
 * @version Bgee 15
 * @since Bgee 15
 */

public class CorrectXrefsAndEquivalentClass {
    
    private final static Logger log = LogManager.getLogger(CorrectXrefsAndEquivalentClass.class.getName());
    
    private static String XREF_ANNOTATION = "http://www.geneontology.org/formats/oboInOwl#hasDbXref";
    private static String CLASS_PREFIX = "http://purl.obolibrary.org/obo/";
    private static String NA = "NA";
    
    public CorrectXrefsAndEquivalentClass() {}
    
    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            throw log.throwing(new IllegalStateException(
                    "You must provide 4 arguments :\n"
                    + "1. path to the text file listing all modifications.\n"
                    + "2. path to the UBERON ontology you want to modify.\n"
                    + "3. path to the OWL ontology after correction.\n"
                    + "4. path to the OBO ontology after correction."));
        }
        
        //init variables
        String tsvInput = args[0];
        String inputOWLOntology = args[1];
        String outputOWLOntology = args[2];
        String outputOBOOntology = args[3];
        CorrectXrefsAndEquivalentClass correctOntology = new CorrectXrefsAndEquivalentClass();
        Set<XRefsAndEquivantClassBean> modifications = correctOntology.parseTextFile(tsvInput);
        
        // init manager and ontology
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(inputOWLOntology));
        OWLDataFactory df = manager.getOWLDataFactory();
                
        OWLAnnotationProperty xrefAnnotProperty = df.getOWLAnnotationProperty(IRI.create(XREF_ANNOTATION));
             
        //apply modifications
        for(XRefsAndEquivantClassBean modification : modifications){
            OWLClass classToModify = df.getOWLClass(
                    correctOntology.oboToOWLClassIRI(modification.classToModify));
            
            //check before modifications
            log.debug("class to modify : " + classToModify);
            
            // add new xrefs annotation property axioms
            if (modification.getXrefsToAdd() != null && !modification.getXrefsToAdd().isEmpty()) {
                for(String xrefToAdd : modification.getXrefsToAdd()) {
                    OWLAnnotation labelAnno = df.getOWLAnnotation(xrefAnnotProperty, 
                            df.getOWLLiteral(xrefToAdd));
                    OWLAxiom axiomToAdd = df.getOWLAnnotationAssertionAxiom(classToModify.getIRI(), 
                            labelAnno);
                    log.debug("add xref annotation axiom : " + axiomToAdd);
                    manager.addAxiom(ontology, axiomToAdd);
                }
            }
            
            // remove unwanted xrefs annotation property OR equivalent class axioms. 
            // Both are described in the same column of the mofification tsv file.
            //  is more tricky because
            // an equivalent class axiom can be defined by more than one 
            // triple (if restrictions applied to this equivalence)
            if (modification.getXrefsToRemove() != null && !modification.getXrefsToRemove().isEmpty()) {
                for(String xrefToRemove : modification.getXrefsToRemove()) {
                 // Depending on the version of OWLAPI OWLAnnotation can be created with or without a default datatype 
                    // (e.g ^^xsd:string). The ontology can potentially be updated with a version of OWLAPI different than
                    // the one the ontology was created with.
                    // Then, in order to be sure the XRef is properly removed we create 2 OWLAnnotation (with and without 
                    // datatype) and remove both of them.
                    
                    // generate OWLAxiom with xsd:string datatype
                    OWLAnnotation labelAnnoWithDatatype = df.getOWLAnnotation(xrefAnnotProperty, 
                            df.getOWLLiteral(xrefToRemove));
                    OWLAxiom axiomToRemoveWithDatatype = df.getOWLAnnotationAssertionAxiom(classToModify.getIRI(), 
                            labelAnnoWithDatatype);
                    
                    // generate OWLAxiom without xsd:string litteral
                    OWLAnnotation labelAnnoWithoutDatatype = df.getOWLAnnotation(xrefAnnotProperty, 
                            df.getOWLLiteral(xrefToRemove, ""));
                    OWLAxiom axiomToRemoveWithoutDatatype = df.getOWLAnnotationAssertionAxiom(classToModify.getIRI(), 
                            labelAnnoWithoutDatatype);
                    if (axiomToRemoveWithDatatype != null || axiomToRemoveWithoutDatatype != null) {
                        log.debug("removed xref annotation axiom : " + axiomToRemoveWithDatatype);
                        manager.removeAxiom(ontology, axiomToRemoveWithDatatype);
                        log.debug("removed xref annotation axiom : " + axiomToRemoveWithoutDatatype);
                        manager.removeAxiom(ontology, axiomToRemoveWithoutDatatype);
                    } else {
                        OWLClass equivalentClassToRemove = df.getOWLClass(
                            correctOntology.oboToOWLClassIRI(xrefToRemove));
                    
                        // retrieve all axioms of the class to modify
                        Set<OWLClassAxiom> equivalentClassesAxiom = ontology.getAxioms(classToModify);
                        for (OWLClassAxiom classAxiom : equivalentClassesAxiom) {
                            if (classAxiom.getAxiomType() == AxiomType.EQUIVALENT_CLASSES) {
                                if (classAxiom.containsEntityInSignature(equivalentClassToRemove)) {
                                    log.debug("equivalent axiom to remove : " + classAxiom);
                                    manager.removeAxiom(ontology, classAxiom);
                                }
                            }
                        }
                    }
                }
            }
            
//            // add equivalent class axioms
//            if (modification.getEquivalentToAdd() != null) {
//                for(String equivalentToAdd : modification.getEquivalentToAdd()) {
//                    OWLClass equivalentClassToAdd = df.getOWLClass(
//                            correctOntology.oboToOWLClassIRI(equivalentToAdd));
//                    OWLAxiom axiomToAdd = df.getOWLEquivalentClassesAxiom(
//                            classToModify, equivalentClassToAdd);
//                    log.debug("add equivalent class axiom : " + axiomToAdd);
//                    manager.addAxiom(ontology, axiomToAdd);
//                }
//            }
        }
        
        
        // save OBO and OWL ontologies
        manager.saveOntology(ontology, new RDFXMLDocumentFormat(),
                new FileOutputStream(outputOWLOntology));
        OntologyUtils ontologyUtils = new OntologyUtils(ontology);
        ontologyUtils.removeOBOProblematicAxioms();
        ontologyUtils.saveAsOBO(outputOBOOntology, false);
    }

    
    private Set<XRefsAndEquivantClassBean> parseTextFile(String pathToFile) throws Exception {
        log.traceEntry("{}", pathToFile);
        log.info("start parsing file {}", pathToFile, "containing all correction to apply.");
        Set<XRefsAndEquivantClassBean> lines = new HashSet<>();
        ICsvBeanReader beanReader = null;
        try {
            beanReader = new CsvBeanReader(new FileReader(pathToFile), CsvPreference.TAB_PREFERENCE);
                
            // the header elements are used to map the values to the bean (names must match)
            final String[] header = beanReader.getHeader(true);
            final CellProcessor[] processors = getCellProcessors();
                
            XRefsAndEquivantClassBean customer;
            while( (customer = beanReader.read(XRefsAndEquivantClassBean.class, header, processors)) != null ) {
                lines.add(customer);
            }
                
        } finally {
            if( beanReader != null ) {
                beanReader.close();
            }
        }
        return log.traceExit(lines);
    }
    
    /**
     * transform an OBO class id to the corresponding OWL class IRI
     * 
     * @param oboClassId String corresponding to wanted class ID in OBO format
     * @return
     */
    private IRI oboToOWLClassIRI(String oboClassId) {
        return IRI.create(CLASS_PREFIX+oboClassId.replace(":", "_"));
    }
    
    
    /**
     * Generate an array of CellProcesseur to parse the columns of the tsv into the right
     * type using the Super CSV framework. Except for the first column which contains 
     * one unique id all other columns can be null or contain more than one value.
     * 
     * @return an array of CellProcessor
     */
    public static CellProcessor[] getCellProcessors(){
        return new CellProcessor[] {
                new NotNull() {
                    // Check that only one uberon ID is defined
                    @Override
                    public Object execute(Object value, CsvContext context) {
                        String content = value.toString();
                        content.replaceAll("\\s","");
                        Set<String> split = new HashSet<String>(Arrays.asList(content.split(",")));
                        if (split.size() > 1) {
                            throw log.throwing(new IllegalArgumentException("first column must "
                                    + "contain only one uberon ID"));
                        }
                        return content;
                    }
                },
                new Optional() {
                    @Override
                    public Object execute(Object value, CsvContext context) {
                        if (value == null) {
                            return null;
                        }
                        String content = value.toString();
                        content.replaceAll("\\s","");
                        if (StringUtils.isBlank(content) || content.equalsIgnoreCase(NA)) {
                            return null;
                        }
                        Set<String> split = new HashSet<String>(Arrays.asList(content.split(",")));
                        return super.execute(split, context);
                    }
                },
                new Optional() {
                    @Override
                    public Object execute(Object value, CsvContext context) {
                        if (value == null) {
                            return null;
                        }
                        String content = value.toString();
                        content.replaceAll("\\s","");
                        if (StringUtils.isBlank(content) || content.equalsIgnoreCase(NA)) {
                            return null;
                        }
                        Set<String> split = new HashSet<String>(Arrays.asList(content.split(",")));
                        return super.execute(split, context);
                    }
                },
                new Optional() {
                    @Override
                    public Object execute(Object value, CsvContext context) {
                        if (value == null) {
                            return null;
                        }
                        String content = value.toString();
                        content.replaceAll("\\s","");
                        if (StringUtils.isBlank(content) || content.equalsIgnoreCase(NA)) {
                            return null;
                        }
                        Set<String> split = new HashSet<String>(Arrays.asList(content.split(",")));
                        return super.execute(split, context);
                    }
                },
                new Optional() {
                    @Override
                    public Object execute(Object value, CsvContext context) {
                        if (value == null) {
                            return null;
                        }
                        String content = value.toString();
                        content.replaceAll("\\s","");
                        if (StringUtils.isBlank(content) || content.equalsIgnoreCase(NA)) {
                            return null;
                        }
                        Set<String> split = new HashSet<String>(Arrays.asList(content.split(",")));
                        return super.execute(split, context);
                    }
                },
                new Optional() {
                    @Override
                    public Object execute(Object value, CsvContext context) {
                        if (value == null) {
                            return null;
                        }
                        return value.toString();
                    }
                },
                new Optional() {
                    @Override
                    public Object execute(Object value, CsvContext context) {
                        if (value == null) {
                            return null;
                        }
                        return value.toString();
                    }
                }
        };
    }
    
    
    /**
     * A bean representing a row of the file describing XRefs and
     * equivalent class relations to modify
     *
     * @author  Julien Wollbrett
     * @version Bgee 15, Oct. 2019
     * @since   Bgee 15
     */
    public static class XRefsAndEquivantClassBean {
        
        private String classToModify;
        private Set<String> xrefsToAdd;
        private Set<String> xrefsToRemove;
        private Set<String> equivalentToAdd;
        private Set<String> equivalentToRemove;
        private String mapppingAnnotationStatus;
        private String comments;
        
        public XRefsAndEquivantClassBean(){
            
        }
        
        public XRefsAndEquivantClassBean(String classToModify, Set<String> xrefsToAdd,
                Set<String> xrefsToRemove, Set<String> equivalentToAdd, 
                Set<String> equivalentToRemove, String mapppingAnnotationStatus, String comments){
            this.classToModify = classToModify;
            this.xrefsToAdd = xrefsToAdd;
            this.xrefsToRemove = xrefsToRemove;
            this.equivalentToAdd = equivalentToAdd;
            this.equivalentToRemove = equivalentToRemove;
            this.mapppingAnnotationStatus = mapppingAnnotationStatus;
            this.comments = comments;
        }
        
        public String getClassToModify() {
            return classToModify;
        }
        public void setClassToModify(String classToModify) {
            this.classToModify = classToModify;
        }
        public Set<String> getXrefsToAdd() {
            return xrefsToAdd;
        }
        public void setXrefsToAdd(Set<String> xrefsToAdd) {
            this.xrefsToAdd = xrefsToAdd;
        }
        public Set<String> getXrefsToRemove() {
            return xrefsToRemove;
        }
        public void setXrefsToRemove(Set<String> xrefsToRemove) {
            this.xrefsToRemove = xrefsToRemove;
        }
        public Set<String> getEquivalentToAdd() {
            return equivalentToAdd;
        }
        public void setEquivalentToAdd(Set<String> equivalentToAdd) {
            this.equivalentToAdd = equivalentToAdd;
        }
        public Set<String> getEquivalentToRemove() {
            return equivalentToRemove;
        }
        public void setEquivalentToRemove(Set<String> equivalentToRemove) {
            this.equivalentToRemove = equivalentToRemove;
        }
        public String getMapppingAnnotationStatus() {
            return mapppingAnnotationStatus;
        }
        public void setMapppingAnnotationStatus(String mapppingAnnotationStatus) {
            this.mapppingAnnotationStatus = mapppingAnnotationStatus;
        }
        public String getComments() {
            return comments;
        }
        public void setComments(String comments) {
            this.comments = comments;
        }

        

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((classToModify == null) ? 0 : classToModify.hashCode());
            result = prime * result + ((mapppingAnnotationStatus == null) ? 0 : mapppingAnnotationStatus.hashCode());
            result = prime * result + ((comments == null) ? 0 : comments.hashCode());
            result = prime * result + ((equivalentToAdd == null) ? 0 : equivalentToAdd.hashCode());
            result = prime * result + ((equivalentToRemove == null) ? 0 : equivalentToRemove.hashCode());
            result = prime * result + ((xrefsToAdd == null) ? 0 : xrefsToAdd.hashCode());
            result = prime * result + ((xrefsToRemove == null) ? 0 : xrefsToRemove.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            XRefsAndEquivantClassBean other = (XRefsAndEquivantClassBean) obj;
            if (classToModify == null) {
                if (other.classToModify != null)
                    return false;
            } else if (!classToModify.equals(other.classToModify))
                return false;
            if (mapppingAnnotationStatus == null) {
                if (other.mapppingAnnotationStatus != null)
                    return false;
            } else if (!mapppingAnnotationStatus.equals(other.mapppingAnnotationStatus))
                return false;
            if (comments == null) {
                if (other.comments != null)
                    return false;
            } else if (!comments.equals(other.comments))
                return false;
            if (equivalentToAdd == null) {
                if (other.equivalentToAdd != null)
                    return false;
            } else if (!equivalentToAdd.equals(other.equivalentToAdd))
                return false;
            if (equivalentToRemove == null) {
                if (other.equivalentToRemove != null)
                    return false;
            } else if (!equivalentToRemove.equals(other.equivalentToRemove))
                return false;
            if (xrefsToAdd == null) {
                if (other.xrefsToAdd != null)
                    return false;
            } else if (!xrefsToAdd.equals(other.xrefsToAdd))
                return false;
            if (xrefsToRemove == null) {
                if (other.xrefsToRemove != null)
                    return false;
            } else if (!xrefsToRemove.equals(other.xrefsToRemove))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "XRefsAndEquivantClassBean [classToModify=" + classToModify + ", xrefsToAdd=" + xrefsToAdd
                    + ", xrefsToRemove=" + xrefsToRemove + ", equivalentToAdd=" + equivalentToAdd
                    + ", equivalentToRemove=" + equivalentToRemove
                    + ", mapppingAnnotationStatus=" + mapppingAnnotationStatus
                    + ", comments=" + comments + "]";
        }

        
    
    }

}
