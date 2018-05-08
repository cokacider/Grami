package llg.grami;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by A on 2017-04-21.
 */

public class DBHelper extends SQLiteOpenHelper {
    static boolean isFirstExec = true; // 설치 후 첫 접속인지 확인
    public final static String tableName = "userItem";

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql= "create table if not exists " + tableName + "("
                + "name text DEFAULT 'null', "
                + "number integer DEFAULT 1"
                +");";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "drop table if exists " + tableName;
        db.execSQL(sql);

        onCreate(db);
    }


    public void insert(String name, int number) {
        // 읽고 쓰기가 가능하게 DB 열기
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("number", number);

        db.insert(tableName, null, values);
        db.close();
    }

    public void update(String name, int number) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues new_values = new ContentValues();
        new_values.put("number", number);

        db.update(tableName, new_values, "name=?", new String[] {name});

        db.close();
    }

    public void delete(String name) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(tableName, "name=?", new String[]{name});
        // 입력한 항목과 일치하는 행 삭제
        db.close();
    }



    public void unpackUserItems(User user) {
        SQLiteDatabase db = getReadableDatabase();
        // grami class DB 값 저장
        Cursor c = db.query(tableName, null, null, null, null, null, null, null);
        ListOfItem listOfItem = new ListOfItem();
        while(c.moveToNext()) {
            String itemName = c.getString(c.getColumnIndex("name"));
            int numItem = c.getInt(c.getColumnIndex("number"));
            user.insert_item(itemName, numItem, listOfItem);

            if(isFirstExec)
                isFirstExec = false;
        }

        db.close();
    }
}
