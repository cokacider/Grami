package llg.grami;

/**
 * Created by LG on 2017-05-11.
 */

public class ScreenConfig {
    private MainActivity mGameActivity;

    public int mScreenWidth;
    public int mScreenHeight;
    public int mVirtualWidth;
    public int mVirtualHeight;

    public int surfaceViewWidth;
    public int surfaceViewHeight;

    public int gramiX;
    public int gramiY;
    public int gramiWidth = 384;
    public int gramiHeight = 368;
    public int gramiBitmap;
    public int[] gramiBit = new int[11];

    public boolean gramiSad = false;

    public int houseX;
    public int houseY;

    public int capX;
    public int capY;

    public int pantsX;
    public int pantsY;

    public String backgroundColor;

    public int mEyeMove = 0;    // 그라미 눈동자 움직임
    public float mEyeSubMove = 0;
    public boolean gramiMove = false;

    private int savedLevel = 0;

    public boolean moving = false;  // 클릭 시 그라미 움직이게 함
    public boolean levelUpCheck = false;    // 레벨업 했을때
    public boolean eating = false;     // 뭔가 먹었을 때
    public boolean wear = false;    // 옷 착용 할 때
    public int clickX;  // 사용자가 클릭한 x 좌표
    public int clickY;  // 사용자가 클릭한 y 좌표
    public boolean resultX = false;    // 그라미가 클릭한 위치에 근접햇을 경우를 제어
    public boolean resultY = false;

    public int displayTime = 0;    // 아이템 먹었을 때 웃는 모습 디스플레이 하는 간격(?)
    public int effectCount = 0;    // 그라미 커지는 횟수
    public int bigger = 0;     // 레벨 업 할 때 그라미 크기 커지게 함

//    public String[] footCountStr = new String[8];

    public ScreenConfig(int ScreenWidth, int ScreenHeight, MainActivity mGameActivity) {
        mScreenWidth = ScreenWidth;
        mScreenHeight = ScreenHeight;
        this.mGameActivity = mGameActivity;

        init();
    }

    public void setSize(int width, int height) {
        mVirtualWidth = width;
        mVirtualHeight = height;
    }

    // 가로 세로 좌표를 가상 크기 좌표로 입력시 실제 화면의 위치에 매핑
    public int getX(int x) { return (int)( x * mScreenWidth/mVirtualWidth); }
    public int getY(int y) { return (int)( y * mScreenHeight/mVirtualHeight); }

    public void initWH(int width, int height) {
        surfaceViewWidth = width;
        surfaceViewHeight = height;

        // 집 위치
        houseX = surfaceViewWidth - 500;
        houseY = 50;
    }

    public void initGramiPosition() {

        gramiX = mGameActivity.width / 2 - gramiWidth / 2;
        gramiY = mGameActivity.height / 2 - gramiHeight / 2;
    }

    private int delay_callBack = 0;
    // callback grami
    public void callBack() {
        backgroundColor = mGameActivity.mainBindService.backGroundColor;
        delay_callBack++;
        if(delay_callBack >= 200) {
            delay_callBack = 0;
            sadCheck();

        }
        if(mGameActivity.mainBindService.levelUpCheck) {
            if(mGameActivity.isOn) {
                LevelCheck();
            }
        }
    }

    private void LevelCheck() {
        if(bigger >= 0 && bigger <= 10) {
            gramiWidth += 5; gramiHeight += 5;
        }
        else {
            gramiWidth -= 5; gramiHeight -= 5;
        }
        if(bigger == 20) {
            bigger = 0;
            effectCount++;
        }
        bigger++;

        if(effectCount == 3) {
            effectCount = 0;
            mGameActivity.mainBindService.levelUpCheck = false;
        }
    }

    private void sadCheck() {
        if(mGameActivity.mainBindService.getGramiHappiness() < 30) {
            gramiSad = true;
        } else {
            gramiSad = false;
        }
    }

    // callback
    public void UpdateGramiBitmap() {
        if(mGameActivity.mainBindService.getGramiHappiness() == 100) {
            gramiBitmap = R.drawable.smile;
        } else {
            gramiEyeMove();
        }
    }
    // callback
    public void UpdateGramiXY() {
        if (moving) {
            if(gramiMoving()) {
                moving = false;
                resultX = false;
                resultY = false;
            }
        }

    }

    //callback
    public void UpdateCapPantsXY() {
        if(!gramiSad) {
            capX = gramiX - getX(30);
            capY = gramiY - getY(130);
            pantsX = gramiX + getX(10);
            pantsY = gramiY + getY(230);
        } else {
            pantsX = surfaceViewWidth / 2 - 300;
            pantsY = surfaceViewHeight / 2 - 300;
            capX = surfaceViewWidth / 2 + 200;
            capY = surfaceViewHeight / 2;
        }
    }


    private boolean gramiMoving() {

        int diffX = clickX - (gramiX + gramiWidth / 2);
        int diffY = clickY - (gramiY + gramiHeight / 2);

        if(diffX > 0 && Math.abs(diffX) > 5) gramiX += 10;
        else if(diffX < 0 && Math.abs(diffX) > 5) gramiX -= 10;
        else resultX = true;
        if (diffY > 0 && Math.abs(diffY) > 5) gramiY += 10;
        else if (diffY < 0 && Math.abs(diffY) > 5) gramiY -= 10;
        else resultY = true;

        return resultX && resultY;
    }

    private void gramiEyeMove() {
        if(0 <= mEyeMove && mEyeMove <= 10) {
            gramiBitmap = gramiBit[mEyeMove];
        } else {
            gramiBitmap = gramiBit[0];
        }

        mEyeSubMove += 0.2;           // 그라미 눈동자 움직임 속도 조절
        mEyeMove = (int)mEyeSubMove;
        if(mEyeMove == 25) {
            mEyeMove = 0;
            mEyeSubMove = 0;
        }
    }

    private void init() {
        gramiBitmap = R.drawable.character_01;
        gramiBit[0] = R.drawable.character_01;
        gramiBit[1] = R.drawable.character_02;
        gramiBit[2] = R.drawable.character_03;
        gramiBit[3] = R.drawable.character_04;
        gramiBit[4] = R.drawable.character_05;
        gramiBit[5] = R.drawable.character_06;
        gramiBit[6] = R.drawable.character_07;
        gramiBit[7] = R.drawable.character_08;
        gramiBit[8] = R.drawable.character_09;
        gramiBit[9] = R.drawable.character_10;
        gramiBit[10] = R.drawable.character_11;

        backgroundColor = "#ffcbf4";

    }



}
