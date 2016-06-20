package com.itpteam11.visualisemandai;

import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NeaDatasetAsyncTask extends AsyncTask<String, Void, String> {

    private String data;

    @Override
    protected void onPreExecute() {
    }

    protected void onProgressUpdate(Integer... progress) {
    }

    @Override
    protected String doInBackground(String... urls) {
        try {
            // Download XML string fro NEA API
            data = downloadUrlHTTP(urls[0]);
            Log.d("NEADATA", "Data downloaded from NEA");
        } catch (IOException e) {
            Log.d("ERROR", "Unable to retrieve web page. URL may be invalid.");
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {

        try {
            parseXML(data);
            Log.d("ParseXML", "Done parsing XML and inserting to database");
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Download of XML from NEA API
    private String downloadUrlHTTP(String myurl) throws IOException {
        BufferedReader reader = null;

        // do the download here
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

            myurl = buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
        return myurl;
    }

    private void parseXML(String xml) throws XmlPullParserException, IOException {

        // Create the pull parser
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();

        // Set the parser's input to be the XML document in the HTTP Response
        parser.setInput(new StringReader(xml));

        // Get the first parser event and start iterating over the XML document
        int eventType = parser.getEventType();
        //final Firebase neaRef = new Firebase("https://visualise-mandai.firebaseio.com/service/");
        DatabaseReference envRef = FirebaseDatabase.getInstance().getReference().child("service");

        loop: while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    String tagName = parser.getName();
                    // If weather API
                    if (tagName.equals("area")) {
                        if (parser.getAttributeValue(null, "name").equals("Mandai")) {
                            String weatherLong = envRef.child("weather").child("abbreviations").child(parser.getAttributeValue(null, "forecast")).toString();
                            Log.d("weatherLong", weatherLong);
                            envRef.child("weather").child("value").setValue(parser.getAttributeValue(null, "forecast"));
                            break loop;
                        }
                    }
                    // If psi API
                    else if (tagName.equals("id")) {
                        if (parser.next() == XmlPullParser.TEXT) {
                            if (parser.getText().equals("rNO")) {
                                while (true){
                                    if (parser.next() == XmlPullParser.START_TAG) {
                                        if (parser.getName().equals("reading")) {
                                            if (parser.getAttributeValue(null, "type").equals("NPSI_PM25_3HR")) {
                                                envRef.child("psi").child("value").setValue(parser.getAttributeValue(null, "value"));
                                                break loop;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // If temperature API
                    else if (tagName.equals("temperature")) {
                        double tempKelvin = Double.parseDouble(parser.getAttributeValue(null, "value"));
                        double tempCelsius = Math.round((tempKelvin/10.554)*100.0)/100.0;
                        envRef.child("temperature").child("value").setValue(tempCelsius);
                        break loop;
                    }
                    break;
                case XmlPullParser.END_TAG:
                    break;
            }
            eventType = parser.next();
        }
        //return forecastList;
        //neaRef.updateChildren(data);j
    }

}





