package llg.grami;

import android.graphics.drawable.Drawable;

import java.text.Collator;
import java.util.Comparator;

/**
 * Created by LG on 2017-06-01.
 */

public class ShopListData {
    public Drawable image;
    public String name;
    public int price;
    public String effect;

    public static final Comparator<ShopListData> ALPHA_COMPARATOR = new Comparator<ShopListData>() {
        private final Collator collator = Collator.getInstance();

        @Override
        public int compare(ShopListData o1, ShopListData o2) {
            return collator.compare(o1.name, o2.name);
        }
    };
}
