package llg.grami;

import android.graphics.drawable.Drawable;

import java.text.Collator;
import java.util.Comparator;

/**
 * Created by LG on 2017-06-01.
 */

public class InventoryListData {
    public Drawable image;
    public String name;
    public int num;
    public String effect;

    public static final Comparator<InventoryListData> ALPHA_COMPARATOR = new Comparator<InventoryListData>() {
        private final Collator collator = Collator.getInstance();

        @Override
        public int compare(InventoryListData o1, InventoryListData o2) {
            return collator.compare(o1.name, o2.name);
        }
    };
}
