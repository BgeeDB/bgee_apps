package org.bgee.model.topanat;

//TODO Mathieu: to run ITs here, create some files in src/test/resources/topanat/ 
//(e.g., geneToOrgan file, etc), containing the information that will produce a real, 
//but predictable and fast to compute result. Launch TopAnatRManager and check the output 
//of the real analysis. No need to use TopAnatAnalysis to move the files, etc. 
//TODO Mathieu: do a test with a real analysis returning no results (but fast to compute), 
//to test the exception thrown when there are no results: rcaller.exception.ParseException, 
//with a message containing "Can not parse output: The generated file xxx is empty". This is important 
//for proper execution of TopAnatAnalysis
public class TopAnatRManagerIT {

}
