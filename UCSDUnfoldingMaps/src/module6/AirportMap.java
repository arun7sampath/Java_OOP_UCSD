package module6;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.data.ShapeFeature;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import de.fhpotsdam.unfolding.geo.Location;
import parsing.ParseFeed;
import processing.core.PApplet;

/** An applet that shows airports (and routes)
 * on a world map.  
 * @author Adam Setters and the UC San Diego Intermediate Software Development
 * MOOC team
 *
 */
public class AirportMap extends PApplet {
	
	UnfoldingMap map;
	private List<Marker> airportList;
	List<Marker> routeList;
	List<Feature> countries;
	List <Marker> countryMarkers;
	
	private AirportMarker lastSelected;
	private Marker lastClicked;
	
	public void setup() {
		// setting up PAppler
		AirportMarker m1, m2;
		Location srcLocation, dstLocation;
		
		size(800,600, OPENGL);
		
		// setting up map and default events
		map = new UnfoldingMap(this, 200, 50, 750, 600, new Google.GoogleMapProvider());
		MapUtils.createDefaultEventDispatcher(this, map);
		
		// Load country polygons and adds them as markers
		countries = GeoJSONReader.loadData(this, "countries.geo.json");
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		
		// get features from airport data
		List<PointFeature> features = ParseFeed.parseAirports(this, "airports.dat");
		
		// list for markers, hashmap for quicker access when matching with routes
		airportList = new ArrayList<Marker>();
		HashMap<Integer, Location> airports = new HashMap<Integer, Location>();
		HashMap<Integer, Marker> airportMarkers = new HashMap<Integer, Marker>();
		
		// create markers from features
		for(PointFeature feature : features) {
			m1 = new AirportMarker(feature);
	
			m1.setRadius(5);
			m1.setHidden(true);
			airportList.add(m1);
			
			// put airport in hashmap with OpenFlights unique id for key
			airports.put(Integer.parseInt(feature.getId()), feature.getLocation());
			airportMarkers.put(Integer.parseInt(feature.getId()), m1);
		}
		
		
		// parse route data
		List<ShapeFeature> routes = ParseFeed.parseRoutes(this, "routes.dat");
		routeList = new ArrayList<Marker>();
		for(ShapeFeature route : routes) {
			
			// get source and destination airportIds
			int source = Integer.parseInt((String)route.getProperty("source"));
			int dest = Integer.parseInt((String)route.getProperty("destination"));
			
			// get locations for airports on route
			if(airports.containsKey(source) && airports.containsKey(dest)) {
				srcLocation = airports.get(source);
				dstLocation = airports.get(dest);
				route.addLocation(srcLocation);
				route.addLocation(dstLocation);
				
				m1 = (AirportMarker)(airportMarkers.get(source));
				m2 = (AirportMarker)(airportMarkers.get(dest));
				
				m1.connectedCities.add(m2);
				m2.connectedCities.add(m1);
				
				SimpleLinesMarker sl = new SimpleLinesMarker(route.getLocations(), route.getProperties());
				sl.setHidden(true);
				
				m1.routes.add(sl);
				m2.routes.add(sl);
				
				//System.out.println(sl.getProperties());
				//UNCOMMENT IF YOU WANT TO SEE ALL ROUTES
				routeList.add(sl);	
			}
			
		}
		
		
		
		//UNCOMMENT IF YOU WANT TO SEE ALL ROUTES
		//map.addMarkers(countryMarkers);
		map.addMarkers(routeList);
		map.addMarkers(airportList);
		
	}
	
	public void draw() {
		background(125);
		map.draw();
		addKey();
	}
	
	/** Event handler that gets called automatically when the 
	 * mouse moves.
	 */
	@Override
	public void mouseClicked()
	{
		// clear the last selection
		if (lastClicked != null) {
			lastClicked = null;
			for(Marker airport : airportList){
				airport.setHidden(true);
				airport.setSelected(false);
			}
		
		}
		else{
			for(Marker mark : countryMarkers){
				if(mark.isInside(map, mouseX, mouseY) == true){
					lastClicked = mark;
					markAirportsbyCountry(mark, false);
					return;
				}
			}
		}
	}
	
	/** Event handler that gets called automatically when the 
	 * mouse moves.
	 */
	@Override
	public void mouseMoved()
	{
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected.drawRoutes(true);
			lastSelected = null;
		}
		for(Marker airport : airportList){
			if(airport.isInside(map, mouseX, mouseY) && 
					airport.isHidden() == false){
				lastSelected = (AirportMarker)airport;
//				lastSelected.setHidden(false);
				lastSelected.setSelected(true);
				lastSelected.selectConnectedCities(false);
				return;
			}
		}
	}
	

	private void markAirportsbyCountry(Marker country, boolean selected){
		
		for(Marker airport : airportList){
			String airCountry = airport.getProperty("country").toString();
			//airCountry = airCountry.replace("\"", "");
			if(country.getProperty("name").equals("United States of America") && 
					airCountry.equals("United States")){
				airport.setHidden(false);
				airport.setSelected(selected);
			}
			if(country.getProperty("name").equals(airCountry) == true){
				airport.setHidden(false);
				airport.setSelected(selected);
			}
		}	
	}
	
	// helper method to draw key in GUI
	private void addKey() {	
		// Remember you can use Processing's graphics methods here
		fill(255, 250, 240);

		int xbase = 25;
		int ybase = 50;

		rect(xbase, ybase, 150, 150);

		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Airport Key", xbase+35, ybase+25);
		text("Airport", xbase+40, ybase+55);
		text("Route", xbase+40, ybase+85);
		
		fill(0);
		ellipse(xbase+25, ybase+55, 7, 7);
		line(xbase+15, ybase+85, xbase+30, ybase+85);

	}
	
}
