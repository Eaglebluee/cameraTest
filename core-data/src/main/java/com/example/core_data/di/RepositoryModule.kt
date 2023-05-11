package com.example.core_data.di

import com.example.core_data.repository.ImageRepository
import com.example.core_data.repository.ImageRepositoryImpl
import com.example.core_data.repository.ReportRepository
import com.example.core_data.repository.ReportRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindsImageRepository(imageRepository: ImageRepositoryImpl) : ImageRepository

    @Binds
    abstract fun bindsReportRepository(reportRepository: ReportRepositoryImpl) : ReportRepository
}