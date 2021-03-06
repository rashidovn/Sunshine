package com.example.mark.helloworld.app.test;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.mark.helloworld.app.data.WeatherContract.LocationEntry;
import com.example.mark.helloworld.app.data.WeatherContract.WeatherEntry;
import com.example.mark.helloworld.app.data.WeatherDbHelper;

import java.util.Map;
import java.util.Set;


/**
 * Created by Mark on 10/25/2014.
 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    public void testDeleteDb() throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }

    public void testInsertReadProvider() {

        ContentValues testValues = createNorthPoleLocationValues();

        Uri locationUri = mContext.getContentResolver().
                insert(LocationEntry.CONTENT_URI, testValues);

        long locationRowId = ContentUris.parseId(locationUri);

        //verify we got a row back
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        //Data's inserted. IN THEORY. Now pull some out to stare at it and verify it made
        // the round trip.


        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                locationUri,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null  // sort order
        );

        validateCursor(cursor, testValues);

        ContentValues weatherValues = createWeatherValues(locationRowId);


        Uri WeatherInsertUri = mContext.getContentResolver().
                insert(WeatherEntry.CONTENT_URI, weatherValues);
        long weatherRowId = ContentUris.parseId(WeatherInsertUri);
        assertTrue(WeatherInsertUri != null);

        Cursor weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        validateCursor(weatherCursor, weatherValues);

        cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        validateCursor(cursor, testValues);

        cursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null,
                null,
                null,
                null
        );

        validateCursor(cursor, testValues);

        addAllContentValues(weatherValues,testValues);

        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocation(TestDb.TEST_LOCATION),
                null,
                null,
                null,
                null
        );
        TestDb.validateCursor(weatherCursor, weatherValues);

        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocationWithStartDate(TestDb.TEST_LOCATION,
                        TestDb.TEST_DATE),
                null,
                null,
                null,
                null
        );
        TestDb.validateCursor(weatherCursor, weatherValues);

        weatherCursor.close();
    }

    static ContentValues createWeatherValues(long locationRowId) {
        String testDate = "20141205";
        double testDegrees = 1.1;
        double testHumidity = 1.2;
        double testPressure = 1.3;
        int testMax = 75;
        int testMin = 65;
        String testDesc = "Asteroids";
        double testWind = 5.5;
        int testId = 321;

        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherEntry.COLUMN_DATETEXT, testDate);
        weatherValues.put(WeatherEntry.COLUMN_DEGREES, testDegrees);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, testHumidity);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, testPressure);
        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, testMax);
        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, testMin);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, testDesc);
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, testWind);
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, testId);

        return weatherValues;
    }

    static ContentValues createNorthPoleLocationValues() {

        // Test data we're going to insert into the DB to
        String testName = "North Pole";
        String testLocationSetting = "99705";
        double testLatitude = 64.772;
        double testLongitude = -147.355;

        //Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(LocationEntry.COLUMN_LOCATION_SETTING, testLocationSetting);
        testValues.put(LocationEntry.COLUMN_CITY_NAME, testName);
        testValues.put(LocationEntry.COLUMN_COORD_LAT, testLatitude);
        testValues.put(LocationEntry.COLUMN_COORD_LONG, testLongitude);

        return testValues;
    }

    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {

        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }

        valueCursor.close();
    }

    public void testGetType() {
        // content://com.example.android.sunshine.app/weather/
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testLocation = "94074";
        // content://com.example.android.sunshine.app/weather/94074
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocation(testLocation));
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testDate = "20140612";
        // content://com.example.android.sunshine.app/weather/94074/20140612
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocationWithDate(testLocation, testDate));
        // vnd.android.cursor.item/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);

        // content://com.example.android.sunshine.app/location/
        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/location
        assertEquals(LocationEntry.CONTENT_TYPE, type);

        // content://com.example.android.sunshine.app/location/1
        type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(1L));
        // vnd.android.cursor.item/com.example.android.sunshine.app/location
        assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    void addAllContentValues(ContentValues destination, ContentValues source) {
        for (String key : source.keySet()) {
            destination.put(key, source.getAsString(key));
        }
    }




}
