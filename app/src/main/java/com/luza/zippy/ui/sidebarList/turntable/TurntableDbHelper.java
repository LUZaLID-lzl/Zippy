package com.luza.zippy.ui.sidebarList.turntable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.luza.zippy.setting.ShardPerfenceSetting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TurntableDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "turntable.db";
    private static final int DATABASE_VERSION = 1;

    // 预设表
    private static final String TABLE_PRESUPPOSE = "presuppose";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_OPTIONS = "options";

    private Context mContext;

    // 创建预设表的SQL语句
    private static final String SQL_CREATE_PRESUPPOSE =
            "CREATE TABLE " + TABLE_PRESUPPOSE + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_NAME + " TEXT NOT NULL," +
                    COLUMN_OPTIONS + " TEXT NOT NULL)";

    public TurntableDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PRESUPPOSE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 如果需要升级数据库，可以在这里添加升级逻辑
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRESUPPOSE);
        onCreate(db);
    }

    // 添加预设
    public long addPresuppose(TurntablePresuppose presuppose) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, presuppose.getName());
        values.put(COLUMN_OPTIONS, joinOptions(presuppose.getOptions()));
        return db.insert(TABLE_PRESUPPOSE, null, values);
    }

    // 更新预设
    public int updatePresuppose(long id, TurntablePresuppose presuppose) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, presuppose.getName());
        values.put(COLUMN_OPTIONS, joinOptions(presuppose.getOptions()));
        return db.update(TABLE_PRESUPPOSE, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    // 删除预设
    public void deletePresuppose(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PRESUPPOSE, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    // 获取所有预设
    public List<TurntablePresuppose> getAllPresuppose() {
        List<TurntablePresuppose> presupposeList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRESUPPOSE,
                null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                String optionsStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OPTIONS));
                List<String> options = splitOptions(optionsStr);
                TurntablePresuppose presuppose = new TurntablePresuppose(name, options);
                presuppose.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                presupposeList.add(presuppose);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return presupposeList;
    }

    // 获取单个预设
    public TurntablePresuppose getPresuppose(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRESUPPOSE,
                null,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null);

        TurntablePresuppose presuppose = null;
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
            String optionsStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OPTIONS));
            List<String> options = splitOptions(optionsStr);
            presuppose = new TurntablePresuppose(name, options);
            presuppose.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        }
        cursor.close();
        return presuppose;
    }

    // 将选项列表转换为字符串
    private String joinOptions(List<String> options) {
        return String.join(",", options);
    }

    // 将字符串转换为选项列表
    private List<String> splitOptions(String optionsStr) {
        return new ArrayList<>(Arrays.asList(optionsStr.split(",")));
    }

    // 添加默认数据
    public void insertDefaultData() {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // 检查是否已经有数据
        Cursor cursor = db.query(TABLE_PRESUPPOSE, new String[]{"COUNT(*)"}, 
            null, null, null, null, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        
        // 只有在表为空时才添加默认数据
        if (count == 0) {
            // 添加默认数据：今天吃什么
            ContentValues foodValues = new ContentValues();
            foodValues.put(COLUMN_NAME, "今天吃什么");
            foodValues.put(COLUMN_OPTIONS, "火锅,烤肉,麻辣烫,炒菜,米线,面条,披萨,汉堡,寿司,盖浇饭");
            db.insert(TABLE_PRESUPPOSE, null, foodValues);

            // 添加默认数据：今天玩什么
            ContentValues gameValues = new ContentValues();
            gameValues.put(COLUMN_NAME, "今天玩什么");
            gameValues.put(COLUMN_OPTIONS, "王者荣耀,和平精英,原神,崩坏:星穹铁道,英雄联盟,守望先锋,我的世界,阴阳师,金铲铲之战");
            db.insert(TABLE_PRESUPPOSE, null, gameValues);
        }
    }
} 