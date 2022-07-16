package com.shivam.locustestapp.data.api

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.shivam.locustestapp.data.model.Post
import java.io.IOException
import java.io.InputStream


object LocalAssetApi {
    @Throws(IOException::class)
    private fun loadJSONFromAsset(context: Context): String {
        val `is`: InputStream = context.assets.open("posts.json")
        val size: Int = `is`.available()
        val buffer = ByteArray(size)
        `is`.read(buffer)
        `is`.close()
        return String(buffer)
    }

    @Throws(IOException::class, JsonSyntaxException::class)
    fun getListOfJson(context: Context): List<Post>? {
        val gson = Gson()
        return gson.fromJson(loadJSONFromAsset(context), object : TypeToken<List<Post>>() {}.type)
    }
}