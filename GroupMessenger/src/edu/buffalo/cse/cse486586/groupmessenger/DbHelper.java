package edu.buffalo.cse.cse486586.groupmessenger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

	private static final String DB_NAME = "groupMessenger.db";
	private static final int DB_VERSION = 1;
	private static final String TABLE_NAME = "provider";
	private static final String C_KEY = "key";
	private static final String C_VALUE = "value";
	private static final String CREATE_TABLE = "create table " + TABLE_NAME
			+ "("+ C_KEY + " text primary key,"
			+ C_VALUE +" text);" ;
	
	public DbHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, DB_NAME, factory, DB_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}

}
