package llg.grami;

/**
 * Created by LG on 2017-06-21.
 */

public class LevelTable {
    public final static int MAX_LEVEL = 100;
    public final int [] levelNeedEXP = new int[MAX_LEVEL + 1];

    public LevelTable() {
        calculateNeedEXP();
    }

    private void calculateNeedEXP() {
        levelNeedEXP[0] = -1;
        for (int i = 1 ; i < MAX_LEVEL + 1 ; i++) {
            levelNeedEXP[i] = (50 * i * i) + (350 * i) - 400;   // i level이 되기 위해서 필요한 exp
        }
    }

}
