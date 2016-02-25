package module6;

import java.util.ArrayList;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import processing.core.PConstants;
import processing.core.PGraphics;

/** 
 * A class to represent AirportMarkers on a world map.
 *   
 * @author Adam Setters and the UC San Diego Intermediate Software Development
 * MOOC team
 *
 */
public class AirportMarker extends CommonMarker {
	public List<SimpleLinesMarker> routes;
	
	public AirportMarker(Feature city ) {
		super(((PointFeature)city).getLocation(), city.getProperties());
		routes = new ArrayList<SimpleLinesMarker>();
	}
	
	@Override
	public void drawMarker(PGraphics pg, float x, float y) {
		pg.fill(11);
		pg.ellipse(x, y, 5, 5);
		
		
	}

	@Override
	public void showTitle(PGraphics pg, float x, float y) {
		 // show rectangle with title
		
		// show routes
		
		pg.pushStyle();
		/*pg.fill(255, 250, 240);
		pg.rectMode(PConstants.CORNER);
		pg.rect(x, y, pg.textWidth(getTitle()) + 5, 15);
		pg.fill(0, 0, 0);
		pg.textSize(10);
		pg.textAlign(PConstants.LEFT, PConstants.CENTER);
		pg.text(getTitle(), x, y + 5);
		*/
		pg.popStyle();
	}
	
	public String getName(){
		return (String)this.getProperty("name");
	}
	
	public String getCity(){
		return (String)this.getProperty("city");
	}
	
	public String getTitle() {
		return getName() + ", " + getCity();	
		
	}
	
}

