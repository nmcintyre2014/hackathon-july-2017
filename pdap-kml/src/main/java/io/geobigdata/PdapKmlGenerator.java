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
	
	public Kml createKml(InputStream metadataInputStream) throws IOException, ParseException{
		
		JSONParser jp = new JSONParser();
		
		JSONObject obj = (JSONObject) jp.parse(new InputStreamReader(metadataInputStream));
		JSONObject imageMetadata = (JSONObject) obj.get("imageMetadata");
		JSONObject georeferencing = (JSONObject) obj.get("imageGeoreferencing"); 
		
		
		Long xTiles =  (Long) imageMetadata.get("numXTiles");
		Long yTiles = (Long) imageMetadata.get("numYTiles");
		Long sizeXTiles = (Long) imageMetadata.get("tileXSize");
		Long sizeYTiles = (Long) imageMetadata.get("tileXSize");
		Long minXTile = (Long) imageMetadata.get("minTileX");
		Long minYTile = (Long) imageMetadata.get("minTileY");

		AffineTransform at = createAffineTransform(georeferencing);   
    	
    	final Kml kml = new Kml();
    	Document document = kml.createAndSetDocument();
    	
    	for(long tileY=minYTile;tileY<minYTile+yTiles;tileY++){
    		for(long tileX=minXTile;tileX<minXTile+xTiles;tileX++){
    			System.out.println(tileX+" :: "+tileY);
    			Double left = (double) (tileX*sizeXTiles);
    			Double right = left+sizeXTiles;
    			Double top = (double) (tileY*sizeYTiles);
    			Double bottom = top+sizeYTiles;
    			
    			Point2D.Double ul = (java.awt.geom.Point2D.Double) at.transform(new Point2D.Double(left, top), null);
    			Point2D.Double lr = (java.awt.geom.Point2D.Double) at.transform(new Point2D.Double(right,bottom), null);
    			
    			/*
    			Coordinate c = new Coordinate(ul.x, ul.y);
    			List<Coordinate> coords = new ArrayList<Coordinate>();
    			coords.add(c);
    			Placemark pm = document.createAndAddPlacemark();
    			pm.createAndSetPoint().withCoordinates(coords);
    			*/
    			
    			
    			LatLonBox bbox = new LatLonBox();
    			bbox.setWest(ul.getX());
    			bbox.setEast(lr.getX());
    			bbox.setNorth(ul.getY());
    			bbox.setSouth(lr.getY());
    			
    			System.out.println(bbox.getWest()+" :: "+bbox.getEast());
    			
    			int randomNum = ThreadLocalRandom.current().nextInt(0, 41 + 1);
    			
    			Icon catIcon = new Icon();
    			catIcon.setHref("http://fourier.eng.hmc.edu/e161/imagedata/CatsDogs/Cats/cat"+randomNum+".tif");
    			document.createAndAddGroundOverlay().withLatLonBox(bbox).withIcon(catIcon);
    			
    		}
    	}

    	return kml;
	}
	
    public static void main( String[] args ) throws IOException, ParseException
    {
    	PdapKmlGenerator a = new PdapKmlGenerator();   	
    	InputStream is = a.getClass().getClassLoader().getResourceAsStream(args[0]);
        
		
		Kml kml = a.createKml(is);
    	
    	System.out.println(kml.toString());
    	
    	//kml.marshal(System.out);
    	kml.marshal(new File("/tmp/HelloKml.kml"));
    	
    }

}
