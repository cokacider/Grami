package llg.grami;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

public class InfoActivity extends AppCompatActivity {

    // 연결 타입 서비스
    private MainBindService mainBindService;
    private boolean mainbound = false;  // 서비스 연결 여부

    EditText cheatEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_info);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        cheatEdit = (EditText)findViewById(R.id.info_cheat);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MainBindService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

        }
    };

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_infocheat:
                String tag = cheatEdit.getText().toString();
                if(tag.length() > 6) {
                    if(tag.substring(0,5).equals("money")) {
                        int thevalue = inputEval(tag);
                        mainBindService.addToUserMoney(thevalue);
                        Toast.makeText(this, "money +" + thevalue, Toast.LENGTH_SHORT).show();
                    }
                    else if(tag.substring(0,5).equals("stren")) {
                        int thevalue = inputEval(tag);
                        mainBindService.addToGramiStrenth(thevalue);
                        Toast.makeText(this, "strength +" + thevalue, Toast.LENGTH_SHORT).show();
                    }
                    else if(tag.substring(0,5).equals("hungr")) {
                        int thevalue = inputEval(tag);
                        mainBindService.addToGramiHungry(thevalue);
                        Toast.makeText(this, "hungry +" + thevalue, Toast.LENGTH_SHORT).show();
                    }
                    else if(tag.substring(0,5).equals("happi")) {
                        int thevalue = inputEval(tag);
                        mainBindService.addToGramiHappiness(thevalue);
                        Toast.makeText(this, "happiness +" + thevalue, Toast.LENGTH_SHORT).show();
                    }
                    else if(tag.substring(0,5).equals("exper")) {
                        int thevalue = inputEval(tag);
                        mainBindService.addToGramiEXP(thevalue);
                        Toast.makeText(this, "exp +" + thevalue, Toast.LENGTH_SHORT).show();
                    }
                    else if(tag.substring(0,5).equals("level")) {
                        int thevalue = inputEval(tag);
                        if(!(thevalue > 0 && thevalue <= 100)) {
                            break;
                        }
                        mainBindService.cheatLevel(thevalue);
                        Toast.makeText(this, "level " + thevalue, Toast.LENGTH_SHORT).show();
                    }
                    else if(tag.substring(0,5).equals("reset")) {
                        mainBindService.cheatLevel(1);
                        mainBindService.addToGramiEXP(-10000000);
                        mainBindService.addToUserMoney(-10000000);
                        mainBindService.addToGramiHappiness(100);
                        mainBindService.addToGramiHungry(100);
                        mainBindService.addToGramiStrenth(100);

                        SharedPreferences mPref = getSharedPreferences("isFirst", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = mPref.edit();
                        editor.putBoolean("isFirst", false);
                        editor.commit();

                        Toast.makeText(this, "reset", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private int inputEval(String tag) {
        int startInd = 5;
        boolean neg = false;
        if (tag.substring(5,6).equals("-")) {
            startInd = 6;
            neg = true;
        }
        String value = tag.substring(startInd,tag.length());
        int thevalue = Integer.parseInt(value);
        if (neg) {
            thevalue *= -1;
        }
        return thevalue;
    }


}
