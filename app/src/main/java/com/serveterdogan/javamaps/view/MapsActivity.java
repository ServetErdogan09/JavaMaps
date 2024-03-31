package com.serveterdogan.javamaps.view;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.serveterdogan.javamaps.R;

import com.serveterdogan.javamaps.databinding.ActivityMapsBinding;
import com.serveterdogan.javamaps.model.Place;
import com.serveterdogan.javamaps.roomdb.PlaceDao;
import com.serveterdogan.javamaps.roomdb.PlaceDatabase;



import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

import io.reactivex.rxjava3.schedulers.Schedulers;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback ,GoogleMap.OnMapLongClickListener{

    /*
      Sonuç olarak, SQLLite ve Room Database, Android uygulamalarında yerel veritabanı
      depolamasını sağlar. SQLLite, yerleşik bir veritabanı motorudur ve temel veritabanı
      işlemlerini gerçekleştirmek için SQL sorguları kullanırken, Room, SQLite üzerine
      bir katman olarak inşa edilmiş bir kütüphanedir ve veritabanı işlemlerini
      kolaylaştırmak için ORM prensiplerini kullanır. Room, genellikle daha
      kolay kullanım ve daha yüksek düzeyde güvenlik sağlar, ancak performans
      bakımından SQLLite ile benzerdir.
     */

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    // izinleri tutacak
    ActivityResultLauncher<String> activityResultLauncher;

    LocationManager locationManager;
    LocationListener locationListener;

    SharedPreferences sharedPreferences;
    boolean info;

    PlaceDatabase db;

    PlaceDao placeDao;

    double selectedLatitude;
    double selectedLongitude;

    Place selectedPlace;

 // birden çok aboneliği tek bir koleksiyon içinde yönetmeyi sağlar. Bu koleksiyon, içindeki tüm abonelikleri tek seferde iptal etmenize olanak tanır.
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    //tek bir aboneliği temsil eder. Bir Observable veya Flowable'a abone olduğunuzda,
    // bu aboneliği Disposable türünden bir değişkene atayabilirsiniz.
    // Bu sayede, abonelik iptal edilmek istendiğinde bu Disposable değişkenini kullanarak aboneliği iptal edebilirsiniz.


    /*
    ActivityResultLauncher nesnesi, izin isteği göndermek ve
    izin isteği sonuçlarını işlemek için kullanılır. Bu nesne,
    izin isteği gönderirken ve sonuçları işlerken kullanılan bir arayüzdür.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        sharedPreferences = MapsActivity.this.getSharedPreferences("com.serveterdogan.javamaps",MODE_PRIVATE);
        info = false;

        // bunu onCreat içinden başlatıyoruz ki activityResultLauncher başlatıyoruz
        registerLauncher();

        // Places adında Room veri tabanı oluşturacak
        db = Room.databaseBuilder(getApplicationContext(),PlaceDatabase.class,"Places")
               // .allowMainThreadQueries() tavsiye edilmez büyük verilerle uğraştığımız için terih edilmez
                .build();

        placeDao = db.placeDao();
        selectedLatitude = 0.0;
        selectedLongitude=0.0;

    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        binding.saveButton.setEnabled(false);
        Intent intent = getIntent();

        String newInfo = intent.getStringExtra("info");

        if (newInfo != null && newInfo.equals("new")) {

            binding.saveButton.setVisibility(View.VISIBLE);
            // GONE ayarladığımız zaman buttonu görünmüyecek button
            binding.deleteButton.setVisibility(View.GONE);
            // uzun tıklama dinleycisini ayaraladık
            mMap.setOnMapLongClickListener(this);
            // ANDROİD işletim sistemine erişim sağlıyor
            //konumun değişip değişmediğine dinliyor
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            // değişiklikleri diniyor ve bize onLocationChanged methodunu veriyor değişen konum olursa işlemleri o method altında yapacağız
            locationListener = new LocationListener() {
                @Override
                // konum değişikliğinde yapılacaklar
                public void onLocationChanged(@NonNull Location location) {

                    info = sharedPreferences.getBoolean("info", false);
                    if (!info) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                        sharedPreferences.edit().putBoolean("info", true).apply();
                    }

                    binding.saveButton.setEnabled(true);

                }
            };

            //  PackageManager içinden izin var mı diye kontrol edecek
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // istek izin gerekçesini gösterelim mi
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                    Snackbar.make(binding.getRoot(), "Permission needed for maps ", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // request permission
                            // hangi izni istediğimizi söylüyooruz
                            activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                        }
                    }).show();


                } else {
                    //request permission
                    activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                }


            } else {

                //  güncel konumunu alıyoruz
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                // son bilinen konumu LocationManager içindeki GPS_PROVIDER'dan al
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (lastLocation != null) {
                    LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLatitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15));
                }

                // benim konumumun doğru olduğundan emin ol
                mMap.setMyLocationEnabled(true);
            }


        }


    }

    private void registerLauncher(){

        // izin isteği göndermek ve izin isteği sonuçlarını işlemek için kullanılır
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {

                if (result){
                    // permission granted
                    if (ContextCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ){
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                        Location lastLocation =   locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        if (lastLocation != null){
                            LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(),lastLocation.getLatitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                        }

                    }
                }else {
                    // permission  denied
                    Toast.makeText(MapsActivity.this,"permission needed!!",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng));

        selectedLongitude = latLng.longitude;
        selectedLatitude = latLng.latitude;
        binding.saveButton.setEnabled(true);

    }

    public void save(View view){


        Place place = new Place(binding.placeNameText.getText().toString(),selectedLatitude,selectedLongitude);
        // insert methodunu çağırırlarak place adında bir nesneyi veri tabanına eklemke için iş parçacaığını kullanıcı arayüzünde(UI) de çalıştırılımadan Schedulers.io() kullanarakta IO iş parçacığında çalışmasını  sağlıyoruz
        //subscribeOn(Schedulers.io()) ifadesi, placeDao.insert(place) gibi bir işlemi IO iş parçacığında çalıştırmak istediğimizi belirtir

        /*
        gibi bir işlemi IO iş parçacığında çalıştırmak istediğimizi belirtir.
         Bu işlem, veritabanına erişim gibi IO işlemlerini içerir ve bu tür
         işlemler için IO iş parçacığı tercih edilir çünkü bu iş parçacığı,
         uzun süren IO işlemlerini etkili bir şekilde yönetebilir ve ana iş
         parçacığını (UI iş parçacığı gibi) kilitlenmeden tutar.
         */

         // placeDao.insert(place).subscribeOn(Schedulers.io()).subscribe(); çalışır ama  verimsiz

        // Disposable

        // tek kullanımlık  abone ekliyoruz olacak place insert edeceğiz IO içinde main thread de gözlemliyeceğiz sonda da abone ol diyoruz ve Observable ve flowable üzerinde veri akışını dinliyoruz
        //// Veritabanı işlemi asenkron olarak gerçekleştirilir
        if (place.name.isEmpty()){
            Toast.makeText(MapsActivity.this,"Enter Place Name",Toast.LENGTH_SHORT).show();
        }else {

            compositeDisposable.add(placeDao.insert(place)
                    .subscribeOn(Schedulers.io()) // IO iş parçacığında çalışır
                    .observeOn(AndroidSchedulers.mainThread()) // Ana iş parçacığında sonucu işler
                    .subscribe(MapsActivity.this :: handleResponse)); // handleResponse metodunu çağırarak işlem sonucunu işler

        }


    }

    public void handleResponse(){

        Intent intent = new Intent(MapsActivity.this ,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // butun activiteleri kapat
        startActivity(intent);


    }

    public void delete(View view){

if (selectedPlace!=null){
    compositeDisposable.add(placeDao.delete(selectedPlace)
            .subscribeOn(Schedulers.io()) // // IO iş parçacığında çalışır
            .observeOn(AndroidSchedulers.mainThread()) // Ana iş parçacığında sonucu işler
            .subscribe(MapsActivity.this :: handleResponse)); // handleResponse metodunu çağırarak işlem sonucunu işler

}



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // içindeki tüm abonelikleri iptal etmek için kullanılır.
        compositeDisposable.clear();
    }
}