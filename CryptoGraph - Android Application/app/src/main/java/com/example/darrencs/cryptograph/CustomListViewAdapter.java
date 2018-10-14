package com.example.darrencs.cryptograph;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import static android.support.v4.content.ContextCompat.checkSelfPermission;

/**
 * Created by DarrenCS on 04/03/18.
 */

public class CustomListViewAdapter extends RecyclerView.Adapter<CustomListViewAdapter.RowViewHolder> {


    List<RowItem> items;
    Context context;
    RowViewHolder holder;
    SingletonClass instance;
    Boolean flag;

    public CustomListViewAdapter(Context context, List<RowItem> items) {
        instance = SingletonClass.getInstance();
        this.flag = instance.getFavouriteCoinListActivity();
        this.context = context;
        this.items = items;
    }

    public CustomListViewAdapter(Context context, List<RowItem> items, Boolean flag) {
        instance = SingletonClass.getInstance();
        this.flag = instance.getFavouriteCoinListActivity();
        this.context = context;
        this.items = items;
    }

    public void itemClick(View view) {
        // allows for the entire row item to be clicked
        CheckBox check = (CheckBox) (view.findViewById(R.id.checkbox));
        boolean checked = check.isChecked();
        if (!checked) {

            setCheckbox((Integer) check.getTag());
            instance.appendFavouriteRowItems(items.get((Integer) check.getTag()));
            check.toggle();

        } else {
            clearCheckbox((Integer) check.getTag());
            instance.removeFavouriteRowItems(items.get((Integer) check.getTag()));
            check.toggle();
        }
        Animation animation1 = new AlphaAnimation(0.3f, 1.0f);
        animation1.setDuration(100);
        view.startAnimation(animation1);
    }

    @Override
    public CustomListViewAdapter.RowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if (!flag) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            this.holder = new RowViewHolder(v);
            v.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    itemClick(v);
                    // onclick for checkbox toggle
                }
            });


        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.favourite_list_item, parent, false);
            this.holder = new RowViewHolder(v);
        }

        v.setOnTouchListener(new OnSwipeTouchListener(context) {
            @Override
            public void onSwipeLeft() {
                // proof of concept
                Toast.makeText(v.getContext(), "LEFT SWIPE", Toast.LENGTH_SHORT).show();
            }
        });

        return holder;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    // bind information to the row items
    public void onBindViewHolder(RowViewHolder holder, int position) {
        holder.imageView.setImageDrawable(null);
        holder.desc.setText(items.get(position).getCoinTitle() + ": " + items.get(position).getCoinName());
        //holder.title.setText(items.get(position).getCoinTitle());

        this.flag = instance.getFavouriteCoinListActivity();
        if (!flag) {
            holder.checkBox.setTag(position);
        } else {
            try {
                new getApi(holder).execute("https://min-api.cryptocompare.com/data/price?fsym=" + items.get(position).getCoinTitle() + "&tsyms=USD");

            } catch (Exception e) {
                holder.price.setText("¬");
                Log.d("Price API Exception", e.toString());
            }
        }
        Bitmap icon = getLocalImage(items.get(position).getCoinName());
        if (!(null == icon)) {
            holder.imageView.setImageBitmap(icon);
        } else {
            if (null != holder) {
                new Download(holder).execute(items.get(position).getImageId(), items.get(position).getCoinName());
            }
        }
        if (!flag) {
            try {
                if (items.get(position).getChecked()) {
                    items.get(position).setChecked();
                    if (!holder.checkBox.isChecked())
                        holder.checkBox.toggle();
                } else {
                    items.get(position).clearChecked();
                    if (holder.checkBox.isChecked())
                        holder.checkBox.toggle();
                }
            } catch (Exception e) {
            }
        }

    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setCheckbox(int position) {
        items.get(position).setChecked();
    }

    public void clearCheckbox(int position) {
        items.get(position).clearChecked();

    }

    // looks for previously downloaded image and displays
    public Bitmap getLocalImage(String coinName) {
        String path = Environment.getExternalStorageDirectory().toString();
        File directory = new File(path + File.separator + "CryptoGraph/Thumbnails/");

        Bitmap bitmap;
        File file = new File(directory + "/" + coinName + ".bmp");
        if (checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            if (file.exists()) {
                //load image from storage
                Bitmap image = BitmapFactory.decodeFile(file.toString());
                if (image != null) {
                    //holder.imageView.setImageBitmap(image);
                    Log.i("Image Loaded from local", file.toString());
                    return image;
                }
            }
        }
        return null;
    }

    public static class RowViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView title;
        TextView desc;
        CheckBox checkBox;
        TextView price;

        private Context context;

        RowViewHolder(View view) {
            super(view);
            SingletonClass instance = SingletonClass.getInstance();
            imageView = (ImageView) view.findViewById(R.id.icon);
            title = (TextView) view.findViewById((R.id.title));
            desc = (TextView) view.findViewById(R.id.desc);
            if (!(instance.getFavouriteCoinListActivity())) {
                checkBox = (CheckBox) view.findViewById(R.id.checkbox);
            } else {
                price = (TextView) view.findViewById(R.id.price);
            }

        }


    }
// async function to download images, also stores to external storage if permissible

    public class Download extends AsyncTask<String, Context, Bitmap> {
        public Download(RowViewHolder holder) {
            this.holder = holder;
        }

        private RowViewHolder holder = null;
        private String path = Environment.getExternalStorageDirectory().toString();
        private File directory = new File(path + File.separator + "CryptoGraph/Thumbnails/");

        protected Bitmap doInBackground(String... url) {

            Bitmap icon = null;
            if (url.length != 0) {
                Log.d("0", url[0]);
                Log.d("1", url[1]);
                String coinName = url[1];
                try {
                    InputStream in = new java.net.URL(url[0]).openStream();
                    icon = BitmapFactory.decodeStream(in);
                    Log.i("Image Loaded from API", coinName);
                    if (checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        File dir = new File(String.valueOf(directory));
                        if (!dir.exists())
                            dir.mkdirs();
                        File file = new File(dir + "/" + coinName + ".bmp");
                        FileOutputStream fOut = new FileOutputStream(file);
                        Log.i("File Saved", coinName);
                        icon.compress(Bitmap.CompressFormat.PNG, 85, fOut);
                        fOut.flush();
                        fOut.close();
                    }
                } catch (Exception e) {
                    Log.i("BitmapDownloadDoInBackground", e.toString());
                }
            }
            return icon;
        }

        public void onPostExecute(Bitmap icon) {
            if (null != icon) {
                if (holder == null) {
                    Log.d("OnPostExecute", "Holder == null");
                }
                try {
                    holder.imageView.setImageBitmap(icon);
                } catch (Exception e) {
                    Log.d("Holder Set Exception", e.toString());
                }
            } else
                Log.d("Aync", "Downloaded image null");
        }

    }

    //gets up to date api json object

    public class getApi extends AsyncTask<String, Void, Double> {
        public getApi(RowViewHolder holder) {
            this.holder = holder;
        }

        private RowViewHolder holder = null;
        private String path = Environment.getExternalStorageDirectory().toString();

        protected Double doInBackground(String... urls) {
            Double res = null;
            String url = urls[0];
            if (url.length() != 0) {
                try {
                    JsonRetrieve json = new JsonRetrieve();
                    JSONObject response = json.doInBackground(url);
                    try {
                        res = Double.parseDouble(response.getString("USD"));
                        //String.valueOf(response.getJSONObject(currency));

                        Log.d("ZZZZ", String.valueOf(res));
                    } catch (Exception APIFail) {
                        Log.d("API FAIL", APIFail.toString());
                    }

                } catch (Exception e) {
                }
            }
            return res;
        }

        public void onPostExecute(Double price) {
            if (null != price) {
                if (holder == null) {
                    Log.d("OnPostExecute", "Holder == null");
                }
                try {
                    holder.price.setText("$" + price);
                } catch (Exception e) {
                    Log.d("Holder Set Exception", e.toString());
                }
            } else
                holder.price.setText("No Connection");
            Log.d("Aync", "Downloaded image null");
        }

    }
}