package edu.buffalo.cse.cse486586.groupmessenger2;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import edu.buffalo.cse.cse486586.groupmessenger2.GroupMessengerSchema.GroupMessageEntry;

/**
 * GroupMessengerProvider is a key-value table. Once again, please note that we do not implement
 * full support for SQL as a usual ContentProvider does. We re-purpose ContentProvider's interface
 * to use it as a key-value table.
 * 
 * Please read:
 * 
 * http://developer.android.com/guide/topics/providers/content-providers.html
 * http://developer.android.com/reference/android/content/ContentProvider.html
 * 
 * before you start to get yourself familiarized with ContentProvider.
 * 
 * There are two methods you need to implement---insert() and query(). Others are optional and
 * will not be tested.
 * 
 * @author veera
 *
 */
public class GroupMessengerProvider extends ContentProvider {

    static final String TAG = GroupMessengerProvider.class.getSimpleName();
    GroupMessengerDbHelper dbHelper;
    SQLiteDatabase db;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // You do not need to implement this.
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /*
         * values will have two columns (a key
         * column and a value column) and one row that contains the actual (key, value) pair to be
         * inserted.
         *
         * Used SQLite for database
         */
        Log.v(TAG,"insert "+values.toString());
        dbHelper = new GroupMessengerDbHelper(this.getContext());
        String key = (String) values.get(GroupMessengerSchema.GroupMessageEntry.COLUMN_NAME_KEY);
        Cursor cursor = query(uri,null, key, null, null);
        db = dbHelper.getWritableDatabase();

        if(cursor.getCount() == 0 ) {
            db.insert(GroupMessageEntry.TABLE_NAME, null, values);
        } else {
            update(uri, values, key, null);
        }
        return uri;
    }

    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it here.
        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Which row to update, based on the title
        String mSelection = GroupMessageEntry.COLUMN_NAME_KEY + " LIKE ?";
        String key = (String) values.get(GroupMessageEntry.COLUMN_NAME_KEY);
        String[] mSelectionArgs = { key };

        int count = db.update(
                GroupMessageEntry.TABLE_NAME,
                values,
                mSelection,
                mSelectionArgs);

        return count;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        dbHelper = new GroupMessengerDbHelper(this.getContext());
        db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try{
            //Querying for all the messages
            if(selection == null) {
                cursor = db.query(
                        GroupMessageEntry.TABLE_NAME,   // The table to query
                        projection,                     // The columns to return
                        null,                     // The columns for the WHERE clause
                        null,                     // The values for the WHERE clause
                        null,                  // don't group the rows
                        null,                   // don't filter by row groups
                        sortOrder                      // The sort order
                );
            } else {
                Log.v("query", selection);
                String mSelection = GroupMessageEntry.COLUMN_NAME_KEY + " = ?";
                String[] mSelectArg = { selection };

                cursor = db.query(
                        GroupMessageEntry.TABLE_NAME,   // The table to query
                        projection,                     // The columns to return
                        mSelection,                     // The columns for the WHERE clause
                        mSelectArg,                     // The values for the WHERE clause
                        null,                  // don't group the rows
                        null,                   // don't filter by row groups
                        sortOrder                      // The sort order
                );
            }
        } catch (Exception e) {
            Log.e(TAG,e.getMessage());
        }
        finally {
            return cursor;
        }
    }
}
