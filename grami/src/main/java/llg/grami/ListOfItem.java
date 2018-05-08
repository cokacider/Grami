package llg.grami;

import java.util.ArrayList;

/**
 * Created by A on 2017-06-01.
 */

public class ListOfItem {

    public ArrayList<Item> items;
    public ArrayList<ClothColor> colors;

    public ListOfItem() // ArrayList 초기화
    {
        items = new ArrayList<Item>();
        colors = new ArrayList<ClothColor>();
        Create_List();
    }

    public Item searchItem(String name) {
        Item searchedItem;
        for(int i = 0; i < items.size() ; i++) {

            if (items.get(i).name.equals(name)) {
                try {
                    searchedItem = (Item)items.get(i).clone();
                    return searchedItem;
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public String searchColor(String name) {
        for(int i = 0; i<colors.size(); i++) {
            if(colors.get(i).name.equals(name)) {
                return colors.get(i).color;
            }
        }
        return "#000000";
    }

    private void Add_Item(String name, int price, int E_Happiness,
                         int E_Helath, int E_Hungry, String category, String imgfile) {
        //리스트에 Item 추가하는 함수.
        Item temp = new Item(name, price, E_Helath, E_Hungry, E_Happiness,category,imgfile);

        items.add(temp);
    }
    private void Add_Color(String name, String color) {
        ClothColor clothColor = new ClothColor();
        clothColor.name = name;
        clothColor.color = color;
        colors.add(clothColor);
    }

    private void Create_List() {
        // 초기에 모든 아이템들을 다 설정한다.
        // 아이템 추가하려면 여기에 추가하세요.
        // (이름, 가격, 행복도, 체력, 포만감, 카테고리, 이미지파일 이름)

        Add_Item("물약", 140, 10, 30 , 0 , "etc", "coin");
        Add_Item("스팀팩", 160, 15, 50, 10, "etc","steampack");
        Add_Item("알약", 130, 10, 25, 0, "etc","circledrug");
        Add_Item("야구공", 80, 12, -5, 0, "etc", "baseball");
        Add_Item("농구공", 100, 15, -5, 0, "etc", "basketball");
        Add_Item("럭비공", 110, 17, -5, 0, "etc", "ruckbyball");
        Add_Item("축구공", 120, 20, -5, 0, "etc", "soccerball");
        Add_Item("연필", 70, 10, 5, 0, "etc", "pencil");

        Add_Item("바나나", 70, 5, 0, 15 , "food", "banana");
        Add_Item("피자", 75, 6, 0, 20 , "food", "pizza");
        Add_Item("라면", 40, 3, 2, 10, "food", "ramyeon");
        Add_Item("딸기", 30, 4, 0, 10, "food", "strawberry");
        Add_Item("햄버거", 60, 0, 0, 12, "food", "hamburger");
        Add_Item("아이스크림", 30, 2, 0, 10, "food","icecream");
        Add_Item("계란후라이", 50, 5, 0, 13, "food","egg");
        Add_Item("오렌지", 40, 3, 0, 10, "food","orange");
        Add_Item("치킨", 80, 8, 0, 22, "food","chicken");
        Add_Item("라즈베리", 60, 2, 0, 10, "food","raspberry");
        Add_Item("수박", 50, 6, 0, 12, "food","watermelon");

        Add_Item("기본 옷", 100, 0, 0, 0, "cloth", "basict");
        Add_Item("파란 옷", 150, 0, 0 , 0, "cloth", "bluetshirt");
        Add_Item("노랑 티셔츠", 150, 0, 0, 0 ,"cloth", "yellowt");
        Add_Item("검은 티셔츠", 150, 0, 0, 0 ,"cloth", "blackt");
        Add_Item("하얀 원피스", 150, 0, 0, 0 ,"cloth", "whitetshirt");
        Add_Item("잔디 옷", 150, 0, 0, 0 ,"cloth", "greensigns");

        // -------------------------------------cloth는 반드시 색도 지정
        Add_Color("기본 옷", "#ffcbf4");
        Add_Color("파란 옷", "#4682b4");
        Add_Color("노랑 티셔츠", "#ffff00");
        Add_Color("검은 티셔츠", "#000000");
        Add_Color("하얀 원피스", "#ffffff");
        Add_Color("잔디 옷", "#98fb98");
    }

    class ClothColor {
        public String name;
        public String color;
    }
}
