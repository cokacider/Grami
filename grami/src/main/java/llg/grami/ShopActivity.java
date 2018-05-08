package llg.grami;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class ShopActivity extends AppCompatActivity {

    // 연결 타입 서비스
    private MainBindService mainBindService;
    private boolean mainbound = false;  // 서비스 연결 여부

    private ListView listView_food = null;
    private ShopAdapter adapter_food = null;

    private ListView listView_cloth = null;
    private ShopAdapter adapter_cloth = null;

    private ListView listView_etc = null;
    private ShopAdapter adapter_etc = null;

    // 상단 상태바
    ImageButton bar_btnOption;
    TextView bar_strength;
    TextView bar_hungry;
    TextView bar_happiness;
    TextView bar_money;
    TextView bar_level;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 상태바 없애기
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_shop);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        listView_food = (ListView)findViewById(R.id.shop_list_food);
        listView_cloth = (ListView)findViewById(R.id.shop_list_cloth);
        listView_etc = (ListView)findViewById(R.id.shop_list_etc);

        adapter_food = new ShopAdapter(this);
        listView_food.setAdapter(adapter_food);

        adapter_cloth = new ShopAdapter(this);
        listView_cloth.setAdapter(adapter_cloth);

        adapter_etc = new ShopAdapter(this);
        listView_etc.setAdapter(adapter_etc);

        setShopItems();

        // food item 클릭시
        listView_food.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ShopListData data = adapter_food.listData.get(position);
                makeDialog(data);
            }
        });

        // cloth item 클릭시
        listView_cloth.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ShopListData data = adapter_cloth.listData.get(position);
                makeDialog(data);
            }
        });

        // etc item 클릭시
        listView_etc.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ShopListData data = adapter_etc.listData.get(position);
                makeDialog(data);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MainBindService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        setResult(RESULT_OK);
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

    public void makeDialog(final ShopListData data) {
        final Dialog buyDialog = new Dialog(ShopActivity.this);
        buyDialog.setContentView(R.layout.shop_buydialog);
        buyDialog.setTitle("아이템 구매하기");

        ImageView image = (ImageView) buyDialog.findViewById(R.id.shopbuydialog_image);
        TextView name = (TextView) buyDialog.findViewById(R.id.shopbuydialog_name);
        TextView price = (TextView) buyDialog.findViewById(R.id.shopbuydialog_price);
        TextView effect = (TextView) buyDialog.findViewById(R.id.shopbuydialog_effect);

        image.setImageDrawable(data.image);
        name.setText(data.name);
        price.setText(Integer.toString(data.price) + " " + getText(R.string.money) );
        effect.setText(data.effect);

        Button btnBuy = (Button) buyDialog.findViewById(R.id.shopbuydialog_buy);
        Button btnCancel = (Button) buyDialog.findViewById(R.id.shopbuydialog_cancel);

        btnBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int userMoney = mainBindService.getUserMoney();
                if(userMoney >= data.price) {
                    // 산다 누르고 돈이 충분할 때
                    mainBindService.buySomething(data.name, data.price);
                    buyDialog.dismiss();

                    AlertDialog.Builder alert = new AlertDialog.Builder(ShopActivity.this);
                    alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();     //닫기
                        }
                    });
                    alert.setMessage(data.name + "을 샀다!!!");
                    alert.show();
                } else {
                    // 돈이 없을 때
                    buyDialog.dismiss();

                    AlertDialog.Builder alert = new AlertDialog.Builder(ShopActivity.this);
                    alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();     //닫기
                        }
                    });
                    alert.setMessage("돈 더 가져와!!!");
                    alert.show();
                }

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buyDialog.dismiss();    // 취소 눌렀을 때
            }
        });

        buyDialog.show();
    }

    private void setShopItems() {
        ListOfItem listOfItem = new ListOfItem();

        for (int i = 0; i < listOfItem.items.size(); i++) {
            Item item = listOfItem.items.get(i);
            String imgresName = "@drawable/" + item.imgFile;
            String imgpackName = this.getPackageName();

            // 설명 string 만들기
            String effect = "";
            if (item.E_Strength != 0) {
                effect += "체력 : +" + item.E_Strength + " ";
            }
            if (item.E_Hungry != 0) {
                effect += "배부름 : +" + item.E_Hungry + " ";
            }
            if (item.E_Happniess != 0) {
                effect += "행복함 : +" + item.E_Happniess;
            }

            if(item.category.equals("food")) {
                adapter_food.addItem(getResources().getDrawable(getResources().getIdentifier(imgresName, "drawable", imgpackName)),
                        item.name,
                        item.price,
                        effect);
            } else if (item.category.equals("cloth")) {
                adapter_cloth.addItem(getResources().getDrawable(getResources().getIdentifier(imgresName, "drawable", imgpackName)),
                        item.name,
                        item.price,
                        effect);
            } else if (item.category.equals("etc")) {
                adapter_etc.addItem(getResources().getDrawable(getResources().getIdentifier(imgresName, "drawable", imgpackName)),
                        item.name,
                        item.price,
                        effect);
            }

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

        statusCallback.recvData();

        return true;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_food:
                    if(listView_cloth.getVisibility() == View.VISIBLE) listView_cloth.setVisibility(View.GONE);
                    else if(listView_etc.getVisibility() == View.VISIBLE) listView_etc.setVisibility(View.GONE);

                    if(listView_food.getVisibility() != View.VISIBLE) listView_food.setVisibility(View.VISIBLE);
                    return true;
                case R.id.navigation_cloth:
                    if(listView_food.getVisibility() == View.VISIBLE) listView_food.setVisibility(View.GONE);
                    else if(listView_etc.getVisibility() == View.VISIBLE) listView_etc.setVisibility(View.GONE);

                    if(listView_cloth.getVisibility() != View.VISIBLE) listView_cloth.setVisibility(View.VISIBLE);
                    return true;
                case R.id.navigation_etc:
                    if(listView_cloth.getVisibility() == View.VISIBLE) listView_cloth.setVisibility(View.GONE);
                    else if(listView_food.getVisibility() == View.VISIBLE) listView_food.setVisibility(View.GONE);

                    if(listView_etc.getVisibility() != View.VISIBLE) listView_etc.setVisibility(View.VISIBLE);
                    return true;
            }
            return false;
        }

    };

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnBack:

                finish();
                break;
        }
    }

    private class ViewHolder {
        public ImageView image;
        public TextView name;
        public TextView price;
        public TextView effect;
    }

    public class ShopAdapter extends BaseAdapter {
        private Context context = null;
        private ArrayList<ShopListData> listData = new ArrayList<ShopListData>();

        public ShopAdapter(Context context) {
            super();
            this.context = context;
        }
        @Override
        public int getCount() {
            return listData.size();
        }

        @Override
        public Object getItem(int position) {
            return listData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.shop_listitem, null);

                holder.image = (ImageView) convertView.findViewById(R.id.shoplistitem_image);
                holder.name = (TextView) convertView.findViewById(R.id.shoplistitem_name);
                holder.price = (TextView)convertView.findViewById(R.id.shoplistitem_price);
                holder.effect = (TextView)convertView.findViewById(R.id.shoplistitem_effect);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ShopListData data = listData.get(position);

            if(data.image != null) {
                holder.image.setVisibility(View.VISIBLE);
                holder.image.setImageDrawable(data.image);
            } else {
                holder.image.setVisibility(View.INVISIBLE); // GONE
            }

            holder.name.setText(data.name);
            holder.price.setText(Integer.toString(data.price) + " " + getText(R.string.money));
            holder.effect.setText(data.effect);

            return convertView;
        }

        public void addItem(Drawable image, String name, int price, String effect) {
            ShopListData addInfo;
            addInfo = new ShopListData();
            addInfo.image = image;
            addInfo.name = name;
            addInfo.price = price;
            addInfo.effect = effect;

            listData.add(addInfo);
        }

        public void remove(int position) {
            listData.remove(position);
            dataChange();
        }

        public void sort() {
            Collections.sort(listData, ShopListData.ALPHA_COMPARATOR);
            dataChange();
        }

        public void dataChange() {
            adapter_food.notifyDataSetChanged();
            adapter_cloth.notifyDataSetChanged();
            adapter_etc.notifyDataSetChanged();
        }

    }
}
