package llg.grami;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * Created by LG on 2017-05-11.
 */

public class MainThread extends Thread {
    private SurfaceHolder surfaceHolder;
    private MainView mainView;
    private boolean running = false;

    public MainThread(SurfaceHolder surfaceHolder, MainView mainView) {
        this.surfaceHolder = surfaceHolder;
        this.mainView = mainView;
    }

    public SurfaceHolder getSurfaceHolder() { return surfaceHolder; }
    public void setRunning(boolean run) { running = run; }

    @Override
    public void run() {
        Log.i("mainThread", "run called:" + running);
        try {
            Canvas c;
            while(running) {
                c = null;
                try {
                    c = surfaceHolder.lockCanvas(null);
                    synchronized (surfaceHolder) {
                        try {
                            mainView.onDraw(c);
                            Thread.sleep(2);
                        } catch (Exception exTemp) {
                            Log.e("log", exTemp.toString());
                        }
                    }
                } finally {
                    if( c!=null ) {
                        surfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        } catch (Exception exTot) {
            Log.e("log", exTot.toString());
        }
    }
}
