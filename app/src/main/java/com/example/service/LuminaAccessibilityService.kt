package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.data.model.AccessibilityNodeDisplay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LuminaAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "LuminaAccessibility"
        var instance: LuminaAccessibilityService? = null
            private set

        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

        private val _nodeTree = MutableStateFlow<List<AccessibilityNodeDisplay>>(emptyList())
        val nodeTree: StateFlow<List<AccessibilityNodeDisplay>> = _nodeTree.asStateFlow()

        private val _lastActionLog = MutableStateFlow<String>("Service ready for autonomous actions")
        val lastActionLog: StateFlow<String> = _lastActionLog.asStateFlow()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isServiceActive.value = true
        _lastActionLog.value = "Accessibility Service Connected & Active"
        refreshNodeHierarchy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            refreshNodeHierarchy()
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "LuminaAccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        _isServiceActive.value = false
    }

    fun refreshNodeHierarchy() {
        try {
            val root = rootInActiveWindow ?: return
            val nodes = mutableListOf<AccessibilityNodeDisplay>()
            traverseNodeTree(root, nodes, 0)
            _nodeTree.value = nodes
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing node hierarchy", e)
        }
    }

    private fun traverseNodeTree(
        node: AccessibilityNodeInfo?,
        list: MutableList<AccessibilityNodeDisplay>,
        depth: Int
    ) {
        if (node == null || depth > 8) return

        try {
            val rect = Rect()
            node.getBoundsInScreen(rect)

            val viewId = try { node.viewIdResourceName } catch (e: Exception) { null }
            val textStr = try { node.text?.toString() } catch (e: Exception) { null }
            val descStr = try { node.contentDescription?.toString() } catch (e: Exception) { null }
            val classNameStr = try { node.className?.toString() ?: "View" } catch (e: Exception) { "View" }
            val isClickable = try { node.isClickable } catch (e: Exception) { false }
            val isEditable = try { node.isEditable } catch (e: Exception) { false }

            val display = AccessibilityNodeDisplay(
                id = "${viewId ?: "node"}_${rect.left}_${rect.top}",
                viewId = viewId,
                text = textStr,
                contentDesc = descStr,
                className = classNameStr,
                isClickable = isClickable,
                isEditable = isEditable,
                bounds = "[${rect.left}, ${rect.top} - ${rect.right}, ${rect.bottom}]",
                depth = depth
            )

            // Only add meaningful nodes to keep list clean
            if (display.viewId != null || !display.text.isNullOrEmpty() || !display.contentDesc.isNullOrEmpty() || display.isClickable) {
                list.add(display)
            }

            val childCount = try { node.childCount } catch (e: Exception) { 0 }
            for (i in 0 until childCount) {
                val child = try { node.getChild(i) } catch (e: Exception) { null }
                if (child != null) {
                    traverseNodeTree(child, list, depth + 1)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error traversing node", e)
        }
    }

    /**
     * Dynamically finds a UI element by ViewID, ContentDescription, or Text and clicks it
     */
    fun performDynamicClick(viewId: String?, text: String?, contentDesc: String?): Boolean {
        return try {
            val rootNode = rootInActiveWindow
            if (rootNode == null) {
                _lastActionLog.value = "Click failed: Root window is null"
                return false
            }

            // 1. Try finding by View Resource ID
            if (!viewId.isNullOrEmpty()) {
                val nodes = rootNode.findAccessibilityNodeInfosByViewId(viewId)
                if (!nodes.isNullOrEmpty()) {
                    val target = nodes.firstOrNull { it.isClickable } ?: nodes[0]
                    val clicked = target.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    _lastActionLog.value = "Action Click dispatched on ViewID: $viewId (Success: $clicked)"
                    return clicked
                }
            }

            // 2. Try finding by Text
            if (!text.isNullOrEmpty()) {
                val nodes = rootNode.findAccessibilityNodeInfosByText(text)
                if (!nodes.isNullOrEmpty()) {
                    val target = nodes.firstOrNull { it.isClickable } ?: nodes[0]
                    val clicked = target.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    _lastActionLog.value = "Action Click dispatched on Text: '$text' (Success: $clicked)"
                    return clicked
                }
            }

            // 3. Fallback: Recursive tree traversal for Content-Description
            val clickedDesc = findAndClickNodeRecursive(rootNode, contentDesc)
            _lastActionLog.value = "Action Click dispatched on ContentDesc: '$contentDesc' (Success: $clickedDesc)"
            clickedDesc
        } catch (e: Exception) {
            _lastActionLog.value = "Click exception: ${e.localizedMessage}"
            false
        }
    }

    private fun findAndClickNodeRecursive(node: AccessibilityNodeInfo?, query: String?): Boolean {
        if (node == null || query.isNullOrEmpty()) return false

        try {
            val desc = node.contentDescription?.toString()
            if (desc != null && desc.contains(query, ignoreCase = true)) {
                return if (node.isClickable) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                } else {
                    node.parent?.performAction(AccessibilityNodeInfo.ACTION_CLICK) ?: false
                }
            }

            val count = try { node.childCount } catch (e: Exception) { 0 }
            for (i in 0 until count) {
                val child = try { node.getChild(i) } catch (e: Exception) { null }
                if (child != null && findAndClickNodeRecursive(child, query)) return true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in recursive search", e)
        }
        return false
    }

    /**
     * Dynamically injects text into active focused input field
     */
    fun performDynamicSetText(text: String): Boolean {
        return try {
            val rootNode = rootInActiveWindow
            if (rootNode == null) {
                _lastActionLog.value = "SetText failed: Root window is null"
                return false
            }
            val focusedNode = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
                ?: rootNode.findAccessibilityNodeInfosByText("").firstOrNull { it.isEditable }

            if (focusedNode != null) {
                val args = Bundle().apply {
                    putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
                }
                val result = focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
                _lastActionLog.value = "SetText dispatched: '$text' (Success: $result)"
                return result
            }

            _lastActionLog.value = "SetText: No editable input field in focus"
            false
        } catch (e: Exception) {
            _lastActionLog.value = "SetText exception: ${e.localizedMessage}"
            false
        }
    }

    /**
     * Performs physical touch tap on exact normalized or screen pixel coordinates
     */
    fun dispatchTouchTap(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 50)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                _lastActionLog.value = "Gesture Tap at ($x, $y) completed"
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                _lastActionLog.value = "Gesture Tap at ($x, $y) cancelled"
            }
        }, null)
    }

    /**
     * Performs physical swipe gesture (useful for Instagram Reels auto-scroll)
     */
    fun dispatchSwipe(startX: Float, startY: Float, endX: Float, endY: Float, durationMs: Long = 300) {
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                _lastActionLog.value = "Gesture Swipe from ($startX,$startY) to ($endX,$endY) finished"
            }
        }, null)
    }

    fun performBack(): Boolean {
        val res = performGlobalAction(GLOBAL_ACTION_BACK)
        _lastActionLog.value = "Global Action BACK dispatched: $res"
        return res
    }

    fun performHome(): Boolean {
        val res = performGlobalAction(GLOBAL_ACTION_HOME)
        _lastActionLog.value = "Global Action HOME dispatched: $res"
        return res
    }
}
