package alexandr_flyagin.gpslock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

public class LocalDB_Helper extends SQLiteOpenHelper implements BaseColumns {
    // Database Version
    public static final int DATABASE_VERSION = 1;
    // Database Name
    public static final String DATABASE_NAME = "LocalDB.db";
    // Location table name
    public static final String TABLE_NAME = "Location";
    // Location Table Columns names
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_ALTITUDE = "altitude";
    public static final String COLUMN_ACCURACY = "accuracy";
    public static final String COLUMN_BEARING = "bearing";
    public static final String COLUMN_SPEED = "speed";
    public static final String COLUMN_TIME = "time";

    public LocalDB_Helper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String DATABASE_CREATE_SCRIPT = "CREATE TABLE " + TABLE_NAME + "("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_LATITUDE + " DOUBLE, "
                + COLUMN_LONGITUDE + " DOUBLE, "
                + COLUMN_ALTITUDE + " DOUBLE, "
                + COLUMN_ACCURACY + " FLOAT, "
                + COLUMN_BEARING + " FLOAT, "
                + COLUMN_SPEED + " FLOAT, "
                + COLUMN_TIME + " BIGINT"
                + ");";
        db.execSQL(DATABASE_CREATE_SCRIPT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addLocation(LocationStore locationStore) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LATITUDE, locationStore.getLatitude());
        values.put(COLUMN_LONGITUDE, locationStore.getLongitude());
        values.put(COLUMN_ALTITUDE, locationStore.getAltitude());
        values.put(COLUMN_ACCURACY, locationStore.getAccuracy());
        values.put(COLUMN_BEARING, locationStore.getBearing());
        values.put(COLUMN_SPEED, locationStore.getSpeed());
        values.put(COLUMN_TIME, locationStore.getTime());
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    // Getting one shop
    public LocationStore getLocationStore(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{
                        BaseColumns._ID,
                        COLUMN_LATITUDE,
                        COLUMN_LONGITUDE,
                        COLUMN_ALTITUDE,
                        COLUMN_ACCURACY,
                        COLUMN_BEARING,
                        COLUMN_SPEED,
                        COLUMN_TIME},
                BaseColumns._ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        LocationStore ls = new LocationStore(
                Integer.parseInt(cursor.getString(0)),
                Double.parseDouble(cursor.getString(1)),
                Double.parseDouble(cursor.getString(2)),
                Double.parseDouble(cursor.getString(3)),
                Float.parseFloat(cursor.getString(4)),
                Float.parseFloat(cursor.getString(5)),
                Float.parseFloat(cursor.getString(6)),
                Long.parseLong(cursor.getString(7))
        );
        cursor.close();
        db.close();
        return ls;
    }

    public List<LocationStore> getAllLocationStores() {
        List<LocationStore> lsList = new ArrayList<LocationStore>();
        String selectQuery = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                LocationStore ls = new LocationStore();
                ls.setId(Integer.parseInt(cursor.getString(0)));
                ls.setLatitude(Double.parseDouble(cursor.getString(1)));
                ls.setLongitude(Double.parseDouble(cursor.getString(2)));
                ls.setAltitude(Double.parseDouble(cursor.getString(3)));

                ls.setAccuracy(Float.parseFloat(cursor.getString(4)));
                ls.setBearing(Float.parseFloat(cursor.getString(5)));
                ls.setSpeed(Float.parseFloat(cursor.getString(6)));

                ls.setTime(Long.parseLong(cursor.getString(7)));
                lsList.add(ls);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return lsList;
    }

    public void updateLocationStore(LocationStore ls) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_LATITUDE, ls.getLatitude());
        values.put(COLUMN_LONGITUDE, ls.getLongitude());
        values.put(COLUMN_ALTITUDE, ls.getTime());
        values.put(COLUMN_ACCURACY, ls.getAccuracy());
        values.put(COLUMN_BEARING, ls.getBearing());
        values.put(COLUMN_SPEED, ls.getSpeed());
        values.put(COLUMN_TIME, ls.getTime());

        db.update(TABLE_NAME, values, BaseColumns._ID + " = ?", new String[]{String.valueOf(ls.getId())});
        db.close();
    }

    public void deleteLocationStore(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, BaseColumns._ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }
}
