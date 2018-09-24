package by.kirilldrob.bynexchangerates.network;

import android.content.Context;
import android.os.Bundle;

import org.xmlpull.v1.XmlPullParser;

import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;


import androidx.loader.content.AsyncTaskLoader;
import by.kirilldrob.bynexchangerates.data.Currency;


/**
 * Created by Kirill Drob on 23.09.2018.
 */

public class XMLLoader extends AsyncTaskLoader<ArrayList<Currency>> {

    private String urlXML;
    public ArrayList<Currency> mCurrencyList;
    static public HashMap<String, Integer> mConfigPosition = new HashMap<>();
    public String mLastErrorCode = "";

    public XMLLoader(Context context, Bundle args) {
        super(context);
        if (args != null) urlXML = args.getString("fileName");
    }
//Для сетевого кэширования лучше использовать библиотеку Retrofit, однако при
// частой смене курса валют кэширование (etag) будет давать мало пользы.

    // Кэширование локальное. Переворот экрана, либо отсутвие интернета в метро. Для обновления - pull request
    @Override
    protected void onStartLoading() {
        if (mCurrencyList != null) {
            super.deliverResult(mCurrencyList);
        } else {
            forceLoad();
        }
    }


    @Override
    public ArrayList<Currency> loadInBackground() {

        return getAndParseXML();
    }

    @Override //работает в основном потоке! Кэширование для переворота экрана
    public void deliverResult(ArrayList<Currency> data) {
        mCurrencyList = data;
        if (isStarted()) {
            super.deliverResult(data);
        }

    }


    private InputStream downloadUrl(String myurl) throws IOException {
        InputStream is = null;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(2000 /* milliseconds */);
            conn.setConnectTimeout(2000 /* milliseconds */); // Для тестов
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            // int response = conn.getResponseCode();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                is = conn.getInputStream();
            } else {
                mLastErrorCode = "Ошибка ответа от сервера" + conn.getResponseCode();
                return null;
            }

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } catch (IOException e) {
            e.printStackTrace();
            mLastErrorCode = "Интернет-соединение не установлено. \n Проверьте соединение и потяните вниз для перезагрузки.";
            if (is != null) {
                is.close();

            }
            return null;

        }


        return is;
    }


    private ArrayList<Currency> getAndParseXML() {
        //LinkedList<Currency> rssCurrencyList = new LinkedList<>();
        int max = 0;
        if (mConfigPosition.size() > 0) max = Collections.max(mConfigPosition.values())+1;
        ArrayList<Currency> rssCurrencyList = new ArrayList<>(Arrays.asList(new Currency[max]));
        InputStream urlStream = null;
        boolean done = false;
        boolean validity = false;
        try {
            String charCode = null, scale = null, name = null, rate = null;

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            urlStream = downloadUrl(urlXML);
            if (urlStream == null) return null;
            parser.setInput(urlStream, null);
            int eventType = parser.getEventType();


            while (eventType != XmlPullParser.END_DOCUMENT && !done) {
                String tagName = parser.getName();

                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:

                        if (tagName.equals("CharCode")) {
                            charCode = parser.nextText();
                        }
                        if (tagName.equals("Scale")) {
                            scale = parser.nextText();
                        }
                        if (tagName.equals("Name")) {
                            name = parser.nextText();
                        }
                        if (tagName.equals("Rate")) {
                            rate = parser.nextText();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (tagName.equals("DailyExRates")) {
                            done = true;
                        } else if (tagName.equals("Currency")) {

                            if (charCode.length() > 0 && name.length() > 0 && scale.length() > 0 && rate.length() > 0)
                                validity = true;

                            // Задаем нужный порядок
                            if (mConfigPosition.containsKey(charCode)) {
                                rssCurrencyList.set(mConfigPosition.get(charCode), new Currency(charCode, rate, name, scale));
                            } else rssCurrencyList.add(new Currency(charCode, rate, name, scale));

                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            mLastErrorCode = "ОШИБКА: Невалидный ответ  от сервера...";
            rssCurrencyList = null;
        }
        if (urlStream != null) {
            try {
                urlStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!done || !validity) {
            mLastErrorCode = "ОШИБКА: Невалидный ответ  от сервера...";
            return null;
        }

        return rssCurrencyList;

    }


//
//    private void showToast(String message) {
//        Toast.makeText(MainActivity.this, "Unable to download data with following reason: " + message, Toast.LENGTH_LONG).show();
//    }

}
