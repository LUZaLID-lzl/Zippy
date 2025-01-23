package com.luza.zippy.data;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.luza.zippy.data.dao.ScrummageRecordDao;
import com.luza.zippy.data.database.AppDatabase;
import com.luza.zippy.ui.sidebarList.scrummage.data.entity.ScrummageRecord;

import java.util.List;

public class Repository {
    private ScrummageRecordDao scrummageRecordDao;
    private LiveData<List<ScrummageRecord>> allScrummageRecords;

    public Repository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        scrummageRecordDao = database.scrummageRecordDao();
        allScrummageRecords = scrummageRecordDao.getAllRecords();
    }

    public LiveData<List<ScrummageRecord>> getAllScrummageRecords() {
        return allScrummageRecords;
    }

    public void insertScrummageRecord(ScrummageRecord record) {
        new InsertScrummageRecordAsyncTask(scrummageRecordDao).execute(record);
    }

    public void updateScrummageRecord(ScrummageRecord record) {
        new UpdateScrummageRecordAsyncTask(scrummageRecordDao).execute(record);
    }

    public void deleteScrummageRecord(ScrummageRecord record) {
        new DeleteScrummageRecordAsyncTask(scrummageRecordDao).execute(record);
    }

    private static class InsertScrummageRecordAsyncTask extends AsyncTask<ScrummageRecord, Void, Void> {
        private ScrummageRecordDao scrummageRecordDao;

        private InsertScrummageRecordAsyncTask(ScrummageRecordDao scrummageRecordDao) {
            this.scrummageRecordDao = scrummageRecordDao;
        }

        @Override
        protected Void doInBackground(ScrummageRecord... records) {
            scrummageRecordDao.insert(records[0]);
            return null;
        }
    }

    private static class UpdateScrummageRecordAsyncTask extends AsyncTask<ScrummageRecord, Void, Void> {
        private ScrummageRecordDao scrummageRecordDao;

        private UpdateScrummageRecordAsyncTask(ScrummageRecordDao scrummageRecordDao) {
            this.scrummageRecordDao = scrummageRecordDao;
        }

        @Override
        protected Void doInBackground(ScrummageRecord... records) {
            scrummageRecordDao.update(records[0]);
            return null;
        }
    }

    private static class DeleteScrummageRecordAsyncTask extends AsyncTask<ScrummageRecord, Void, Void> {
        private ScrummageRecordDao scrummageRecordDao;

        private DeleteScrummageRecordAsyncTask(ScrummageRecordDao scrummageRecordDao) {
            this.scrummageRecordDao = scrummageRecordDao;
        }

        @Override
        protected Void doInBackground(ScrummageRecord... records) {
            scrummageRecordDao.delete(records[0]);
            return null;
        }
    }
} 