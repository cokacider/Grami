package llg.grami;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

public class ImagePageActivity extends AppCompatActivity {

    ViewPager pager;

    int key = 1 ;

    ImagePageAdapter adapter1;
    WormPageAdapter adapter2;
    SdokuPageAdapter adapter3;
    QuizPageAdapter adapter4;
    RscPageAdapter adapter5;

    Button btnskip;
    private ImageButton btnback;
    private ImageButton btnfront;
        //PagerAdapter를 상속받은 ImagePageAdapter 객체 생성
        //CustomAdapter에게 LayoutInflater 객체 전달
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_image_page_view);

        btnskip = (Button) findViewById(R.id.pageView_skip);

        btnback = (ImageButton) findViewById(R.id.pageView_back);
        btnfront = (ImageButton)findViewById(R.id.pageView_front);
        pager = (ViewPager) findViewById(R.id.pager);
        try {
            Intent intent = getIntent();
            key = intent.getExtras().getInt("key");
        }catch (NullPointerException e) {
            key = 1;
        }
        //ViewPager에 설정할 Adapter 객체 생성
        //ListView에서 사용하는 Adapter와 같은 역할.
        //다만. ViewPager로 스크롤 될 수 있도록 되어 있다는 것이 다름

        //ViewPager에 Adapter 설정
        switch (key) {
            case 1:
                adapter1 = new ImagePageAdapter(getLayoutInflater());
                pager.setAdapter(adapter1);
                break;
            case 2:
                adapter2 = new WormPageAdapter(getLayoutInflater());
                pager.setAdapter(adapter2);
                break;
            case 3:
                adapter3 = new SdokuPageAdapter(getLayoutInflater());
                pager.setAdapter(adapter3);
                break;
            case 4:
                adapter4 = new QuizPageAdapter(getLayoutInflater());
                pager.setAdapter(adapter4);
                break;
            case 5:
                adapter5 = new RscPageAdapter(getLayoutInflater());
                pager.setAdapter(adapter5);
                break;

            default:

                break;
        }
        //ViewPager에 Adapter 설정

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position == 0) {
                    if(View.VISIBLE == btnback.getVisibility())
                        btnback.setVisibility(View.INVISIBLE);
                } else {
                    if(View.INVISIBLE == btnback.getVisibility())
                        btnback.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void onClick(View view) {
        int position=pager.getCurrentItem();
        switch (view.getId()) {
            case R.id.pageView_skip:
                startMiniGame();
                break;
            case R.id.pageView_back:
                if (position != 0) {
                    pager.setCurrentItem(position-1,true);
                }
                break;
            case R.id.pageView_front:

                switch(key) {
                    case 1:
                        if (position == 9) {
                            startMiniGame();
                        } else {
                            pager.setCurrentItem(position+1,true);
                        }
                        break;
                    case 2:
                        if (position == adapter2.getCount() - 1) {
                            startMiniGame();
                        } else {
                            pager.setCurrentItem(position+1,true);
                        }
                        break;
                    case 3:
                        if (position == adapter3.getCount() - 1) {
                            startMiniGame();
                        } else {
                            pager.setCurrentItem(position+1,true);
                        }
                        break;
                    case 4:
                        if (position== adapter4.getCount() - 1) {
                            startMiniGame();
                        } else {
                            pager.setCurrentItem(position+1,true);
                        }
                        break;
                    case 5:
                        if (position == adapter5.getCount() - 1) {
                            startMiniGame();
                        } else {
                            pager.setCurrentItem(position+1,true);
                        }
                        break;
                }
                break;
        }
    }

    private void startMiniGame() {
        switch (key) {
            case 1: break;
            case 2://지렁이 게임
                //startActivity(new Intent(ImagePageActivity.this, SnakeActivity.class));
                break;
            case 3://스도쿠 게임
                //startActivity(new Intent(ImagePageActivity.this, SudroidActivity.class));
                break;
            case 4://퀴즈 게임
                //startActivity(new Intent(ImagePageActivity.this, QuizActivity.class));
                break;
            case 5://가위바위보 게임
                startActivity(new Intent(ImagePageActivity.this, RspActivity.class));
                break;

        }
        finish();
    }
}
