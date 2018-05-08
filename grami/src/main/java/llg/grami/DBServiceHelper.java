package llg.grami;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by LG on 2017-06-08.
 */

public class DBServiceHelper extends SQLiteOpenHelper {
    static boolean AfterInstall = false; // 설치 후 첫 접속인지 확인
    public final static String tableName = "footservice";


    public DBServiceHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql_footcount = "create table if not exists "+tableName+"("
                + "date text DEFAULT '00000000', "
                + "step integer DEFAULT 0"
                +");";

        db.execSQL(sql_footcount);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + tableName);
        onCreate(db);
    }

    public void insert(String date, int step) {
        // 읽고 쓰기가 가능하게 DB 열기
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("step", step);

        db.insert(tableName, null, values);
        db.close();
    }

    public void update(String date, int step) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues new_values = new ContentValues();
        new_values.put("step", step);

        db.update(tableName, new_values, "date=?", new String[] {date});

        db.close();
    }

    public void delete(String date) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(tableName, "date=?", new String[]{date});
        // 입력한 항목과 일치하는 행 삭제
        db.close();
    }

    public boolean getDataFromDB(FootCountService footCountService) {
        Log.i("DBServiceHelper", "get Data From DB");
        boolean result = false ;    //DB에 값이 있는지 없는지
        boolean thereIsTodayData = false;

        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();

        // 현재 시간 구하기
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        // 출력될 포맷 설정
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        String nowdate = simpleDateFormat.format(date);

        // FootCountValues 초기화
        for(int i = 0; i < FootCountValues.NUMDATE; i++) {
            FootCountValues.step[i] = 0;
            FootCountValues.date[i] = "000000";
        }

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName, null);
        while (cursor.moveToNext()) {
            String thisdate = cursor.getString(0);
            int thisstep = cursor.getInt(1);

            Log.d("DBServiceHelper", "getDataFromDB " + thisdate + " " + thisstep);

            int gapOfDate = Integer.parseInt(nowdate) - Integer.parseInt(thisdate);

            if (gapOfDate < FootCountValues.NUMDATE && gapOfDate >= 0) {
                try {
                    FootCountValues.step[gapOfDate] = thisstep;
                    FootCountValues.date[gapOfDate] = thisdate;
                } catch (NullPointerException e) {
                    Log.e("getDataFromDB", "NullPointerException");
                }
            } else {
                // 7일 전보다 더 오래된 항목 삭제
                // table 의 항목은 최대 FootCountService.NUMDATE (8) 개로 유지
                delete(thisdate);
            }
            if(!result) {
                result = true;
            }
            if(gapOfDate == 0) {
                thereIsTodayData = true;
            }
        }

        footCountService.count = FootCountValues.step[0];
        if(!thereIsTodayData) {   // DB에 오늘 값이 없을 경우
            insert(nowdate, 0);
        }
        return result;
    }

}
