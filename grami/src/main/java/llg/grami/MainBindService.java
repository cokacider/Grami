package llg.grami;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainBindService extends Service {
    DBHelper dbHelper;

    public String backGroundColor = "#ffcbf4";

    private final String prefKeylastdate = "lastdate";
    private final String prefKeyfoot = "footcount";
    private final String prefKeylasttimeMillis = "lasttimeMillis";
    private final String prefName = "MainBindServicePref";

    BroadcastReceiver footCountReceiver;
    int[] footCounts = new int[FootCountValues.NUMDATE];
    int[] lastFootCount = new int[FootCountValues.NUMDATE]; // 어플 마지막으로 켰을 때 저장된 풋 카운트
    String lastDate;
    boolean firstFootReceive = true;    // foot receive 를 처음 받냐
    private final int pointPerFoot = 20;    // 몇 걸음에 1 point
    private final int happinessPerMinute = 5;    // 몇 분에 행복함 1 감소

    Grami grami = new Grami(); // 그라미의 정보
    User user = new User(); // 유저의 정보

    ListOfItem listOfItem = new ListOfItem();

    LevelTable levelTable = new LevelTable();

    private StatusCallback statusCallback;

    public boolean levelUpCheck = false;

    // 컴포넌트에 반환되는 IBinder
    private final IBinder binder = new LocalBinder();

    // 컴포넌트에 반환해줄 IBinder를 위한 클래스
    public class LocalBinder extends Binder {
        MainBindService getService() {
            return MainBindService.this;
        }
    }

    public MainBindService() {}

    @Override
    public void onCreate() {
        super.onCreate();

        init();

        // 걸음 수 측정 서비스 등록
        IntentFilter mainFilter = new IntentFilter("llg.grami.step");
        footCountReceiver = new FootCountReceiver();
        Log.d("MainBindService","registerReceiver");
        registerReceiver(footCountReceiver, mainFilter);

        // 젤 처음에 걸음 수 달라고 FootCountService에 요청
        Intent footcountResponse = new Intent("llg.grami.footcountreq");
        footcountResponse.putExtra("appStart", true);
        sendBroadcast(footcountResponse);

        // 어플이 꺼져있는 동안 걸음 수와 시간을 알기 위해 하는 연산
        SharedPreferences prefs = getSharedPreferences(prefName, MODE_PRIVATE);
        lastDate = prefs.getString(prefKeylastdate, "00000000");
        long now = System.currentTimeMillis();
        long lastTime = prefs.getLong(prefKeylasttimeMillis, now);
        int gapOfTime = (int)((now - lastTime) / (1000 * 60));   //  분
        //아직 Activity와 바인드 안 됐으므로 바로 변경
        grami.Happiness -= (gapOfTime / happinessPerMinute);
        if(grami.Happiness < 0 ) grami.Happiness = 0;

        for (int i = 0 ; i < FootCountValues.NUMDATE; i++) {
            lastFootCount[i] = prefs.getInt(prefKeyfoot + i, 0);
        }
    }

    private final String initprefName = "initValues";
    private final String level = "level";
    private final String strength = "strength";
    private final String hungry = "hungry";
    private final String happiness = "happiness";
    private final String exp = "exp";
    private final String money = "money";

    private void init() {
        Log.d("MainBindService", "init");
        // DB 셋팅
        dbHelper = new DBHelper(getApplicationContext(), "userItem.db", null, 1);
        dbHelper.unpackUserItems(user);

        SharedPreferences prefs = getSharedPreferences(initprefName, MODE_PRIVATE);
        grami.Level = prefs.getInt(level, 1);
        grami.Health = prefs.getInt(strength, 100);
        grami.Hungry = prefs.getInt(hungry, 100);
        grami.Happiness = prefs.getInt(happiness, 100);
        grami.exp = prefs.getInt(exp, 0);
        user.Money = prefs.getInt(money, 0);
        backGroundColor = prefs.getString("color","#ffcbf4");

    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        SharedPreferences prefs = getSharedPreferences(prefName, MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = prefs.edit();
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        prefEditor.putLong(prefKeylasttimeMillis, now);
        prefEditor.putString(prefKeylastdate, simpleDateFormat.format(date));
        for (int i = 0 ; i < FootCountValues.NUMDATE; i++) {
            prefEditor.putInt(prefKeyfoot + i, footCounts[i]);
        }
        prefEditor.commit();

        SharedPreferences initprefs = getSharedPreferences(initprefName, MODE_PRIVATE);
        SharedPreferences.Editor initprefEditor = initprefs.edit();
        initprefEditor.putInt(happiness, grami.Happiness);
        initprefEditor.putInt(hungry, grami.Hungry);
        initprefEditor.putInt(strength, grami.Health);
        initprefEditor.putInt(level, grami.Level);
        initprefEditor.putInt(exp,grami.exp);
        initprefEditor.putInt(money, user.Money);
        initprefEditor.commit();

        unregisterReceiver(footCountReceiver);

        super.onDestroy();
    }
    //콜백 인터페이스 선언
    public interface StatusCallback {
        public void recvData(); // 액티비티에서 선언한 콜백 함수
    }

    //액티비티에서 콜백 함수를 등록하기 위함.
    public void registerCallback(StatusCallback cb) {
        statusCallback = cb;
    }
    //서비스에서 액티비티 함수 호출은..
    //statusCallback.recvData();

    public Grami getGrami() { return grami; }
    public User getUser() { return user; }
    public ListOfItem getListOfItem() { return listOfItem; }
    public int getGramiStrenth() {
        return grami.Health;
    }
    public int getGramiHungry() {
        return grami.Hungry;
    }
    public int getGramiHappiness() {
        return grami.Happiness;
    }
    public int getGramiLevel() {
        return grami.Level;
    }
    public int getUserMoney() {
        return user.Money;
    }
    public void getFootCounts(int[] footCount) {
        for(int i = 0; i < FootCountValues.NUMDATE; i++) {
            footCount[i] = footCounts[i];
        }
    }

    public void addToGramiStrenth(int value) {
        grami.Health += value;
        if(grami.Health > 100) {
            grami.Health = 100;
        } else if(grami.Health < 0) {
            grami.Health = 0;
        }

        statusCallback.recvData();

        SharedPreferences prefs = getSharedPreferences(initprefName, MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putInt(strength, grami.Health);
        prefEditor.commit();

    }
    public void addToGramiHungry(int value) {
        grami.Hungry += value;
        if(grami.Hungry > 100) {
            grami.Hungry = 100;
        } else if(grami.Hungry < 0) {
            grami.Hungry = 0;
        }
        statusCallback.recvData();
        SharedPreferences prefs = getSharedPreferences(initprefName, MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putInt(hungry, grami.Hungry);
        prefEditor.commit();
    }
    public void addToGramiHappiness(int value) {
        grami.Happiness += value;
        if(grami.Happiness > 100) {
            grami.Happiness = 100;
        } else if(grami.Happiness < 0) {
            grami.Happiness = 0;
        }
        statusCallback.recvData();
        SharedPreferences prefs = getSharedPreferences(initprefName, MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putInt(happiness, grami.Happiness);
        prefEditor.commit();
    }
    public void addToGramiEXP(int value) {
        if(value > 0 && grami.exp > Integer.MAX_VALUE - 10000) return;
        grami.exp += value;
        if(grami.exp < 0) grami.exp = 0;
        statusCallback.recvData();
        SharedPreferences prefs = getSharedPreferences(initprefName, MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putInt(exp, grami.exp);
        prefEditor.commit();

        checkLevel();

    }
    public void cheatLevel(int value) {
        if( value < 1 && value > LevelTable.MAX_LEVEL) {
            return;
        }
        grami.Level = value;
        statusCallback.recvData();
        SharedPreferences prefs = getSharedPreferences(initprefName, MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putInt(level, grami.Level);
        prefEditor.commit();
    }

    private void checkLevel() {
        if(grami.Level == LevelTable.MAX_LEVEL) return;
        boolean loop = true;
        while(loop) {
            if (grami.Level == LevelTable.MAX_LEVEL) {
                return;
            }
            if (grami.exp >= levelTable.levelNeedEXP[grami.Level + 1]) {
                incrementGramiLevel();
                levelUpCheck = true;
            } else {
                loop = false;
            }
        }
    }

    public void incrementGramiLevel() {
        if(grami.Level == LevelTable.MAX_LEVEL) return;
        grami.Level++;
        statusCallback.recvData();
        SharedPreferences prefs = getSharedPreferences(initprefName, MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putInt(level, grami.Level);
        prefEditor.commit();
    }
    public boolean addToUserMoney(int value) {
        if (value > 0 && user.Money > Integer.MAX_VALUE - 10000) return false;
        user.Money += value;
        if(user.Money < 0) {
            user.Money = 0;
        }
        statusCallback.recvData();
        SharedPreferences prefs = getSharedPreferences(initprefName, MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putInt(money, user.Money);
        prefEditor.commit();
        return true;
    }
    public void buySomething(String somethingName, int somethingPrice) {
        int result = user.searchItem(somethingName);
        addToUserMoney(somethingPrice * -1);
        if (result == -1) {// 사려는게 user한테 없으면 1개 insert
            user.insert_item(somethingName, 1, listOfItem);
            dbHelper.insert(somethingName,1);
        } else {
            int prevNum = user.numMyItem.get(result);
            user.numMyItem.set(result, prevNum + 1);
            dbHelper.update(somethingName, prevNum + 1);
        }

    }

    public boolean useSomething(String somethingName) {
        //cloth 이면 false
        boolean res = true;
        int result = user.searchItem(somethingName);
        if(result == -1) {
            Log.e("MainBindService" , "useSomething there is no item : " + somethingName);
            return false;
        }
        Item usedItem = user.myItems.get(result);

        if (usedItem.category.equals("cloth")) res = false;

        if(res) {
            if (user.numMyItem.get(result) == 1) {
                // 가지고 있는게 하나일 때
                user.removeItem(result);
                dbHelper.delete(somethingName);
            } else {
                // 여러개일 때
                int prevNum = user.numMyItem.get(result);
                user.numMyItem.set(result, prevNum - 1);
                dbHelper.update(somethingName, prevNum - 1);
            }
        }
        execEffect(usedItem);
        return res;
    }

    private void execEffect(Item item) {
        // 아이템 사용시 효과
        addToGramiStrenth(item.E_Strength);
        addToGramiHungry(item.E_Hungry);
        addToGramiHappiness(item.E_Happniess);

        if(item.category.equals("food")) {

        } else if (item.category.equals("cloth")) {
             backGroundColor = listOfItem.searchColor(item.name);
            SharedPreferences prefs = getSharedPreferences(initprefName, MODE_PRIVATE);
            SharedPreferences.Editor prefEditor = prefs.edit();
            prefEditor.putString("color", backGroundColor);
            prefEditor.commit();

        } else if (item.category.equals("etc")) {

        }
    }

    class FootCountReceiver extends BroadcastReceiver {
        private int storedCount = 0;

        @Override
        public void onReceive(Context context, Intent intent) {

            // 걸음 수 받아옴
            String[] steps = new String[FootCountValues.NUMDATE];

            for (int i = 0 ; i < FootCountValues.NUMDATE; i++) {
                String bcName = i + "agoCount";
                steps[i] = intent.getStringExtra(bcName);
                footCounts[i] = Integer.parseInt(steps[i]);
            }
            if(firstFootReceive) {
                // 어플이 꺼져있는 동안 모아진 걸음 수로 체력 증가 / 배고픔 감소 / 경험치 증가
                String date = intent.getStringExtra("date");
                firstFootReceiveHandle(date);
                firstFootReceive = false;
            } else {
                storedCount++;
                if(storedCount == pointPerFoot) {
                    addToGramiStrenth(1);
                    addToGramiHungry(-2);
                    addToGramiEXP(10);
                    storedCount -= pointPerFoot;
                }
            }

            if(grami.Happiness == 0 && grami.Hungry == 0) {
                killGrami();
            }
        }

        private void firstFootReceiveHandle(String date) {
            int gapOfDate = Integer.parseInt(date)-Integer.parseInt(lastDate);
            int storedFoot = 0;
            for(int i = gapOfDate ; i < FootCountValues.NUMDATE; i++) {
                storedFoot += (footCounts[i] - lastFootCount[i - gapOfDate]);
            }
            int point = storedFoot / pointPerFoot;
            addToGramiEXP(point * 10);
            addToGramiStrenth(point);
            addToGramiHungry(point * -1);
        }
    }

    private void killGrami() {
        addToGramiEXP(Integer.MAX_VALUE * -1 + 10000);
        cheatLevel(1);
        addToGramiHappiness(-100);
        addToGramiStrenth(-100);
        addToGramiHungry(-100);
        statusCallback.recvData();

    }

    private String currentDate(){
        // 현재 날짜 구하기
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        // 출력될 포맷 설정 "년월일" ex) "20170609"
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        return simpleDateFormat.format(date);
    }

}
