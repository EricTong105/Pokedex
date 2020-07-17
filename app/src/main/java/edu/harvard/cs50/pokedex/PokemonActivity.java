package edu.harvard.cs50.pokedex;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.AsyncTaskLoader;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PokemonActivity extends AppCompatActivity {
    public class DownloadSpriteTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                return BitmapFactory.decodeStream(url.openStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
        }
    }
    private TextView nameTextView;
    private TextView numberTextView;
    private TextView type1TextView;
    private TextView type2TextView;
    private TextView descriptionTextView;
    private String url;
    private RequestQueue requestQueue;
    private String name;
    private boolean caught = false;
    SharedPreferences saveCatchState;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        url = getIntent().getStringExtra("url");
        nameTextView = findViewById(R.id.pokemon_name);
        numberTextView = findViewById(R.id.pokemon_number);
        type1TextView = findViewById(R.id.pokemon_type1);
        type2TextView = findViewById(R.id.pokemon_type2);
        //Get name of pokemon
        name = "Catch Pokemon" + url.substring(34, url.length() - 1);
        saveCatchState = getSharedPreferences("CatchState", MODE_PRIVATE);
//        Log.d("AAAAA", String.valueOf(saveCatchState.getBoolean("Catch", false)));
        //Get boolean saved from previous if DNE then assign false
        caught = saveCatchState.getBoolean(name, false);
//        Log.d("AAAAA", String.valueOf(saveCatchState.getBoolean("Catch", false)));
        Button button = (Button) findViewById(R.id.catch_button);
        if (caught/*button.getText().toString().equals(getString(R.string.Catch))*/) {
            button.setText(getString(R.string.Release));
        } else {
            button.setText(getString(R.string.Catch));
        }
        imageView = findViewById(R.id.pokemon_image);
        descriptionTextView = findViewById(R.id.pokemon_description);
        load();
    }

//    @Override
//    public void onRestoreInstanceState(Bundle savedInstanceState) {
//        caught = savedInstanceState.getBoolean("CatchState");
//        super.onRestoreInstanceState(savedInstanceState);
//    }

    public void load() {
        type1TextView.setText("");
        type2TextView.setText("");

        JsonObjectRequest request1 = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    nameTextView.setText(response.getString("name"));
                    numberTextView.setText(String.format("#%03d", response.getInt("id")));

                    JSONArray typeEntries = response.getJSONArray("types");
                    for (int i = 0; i < typeEntries.length(); i++) {
                        JSONObject typeEntry = typeEntries.getJSONObject(i);
                        int slot = typeEntry.getInt("slot");
                        String type = typeEntry.getJSONObject("type").getString("name");

                        if (slot == 1) {
                            type1TextView.setText(type);
                        }
                        else if (slot == 2) {
                            type2TextView.setText(type);
                        }
                    }
                    JSONObject spriteUrls = response.getJSONObject("sprites");
                    String spriteUrl_FD = spriteUrls.getString("front_default");
                    Log.d("spriteUrl_FD", spriteUrl_FD);
                    new DownloadSpriteTask().execute(spriteUrl_FD);

//                    JSONObjectRequest a = new JsonObjectRequest(Request.Method.GET, descriptionUrl, null, Response.Listener<JSONObject>);
//                    JSONArray descriptions = response.getJSONArray("flavor_text_entries");
//                    for (int i = 0; i < descriptions.length(); i++) {
//                        String language = descriptions.getJSONObject(i).getJSONObject("language").getString("name");
//                        if (language.equals("en")) {
//                            descriptionTextView.setText(descriptions.getJSONObject(i).getString("flavor_text"));
//                            break;
//                        }
//                    }
                } catch (JSONException e) {
                    Log.e("cs50", "Pokemon json error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("cs50", "Pokemon details error", error);
            }
        });

        String descriptionUrl = "https://pokeapi.co/api/v2/pokemon-species/" + url.substring(34, url.length() - 1);
        JsonObjectRequest request2 = new JsonObjectRequest(Request.Method.GET, descriptionUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray descriptions = response.getJSONArray("flavor_text_entries");
                    for (int i = 0; i < descriptions.length(); i++) {
                        String language = descriptions.getJSONObject(i).getJSONObject("language").getString("name");
                        if (language.equals("en")) {
                            String description = descriptions.getJSONObject(i).getString("flavor_text");
                            description = description.replaceAll("\n", " ");
                            descriptionTextView.setText(description);
//                            Log.d("AAAAA", descriptions.getJSONObject(i).getString("flavor_text"));
                            break;
                        }
                    }
                } catch (JSONException e) {
                    Log.e("cs50", "Pokemon json error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("cs50", "Pokemon details error", error);
            }
        });
        requestQueue.add(request1);
        requestQueue.add(request2);
    }

    public void toggleCatch(View view) {

        Button button = (Button) findViewById(R.id.catch_button);
        caught = !caught;
        if (caught/*button.getText().toString().equals(getString(R.string.Catch))*/) {
            button.setText(getString(R.string.Release));
        } else {
            button.setText(getString(R.string.Catch));
        }
        saveCatchState = getSharedPreferences("CatchState", MODE_PRIVATE);
        SharedPreferences.Editor editor = saveCatchState.edit();
        editor.putBoolean(name, caught);
        editor.apply();
//        Log.d("AAAAA", String.valueOf(saveCatchState.getBoolean("Catch ", false)));
    }

//    @Override
//    public void onSaveInstanceState(@NonNull Bundle outState) {
//        Log.d("aaaaa", String.valueOf(caught));
//        outState.putBoolean("CatchState", caught);
//        super.onSaveInstanceState(outState);
//    }
}
