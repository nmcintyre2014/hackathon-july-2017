package io.geobigdata;

import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

public class MetadataParsingTest {

	@Test
	public void testMetadata() throws IOException, ParseException{
		
		// Get an input stream to the json metadata
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("metadata.json");
		
		// Create a json parser
		JSONParser parser = new JSONParser();
		
		// Parse the stream using the parser
		JSONObject obj = (JSONObject) parser.parse(new InputStreamReader(is));
		JSONObject georeferencing = (JSONObject) obj.get("imageGeoreferencing");
		Double scaleX = (Double) georeferencing.get("scaleX");
		
		
		
		//Example of getting one of the 6 parameters for the AffineTransform
		//double scaleX = 
		
		// Once you have all six of the param, you instantiate and AffineTransform like this
		//AffineTransform at = new AffineTransform(scaleX, shearY, shearX, scaleY, translateX, translateY);
		
		System.out.println("Scale X: "+scaleX);
	}
}
