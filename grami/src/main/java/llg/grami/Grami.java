package llg.grami;

/**
 * Created by A on 2017-04-21.
 */

public class Grami {

    public Grami() {
        Level = 1;
        Health = Hungry = Happiness = 100;
        exp = 0;
    }

    public void insert_data(int _Level, int _Health,
               int _Hungry, int _Happiness
            , int _exp
                            ) {
        this.Level = _Level;
        this.Health = _Health;
        this.Hungry = _Hungry;
        this.Happiness = _Happiness;
        this.exp = _exp;
    }

    public int Level;
    public int Health;
    public int Hungry;
    public int Happiness;
    public int exp;

    public String cap;
    public String pants;
}
