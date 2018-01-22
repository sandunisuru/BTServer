package com.satsml1projects.btserver;

import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    BluetoothSend bs = new BluetoothSend();
    TimerTask timerTask;
    TimerTask timerTask2;
    TextView txtunits;
    TextView txtprice;
    TextView txtph;
    TextView txtcol;

    static int rpsCount = 0 ;
    static int unit = 0;
    static int price = 0;

    public MainActivity(){
        try {
            bs.init();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Timer timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 5000, 30000);

        Timer timer2 = new Timer();
        initializeTimerTask2();
        timer2.schedule(timerTask2, 500, 1000);
    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {
                new REST().execute();
                Log.d("++++++++++++++","+++++++++++++++");
            }
        };
    }

    public synchronized void updateRps(int inc,boolean reset){
        if (!reset)
            rpsCount+=inc;
        else
            rpsCount=0;
    }



    public void initializeTimerTask2() {

        timerTask2 = new TimerTask() {
            public void run() {
                if(rpsCount>=415){
                    updateRps(0,true);
                    unit++;

                }
                price = unit*5;

           //     txtunits.setText("452");
             //   txtprice.setText("852");
                Log.i(String.valueOf(unit), String.valueOf(price));




            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtunits = (TextView) findViewById(R.id.tol_units);
        txtprice = (TextView) findViewById(R.id.tol_price);
        txtph = (TextView) findViewById(R.id.txt_pHstat);
        txtcol = (TextView) findViewById(R.id.txt_color);
    }


    class REST extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection=null;
            String json = null;

            try {
                HttpResponse response;

                ArrayList<Model> models = (ArrayList<Model>) BluetoothSend.list.clone();
                BluetoothSend.list = new ArrayList<>();
                int length = models.size();

                for(int i=0;i<length;i++){
                    Model model = models.get(i);
                    updateRps(model.getRps(),false);
                }

                Model model = models.get(length-1);
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("unit", unit);
                jsonObject.accumulate("price", unit*5);
                jsonObject.accumulate("ph",model.getPh());
                jsonObject.accumulate("color",model.getColor());

                json = jsonObject.toString();
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://192.248.12.7/~154073D/post.php");
                httpPost.setEntity(new StringEntity(json, "UTF-8"));
                httpPost.setHeader("Content-Type", "application/json");
                httpPost.setHeader("Accept-Encoding", "application/json");
                httpPost.setHeader("Accept-Language", "en-US");
                response = httpClient.execute(httpPost);
                String sresponse = response.getEntity().toString();
                Log.w("QueingSystem", sresponse);
                Log.w("QueingSystem", EntityUtils.toString(response.getEntity()));


                ////////////////////////////////////////////////////


                if(model.getPh()==1){
                    Log.v("Send SMS", "");

                    try {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage("94719777909", null, "Dear customer pH value of your water supply is not in suitable condition.", null, null);
                        Toast.makeText(getApplicationContext(), "SMS sent.",
                                Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(),
                                "SMS faild, please try again.",
                                Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }else if(model.getColor()==1){
                    Log.v("Send SMS", "");

                    try {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage("94719777909", null, "Dear customer Colour status of your water supply is not in suitable condition.", null, null);
                        Toast.makeText(getApplicationContext(), "SMS sent.",
                                Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(),
                                "SMS faild, please try again.",
                                Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }

            }
            catch (Exception e) {
                Log.d("InputStream", e.getLocalizedMessage());

            } finally {
        /* nothing to do here */
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

        }
    }

}


