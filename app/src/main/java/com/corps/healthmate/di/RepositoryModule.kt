package com.corps.healthmate.di

import com.corps.healthmate.repository.MedicineRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideMedicineRepository(): MedicineRepository {
        return MedicineRepository()
    }
} 