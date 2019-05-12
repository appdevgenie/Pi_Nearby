package com.appdevgenie.pinearby;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.nio.charset.Charset;
import java.util.Random;

public class MainMobileActivity extends AppCompatActivity {

    private static final String TAG = "nearbyLog";
    private static final String NEARBY_SERVICE_ID = "com.appdevgenie.pinearby";
    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;
    private static final Strategy STRATEGY = Strategy.P2P_CLUSTER;

    private static final String[] REQUIRED_PERMISSIONS =
            new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };

    private TextView tvNearbyLog;
    private Button bSend, bConnect;
    private EditText etMessage;
    //private GoogleApiClient googleApiClient;
    // Our handle to Nearby Connections
    private ConnectionsClient connectionsClient;
    private String endpoint;
    private Context context;
    private String randomNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_mobile);
        Log.d(TAG, "onCreate: ");

        setupVariables();

        connectionsClient = Nearby.getConnectionsClient(this);
        //buildApiClient();
        startDiscovering();
    }

    private void setupVariables() {

        context = getApplicationContext();

        tvNearbyLog = findViewById(R.id.tvNearbyLog);
        tvNearbyLog.setText("initialised");

        etMessage = findViewById(R.id.etMessage);

        Random random = new Random();
        randomNumber = String.valueOf(random.nextInt(999) + 99);

        bSend = findViewById(R.id.bSend);
        bSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*if(!googleApiClient.isConnected()){
                    Log.d(TAG, "onClick: not connected");
                    return;
                }*/

                sendMessage(etMessage.getText().toString());

            }
        });
        bConnect = findViewById(R.id.bConnect);
        bConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startDiscovering();
            }
        });

    }

    /*private void buildApiClient() {

     *//*googleApiClient = new GoogleApiClient
                .Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Nearby.CONNECTIONS_API)
                .enableAutoManage(this, this)
                .build();*//*

     *//*googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.d(TAG, "onConnected: ");
                        startDiscovering();

                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d(TAG, "onConnectionSuspended: ");
                        googleApiClient.reconnect();

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d(TAG, "onConnectionFailed: " + connectionResult.getErrorMessage());

                    }
                })
                .addApi(Nearby.CONNECTIONS_API)
                .build();*//*
    }*/

    /*@Override
    public void onConnected(@Nullable Bundle bundle) {
        tvNearbyLog.setText("connected to client");
        Log.d(TAG, "onConnected: to google client");
        startDiscovering();
    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.d(TAG, "onConnectionSuspended: ");
        //googleApiClient.reconnect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.d(TAG, "onConnectionFailed: " + connectionResult.toString());
    }*/

    private PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
            Log.d(TAG, "onPayloadReceived: " + new String(payload.asBytes()));
            tvNearbyLog.setText(new String(payload.asBytes()));
        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {

        }
    };

    private final ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(@NonNull String endPointId, @NonNull ConnectionInfo connectionInfo) {
            /*endpoint = endPointId;
            Nearby.getConnectionsClient(getApplicationContext()).acceptConnection(endPointId, payloadCallback);*/

                    endpoint = endPointId;
                    Log.d(TAG, "onConnectionInitiated: accepting connection to " + endPointId);
                    connectionsClient.acceptConnection(endPointId, payloadCallback);
                    String connection = connectionInfo.getEndpointName();
                    tvNearbyLog.setText("connected to " + connection);

            /*Nearby.Connections.acceptConnection(googleApiClient, endPointId, payloadCallback);
            endpoint = endPointId;
            Nearby.Connections.stopDiscovery(googleApiClient);*/
                }

                @Override
                public void onConnectionResult(@NonNull String endPointId, @NonNull ConnectionResolution connectionResolution) {

                    if (connectionResolution.getStatus().isSuccess()) {
                        Log.d(TAG, "onConnectionResult: connection successful");
                        connectionsClient.stopDiscovery();
                    } else {
                        Log.d(TAG, "onConnectionResult: connection failed");
                    }
            /*switch (connectionResolution.getStatus().getStatusCode()) {
                case ConnectionsStatusCodes.STATUS_OK:
                    // We're connected! Can now start sending and receiving data.
                    tvNearbyLog.setText("connected to mobile");

                    break;
                case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                    // The connection was rejected by one or both sides.
                    tvNearbyLog.setText("connection rejected");
                    break;
                case ConnectionsStatusCodes.STATUS_ERROR:
                    tvNearbyLog.setText("error connecting");
                    // The connection broke before it was able to be accepted.
                    break;
                default:
                    // Unknown status code
            }*/
                }

                @Override
                public void onDisconnected(@NonNull String endPointId) {
                    Log.d(TAG, "onDisconnected: ");
                }
            };

    private final EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(@NonNull String endPointId, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
            if (discoveredEndpointInfo.getServiceId().equalsIgnoreCase(NEARBY_SERVICE_ID)) {

                Log.d(TAG, "onEndpointFound: endpoint found, connecting to " + discoveredEndpointInfo.getEndpointName());
                tvNearbyLog.setText("onEndpointFound: endpoint found, connecting to " + discoveredEndpointInfo.getEndpointName());
                connectionsClient.requestConnection("mobile" + randomNumber, endPointId, connectionLifecycleCallback)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //tvNearbyLog.setText("onSuccess: EndpointDiscoveryCallback");
                                Log.d(TAG, "onSuccess: EndpointDiscoveryCallback");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: EndpointDiscoveryCallback" + e.getMessage());
                            }
                        });
                /*Nearby.getConnectionsClient(getApplicationContext())
                        .requestConnection("mobile", endPointId, connectionLifecycleCallback)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Log.d(TAG, "onSuccess: EndpointDiscoveryCallback");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Log.d(TAG, "onFailure: EndpointDiscoveryCallback" + e.getMessage());
                            }
                        });*/
                /*Nearby.Connections.requestConnection(
                        googleApiClient,
                        "mobile",
                        endPointId,
                        connectionLifecycleCallback
                );*/
                /*Nearby.getConnectionsClient(context)
                        .requestConnection(
                                "mobile",
                                endPointId,
                                connectionLifecycleCallback);*/
            }
        }

        @Override
        public void onEndpointLost(@NonNull String endPointId) {
            Log.d(TAG, "onEndpointLost: disconnected");
            tvNearbyLog.setText("disconnected");
        }
    };

    private void startDiscovering() {

        Log.d(TAG, "startDiscovering: ");
        tvNearbyLog.setText("start discovery");

        // Note: Discovery may fail. To keep this demo simple, we don't handle failures.
        /*connectionsClient.startDiscovery(
                NEARBY_SERVICE_ID, endpointDiscoveryCallback,
                new DiscoveryOptions.Builder().setStrategy(STRATEGY).build());*/

        DiscoveryOptions discoveryOptions = new DiscoveryOptions
                        .Builder()
                        .setStrategy(STRATEGY)
                        .build();
        Nearby.getConnectionsClient(context)
                .startDiscovery(NEARBY_SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "onComplete: discovery");
                        tvNearbyLog.setText("discovery complete");
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        tvNearbyLog.setText("connection success");
                        Log.d(TAG, "onSuccess: ");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.getMessage());
                    }
                });

        /*Nearby.getConnectionsClient(this)
                .startDiscovery(
                        NEARBY_SERVICE_ID,
                        endpointDiscoveryCallback,
                        new DiscoveryOptions(Strategy.P2P_STAR));*/

        /*DiscoveryOptions discoveryOptions =
                new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build();
        Nearby.getConnectionsClient(this)
                .startDiscovery(NEARBY_SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        tvNearbyLog.setText("found things");
                        Log.d(TAG, "onSuccess: discovery");
                        tvNearbyLog.setText("discovery success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.getMessage());
                    }
                });*/
        /*Nearby.Connections.startDiscovery(
                googleApiClient,
                NEARBY_SERVICE_ID,
                endpointDiscoveryCallback,
                new DiscoveryOptions(Strategy.P2P_STAR)
        );*/

    }

    private void sendMessage(String message) {
        connectionsClient.sendPayload(endpoint, Payload.fromBytes(message.getBytes(Charset.forName("UTF-8"))));
        //Nearby.Connections.sendPayload(googleApiClient, endpoint, Payload.fromBytes(message.getBytes(Charset.forName("UTF-8"))));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: check permissions");
        //googleApiClient.connect();
        if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
            }
        }
    }

    /**
     * Returns true if the app was granted all the permissions. Otherwise, returns false.
     */
    private static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Handles user acceptance (or denial) of our permission request.
     */
    @CallSuper
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_CODE_REQUIRED_PERMISSIONS) {
            return;
        }

        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "required permissions", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }
        recreate();
    }

    @Override
    protected void onStop() {
        super.onStop();
        connectionsClient.stopAllEndpoints();
    }

    /*@Override
    protected void onDestroy() {
        super.onDestroy();
        *//*if(googleApiClient.isConnected()) {
            //Nearby.Connections.disconnectFromEndpoint(googleApiClient, endpoint);
            Nearby.getConnectionsClient(context).disconnectFromEndpoint(endpoint);
        }*//*
    }*/
}
