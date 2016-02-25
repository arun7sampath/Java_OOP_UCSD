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
	
	private Marker lastSelected;
	private Marker lastClicked;
	private AirportMarker tempAirport;
	
	public void setup() {
		// setting up PAppler
		size(800,600, OPENGL);
		
		// setting up map and default events
		map = new UnfoldingMap(this, 50, 50, 750, 550, new Google.GoogleMapProvider());
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
			AirportMarker m = new AirportMarker(feature);
	
			m.setRadius(5);
			m.setHidden(true);
			airportList.add(m);
			
			// put airport in hashmap with OpenFlights unique id for key
			airports.put(Integer.parseInt(feature.getId()), feature.getLocation());
			airportMarkers.put(Integer.parseInt(feature.getId()), m);
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
				route.addLocation(airports.get(source));
				route.addLocation(airports.get(dest));
			}
			
			SimpleLinesMarker sl = new SimpleLinesMarker(route.getLocations(), route.getProperties());
		
			//System.out.println(sl.getProperties());
			if(airportMarkers.containsKey(source) && airportMarkers.containsKey(dest)){
				tempAirport = (AirportMarker) airportMarkers.get(source);
				tempAirport.routes.add(sl);
			}
			//UNCOMMENT IF YOU WANT TO SEE ALL ROUTES
			routeList.add(sl);
		}
		
		
		
		//UNCOMMENT IF YOU WANT TO SEE ALL ROUTES
		//map.addMarkers(routeList);
		//map.addMarkers(countryMarkers);
		map.addMarkers(airportList);
		
	}
	
	public void draw() {
		background(0);
		markRoutesforAirports();
		map.draw();
	}
	
	/** Event handler that gets called automatically when the 
	 * mouse moves.
	 */
	@Override
	public void mouseMoved()
	{
		// clear the last selection
		if (lastSelected != null) {
			if(lastSelected == lastClicked)
				lastClicked = null;
			lastSelected.setSelected(false);
			lastSelected = null;
			for(Marker airport : airportList){
				airport.setHidden(true);
				airport.setSelected(false);
			}
		
		}
		for(Marker mark : countryMarkers){
			if(mark.isInside(map, mouseX, mouseY) == true){
				lastSelected = mark;
				lastSelected.setSelected(true);
				markAirportsbyCountry(mark, false);
			}
		}
		//loop();
	}
	
	/** Event handler that gets called automatically when the 
	 * mouse moves.
	 */
	@Override
	public void mouseClicked()
	{
		if(lastClicked == null){
			for(Marker mark : countryMarkers){
				if(mark.isInside(map, mouseX, mouseY) == true){
					lastClicked = mark;
				}
			}
				markAirportsbyCountry(lastClicked, true);
		}
		else{
			if(lastSelected == lastClicked)
				lastSelected = null;
			lastClicked.setSelected(false);
			lastClicked = null;
			for(Marker airport : airportList){
				airport.setHidden(true);
				airport.setSelected(false);
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
	
	private void markRoutesforAirports(){
		for(Marker airport : airportList){
			if(airport.isHidden() == false && airport.isSelected() == true){
				tempAirport = (AirportMarker)airport;
				for(SimpleLinesMarker s : tempAirport.routes){
					map.addMarkers(s);
				}
			}
		}
	}
}
