package com.obedchan.practica_5;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private JSONArray jsonResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText idText = findViewById(R.id.editText);
        final EditText valText = findViewById(R.id.editText2);


        Button saveBtn = findViewById(R.id.button);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer id = Integer.parseInt(idText.getText().toString());
                Double val = Double.parseDouble(valText.getText().toString());
                insertVal(id,val);
            }
        });

        final Button getDataBtn = findViewById(R.id.button2);
        getDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer id = Integer.parseInt(idText.getText().toString());
                getData(id);
            }
        });

    }

    public void insertVal(int id,Double val){
        String ws = "http://tdvib.obedchan.com/sensor/insertdata/format/json";
        String params="";
        try {
            JSONObject json = new JSONObject();
            json.put("idSensor",id);
            json.put("valores",val);

            params = json.toString();

        }catch (JSONException jex){
            Toast.makeText(this, jex.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        LoadURL loader = new LoadURL();
        loader.execute(ws,params);
    }

    public void getData(int id){
        String ws = "http://tdvib.obedchan.com/sensor/data/format/json";
        String params = "{\"idSensor\":"+id+"}";

        LoadURL loader = new LoadURL();
        loader.execute(ws,params);

    }

    private class LoadURL extends AsyncTask<String, Void, String> {
        ProgressDialog pd;


        @Override
        protected void onPreExecute() {

            pd = new ProgressDialog(MainActivity.this);
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setTitle("Processing...");
            pd.setMessage("Please wait.");
            pd.show();

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... ss) {
            
            try {

                URL ws = new URL(ss[0]);
                String jsonParams = ss[1];
                // Create connection
                HttpURLConnection myConnection = (HttpURLConnection) ws.openConnection();
                myConnection.setRequestMethod("POST");
                myConnection.setRequestProperty("Content-Type", "application/json; utf-8");
                myConnection.setRequestProperty("Accept", "application/json");

                //Prepara el objeto JSON y lo escribe en el cuerpo del envío
                JSONObject obj = new JSONObject(jsonParams);
                OutputStream os = myConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(obj.toString());
                writer.flush();
                writer.close();
                os.close();

                if (myConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream responseBody = myConnection.getInputStream();
                    InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
                    BufferedReader streamReader = new BufferedReader(responseBodyReader);
                    StringBuilder responseStrBuilder = new StringBuilder();

                    //get JSON String
                    String inputStr;
                    while ((inputStr = streamReader.readLine()) != null)
                        responseStrBuilder.append(inputStr);

                    myConnection.disconnect();
                    return responseStrBuilder.toString();
                } else {
                    Log.d("Main", "error in connection");
                    return "";
                }
            }catch (Exception e){
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            final TextView textResult = findViewById(R.id.textView2);
            // JSON Object
            try {
                jsonResult = new JSONArray(s);
                String msg="";
                for(int i =0; i < jsonResult.length(); i++){
                    JSONObject obj = jsonResult.getJSONObject(i);
                    msg += String.format("Valor: %f, Fecha: %s \n",obj.getDouble("valores"),obj.getString("Fecha"));
                }

                textResult.setText(msg);

                pd.dismiss();
            }catch (Exception e) {
                pd.dismiss();
                return;
            }
        }
    }
}
