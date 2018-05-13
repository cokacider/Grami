package llg.grami;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MinigameActivity extends AppCompatActivity {

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

    ListView listview ;
    ListViewAdapter listAdapter;

    private boolean isInit = false;

    private long startTime ;
    private int requestCode = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 상태바 없애기
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_minigame);

        // Adapter 생성
        listAdapter = new ListViewAdapter(this) ;

        // 리스트뷰 참조 및 Adapter달기
        listview = (ListView) findViewById(R.id.listview1);
        listview.setAdapter(listAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                // get item
                ListViewItem item = (ListViewItem) parent.getItemAtPosition(position) ;

                String titleStr = item.getTitle() ;
                String descStr = item.getDesc() ;
                Drawable iconDrawable = item.getIcon() ;
                String IntentName = item.getIntent();
                int limitLevel = item.getLimitLevel();
                int gramiLevel = mainBindService.getGramiLevel();
                int gramiStrength = mainBindService.getGramiStrenth();


                final AlertDialog.Builder dialog = new AlertDialog.Builder(MinigameActivity.this);
                dialog.setTitle("감히!");
                dialog.setMessage("아직 자격이나 없거나 체력이 부족합니다!!");

                if(gramiLevel >= limitLevel && gramiStrength >= 20) {
                    Intent gameIntent;

                    Intent tempIntent = new Intent(MinigameActivity.this, ImagePageActivity.class);
                    switch (IntentName) {
                        case "SudroidActivity.class":

                            tempIntent.putExtra("key",3);
//                            gameIntent = new Intent(MinigameActivity.this, SudroidActivity.class);
                            dialogMake(tempIntent,titleStr);
                            break;
                        case "SnakeActivity.class":

                            tempIntent.putExtra("key",2);
                            dialogMake(tempIntent,titleStr);
                            break;
                        case "QuizActivity.class":

                            tempIntent.putExtra("key",4);
                            dialogMake(tempIntent,titleStr);
                            break;
                        case "RspActivity.class":

                            tempIntent.putExtra("key",5);
                            dialogMake(tempIntent,titleStr);
                            break;
                        default:
                            dialog.show();
                    }

                } else {
                    dialog.show();
                }

            }

            private void getReward() {
                mainBindService.addToGramiStrenth(-20);
                mainBindService.addToGramiHappiness(10);
                mainBindService.addToGramiHungry(-5);
                mainBindService.addToUserMoney(70);
                mainBindService.addToGramiEXP(200);
            }

            private void dialogMake(final Intent gameIntent, String title) {

                AlertDialog.Builder alert = new AlertDialog.Builder(MinigameActivity.this);

                alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();     //닫기
                    }
                });
                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();     //닫기
                        getReward();
                        startActivity(gameIntent);
                    }
                });

                alert.setMessage(title + "을 시작하겠습니다");
                alert.show();
            }
        }) ;
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == this.requestCode) {
//            Log.d("MinigameActivity", " onActivityResult");
//            long finishTime = System.currentTimeMillis();
//            long gapofTime = 0;
//            if(startTime != 0) {
//                gapofTime = finishTime - startTime;
//            }
//            String whatgame = data.getStringExtra("whatgame");
//            if (whatgame.equals("Snake")) {
//                int result = data.getIntExtra("result", 1);
//                Log.d("Minigame", "snake result " + result);
//                mainBindService.addToGramiEXP(result * 15);     // 300
//                mainBindService.addToGramiHappiness(result * 1);    // 20
//                mainBindService.addToUserMoney(result * 40);     // 800
//            } else if (whatgame.equals("Sudroid")) {
//
//            } else if (whatgame.equals("Quiz")) {
//
//            } else if (whatgame.equals("RSP")) {
//
//            }
//        }
//    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MainBindService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
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

            if(!isInit) {
                isInit = true;
                initList();
            }

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


        return true;
    }


    @Override
    protected void onResume() {
        if(mainbound)
            statusCallback.recvData();
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mainbound) {
            unbindService(serviceConnection);
            mainbound = false;
        }
    }

    private void initList() {
        // 첫 번째 아이템 추가.
        listAdapter.addItem(ContextCompat.getDrawable(this, R.drawable.coin),
                " 에피소드 1", " 나랑 내기 할렝?","RspActivity.class", 0) ;
        // 두 번째 아이템 추가.
        listAdapter.addItem(ContextCompat.getDrawable(this, R.drawable.coin),
                " 에피소드 2", " 같이 수도쿠 해볼렝?", "SudroidActivity.class", 5) ;
        // 세 번째 아이템 추가.
        listAdapter.addItem(ContextCompat.getDrawable(this, R.drawable.coin),
                " 에피소드 3", " 퀴즈문제 풀어보자!","QuizActivity.class", 10) ;
        // 네 번째 아이템 추가.
        listAdapter.addItem(ContextCompat.getDrawable(this, R.drawable.coin),
                " 에피소드 4", " 지렁이 먹이 먹어서 길게 키우자!","SnakeActivity.class", 15) ;
        // 네 번째 아이템 추가.
        listAdapter.addItem(ContextCompat.getDrawable(this, R.drawable.coin),
                " 에피소드 5", " 퍼즐맞추기! 재밌겠지?","Activity.class", 20) ;
        // 네 번째 아이템 추가.
        listAdapter.addItem(ContextCompat.getDrawable(this, R.drawable.coin),
                " 에피소드 6", " 반응 속도 빨라용?","Activity.class", 25) ;
        // 네 번째 아이템 추가.
        listAdapter.addItem(ContextCompat.getDrawable(this, R.drawable.coin),
                " 에피소드 7", " 공놀이가 제일 재밌어!","Activity.class", 30) ;

        listAdapter.notifyDataSetChanged();

    }

    class ListViewItem {
        private Drawable iconDrawable ;
        private String titleStr ;
        private String descStr ;
        private String intentName;
        private int limitLevel;

        public void setIcon(Drawable icon) {
            iconDrawable = icon ;
        }
        public void setTitle(String title) {
            titleStr = title ;
        }
        public void setDesc(String desc) {
            descStr = desc ;
        }
        public void setIntentName(String intent){
            intentName = intent;
        }
        public void setLimitLevel(int level) { limitLevel = level;}

        public Drawable getIcon() {
            return this.iconDrawable ;
        }
        public String getTitle() {
            return this.titleStr ;
        }
        public String getDesc() {
            return this.descStr ;
        }
        public String getIntent() {
            return this.intentName;
        }
        public int getLimitLevel() { return limitLevel; }
    }

    class ListViewAdapter extends BaseAdapter {
        // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
        private ArrayList<ListViewItem> listViewItemList = new ArrayList<ListViewItem>() ;
        private Context context;

        // ListViewAdapter의 생성자
        public ListViewAdapter(Context context) {
            this.context = context;
        }

        // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
        @Override
        public int getCount() {
            return listViewItemList.size() ;
        }

        // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d("MinigameAcitivity", "adapter getView");
            final int pos = position;

            // "minigame_listitem" Layout을 inflate하여 convertView 참조 획득.
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.minigame_listitem, parent, false);
            }

            // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
            ImageView iconImageView = (ImageView) convertView.findViewById(R.id.imageView1) ;
            TextView titleTextView = (TextView) convertView.findViewById(R.id.textView1) ;
            TextView descTextView = (TextView) convertView.findViewById(R.id.textView2) ;

            // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
            ListViewItem listViewItem = listViewItemList.get(position);

            int gramiLevel = mainBindService.getGramiLevel();
            int limitLevel = listViewItem.getLimitLevel();

            // 아이템 내 각 위젯에 데이터 반영
            Log.d("MinigameActivity", "아이템 내 각 위젯에 데이터 반영");
            iconImageView.setImageDrawable(listViewItem.getIcon());
            titleTextView.setText(listViewItem.getTitle());
            if(gramiLevel >= limitLevel) {
                descTextView.setText(listViewItem.getDesc());
            } else {
                descTextView.setText("제한 레벨 : " + limitLevel + "이상의 그라미\n 밥 좀 더 먹고 온나~");
            }

            return convertView;
        }

        // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
        @Override
        public long getItemId(int position) {
            return position ;
        }

        // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
        @Override
        public Object getItem(int position) {
            return listViewItemList.get(position) ;
        }

        // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
        public void addItem(Drawable icon, String title, String desc, String intentName, int limitLevel) {
            Log.d("MinigameAcitivity", "addItem " + title);

            ListViewItem item = new ListViewItem();

            item.setIcon(icon);
            item.setTitle(title);
            item.setDesc(desc);
            item.setIntentName(intentName);
            item.setLimitLevel(limitLevel);

            listViewItemList.add(item);
        }
    }

    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btnBack:
                finish();
                break;
        }
    }

}
