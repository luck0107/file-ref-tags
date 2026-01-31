package org.jetbrains.plugins.template.model

import com.google.gson.annotations.SerializedName

/**
 * 引用项数据结构
 */
data class ReferenceItem(
    val id: String,
    val type: ReferenceType,
    val title: String,
    val filePath: String? = null,
    val snippet: String? = null,
    val comment: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val targetFilePath: String? = null,
    val groupId: String? = null
)

/**
 * 引用类型
 */
enum class ReferenceType {
    @SerializedName("file")
    FILE,
    
    @SerializedName("file-snippet")
    FILE_SNIPPET,
    
    @SerializedName("global-snippet")
    GLOBAL_SNIPPET,
    
    @SerializedName("comment")
    COMMENT
}
