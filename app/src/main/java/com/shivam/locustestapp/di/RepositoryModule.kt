package com.shivam.locustestapp.di

import android.content.Context
import com.shivam.locustestapp.data.repository.PostRepository
import com.shivam.locustestapp.data.repository.PostRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    fun provideRepositoryModule(
        @ApplicationContext context: Context
    ): PostRepository {
        return PostRepositoryImpl(context)
    }
}