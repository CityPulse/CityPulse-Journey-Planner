# Contextual-Aware Real Time Travel Planner

In order to demonstrate how the CityPulse framework can be used to develop applications for smart cities and citizens, we have implemented a context-aware real time Travel Planner using the live data from the city of Aarhus, Denmark. 
This scenario aims to provide travel-planning solutions, which go beyond the state of the art solutions by allowing users to provide multi dimensional requirements and preferences such as air quality, traffic conditions and parking availability. 
In this way the users receive parking and route recommendations based on the current context of the city. In addition to this, Travel Planner continuously monitors the user context and events detected on the planned route. User will be prompted to opt for a detour if the real time conditions on the planned journey do not meet the user specified criteria anymore.
All the CityPulse framework components are deployed on a back-end server and are accessible via a set of APIs. As a result of that the application developer has only to develop a user-friendly front-end application, which calls the framework APIs. In our case we have developed an Android application.


## Application UI


Figure 1 depict the user interfaces used by the end user to set the travel preferences and the destination point.

![alt text](https://github.com/CityPulse/CityPulse-enabled-application-CONTEXT-AWARE-REAL-TIME-TRAVEL-PLANNER/blob/master/fig1.a.PNG "Figure 1 a)- The user interfaces of the Android application used to select the starting point")
![alt text](https://github.com/CityPulse/CityPulse-enabled-application-CONTEXT-AWARE-REAL-TIME-TRAVEL-PLANNER/blob/master/fig1.b.PNG "Figure 1 b)- Travel preferences ")


After the user has filled in the details and made the request using the user interface, the mobile application generates the appropriate request for the Decision support component which has the following main fields:

* Type: indicating what decision support module is to be used for this application (“TRAVEL-PLANNER” in this case);
* Functional details: specifying possible values of user’s requirements, including: 
 * Functional parameters: mandatory information that the user provides such as starting and ending locations, starting date and time, and transportation type (car, bicycle, or walk). 
 * Functional constraints: numerical thresholds for cost of a trip, distance, or travel time. 
 * Functional preferences: the user can specify his preferences along selected routes, which hold the functional constraints. These preferences can be the minimization or the maximisation of travel time or distance.


The functional constraints and preferences specify different thresholds and minimization criteria for electing the route. During the development of the mobile application the domain expert has computed a default set of values for these thresholds. As a result of that, the route constraints user interface from Figure 1 b) allows the user to select between the fastest/shortest routes. If needed, more fields can be added in this user interface in order to allow more fine-grained constraints specification, but the usability of the application may suffer.


Figure 2 a) depicts the user interface where the routes computed by the Decision support are displayed to the end user. 


![alt text](https://github.com/CityPulse/CityPulse-enabled-application-CONTEXT-AWARE-REAL-TIME-TRAVEL-PLANNER/blob/master/fig2.a.PNG "Figure 2 a)- The user interfaces of the Android application while selecting the preferred route")
![alt text](https://github.com/CityPulse/CityPulse-enabled-application-CONTEXT-AWARE-REAL-TIME-TRAVEL-PLANNER/blob/master/fig2.b.PNG "Figure 2 b)- Notification of a traffic jam which appeared on the selected route while the user is travelling")


After the user selects the preferred route, a request is generated to the Contextual filtering component in order to identify the relevant events for the use while he/she is traveling. 
The request includes the following properties: 
* Route of interest: the current route of the user
* Filtering Factors: used to filter unrelated (unwanted) events out, and include event’s source, event’s category, and user’s activity, as specified in a user-context ontology . For this particular scenario, the activities included in our ontology include CarCommute (user is traveling by a car), or Walk, or BikeCommute (user is traveling by a bike).
* Ranking Factor: identifies which metric is preferred by the user for ranking the criticality of incoming events. We have currently implemented the Ranking Factor based on two metrics: distance, and gravity of the event. In order to combine these two metrics, we use the linear combination approach, where the user can identify weights (or importance) for each metric.


Once the user has selected one of the routes computed by the Decision support component, the Contextual Filtering component sends a request to the Geospatial Database Infrastructure component to obtain the description of the event streams available on the selected route, as registered at design time by the Event detection component. The Contextual Filtering component uses these descriptions to subscribe to detected events via a Data Bus. In addition, the Contextual Filtering also receives the contextual information of the user (currently including location) from the user/application as a stream. Whenever there is a new event detected by the Event detection component, the Contextual Filtering component filters and assigns the most appropriate criticality (0 if not critical, from 1 to 5 if it is critical) to the new event. If the new event is marked as critical, the user receives a notification and he/she has the option to change the current solution and request a new one or ignore the event. 


 
Figure 2 b) depicts the notification received by the end user, while s/he is traveling and a traffic event is detected on his/hers route.  


## Dependencies


In order to allow the application developer to use Google Maps services, he/she will need to follow there steps:

* Import the project in his/hers preffered IDE.
* Edit the AndroidManifest.xml file al line 53(the key to be more exact)
* In order to obain the new keys, we will need to get the SHA1 key from his IDE and go to https://console.developers.google.com/home/dashboard to get a new key. This will allow him to run the application while still connected via USB cable to the computer.


The application developer also has to edit the DefaultValues.java . He will need to adapt to his scenario the Decision Support, Contextual Fintering and Data Federation websocket URLs.(lines 19,23,30 in DefaultValues.java)


## Authors


* **Dan Puiu**
