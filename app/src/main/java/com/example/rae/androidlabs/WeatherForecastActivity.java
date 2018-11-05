package com.example.rae.androidlabs;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class WeatherForecastActivity extends Activity {
    final String ACTIVITY_NAME = "WeatherForecastActivity";
    ProgressBar progressBar;
    ImageView image;
    TextView currentTemp;
    TextView minTemp;
    TextView maxTemp;
    TextView wind;
    String status = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_forecast);
        Log.i(ACTIVITY_NAME, "in onCreate()");

        image = findViewById(R.id.weatherimg);
        currentTemp = findViewById(R.id.currentTemp);
        minTemp = findViewById(R.id.minTemp);
        maxTemp = findViewById(R.id.maxTemp);
        wind = findViewById(R.id.windSpeed);

        progressBar = findViewById(R.id.weatherProg);
        progressBar.setMax(100);
        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);

        final ForecastQuery attempt = new ForecastQuery();
        attempt.execute("http://api.openweathermap.org/data/2.5/weather?q=ottawa,ca&APPID=d99666875e0e51521f0040a3d97d0f6a&mode=xml&units=metric");
    }

    private class ForecastQuery extends AsyncTask<String, Integer, String> {
        public String currentT, minT, maxT, windSpeed, icon;
        public Bitmap weatherImg;
        XmlPullParser parser;
        private final String ns = null;

        @Override
        protected void onProgressUpdate(Integer...values) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            String filename = icon + ".png";
            if (fileExists(filename)) {
                FileInputStream fis = null;
                try {
                    fis = openFileInput(filename);
                }
                catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Bitmap bm = BitmapFactory.decodeStream(fis);
                image.setImageBitmap(bm);
            } else
                Log.w(ACTIVITY_NAME, "File not found");
            currentTemp.setText("Current Temperature: " + currentT + "°C");
            minTemp.setText("Min Temperature: "+minT+ "°C");
            maxTemp.setText("Max Temperature: "+maxT+ "°C");
            wind.setText("Wind Speed: "+windSpeed);
        }

        @Override
        protected String doInBackground(String...urls) {
            URL url = null;
            HttpURLConnection in = null;

            // load URL
            try {
                url = new URL(urls[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            // HttpURLConnection object
            try {
                in = (HttpURLConnection) url.openConnection();
                in.setReadTimeout(10000 /* milliseconds */);
                in.setConnectTimeout(15000 /* milliseconds */);
                in.setRequestMethod("GET");
                in.setDoInput(true);
                in.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // getting input stream and putting it in parser
            parser = Xml.newPullParser();
            try {
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(in.getInputStream(), null);
                parser.nextTag();
                parser.require(XmlPullParser.START_TAG, ns, "current");
                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }
                    String name = parser.getName();
                    // Starts by looking for the entry tag
                    if (name.equals("temperature")) {
                        readTemperature(parser);
                    } else if (name.equals("wind")) {
                        readWind(parser);
                    } else if (name.equals("weather")) {
                        readWeatherIcon(parser);
                    } else {
                        skip(parser);
                    }
                }
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            status = "Done";

            return "Done";
        }

        private void readTemperature(XmlPullParser parser) throws IOException, XmlPullParserException {
            parser.require(XmlPullParser.START_TAG, ns, "temperature");
            currentT = parser.getAttributeValue(ns, "value") + "";
            publishProgress(25);
            minT = parser.getAttributeValue(ns, "min") + "";
            publishProgress(50);
            maxT = parser.getAttributeValue(ns, "max") + "";
            publishProgress(75);
            parser.nextTag();
            parser.require(XmlPullParser.END_TAG, ns, "temperature");
        }

        private void readWind(XmlPullParser parser) throws IOException, XmlPullParserException {
            parser.require(XmlPullParser.START_TAG, ns, "wind");

            // get attribute of subtag "speed"
            if (parser.next() == XmlPullParser.START_TAG) {
                String name = parser.getName();
                if (name.equals("speed")) {
                    windSpeed = parser.getAttributeValue(ns, "value") + "";
                }

                // skip through rest of tags until </wind>
                while (true) {
                    if (parser.next() == XmlPullParser.END_TAG && parser.getName().equals("wind"))
                        break;
                }
                parser.require(XmlPullParser.END_TAG, ns, "wind");
            }
        }

        private void readWeatherIcon(XmlPullParser parser) throws IOException, XmlPullParserException {
            // get icon string name
            parser.require(XmlPullParser.START_TAG, ns, "weather");
            icon = parser.getAttributeValue(ns, "icon");

            // save image
            HTTPUtils httpUtils = new HTTPUtils();
            weatherImg  = httpUtils.getImage("http://openweathermap.org/img/w/"+ icon + ".png");
            FileOutputStream outputStream = openFileOutput( icon + ".png", Context.MODE_PRIVATE);
            weatherImg.compress(Bitmap.CompressFormat.PNG, 80, outputStream);
            outputStream.flush();
            outputStream.close();
            Log.i(ACTIVITY_NAME, "Saved image as " + icon + ".png");

            // update progress bar
            publishProgress(100);
        }

        private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                throw new IllegalStateException();
            }
            int depth = 1;
            while (depth != 0) {
                switch (parser.next()) {
                    case XmlPullParser.END_TAG:
                        depth--;
                        break;
                    case XmlPullParser.START_TAG:
                        depth++;
                        break;
                }
            }
        }
    }

    class HTTPUtils {
        public Bitmap getImage(URL url) {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    return BitmapFactory.decodeStream(connection.getInputStream());
                } else
                    return null;
            } catch (Exception e) {
                return null;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        public Bitmap getImage(String urlString) {
            try {
                URL url = new URL(urlString);
                return getImage(url);
            } catch (MalformedURLException e) {
                return null;
            }
        }

    }

    public boolean fileExists(String fname){
        File file = getBaseContext().getFileStreamPath(fname);
        return file.exists();
    }
}
