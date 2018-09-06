    package com.example.pavilion.geotest;

    import android.*;
    import android.Manifest;
    import android.app.Notification;
    import android.app.NotificationManager;
    import android.app.PendingIntent;
    import android.app.ProgressDialog;
    import android.content.Context;
    import android.content.Intent;
    import android.content.pm.PackageManager;
    import android.graphics.Color;
    import android.location.Location;
    import android.location.LocationListener;
    import android.support.annotation.NonNull;
    import android.support.annotation.Nullable;
    import android.support.v4.app.ActivityCompat;
    import android.support.v4.app.FragmentActivity;
    import android.os.Bundle;
    import android.support.v7.app.AppCompatActivity;
    import android.util.Log;
    import android.view.Menu;
    import android.view.MenuInflater;
    import android.view.MenuItem;
    import android.widget.SeekBar;
    import android.widget.Toast;

    import com.firebase.geofire.GeoFire;
    import com.firebase.geofire.GeoLocation;
    import com.firebase.geofire.GeoQuery;
    import com.firebase.geofire.GeoQueryEventListener;
    import com.google.android.gms.common.ConnectionResult;
    import com.google.android.gms.common.GooglePlayServicesUtil;
    import com.google.android.gms.common.api.GoogleApiClient;
    import com.google.android.gms.location.LocationRequest;
    import com.google.android.gms.location.LocationServices;
    import com.google.android.gms.maps.CameraUpdateFactory;
    import com.google.android.gms.maps.GoogleMap;
    import com.google.android.gms.maps.OnMapReadyCallback;
    import com.google.android.gms.maps.SupportMapFragment;
    import com.google.android.gms.maps.model.CircleOptions;
    import com.google.android.gms.maps.model.LatLng;
    import com.google.android.gms.maps.model.Marker;
    import com.google.android.gms.maps.model.MarkerOptions;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;

    import java.util.Map;
    import java.util.Random;

    public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener,
            com.google.android.gms.location.LocationListener{

        //Firebase Authentication
        private FirebaseAuth mAuth;
        private FirebaseAuth.AuthStateListener authStateListener;

        private GoogleMap mMap;

        //get radius
        private static int SET_RADIUS=0;

        //Play services location
        private static final int MY_PERMISSION_REQUEST_CODE = 123;
        private static  final int PLAY_SERVICES_RESOLUTION_REQUEST = 890;

        private LocationRequest mLocationRequest;
        private GoogleApiClient mGoogleApiClient;
        private Location mLastLocation;

        private static int UPDATE_INTERVAL = 5000;//5 SECS
        private static int FASTEST_INTERVAL = 3000;//3 secs
        private static int DISPLACEMENT = 10;// 10 metres

        DatabaseReference ref;
        GeoFire geoFire;

        Marker mCurrent;

        VerticalSeekBar mSeekbar;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_maps);
            Intent i=getIntent();
            SET_RADIUS=i.getIntExtra("USER_RADIUS",0);
            // Obtain the SupportMapFragment an d get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            mAuth=FirebaseAuth.getInstance();
            authStateListener=new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if(firebaseAuth.getCurrentUser()==null){
                        Intent intent=new Intent(MapsActivity.this,Login.class);
                        startActivity(intent);
                        finish();
                    }
                }
            };
            ref= FirebaseDatabase.getInstance().getReference("MyLocation");
            geoFire=new GeoFire(ref);
            mSeekbar=(VerticalSeekBar)findViewById(R.id.verticalseekbar);
            mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(i),2000,null);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            setUpLocation();
        }


        @Override
        protected void onStart() {
            super.onStart();
            mAuth.addAuthStateListener(authStateListener);
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            switch (requestCode){
                case MY_PERMISSION_REQUEST_CODE:
                    if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                        if(checkPlayServices()){
                            buildGoogleApiClient();
                            createLocationRequest();
                            displayLocation();
                        }
                    }
                    break;
            }
        }

        private void setUpLocation() {
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                //Request runtime permission
                ActivityCompat.requestPermissions(this,new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                },MY_PERMISSION_REQUEST_CODE);
            }
            else{
                if(checkPlayServices()){
                    buildGoogleApiClient();
                    createLocationRequest();
                    displayLocation();
                }
            }
        }

        private void displayLocation() {
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                return;
            }
            mLastLocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(mLastLocation != null){
                final double latitude=mLastLocation.getLatitude();
                Log.d("lat/long",latitude+"");
                final double longitude=mLastLocation.getLongitude();
                Log.d("lat/long",longitude+"");
                //Update to firebase
                geoFire.setLocation("You", new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        //Add marker
                        if(mCurrent!=null)
                            mCurrent.remove();//remove old marker
                        mCurrent=mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude,longitude))
                        .title("You"));
                        //Move camera to position
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),12.0f));
                    }
                });



                Log.d("Ankur","Your location was changed : "+latitude+"/"+longitude);
            }
            else
                Log.d("Ankur","Cannot get your location");
        }

        private void createLocationRequest() {
            mLocationRequest=new LocationRequest();
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
        }

        private void buildGoogleApiClient() {
            mGoogleApiClient=new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }

        private boolean checkPlayServices() {
            int resultCode= GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
            if(resultCode != ConnectionResult.SUCCESS){
                if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                    GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICES_RESOLUTION_REQUEST).show();
                else{
                    Toast.makeText(this,"This device is not supported",Toast.LENGTH_SHORT).show();
                    finish();
                }
                return false;
            }
            return true;
        }


        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera. In this case,
         * we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to install
         * it inside the SupportMapFragment. This method will only be triggered once the user has
         * installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            //Create safe zone
            LatLng dangerous_area=new LatLng(30.354486,78.0625612);
            mMap.addCircle(new CircleOptions()
            .center(dangerous_area)
            .radius(1000)//in meters
            .strokeColor(Color.BLUE)
            .fillColor(0x220000FF)
            .strokeWidth(5.0f)
            );

            //Add Geoquery here
            //0.5f=0.5km=500m
            GeoQuery geoQuery=geoFire.queryAtLocation(new GeoLocation(dangerous_area.latitude,dangerous_area.longitude),0.1f);
            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    sendNotification("Keep Calm!","Your child is in safe zone!");
                }

                @Override
                public void onKeyExited(String key) {
                    sendNotification("ALERT!","Your child has breached the safe zone!");
                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {
                    Log.d("MOVE",String.format("%s moved within the safe zone [%f/%f] ",key,location.latitude,location.longitude));
                }

                @Override
                public void onGeoQueryReady() {

                }

                @Override
                public void onGeoQueryError(DatabaseError error) {
                    Log.e("ERROR : ",""+error);
                }
            });
        }

        private void sendNotification(String title, String content) {
            Notification.Builder builder=new Notification.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle(title)
                    .setContentText(content);

            NotificationManager manager=(NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent intent=new Intent(this,MapsActivity.class);
            PendingIntent contentIntent =PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_IMMUTABLE);
            builder.setContentIntent(contentIntent);
            Notification notification=builder.build();
            notification.flags |=Notification.FLAG_AUTO_CANCEL;
            notification.defaults |= Notification.DEFAULT_SOUND;

            manager.notify(new Random().nextInt(),notification);
        }

        @Override
        public void onLocationChanged(Location location){
            mLastLocation=location;
            displayLocation();
        }

        @Override
        public void onConnected(@Nullable Bundle bundle) {
            displayLocation();
            startLocationUpdates();
        }

        private void startLocationUpdates() {
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
        }

        @Override
        public void onConnectionSuspended(int i) {
            mGoogleApiClient.connect();

        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        }
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.action_button, menu);
            return true;
        }
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle item selection
            switch (item.getItemId()) {
                case R.id.logout:
                    final ProgressDialog progressDialog=new ProgressDialog(MapsActivity.this);
                    progressDialog.setMessage("Please wait...");
                    progressDialog.setTitle("Logging out...");
                    progressDialog.show();
                    Toast.makeText(MapsActivity.this,"Logout button clicked!",Toast.LENGTH_SHORT).show();
                    mAuth.signOut();
                    return true;

                default:
                    return super.onOptionsItemSelected(item);
            }
        }
    }
