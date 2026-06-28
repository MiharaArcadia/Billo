package com.mihara.billio.di

import android.content.Context
import androidx.room.Room
import com.mihara.billio.data.db.BillioDatabase
import com.mihara.billio.data.db.dao.ClientDao
import com.mihara.billio.data.db.dao.CounterDao
import com.mihara.billio.data.db.dao.InvoiceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BillioDatabase =
        Room.databaseBuilder(context, BillioDatabase::class.java, BillioDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideClientDao(db: BillioDatabase): ClientDao = db.clientDao()

    @Provides
    fun provideInvoiceDao(db: BillioDatabase): InvoiceDao = db.invoiceDao()

    @Provides
    fun provideCounterDao(db: BillioDatabase): CounterDao = db.counterDao()
}
