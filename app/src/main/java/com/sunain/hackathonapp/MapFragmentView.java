package com.sunain.hackathonapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.here.android.mpa.common.GeoBoundingBox;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPolygon;
import com.here.android.mpa.common.GeoPolyline;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.guidance.NavigationManager;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapCircle;
import com.here.android.mpa.mapping.MapFragment;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapPolygon;
import com.here.android.mpa.mapping.MapPolyline;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.mapping.MapTrafficLayer;
import com.here.android.mpa.mapping.MapTransitLayer;
import com.here.android.mpa.routing.CoreRouter;
import com.here.android.mpa.routing.Route;
import com.here.android.mpa.routing.RouteOptions;
import com.here.android.mpa.routing.RoutePlan;
import com.here.android.mpa.routing.RouteResult;
import com.here.android.mpa.routing.RouteWaypoint;
import com.here.android.mpa.routing.Router;
import com.here.android.mpa.routing.RoutingError;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This class encapsulates the properties and functionality of the Map view.It also triggers a
 * turn-by-turn navigation from HERE Burnaby office to Langley BC.There is a sample voice skin
 * bundled within the SDK package to be used out-of-box, please refer to the Developer's guide for
 * the usage.
 */
public class MapFragmentView {
    private MapFragment m_mapFragment;
    private Activity m_activity;
    private Button m_naviControlButton;
    private Map m_map;
    private NavigationManager m_navigationManager;
    private GeoBoundingBox m_geoBoundingBox;
    private Route m_route;
    private boolean m_foregroundServiceStarted;
    private MapCircle m_circle;
    private MapPolygon m_polygon;
    private MapPolyline m_polyline;
    TextView msg,spd;
    RelativeLayout msglay,spdlay;
    private MapMarker m_map_marker;
//    GeoPosition gp=new GeoPosition(new GeoCoordinate(26.398209813559323,80.19302722033899,0.0));


    public MapFragmentView(Activity activity) {
        m_activity = activity;
        initMapFragment();
        initNaviControlButton();

    }

    private void initMapFragment() {
        /* Locate the mapFragment UI element */
        m_mapFragment = (MapFragment) m_activity.getFragmentManager()
                .findFragmentById(R.id.mapfragment);
        msg=m_activity.findViewById(R.id.textviewMessage);
        spd=m_activity.findViewById(R.id.textviewSpeed);
        msglay=m_activity.findViewById(R.id.rel);
        spdlay=m_activity.findViewById(R.id.speedlayout);
        // Set path of isolated disk cache
        String diskCacheRoot = Environment.getExternalStorageDirectory().getPath()
                + File.separator + ".isolated-here-maps";
        // Retrieve intent name from manifest
        String intentName = "";
        try {
            ApplicationInfo ai = m_activity.getPackageManager().getApplicationInfo(m_activity.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            intentName = bundle.getString("INTENT_NAME");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(this.getClass().toString(), "Failed to find intent name, NameNotFound: " + e.getMessage());
        }

        boolean success = com.here.android.mpa.common.MapSettings.setIsolatedDiskCacheRootPath(diskCacheRoot, intentName);
        if (!success) {
            // Setting the isolated disk cache was not successful, please check if the path is valid and
            // ensure that it does not match the default location
            // (getExternalStorageDirectory()/.here-maps).
            // Also, ensure the provided intent name does not match the default intent name.
        } else {
            if (m_mapFragment != null) {
                /* Initialize the MapFragment, results will be given via the called back. */
                m_mapFragment.init(new OnEngineInitListener() {
                    @Override
                    public void onEngineInitializationCompleted(OnEngineInitListener.Error error) {

                        if (error == Error.NONE) {
                            m_map = m_mapFragment.getMap();
                            m_map.setCenter(new GeoCoordinate(28.7027705,77.0859461),
                                    Map.Animation.NONE);
                            //Put this call in Map.onTransformListener if the animation(Linear/Bow)
                            //is used in setCenter()
                            m_map.setZoomLevel(13.2);

                            /*
                             * Get the NavigationManager instance.It is responsible for providing voice
                             * and visual instructions while driving and walking
                             */
                            m_navigationManager = NavigationManager.getInstance();
                        } else {
                            Toast.makeText(m_activity,
                                    "ERROR: Cannot initialize Map with error " + error,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }
    }
    private void locateareas()
    {
        GeoCoordinate[] gpc=new GeoCoordinate[]
                {
                        new GeoCoordinate(26.398209813559323,80.19302722033899,0.0),
                        new GeoCoordinate(24.18038478125 , 85.585357125,0.0),
                        new GeoCoordinate(26.86587927118644 , 78.92878988135593,0.0),
                        new GeoCoordinate(23.51828331707317 , 87.32711434146341,0.0),
                        new GeoCoordinate(25.604754461538462 , 81.4786801025641,0.0),
                        new GeoCoordinate(24.7388709375 , 84.37840903125,0.0),
                        new GeoCoordinate(25.248076205882352 , 82.96096985294118,0.0),
                        new GeoCoordinate(27.107469921296296 , 78.58212604166667,0.0),
                        new GeoCoordinate(26.503612567901236 , 79.42409491358025,0.0),
                        new GeoCoordinate(23.82432172413793 , 86.53666348275863,0.0),
                        new GeoCoordinate(23.11853096551724 , 88.06070427586207,0.0),
                        new GeoCoordinate(25.985579836734694 , 80.77354673469388,0.0)
                };
        for(int i=0;i<12;i++)
        {
            m_circle = new MapCircle(1000.0, gpc[i]);
            m_circle.setLineColor(Color.BLACK);
            m_circle.setFillColor(Color.RED);
            m_circle.setLineWidth(12);
            m_map.addMapObject(m_circle);
        }

createMapMarker();

    }

    private void createMapMarker() {

        GeoCoordinate[] gpc=new GeoCoordinate[]
                {
                        new GeoCoordinate(27.119329, 78.568011,0.0),
                        new GeoCoordinate(27.099175, 78.573470,0.0),
                       new GeoCoordinate(27.125303, 78.610763,0.0),
                        new GeoCoordinate(27.090755, 78.592033,0.0),
                        new GeoCoordinate(27.145531, 78.53182,0.0),
                        new GeoCoordinate(27.114112, 78.561815,0.0),
                        new GeoCoordinate(27.112278, 78.558854,0.0)
                };
        for(int i=0;i<gpc.length;i++)
        {
            Image marker_img = new Image();
            try {
                marker_img.setImageResource(R.drawable.cafe);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // create a MapMarker centered at current location with png image.
            m_map_marker = new MapMarker(gpc[i], marker_img);
            // add a MapMarker to current active map.
            m_map.addMapObject(m_map_marker);
        }

    }
    private void locateareasmoving(GeoPosition geoPosition)
    {
        GeoCoordinate[] gpc=new GeoCoordinate[]
                {
                        new GeoCoordinate(26.398209813559323,80.19302722033899,0.0),
                        new GeoCoordinate(24.18038478125 , 85.585357125,0.0),
                        new GeoCoordinate(26.86587927118644 , 78.92878988135593,0.0),
                        new GeoCoordinate(23.51828331707317 , 87.32711434146341,0.0),
                        new GeoCoordinate(25.604754461538462 , 81.4786801025641,0.0),
                        new GeoCoordinate(24.7388709375 , 84.37840903125,0.0),
                        new GeoCoordinate(25.248076205882352 , 82.96096985294118,0.0),
                        new GeoCoordinate(27.107469921296296 , 78.58212604166667,0.0),
                        new GeoCoordinate(26.503612567901236 , 79.42409491358025,0.0),
                        new GeoCoordinate(23.82432172413793 , 86.53666348275863,0.0),
                        new GeoCoordinate(23.11853096551724 , 88.06070427586207,0.0),
                        new GeoCoordinate(25.985579836734694 , 80.77354673469388,0.0)
                };
        msg.setText("SmartDrive you are in safe area now.");
        //msglay.setBackgroundColor(Color.BLUE);
        msg.setTextColor(Color.BLUE);
        for(int i=0;i<12;i++)
        {
           if(geoPosition.getCoordinate().distanceTo(gpc[i])<2000)
           {
               if(geoPosition.getCoordinate().distanceTo(gpc[i])<1000)
               {
                   msg.setText("your are inside an accident prone area");
                   msg.setTextColor(Color.RED);
                   //msglay.setBackgroundColor(Color.RED);
               }
               else
               {
                   msg.setText("your are in 2km radius of accident prone area");
                   //msglay.setBackgroundColor(Color.YELLOW);
                   msg.setTextColor(Color.BLACK);
               }
           }
//           if(geoPosition.getCoordinate().distanceTo(gpc[i])>10000)
//           {
//               msg.setText("You are now out of accident prone area range driving at "+geoPosition.getSpeed()*3.6+"km/h");
//           }
//           else
//               {
//                   msg.setText("You are now out of accident prone area range");
//               }

        }


    }
    private void createCircle() {
        // create a MapCircle centered at current location with radius 400
        m_circle = new MapCircle(400.0, m_map.getCenter());
        m_circle.setLineColor(Color.BLUE);
        m_circle.setFillColor(Color.GRAY);
        m_circle.setLineWidth(12);
        m_map.addMapObject(m_circle);
    }
    private void createPolygon() {
        // create an bounding box centered at current cent
        GeoBoundingBox boundingBox = new GeoBoundingBox(m_map.getCenter(), 1000, 1000);
        // add boundingbox's four vertices to list of Geocoordinates.
        List<GeoCoordinate> coordinates = new ArrayList<GeoCoordinate>();
        coordinates.add(boundingBox.getTopLeft());
        coordinates.add(new GeoCoordinate(boundingBox.getTopLeft().getLatitude(),
                boundingBox.getBottomRight().getLongitude(),
                boundingBox.getTopLeft().getAltitude()));
        coordinates.add(boundingBox.getBottomRight());
        coordinates.add(new GeoCoordinate(boundingBox.getBottomRight().getLatitude(),
                boundingBox.getTopLeft().getLongitude(), boundingBox.getTopLeft().getAltitude()));
        // create GeoPolygon with list of GeoCoordinates.
        GeoPolygon geoPolygon = new GeoPolygon(coordinates);
        // create MapPolygon with GeoPolygon.
        m_polygon = new MapPolygon(geoPolygon);
        // set line color, fill color and line width
        m_polygon.setLineColor(Color.RED);
        m_polygon.setFillColor(Color.GRAY);
        m_polygon.setLineWidth(12);
        // add MapPolygon to map.
        m_map.addMapObject(m_polygon);
    }
    private void createPolyline() {
        // create boundingBox centered at current location
        GeoBoundingBox boundingBox = new GeoBoundingBox(m_map.getCenter(), 1000, 1000);
        // add boundingBox's top left and bottom right vertices to list of GeoCoordinates
        List<GeoCoordinate> coordinates = new ArrayList<>();
        coordinates.add(boundingBox.getTopLeft());
        coordinates.add(boundingBox.getBottomRight());
        // create GeoPolyline with list of GeoCoordinates
        GeoPolyline geoPolyline = new GeoPolyline(coordinates);
        m_polyline = new MapPolyline(geoPolyline);
        m_polyline.setLineColor(Color.BLUE);
        m_polyline.setLineWidth(12);
        // add GeoPolyline to current active map
        m_map.addMapObject(m_polyline);
    }
    private void createRoute() {
        /* Initialize a CoreRouter */
        CoreRouter coreRouter = new CoreRouter();

        /* Initialize a RoutePlan */
        RoutePlan routePlan = new RoutePlan();

        /*
         * Initialize a RouteOption.HERE SDK allow users to define their own parameters for the
         * route calculation,including transport modes,route types and route restrictions etc.Please
         * refer to API doc for full list of APIs
         */
        RouteOptions routeOptions = new RouteOptions();
        /* Other transport modes are also available e.g Pedestrian */
        routeOptions.setTransportMode(RouteOptions.TransportMode.CAR);
        /* Disable highway in this route. */
        routeOptions.setHighwaysAllowed(true);
        /* Calculate the shortest route available. */
        routeOptions.setRouteType(RouteOptions.Type.BALANCED);
        /* Calculate 1 route. */
        routeOptions.setRouteCount(3);
        /* Finally set the route option */
        routePlan.setRouteOptions(routeOptions);

        /* Define waypoints for the route */
        /* START: 4350 Still Creek Dr */
        RouteWaypoint startPoint = new RouteWaypoint(new GeoCoordinate(27.113285,78.561293));
        /* END: Langley BC */
        RouteWaypoint destination = new RouteWaypoint(new GeoCoordinate(22.914691, 88.197071));

        RouteWaypoint passthrough=new RouteWaypoint(new GeoCoordinate(26.430770, 80.164305));
        /* Add both waypoints to the route plan */
        routePlan.addWaypoint(startPoint);
        routePlan.addWaypoint(passthrough);
        routePlan.addWaypoint(destination);

        /* Trigger the route calculation,results will be called back via the listener */
        coreRouter.calculateRoute(routePlan,
                new Router.Listener<List<RouteResult>, RoutingError>() {

                    @Override
                    public void onProgress(int i) {
                        /* The calculation progress can be retrieved in this callback. */
                    }

                    @Override
                    public void onCalculateRouteFinished(List<RouteResult> routeResults,
                                                         RoutingError routingError) {
                        /* Calculation is done.Let's handle the result */
                        if (routingError == RoutingError.NONE) {
                            if (routeResults.get(0).getRoute() != null) {

                                m_route = routeResults.get(0).getRoute();
                                /* Create a MapRoute so that it can be placed on the map */
                                MapRoute mapRoute = new MapRoute(routeResults.get(0).getRoute());

                                /* Show the maneuver number on top of the route */
                                mapRoute.setManeuverNumberVisible(true);

                                /* Add the MapRoute to the map */
                                m_map.addMapObject(mapRoute);

                                /*
                                 * We may also want to make sure the map view is orientated properly
                                 * so the entire route can be easily seen.
                                 */
                                m_geoBoundingBox = routeResults.get(0).getRoute().getBoundingBox();
                                m_map.zoomTo(m_geoBoundingBox, Map.Animation.LINEAR,
                                        Map.MOVE_PRESERVE_ORIENTATION);
                                m_map.setTrafficInfoVisible(true);
                                m_map.getMapTrafficLayer().setEnabled(MapTrafficLayer.RenderLayer.INCIDENT,
                                        true);
                                m_map.getMapTrafficLayer().setEnabled(MapTrafficLayer.RenderLayer.FLOW, true);
                                m_map.getMapTransitLayer().setMode(MapTransitLayer.Mode.EVERYTHING);
                                startNavigation();
                            } else {
                                Toast.makeText(m_activity,
                                        "Error:route results returned is not valid",
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(m_activity,
                                    "Error:route calculation returned error code: " + routingError,
                                    Toast.LENGTH_LONG).show();

                        }
                    }
                });
    }

    @SuppressLint("ResourceAsColor")
    private void initNaviControlButton() {
        m_naviControlButton = m_activity.findViewById(R.id.naviCtrlButton);
        m_naviControlButton.setText(R.string.start_navi);
        //m_naviControlButton.setImageResource(R.drawable.);

        m_naviControlButton.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                /*
                 * To start a turn-by-turn navigation, a concrete route object is required.We use
                 * the same steps from Routing sample app to create a route from 4350 Still Creek Dr
                 * to Langley BC without going on HWY.
                 *
                 * The route calculation requires local map data.Unless there is pre-downloaded map
                 * data on device by utilizing MapLoader APIs,it's not recommended to trigger the
                 * route calculation immediately after the MapEngine is initialized.The
                 * INSUFFICIENT_MAP_DATA error code may be returned by CoreRouter in this case.
                 *
                 */
                msg.setText("Processing....");
                locateareas();
                if (m_route == null) {
                    createRoute();
                } else {
                    m_navigationManager.stop();
                    /*
                     * Restore the map orientation to show entire route on screen
                     */
                    m_map.zoomTo(m_geoBoundingBox, Map.Animation.BOW, 0f);
                    m_naviControlButton.setText(R.string.start_navi);
                    spdlay.setVisibility(View.INVISIBLE);
                    m_route = null;
                }
            }
        });
    }

    /*
     * Android 8.0 (API level 26) limits how frequently background apps can retrieve the user's
     * current location. Apps can receive location updates only a few times each hour.
     * See href="https://developer.android.com/about/versions/oreo/background-location-limits.html
     * In order to retrieve location updates more frequently start a foreground service.
     * See https://developer.android.com/guide/components/services.html#Foreground
     */
    private void startForegroundService() {
        if (!m_foregroundServiceStarted) {
            m_foregroundServiceStarted = true;
            Intent startIntent = new Intent(m_activity, ForegroundService.class);
            startIntent.setAction(ForegroundService.START_ACTION);
            m_activity.getApplicationContext().startService(startIntent);
        }
    }

    private void stopForegroundService() {
        if (m_foregroundServiceStarted) {
            m_foregroundServiceStarted = false;
            Intent stopIntent = new Intent(m_activity, ForegroundService.class);
            stopIntent.setAction(ForegroundService.STOP_ACTION);
            m_activity.getApplicationContext().startService(stopIntent);
        }
    }

    @SuppressLint("ResourceAsColor")
    private void startNavigation() {
        m_naviControlButton.setText(R.string.stop_navi);

        /* Display the position indicator on map */
        m_map.getPositionIndicator().setVisible(true);
        /* Configure Navigation manager to launch navigation on current map */
        m_navigationManager.setMap(m_map);

        /*
         * Start the turn-by-turn navigation.Please note if the transport mode of the passed-in
         * route is pedestrian, the NavigationManager automatically triggers the guidance which is
         * suitable for walking. Simulation and tracking modes can also be launched at this moment
         * by calling either simulate() or startTracking()
         */

        /* Choose navigation modes between real time navigation and simulation */
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(m_activity);
        alertDialogBuilder.setTitle("Navigation");
        alertDialogBuilder.setMessage("Choose Mode");
        alertDialogBuilder.setNegativeButton("Navigation",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                m_navigationManager.startNavigation(m_route);
                Toast.makeText(m_activity,"This app is a limited version for NH 19 .Please Check simulation ",Toast.LENGTH_LONG).show();
                m_map.setTilt(60);
                startForegroundService();
            };
        });
        alertDialogBuilder.setPositiveButton("Simulation",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                msg.setText("Initiate Simulation");
                spdlay.setVisibility(View.VISIBLE);
                m_navigationManager.simulate(m_route,30); //Simualtion speed is set to 60 m/s
                m_map.setTilt(60);
                startForegroundService();


            };
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        /*
         * Set the map update mode to ROADVIEW.This will enable the automatic map movement based on
         * the current location.If user gestures are expected during the navigation, it's
         * recommended to set the map update mode to NONE first. Other supported update mode can be
         * found in HERE Android SDK API doc
         */
        m_navigationManager.setMapUpdateMode(NavigationManager.MapUpdateMode.ROADVIEW);

        /*
         * NavigationManager contains a number of listeners which we can use to monitor the
         * navigation status and getting relevant instructions.In this example, we will add 2
         * listeners for demo purpose,please refer to HERE Android SDK API documentation for details
         */
        addNavigationListeners();
    }

    private void addNavigationListeners() {

        /*
         * Register a NavigationManagerEventListener to monitor the status change on
         * NavigationManager
         */
        m_navigationManager.addNavigationManagerEventListener(
                new WeakReference<NavigationManager.NavigationManagerEventListener>(
                        m_navigationManagerEventListener));

        /* Register a PositionListener to monitor the position updates */
        m_navigationManager.addPositionListener(
                new WeakReference<NavigationManager.PositionListener>(m_positionListener));
    }

    private NavigationManager.PositionListener m_positionListener = new NavigationManager.PositionListener() {
        @Override
        public void onPositionUpdated(GeoPosition geoPosition) {
            /* Current position information can be retrieved in this callback */
           //createPolyline();
            locateareasmoving(geoPosition);
            String speed = String.format(Locale.ENGLISH, "%.0f", geoPosition.getSpeed() * 3.6) + "km/h";
            SpannableString s = new SpannableString(speed);
            s.setSpan(new RelativeSizeSpan(0.50f), s.length()-4, s.length(), 0);
            spd.setText(s);
            Log.d("Speed",""+geoPosition.getSpeed());
            Log.d("Location",""+geoPosition.getCoordinate().getLatitude()+" "+geoPosition.getCoordinate().getLongitude());
        }
    };

    private NavigationManager.NavigationManagerEventListener m_navigationManagerEventListener = new NavigationManager.NavigationManagerEventListener() {
        @Override
        public void onRunningStateChanged() {
            Toast.makeText(m_activity, "Running state changed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onNavigationModeChanged() {
            Toast.makeText(m_activity, "Navigation mode changed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onEnded(NavigationManager.NavigationMode navigationMode) {
            Toast.makeText(m_activity, navigationMode + " was ended", Toast.LENGTH_SHORT).show();
            stopForegroundService();
        }

        @Override
        public void onMapUpdateModeChanged(NavigationManager.MapUpdateMode mapUpdateMode) {
            Toast.makeText(m_activity, "Map update mode is changed to " + mapUpdateMode,
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRouteUpdated(Route route) {
            Toast.makeText(m_activity, "Route updated", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCountryInfo(String s, String s1) {
            Toast.makeText(m_activity, "Country info updated from " + s + " to " + s1,
                    Toast.LENGTH_SHORT).show();
        }
    };

    public void onDestroy() {
        /* Stop the navigation when app is destroyed */
        if (m_navigationManager != null) {
            stopForegroundService();
            m_navigationManager.stop();
        }
    }
}