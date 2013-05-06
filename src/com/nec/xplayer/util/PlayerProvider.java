/*--------------------------------------------------------------------
 *
 *  Copyright (C) NEC Corporation 2012
 *
 *    NEC CONFIDENTIAL AND PROPRIETARY
 *    All rights reserved by NEC Corporation.
 *    This program must be used solely for the purpose for which
 *    it was furnished by NEC Corporation.   No part of this
 *    program may be reproduced or disclosed to others, in any
 *    form, without prior written permission of NEC
 *    Corporation.   Use of copyright notice dose not evidence
 *    publication of the program.
 *
 *    NEC Corporation accepts no responsibility for any damages
 *    resulting from the use of this software.
 *    This software is provided "AS IS", and its user assume all risks
 *    when using it.
 *
 *--------------------------------------------------------------------*/

package com.nec.xplayer.util;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

public class PlayerProvider extends ContentProvider {
//    /** LogÂá∫ÂäõÁî®TAG */
//    private static final String TAG = SampleProvider.class.getSimpleName();;
//    private static final boolean DBG = false;

    public static final String AUTHORITY = "com.nec.xplayer.util.PlayerProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/info");
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/com.nec.xplayer";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/com.nec.xplayer";
    public static final String DEFAULT_SORT_ORDER = "score DESC";
    
    public static final String ID = BaseColumns._ID;
    public static final String NAME = "name";
    public static final String URL = "url";

    private static final String DATABASE_NAME = "xplayer.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "info";
    
    private static final int PLAYER = 1;
    private static final int PLAYER_ID = 2;

    private static final UriMatcher sUriMatcher;
        
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        /* (Èù?Javadoc)
         * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                    + ID + " INTEGER PRIMARY KEY,"
                    + NAME + " TEXT NOT NULL,"
                    + URL + " TEXT NOT NULL"
                    +");");
        }

        /* (Èù?Javadoc)
         * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

    private DatabaseHelper mDatabaseHelper;

    /* (Èù?Javadoc)
     * @see android.content.ContentProvider#onCreate()
     */
    @Override
    public boolean onCreate() {
        mDatabaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    /* (Èù?Javadoc)
     * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    	SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
    	qb.setTables(TABLE_NAME);
    	
    	SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
    	Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
    	
    	c.setNotificationUri(getContext().getContentResolver(), uri);
    	return c;
    }

    /* (Èù?Javadoc)
     * @see android.content.ContentProvider#getType(android.net.Uri)
     */
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case PLAYER:
            return CONTENT_TYPE;

        case PLAYER_ID:
            return CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    /* (Èù?Javadoc)
     * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
     */
    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        if (sUriMatcher.match(uri) != PLAYER) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

/*        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        final long id = db.insert(TABLE_NAME, null, values);
        final Uri newUri = ContentUris.withAppendedId(CONTENT_URI, id);
        getContext().getContentResolver().notifyChange(newUri, null);

        return newUri;
*/
        ContentValues values;

        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

        long rowId = db.insert(TABLE_NAME, null, values);

        // If the insert succeeded, the row ID exists.
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    
    }

    /* (Èù?Javadoc)
     * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        final int updateCount;
        switch (sUriMatcher.match(uri)) {
        case PLAYER:
            updateCount = db.update(TABLE_NAME, values, selection, selectionArgs);
            break;

        case PLAYER_ID:
            final long id = Long.parseLong(uri.getPathSegments().get(1));
            final String idPlusSelection = android.provider.BaseColumns._ID + "=" + Long.toString(id) + (selection == null ? "" : "AND (" + selection + ")");
            updateCount = db.update(TABLE_NAME, values, idPlusSelection, selectionArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Â§âÊõ¥„ÇíÈ?Áü•„Åô„Ç?
        getContext().getContentResolver().notifyChange(uri, null);

        return updateCount;
    }

    /* (Èù?Javadoc)
     * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        final int updateCount;
        switch (sUriMatcher.match(uri)) {
        case PLAYER:
            updateCount = db.delete(TABLE_NAME, selection, selectionArgs);
            break;

        case PLAYER_ID:
            final long id = Long.parseLong(uri.getPathSegments().get(1));
            final String idPlusSelection = android.provider.BaseColumns._ID + "=" + Long.toString(id) + (selection == null ? "" : "AND (" + selection + ")");
            updateCount = db.delete(TABLE_NAME, idPlusSelection, selectionArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Â§âÊõ¥„ÇíÈ?Áü•„Åô„Ç?
        getContext().getContentResolver().notifyChange(uri, null);

        return updateCount;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME, PLAYER);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME+"#", PLAYER_ID);
    }
}
