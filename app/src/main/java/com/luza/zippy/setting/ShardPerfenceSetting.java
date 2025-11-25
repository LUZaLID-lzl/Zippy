package com.luza.zippy.setting;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class ShardPerfenceSetting {
    private static final String TAG = "ShardPerfenceSetting_settings";
    private static final String PREF_NAME = "zippy_settings";
    private static final String PREF_FAVORITE_MENU_ITEMS = "favorite_menu_items";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static ShardPerfenceSetting instance;
    private Context mContext;

    // 定义多个数据项的键
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_HOMETHEM = "homeTheme";
    private static final String KEY_HOMEANIMATIONNUM = "homeAnimationNum";
    private static final String KEY_ACTIVATE = "activate";
    private static final String KEY_ACTIVATE_NAME = "activeName";
    private static final String KEY_LAUNCH_NUM = "launchNum";
    private static final String KEY_ARRANGE = "arrange";
    private static final String KEY_BOTTOM_MENU_BKG = "bottom_menu_bkg";
    private static final String KEY_WORK_MODE = "work_mode";
    private static final String KEY_GAME_2048_HIGH_SCORE = "game_2048_high_score";
    private static final String KEY_TETRIS_HIGH_SCORE = "tetris_high_score";
    private static final String KEY_BOTTOM_MENU_ITEM_SIZE = "bottom_menu_item_size";
    private static final String KEY_RANDOM_THEME = "random_theme";

    // 实际数据
    private String language;            //en - zh
    private String homeTheme;           //pikachu - bulbasaur - squirtle - mew - karsa - capoo - maple - winter - gengar
    private int homeAnimationNum;       //0 -> 100
    private boolean activate;           //true - false
    private String activeName;
    private int launchNum;              // 0 ->
    private boolean arrange;            //true -> horizontal  false -> vertical
    private boolean bottom_menu_bkg;    //true - false
    private String workMode;            //work - life - all
    private int game2048HighScore;      //2048游戏最高分数
    private int tetrisHighScore;        //俄罗斯方块游戏最高分数
    private int bottomMenuItemSize;     //底部菜单项大小 60-120dp，默认80dp
    private boolean randomTheme;        //随机主题开关

    public boolean isBottom_menu_bkg() {
        return bottom_menu_bkg;
    }

    public void setBottom_menu_bkg(boolean bottom_menu_bkg) {
        this.bottom_menu_bkg = bottom_menu_bkg;
        editor.putBoolean(KEY_BOTTOM_MENU_BKG, bottom_menu_bkg);
        editor.apply();
    }

    public Boolean getArrange() {
        return arrange;
    }

    public void setArrange(Boolean arrange) {
        this.arrange = arrange;
        editor.putBoolean(KEY_ARRANGE, arrange);
        editor.apply();
    }

    public int getLaunchNum() {
        return launchNum;
    }

    public void addLaunchNum() {
        this.launchNum++;
        editor.putInt(KEY_LAUNCH_NUM, launchNum);
        editor.apply();
    }

    public String getActiveName() {
        return activeName;
    }

    public void setActiveName(String activeName) {
        this.activeName = activeName;
        editor.putString(KEY_ACTIVATE_NAME, activeName);
        editor.apply();
    }

    public ShardPerfenceSetting(Context context) {
        //构造函数
        mContext = context;
        sharedPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        //初始化时读取 SharedPreferences 中的数据
        language = sharedPreferences.getString(KEY_LANGUAGE, "zh");
        homeTheme = sharedPreferences.getString(KEY_HOMETHEM, "winter");
        homeAnimationNum = sharedPreferences.getInt(KEY_HOMEANIMATIONNUM, 38);
        activate = sharedPreferences.getBoolean(KEY_ACTIVATE, false);
        activeName = sharedPreferences.getString(KEY_ACTIVATE_NAME, "none");
        launchNum = sharedPreferences.getInt(KEY_LAUNCH_NUM, 0);
        arrange = sharedPreferences.getBoolean(KEY_ARRANGE, true);
        bottom_menu_bkg = sharedPreferences.getBoolean(KEY_BOTTOM_MENU_BKG, true);
        workMode = sharedPreferences.getString(KEY_WORK_MODE, "work");
        game2048HighScore = sharedPreferences.getInt(KEY_GAME_2048_HIGH_SCORE, 0);
        tetrisHighScore = sharedPreferences.getInt(KEY_TETRIS_HIGH_SCORE, 0);
        bottomMenuItemSize = sharedPreferences.getInt(KEY_BOTTOM_MENU_ITEM_SIZE, 80);
        randomTheme = sharedPreferences.getBoolean(KEY_RANDOM_THEME, false);
        logToString();
    }

    public static synchronized ShardPerfenceSetting getInstance(Context context) {
        if (instance == null) {
            instance = new ShardPerfenceSetting(context.getApplicationContext());
        }
        return instance;
    }

    public String getHomeTheme() {
        return homeTheme;
    }

    public void setHomeTheme(String homeTheme) {
        this.homeTheme = homeTheme;
        editor.putString(KEY_HOMETHEM, homeTheme);
        editor.apply();
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
        editor.putString(KEY_LANGUAGE, language);
        editor.apply();
    }

    public int getHomeAnimationNum() {
        return homeAnimationNum;
    }

    public void setHomeAnimationNum(int homeAnimationNum) {
        this.homeAnimationNum = homeAnimationNum;
        editor.putInt(KEY_HOMEANIMATIONNUM, homeAnimationNum);
        editor.apply();
    }

    public Boolean getActivate() {
        return activate;
    }

    public void setActivate(Boolean activate) {
        this.activate = activate;
        editor.putBoolean(KEY_ACTIVATE, activate);
        editor.apply();
    }

    public String getWorkMode() {
        return workMode;
    }

    public void setWorkMode(String workMode) {
        this.workMode = workMode;
        editor.putString(KEY_WORK_MODE, workMode);
        editor.apply();
    }

    public int getGame2048HighScore() {
        return game2048HighScore;
    }

    public void setGame2048HighScore(int game2048HighScore) {
        this.game2048HighScore = game2048HighScore;
        editor.putInt(KEY_GAME_2048_HIGH_SCORE, game2048HighScore);
        editor.apply();
    }

    public int getTetrisHighScore() {
        return tetrisHighScore;
    }

    public void setTetrisHighScore(int tetrisHighScore) {
        this.tetrisHighScore = tetrisHighScore;
        editor.putInt(KEY_TETRIS_HIGH_SCORE, tetrisHighScore);
        editor.apply();
    }

    public int getBottomMenuItemSize() {
        return bottomMenuItemSize;
    }

    public void setBottomMenuItemSize(int bottomMenuItemSize) {
        this.bottomMenuItemSize = bottomMenuItemSize;
        editor.putInt(KEY_BOTTOM_MENU_ITEM_SIZE, bottomMenuItemSize);
        editor.apply();
    }

    public boolean getRandomTheme() {
        return randomTheme;
    }

    public void setRandomTheme(boolean randomTheme) {
        this.randomTheme = randomTheme;
        editor.putBoolean(KEY_RANDOM_THEME, randomTheme);
        editor.apply();
    }

    public void logToString() {
        String log =  "ShardPerfenceSetting{" +
                "language='" + language + '\'' +
                ", homeTheme='" + homeTheme + '\'' +
                ", homeAnimationNum='" + homeAnimationNum + '\'' +
                ", activate='" + activate + '\'' +
                ", acticeName='" + activeName + '\'' +
                ", launchNum='" + launchNum + '\'' +
                ", arrange='" + arrange + '\'' +
                ", bottom_menu_bkg='" + bottom_menu_bkg + '\'' +
                ", workMode='" + workMode + '\'' +
                ", game2048HighScore='" + game2048HighScore + '\'' +
                ", tetrisHighScore='" + tetrisHighScore + '\'' +
                ", bottomMenuItemSize='" + bottomMenuItemSize + '\'' +
                ", randomTheme='" + randomTheme + '\'' +
                '}';
        android.util.Log.d(TAG,log);
    }

    public boolean isMenuItemFavorite(int menuItemId) {
        Set<String> favorites = sharedPreferences.getStringSet(
            PREF_FAVORITE_MENU_ITEMS, 
            new HashSet<>()
        );
        return favorites.contains(String.valueOf(menuItemId));
    }

    public void setMenuItemFavorite(int menuItemId, boolean isFavorite) {
        Set<String> favorites = new HashSet<>(
            sharedPreferences.getStringSet(PREF_FAVORITE_MENU_ITEMS, new HashSet<>())
        );
        
        if (isFavorite) {
            favorites.add(String.valueOf(menuItemId));
        } else {
            favorites.remove(String.valueOf(menuItemId));
        }
        
        sharedPreferences.edit()
            .putStringSet(PREF_FAVORITE_MENU_ITEMS, favorites)
            .apply();
    }
}
