package com.siemens.parkingapp;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import citypulse.commons.contextual_events_request.ContextualEventRequest;
import citypulse.commons.data.Coordinate;
import citypulse.commons.reasoning_request.ARType;
import citypulse.commons.reasoning_request.Answer;
import citypulse.commons.reasoning_request.Answers;
import citypulse.commons.reasoning_request.ReasoningRequest;
import citypulse.commons.reasoning_request.User;
import citypulse.commons.reasoning_request.concrete.AnswerAdapter;
import citypulse.commons.reasoning_request.concrete.AnswerTravelPlanner;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Route extends Fragment {

	private String webSocketRequest = null;
	private String webSocketResponse = null;
	private String webSocketURL = null;
	private String routeReasoningRequest = "";
	private Double point_of_interes_latitude = 0.0;
	private Double point_of_interes_longitude = 0.0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View route = inflater.inflate(R.layout.route_frag, container, false);

		Button constraintsButton2 = (Button) route
				.findViewById(R.id.constraintsButton2);

		constraintsButton2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Intent intent = new Intent(getActivity(),
						RouteConstraintsActivity.class);
				startActivityForResult(intent, 999);

			}
		});

		Button okButtonRoute = (Button) route.findViewById(R.id.okButton2);

		okButtonRoute.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				SendMessageToServer sendMessageTask = new SendMessageToServer();
				sendMessageTask.execute();

			}
		});

		return route;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 999) {

			if (resultCode == RouteConstraintsActivity.RESULT_OK) {

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

			if (resultCode == RouteConstraintsActivity.RESULT_CANCELED) {
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
					new User(), ARType.TRAVEL_PLANNER, new FunctionalDetails(
							requestFunctionalParameters,
							requestFunctionalConstraints,
							requestFunctionalPreferences));

			GsonBuilder builder = new GsonBuilder();
			builder.registerTypeAdapter(FunctionalConstraintValue.class,
					new FunctionalConstraintValueAdapter());
			builder.registerTypeAdapter(FunctionalParameterValue.class,
					new FunctionalParameterValueAdapter());
			builder.registerTypeAdapter(Answer.class, new AnswerAdapter());
			Gson gson = builder.create();

			routeReasoningRequest = gson.toJson(reasoningRequest);

			System.out.println("routeReasoningRequestMessage "
					+ routeReasoningRequest);

			String routeReasoningResponse = sendWebsocketRequest(
					"ws://10.196.2.61:8005/websockets/reasoning_request",
					routeReasoningRequest);

			System.out.println("routeReasoningResponse "
					+ routeReasoningResponse);

			if (routeReasoningResponse == null) {
				System.out
						.println("The routeReasoningResponse message is empty");
			} else {

				System.out.println("1");

				Answers answers = (Answers) gson.fromJson(
						routeReasoningResponse, Answers.class);

				System.out.println("2");

				if (answers.getAnswers().isEmpty()) {
					System.out
							.println("The reasoning request response is empty!!!!");
				} else {
					ContextualEventRequest contextualEventRequest = new ContextualEventRequest();
					contextualEventRequest
							.setReasoningRequest(reasoningRequest);
					contextualEventRequest.setAnswer(answers.getAnswers()
							.get(0));

					String contextualEventsRequestRouteMessage = gson
							.toJson(contextualEventRequest);

					System.out
							.println("The contextualEventsRequestRouteMessage "
									+ contextualEventsRequestRouteMessage);

					String contextualEventsRequestRouteResponse = sendWebsocketRequest(
							"ws://10.196.2.61:8005/websockets/contextual_events_request",
							contextualEventsRequestRouteMessage);

					System.out.println(contextualEventsRequestRouteResponse);

				}

			}

			return null;
		}

		private String sendWebsocketRequest(String webSocketEndPoint,
				String payload) {

			webSocketRequest = payload;
			webSocketURL = webSocketEndPoint;
			webSocketResponse = null;

			final CountDownLatch messageLatch = new CountDownLatch(1);

			ClientEndpointConfig cec = ClientEndpointConfig.Builder.create()
					.build();

			ClientManager clientManager = ClientManager.createClient();

			Endpoint clientEndpoint = new Endpoint() {

				@Override
				public void onOpen(Session session, EndpointConfig config) {

					session.addMessageHandler(new MessageHandler.Whole<String>() {

						public void onMessage(String message) {

							webSocketResponse = message;

							messageLatch.countDown();

						}

					});

					try {
						session.getBasicRemote().sendText(webSocketRequest);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			};

			try {
				clientManager.connectToServer(clientEndpoint, cec, new URI(
						webSocketURL));

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

			return webSocketResponse;

		}
	}

}