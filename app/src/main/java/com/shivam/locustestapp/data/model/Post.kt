package com.shivam.locustestapp.data.model

import java.io.File

data class Post(
    val type: PostType,
    val id: String,
    val title: String,
    val dataMap: DataMap,
    val selectedOption: String? = null,
    val allowComment:Boolean = false,
    val comment: String? = null,
    val image: File? = null
)

data class DataMap(val options: List<String>)
