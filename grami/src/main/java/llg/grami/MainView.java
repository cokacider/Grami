package llg.grami;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by LG on 2017-05-11.
 */

public class MainView extends SurfaceView implements SurfaceHolder.Callback {
    private MainActivity mGameActivity;
    MainThread mMainThread;
    SurfaceHolder mHolder;
    Handler mHandler;
    Context mMainContext;
    boolean mDrawCls = false;
    ScreenConfig mScreenConfig;

    public MainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mMainThread = new MainThread(getHolder(), this);
        setFocusable(true);
        mMainContext = context;
    }

    public void init(int width, int height, MainActivity gameActivity, ScreenConfig screenConfig) {
        this.mGameActivity = gameActivity;
        mScreenConfig = screenConfig;
        mScreenConfig.setSize(width, height);
        mDrawCls = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mDrawCls == false) {
            return;
        }
        // 그리기
        // 범위 : (0, 0) ~ (viewWidth, viewHeight)
        int viewWidth = mGameActivity.width;
        int viewHeight = mGameActivity.height;

        Paint paint = new Paint();

        // 기본 배경 색 #ffcbf4
        canvas.drawColor(Color.parseColor(mScreenConfig.backgroundColor));

        // House
        Bitmap b_house = BitmapFactory.decodeResource(getResources(), R.drawable.house);
        if (mScreenConfig.gramiSad) {
            b_house = BitmapFactory.decodeResource(getResources(), R.drawable.house_sad);
        }
        canvas.drawBitmap(b_house, mScreenConfig.houseX, mScreenConfig.houseY, null);

        // Grami
        mScreenConfig.callBack();
        mScreenConfig.UpdateGramiBitmap();
        mScreenConfig.UpdateGramiXY();
        Bitmap bitmap_grami = BitmapFactory.decodeResource(getResources(), mScreenConfig.gramiBitmap);
        if (!mScreenConfig.gramiSad) {
            if(mScreenConfig.levelUpCheck) {    // 레벨 업 했을 때
                bitmap_grami = Bitmap.createScaledBitmap(bitmap_grami, mScreenConfig.gramiWidth, mScreenConfig.gramiHeight, true);
            }
            canvas.drawBitmap(bitmap_grami, mScreenConfig.gramiX, mScreenConfig.gramiY, null);
        }
        mScreenConfig.UpdateCapPantsXY();
        //바지랑 캡
        if (mScreenConfig.wear) {
            Bitmap capGrami = BitmapFactory.decodeResource(getResources(), R.drawable.cap);
            Bitmap pants = BitmapFactory.decodeResource(getResources(), R.drawable.yellow_pants);
            canvas.drawBitmap(capGrami, mScreenConfig.capX, mScreenConfig.capY, null);
            canvas.drawBitmap(pants, mScreenConfig.pantsX, mScreenConfig.pantsY, null);
        }


        mGameActivity.statusCallback.recvData();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int keyAction = event.getAction();
        mScreenConfig.clickX = (int)event.getX();
        mScreenConfig.clickY = (int)event.getY();

        switch (keyAction) {
            case MotionEvent.ACTION_UP:
                mScreenConfig.gramiMove = false;
                break;
            case MotionEvent.ACTION_DOWN:
                mScreenConfig.moving = true;

                break;
        }
        return true;
    }

    // 현재 이미지 위에 마우스가 위치하는지 판단한다.
    private void checkImageMove(int x, int y) {
        if((mScreenConfig.gramiX < x) && (x < mScreenConfig.gramiX + mScreenConfig.gramiWidth)) {
            if((mScreenConfig.gramiY < y) && (y < mScreenConfig.gramiY + mScreenConfig.gramiHeight)) {
                mScreenConfig.gramiMove = true;
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mMainThread.setRunning(true);
        try {
            if(mMainThread.getState() == Thread.State.TERMINATED) {
                mMainThread = new MainThread(getHolder(), this);
                mMainThread.setRunning(true);
                setFocusable(true);
                mMainThread.start();
            } else {
                mMainThread.start();
            }
        } catch (Exception ex) {
            Log.i("MainView", "ex:" + ex.toString());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        mMainThread.setRunning(false);
        while(retry) {
            try {
                mMainThread.join();
                retry = false;
            } catch(Exception ex) {
                Log.i("MainView", "surfaceDestroyed ex" + ex.toString());
            }
        }
    }
}
