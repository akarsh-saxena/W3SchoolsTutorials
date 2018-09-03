package com.application.akarsh.w3schoolstutorials;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.multidex.MultiDex;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ListView listView;
    ArrayAdapter adapter;
    List<String> array = new ArrayList<>();
    List<String> links = new ArrayList<>();
    LinkedHashMap<String, String> values = new LinkedHashMap<>();
    public static NavigationView navigationView;
    private AdView mAdView;

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MobileAds.initialize(this, Constants.AD_MOB_ID);
        mAdView = findViewById(R.id.adView);
        //AdRequest adRequest = new AdRequest.Builder().addTestDevice("A97963B0F148599231DA2CAAD151C7D6").build();
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        listView = findViewById(R.id.listView);
        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,array);

        listView.setOnItemClickListener(
                new ListView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(MainActivity.this, Tutorial.class);
                        intent.putExtra("language", links.get(position));
                        intent.putExtra("base_url", links.get(position));
                        intent.putExtra("group_name", listView.getItemAtPosition(position).toString());
                        Log.d("myonclick",new ArrayList<>(values.values()).get(position));
                        startActivity(intent);
                    }
                }
        );

        final ProgressDialog p = new ProgressDialog(this);
        p.setTitle("Loading");
        p.setMessage("Please Wait..");
        p.show();

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest getTitles = new StringRequest(Request.Method.POST, Constants.MAIN_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Document document = Jsoup.parse(response);
                        Element parseElement = document.getElementById("mySidenav");
                        Elements divElements = parseElement.getElementsByClass("w3-bar-block");
                        Elements mainLinks = divElements.select("a");

                        for (Element element : mainLinks) {
                            element.setBaseUri(Constants.MAIN_URL);
                            array.add(element.text());
                            element.setBaseUri(Constants.MAIN_URL);
                            links.add(element.attr("abs:href"));
                            //adapter.notifyDataSetChanged();
                            adapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1,array);
                            listView.setAdapter(adapter);
                            //MenuItem menuItem = menu.add(element.text());
                            values.put(element.text(), element.attr("abs:href"));
                        }
                        p.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        p.dismiss();
                        error.printStackTrace();
                    }
                });
        requestQueue.add(getTitles);
    }

    long time1;
    boolean singleBack = false, firstTime = true;

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (!singleBack) {
                if(!firstTime) {
                    Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
                    time1 = System.currentTimeMillis();
                }
                time1 = System.currentTimeMillis();
                Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
                singleBack = true;
            } else {
                if (System.currentTimeMillis() - time1 <= 2000) {
                    singleBack = false;
                    super.onBackPressed();
                }
                else {
                    singleBack = true;
                    Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
                    time1 = System.currentTimeMillis();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

        if (id == R.id.favorites) {
            Intent intent = new Intent(this, Favorites.class);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
