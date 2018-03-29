package org.bgee.pipeline.bgeelight;

/**
 * The purpose of this class is to provide a mapping between the name of tables/columns of the Bgee database
 * and those of the Bgee Light database.
 * It will then be possible to change the name of the columns of the Bgee Light database.
 * Each column from Bgee having to be matched to a new column name in Bgee Light has to have a mapping methods in this class.
 * Practically, this mapping will be used to generate the name of the columns of the file containing data extracted from Bgee.
 * 
 * TODO: how to manage columns in Bgee light that will have no corresponding columns in Bgee?
 * For instance Bgee light v2 could have a "rank" column corresponding to merging of all datatype specific ranks in bgee
 * @author jwollbrett
 *
 */
public class BgeeToBgeeLightMapping {

}
