package io.geobigdata;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;



import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;




import de.micromata.opengis.kml.v_2_2_0.Kml;

public class App 
{
	
	private Long xTiles;
	private Long yTiles;
	private Long sizeXTiles;
	private Long sizeYTiles;
	private Long minXTile;
	private Long minYTile;
	
	
	public Long getxTiles() {
		return xTiles;
	}

	public void setxTiles(Long xTiles) {
		this.xTiles = xTiles;
	}

	public Long getyTiles() {
		return yTiles;
	}

	public void setyTiles(Long yTiles) {
		this.yTiles = yTiles;
	}

	public double getSizeXTiles() {
		return sizeXTiles;
	}

	public void setSizeXTiles(Long sizeXTiles) {
		this.sizeXTiles = sizeXTiles;
	}

	public Long getSizeYTiles() {
		return sizeYTiles;
	}

	public void setSizeYTiles(Long sizeYTiles) {
		this.sizeYTiles = sizeYTiles;
	}

	public Long getMinXTile() {
		return minXTile;
	}

	public void setMinXTile(Long minXTile) {
		this.minXTile = minXTile;
	}

	public Long getMinYTile() {
		return minYTile;
	}

	public void setMinYTile(Long minYTile) {
		this.minYTile = minYTile;
	}
	
	public void setAllVars(JSONObject georeferencing){
		xTiles =  (Long) georeferencing.get("numXTiles");
		yTiles = (Long) georeferencing.get("numYTiles");
		sizeXTiles = (Long) georeferencing.get("tileXSize");
		sizeYTiles = (Long) georeferencing.get("tileXSize");
		minXTile = (Long) georeferencing.get("minTileX");
		minYTile = (Long) georeferencing.get("minTileY");
	}

	public AffineTransform setAffine(JSONObject georeferencing) throws IOException, ParseException{
		
		Double scaleX = (Double) georeferencing.get("scaleX");
		Double scaleY = (Double) georeferencing.get("scaleY");
		Long shearX = (Long) georeferencing.get("shearX");
		Long shearY = (Long) georeferencing.get("shearY");
		Double translateX = (Double) georeferencing.get("translateX");
		Double translateY = (Double) georeferencing.get("translateY");

		AffineTransform aT = new AffineTransform(scaleX, scaleY, shearX, shearY, translateX, translateY);
		
		return aT;
	}
	
		
	public Point2D[][] setCoordinates(JSONObject georeferencing){
		int second = (int) (xTiles * 1); 
		int first = (int) (yTiles * 1);
		Point2D.Double [][] pointArr = new Point2D.Double [first][second];
		
		for(int y = 0; y < pointArr.length; y++){
			Long temp = minXTile;
			for(int x = 0; x < pointArr[y].length; x++){
				pointArr[y][x] = new Point2D.Double(temp * sizeXTiles, minYTile * sizeYTiles);
				//System.out.println(pointArr[y][x].getY());
				temp++;
			}
			minYTile++;
		}
		return pointArr;
	}
	
	public Point2D[] transform(AffineTransform aT, Point2D[][] p2){
		Point2D[] p = new Point2D[(int) (xTiles * yTiles)];
		int count = 0;
		for(int i = 0; i < p2.length; i++){
			for(int j = 0; j < p2[i].length; j++){
				p[count] = aT.transform(p2[i][j], null);
				count++;
			}
		}
		return p;
	}
	
    public static void main( String[] args ) throws IOException, ParseException
    {
    	App a = new App();   	
    	InputStream is = a.getClass().getClassLoader().getResourceAsStream(args[0]);
        
		JSONParser jp = new JSONParser();
		
		JSONObject obj = (JSONObject) jp.parse(new InputStreamReader(is));
		JSONObject georeferencing = (JSONObject) obj.get("imageGeoreferencing"); //will this name change??
    	JSONObject metadata = (JSONObject) obj.get("imageMetadata");
		
		
		a.setAllVars(metadata);
		
		AffineTransform at = a.setAffine(georeferencing);   
    	Point2D[] p = new Point2D[(int) (a.getxTiles() * a.getyTiles())];
    	p = a.transform(at, a.setCoordinates(georeferencing));
    	
    	
    	final Kml kml = new Kml();
    	//how many place mark do we need?
    	for(int i = 0; i < p.length; i++){
    		kml.createAndSetPlacemark()
     	   .withName("London, UK"+i).withOpen(Boolean.TRUE)
     	   .createAndSetPoint().addToCoordinates(p[i].getX(), p[i].getY()); 
    		System.out.println("Added placemark...");
    	}
    	
    	kml.marshal(new File("HelloKml.kml"));
    	
    }

}
