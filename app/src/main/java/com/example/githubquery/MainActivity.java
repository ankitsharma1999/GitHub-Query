package com.example.githubquery;

import androidx.appcompat.app.AppCompatActivity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    Spinner topRepos;
    ProgressBar progressSpinner;
    ListView reposListView;
    EditText queryInput;

    public class GitHubSearcher  extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressSpinner.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                StringBuilder res= new StringBuilder();
                URL reqUrl = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection)reqUrl.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                int data = inputStreamReader.read();
                while (data != -1){
                    res.append((char) data);
                    data=inputStreamReader.read();
                }
                return res.toString();

            } catch (MalformedURLException e) {
                Log.i("Exception", "MalformedURLException");
                return null;
            } catch (IOException e) {
                Log.i("Exception", "IOException");
                return null;
            }
        }

        @Override
        protected void onPostExecute(String res) {
            super.onPostExecute(res);
            try {
                JSONObject response = new JSONObject(res);
                JSONArray repoArray = new JSONArray(response.getString("items"));
                if(repoArray.length() == 0){
                    ArrayList<String> list = new ArrayList<>();
                    ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, list);
                    reposListView.setAdapter(emptyAdapter);
                    Toast.makeText(getApplicationContext(), "No Repos Found!!!", Toast.LENGTH_SHORT).show();
                }
                else {
                    String num = topRepos.getSelectedItem().toString();
                    int limit = Math.min(repoArray.length(), Integer.parseInt(num));
                    List<Map<String, String>> data = new ArrayList<>();
                    for(int i=0; i<limit; i++){
                        JSONObject repoEach = repoArray.getJSONObject(i);
                        Map<String, String> datum = new HashMap<>(2);
                        datum.put("title", repoEach.getString("name"));
                        datum.put("date", "Forks: "+repoEach.getInt("forks_count"));
                        data.add(datum);
                    }
                    SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(), data,
                            android.R.layout.simple_list_item_2,
                            new String[] {"title", "date"},
                            new int[] {android.R.id.text1,
                                    android.R.id.text2});
                    reposListView = findViewById(R.id.reposListView);
                    reposListView.setAdapter(adapter);
                }
                progressSpinner.setVisibility(View.INVISIBLE);
                reposListView.setVisibility(View.VISIBLE);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void search(View view) {

        String query = queryInput.getText().toString();
        if(query.isEmpty()){
            Toast.makeText(getApplicationContext(), "Please enter something to search!!", Toast.LENGTH_LONG).show();
        }
        else {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .authority("api.github.com")
                    .appendPath("search")
                    .appendPath("repositories")
                    .appendQueryParameter("q",query)
                    .appendQueryParameter("sort", "stars");
            String queryURL = builder.build().toString();
            Log.i("Query URL", queryURL);
            String num = topRepos.getSelectedItem().toString();
            if(num.equals("--Number of Repos--")){
                Toast.makeText(getApplicationContext(), "Please select the number of repos", Toast.LENGTH_LONG).show();
            } else {
                GitHubSearcher searcher = new GitHubSearcher();
                searcher.execute(queryURL);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        topRepos = findViewById(R.id.topRepos);
        progressSpinner = findViewById(R.id.progressSpinner);
        queryInput = findViewById(R.id.queryInput);
        ArrayList<String> numbers = new ArrayList<>();
        numbers.add("--Number of Repos--");
        for(int i=10; i<=30; i+=10){
            numbers.add(Integer.toString(i));
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, numbers);
        topRepos.setAdapter(arrayAdapter);
    }
}