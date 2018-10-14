package com.example.darrencs.cryptograph;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CoinList extends AppCompatActivity {
    static List<String> coinList = new ArrayList<String>();
    static List<String> imageLinks = new ArrayList<>();
    static List<String> coinName = new ArrayList<>();
    CustomListViewAdapter adapter;
    Handler handler = new Handler();

    String[] images;
    String[] coins;
    String[] coinNames;
    Context context;
    SharedPreferences sortedPref;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    List<RowItem> rowItems;
    SingletonClass instance = SingletonClass.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_coin_list);

        instance.clearFavouriteCoinListActivity();
        layoutManager = new LinearLayoutManager(this);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(layoutManager);
        context = getApplicationContext();
        ExecutorService es = Executors.newCachedThreadPool();
        ExecutorService es2 = Executors.newCachedThreadPool();
        //Get Shared Preference For Sort
        sortedPref = PreferenceManager.getDefaultSharedPreferences(context);


        // load from res and display then load from internal storage cache and display

        try {
            es.execute(new Runnable() {
                public void run() {
                    try {
                        populateCoinList();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });


            es.shutdown();
            try {
                es.awaitTermination(500, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            setAdapter();

            // then attempt to download a brand new cache is possible

            if (hasConnection()) {
                es2.execute(new Runnable() {
                    public void run() {
                        if (readFromFile(getContext()) != null) {

                            setAdapter();
                        }
                        getApiResponse("https://min-api.cryptocompare.com/data/all/coinlist");
                        setAdapter();
                    }
                });
            }
            es2.shutdown();
            try {
                es2.awaitTermination(500, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
        }
    }

    public void onPause() {
        super.onPause();
    }

    public Context getContext() {
        return context;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setAdapter() {
        instance.clearFavouriteCoinListActivity();
        images = new String[imageLinks.size()];
        images = imageLinks.toArray(images);

        coins = new String[coinList.size()];
        coins = coinList.toArray(coins);

        coinNames = new String[coinName.size()];
        coinNames = coinName.toArray(coinNames);

        rowItems = new ArrayList<RowItem>();

        Log.i("images length", "" + images.length);
        Log.i("coins length", "" + coins.length);
        Log.i("coinNames length", "" + coinNames.length);

        for (int i = 0; i < coinNames.length; i++) {
            //RowItem item = new RowItem(images[i], coinNames[i], coins[i]);
            RowItem item = new RowItem(recyclerView);
            item.setImageId(images[i]);
            item.setName(coinNames[i]);
            item.setCoinTitle(coins[i]);
            item.setAdapterPostion(i);
            // item.clearChecked();
            rowItems.add(item);
        }

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        Boolean checkSort = sortedPref.getBoolean("sort_settings", false);

        if (!checkSort) {
            Collections.sort(rowItems, RowItem.Comparators.title);
            Log.d("SharedPref Sort", "false");
        } else {
            Collections.sort(rowItems, RowItem.Comparators.name);
            Log.d("SharedPref Sort", "true");
        }

        adapter = new CustomListViewAdapter(context, rowItems);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(adapter);

    }


    public void onResume() {
        super.onResume();
        instance.clearFavouriteCoinListActivity();

        // Boolean checkSort = sortedPref.getBoolean("sort_settings", false);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean checkSort = pref.getBoolean("change_sort_by", false);
        Log.d("Checksort", String.valueOf(checkSort));
        Log.d("pref", pref.toString());
        if (!checkSort) {
            Collections.sort(rowItems, RowItem.Comparators.title);
            Log.d("SharedPref Sort", "false");
        } else {
            Collections.sort(rowItems, RowItem.Comparators.name);
            Log.d("SharedPref Sort", "true");
        }
        adapter.notifyDataSetChanged();
    }

    public void onCheckboxClicked(View view) {
        instance = SingletonClass.getInstance();

        boolean checked = ((CheckBox) view).isChecked();
        if (checked) {
            adapter.setCheckbox((Integer) view.getTag());
            instance.appendFavouriteRowItems(rowItems.get((Integer) view.getTag()));
        } else {
            adapter.clearCheckbox((Integer) view.getTag());
            instance.removeFavouriteRowItems(rowItems.get((Integer) view.getTag()));
        }
    }

    public void getApiResponse(String link) {
        final String url = link;
        String filename = "cachedJson";
        OutputStreamWriter outputStreamWriter = null;
        try {
            outputStreamWriter = new OutputStreamWriter(getContext().openFileOutput(filename, Context.MODE_PRIVATE));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // write out to internal storage, doesn't require permissions
        OutputStreamWriter finalOutputStreamWriter = outputStreamWriter;
        new Thread(new Runnable() {
            public void run() {
                JsonRetrieve json = new JsonRetrieve();
                JSONObject response = json.doInBackground(url);
                try {
                    JSONObject jsonResponse = response.getJSONObject("Data");
                    String help = String.valueOf(jsonResponse);
                    try {
                        finalOutputStreamWriter.write(help);
                        finalOutputStreamWriter.close();
                        Log.d("output file", finalOutputStreamWriter.toString());
                    } catch (Exception APIFail) {
                        Log.d("API FAIL", APIFail.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private String readFromFile(Context context) {

        String ret = null;
        // attempt to read from internal storage cache, if first installation and no internet this will be empty
        try {
            InputStream inputStream = context.openFileInput("cachedJson");

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }


    public void populateCoinList() throws JSONException {
        JSONObject response = null;
        // read from internal
        String getFromApi = readFromFile(context);
        try {
            //load from res object initially
            response = new JSONObject(loadJSONFromAsset(context));
        } catch (Exception e) {
            Log.i("loadJSONFromAsset Fail:", e.toString());
        }
        populateNames(response);
        // then load from internal
        JSONObject response2 = new JSONObject(getFromApi);

        populateNames(response2);
    }

    public static boolean isNetworkAvailable(Context context, int[] networkTypes) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            for (int networkType : networkTypes) {
                NetworkInfo netInfo = cm.getNetworkInfo(networkType);
                if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public boolean hasConnection() {
        Boolean mobileData = sortedPref.getBoolean("change_update", false);
        if (isNetworkAvailable(context, new int[]{ConnectivityManager.TYPE_WIFI})) {
            return true;
        } else if (mobileData) {
            if (isNetworkAvailable(context, new int[]{ConnectivityManager.TYPE_MOBILE})) {
                return true;
            }
        }
        return false;
    }

    public void populateNames(JSONObject rawJSON) {
        JSONObject data = null;
        Iterator<?> keys = null;
        try {
            data = rawJSON.getJSONObject("Data");

            keys = data.keys();
        } catch (Exception e) {
            Log.i("Get Data Object: ", e.toString());
            data = rawJSON;
        }
        String CoinName = "Empty";
        Integer count = 0;
        if (keys != null) {

            while (keys.hasNext()) { //TODO remove limiter
                count++;
                try {
                    String key = (String) keys.next();
                    JSONObject coin = data.getJSONObject(key);
                    if ((coin.get("IsTrading").toString()) == "true") {
                        CoinName = (coin.get("Name").toString());
                        if (!coinList.contains(CoinName.replaceAll("\\s+", ""))) {
                            imageLinks.add(("https://www.cryptocompare.com") + (coin.get("ImageUrl")).toString());
                            coinList.add(CoinName.replaceAll("\\s+", ""));
                            coinName.add(coin.get("CoinName").toString().replaceAll("\\s+", ""));
                        }
                    }
                } catch (JSONException e) {
                    Log.i("Error", e.toString());
                } catch (Exception e) {
                    Log.i("Error", e.toString());
                }
            }

            Log.i("Link Transcribe", "Complete");
        }
    }


    public void onSaveInstanceState(Bundle savedInstanceState) {

        if (instance.getFavouriteRowItems() != null) {
            String[] favourites = new String[instance.getFavouriteRowItems().size()];
            for (int x = 0; x < instance.getFavouriteRowItems().size(); x++) {
                favourites[x] = instance.getFavouriteRowItems().get(x).getCoinTitle();
            }
            savedInstanceState.putStringArray("Favourites", favourites);
            // Always call the superclass so it can save the view hierarchy state
            super.onSaveInstanceState(savedInstanceState);
        }
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey("Favourites")) {
            for (int x = 0; x < savedInstanceState.getStringArray("Favourites").length; x++) {
                Log.d("FAVOURITES", (savedInstanceState.getStringArray("Favourites"))[x]);
            }
        }

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    public String loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("coinlist.json");

            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");

        } catch (IOException ex) {
            ex.printStackTrace();
            Log.i("LoadJSONFromAsset Error", "");
            return null;
        }
        return json;
    }
}

