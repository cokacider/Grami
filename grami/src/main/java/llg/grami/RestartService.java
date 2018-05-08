package llg.grami;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RestartService extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("RestartService", "RestartService called : " + intent.getAction());

        // 서비스 죽일 때 알람으로 다시 서비스 등록
        if(intent.getAction().equals("ACTION.RESTART.FootCountService")) {
            Log.i("RestartService", "ACTION.RESTART.FootCountService");

            Intent i = new Intent(context, FootCountService.class);
            context.startService(i);
        }

        // 휴대폰 실행시 서비스 자동 실행
        if(intent.getAction().equals(intent.ACTION_BOOT_COMPLETED)) {


            Intent i = new Intent(context, FootCountService.class);
            context.startService(i);
        }
    }
}
