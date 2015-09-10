package org.bgee.utils;

import junit.framework.Assert;
import org.bgee.model.species.Species;
import org.junit.Test;

/**
 * @author Philippe Moret
 */
public class JSHelperTest {
    @Test
    public void testSpeciesToJson() {
        Species species = new Species("12", "SpeciesName", "A string description of that species");

        String json = JSHelper.toJson(species);
        String expected = "{\n  \"id\": \"12\",\n  " +
                "\"name\": \"SpeciesName\",\n  " +
                "\"description\": \"A string description of that species\"\n}";
        Assert.assertEquals(expected,json);

        System.out.println(expected);
    }
}
