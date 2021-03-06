package com.melon.myapp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.melon.myapp.bean.Note;
import com.melon.myapp.bean.Notify;

import java.sql.SQLException;

/**
 * Created by admin on 2017/5/10.
 * Email 530274554@qq.com
 */

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    // 数据库名称
    private static final String DATABASE_NAME = "HelloOrmlite.db";
    // 数据库version
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // 可以用配置文件来生成 数据表，有点繁琐，不喜欢用
        // super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
    }

    public DatabaseHelper(Context context, String databaseName, SQLiteDatabase.CursorFactory factory, int databaseVersion) {
        super(context, databaseName, factory, databaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            //建立表
            TableUtils.createTable(connectionSource, Note.class);
            TableUtils.createTable(connectionSource, Notify.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //创建笔记dao
    public RuntimeExceptionDao<Note, Integer> getNoteDao() {
        return getRuntimeExceptionDao(Note.class);
    }
    //创建通知dao
    public RuntimeExceptionDao<Notify, Integer> getNotifyDao() {
        return getRuntimeExceptionDao(Notify.class);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        //TableUtils.dropTable(connectionSource, Note.class, true);
    }
}
