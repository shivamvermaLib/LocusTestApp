package com.shivam.locustestapp.data.repository

import android.content.Context
import com.shivam.locustestapp.data.api.LocalAssetApi
import com.shivam.locustestapp.data.model.Post

interface PostRepository {
    suspend fun getPostList(): List<Post>?
}

class PostRepositoryImpl(private val context: Context) : PostRepository {
    override suspend fun getPostList(): List<Post>? {
        try {
            return LocalAssetApi.getListOfJson(context)
        } catch (e: Exception) {
            throw e
        }
    }
}