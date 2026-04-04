package com.example.careai

data class TextContent(val type: String = "text", val text: String)
data class ImageContent(val type: String = "image_url", val image_url: ImageUrl)
data class ImageUrl(val url: String)