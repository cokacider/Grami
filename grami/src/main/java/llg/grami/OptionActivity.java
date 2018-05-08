package llg.grami;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.Nullable;
import android.view.WindowManager;

/**
 * Created by LG on 2017-06-21.
 */

public class OptionActivity extends PreferenceActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new OptionFragment()).commit();
    }

}
