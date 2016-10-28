package com.siemens.citypulse.androidapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import citypulse.commons.contextual_filtering.city_event_ontology.CityEvent;
import citypulse.commons.contextual_filtering.contextual_event_request.Place;
import citypulse.commons.contextual_filtering.contextual_event_request.PlaceAdapter;
import citypulse.commons.data.Coordinate;
import citypulse.commons.reasoning_request.ARType;
import citypulse.commons.reasoning_request.Answer;
import citypulse.commons.reasoning_request.ReasoningRequest;
import citypulse.commons.reasoning_request.User;
import citypulse.commons.reasoning_request.concrete.AnswerAdapter;
import citypulse.commons.reasoning_request.concrete.FunctionalConstraintValueAdapter;
import citypulse.commons.reasoning_request.concrete.FunctionalParameterValueAdapter;
import citypulse.commons.reasoning_request.concrete.IntegerFunctionalConstraintValue;
import citypulse.commons.reasoning_request.concrete.StringFunctionalParameterValue;
import citypulse.commons.reasoning_request.functional_requirements.FunctionalConstraint;
import citypulse.commons.reasoning_request.functional_requirements.FunctionalConstraintName;
import citypulse.commons.reasoning_request.functional_requirements.FunctionalConstraintOperator;
import citypulse.commons.reasoning_request.functional_requirements.FunctionalConstraintValue;
import citypulse.commons.reasoning_request.functional_requirements.FunctionalConstraints;
import citypulse.commons.reasoning_request.functional_requirements.FunctionalDetails;
import citypulse.commons.reasoning_request.functional_requirements.FunctionalParameter;
import citypulse.commons.reasoning_request.functional_requirements.FunctionalParameterName;
import citypulse.commons.reasoning_request.functional_requirements.FunctionalParameterValue;
import citypulse.commons.reasoning_request.functional_requirements.FunctionalParameters;
import citypulse.commons.reasoning_request.functional_requirements.FunctionalPreference;
import citypulse.commons.reasoning_request.functional_requirements.FunctionalPreferenceOperation;
import citypulse.commons.reasoning_request.functional_requirements.FunctionalPreferences;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.siemens.citypulse.androidapp.autocompletetext.GeocodeJSONParser;
import com.siemens.citypulse.androidapp.autocompletetext.PlaceDetailsJSONParser;
import com.siemens.citypulse.androidapp.autocompletetext.SimpleGeocodeJSONParser;
import com.siemens.citypulse.androidapp.common.ApplicationExecutionConditions;
import com.siemens.citypulse.androidapp.common.DefaultValues;
import com.siemens.citypulse.androidapp.decisionsupport.ReRouteReasoningRequest;
import com.siemens.citypulse.webSockets.WebSocketBasicClient;

public class TravelPlannerActivity extends Fragment implements LocationListener {

	public static CityEvent lastCityEvent = null;
	public static Long lastCityEventTimestamp = null;

	private LatLng destinationPoint = null;
	// private LatLng startingPoint = null;
	private LatLng interestPoint = null;

	private Location lastLocation = null;

	private String transportationType = DefaultValues.CAR_TRANSPORTATION_TYPE;

	private String routeReasoningRequest = "";

	private AutoCompleteTextView endPointTextField;

	private SupportMapFragment travelPlannerSupportMapFragment;
	private GoogleMap map;

	private AutocompleteGeoLocationDownloadTask placesDownloadTask;
	private AutocompleteGeoLocationDownloadTask placeDetailsDownloadTask;
	private AutocompleteGeoLocationParserTask placesParserTask;
	private AutocompleteGeoLocationParserTask placeDetailsParserTask;
	

	private Marker destinationPointMarkerOnMap = null;
	private Marker interestPointMarkerOnMap = null;
	private Marker userPositionMarker;

	private Button carButton;
	private Button walkButton;
	private Button bicycleButton;

	private Boolean locationAvailable;

	final static int BUTTON_SELECTED_COLOR = Color.CYAN;
	final static int BUTTON_NOT_SELECTED_COLOR = Color.BLACK;

	final static int PLACES = 0;
	final static int PLACES_DETAILS = 1;

	private LocationManager locationManager;

	private BroadcastReceiver broadcastReceiver;
	private Bundle requestBundle = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View travelPlannerView = inflater.inflate(R.layout.travel_planner_frag,
				container, false);

		carButton = (Button) travelPlannerView.findViewById(R.id.carButton);
		carButton.setTextColor(BUTTON_SELECTED_COLOR);

		carButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				carButton.setTextColor(BUTTON_SELECTED_COLOR);
				walkButton.setTextColor(BUTTON_NOT_SELECTED_COLOR);
				bicycleButton.setTextColor(BUTTON_NOT_SELECTED_COLOR);

				transportationType = DefaultValues.CAR_TRANSPORTATION_TYPE;

			}
		});

		walkButton = (Button) travelPlannerView.findViewById(R.id.walkButton);
		walkButton.setTextColor(BUTTON_NOT_SELECTED_COLOR);

		walkButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				carButton.setTextColor(BUTTON_NOT_SELECTED_COLOR);
				walkButton.setTextColor(BUTTON_SELECTED_COLOR);
				bicycleButton.setTextColor(BUTTON_NOT_SELECTED_COLOR);

				transportationType = DefaultValues.WALK_TRANSPORTATION_TYPE;

			}
		});

		bicycleButton = (Button) travelPlannerView
				.findViewById(R.id.bicicleButton);
		bicycleButton.setTextColor(BUTTON_NOT_SELECTED_COLOR);

		bicycleButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				carButton.setTextColor(BUTTON_NOT_SELECTED_COLOR);
				walkButton.setTextColor(BUTTON_NOT_SELECTED_COLOR);
				bicycleButton.setTextColor(BUTTON_SELECTED_COLOR);

				transportationType = DefaultValues.BICYCLE_TRANSPORTATION_TYPE;

			}
		});

		Button travelPlannerConstraintsButton = (Button) travelPlannerView
				.findViewById(R.id.TravelPlannerConstraints);

		travelPlannerConstraintsButton
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						Intent intent = new Intent(getActivity(),
								TravelPlannerConstraintsActivity.class);
						startActivityForResult(
								intent,
								DefaultValues.TRAVEL_PLANNER_CONSTRAINTS_REQUEST_CODE);

					}
				});

		Button travelPlannerOKButton = (Button) travelPlannerView
				.findViewById(R.id.travelPlannerOKButton);

		travelPlannerOKButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!ApplicationExecutionConditions
						.isGPSandInternetSignal(getActivity())) {
					return;
				}

				if (destinationPoint == null) {
					Toast.makeText(getActivity(),
							"Please select a destination point",
							Toast.LENGTH_SHORT).show();
					return;
				}

				if (lastLocation == null) {
					Toast.makeText(getActivity(), "There is no GPS coverage.",
							Toast.LENGTH_SHORT).show();
					return;
				}

				SendMessageToServer sendMessageTask = new SendMessageToServer();
				sendMessageTask.execute();

				Toast.makeText(
						getActivity(),
						"Please wait until the system computes the best routes to travel.",
						Toast.LENGTH_LONG).show();

			}
		});

		endPointTextField = (AutoCompleteTextView) travelPlannerView
				.findViewById(R.id.travelPlannerEndPointTextField);

		endPointTextField.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				placesDownloadTask = new AutocompleteGeoLocationDownloadTask(
						PLACES);

				// Getting url to the Google Places Autocomplete api
				String url = getAutoCompleteUrl(s.toString());

				// Start downloading Google Places
				// This causes to execute doInBackground() of DownloadTask class
				placesDownloadTask.execute(url);

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});

		endPointTextField.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				ListView lv = (ListView) parent;
				SimpleAdapter adapter = (SimpleAdapter) lv.getAdapter();

				HashMap<String, String> hm = (HashMap<String, String>) adapter
						.getItem(position);

				// Creating a DownloadTask to download Places details of the
				// selected place
				placeDetailsDownloadTask = new AutocompleteGeoLocationDownloadTask(
						PLACES_DETAILS);

				// Getting url to the Google Places details api
				String url = getPlaceDetailsUrl(hm.get("reference"));

				// Start downloading Google Place Details
				// This causes to execute doInBackground() of DownloadTask class
				placeDetailsDownloadTask.execute(url);

			}
		});

		endPointTextField
				.setOnEditorActionListener(new OnEditorActionListener() {

					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {

						if (!ApplicationExecutionConditions
								.isInternetSignal(getActivity())) {
							return false;
						}

						if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
								|| (actionId == EditorInfo.IME_ACTION_DONE)) {

							String location = endPointTextField.getText()
									.toString();

							if (location == null || location.equals("")) {
								Toast.makeText(getActivity(),
										"No Place is entered",
										Toast.LENGTH_SHORT).show();
								return false;
							}

							String url = "https://maps.googleapis.com/maps/api/geocode/json?";

							try {
								// encoding special characters like space in the
								// user input
								// place
								location = URLEncoder.encode(location, "utf-8");
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}

							String address = "address=" + location;

							String sensor = "sensor=false";

							// url , from where the geocoding data is fetched
							url = url + address + "&" + sensor;

							SimpleGeolocationDownloadTask simpleGeolocationDownloadTask = new SimpleGeolocationDownloadTask();

							simpleGeolocationDownloadTask.execute(url);

						}
						return false;
					}
				});

		locationManager = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				1000, 10, this);

		broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				requestBundle = intent
						.getBundleExtra(DefaultValues.COMMMAND_BUNDLE);

				destinationPoint = new Gson().fromJson(
						requestBundle.getString(Execution.DESTINATION_POINT),
						LatLng.class);

				interestPoint = new Gson().fromJson(
						requestBundle.getString(Execution.INTEREST_POINT),
						LatLng.class);

				char[] initialText = ("Parking place: " + destinationPoint
						.toString()).toCharArray();

				endPointTextField.setText(initialText, 0, initialText.length);

				if (destinationPoint != null){
					
					if(destinationPointMarkerOnMap!=null){
					destinationPointMarkerOnMap.remove();
				}
				
				destinationPointMarkerOnMap =map.addMarker(new MarkerOptions()
							.position(destinationPoint).title(
									"Destination point"));
				}

				if (interestPoint != null) {
					
				if(interestPointMarkerOnMap!=null){
					interestPointMarkerOnMap.remove();
				}
				
				interestPointMarkerOnMap = map.addMarker(new MarkerOptions()
							.title("Point of interest")
							.position(interestPoint)
							.icon(BitmapDescriptorFactory
									.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
				}

				LatLng latLng = new LatLng(lastLocation.getLatitude(),
						lastLocation.getLongitude());
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));

			}
		};

		IntentFilter intentFilter = new IntentFilter();
		intentFilter
				.addAction(ParkingPlaceSelectionActivity.GO_TO_ROUTE_SELECTION);
		getActivity().registerReceiver(broadcastReceiver, intentFilter);

		return travelPlannerView;
	}

	@Override
	public void onDestroyView() {

		getActivity().unregisterReceiver(broadcastReceiver);

		super.onDestroyView();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		FragmentManager fm = getChildFragmentManager();
		travelPlannerSupportMapFragment = (SupportMapFragment) fm
				.findFragmentById(R.id.travelPlannerFragmentMap);
		if (travelPlannerSupportMapFragment == null) {
			travelPlannerSupportMapFragment = SupportMapFragment.newInstance();
			fm.beginTransaction()
					.replace(R.id.travelPlannerFragmentMap,
							travelPlannerSupportMapFragment).commit();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		locationAvailable = false;

		if (map == null) {
			map = travelPlannerSupportMapFragment.getMap();

		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == DefaultValues.TRAVEL_PLANNER_CONSTRAINTS_REQUEST_CODE) {

			if (resultCode == TravelPlannerConstraintsActivity.RESULT_OK) {

				Bundle extras = data.getExtras();
				Boolean shortest = extras.getBoolean("shortest");
				Boolean fastest = extras.getBoolean("fastest");
				Boolean cleaner = extras.getBoolean("cleaner");
				Toast.makeText(
						getActivity(),
						"Cheapest: " + shortest + "\n Shortest Walk: "
								+ fastest + "\n Cleaner: " + cleaner,
						Toast.LENGTH_LONG).show();
			}

			if (resultCode == TravelPlannerConstraintsActivity.RESULT_CANCELED) {
			}

		}
	}

	private String genereateTravelPlannerRequest() {

		// create the request object
		FunctionalParameters requestFunctionalParameters = new FunctionalParameters();

		Coordinate travelPlannerStartingPointCoordinate = new Coordinate(
				lastLocation.getLongitude(), lastLocation.getLatitude());

		Coordinate destinationPointCoordinate = new Coordinate(
				destinationPoint.longitude, destinationPoint.latitude);

		requestFunctionalParameters
				.addFunctionalParameter(new FunctionalParameter(
						FunctionalParameterName.STARTING_POINT,
						new StringFunctionalParameterValue(
								travelPlannerStartingPointCoordinate.toString())));

		requestFunctionalParameters
				.addFunctionalParameter(new FunctionalParameter(
						FunctionalParameterName.ENDING_POINT,
						new StringFunctionalParameterValue(
								destinationPointCoordinate.toString())));

		requestFunctionalParameters
				.addFunctionalParameter(new FunctionalParameter(
						FunctionalParameterName.TRANSPORTATION_TYPE,
						new StringFunctionalParameterValue(transportationType)));

		requestFunctionalParameters
				.addFunctionalParameter(new FunctionalParameter(
						FunctionalParameterName.STARTING_DATETIME,
						new StringFunctionalParameterValue(new Long(System
								.currentTimeMillis()).toString())));

		FunctionalConstraints functionalConstraints = new FunctionalConstraints();
		functionalConstraints.addFunctionalConstraint(new FunctionalConstraint(
				FunctionalConstraintName.POLLUTION,
				FunctionalConstraintOperator.LESS_THAN,
				new IntegerFunctionalConstraintValue(135)));

		FunctionalPreferences requestFunctionalPreferences = new FunctionalPreferences();

		SharedPreferences routeConstraintsPreferences = getActivity()
				.getSharedPreferences("RouteConstraintsPreferences",
						getActivity().MODE_PRIVATE);
		final Editor routeConstraintsEditor = routeConstraintsPreferences
				.edit();

		boolean fastestRestoredRestoredCheckBox = routeConstraintsPreferences
				.getBoolean("fastestCheckBox", false);
		boolean shortestRestoredCheckBox = routeConstraintsPreferences
				.getBoolean("shortestCheckBox", false);
		boolean cleanerRestoredCheckBox = routeConstraintsPreferences
				.getBoolean("cleanerCheckBox", false);

		if (fastestRestoredRestoredCheckBox)
			requestFunctionalPreferences
					.addFunctionalPreference(new FunctionalPreference(1,
							FunctionalPreferenceOperation.MINIMIZE,
							FunctionalConstraintName.TIME));

		if (shortestRestoredCheckBox)
			requestFunctionalPreferences
					.addFunctionalPreference(new FunctionalPreference(2,
							FunctionalPreferenceOperation.MINIMIZE,
							FunctionalConstraintName.DISTANCE));

		if (cleanerRestoredCheckBox)
			requestFunctionalPreferences
					.addFunctionalPreference(new FunctionalPreference(3,
							FunctionalPreferenceOperation.MINIMIZE,
							FunctionalConstraintName.POLLUTION));

		ReasoningRequest reasoningRequest = new ReasoningRequest(new User(),
				ARType.TRAVEL_PLANNER, new FunctionalDetails(
						requestFunctionalParameters, functionalConstraints,
						requestFunctionalPreferences));

		if ((lastCityEvent != null)
				&& (lastCityEventTimestamp != null)
				&& (System.currentTimeMillis() - lastCityEventTimestamp < 300000)) {

			ReRouteReasoningRequest reRouteReasoningRequest = new ReRouteReasoningRequest(
					reasoningRequest, lastCityEvent);

			final GsonBuilder builderReroute = new GsonBuilder();
			builderReroute.registerTypeAdapter(FunctionalParameterValue.class,
					new FunctionalParameterValueAdapter());
			builderReroute.registerTypeAdapter(FunctionalConstraintValue.class,
					new FunctionalConstraintValueAdapter());
			builderReroute.registerTypeAdapter(Place.class, new PlaceAdapter());

			final Gson gsonReroute = builderReroute.create();

			return gsonReroute.toJson(reRouteReasoningRequest);
		} else{
			GsonBuilder builder = new GsonBuilder();

			builder.registerTypeAdapter(FunctionalParameterValue.class,
					new FunctionalParameterValueAdapter());
			builder.registerTypeAdapter(FunctionalConstraintValue.class,
					new FunctionalConstraintValueAdapter());
			builder.registerTypeAdapter(Answer.class, new AnswerAdapter());

			Gson gson = builder.create();
			
			return gson.toJson(reasoningRequest);
		}
			

	}

	private class SendMessageToServer extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {

			routeReasoningRequest = genereateTravelPlannerRequest();

			SharedPreferences settingsPreferences = getActivity()
					.getSharedPreferences("SettingsPreferences",
							Context.MODE_PRIVATE);

			String requestEndpoint = DefaultValues.getEndPointForIP(
					DefaultValues.REASONING_REQUEST_WEB_SOCKET_END_POINT,
					settingsPreferences.getString("serverLocation",
							DefaultValues.WEB_SOCKET_SERVER_IP));

			System.out
					.println("Travel module: the following request was sent to the decision support ( requestEndpoint ) "
							+ routeReasoningRequest);

			WebSocketBasicClient webSocketBasicClient = new WebSocketBasicClient(
					requestEndpoint, routeReasoningRequest);

			String routeReasoningResponse = webSocketBasicClient
					.sendWebsocketRequest();

			// String routeReasoningResponse =
			// "[{\"route\":[{\"longitude\":10.1542546,\"latitude\":},{\"longitude\":10.1544869,\"latitude\":56.2104016},{\"longitude\":10.1550011,\"latitude\":56.2102371},{\"longitude\":10.1550785,\"latitude\":56.2101647},{\"longitude\":10.1550606,\"latitude\":56.2100706}],\"length\":1200,\"number_of_seconds\":15000,\"arType\":\"TRAVEL_PLANNER\"},{\"route\":[{\"longitude\":10.1543473,\"latitude\":56.2077227},{\"longitude\":10.1543388,\"latitude\":56.2076823},{\"longitude\":10.1543326,\"latitude\":56.2076354},{\"longitude\":10.1543366,\"latitude\":56.2075876},{\"longitude\":10.1543478,\"latitude\":56.2075505},{\"longitude\":10.1543629,\"latitude\":56.2075121}],\"length\":1700,\"number_of_seconds\":12000,\"arType\":\"TRAVEL_PLANNER\"}]";

			System.out
					.println("Travel module       : the following respose was received from the decision support "
							+ routeReasoningResponse);

			if (routeReasoningResponse == null) {
				System.out
						.println("The routeReasoningResponse message is null");

				Intent intent = new Intent(getActivity(), ErrorPanel.class);

				intent.putExtra(
						"Error",
						"The route recomendation message is null. Please check if "
								+ "the IP of the CityPulse framework is correct (in Settings tab) and if "
								+ "the decisions support component is running. The current decision "
								+ "support endpoint is " + requestEndpoint);
				startActivity(intent);

			} else if (routeReasoningResponse.equals("{\"answers\":[]}")) {
				System.out
						.println("The decision support provided an empty answer.");

				Intent intent = new Intent(getActivity(), ErrorPanel.class);

				intent.putExtra(
						"Error",
						"The decision support component is working but is not able to provide a recomendation. The current decision "
								+ "support endpoint is " + requestEndpoint);
				startActivity(intent);

			} else {



				Intent intent = new Intent(getActivity(),
						TravelPlannerRouteSelection.class);

				if (requestBundle == null) {
					requestBundle = new Bundle();
					requestBundle
							.putString(Execution.STARTING_POINT, new Gson()
									.toJson(new LatLng(lastLocation
											.getLatitude(), lastLocation
											.getLongitude())));
					

				}
				
				requestBundle.putString(Execution.DESTINATION_POINT,
							new Gson().toJson(destinationPoint));

				requestBundle.putString(
						Execution.DECISION_SUPPORT_TRAVEL_PLANNER_RESONSE,
						routeReasoningResponse);
				requestBundle.putString(
						Execution.DECISION_SUPPORT_TRAVEL_PLANNER_REQUEST,
						routeReasoningRequest);

				intent.putExtra(Execution.EXECUTION_DETAILS, requestBundle);
				startActivity(intent);

			}

			return null;
		}

	}

	@Override
	public void onLocationChanged(Location location) {
		lastLocation = location;

		LatLng latLng = new LatLng(location.getLatitude(),
				location.getLongitude());

		if (locationAvailable == false) {

			map.clear();

			userPositionMarker = map.addMarker(new MarkerOptions()
					.position(latLng)
					.title("my position")
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.user_position_marker)));

			map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));

			if (destinationPoint != null){
				if(destinationPointMarkerOnMap!=null){
					destinationPointMarkerOnMap.remove();
				}
				
				destinationPointMarkerOnMap = map.addMarker(new MarkerOptions().position(destinationPoint)
						.title(Execution.DESTINATION_POINT));
			}

		} else {
			userPositionMarker.setPosition(latLng);
		}

		locationAvailable = true;

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	private Marker dislayMarkerAtLocationAndZoomIn(LatLng location) {

		CameraUpdate cameraPosition = CameraUpdateFactory.newLatLng(location);
		CameraUpdate cameraZoom = CameraUpdateFactory.zoomBy(5);

		// Showing the user input location in the Google Map
		map.moveCamera(cameraPosition);
		map.animateCamera(cameraZoom);

		MarkerOptions options = new MarkerOptions();
		options.position(location);
		options.title("Position");
		options.snippet("Latitude:" + location + ",Longitude:" + location);

		// Adding the marker in the Google Map
		Marker marker = map.addMarker(options);

		map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10));
		
		return marker;
	}

	// =================Classes and methods used for auto complete
	// field=========================

	private String downloadUrl(String strUrl) throws IOException {
		String data = "";
		InputStream iStream = null;
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(strUrl);
			// Creating an http connection to communicate with url
			urlConnection = (HttpURLConnection) url.openConnection();

			// Connecting to url
			urlConnection.connect();

			// Reading data from url
			iStream = urlConnection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					iStream));

			StringBuffer sb = new StringBuffer();

			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			data = sb.toString();
			br.close();

		} catch (Exception e) {
		} finally {
			iStream.close();
			urlConnection.disconnect();
		}

		return data;
	}

	private String getAutoCompleteUrl(String place) {

		// Obtain browser key from https://code.google.com/apis/console
		String key = "key=AIzaSyBZnohTsdOvK3WEuS3C549ml43v48bG5YI";

		// place to be be searched
		String input = "input=" + place;

		// place type to be searched
		String types = "types=geocode";

		// Sensor enabled
		String sensor = "sensor=false";

		// Building the parameters to the web service
		String parameters = input + "&" + types + "&" + sensor + "&" + key;

		// Output format
		String output = "json";

		// Building the url to the web service
		String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"
				+ output + "?" + parameters;

		return url;
	}

	private String getPlaceDetailsUrl(String ref) {

		// Obtain browser key from https://code.google.com/apis/console
		String key = "key=AIzaSyBZnohTsdOvK3WEuS3C549ml43v48bG5YI";

		// reference of place
		String reference = "reference=" + ref;

		// Sensor enabled
		String sensor = "sensor=false";

		// Building the parameters to the web service
		String parameters = reference + "&" + sensor + "&" + key;

		// Output format
		String output = "json";

		// Building the url to the web service
		String url = "https://maps.googleapis.com/maps/api/place/details/"
				+ output + "?" + parameters;

		return url;
	}

	/** A class, to download Places from Geocoding webservice */
	private class AutocompleteGeoLocationDownloadTask extends
			AsyncTask<String, Void, String> {

		private int downloadType = 0;

		// Constructor
		public AutocompleteGeoLocationDownloadTask(int type) {
			this.downloadType = type;
		}

		@Override
		protected String doInBackground(String... url) {

			// For storing data from web service
			String data = "";

			try {
				// Fetching the data from web service
				data = downloadUrl(url[0]);
			} catch (Exception e) {
			}
			return data;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			switch (downloadType) {
			case PLACES:
				// Creating ParserTask for parsing Google Places
				placesParserTask = new AutocompleteGeoLocationParserTask(PLACES);

				// Start parsing google places json data
				// This causes to execute doInBackground() of ParserTask class

				if (result != "")
					placesParserTask.execute(result);
				else
					System.out.println("The result is empty");

				break;

			case PLACES_DETAILS:
				// Creating ParserTask for parsing Google Places
				placeDetailsParserTask = new AutocompleteGeoLocationParserTask(
						PLACES_DETAILS);

				// Starting Parsing the JSON string
				// This causes to execute doInBackground() of ParserTask class
				placeDetailsParserTask.execute(result);
			}
		}
	}

	/** A class to parse the Geocoding Places in non-ui thread */
	private class AutocompleteGeoLocationParserTask extends
			AsyncTask<String, Integer, List<HashMap<String, String>>> {

		int parserType = 0;

		public AutocompleteGeoLocationParserTask(int type) {
			this.parserType = type;
		}

		@Override
		protected List<HashMap<String, String>> doInBackground(
				String... jsonData) {

			JSONObject jObject;
			List<HashMap<String, String>> list = null;

			try {
				jObject = new JSONObject(jsonData[0]);

				switch (parserType) {
				case PLACES:
					GeocodeJSONParser placeJsonParser = new GeocodeJSONParser();
					// Getting the parsed data as a List construct
					list = placeJsonParser.parse(jObject);
					break;
				case PLACES_DETAILS:
					PlaceDetailsJSONParser placeDetailsJsonParser = new PlaceDetailsJSONParser();
					// Getting the parsed data as a List construct
					list = placeDetailsJsonParser.parse(jObject);
				}

			} catch (Exception e) {
			}
			return list;
		}

		@Override
		protected void onPostExecute(List<HashMap<String, String>> result) {

			switch (parserType) {
			case PLACES:
				String[] from = new String[] { "description" };
				int[] to = new int[] { android.R.id.text1 };

				try{
					// Creating a SimpleAdapter for the AutoCompleteTextView
					SimpleAdapter adapter = new SimpleAdapter(getActivity()
						.getBaseContext(), result,
						android.R.layout.simple_list_item_1, from, to);

					// Setting the adapter
					endPointTextField.setAdapter(adapter);
				}catch(Exception ex){
					
				}

				break;
			case PLACES_DETAILS:
				HashMap<String, String> hm = result.get(0);

				// Getting latitude from the parsed data
				double endPointLatitude = Double.parseDouble(hm.get("lat"));

				// Getting longitude from the parsed data
				double endPointLongitude = Double.parseDouble(hm.get("lng"));

				destinationPoint = new LatLng(endPointLatitude,
						endPointLongitude);

				if(destinationPointMarkerOnMap!=null){
					destinationPointMarkerOnMap.remove();
				}
				
				destinationPointMarkerOnMap = dislayMarkerAtLocationAndZoomIn(destinationPoint);

				InputMethodManager inputManager = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);

				inputManager.hideSoftInputFromWindow(getActivity()
						.getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);

				break;
			}
		}
	}

	/** A class to parse the Geocoding Places in non-ui thread */
	class SimpleGeolocationParserTask extends
			AsyncTask<String, Integer, List<HashMap<String, String>>> {

		JSONObject jObject;
		List<String> locations;
		List<HashMap<String, String>> responseList;

		// Invoked by execute() method of this object
		@Override
		protected List<HashMap<String, String>> doInBackground(
				String... jsonData) {

			List<HashMap<String, String>> places = null;
			SimpleGeocodeJSONParser parser = new SimpleGeocodeJSONParser();

			try {
				jObject = new JSONObject(jsonData[0]);

				/** Getting the parsed data as a an ArrayList */
				places = parser.parse(jObject);

			} catch (Exception e) {
			}
			return places;
		}

		@Override
		protected void onPostExecute(List<HashMap<String, String>> list) {

			responseList = list;

			if (responseList != null) {

				locations = new ArrayList<String>();

				for (HashMap<String, String> item : responseList) {

					locations.add(item.get("formatted_address"));

				}

				if (locations.size() == 0) {
					Toast.makeText(getActivity(),
							"No result found. Please make another selection",
							Toast.LENGTH_SHORT).show();

				} else if (locations.size() == 1) {

					char[] location = locations.get(0).toCharArray();

					endPointTextField.setText(location, 0, location.length);

					// Getting latitude from the parsed data
					double endPointLatitude = Double.parseDouble(responseList
							.get(0).get("lat"));

					// Getting longitude from the parsed data
					double endPointLongitude = Double.parseDouble(responseList
							.get(0).get("lng"));

					destinationPoint = new LatLng(endPointLatitude,
							endPointLongitude);

					if(destinationPointMarkerOnMap!=null){
						destinationPointMarkerOnMap.remove();
					}
					
					destinationPointMarkerOnMap = dislayMarkerAtLocationAndZoomIn(destinationPoint);

				} else {

					CharSequence locationsCharSequence[] = locations
							.toArray(new CharSequence[locations.size()]);

					AlertDialog.Builder builder = new AlertDialog.Builder(
							getActivity());
					builder.setTitle("Pick a location");
					builder.setItems(locationsCharSequence,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									char[] location = locations.get(which)
											.toCharArray();

									endPointTextField.setText(location, 0,
											location.length);

									// Getting latitude from the parsed data
									double endPointLatitude = Double
											.parseDouble(responseList
													.get(which).get("lat"));

									// Getting longitude from the parsed data
									double endPointLongitude = Double
											.parseDouble(responseList
													.get(which).get("lng"));

									destinationPoint = new LatLng(
											endPointLatitude, endPointLongitude);

									if(destinationPointMarkerOnMap!=null){
										destinationPointMarkerOnMap.remove();
									}
									
									destinationPointMarkerOnMap = dislayMarkerAtLocationAndZoomIn(destinationPoint);

								}
							});
					builder.show();
				}
			} else {
				Toast.makeText(
						getActivity(),
						"Unable to sugest any location. Most probably you are not connected to the internet.",
						Toast.LENGTH_SHORT).show();
			}

		}

	}

	/** A class, to download Places from Geocoding webservice */
	private class SimpleGeolocationDownloadTask extends
			AsyncTask<String, Integer, String> {

		String data = null;

		// Invoked by execute() method of this object
		@Override
		protected String doInBackground(String... url) {
			try {
				data = downloadUrl(url[0]);
			} catch (Exception e) {
			}
			return data;
		}

		// Executed after the complete execution of doInBackground() method
		@Override
		protected void onPostExecute(String result) {

			// Instantiating ParserTask which parses the json data from
			// Geocoding webservice
			// in a non-ui thread
			SimpleGeolocationParserTask parserTask = new SimpleGeolocationParserTask();

			// Start parsing the places in JSON format
			// Invokes the "doInBackground()" method of the class ParseTask
			parserTask.execute(result);
		}
	}

}