package llg.grami;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by A on 2017-04-21.
 */

public class User {

    public int Money;
    public ArrayList<Item> myItems;
    public ArrayList<Integer> numMyItem = new ArrayList<>();

    public User() {
        Money = 0;
        myItems = new ArrayList<Item>();
    }

    public void insert_item(String name, int number, ListOfItem listOfItem )
    { // 유저가 가지고 있는 아이템을 추가한다.
        try {
            Item newItem = listOfItem.searchItem(name);
            Log.d("User", "insert_item " + newItem.name + " " + number);

            myItems.add(newItem);
            numMyItem.add(number);
        } catch (NullPointerException e) {
            Log.e("User Item", "해당하는 아이템을 찾지 못함");
        }
    }

    public int searchItem(String name) {
        // 찾는 물건의 arraylist index를 리턴함
        for(int i = 0 ; i < myItems.size(); i++) {
            if(myItems.get(i).name.equals(name)) {
                return i;
            }
        }
        return -1;  // 찾는 물건이 user 한테 없음
    }

    public void removeItem(int index) {
        myItems.remove(index);
        numMyItem.remove(index);
    }

}
