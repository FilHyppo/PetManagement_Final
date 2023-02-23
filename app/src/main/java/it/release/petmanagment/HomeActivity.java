package it.release.petmanagment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import it.release.petmanagment.R;
import it.release.petmanagment.databinding.ActivityHomeBinding;
import it.release.petmanagment.login.MainActivity;
import it.release.petmanagment.login.PhotoCreator;
import it.release.petmanagment.ui.Customers.CustomersFragment;
import it.release.petmanagment.ui.home.HomeFragment;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private AdView adView;
    private AdRequest adRequest;
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityHomeBinding binding;

    /* drawerLayout e navigationView servono per rendere il pulsante di logout un semplice pulsante*/
    private NavigationView navigationView;
    private DrawerLayout drawer;
    Bitmap puppet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        //codice per Ads


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
                Toast.makeText(HomeActivity.this,"init completed",Toast.LENGTH_LONG).show();
            }
        });
        adView=findViewById(R.id.bannerAds);
        adRequest=new AdRequest.Builder().build();
        System.out.println(adRequest.getContentUrl());
        adView.loadAd(adRequest);


        //fine codice per Ads




        FragmentManager fragmentManager = getSupportFragmentManager();

        setSupportActionBar(binding.appBarHome.toolbar);
        drawer = binding.drawerLayout;
        navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_customers)//per aggiungere il menu settings incollare nella lista questo: R.id.nav_settings
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        /*codice per fare logout schiacciando il pulsante dal menu a tendina*/
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            drawer.closeDrawer(GravityCompat.START);
            switch (item.getItemId()) {
                case R.id.nav_logout:
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(HomeActivity.this, MainActivity.class));
                    Toast.makeText(HomeActivity.this, "logout successul", Toast.LENGTH_LONG).show();
                    return true;

                case R.id.nav_customers:
                    replaceFragment(CustomersFragment.class, fragmentManager);
                    Objects.requireNonNull(getSupportActionBar()).setTitle("Customers");
                    return true;

                case R.id.nav_home:
                    replaceFragment(HomeFragment.class, fragmentManager);
                    Objects.requireNonNull(getSupportActionBar()).setTitle("Home");

                    return true;

            }
            return true;
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        TextView username = (TextView) findViewById(R.id.mailView);
        //profileImage.setVisibility(View.INVISIBLE);
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            username.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        else
            username.setText("none");
        return true;
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PhotoCreator.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            puppet = (Bitmap) extras.get("data");
        }
    }

    private void replaceFragment(Class fragment, FragmentManager manager) {
        manager.beginTransaction()
                .replace(R.id.nav_host_fragment_content_home, fragment, null)
                .setReorderingAllowed(true)
                .addToBackStack("name") // name can be null
                .commit();
    }

}