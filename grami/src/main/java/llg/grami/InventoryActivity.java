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
import android.util.Log;
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

public class InventoryActivity extends AppCompatActivity {
    private boolean isSet = false;

    // 연결 타입 서비스
    private MainBindService mainBindService;
    private boolean mainbound = false;  // 서비스 연결 여부

    private ListView listView_food = null;
    private InventoryAdapter adapter_food = null;

    private ListView listView_cloth = null;
    private InventoryAdapter adapter_cloth = null;

    private ListView listView_etc = null;
    private InventoryAdapter adapter_etc = null;

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
        setContentView(R.layout.activity_inventory);
        // 상태바 없애기
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        listView_food = (ListView)findViewById(R.id.inventory_list_food);
        listView_cloth = (ListView)findViewById(R.id.inventory_list_cloth);
        listView_etc = (ListView)findViewById(R.id.inventory_list_etc);

        adapter_food = new InventoryAdapter(this);
        listView_food.setAdapter(adapter_food);

        adapter_cloth = new InventoryAdapter(this);
        listView_cloth.setAdapter(adapter_cloth);

        adapter_etc = new InventoryAdapter(this);
        listView_etc.setAdapter(adapter_etc);



        listView_food.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InventoryListData data = adapter_food.listData.get(position);
                makeDialog(data,adapter_food, position);
            }
        });

        listView_cloth.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InventoryListData data = adapter_cloth.listData.get(position);
                makeDialog(data,adapter_cloth, position);
            }
        });
        listView_etc.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InventoryListData data = adapter_etc.listData.get(position);
                makeDialog(data,adapter_etc, position);
            }
        });

        setResult(RESULT_OK);
    }

    private void makeDialog(final InventoryListData data, final InventoryAdapter adapter, final int position) {
        final Dialog buyDialog = new Dialog(InventoryActivity.this);
        buyDialog.setContentView(R.layout.inventory_usedialog);
        buyDialog.setTitle("아이템 사용하기");

        ImageView image = (ImageView) buyDialog.findViewById(R.id.inventoryusedialog_image);
        TextView name = (TextView) buyDialog.findViewById(R.id.inventoryusedialog_name);
        TextView num = (TextView) buyDialog.findViewById(R.id.inventoryusedialog_num);
        TextView effect = (TextView) buyDialog.findViewById(R.id.inventoryusedialog_effect);

        image.setImageDrawable(data.image);
        name.setText(data.name);
        num.setText(Integer.toString(data.num) + " " + getText(R.string.num) );
        effect.setText(data.effect);

        Button btnUse = (Button) buyDialog.findViewById(R.id.inventoryusedialog_buy);
        Button btnCancel = (Button) buyDialog.findViewById(R.id.inventoryusedialog_cancel);

        btnUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mainBindService.useSomething(data.name)) {
                    if (data.num == 1) {
                        adapter.listData.remove(position);
                        adapter.notifyDataSetChanged();
                    } else {
                        data.num -= 1;
                        adapter.listData.set(position, data);
                        adapter.notifyDataSetChanged();
                    }
                }
                buyDialog.dismiss();

                AlertDialog.Builder alert = new AlertDialog.Builder(InventoryActivity.this);
                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();     //닫기
                    }
                });
                alert.setMessage(data.name + "을 사용했다!!!");
                alert.show();

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

    private void setInventoryItems() {
        Log.d("InventoryAcitivity", "setInven");


        User user = mainBindService.getUser();

        for(int i = 0; i < user.myItems.size();i ++) {
            Item item = user.myItems.get(i);
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
                        user.numMyItem.get(i),
                        effect);
            } else if (item.category.equals("cloth")) {
                adapter_cloth.addItem(getResources().getDrawable(getResources().getIdentifier(imgresName, "drawable", imgpackName)),
                        item.name,
                        user.numMyItem.get(i),
                        effect);
            } else if (item.category.equals("etc")) {
                adapter_etc.addItem(getResources().getDrawable(getResources().getIdentifier(imgresName, "drawable", imgpackName)),
                        item.name,
                        user.numMyItem.get(i),
                        effect);
            }
        }
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

            if(!isSet) {
                isSet = true;
                setInventoryItems();

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

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.inventory_navigation_food:
                    if(listView_cloth.getVisibility() == View.VISIBLE) listView_cloth.setVisibility(View.GONE);
                    else if(listView_etc.getVisibility() == View.VISIBLE) listView_etc.setVisibility(View.GONE);

                    if(listView_food.getVisibility() != View.VISIBLE) listView_food.setVisibility(View.VISIBLE);
                    return true;
                case R.id.inventory_navigation_cloth:
                    if(listView_food.getVisibility() == View.VISIBLE) listView_food.setVisibility(View.GONE);
                    else if(listView_etc.getVisibility() == View.VISIBLE) listView_etc.setVisibility(View.GONE);

                    if(listView_cloth.getVisibility() != View.VISIBLE) listView_cloth.setVisibility(View.VISIBLE);
                    return true;
                case R.id.inventory_navigation_etc:
                    if(listView_cloth.getVisibility() == View.VISIBLE) listView_cloth.setVisibility(View.GONE);
                    else if(listView_food.getVisibility() == View.VISIBLE) listView_food.setVisibility(View.GONE);

                    if(listView_etc.getVisibility() != View.VISIBLE) listView_etc.setVisibility(View.VISIBLE);
                    return true;
            }
            return false;
        }

    };

    private class ViewHolder {
        public ImageView image;
        public TextView name;
        public TextView num;
        public TextView effect;
    }

    public class InventoryAdapter extends BaseAdapter{
        private Context context = null;
        public ArrayList<InventoryListData> listData = new ArrayList<InventoryListData>();

        public InventoryAdapter(Context context) {
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
                convertView = inflater.inflate(R.layout.inventory_listitem, null);

                holder.image = (ImageView) convertView.findViewById(R.id.inventorylistitem_image);
                holder.name = (TextView) convertView.findViewById(R.id.inventorylistitem_name);
                holder.num = (TextView)convertView.findViewById(R.id.inventorylistitem_num);
                holder.effect = (TextView)convertView.findViewById(R.id.inventorylistitem_effect);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            InventoryListData data = listData.get(position);

            if(data.image != null) {
                holder.image.setVisibility(View.VISIBLE);
                holder.image.setImageDrawable(data.image);
            } else {
                holder.image.setVisibility(View.INVISIBLE); // GONE
            }

            holder.name.setText(data.name);
            holder.num.setText(Integer.toString(data.num) + " " + getText(R.string.num));
            holder.effect.setText(data.effect);

            return convertView;
        }

        public void addItem(Drawable image, String name, int num, String effect) {
            InventoryListData addInfo;
            addInfo = new InventoryListData();
            addInfo.image = image;
            addInfo.name = name;
            addInfo.num = num;
            addInfo.effect = effect;

            listData.add(addInfo);
        }

        public void remove(int position) {
            listData.remove(position);
            dataChange();
        }

        public void sort() {
            Collections.sort(listData, InventoryListData.ALPHA_COMPARATOR);
            dataChange();
        }

        public void dataChange() {
            adapter_food.notifyDataSetChanged();
            adapter_cloth.notifyDataSetChanged();
            adapter_etc.notifyDataSetChanged();
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                finish();
                break;
        }
    }

}
