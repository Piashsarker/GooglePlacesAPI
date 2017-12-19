package com.dcastalia.androidplacepickerexample;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {
    private final int LOCATION_PERMISSION_REQUEST_CODE = 1001 ;
    private final int REQUEST_PLACE_PICKER = 1002 ;
    private TextView name , address , phone , placeId , webUri , latlng , priceLevel , rating  ;
    private ImageView placeImage ;
    private GeoDataClient mGeoDataClient;
    private Context context ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGeoDataClient   = Places.getGeoDataClient(this, null);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        context = MainActivity.this;
        setSupportActionBar(toolbar);
        findViews();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Utils.chekcGPSEnable(context)){
                    checkPermission();
                }else{
                    Utils.showGPSDialog(context);
                }

            }
        });
    }

    private void findViews() {
        name  = findViewById(R.id.place_name);
        address   = findViewById(R.id.place_address);
        phone = findViewById(R.id.place_phone);
        placeId = findViewById(R.id.place_id);
        webUri = findViewById(R.id.place_url);
        latlng = findViewById(R.id.place_latlng);
        priceLevel = findViewById(R.id.place_price_level);
        rating  = findViewById(R.id.place_rating);
        placeImage = findViewById(R.id.place_image);
    }

    private void checkPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            //Start Map Work
            openPlacePicker();
        }
        else{
            requestLocationPermission();
        }
    }

    private void openPlacePicker() {

        try {
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();

            Intent intent = intentBuilder.build(this);
            startActivityForResult(intent ,REQUEST_PLACE_PICKER );
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
            Toast.makeText(this, "Google Play Services is not available.",
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void requestLocationPermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
        else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode==REQUEST_PLACE_PICKER){
            if(resultCode == Activity.RESULT_OK){
               final Place place = PlacePicker.getPlace(this , data);
               if(place!=null){
                   if(place.getId()!=null){
                       placeId.setText(place.getId());
                       getPhotos(place.getId());
                   }
                   if(place.getName()!=null){
                       name.setText(place.getName());
                   }
                   if(place.getAddress()!=null){
                       address.setText(place.getAddress());
                   }
                   if(place.getPhoneNumber()!=null){
                       phone.setText(place.getPhoneNumber());
                   }
                   if(place.getPriceLevel()!=0){
                      priceLevel.setText(String.valueOf(place.getPriceLevel()));
                   }
                   if(place.getLatLng()!=null){
                       String  lat = place.getLatLng().toString() ;
                       latlng.setText(lat);
                   }

                   if(place.getWebsiteUri()!=null){
                       webUri.setText(place.getWebsiteUri().toString());
                   }
                   if(place.getRating()!=-1){
                       rating.setText(String.valueOf(place.getRating()));
                   }

               }
            }
        }
        else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    private void getPhotos(String placeId) {
        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(placeId);
        photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                // Get the list of photos.
                PlacePhotoMetadataResponse photos = task.getResult();
                // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
                PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                // Get the first photo in the list.
                PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(0);
                // Get the attribution text.
                CharSequence attribution = photoMetadata.getAttributions();
                // Get a full-size bitmap for the photo.
                Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
                photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                        PlacePhotoResponse photo = task.getResult();
                        Bitmap bitmap = photo.getBitmap();
                        placeImage.setImageBitmap(bitmap);
                    }
                });
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==LOCATION_PERMISSION_REQUEST_CODE && grantResults.length==1 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            // Start Map Work Here
            openPlacePicker();
        }
        else{
            Snackbar.make(findViewById(android.R.id.content), getResources().getString(R.string.sorry_for_not_permission), Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
