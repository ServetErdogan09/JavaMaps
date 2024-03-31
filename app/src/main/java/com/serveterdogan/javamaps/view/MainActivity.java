package com.serveterdogan.javamaps.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;

import com.serveterdogan.javamaps.R;
import com.serveterdogan.javamaps.adapter.PlaceAdaptor;
import com.serveterdogan.javamaps.databinding.ActivityMainBinding;
import com.serveterdogan.javamaps.model.Place;
import com.serveterdogan.javamaps.roomdb.PlaceDao;
import com.serveterdogan.javamaps.roomdb.PlaceDatabase;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

   private ActivityMainBinding binding;

   private final CompositeDisposable compositeDisposable = new CompositeDisposable();

   PlaceDatabase db;

   PlaceDao placeDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        db = Room.databaseBuilder(getApplicationContext(),PlaceDatabase.class,"Places").build();
        placeDao = db.placeDao();

        compositeDisposable.add(
                placeDao.getAll()
                        .subscribeOn(Schedulers.io()) // IO iş parçacığında çalışacak
                        .observeOn(AndroidSchedulers.mainThread()) // UI da işlenecek
                        .subscribe(MainActivity.this:: handlerResponse)); // handlerResponse methodu çağrılarak işlem sonucu işler
    }
    public void handlerResponse(List<Place> placeList){

            binding.recyclerViewTest.setLayoutManager(new LinearLayoutManager(this));
            PlaceAdaptor placeAdaptor = new PlaceAdaptor(placeList);
            binding.recyclerViewTest.setAdapter(placeAdaptor);

    }

    @Override
    // menu oluştuğunda yapılacakalr
    public boolean onCreateOptionsMenu(Menu menu) {
        // bu yöntem activitey içinde tanımlı olduğu için Activity'nin bağlamını otomatik olarak alır ve buna göre bir MenuInflater örneği oluşturur.
         MenuInflater menuInflater = getMenuInflater();
       // MenuInflater menuInflater1 = new MenuInflater(this);
         menuInflater.inflate(R.menu.travel,menu);
         return super.onCreateOptionsMenu(menu);
    }


    @Override

    //menu 'a  basıldığında yapılacaklar
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.travel_menu){
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            intent.putExtra("info","new");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onDestroy() {
        super.onDestroy();
        // içindeki tüm abonelikleri iptal etmek için kullanılır.
        compositeDisposable.clear();
    }

}



