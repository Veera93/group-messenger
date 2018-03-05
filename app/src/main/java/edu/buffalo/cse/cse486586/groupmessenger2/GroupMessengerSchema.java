package edu.buffalo.cse.cse486586.groupmessenger2;

import android.provider.BaseColumns;

/**
 * Created by veera on 2/16/18.
 */

/*
 * Defines the schema of the Table
 */

public final class GroupMessengerSchema {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private GroupMessengerSchema() {}

    /* Inner class that defines the table contents */
    public static class GroupMessageEntry implements BaseColumns {
        public static final String TABLE_NAME = "message";
        public static final String COLUMN_NAME_KEY = "key";
        public static final String COLUMN_NAME_VALUE = "value";
    }
}
