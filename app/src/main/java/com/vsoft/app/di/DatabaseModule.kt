package com.vsoft.app.di

import android.content.Context
import androidx.room.Room
import com.vsoft.app.data.db.AppDatabase
import com.vsoft.app.data.db.MIGRATION_1_2
import com.vsoft.app.data.db.MIGRATION_2_3
import com.vsoft.app.data.db.MIGRATION_3_4
import com.vsoft.app.data.db.MIGRATION_4_5
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDb(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "vsoft.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideTransactionDao(db: AppDatabase) = db.transactionDao()
    @Provides fun provideAccountDao(db: AppDatabase) = db.accountDao()
    @Provides fun provideTransferDao(db: AppDatabase) = db.transferDao()
    @Provides fun provideProjectDao(db: AppDatabase) = db.projectDao()
    @Provides fun provideTaskDao(db: AppDatabase) = db.taskDao()
    @Provides fun provideSubtaskDao(db: AppDatabase) = db.subtaskDao()
    @Provides fun provideJobDao(db: AppDatabase) = db.jobDao()
}
