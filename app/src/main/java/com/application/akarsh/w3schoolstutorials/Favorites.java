package com.application.akarsh.w3schoolstutorials;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Favorites extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    DatabaseHelper databaseHelper;
    NavigationView navigationView;
    WebView webView;
    List<String> links = new ArrayList<>();
    List<DatabaseModel> databaseModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.favorite_added);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatabaseHelper databaseHelper = new DatabaseHelper(Favorites.this);
                if(databaseHelper.removeFavorite(webView.getUrl()))
                    Snackbar.make(view, "Favorite Removed", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                else
                    Snackbar.make(view, "Error Removing Favorite", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                if(getFavorites() == -1)
                    finish();
                else {
                    webView.loadUrl(links.get(0));
                    navigationView.getMenu().getItem(0).setChecked(true);
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);

        if(getFavorites() == -1) {
            finish();
            MainActivity.navigationView.getMenu().getItem(0).setChecked(false);
        }
        else {
            webView.loadUrl(links.get(0));
            navigationView.getMenu().getItem(0).setChecked(true);
        }
    }

    int getFavorites() {
        navigationView.getMenu().clear();
        links.clear();
        databaseHelper = new DatabaseHelper(this);
        /*links = databaseHelper.getLink();
        for(int i=0; i<links.size(); ++i) {
            navigationView.getMenu().add(databaseHelper.getNames(links.get(i)));
        }*/
        int count=0;
        databaseModels = databaseHelper.getDetails();
        if(databaseModels == null) {
            Toast.makeText(this, "No Favorites", Toast.LENGTH_SHORT).show();
            Log.d("alka", "No");
            return -1;
        }
        for(DatabaseModel databaseModel : databaseModels) {
            MenuItem menuItem = navigationView.getMenu().add(0, count, Menu.NONE, databaseModel.getName());
            menuItem.setCheckable(true);
            links.add(databaseModel.getLink());
            count++;
        }
        return 1;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.favorites, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        webView.loadUrl(links.get(id));
        Toast.makeText(this, "Loading: "+links.get(id), Toast.LENGTH_SHORT).show();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
