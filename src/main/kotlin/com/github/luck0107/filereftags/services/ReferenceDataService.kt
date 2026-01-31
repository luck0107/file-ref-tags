package com.github.luck0107.filereftags.services

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.github.luck0107.filereftags.MyBundle
import com.github.luck0107.filereftags.model.ReferenceGroup
import com.github.luck0107.filereftags.model.ReferenceItem
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import java.time.Instant

/**
 * 引用数据管理服务
 * 负责引用数据的加载、保存和管理
 * 每个项目有独立的引用数据存储
 */
@Service(Service.Level.PROJECT)
class ReferenceDataService(private val project: Project) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val baseDir: File
    private val projectsMapFile: File
    private val storagePath: String
    private val storageFile: File

    init {
        // 获取基础目录：用户主目录下的 .file-ref-tags
        val userHome = System.getProperty("user.home")
        baseDir = Paths.get(userHome, ".file-ref-tags").toFile()
        Files.createDirectories(baseDir.toPath())
        
        // 项目映射文件
        projectsMapFile = File(baseDir, "projects.json")
        
        // 根据项目路径获取或创建项目特定的存储路径
        val projectStorageDir = getOrCreateProjectStorageDir()
        storagePath = File(projectStorageDir, "references.json").absolutePath
        storageFile = File(storagePath)
        
        // 延迟加载：不在初始化时立即加载数据，而是在需要时调用 loadReferences()
        // loadReferences() 将在工具窗口显示时或首次访问数据时调用
    }

    private var references: MutableList<ReferenceItem> = mutableListOf()
    private var groups: MutableList<ReferenceGroup> = mutableListOf()

    /**
     * 获取或创建项目特定的存储目录
     * 使用项目路径的哈希值作为文件夹名，避免路径中的特殊字符问题
     */
    private fun getOrCreateProjectStorageDir(): File {
        val projectPath = project.basePath
        
        if (projectPath == null || projectPath.isBlank()) {
            // 如果没有项目路径，使用默认文件夹
            val defaultDir = File(baseDir, "default")
            Files.createDirectories(defaultDir.toPath())
            return defaultDir
        }

        // 规范化项目路径（统一使用正斜杠，并转换为小写）
        val normalizedPath = projectPath.replace("\\", "/").lowercase()
        
        // 读取项目映射
        val projectsMap = loadProjectsMap()
        
        // 查找是否已有映射
        val existingDirName = projectsMap[normalizedPath]
        if (existingDirName != null) {
            val existingDir = File(baseDir, existingDirName)
            if (existingDir.exists()) {
                return existingDir
            } else {
                // 映射存在但文件夹不存在，删除映射
                projectsMap.remove(normalizedPath)
            }
        }

        // 创建新的项目文件夹（使用路径的哈希值）
        val dirName = hashPath(normalizedPath)
        val projectDir = File(baseDir, dirName)
        Files.createDirectories(projectDir.toPath())
        
        // 更新映射
        projectsMap[normalizedPath] = dirName
        saveProjectsMap(projectsMap)
        
        return projectDir
    }

    /**
     * 对路径进行哈希，生成文件夹名
     */
    private fun hashPath(path: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val hashBytes = digest.digest(path.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * 加载项目映射文件
     */
    private fun loadProjectsMap(): MutableMap<String, String> {
        try {
            if (projectsMapFile.exists() && projectsMapFile.length() > 0) {
                val json = projectsMapFile.readText()
                if (json.isNotBlank()) {
                    val mapType = object : TypeToken<Map<String, String>>() {}.type
                    val map = gson.fromJson<Map<String, String>>(json, mapType)
                    return map?.toMutableMap() ?: mutableMapOf()
                }
            }
        } catch (e: Exception) {
            // 忽略错误，创建新的映射
        }
        return mutableMapOf()
    }

    /**
     * 保存项目映射文件
     */
    private fun saveProjectsMap(map: Map<String, String>) {
        try {
            val json = gson.toJson(map)
            val tempFile = File(projectsMapFile.parent, "${projectsMapFile.name}.tmp")
            java.io.FileOutputStream(tempFile).use { fos ->
                fos.write(json.toByteArray(Charsets.UTF_8))
                fos.fd.sync()
            }
            if (projectsMapFile.exists()) {
                projectsMapFile.delete()
            }
            tempFile.renameTo(projectsMapFile)
        } catch (e: Exception) {
            // 忽略保存错误
        }
    }

    /**
     * 加载引用数据
     * 在服务初始化时自动调用，确保插件启动时就能读取到存储的数据
     */
    private fun loadReferences() {
        try {
            if (storageFile.exists() && storageFile.length() > 0) {
                val json = storageFile.readText()
                
                if (json.isNotBlank()) {
                    try {
                        // 尝试解析为新格式（对象格式：{references: [], groups: []}）
                        val data = gson.fromJson(json, ReferenceData::class.java)
                        if (data.references != null && data.groups != null) {
                            // 新格式
                            references = data.references.toMutableList()
                            groups = data.groups.toMutableList()
                        } else {
                            // 数据格式不完整，初始化为空
                            references = mutableListOf()
                            groups = mutableListOf()
                        }
                    } catch (e: JsonSyntaxException) {
                        // 打印错误信息以便调试
                        e.printStackTrace()
                        // 如果解析失败，尝试向后兼容：可能是旧的数组格式
                        try {
                            val oldFormatArray = gson.fromJson(json, Array<ReferenceItem>::class.java)
                            references = oldFormatArray.toMutableList()
                            groups = mutableListOf()
                            // 自动迁移到新格式
                            saveReferences()
                        } catch (e2: Exception) {
                            // 打印错误信息以便调试
                            e2.printStackTrace()
                            references = mutableListOf()
                            groups = mutableListOf()
                        }
                    }
                } else {
                    references = mutableListOf()
                    groups = mutableListOf()
                }
            } else {
                references = mutableListOf()
                groups = mutableListOf()
                // 创建空文件
                saveReferences()
            }
        } catch (e: Exception) {
            // 打印错误信息以便调试
            e.printStackTrace()
            references = mutableListOf()
            groups = mutableListOf()
        }
    }

    /**
     * 保存引用数据
     */
    private fun saveReferences() {
        try {
            val data = ReferenceData(references, groups)
            val json = gson.toJson(data)
            // 确保目录存在
            storageFile.parentFile?.mkdirs()
            // 使用原子写入，先写入临时文件再重命名，确保数据完整性
            val tempFile = File(storageFile.parent, "${storageFile.name}.tmp")
            // 写入临时文件并强制刷新到磁盘
            java.io.FileOutputStream(tempFile).use { fos ->
                fos.write(json.toByteArray(Charsets.UTF_8))
                fos.fd.sync() // 强制刷新到磁盘
            }
            // 原子性替换：先删除旧文件（如果存在），再重命名临时文件
            if (storageFile.exists()) {
                storageFile.delete()
            }
            tempFile.renameTo(storageFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 重新加载引用数据（从存储文件）
     */
    fun reloadReferences() {
        // 清空当前数据，强制重新加载
        references.clear()
        groups.clear()
        loadReferences()
    }
    
    /**
     * 初始化并加载数据（在工具窗口显示时调用）
     */
    fun initialize() {
        loadReferences()
    }

    /**
     * 确保数据已加载（延迟加载）
     */
    private fun ensureLoaded() {
        if (references.isEmpty() && groups.isEmpty() && storageFile.exists()) {
            loadReferences()
        }
    }

    /**
     * 获取所有引用项
     */
    fun getReferences(): List<ReferenceItem> {
        ensureLoaded()
        return references.toList()
    }

    /**
     * 获取所有分组
     */
    fun getGroups(): List<ReferenceGroup> {
        ensureLoaded()
        return groups.toList()
    }

    /**
     * 添加引用项
     */
    fun addReference(reference: ReferenceItem): ReferenceItem {
        references.add(reference)
        saveReferences()
        return reference
    }

    /**
     * 删除引用项
     */
    fun deleteReference(id: String) {
        references.removeAll { it.id == id }
        saveReferences()
    }

    /**
     * 更新引用项标题
     */
    fun updateReferenceTitle(id: String, title: String) {
        val index = references.indexOfFirst { it.id == id }
        if (index >= 0) {
            val ref = references[index]
            references[index] = ref.copy(title = title, updatedAt = Instant.now().toString())
            saveReferences()
        }
    }

    /**
     * 更新引用项顺序
     */
    fun updateOrder(newOrder: List<String>) {
        val ordered = newOrder.mapNotNull { id -> references.find { it.id == id } }
        val remaining = references.filterNot { it.id in newOrder }
        references = (ordered + remaining).toMutableList()
        saveReferences()
    }

    /**
     * 更新引用项的目标文件路径
     */
    fun updateReferenceTargetFilePath(id: String, targetFilePath: String) {
        val index = references.indexOfFirst { it.id == id }
        if (index >= 0) {
            val ref = references[index]
            references[index] = ref.copy(targetFilePath = targetFilePath, updatedAt = Instant.now().toString())
            saveReferences()
        }
    }

    /**
     * 更新引用项的分组
     */
    fun updateReferenceGroup(id: String, groupId: String?) {
        val index = references.indexOfFirst { it.id == id }
        if (index >= 0) {
            val ref = references[index]
            references[index] = ref.copy(groupId = groupId, updatedAt = Instant.now().toString())
            saveReferences()
        }
    }

    /**
     * 添加分组
     */
    fun addGroup(group: ReferenceGroup): ReferenceGroup {
        groups.add(group)
        saveReferences()
        return group
    }

    /**
     * 删除分组
     */
    fun deleteGroup(id: String) {
        // 移除引用项中对已删除组的引用
        references = references.map { ref ->
            if (ref.groupId == id) {
                ref.copy(groupId = null, updatedAt = Instant.now().toString())
            } else {
                ref
            }
        }.toMutableList()
        groups.removeAll { it.id == id }
        saveReferences()
    }

    /**
     * 更新分组顺序
     */
    fun updateGroupOrder(newOrder: List<String>) {
        val ordered = newOrder.mapNotNull { id -> groups.find { it.id == id } }
        val remaining = groups.filterNot { it.id in newOrder }
        groups = (ordered + remaining).toMutableList()
        saveReferences()
    }

    /**
     * 获取存储路径
     */
    fun getStoragePath(): String = storagePath

    /**
     * 数据类：用于序列化/反序列化
     */
    private data class ReferenceData(
        val references: List<ReferenceItem>?,
        val groups: List<ReferenceGroup>?
    )
}
