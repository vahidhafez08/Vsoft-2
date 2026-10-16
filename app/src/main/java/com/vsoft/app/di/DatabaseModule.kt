package com.vsoft.app.di
import android.content.Context
import androidx.room.Room
import com.vsoft.app.data.db.AppDatabase
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
        Room.databaseBuilder(ctx, AppDatabase::class.java, "vsoft.db").fallbackToDestructiveMigration().build()
    @Provides fun provideTransactionDao(db: AppDatabase) = db.transactionDao()
    @Provides fun provideAccountDao(db: AppDatabase) = db.accountDao()
    @Provides fun provideProjectDao(db: AppDatabase) = db.projectDao()
    @Provides fun provideTaskDao(db: AppDatabase) = db.taskDao()
}
