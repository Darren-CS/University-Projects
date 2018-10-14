package com.example.darrencs.cryptograph;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

class JsonRetrieve extends AsyncTask<String, Void, JSONObject> {

    public static JSONObject json_parse(String address) {
        URL url;
        try {
            String link = URLEncoder.encode(address, "UTF-8");
            url = new URL(address);

            //read from the URL
            String str = "";
            Scanner scan = new Scanner(url.openStream());

            while (scan.hasNext()) {
                str += scan.nextLine();
            }
            scan.close();


            JSONObject obj = new JSONObject(str);

            if (!obj.optString("status").equals("OK")) {
                System.out.print(obj);
                Log.d("Response", obj.toString());
                return obj;
            }

            return null;

        } catch (JSONException j)

        {
            throw new AssertionError("Error" + j);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected JSONObject doInBackground(String... strings) {
        return json_parse(strings[0]);

    }
}

