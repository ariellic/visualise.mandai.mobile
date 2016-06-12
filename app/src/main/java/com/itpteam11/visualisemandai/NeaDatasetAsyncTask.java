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
        // used to setup the task
    }

    protected void onProgressUpdate(Integer... progress) {
    }

    @Override
    protected String doInBackground(String... urls) {
        try {
            // Download XML string fro NEA API
            data = downloadUrlHTTP(urls[0]);
            Log.d("Data", "Data downloaded from NEA");
        } catch (IOException e) {
            //return "Unable to retrieve web page. URL may be invalid.";
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
        InputStream is = null;
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

        // Create the Pull Parser
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();

        // Set the Parser's input to be the XML document in the HTTP Response
        parser.setInput(new StringReader(xml));

        // Get the first Parser event and start iterating over the XML document
        int eventType = parser.getEventType();
        //final Firebase neaRef = new Firebase("https://visualise-mandai.firebaseio.com/service/");
        DatabaseReference neaRef = FirebaseDatabase.getInstance().getReference().child("service");

        //String weather;
        //String psi;
        loop: while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    String tagName = parser.getName();
                    if (tagName.equals("area")) {
                        if (parser.getAttributeValue(null, "name").equals("Mandai")) {
                            //weather = parser.getAttributeValue(null, "forecast").toString();
                            //wf.psi = 1.1;
                            //forecastList.add(wf);
                            String weatherLong = neaRef.child("weather").child("abbreviations").child(parser.getAttributeValue(null, "forecast")).toString();
                            Log.d("weatherLong", weatherLong);
                            neaRef.child("weather").child("value").setValue(parser.getAttributeValue(null, "forecast"));
                            break loop;
                        }
                    }
                    else if (tagName.equals("id")) {
                        if (parser.next() == XmlPullParser.TEXT) {
                            if (parser.getText().equals("rNO")) {
                                boolean reach = false;
                                while (true){
                                    if (parser.next() == XmlPullParser.START_TAG) {
                                        if (parser.getName().equals("reading")) {
                                            if (parser.getAttributeValue(null, "type").equals("NPSI_PM25_3HR")) {
                                                //data.psi = parser.getAttributeValue(null, "value").toString();
                                                neaRef.child("psi").child("value").setValue(parser.getAttributeValue(null, "value"));
                                                break loop;
                                            }
                                        }
                                    }
                                }
                            }
                        }
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





