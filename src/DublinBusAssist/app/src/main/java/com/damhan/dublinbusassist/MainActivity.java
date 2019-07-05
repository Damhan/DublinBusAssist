package com.damhan.dublinbusassist;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle( this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if(savedInstanceState ==null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new StopNoFragment()).commit();
            navigationView.setCheckedItem(R.id.stop_no_menu);
        }



//        Button searchStopNo = (Button) findViewById(R.id.buttonStopNo);
//        searchStopNo.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//
//            public void onClick(View v) {
//
//                goToStopNoActivity();
//
//            }
//
//        });
//
//        Button searchRoute = (Button) findViewById(R.id.buttonRoute);
//        searchRoute.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//
//            public void onClick(View v) {
//
//                goToRouteActivity();
//
//            }
//
//        });



    }

//    private void goToRouteActivity() {
//
//        Intent intent = new Intent(this, SearchByRouteActivity.class);
//
//        startActivity(intent);
//
//    }
//
//    private void goToStopNoActivity() {
//
//        Intent intent = new Intent(this, SearchByStopNoActivity.class);
//
//        startActivity(intent);
//
//    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.stop_no_menu:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new StopNoFragment()).commit();
                break;

            case R.id.settings_menu:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commit();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }


}
