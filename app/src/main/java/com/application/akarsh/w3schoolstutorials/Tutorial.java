package com.application.akarsh.w3schoolstutorials;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.multidex.MultiDex;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SubMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebBackForwardList;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Tutorial extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    EditText etSearch;
    //Button btnSearch;
    WebView webView;
    Document topicDocument;
    Elements topicLinks;
    NavigationView navigationView;
    Menu menu;
    RequestQueue requestQueue;
    String base_url;
    List<String> href_links = new ArrayList<>();
    private AdView mAdView;
    FloatingActionButton fab;
    DatabaseHelper databaseHelper;
    String nameOfTopic, nameOfGroup;
    LinkedHashMap<String, String> values = new LinkedHashMap<>();
    String current_url;
    boolean valueChanged=false;

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MobileAds.initialize(this, Constants.AD_MOB_ID);
        mAdView = findViewById(R.id.adView);
        //AdRequest adRequest = new AdRequest.Builder().addTestDevice("A97963B0F148599231DA2CAAD151C7D6").build();
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        String language = getIntent().getExtras().get("language").toString();
        base_url = getIntent().getExtras().get("base_url").toString();
        base_url = base_url.substring(0, base_url.lastIndexOf('/') + 1);
        nameOfTopic = getIntent().getExtras().get("group_name").toString();
        nameOfGroup = nameOfTopic;

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        menu = navigationView.getMenu();

        requestQueue = Volley.newRequestQueue(this);

        webView = findViewById(R.id.webView);
        //webView.loadUrl(base_url);

        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        loadURL(base_url);
        current_url = base_url;

        fab = findViewById(R.id.fab);

        databaseHelper = new DatabaseHelper(Tutorial.this);
        if(databaseHelper.isFavorite(webView.getUrl()))
            fab.setImageResource(R.drawable.favorite_added);
        else
            fab.setImageResource(R.drawable.favorite_removed);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = webView.getUrl();
                Snackbar.make(view, "Saved: " + url, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Toast.makeText(Tutorial.this, "URL: "+webView.getUrl(), Toast.LENGTH_SHORT).show();
                Toast.makeText(Tutorial.this, "Organized: "+webView.getOriginalUrl(), Toast.LENGTH_SHORT).show();

                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                try {
                    if (!databaseHelper.isFavorite(webView.getUrl())) {
                        if (databaseHelper.insertFavorite(current_url, nameOfTopic, nameOfGroup)) {
                            fab.setImageResource(R.drawable.favorite_added);
                            Toast.makeText(Tutorial.this, "Added to favorites", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(Tutorial.this, "Error adding in favorites", Toast.LENGTH_SHORT).show();
                    } else {
                        if (databaseHelper.removeFavorite(current_url)) {
                            fab.setImageResource(R.drawable.favorite_removed);
                            Toast.makeText(Tutorial.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(Tutorial.this, "Error removing from favorites", Toast.LENGTH_SHORT).show();
                    }
                } catch(IOException io){
                    io.printStackTrace();
                }

            }
        });

        View view = ((NavigationView)findViewById(R.id.nav_view)).getHeaderView(0);
        etSearch = view.findViewById(R.id.etSearch);

        etSearch.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        menu.clear();
                        for(Map.Entry<String, String> e : values.entrySet()) {
                            if(e.getKey().toLowerCase().contains(editable.toString().toLowerCase())) {
                                Toast.makeText(Tutorial.this, "Contains", Toast.LENGTH_SHORT).show();
                                menu.add(e.getKey());
                            }
                        }
                        valueChanged=true;
                        //etSearch.focus();
                    }
                }
        );

        etSearch.setOnFocusChangeListener(
                new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean b) {
                        if(!b && valueChanged)
                            etSearch.requestFocus();
                    }
                }
        );

        getTopicList(language);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(etSearch.hasFocus())
            etSearch.clearFocus();
        if(webView.canGoBack()) {
            WebBackForwardList history = webView.copyBackForwardList();
            for(int i=0; i<history.getSize(); ++i)
                Log.d("History: ",history.getItemAtIndex(i).getUrl());
            Toast.makeText(this, "WebView can go back", Toast.LENGTH_SHORT).show();
            webView.goBack();
            return true;
        }
        else
            Toast.makeText(this, "WebView cannot go back", Toast.LENGTH_SHORT).show();
        return super.onKeyDown(keyCode, event);
    }

    public void getTopicList(final String url) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        topicDocument = Jsoup.parse(response);
                        Element partsElement = topicDocument.getElementById("leftmenuinnerinner");
                        topicLinks = partsElement.select("a");
                        int item_id=0;
                        for (Element element : topicLinks) {
                            //Log.d("elements", element.text() + ": " + element.attr("href"));
                            //Log.d("elements", "Element: " + element.getElementsContainingText(element.text()).attr("href"));
                            if(item_id==0) {
                                item_id++;
                                continue;
                            }
                            //MenuItem item = menu.add(0, item_id, Menu.NONE, element.text());
                            //item_id++;
                           // item.setCheckable(true);
                            element.setBaseUri(url);
                            //String link = element.attr("abs:href");
                            //href_links.add(link);
                            values.put(element.text(), element.attr("abs:href"));
                            //DatabaseModel dm = new DatabaseModel();
                            //dm.setName(element.text());
                            //dm.setLink(link);
                            //databaseModels.add(dm);
                        }
                        //appDrawerListView.setItemChecked();
                        //names.addAll(databaseModels.get
                        //appDrawerListView.setAdapter(arrayAdapter);
                       // arrayAdapter.notifyDataSetChanged();
                        //navigationView.getMenu().getItem(0).setChecked(true);
                        //nameOfTopic = navigationView.getMenu().getItem(0).toString();

                        for(Map.Entry<String, String> key : values.entrySet()) {
                            MenuItem item = menu.add(key.getKey());
                            item.setCheckable(true);
                            //Log.d("myonclick", "Adding: "+key.getKey());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        requestQueue.add(stringRequest);
    }


    @Override
    public void onBackPressed() {
        Toast.makeText(this, "onBackPressed", Toast.LENGTH_SHORT).show();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }/*
        if(etSearch.hasFocus())
            etSearch.clearFocus();

        if(webView.canGoBack()) {
            Toast.makeText(this, "WebView can go back backpressed", Toast.LENGTH_SHORT).show();
            webView.goBack();
        }
        else
            Toast.makeText(this, "WebView cannot go back backpressed", Toast.LENGTH_SHORT).show();
*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tutorial, menu);
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
    public boolean onNavigationItemSelected(final MenuItem item) {

        final ProgressDialog p = new ProgressDialog(this);
        p.setMessage("Loading..");
        p.setTitle("Please Wait");
        p.show();
        //Toast.makeText(this, "Clicked: "+(item.getItemId()-1), Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, "Loading URL: "+href_links.get(item.getItemId()-1), Toast.LENGTH_SHORT).show();
        //webView.loadUrl(href_links.get(item.getItemId()-1));
        //Toast.makeText(this, "Loading: "+values.get(item.toString()), Toast.LENGTH_SHORT).show();

        current_url = values.get(item.toString());

        loadURL(values.get(item.toString()));

        //webView.loadUrl(values.get(item.toString()));
        nameOfTopic=item.toString();

        webView.setWebViewClient(
                new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        //webView.loadUrl("javascript:document.getElementsByClassName('w3-container top')[0].style.disp‌​lay = 'none'");
                        //webView.loadUrl("javascript:(function() {document.getElementsByClassName(\"w3-container top\")[0].style=\"display:none\"; })()");
                        p.dismiss();
                        webView.clearHistory();
                        if(databaseHelper.isFavorite(webView.getUrl()))
                            fab.setImageResource(R.drawable.favorite_added);
                        else
                            fab.setImageResource(R.drawable.favorite_removed);
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                        loadURL(request.toString());
                        //view.loadUrl(request.toString());
                        return super.shouldOverrideUrlLoading(view, request);
                    }
                }
        );

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    void loadURL(final String url) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Document document = Jsoup.parse(response);

                        if(document.getElementsByClass("w3-container top") != null)
                            document.getElementsByClass("w3-container top").remove();

                        if(document.getElementsByClass("w3-card-2 topnav") != null)
                        document.getElementsByClass("w3-card-2 topnav").remove();

                        if(document.getElementsByClass("w3-clear nextprev") != null)
                        document.getElementsByClass("w3-clear nextprev").remove();

                        if(document.getElementsByClass("sidesection") != null)
                        document.getElementsByClass("sidesection").remove();

                        if(document.getElementsByClass("footer") != null)
                        document.getElementById("footer").remove();

                        //document.getElementsByAttribute("3rd party ad content").remove();
                        Toast.makeText(Tutorial.this, "Size: "+document.getElementsByAttributeValue("title", "3rd party ad content").size(), Toast.LENGTH_SHORT).show();

                        if(document.getElementById("mainLeaderboard") != null)
                            document.getElementById("mainLeaderboard").remove();

                        if(document.getElementById("div-gpt-ad-1493883843099-0") != null)
                            document.getElementById("div-gpt-ad-1493883843099-0").remove();

                        //document.getElementById("google_ads_iframe_/16833175/")
                        /*document.getElementById("mainLeaderboard").remove();
                        document.getElementsByClass("w3-col l2 m12").remove();
                        document.getElementById("div-gpt-ad-1493883843099-0").remove();
                        document.getElementsByClass("bottomad").remove();*/
                        Toast.makeText(Tutorial.this, "URL: "+url, Toast.LENGTH_SHORT).show();
                        webView.loadDataWithBaseURL(url, document.toString(), "text/html", "utf-8", url);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        requestQueue.add(stringRequest);

    }
}
