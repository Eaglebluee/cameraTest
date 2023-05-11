package com.example.core_network.di

import com.example.core_network.datasource.ImageRemoteDataSource
import com.example.core_network.datasource.ImageRemoteDataSourceImpl
import com.example.core_network.datasource.ReportRemoteDataSource
import com.example.core_network.datasource.ReportRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
    @Binds
    abstract fun bindImageDataSource(imageRemoteDataSource: ImageRemoteDataSourceImpl): ImageRemoteDataSource

    @Binds
    abstract fun bindReportDataSource(reportRemoteDataSource: ReportRemoteDataSourceImpl): ReportRemoteDataSource
}