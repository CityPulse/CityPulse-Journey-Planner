package com.siemens.parkingapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import citypulse.commons.data.Coordinate;
import citypulse.commons.reasoning_request.ARType;
import citypulse.commons.reasoning_request.NF_Details;
import citypulse.commons.reasoning_request.ReasoningRequest;
import citypulse.commons.reasoning_request.User;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.siemens.parkingapp.common.DefaultValues;

public class ParkingPlace extends Fragment {

	private String message = "";
	EditText defaultLocation;
	private SupportMapFragment mMap;
	private GoogleMap map;

	// this fields are used to create the JSON message
	private Boolean cheapestParking = DefaultValues.PARKING_PLACE_CHEAPEST_FIELD_INITIAL_VALUE;
	private Boolean shortestWalk = DefaultValues.PARKING_PLACE_SHORTES_WALK_FIELD_INITIAL_VALUE;
	private Double point_of_interes_latitude = 0.0;
	private Double point_of_interes_longitude = 0.0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View parkingPlace = inflater.inflate(R.layout.parking_place_frag,
				container, false);
		Button constraintButton1 = (Button) parkingPlace
				.findViewById(R.id.constraintsButton1);
		Button okButton = (Button) parkingPlace.findViewById(R.id.okButton1);
		Button goButton = (Button) parkingPlace.findViewById(R.id.goButton);
		defaultLocation = (EditText) parkingPlace
				.findViewById(R.id.defaultLocation1);

		goButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				InputMethodManager mgr = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				mgr.hideSoftInputFromWindow(defaultLocation.getWindowToken(), 0);

				String location = defaultLocation.getText().toString();

				if (location == null || location.equals("")) {
					Toast.makeText(getActivity(), "No Place is entered",
							Toast.LENGTH_SHORT).show();
					return;
				}

				String url = "https://maps.googleapis.com/maps/api/geocode/json?";

				try {
					// encoding special characters like space in the user input
					// place
					location = URLEncoder.encode(location, "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				String address = "address=" + location;

				String sensor = "sensor=false";

				// url , from where the geocoding data is fetched
				url = url + address + "&" + sensor;

				// Instantiating DownloadTask to get places from Google
				// Geocoding service
				// in a non-ui thread
				DownloadTask downloadTask = new DownloadTask();

				// Start downloading the geocoding places
				downloadTask.execute(url);

			}
		});

		okButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// TODO Auto-generated method stub

				// EditText et = (EditText) getActivity().findViewById(
				// R.id.defaultLocation1);
				// message = et.getText().toString(); // get the text message on
				// // the text field
				//
				// // finalizare json si trimitere
				//
				// et.setText(""); // Reset the text field to blank

				SendMessageToServer sendMessageTask = new SendMessageToServer();
				sendMessageTask.execute();

			}
		});

		constraintButton1.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Intent intent = new Intent(getActivity(),
						ParkingPlaceConstraintsActivity.class);
				startActivityForResult(intent, 999);

			}
		});
		return parkingPlace;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		FragmentManager fm = getChildFragmentManager();
		mMap = (SupportMapFragment) fm.findFragmentById(R.id.map1);
		if (mMap == null) {
			mMap = SupportMapFragment.newInstance();
			fm.beginTransaction().replace(R.id.map1, mMap).commit();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (map == null) {
			map = mMap.getMap();
			map.addMarker(new MarkerOptions().position(new LatLng(0, 0)));
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 999) {

			if (resultCode == ParkingPlaceConstraintsActivity.RESULT_OK) {

				Bundle extras = data.getExtras();
				cheapestParking = extras.getBoolean("cheapest");
				shortestWalk = extras.getBoolean("shortestWalk");
			}

			if (resultCode == ParkingPlaceConstraintsActivity.RESULT_CANCELED) {
			}

		}
	}

	private class SendMessageToServer extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			
			System.out.println("Creating message request");

			// create the request object
			FunctionalParameters requestFunctionalParameters = new FunctionalParameters();

			Coordinate poiCoordinate = new Coordinate(
					point_of_interes_latitude, point_of_interes_longitude);

			requestFunctionalParameters
					.addFunctionalParameter(new FunctionalParameter(
							FunctionalParameterName.POINT_OF_INTEREST,
							new StringFunctionalParameterValue(poiCoordinate
									.toString())));

			requestFunctionalParameters
					.addFunctionalParameter(new FunctionalParameter(
							FunctionalParameterName.STARTING_DATETIME,
							new StringFunctionalParameterValue(new Long(System
									.currentTimeMillis()).toString())));

			requestFunctionalParameters
					.addFunctionalParameter(new FunctionalParameter(
							FunctionalParameterName.DISTANCE_RANGE,
							new StringFunctionalParameterValue("1000")));

			requestFunctionalParameters
					.addFunctionalParameter(new FunctionalParameter(
							FunctionalParameterName.TIME_OF_STAY,
							new StringFunctionalParameterValue("100")));

			FunctionalConstraints requestFunctionalConstraints = new FunctionalConstraints();

			requestFunctionalConstraints
					.addFunctionalConstraint(new FunctionalConstraint(
							FunctionalConstraintName.COST,
							FunctionalConstraintOperator.LESS_THAN,
							new IntegerFunctionalConstraintValue(100)));

			FunctionalPreferences requestFunctionalPreferences = new FunctionalPreferences();

			requestFunctionalPreferences
					.addFunctionalPreference(new FunctionalPreference(1,
							FunctionalPreferenceOperation.MINIMIZE,
							FunctionalConstraintName.COST));

			requestFunctionalPreferences
					.addFunctionalPreference(new FunctionalPreference(2,
							FunctionalPreferenceOperation.MINIMIZE,
							FunctionalConstraintName.DISTANCE));

			ReasoningRequest reasoningRequest = new ReasoningRequest(
					new User(), ARType.PARKING_SPACES, new FunctionalDetails(
							requestFunctionalParameters,
							requestFunctionalConstraints,
							requestFunctionalPreferences));

			GsonBuilder builder = new GsonBuilder();
			builder.registerTypeAdapter(FunctionalConstraintValue.class,
					new FunctionalConstraintValueAdapter());
			builder.registerTypeAdapter(FunctionalParameterValue.class,
					new FunctionalParameterValueAdapter());
			Gson gson = builder.create();

			message = gson.toJson(reasoningRequest);
			
			System.out.println("Message " + message);

			final CountDownLatch messageLatch = new CountDownLatch(1);

			ClientEndpointConfig cec = ClientEndpointConfig.Builder.create()
					.build();

			ClientManager clientManager = ClientManager.createClient();

			Endpoint clientEndpoint = new Endpoint() {

				@Override
				public void onOpen(Session session, EndpointConfig config) {

					session.addMessageHandler(new MessageHandler.Whole<String>() {

						public void onMessage(String message) {

							System.out.println("Received message: " + message);

							messageLatch.countDown();

						}

					});

					try {
						session.getBasicRemote().sendText(message);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			};


			try {
				clientManager
						.connectToServer(
								clientEndpoint,
								cec,
								new URI(
										"ws://10.196.2.54:8005/websockets/reasoning_request"));

				messageLatch.await(100, TimeUnit.SECONDS);

			} catch (DeploymentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}
	}

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

	/** A class, to download Places from Geocoding webservice */
	private class DownloadTask extends AsyncTask<String, Integer, String> {

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
			ParserTask parserTask = new ParserTask();

			// Start parsing the places in JSON format
			// Invokes the "doInBackground()" method of the class ParseTask
			parserTask.execute(result);
		}
	}

	/** A class to parse the Geocoding Places in non-ui thread */
	class ParserTask extends
			AsyncTask<String, Integer, List<HashMap<String, String>>> {

		JSONObject jObject;

		// Invoked by execute() method of this object
		@Override
		protected List<HashMap<String, String>> doInBackground(
				String... jsonData) {

			List<HashMap<String, String>> places = null;
			GeocodeJSONParser parser = new GeocodeJSONParser();

			try {
				jObject = new JSONObject(jsonData[0]);

				/** Getting the parsed data as a an ArrayList */
				places = parser.parse(jObject);

			} catch (Exception e) {
				Log.d("Exception", e.toString());
			}
			return places;
		}

		@Override
		protected void onPostExecute(List<HashMap<String, String>> list) {

			// Clears all the existing markers
			map.clear();

			for (int i = 0; i < list.size(); i++) {

				// Creating a marker
				MarkerOptions markerOptions = new MarkerOptions();

				// Getting a place from the places list
				HashMap<String, String> hmPlace = list.get(i);

				// Getting latitude of the place
				double lat = Double.parseDouble(hmPlace.get("lat"));

				// Getting longitude of the place
				double lng = Double.parseDouble(hmPlace.get("lng"));

				// obtin lat si longitudine

				// Getting name
				String name = hmPlace.get("formatted_address");

				LatLng latLng = new LatLng(lat, lng);

				// Setting the position for the marker
				markerOptions.position(latLng);

				// Setting the title for the marker
				markerOptions.title(name);

				// Placing a marker on the touched position
				map.addMarker(markerOptions);

				// Locate the first location
				if (i == 0)
					map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
			}

			// restore the the coordinates of the first point
			HashMap<String, String> hmPlace = list.get(0);
			point_of_interes_latitude = Double.parseDouble(hmPlace.get("lat"));
			point_of_interes_longitude = Double.parseDouble(hmPlace.get("lng"));

		}

	}
}