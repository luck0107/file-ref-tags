package com.github.luck0107.filereftags.ui

import com.intellij.icons.AllIcons
import com.intellij.ide.actions.RevealFileAction
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.io.File
import com.github.luck0107.filereftags.MyBundle
import com.github.luck0107.filereftags.actions.DeleteReferenceAction
import com.github.luck0107.filereftags.actions.EditReferenceTitleAction
import com.github.luck0107.filereftags.model.ReferenceGroup
import com.github.luck0107.filereftags.model.ReferenceItem
import com.github.luck0107.filereftags.model.ReferenceType
import com.github.luck0107.filereftags.services.ReferenceDataService
import com.github.luck0107.filereftags.utils.ReferenceNavigationUtils
import java.awt.BorderLayout
import java.awt.Component
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.DefaultListModel
import javax.swing.DropMode
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.TransferHandler

/**
 * 引用列表面板
 */
class ReferenceListPanel(private val project: Project) : SimpleToolWindowPanel(false, true) {

    // 特殊标记对象，用于在列表中显示按钮
    private object StorageButtonMarker

    private val dataService = project.service<ReferenceDataService>()
    private val listModel = DefaultListModel<Any>()
    private val list = JBList<Any>(listModel)
    private val emptyStateLabel = createEmptyStateLabel()
    private val showStorageButton = createShowStorageButton()
    private val collapsedGroups = mutableSetOf<String>() // 记录折叠的分组ID

    init {
        setupUI()
        // 使用 invokeLater 确保服务初始化完成后再加载数据
        com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
            loadReferences()
        }
    }

    private fun setupUI() {
        // 设置列表渲染器
        list.cellRenderer = ReferenceListCellRenderer()
        
        // 设置列表选择模式
        list.selectionMode = javax.swing.ListSelectionModel.SINGLE_SELECTION
        
        // 启用拖拽排序
        list.dragEnabled = true
        list.dropMode = DropMode.INSERT
        list.transferHandler = ReferenceListTransferHandler()
        
        // 添加鼠标事件处理（区分点击和拖拽）
        var mousePressedPoint: java.awt.Point? = null
        var isDragging = false
        
        list.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                mousePressedPoint = e.point
                isDragging = false
                if (e.isPopupTrigger) {
                    showContextMenu(e)
                }
            }

            override fun mouseReleased(e: MouseEvent) {
                if (e.isPopupTrigger) {
                    showContextMenu(e)
                    return
                }
                
                // 只有在非拖拽的情况下才处理点击
                if (!isDragging && mousePressedPoint != null) {
                    val distance = mousePressedPoint!!.distance(e.point.x.toDouble(), e.point.y.toDouble())
                    // 如果移动距离小于 5 像素，认为是点击而不是拖拽
                    if (distance < 5) {
                        val index = list.locationToIndex(e.point)
                        if (index >= 0) {
                            val selectedValue = list.model.getElementAt(index)
                            when (selectedValue) {
                                is ReferenceItem -> {
                                    handleReferenceClick(selectedValue)
                                    // 延迟清除选择状态，避免按钮一直处于按下状态
                                    com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
                                        list.clearSelection()
                                    }
                                }
                                is ReferenceGroup -> {
                                    handleGroupClick(selectedValue)
                                    // 清除选择状态
                                    list.clearSelection()
                                }
                                StorageButtonMarker -> {
                                    // 检查点击位置是否真的在按钮组件上
                                    val cellBounds = list.getCellBounds(index, index)
                                    if (cellBounds != null && cellBounds.contains(e.point)) {
                                        // 按钮面板有 8px 的 border，按钮在面板中央
                                        // 将点击位置转换为按钮面板内的相对坐标
                                        val buttonPanelPoint = java.awt.Point(
                                            e.point.x - cellBounds.x,
                                            e.point.y - cellBounds.y
                                        )
                                        // 按钮面板的 border 是 8px（使用 JBUI.scale 确保在不同DPI下正确）
                                        val borderSize = JBUI.scale(8)
                                        val buttonX = borderSize
                                        val buttonY = borderSize
                                        val buttonWidth = cellBounds.width - borderSize * 2
                                        val buttonHeight = showStorageButton.preferredSize.height
                                        
                                        // 检查点击位置是否在按钮的实际区域内
                                        if (buttonPanelPoint.x >= buttonX && 
                                            buttonPanelPoint.x <= buttonX + buttonWidth &&
                                            buttonPanelPoint.y >= buttonY && 
                                            buttonPanelPoint.y <= buttonY + buttonHeight) {
                                            // 触发按钮点击
                                            showStorageButton.doClick()
                                        }
                                    }
                                    list.clearSelection()
                                }
                            }
                        }
                    }
                }
                mousePressedPoint = null
                isDragging = false
            }

            override fun mouseClicked(e: MouseEvent) {
                if (e.isPopupTrigger || (e.button == MouseEvent.BUTTON3 && e.clickCount == 1)) {
                    showContextMenu(e)
                }
            }
        })
        
        // 添加鼠标移动监听，检测拖拽
        list.addMouseMotionListener(object : java.awt.event.MouseMotionAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                isDragging = true
            }
        })

        // 创建工具栏
        val actionManager = ActionManager.getInstance()
        var actionGroup = actionManager.getAction("FileRefTags.ToolWindowToolbar") as? DefaultActionGroup
        
        // 如果组不存在，手动创建并添加动作
        if (actionGroup == null) {
            actionGroup = DefaultActionGroup().apply {
                val refreshAction = actionManager.getAction("FileRefTags.RefreshReferences")
                val addGroupAction = actionManager.getAction("FileRefTags.AddGroup")
                val showStorageAction = actionManager.getAction("FileRefTags.ShowStorageLocation")
                if (refreshAction != null) add(refreshAction)
                addSeparator()
                if (addGroupAction != null) add(addGroupAction)
                if (showStorageAction != null) add(showStorageAction)
            }
        }
        
        val toolbar = actionManager.createActionToolbar(
            "FileRefTags.Toolbar",
            actionGroup,
            false  // 只显示图标
        )
        toolbar.targetComponent = this
        setToolbar(toolbar.component)

        // 设置列表布局：垂直排列，不显示滚动条
        list.layoutOrientation = javax.swing.JList.VERTICAL
        list.visibleRowCount = -1 // 显示所有行
        
        // 监听列表宽度变化，确保单元格宽度自适应
        list.addComponentListener(object : java.awt.event.ComponentAdapter() {
            override fun componentResized(e: java.awt.event.ComponentEvent?) {
                list.repaint() // 重新绘制列表，让渲染器使用新的宽度
            }
        })
        
        // 设置内容：直接使用列表，不使用滚动条
        val contentPanel = JPanel(BorderLayout())
        contentPanel.add(list, BorderLayout.CENTER)
        
        setContent(contentPanel)
        updateEmptyState()
    }

    /**
     * 加载引用数据
     */
    fun loadReferences() {
        listModel.clear()
        
        val references = dataService.getReferences()
        val groups = dataService.getGroups()
        
        if (references.isEmpty() && groups.isEmpty()) {
            updateEmptyState()
            list.repaint()
            return
        }

        // 按分组组织引用项
        val groupedReferences = references.groupBy { it.groupId }
        val ungroupedReferences = groupedReferences[null] ?: emptyList()

        // 添加分组和其引用项
        groups.forEach { group ->
            listModel.addElement(group)
            // 如果分组未折叠，显示其引用项
            if (!collapsedGroups.contains(group.id)) {
                (groupedReferences[group.id] ?: emptyList()).forEach { ref ->
                    listModel.addElement(ref)
                }
            }
        }

        // 添加未分组的引用项
        ungroupedReferences.forEach { ref ->
            listModel.addElement(ref)
        }

        // 在最后添加存储位置按钮标记
        listModel.addElement(StorageButtonMarker)

        updateEmptyState()
        list.repaint()
    }

    /**
     * 创建空状态标签
     */
    private fun createEmptyStateLabel(): JBLabel {
        val label = JBLabel("<html><div style='text-align: center; padding: 40px 20px;'>" +
                "<div style='font-size: 14px; color: #666; margin-bottom: 8px;'>${MyBundle.message("toolWindow.emptyState.title")}</div>" +
                "<div style='font-size: 12px; color: #999;'>${MyBundle.message("toolWindow.emptyState.description")}</div>" +
                "<div style='font-size: 11px; color: #bbb; margin-top: 8px; font-style: italic;'>${MyBundle.message("toolWindow.emptyState.hint")}</div>" +
                "</div></html>")
        label.horizontalAlignment = javax.swing.SwingConstants.CENTER
        label.verticalAlignment = javax.swing.SwingConstants.CENTER
        return label
    }
    
    /**
     * 创建显示存储位置按钮
     */
    private fun createShowStorageButton(): JButton {
        val button = JButton(MyBundle.message("action.showStorageLocation.text"))
        button.addActionListener {
            val storagePath = dataService.getStoragePath()
            val storageFile = File(storagePath)
            
            if (!storageFile.exists()) {
                com.github.luck0107.filereftags.utils.NotificationUtils.showWarning(
                    project,
                    MyBundle.message("action.showStorageLocation.title"),
                    MyBundle.message("action.showStorageLocation.notFound", storagePath)
                )
                return@addActionListener
            }
            
            // 在文件管理器中显示文件
            RevealFileAction.openFile(storageFile)
            
            // 同时在编辑器中打开文件
            val virtualFile = LocalFileSystem.getInstance().findFileByPath(storagePath)
            if (virtualFile != null) {
                FileEditorManager.getInstance(project).openFile(virtualFile, true)
            }
            
            com.github.luck0107.filereftags.utils.NotificationUtils.showInfo(
                project,
                MyBundle.message("action.showStorageLocation.title"),
                MyBundle.message("action.showStorageLocation.success", storagePath)
            )
        }
        return button
    }
    
    /**
     * 更新空状态显示
     */
    private fun updateEmptyState() {
        val isEmpty = listModel.isEmpty || (listModel.size == 1 && listModel.getElementAt(0) === StorageButtonMarker)
        list.isVisible = !isEmpty
        emptyStateLabel.isVisible = isEmpty
        
        val contentPanel = component as? JPanel
        contentPanel?.removeAll()
        
        if (isEmpty) {
            contentPanel?.add(emptyStateLabel, BorderLayout.CENTER)
        } else {
            contentPanel?.add(list, BorderLayout.CENTER)
        }
        
        contentPanel?.revalidate()
        contentPanel?.repaint()
    }

    /**
     * 处理引用项点击
     */
    private fun handleReferenceClick(reference: ReferenceItem) {
        // 使用 invokeLater 确保在 UI 线程执行，并在执行后清除选择状态
        com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
            ReferenceNavigationUtils.jumpToReference(project, reference)
            // 延迟清除选择状态，确保跳转操作完成
            com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
                list.clearSelection()
            }
        }
    }

    /**
     * 处理分组点击（展开/折叠）
     */
    private fun handleGroupClick(group: ReferenceGroup) {
        if (collapsedGroups.contains(group.id)) {
            // 展开分组
            collapsedGroups.remove(group.id)
        } else {
            // 折叠分组
            collapsedGroups.add(group.id)
        }
        refreshList()
    }

    /**
     * 显示右键菜单
     */
    private fun showContextMenu(e: MouseEvent) {
        val point = e.point
        val index = list.locationToIndex(point)
        if (index < 0) return

        val selectedValue = list.model.getElementAt(index)
        if (selectedValue !is ReferenceItem) return

        val actionGroup = DefaultActionGroup()
        actionGroup.add(EditReferenceTitleAction(selectedValue) { refreshList() })
        actionGroup.addSeparator()
        actionGroup.add(DeleteReferenceAction(selectedValue) { refreshList() })

        val dataContext = SimpleDataContext.builder()
            .add(CommonDataKeys.PROJECT, project)
            .build()
        val popup = JBPopupFactory.getInstance().createActionGroupPopup(
            MyBundle.message("contextMenu.title"),
            actionGroup,
            dataContext,
            JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
            true
        )

        // 将鼠标位置转换为屏幕坐标并显示菜单
        val screenPoint = java.awt.Point(e.locationOnScreen)
        popup.showInScreenCoordinates(list, screenPoint)
    }

    /**
     * 刷新列表
     */
    fun refreshList() {
        loadReferences()
        list.repaint()
    }

    /**
     * 列表单元格渲染器
     */
    private inner class ReferenceListCellRenderer : javax.swing.DefaultListCellRenderer() {
        override fun getListCellRendererComponent(
            list: javax.swing.JList<*>,
            value: Any?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): Component {
            // 如果是存储位置按钮标记，返回按钮组件
            if (value === StorageButtonMarker) {
                val buttonPanel = JPanel(BorderLayout())
                buttonPanel.border = JBUI.Borders.empty(8, 8)
                // 让按钮可以扩展到任意宽度
                showStorageButton.maximumSize = java.awt.Dimension(Integer.MAX_VALUE, showStorageButton.preferredSize.height)
                buttonPanel.add(showStorageButton, BorderLayout.CENTER)
                buttonPanel.background = UIUtil.getListBackground()
                return buttonPanel
            }
            
            val component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
            
            // 获取IDE背景色作为基准
            val baseBg = UIUtil.getListBackground()
            
            // 辅助函数：根据背景色亮度计算前景色（黑色或白色）
            fun getForegroundColor(backgroundColor: java.awt.Color): java.awt.Color {
                // 计算相对亮度 (0-255)
                val luminance = (0.299 * backgroundColor.red + 0.587 * backgroundColor.green + 0.114 * backgroundColor.blue).toInt()
                // 如果背景色较亮（亮度 > 128），使用黑色；否则使用白色
                return if (luminance > 128) java.awt.Color(0, 0, 0) else java.awt.Color(255, 255, 255)
            }
            
            when (value) {
                is ReferenceGroup -> {
                    val isCollapsed = collapsedGroups.contains(value.id)
                    text = if (isCollapsed) "▶ ${value.name}" else "▼ ${value.name}"
                    icon = AllIcons.Nodes.Folder
                    if (!isSelected) {
                        // 基于IDE背景色的深蓝色调
                        val bgColor = adjustColor(baseBg, -15, -10, 5)
                        background = bgColor
                        foreground = getForegroundColor(bgColor)
                    }
                }
                is ReferenceItem -> {
                    text = value.title
                    icon = when (value.type) {
                        ReferenceType.FILE -> {
                            if (!isSelected) {
                                // 基于IDE背景色的深黄色调
                                val bgColor = adjustColor(baseBg, 5, -5, -10)
                                background = bgColor
                                foreground = getForegroundColor(bgColor)
                            }
                            AllIcons.FileTypes.Text
                        }
                        ReferenceType.FILE_SNIPPET -> {
                            if (!isSelected) {
                                // 基于IDE背景色的深绿色调
                                val bgColor = adjustColor(baseBg, -10, 5, -10)
                                background = bgColor
                                foreground = getForegroundColor(bgColor)
                            }
                            AllIcons.Nodes.Method
                        }
                        ReferenceType.GLOBAL_SNIPPET -> {
                            if (!isSelected) {
                                // 基于IDE背景色的深粉色调
                                val bgColor = adjustColor(baseBg, 5, -10, -5)
                                background = bgColor
                                foreground = getForegroundColor(bgColor)
                            }
                            AllIcons.Nodes.Static
                        }
                        ReferenceType.COMMENT -> {
                            if (!isSelected) {
                                // 基于IDE背景色的深灰色调
                                val bgColor = adjustColor(baseBg, -8, -8, -8)
                                background = bgColor
                                foreground = getForegroundColor(bgColor)
                            }
                            AllIcons.General.Note
                        }
                    }
                }
            }
            
            return component
        }
        
        /**
         * 辅助函数：基于基准颜色调整RGB值
         * @param baseColor 基准颜色
         * @param rDelta RGB红色分量调整值（可以为负）
         * @param gDelta RGB绿色分量调整值（可以为负）
         * @param bDelta RGB蓝色分量调整值（可以为负）
         */
        private fun adjustColor(baseColor: java.awt.Color, rDelta: Int, gDelta: Int, bDelta: Int): java.awt.Color {
            val r = (baseColor.red + rDelta).coerceIn(0, 255)
            val g = (baseColor.green + gDelta).coerceIn(0, 255)
            val b = (baseColor.blue + bDelta).coerceIn(0, 255)
            return java.awt.Color(r, g, b)
        }
    }

    /**
     * 拖拽传输处理器
     */
    private inner class ReferenceListTransferHandler : TransferHandler() {
        private var draggedIndex = -1

        override fun createTransferable(c: JComponent): Transferable {
            val list = c as? JBList<*> ?: return object : Transferable {
                override fun getTransferDataFlavors() = arrayOf(DataFlavor.stringFlavor)
                override fun isDataFlavorSupported(flavor: DataFlavor) = false
                @Throws(UnsupportedFlavorException::class)
                override fun getTransferData(flavor: DataFlavor) = null
            }
            
            draggedIndex = list.selectedIndex
            if (draggedIndex < 0) {
                return object : Transferable {
                    override fun getTransferDataFlavors() = arrayOf(DataFlavor.stringFlavor)
                    override fun isDataFlavorSupported(flavor: DataFlavor) = false
                    @Throws(UnsupportedFlavorException::class)
                    override fun getTransferData(flavor: DataFlavor) = null
                }
            }
            
            val value = list.model.getElementAt(draggedIndex)
            // 不允许拖拽按钮标记
            if (value === StorageButtonMarker) {
                return object : Transferable {
                    override fun getTransferDataFlavors() = arrayOf(DataFlavor.stringFlavor)
                    override fun isDataFlavorSupported(flavor: DataFlavor) = false
                    @Throws(UnsupportedFlavorException::class)
                    override fun getTransferData(flavor: DataFlavor) = null
                }
            }
            
            return object : Transferable {
                override fun getTransferDataFlavors() = arrayOf(DataFlavor.stringFlavor)
                override fun isDataFlavorSupported(flavor: DataFlavor) = flavor == DataFlavor.stringFlavor
                @Throws(UnsupportedFlavorException::class)
                override fun getTransferData(flavor: DataFlavor): Any {
                    if (flavor == DataFlavor.stringFlavor) {
                        return when (value) {
                            is ReferenceItem -> value.id
                            is ReferenceGroup -> value.id
                            else -> ""
                        }
                    }
                    throw UnsupportedFlavorException(flavor)
                }
            }
        }

        override fun getSourceActions(c: JComponent) = MOVE

        override fun canImport(support: TransferHandler.TransferSupport): Boolean {
            return support.isDataFlavorSupported(DataFlavor.stringFlavor) &&
                   support.component is JBList<*>
        }

        override fun importData(support: TransferHandler.TransferSupport): Boolean {
            if (!canImport(support)) return false

            val targetList = support.component as? JBList<*> ?: return false
            val targetModel = targetList.model as? DefaultListModel<*> ?: return false

            try {
                val data = support.transferable.getTransferData(DataFlavor.stringFlavor) as? String ?: return false
                val dropIndex = support.dropLocation as? javax.swing.JList.DropLocation ?: return false
                val targetIndex = dropIndex.index

                if (targetIndex < 0 || draggedIndex < 0) return false

                val sourceValue = listModel.getElementAt(draggedIndex)
                val targetValue = if (targetIndex < targetModel.size) targetModel.getElementAt(targetIndex) else null

                // 不允许拖拽到按钮标记位置，也不允许拖拽按钮标记
                if (targetValue === StorageButtonMarker || sourceValue === StorageButtonMarker) {
                    return false
                }

                // 不允许拖拽分组到引用项之间，也不允许拖拽引用项到分组位置
                when {
                    sourceValue is ReferenceGroup && targetValue is ReferenceItem -> return false
                    sourceValue is ReferenceItem && targetValue is ReferenceGroup -> {
                        // 允许将引用项拖到分组上（添加到分组）
                        if (sourceValue is ReferenceItem) {
                            dataService.updateReferenceGroup(sourceValue.id, (targetValue as ReferenceGroup).id)
                            refreshList()
                            return true
                        }
                        return false
                    }
                }

                // 移动元素（确保不移动按钮标记）
                listModel.removeElementAt(draggedIndex)
                // 如果目标位置是最后一个（按钮标记），则插入到倒数第二个位置
                val lastIndex = listModel.size - 1
                val insertIndex = if (targetIndex >= lastIndex) lastIndex else {
                    if (targetIndex > draggedIndex) targetIndex - 1 else targetIndex
                }
                listModel.insertElementAt(sourceValue, insertIndex)

                // 更新数据服务中的顺序
                when (sourceValue) {
                    is ReferenceItem -> {
                        val newOrder = (0 until listModel.size)
                            .mapNotNull { listModel.getElementAt(it) as? ReferenceItem }
                            .map { it.id }
                        dataService.updateOrder(newOrder)
                    }
                    is ReferenceGroup -> {
                        val newOrder = (0 until listModel.size)
                            .mapNotNull { listModel.getElementAt(it) as? ReferenceGroup }
                            .map { it.id }
                        dataService.updateGroupOrder(newOrder)
                    }
                }

                refreshList()
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }
    }
}
