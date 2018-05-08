package llg.grami;

/**
 * Created by A on 2017-04-23.
 */

public class Item implements Cloneable{
    public String name;
    public int price;
    public int E_Happniess;
    public int E_Strength;
    public int E_Hungry;
    public String category;
    public String imgFile;

    public Item() {

    }

    public Item(String name, int price, int E_Strength, int E_Hungry,int E_Happiness,  String category, String imgFile) {
        this.name =name;
        this.price = price;
        this.E_Happniess = E_Happiness;
        this.E_Strength = E_Strength;
        this.E_Hungry = E_Hungry;
        this.category = category;   // "cloth", "etc", "food"
        this.imgFile = imgFile;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}