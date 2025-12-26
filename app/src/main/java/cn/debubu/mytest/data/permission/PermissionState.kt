package cn.debubu.mytest.data.permission

data class PermissionState(
    val allGranted: Boolean = false,
    val missingPermissions: List<String> = emptyList(),
    val permanentlyDenied: Boolean = false,
    val shouldRequest: Boolean = false
)