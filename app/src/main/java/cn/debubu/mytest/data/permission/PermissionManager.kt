package cn.debubu.mytest.data.permission

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import javax.inject.Inject

/**
 * 权限管理类，负责处理所有权限相关逻辑
 */
class PermissionManager @Inject constructor(
    private val appContext: Context
) {
    
    /**
     * 检查所有权限是否已经授予
     * 注意：对于从未请求过的权限，permanentlyDenied始终为false
     * 只有在handlePermissionResult方法中才能正确判断是否永久拒绝
     */
    fun checkPermissions(permissions: List<String>, activity: Activity? = null): PermissionState {
        val missing = missingPermissions(permissions)
        val allGranted = missing.isEmpty()
        
        // 对于从未请求过的权限，permanentlyDenied始终为false
        // 只有在handlePermissionResult方法中才能正确判断是否永久拒绝
        val permanentlyDenied = false
        
        return PermissionState(
            allGranted = allGranted,
            missingPermissions = missing,
            permanentlyDenied = permanentlyDenied,
            shouldRequest = !permanentlyDenied && missing.isNotEmpty()
        )
    }
    
    /**
     * 获取缺失的权限列表
     */
    fun missingPermissions(permissions: List<String>): List<String> = 
        permissions.filter { 
            ContextCompat.checkSelfPermission(appContext, it) != PackageManager.PERMISSION_GRANTED 
        }
    
    /**
     * 处理权限申请结果
     */
    fun handlePermissionResult(
        permissions: List<String>,
        result: Map<String, Boolean>,
        activity: Activity?
    ): PermissionState {
        val allGranted = permissions.all { result[it] == true }
        val missing = missingPermissions(permissions)
        val permanentlyDenied = if (activity != null) {
            isPermanentlyDenied(activity, missing)
        } else {
            false
        }
        
        return PermissionState(
            allGranted = allGranted,
            missingPermissions = missing,
            permanentlyDenied = permanentlyDenied,
            shouldRequest = !permanentlyDenied && missing.isNotEmpty()
        )
    }
    
    /**
     * 检查是否有权限被永久拒绝
     * 注意：只有当用户已经拒绝过权限请求时，shouldShowRationale返回false才表示永久拒绝
     * 对于从未请求过的权限，shouldShowRationale也会返回false，但不应视为永久拒绝
     */
    fun isPermanentlyDenied(activity: Activity, permissions: List<String>): Boolean {
        if (permissions.isEmpty()) return false
        
        return permissions.any { perm ->
            ContextCompat.checkSelfPermission(appContext, perm) != PackageManager.PERMISSION_GRANTED &&
            !shouldShowRationale(activity, perm)
        }
    }
    
    /**
     * 检查是否需要显示权限申请理由
     */
    fun shouldShowRationale(activity: Activity, permission: String): Boolean {
        return activity.shouldShowRequestPermissionRationale(permission)
    }
    
    /**
     * 启动权限申请
     */
    fun requestPermissions(
        launcher: ActivityResultLauncher<Array<String>>,
        permissions: List<String>
    ) {
        if (permissions.isNotEmpty()) {
            launcher.launch(permissions.toTypedArray())
        }
    }
}
