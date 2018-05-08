package llg.grami;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FootCountService extends Service implements SensorEventListener{

    public int count;

//    public final static int NUMDATE = 8;
//    public FootCount[] footCounts; // 0은 오늘 1은 어제...

    private long lastTime;
    private float speed;
    private float lastX;
    private float lastY;
    private float lastZ;

    private float x, y, z;
    private static final int SHAKE_THRESHOLD = 800;

    private static final int DATA_X = 0;
    private static final int DATA_Y = 1;
    private static final int DATA_Z = 2;

    DBServiceHelper helper;
    SQLiteDatabase db;

    private SensorManager sensorManager;
    private Sensor accelerormeterSensor;

    // 재시작
    private static final int MILLISINFUTURE = 1000 * 1000;
    private static final int COUNT_DOWN_INTERVAL = 1000;

    private CountDownTimer countDownTimer;

    private BroadcastReceiver mainServiceReceiver;

    public FootCountService() {
    }

    @Override
    public void onCreate() {
        unregisterRestartAlarm();
        super.onCreate();
        countDownTimer();
        countDownTimer.start();

        Log.i("FootCountService","Service is Create");
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerormeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

//        if(footCounts == null) footCounts = new FootCount[NUMDATE];
        new FootCountValues();
        helper = new DBServiceHelper(getApplicationContext(), "footservice.db", null, 1);

        // 방송 from MainBindService
        mainServiceReceiver = new MainServiceReceiver();
        IntentFilter mainFilter = new IntentFilter("llg.grami.footcountreq");
        registerReceiver(mainServiceReceiver,mainFilter);
    }

    private void unregisterRestartAlarm() {
        Intent intent = new Intent(FootCountService.this, RestartService.class);
        PendingIntent sender = PendingIntent.getBroadcast(FootCountService.this,0,intent,0);

        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        //알람 취소
        alarmManager.cancel(sender);
    }

    public void countDownTimer() {
        countDownTimer = new CountDownTimer(MILLISINFUTURE, COUNT_DOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

            }
        };
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("FootCountService","Service is started");


        if (accelerormeterSensor != null)
            sensorManager.registerListener(this, accelerormeterSensor, SensorManager.SENSOR_DELAY_GAME);

        if (helper == null)
            helper = new DBServiceHelper(getApplicationContext(), "footservice.db", null, 1);


        helper.getDataFromDB(this);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {

        Log.i("FootCountService","Service is destroyed");
        if (sensorManager != null)
            sensorManager.unregisterListener(this);

        unregisterReceiver(mainServiceReceiver);

        updateDB(currentDate());

        super.onDestroy();

        countDownTimer.cancel();
        //서비스 종료 시 알람 등록을 통해 서비스 재실행
        registerRestartAlarm();
    }

    private void registerRestartAlarm() {
        Intent intent = new Intent(FootCountService.this, RestartService.class);
        intent.setAction("ACTION.RESTART.FootCountService");
        PendingIntent sender = PendingIntent.getBroadcast(FootCountService.this,0,intent,0);
        long firstTime = SystemClock.elapsedRealtime();
        firstTime += 1*1000;

        AlarmManager alarmMangager = (AlarmManager)getSystemService(ALARM_SERVICE);
        // 알람 등록
        alarmMangager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 1*1000,sender);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();
            long gabOfTime = (currentTime - lastTime);

            Date date = new Date(currentTime);
            String currentDate = new SimpleDateFormat("yyyyMMdd").format(date);

            // 변수에 저장된 날짜와 현재 날짜가 맞지 않을 경우
            if (!currentDate.equals(FootCountValues.date[0])) {
                Log.d("FootCountService", "currentDate : " + currentDate + " savedDate : " + FootCountValues.date[0]);
                helper.getDataFromDB(this);
            }

            if (gabOfTime > 100) {
                lastTime = currentTime;

                x = event.values[DATA_X];
                y = event.values[DATA_Y];
                z = event.values[DATA_Z];

                // 걸음 수 측정 algorithm
                speed = Math.abs(x + y + z - lastX - lastY - lastZ) / gabOfTime * 10000;
                if (speed > SHAKE_THRESHOLD) {

                    if(count < Integer.MAX_VALUE)
                        FootCountValues.step[0] = ++count;

                    Log.e("Step!", "SHAKE " + count);

                    broadCastFootCount(currentDate);

                    // 매번 저장함
                    updateDB(currentDate);

                }
                lastX = event.values[DATA_X];
                lastY = event.values[DATA_Y];
                lastZ = event.values[DATA_Z];
            }
        }
    }

    private void broadCastFootCount(String date) {
        Intent myFilteredResponse = new Intent("llg.grami.step");

        myFilteredResponse.putExtra("date", date);
        // 8일치 step 을 방송
        for (int i = 0 ; i < FootCountValues.NUMDATE; i ++) {
            String bcName = i + "agoCount";

            myFilteredResponse.putExtra(bcName, FootCountValues.step[i] + "");
        }

        sendBroadcast(myFilteredResponse);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private void updateDB(String currentDate) {
        //int nowint = Integer.parseInt(currentDate());
        Log.i("FootCountService","update DB");
//        for(int i = 0 ; i < NUMDATE ; i++) {
//            dbHelper.update(Integer.toString(nowint - i), footCounts[i].step);
//        }
        helper.update(currentDate, FootCountValues.step[0]);
    }

    private String currentDate(){
        // 현재 날짜 구하기
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        // 출력될 포맷 설정 "년월일" ex) "20170609"
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        return simpleDateFormat.format(date);
    }

    class MainServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean appStart = intent.getBooleanExtra("appStart", false);
            if(appStart){
                broadCastFootCount(currentDate());
            }
        }
    }

}
