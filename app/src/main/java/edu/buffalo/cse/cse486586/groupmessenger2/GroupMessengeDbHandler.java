package edu.buffalo.cse.cse486586.groupmessenger2;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import edu.buffalo.cse.cse486586.groupmessenger2.GroupMessengerSchema.GroupMessageEntry;

/**
 * Created by veera on 2/18/18.
 */
/*
 * GroupMessengeDbHandler manages calls to ContentProvider and also prints out text to the UI
 */

public class GroupMessengeDbHandler {
    private static final String TAG = GroupMessengeDbHandler.class.getName();
    private final ContentResolver mContentResolver;
    private final Uri mUri;

    public GroupMessengeDbHandler(ContentResolver _cr) {
        mContentResolver = _cr;
        mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
    }

    /**
     * buildUri() demonstrates how to build a URI for a ContentProvider.
     *
     * @param scheme
     * @param authority
     * @return the URI
     */
    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }

    /*
     * Inserts the given key and calue in the database and also prints the values to the screen
     */
    public boolean insertMessage(String key, String value) {
        ContentValues values = new ContentValues();
        values.put(GroupMessageEntry.COLUMN_NAME_KEY, key);
        values.put(GroupMessageEntry.COLUMN_NAME_VALUE, value);
        try {
            mContentResolver.insert(mUri, values);
        } catch (Exception e) {
            Log.e(TAG,e.getMessage());
            return false;
        }

        return true;
    }

    /*
     * Checks if the key is already present in the table
     */

    public boolean isKeyPresent(String key) {
        ContentValues values = new ContentValues();
        values.put(GroupMessageEntry.COLUMN_NAME_KEY, key);
        Cursor resultCursor=null;
        try {
            resultCursor = mContentResolver.query(mUri, null, key, null, null);
            if (resultCursor == null) {
                Log.e(TAG, "Result null");
                throw new Exception();
            }
            int keyIndex = resultCursor.getCount();
            if(keyIndex > 0) {
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            resultCursor.close();
        }
        return false;
    }

    /*
     * Queries all the messages from the table
     */

    public String[] queryAllMessages() {
        String[] messages= null;
        Cursor resultCursor = null;
        try {
            resultCursor = mContentResolver.query(mUri, null, null, null, null);
            messages = new String[resultCursor.getCount()];
            int counter = 0;
            while (resultCursor.moveToNext()) {
                int valueIndex = resultCursor.getColumnIndex(GroupMessageEntry.COLUMN_NAME_VALUE);
                messages[counter++] = resultCursor.getString(valueIndex);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            resultCursor.close();
        }

        return messages;
    }

    /*
     * Queries the message for the given key from the table
     */

    public String queryMessage(String key) {
        Cursor resultCursor=null;
        String value = null;
        try {
            resultCursor = mContentResolver.query(mUri, null, key, null, null);
            if (resultCursor == null) {
                Log.e(TAG, "Result null");
                throw new Exception();
            }
            if(resultCursor.getColumnCount() == 1) {
                int valueIndex = resultCursor.getColumnIndex(GroupMessageEntry.COLUMN_NAME_VALUE);
                value = resultCursor.getString(valueIndex);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            resultCursor.close();
        }
        return value;
    }
}
