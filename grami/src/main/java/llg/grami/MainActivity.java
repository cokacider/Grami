package llg.grami;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    private MainView mainView;

    private ScreenConfig screenConfig;
    private boolean confirmMainViewScreenSize;

    public int width;
    public int height;

    // 걸음수 측정 서비스
    Intent intentFootCountService;

    // 연결 타입 서비스
    public MainBindService mainBindService;
    private boolean mainbound = false;  // 서비스 연결 여부

    // 상단 상태바
    ImageButton bar_btnOption;
    TextView bar_strength;
    TextView bar_hungry;
    TextView bar_happiness;
    TextView bar_money;
    TextView bar_level;

    public boolean isOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 상태바 없애기
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        // 전체 화면 크기 구하기
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;

        mainView = (MainView)findViewById(R.id.main_view);

        Log.e("Start", "Start...");

        // surfaceview, screenConfig 초기화
        screenConfig = new ScreenConfig(screenWidth, screenHeight, this);
        mainView.init(screenWidth, screenHeight, this, screenConfig);

        // 걸음 수 측정 서비스 켜기
        if(!isServiceRunningCheck()) {
            intentFootCountService = new Intent(this,FootCountService.class);
            Log.d("MainActivity","startService");
            startService(intentFootCountService);
        }

        confirmFirst();
    }

    private void confirmFirst() {
        try{
            SharedPreferences mPref = getSharedPreferences("isFirst", Activity.MODE_PRIVATE);

            Boolean bFirst = mPref.getBoolean("isFirst", false);
            if(!bFirst)
            {
                Log.d("version", "first");
                SharedPreferences.Editor editor = mPref.edit();
                editor.putBoolean("isFirst", true);
                editor.commit();

                Intent pagerIntent = new Intent(MainActivity.this, ImagePageActivity.class);
                pagerIntent.putExtra("key",1);
                startActivity(pagerIntent);
            }
            if(bFirst)
            {
                Log.d("version", "not first");
            }
        } catch (Exception e){};
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MainBindService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        if(mainbound) {
            statusCallback.recvData();
        }
        isOn =true;
        super.onResume();
    }

    @Override
    protected void onPause() {
        if(mainbound)
            statusCallback.recvData();
        isOn = false;
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mainbound) {
            unbindService(serviceConnection);
            mainbound = false;
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MainBindService.LocalBinder binder = (MainBindService.LocalBinder) service;
            mainBindService = binder.getService();
            mainbound = true;
            mainBindService.registerCallback(statusCallback);   //콜백 등록
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mainbound = false;
        }
    };
    public boolean untilConfirm = true;

    public MainBindService.StatusCallback statusCallback = new MainBindService.StatusCallback() {
        @Override
        public void recvData() {
            // 상태바 값 바꾸기
            bar_strength.setText(mainBindService.getGramiStrenth() + "");
            bar_hungry.setText(mainBindService.getGramiHungry() + "");
            bar_happiness.setText(mainBindService.getGramiHappiness() + "");
            bar_level.setText(mainBindService.getGramiLevel() + "");
            bar_money.setText(mainBindService.getUserMoney() + "");

            if(mainBindService.getGramiHungry() == 0 && mainBindService.getGramiHappiness() == 0) {
                if(untilConfirm) {
                    gramiDieDialg();
                }
            }
        }

        private void gramiDieDialg() {
            untilConfirm = false;
            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mainBindService.addToGramiHappiness(100);
                    mainBindService.addToGramiStrenth(100);
                    mainBindService.addToGramiHungry(100);
                    untilConfirm = true;
                    dialog.dismiss();     //닫기
                }
            });
            alert.setMessage("그라미가 사랑과 관심이 없어서 죽었어요ㅠㅠ\n근데 다시 살아났어요~");
            alert.show();
        }
    };

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(confirmMainViewScreenSize == false) {
            // 서피스뷰의 크기 구하기
            View main = (FrameLayout) findViewById(R.id.shop_content);
            width = main.getWidth();
            height = main.getHeight();

            screenConfig.initGramiPosition();
            screenConfig.initWH(width, height);

            if(width != 0 && height != 0)
            confirmMainViewScreenSize = true;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Action Bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);      //액션바 아이콘을 업 네비게이션 형태로 표시합니다.
        actionBar.setDisplayShowTitleEnabled(false);     //액션바에 표시되는 제목의 표시유무를 설정합니다.
        actionBar.setDisplayShowHomeEnabled(false);      //홈 아이콘을 숨김처리합니다.

        View mCustomView = LayoutInflater.from(this).inflate(R.layout.main_actionbar, null);
        actionBar.setCustomView(mCustomView);

        //액션바 양쪽 공백 없애기
        Toolbar parent = (Toolbar)mCustomView.getParent();
        parent.setContentInsetsAbsolute(0,0);

        bar_btnOption = (ImageButton)findViewById(R.id.btnBack);
        bar_strength = (TextView)findViewById(R.id.text_strength);
        bar_hungry = (TextView)findViewById(R.id.text_hungry);
        bar_happiness = (TextView)findViewById(R.id.text_happiness);
        bar_money = (TextView)findViewById(R.id.text_money);
        bar_level = (TextView)findViewById(R.id.text_level);

        //main activity 에서는 뒤로가기 그림이 아니라 옵션
        bar_btnOption.setBackground(getDrawable(R.drawable.option));
        statusCallback.recvData();
        return true;
    }

    private final int REQUEST_CODE_SHOP = 1111;
    private final int REQUEST_CODE_INVENTORY = 2222;
    private final int REQUEST_CODE_MINIGAME = 3333;
    private final int REQUEST_CODE_OPTION = 4444;

    public void onClick(View view) {
        switch (view.getId()) {

            //하단 메뉴바 버튼
            case R.id.btnShop:
                startActivityForResult(new Intent(MainActivity.this, ShopActivity.class), REQUEST_CODE_SHOP);
                break;

            case R.id.btnInventory:
                startActivityForResult(new Intent(MainActivity.this, InventoryActivity.class), REQUEST_CODE_INVENTORY);
                break;

            case R.id.btnPlay:
                final PopupMenu playpopupMenu = new PopupMenu(this, view);
                playpopupMenu.getMenuInflater().inflate(R.menu.play_popup, playpopupMenu.getMenu());
                playpopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.popup_benice:
                                String wearthis;
                                if (!screenConfig.wear) {
                                    wearthis = "이걸 입혀볼까?";
                                } else {
                                    wearthis = "옷 벗자";
                                }
                                Toast.makeText(MainActivity.this, wearthis, Toast.LENGTH_SHORT).show();
                                screenConfig.wear = !screenConfig.wear;
                                break;
                            case R.id.popup_exercise:
                                startActivity(new Intent(MainActivity.this, ExerciseChartActivity.class));
                                break;
                            case R.id.popup_minigame:
                                // 미니게임
                                startActivityForResult(new Intent(MainActivity.this, MinigameActivity.class), REQUEST_CODE_MINIGAME);
                                break;
                        }
                        return false;
                    }
                });
                playpopupMenu.show();
                break;

            case R.id.btnBack:
                // 옵션
                startActivityForResult(new Intent(MainActivity.this, OptionActivity.class),REQUEST_CODE_OPTION);
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SHOP:
            case REQUEST_CODE_INVENTORY:
            case REQUEST_CODE_MINIGAME:
            case REQUEST_CODE_OPTION:
                if(resultCode == RESULT_OK) {
                    Log.d("MainActivity", "onActivityResult RESULT_OK");
                    statusCallback.recvData();
                }
                break;

        }
    }

    @Override
    public void onBackPressed() {
        // 뒤로가기 버튼 클릭 시 종료 대화상자
        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setTitle("Grami").setMessage("종료하시겠습니까?").setPositiveButton("네", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }).setNegativeButton("아니요", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).create().show();
    }

    // FootCountService  서비스가 작동 중인지 확인하는 함수
    private boolean isServiceRunningCheck() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("llg.grami.FootCountService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
