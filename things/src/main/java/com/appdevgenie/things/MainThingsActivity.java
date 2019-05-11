package com.appdevgenie.things;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.nio.charset.Charset;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainThingsActivity extends AppCompatActivity {

    private static final String TAG = "nearbyLog";
    private static final String NEARBY_SERVICE_ID = "com.appdevgenie.pinearby";
    private static final Strategy STRATEGY = Strategy.P2P_STAR;

    //private GoogleApiClient googleApiClient;
    private ConnectionsClient connectionsClient;
    private String endpoint;
    private TextView tvInfo;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_things);

        Log.d(TAG, "onCreate: ");

        context = getApplicationContext();

        tvInfo = findViewById(R.id.tvInfo);
        tvInfo.setText("onCreate");

        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);

        connectionsClient = Nearby.getConnectionsClient(this);
        //startAdvertising();

        //buildApiClient();
    }

    /*private void buildApiClient() {

        googleApiClient = new GoogleApiClient
                .Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Nearby.CONNECTIONS_API)
                .enableAutoManage(this, this)
                .build();
    }*/

    private PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
            Log.d(TAG, "onPayloadReceived: " + new String(payload.asBytes()));
            tvInfo.setText(new String(payload.asBytes()));
            sendMessage(new String(payload.asBytes()) + " received");
        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {

        }
    };

    private void sendMessage(String message) {
        //Nearby.Connections.sendPayload(googleApiClient, endpoint, Payload.fromBytes(message.getBytes()));
        //Nearby.getConnectionsClient(context).sendPayload(endpoint, Payload.fromBytes(message.getBytes()));
        connectionsClient.sendPayload(endpoint, Payload.fromBytes(message.getBytes(Charset.forName("UTF-8"))));
    }

    private final ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {

            /*endpoint = endpointId;
            // Automatically accept the connection on both sides.
            Nearby.getConnectionsClient(context).acceptConnection(endpointId, payloadCallback);*/

            endpoint = endpointId;
            Log.d(TAG, "onConnectionInitiated: accepting connection");
            connectionsClient.acceptConnection(endpointId, payloadCallback);
            String connection = connectionInfo.getEndpointName();
            tvInfo.setText(connection);
            /*endpoint = endpointId;

            Nearby.Connections.acceptConnection(googleApiClient, endpointId, payloadCallback)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull com.google.android.gms.common.api.Status status) {
                            if( status.isSuccess() ) {
                                //Connection accepted
                                Log.d(TAG, "onResult: connection success");
                            }
                        }
                    });*/
        }

        @Override
        public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution connectionResolution) {

            if (connectionResolution.getStatus().isSuccess()) {
                Log.d(TAG, "onConnectionResult: connection successful");

                connectionsClient.stopDiscovery();
            } else {
                Log.d(TAG, "onConnectionResult: connection failed");
            }
            /*switch (connectionResolution.getStatus().getStatusCode()) {
                case ConnectionsStatusCodes.STATUS_OK:
                    // We're connected! Can now start sending and receiving data.
                    tvInfo.setText("connected to mobile");

                    break;
                case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                    // The connection was rejected by one or both sides.
                    tvInfo.setText("connection rejected");
                    break;
                case ConnectionsStatusCodes.STATUS_ERROR:
                    tvInfo.setText("error connecting");
                    // The connection broke before it was able to be accepted.
                    break;
                default:
                    // Unknown status code
            }*/
        }

        @Override
        public void onDisconnected(@NonNull String endpointId) {
            Log.d(TAG, "onDisconnected: ");
            tvInfo.setText("diconnected from mobile");
        }
    };

    private void startAdvertising() {

        Log.d(TAG, "startAdvertising: ");
        tvInfo.setText("advertising");

        // Note: Advertising may fail. To keep this demo simple, we don't handle failures.
        /*connectionsClient.startAdvertising(
                "things", getPackageName(), connectionLifecycleCallback,
                new AdvertisingOptions.Builder().setStrategy(STRATEGY).build());*/
        AdvertisingOptions advertisingOptions =
                new AdvertisingOptions.Builder().setStrategy(STRATEGY).build();
        Nearby.getConnectionsClient(context)
                .startAdvertising(
                        "things", NEARBY_SERVICE_ID, connectionLifecycleCallback, advertisingOptions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: ");
                        tvInfo.setText("success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.getMessage());
                        tvInfo.setText("failure " + e.getMessage());
                    }
                });

        /*Nearby.getConnectionsClient(this)
                .startAdvertising(
                        "things",
                        NEARBY_SERVICE_ID,
                        connectionLifecycleCallback,
                        new AdvertisingOptions(Strategy.P2P_STAR));*/

        /*AdvertisingOptions advertisingOptions =
                new AdvertisingOptions.Builder()
                        .setStrategy(Strategy.P2P_STAR)
                        .build();

        Nearby.getConnectionsClient(context)
                .startAdvertising(
                        "things", NEARBY_SERVICE_ID, connectionLifecycleCallback, advertisingOptions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: ");
                        tvInfo.setText("advertising success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        tvInfo.setText("advertising failure " + e.getMessage());
                    }
                });*/
        /*Nearby.Connections.startAdvertising(
                googleApiClient,
                "things",
                NEARBY_SERVICE_ID,
                connectionLifecycleCallback,
                new AdvertisingOptions(Strategy.P2P_STAR)
        );*/
    }

   /* @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: ");
        tvInfo.setText("onConnected");
        startAdvertising();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: ");
        //googleApiClient.reconnect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: " + connectionResult.toString());
        tvInfo.setText("onConnectionFailed: " + connectionResult.toString());
    }*/

    @Override
    protected void onStart() {
        super.onStart();
        //googleApiClient.connect();
        startAdvertising();
    }

    @Override
    protected void onStop() {
        super.onStop();
        connectionsClient.stopAllEndpoints();

        /*if (googleApiClient != null && googleApiClient.isConnected()) {
            Nearby.Connections.stopAdvertising(googleApiClient);

            googleApiClient.disconnect();
        }*/
    }
}
