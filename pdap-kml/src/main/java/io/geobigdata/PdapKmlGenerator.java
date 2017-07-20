package io.geobigdata;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;









import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;













import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Icon;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LatLonAltBox;
import de.micromata.opengis.kml.v_2_2_0.LatLonBox;
import de.micromata.opengis.kml.v_2_2_0.Placemark;

public class PdapKmlGenerator 
{
	
	public AffineTransform createAffineTransform(JSONObject georeferencing) {
		
		Double scaleX = (Double) georeferencing.get("scaleX");
		Double scaleY = (Double) georeferencing.get("scaleY");
		Long shearX = (Long) georeferencing.get("shearX");
		Long shearY = (Long) georeferencing.get("shearY");
		Double translateX = (Double) georeferencing.get("translateX");
		Double translateY = (Double) georeferencing.get("translateY");

		AffineTransform aT = new AffineTransform(scaleX, shearY, shearX, scaleY, translateX, translateY);
		
		return aT;
	}
	
	public Kml createKml(InputStream metadataInputStream, String token, String graphId, String nodeId) throws IOException, ParseException{
		
		JSONParser jp = new JSONParser();
		
		JSONObject obj = (JSONObject) jp.parse(new InputStreamReader(metadataInputStream));
		JSONObject imageMetadata = (JSONObject) obj.get("imageMetadata");
		JSONObject georeferencing = (JSONObject) obj.get("imageGeoreferencing"); 
		
		
		Long numXTiles =  (Long) imageMetadata.get("numXTiles");
		Long numYTiles = (Long) imageMetadata.get("numYTiles");
		Long tileXSize = (Long) imageMetadata.get("tileXSize");
		Long tileYSize = (Long) imageMetadata.get("tileXSize");
		Long minTileX = (Long) imageMetadata.get("minTileX");
		Long minTileY = (Long) imageMetadata.get("minTileY");

		AffineTransform at = createAffineTransform(georeferencing);   
    	
    	final Kml kml = new Kml();
    	Document document = kml.createAndSetDocument();
    	
    	for(long tileY=minTileY;tileY<minTileY+numYTiles;tileY++){
    		for(long tileX=minTileX;tileX<minTileX+numXTiles;tileX++){
    			Double left = (double) (tileX*tileXSize);
    			Double right = left+tileXSize;
    			Double top = (double) (tileY*tileYSize);
    			Double bottom = top+tileYSize;
    			
    			Point2D.Double ul = (java.awt.geom.Point2D.Double) at.transform(new Point2D.Double(left, top), null);
    			Point2D.Double lr = (java.awt.geom.Point2D.Double) at.transform(new Point2D.Double(right,bottom), null);
    			
    			LatLonBox bbox = new LatLonBox();
    			bbox.setWest(ul.getX());
    			bbox.setEast(lr.getX());
    			bbox.setNorth(ul.getY());
    			bbox.setSouth(lr.getY());
    			    			
    			int randomNum = ThreadLocalRandom.current().nextInt(0, 41 + 1);
    			
    			Icon catIcon = new Icon();
    			catIcon.setHref("https://idaho-api.geobigdata.io/v1/tile/"+graphId+"/"+nodeId+"/"+tileX+"/"+tileY+".tif?token="+token);
    			document.createAndAddGroundOverlay().withLatLonBox(bbox).withIcon(catIcon);
    			
    		}
    	}

    	return kml;
	}
	
    public static void main( String[] args ) throws IOException, ParseException
    {
    	PdapKmlGenerator a = new PdapKmlGenerator();   	
    	InputStream is = a.getClass().getClassLoader().getResourceAsStream(args[0]);
        
		
		//Kml kml = a.createKml(is, null);
    	
    	//System.out.println(kml.toString());
    	
    	//kml.marshal(System.out);
    	//kml.marshal(new File("/tmp/HelloKml.kml"));
    	
    }

}
