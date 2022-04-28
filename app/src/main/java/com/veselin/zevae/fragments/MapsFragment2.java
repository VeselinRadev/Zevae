package com.veselin.zevae.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.veselin.zevae.R;
import com.veselin.zevae.models.User;
import com.veselin.zevae.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

/**
 * Use a recyclerview with a Mapbox map to easily explore content all on one screen
 */
public class MapsFragment2 extends Fragment implements OnMapReadyCallback {

    private static final String SYMBOL_ICON_ID = "SYMBOL_ICON_ID";
    private static final String SOURCE_ID = "SOURCE_ID";
    private static final String LAYER_ID = "LAYER_ID";
    public MapboxMap mapboxMap;
    private MapView mapView;
    private FeatureCollection featureCollection;
    private View view;
    private FirebaseUser firebaseUser;
    private LocationManager locationManager;
    private String latitude, longitude;
    private User currUser;
    public MapsFragment2(){

    }
    private LatLng[] coordinates;
    private String[] times;
    private String[] names;
    private String[] imagesURL;
    private MarkerOptions[] markersOptions;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

// Mapbox access token is configured here. This needs to be called either in your application
// object or in the same activity which contains the mapview.
        Mapbox.getInstance(getContext(), getString(R.string.mapbox_access_token));

// This contains the MapView in XML and needs to be called after the access token is configured.
        view = inflater.inflate(R.layout.fragment_map, container, false);

        getCurrUser();

// Initialize the map view
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        return view;
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        MapsFragment2.this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                OnGPS();
            } else {
                getLocation(mapboxMap, style);

                new Thread() {
                    @Override
                    public void run() {
                        while (currUser == null || coordinates == null || times == null || names == null || imagesURL == null) {
                            try {
                                sleep(200);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //initFeatureCollection();
                                initMarkerIcons(style);
                                initRecyclerView();
                            }
                        });
                    }
                }.start();
            }
        });
    }public void replaceFragments(Class fragmentClass) {
        Fragment fragment = null;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.nav_host_fragment, fragment)
                .commit();
    }
    private void initRecyclerView() {
        RecyclerView recyclerView = view.findViewById(R.id.rv_on_top_of_map);
        LocationRecyclerViewAdapter locationAdapter =
                new LocationRecyclerViewAdapter(createRecyclerViewLocations(), mapboxMap, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(locationAdapter);
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
    }

    private void initMarkerIcons(@NonNull Style loadedMapStyle) {
//        loadedMapStyle.addImage(SYMBOL_ICON_ID, BitmapFactory.decodeResource(
//                this.getResources(), R.drawable.mapbox_marker_icon_default));
//        loadedMapStyle.addSource(new GeoJsonSource(SOURCE_ID, featureCollection));
//        loadedMapStyle.addLayer(new SymbolLayer(LAYER_ID, SOURCE_ID).withProperties(
//                iconImage(SYMBOL_ICON_ID),
//                iconAllowOverlap(true),
//                iconOffset(new Float[] {0f, -4f})
//        ));
        markersOptions = new MarkerOptions[coordinates.length];
        for(int i = 0; i < coordinates.length; i++) {
            if (coordinates[i] != null) {
                if (i == 0) {
                    markersOptions[i] = new MarkerOptions()
                            .position(coordinates[i])
                            //.setIcon(drawableToIcon(getContext(), R.drawable.ic_placeholder, coordinates[i].getLatitude(), coordinates[i].getLongitude()))
                            .setTitle(names[i]);
                    mapboxMap.addMarker(markersOptions[i]);
                } else {
                    markersOptions[i] = new MarkerOptions()
                            .position(coordinates[i])
                            //.setIcon(drawableToIcon(getContext(), R.drawable.ic_placeholder__2_,  coordinates[i].getLatitude(), coordinates[i].getLongitude()))
                            .setTitle(names[i]);
                    mapboxMap.addMarker(markersOptions[i]);
                }
            }
        }
    }
    public static Icon drawableToIcon(@NonNull Context context, @DrawableRes int id, double left, double right) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(context.getResources(), id, context.getTheme());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds((int)left, (int)right, (canvas.getWidth()/4), (canvas.getHeight()/4));
        vectorDrawable.draw(canvas);
        return IconFactory.getInstance(context).fromBitmap(bitmap);
    }

    private List<SingleRecyclerViewLocation> createRecyclerViewLocations() {
        ArrayList<SingleRecyclerViewLocation> locationList = new ArrayList<>();
        for (int x = 0; x < coordinates.length; x++) {
            if(coordinates[x] != null) {
                SingleRecyclerViewLocation singleLocation = new SingleRecyclerViewLocation();
                singleLocation.setName(String.format(names[x]));
                singleLocation.setBedInfo(times[x]);
                singleLocation.setImage(imagesURL[x]);
                singleLocation.setLocationCoordinates(coordinates[x]);
                locationList.add(singleLocation);
            }
        }
        return locationList;
    }
    private void findFirebaseUser() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(firebaseUser.getUid()).child("FriendRequest").child("Accepted");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                coordinates = new LatLng[(int) (snapshot.getChildrenCount() + 1)];
                times = new String[(int) (snapshot.getChildrenCount()  + 1)];
                names = new String[(int) (snapshot.getChildrenCount() + 1)];
                imagesURL = new String[(int) (snapshot.getChildrenCount()  + 1)];
                coordinates[0] = currUser.getLatLon();
                times[0] = "Now";
                names[0] = "Me";
                imagesURL[0] = currUser.getImageURL();
                int i = 1;
                for (DataSnapshot snap : snapshot.getChildren()) {
                    int finalI = i;
                    userRef.child(snap.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User friend = new User(snapshot.child("id").getValue().toString(), snapshot.child("imageURL").getValue().toString(), snapshot.child("username").getValue().toString());
                            if(snapshot.child("latitude").getValue() != null){
                                friend.setLatitude(Double.parseDouble(snapshot.child("latitude").getValue().toString()));
                                friend.setLongitude(Double.parseDouble(snapshot.child("longitude").getValue().toString()));
                                friend.setLastLocationTime(snapshot.child("lastLocationTime").getValue().toString());
                                coordinates[finalI] = friend.getLatLon();
                                times[finalI] = friend.getLastLocationTime();
                                names[finalI] = friend.getUsername();
                                imagesURL[finalI] = friend.getImageURL();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }

                    });
                    i++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", (dialog, which) -> {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            replaceFragments(MapsFragment2.class);
        }).setNegativeButton("No", (dialog, which) -> {
            replaceFragments(MessagesFragment.class);
            dialog.cancel();
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private String getCurrDate(){
        String date;
        Date d = new Date();
        date = String.valueOf(d.getYear()) + "." + String.valueOf(d.getMonth()) +"."+ String.valueOf(d.getDate()) +"."+ String.valueOf(d.getHours() - (TimeZone.getDefault().getDSTSavings() + TimeZone.getDefault().getRawOffset())/3600000) +"."+ String.valueOf(d.getMinutes())+"."+ String.valueOf(d.getSeconds());
        return date;
    }
    private void getCurrUser(){
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase.getInstance().getReference("Users")
                .child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currUser = snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getLocation(MapboxMap mapboxMap, Style style) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (ActivityCompat.checkSelfPermission(
                getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                getContext(),Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    if (location != null) {
                        double lat = location.getLatitude();
                        double longi = location.getLongitude();
                        latitude = String.valueOf(lat);
                        longitude = String.valueOf(longi);
                        CameraPosition newCameraPosition = new CameraPosition.Builder()
                                .target(new LatLng(lat, longi))
                                .zoom(15)
                                .build();
                        mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition));
                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(firebaseUser.getUid()).child("latitude").setValue(lat);
                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(firebaseUser.getUid()).child("longitude").setValue(longi);
                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(firebaseUser.getUid()).child("lastLocationTime").setValue(getCurrDate()).addOnCompleteListener(task -> findFirebaseUser());
                        getCurrUser();
                    } else {
                        Toast.makeText(getActivity(), "Unable to find location.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    // Add the mapView lifecycle to the activity's lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * POJO model class for a single location in the recyclerview
     */
    class SingleRecyclerViewLocation {

        private String name;
        private String bedInfo;
        private LatLng locationCoordinates;
        String image;
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getBedInfo() {
            return bedInfo;
        }

        public void setBedInfo(String bedInfo) {
            this.bedInfo = bedInfo;
        }

        public LatLng getLocationCoordinates() {
            return locationCoordinates;
        }

        public void setLocationCoordinates(LatLng locationCoordinates) {
            this.locationCoordinates = locationCoordinates;
        }

        public void setImage(String s) {
            this.image = s;
        }

        public String getImage() {
            return image;
        }
    }

    static class LocationRecyclerViewAdapter extends
            RecyclerView.Adapter<LocationRecyclerViewAdapter.MyViewHolder> {

        private List<SingleRecyclerViewLocation> locationList;
        private MapboxMap map;
        private Context context;
        public LocationRecyclerViewAdapter(List<SingleRecyclerViewLocation> locationList, MapboxMap mapBoxMap, Context context) {
            this.locationList = locationList;
            this.map = mapBoxMap;
            this.context = context;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_on_top_of_map_card, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            SingleRecyclerViewLocation singleRecyclerViewLocation = locationList.get(position);
            holder.name.setText(singleRecyclerViewLocation.getName());
            if(singleRecyclerViewLocation.getBedInfo() != "Now") {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy - HH:mm");
                holder.numOfBeds.setText(dateFormat.format(Utils.convertStringToDate(singleRecyclerViewLocation.getBedInfo())));
            }else{
                holder.numOfBeds.setText(singleRecyclerViewLocation.getBedInfo());
            }
            Glide.with(context).load(FirebaseStorage.getInstance().getReferenceFromUrl(singleRecyclerViewLocation.getImage())).centerCrop().into(holder.image);
            holder.setClickListener(new ItemClickListener() {
                @Override
                public void onClick(View view, int position) {
                    LatLng selectedLocationLatLng = locationList.get(position).getLocationCoordinates();
                    CameraPosition newCameraPosition = new CameraPosition.Builder()
                            .target(selectedLocationLatLng)
                            .zoom(20)
                            .build();
                    map.easeCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition));
                }
            });
        }

        @Override
        public int getItemCount() {
            return locationList.size();
        }

        static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView name;
            TextView numOfBeds;
            CardView singleCard;
            CircularImageView image;
            ItemClickListener clickListener;

            MyViewHolder(View view) {
                super(view);
                image = view.findViewById(R.id.image);
                name = view.findViewById(R.id.location_title_tv);
                numOfBeds = view.findViewById(R.id.location_num_of_beds_tv);
                singleCard = view.findViewById(R.id.single_location_cardview);
                singleCard.setOnClickListener(this);
            }

            public void setClickListener(ItemClickListener itemClickListener) {
                this.clickListener = itemClickListener;
            }

            @Override
            public void onClick(View view) {
                clickListener.onClick(view, getLayoutPosition());
            }
        }
    }

    public interface ItemClickListener {
        void onClick(View view, int position);
    }
}