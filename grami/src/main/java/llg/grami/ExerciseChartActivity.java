package llg.grami;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class ExerciseChartActivity extends AppCompatActivity {

    int[] footCount = new int[FootCountValues.NUMDATE];

    // 연결 타입 서비스
    private MainBindService mainBindService;
    private boolean mainbound = false;  // 서비스 연결 여부

    // 상단 상태바
    ImageButton bar_btnOption;
    TextView bar_strength;
    TextView bar_hungry;
    TextView bar_happiness;
    TextView bar_money;
    TextView bar_level;

    BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 상태바 없애기
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_exercise_chart);

        barChart = (BarChart)findViewById(R.id.exerciseChart);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MainBindService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        if(mainbound)
            statusCallback.recvData();
        super.onResume();
    }

    @Override
    protected void onPause() {
        if(mainbound)
            statusCallback.recvData();
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

    private MainBindService.StatusCallback statusCallback = new MainBindService.StatusCallback() {
        @Override
        public void recvData() {
            // 상태바 값 바꾸기
            bar_strength.setText(mainBindService.getGramiStrenth() + "");
            bar_hungry.setText(mainBindService.getGramiHungry() + "");
            bar_happiness.setText(mainBindService.getGramiHappiness() + "");
            bar_level.setText(mainBindService.getGramiLevel() + "");
            bar_money.setText(mainBindService.getUserMoney() + "");
        }
    };

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

        statusCallback.recvData();
        
        // line chart 만들기
        createChart();
        return true;
    }

    private void createChart() {
        // MPAndroidChart

        mainBindService.getFootCounts(footCount);

        ArrayList<BarEntry> entries = new ArrayList<>();
        for(int i = 0;i < FootCountValues.NUMDATE; i++) {
            entries.add(new BarEntry(footCount[FootCountValues.NUMDATE - 1 - i], i));
        }

        BarDataSet dataset = new BarDataSet(entries, "걸음 에너지 (일주일 전부터 오늘까지)");

        ArrayList<String> labels = new ArrayList<String>();
        for(int i = FootCountValues.NUMDATE - 1; i > 0; i--) {
            labels.add(i + "일 전");
        }
        labels.add("오늘");

        BarData data = new BarData(labels, dataset);
        dataset.setColors(ColorTemplate.COLORFUL_COLORS); //

        barChart.setData(data);
        barChart.setDescription(" ");
        barChart.animateY(5000);
        barChart.getLegend().setEnabled(false);
    }

    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btnBack:
                finish();
                break;
        }
    }
}
