package com.example.darrencs.cryptograph;

import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONObject;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Graph extends AppCompatActivity {

    GraphView graph;
    RowItem topItem;
    SingletonClass instance;
    LineGraphSeries<DataPoint> month = new LineGraphSeries<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph_activity);
        instance = SingletonClass.getInstance();
        graph = new GraphView(this);

        progressBar = findViewById(R.id.progress_loader);

//        GraphView graph = new GraphView(this);
//        graph = (GraphView) findViewById(R.id.graph);
//        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[]{
//                new DataPoint(0, 1),
//                new DataPoint(1, 5),
//                new DataPoint(2, 3)
//        });
//        graph.addSeries(series);
    }

    ProgressBar progressBar;


    public void onResume() {
        super.onResume();
        if (instance.getFavouriteRowItems() != null && instance.getFavouriteRowItems().size() > 0 && CoinList.isNetworkAvailable(this, new int[]{ConnectivityManager.TYPE_WIFI, ConnectivityManager.TYPE_MOBILE})) {
            graph = (GraphView) findViewById(R.id.graph);
            topItem = instance.getFavouriteRowItems().get(0);
            if (topItem != null) {
                String name = topItem.getCoinTitle();
                String image = topItem.getImageId();
                LineGraphSeries<DataPoint> month = new LineGraphSeries<>();
                DataPoint values[] = new DataPoint[30];
                String url;
                ExecutorService es = Executors.newCachedThreadPool();

                es.execute(new Runnable() {
                    public void run() {
                        new getApi(name).execute();
                        progressBar.bringToFront();
                        progressBar.setVisibility(View.VISIBLE);
                    }
                });


                es.shutdown();
                try {
                    es.awaitTermination(500, TimeUnit.DAYS);

                } catch (InterruptedException e) {

                    e.printStackTrace();
                }

            }
        } else {
            if (!(CoinList.isNetworkAvailable(this, new int[]{ConnectivityManager.TYPE_WIFI, ConnectivityManager.TYPE_MOBILE}))) {
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.INVISIBLE);
            } else {
                Toast.makeText(this, "No Favourite Coins Selected", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.INVISIBLE);
            }

        }
    }

    public class getApi extends AsyncTask<String, Void, DataPoint[]> {
        public getApi(String coinName) {
            this.name = coinName;
            unixTime = System.currentTimeMillis() / 1000L;
        }

        String name;
        long unixTime;

        //DataPoint values[] = new DataPoint[30];
        List<DataPoint> values = new Vector<DataPoint>();
        String url;
        Float res;
        private String path = Environment.getExternalStorageDirectory().toString();
        Integer tolerence = 5;

        protected DataPoint[] doInBackground(String... urls) {

            for (int x = 0; x < 30; x++) {
                url = "https://min-api.cryptocompare.com/data/pricehistorical?fsym=" + this.name + "&tsyms=USD&ts=" + unixTime + "&extraParams=your_app_name";

                if (url.length() != 0) {
                    try {
                        JsonRetrieve json = new JsonRetrieve();
                        JSONObject response = json.doInBackground(url);
                        try {
                            JSONObject cur = response.getJSONObject(name);
                            res = Float.valueOf((cur.getString("USD")));

                        } catch (Exception APIFail) {
                            Log.d("API FAIL", APIFail.toString());
                        }

                    } catch (Exception e) {
                    }
                }
                unixTime -= (1000 * 24 * 60);


                DataPoint dataPoint = new DataPoint(x, res);
                if (res != 0) {
                    values.add(dataPoint);
                }
            }

            return values.toArray(new DataPoint[values.size()]);
        }

        public void onPostExecute(DataPoint dataPoint[]) {
            progressBar.setVisibility(View.INVISIBLE);
            month = new LineGraphSeries<>(dataPoint);
            graph.removeAllSeries();
            graph.addSeries(month);

        }
    }
}