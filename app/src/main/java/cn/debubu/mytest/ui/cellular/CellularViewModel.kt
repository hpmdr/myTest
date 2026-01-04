package cn.debubu.mytest.ui.cellular

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.debubu.mytest.data.cellular.CellularSignalModel
import cn.debubu.mytest.data.permission.PermissionManager
import cn.debubu.mytest.data.permission.PermissionState
import cn.debubu.mytest.data.cellular.CellularRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CellularUiState(
    val status: String = "准备获取权限",
    val signals: CellularSignalModel? = null,
    val isLoading: Boolean = false,
    val showSettingsButton: Boolean = false,
    val permissionState: PermissionState = PermissionState(),
    val showPermissionDialog: Boolean = false,
    val showPermanentDenialDialog: Boolean = false
)

@HiltViewModel
class CellularViewModel @Inject constructor(
    private val repository: CellularRepository,
    private val permissionManager: PermissionManager,
    @param:ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(CellularUiState())
    val uiState: StateFlow<CellularUiState> = _uiState.asStateFlow()

    init {
        // 收集Repository的信号数据流
        viewModelScope.launch(Dispatchers.Main) {
            repository.signalFlow.collectLatest { signalInfo ->
                val status = if (signalInfo.cells.isEmpty()) {
                    "未能读取到信号数据"
                } else {
                    "已读取到信号数据 - ${signalInfo.networkType}"
                }
                _uiState.value = _uiState.value.copy(
                    status = status,
                    signals = signalInfo,
                    isLoading = false
                )
            }
        }
    }

    val requiredPermissions: List<String> = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        if (Build.VERSION.SDK_INT >= 34) {
            Manifest.permission.READ_BASIC_PHONE_STATE
        } else {
            Manifest.permission.READ_PHONE_STATE
        }
    )

    fun checkPermissions(activity: Activity? = null) {
        val permissionState = permissionManager.checkPermissions(requiredPermissions, activity)
        updateUiStateWithPermission(permissionState)
    }

    fun missingPermissions(): List<String> = 
        permissionManager.missingPermissions(requiredPermissions)

    fun onPermissionResult(result: Map<String, Boolean>, activity: Activity?) {
        val permissionState = permissionManager.handlePermissionResult(requiredPermissions, result, activity)
        updateUiStateWithPermission(permissionState)
        
        if (permissionState.allGranted) {
            refreshSignals()
        } else if (permissionState.permanentlyDenied) {
            showPermanentDenialDialog()
        }
    }

    fun onPermissionDialogAccepted() {
        // 用户接受权限说明，关闭弹窗，不再自动显示
        hidePermissionDialog()
    }

    fun onPermissionDialogDeclined() {
        // 用户拒绝权限说明，关闭弹窗，不再自动显示
        hidePermissionDialog()
    }

    fun showPermissionDialog() {
        _uiState.value = _uiState.value.copy(showPermissionDialog = true)
    }

    fun hidePermissionDialog() {
        _uiState.value = _uiState.value.copy(showPermissionDialog = false)
    }

    fun showPermanentDenialDialog() {
        _uiState.value = _uiState.value.copy(showPermanentDenialDialog = true)
    }

    fun hidePermanentDenialDialog() {
        _uiState.value = _uiState.value.copy(showPermanentDenialDialog = false)
    }

    private fun updateUiStateWithPermission(permissionState: PermissionState) {
        val showSettingsButton = permissionState.permanentlyDenied
        val status = when {
            permissionState.allGranted -> "权限已授予"
            permissionState.permanentlyDenied -> "权限被永久拒绝，请在系统设置中开启定位/电话权限"
            else -> "需要定位/电话权限以读取蜂窝信号，仅用于本地展示，不会上报。"
        }
        
        // 根据权限状态设置弹窗显示
        val showPermissionDialog = !permissionState.allGranted && !permissionState.permanentlyDenied
        val showPermanentDenialDialog = permissionState.permanentlyDenied
        
        _uiState.value = _uiState.value.copy(
            permissionState = permissionState,
            showSettingsButton = showSettingsButton,
            status = status,
            showPermissionDialog = showPermissionDialog,
            showPermanentDenialDialog = showPermanentDenialDialog
        )
    }

    fun refreshSignals() {
        val permissionState = permissionManager.checkPermissions(requiredPermissions)
        if (!permissionState.allGranted) {
            updateUiStateWithPermission(permissionState)
            return
        }
        
        _uiState.value = _uiState.value.copy(
            isLoading = true, 
            status = "读取信号中..."
        )
        
        viewModelScope.launch(Dispatchers.Default) {
            repository.fetchSignalInfo() // 调用后会自动更新StateFlow
        }
    }

    fun registerCallback() {
        repository.registerCallback()
    }

    fun unregisterCallback() {
        repository.unregisterCallback()
    }

    fun checkPermissionsAndRegisterCallback() {
        val permissionState = permissionManager.checkPermissions(requiredPermissions)
        if (permissionState.allGranted) {
            repository.registerCallback()
        }
    }

    // 确保在ViewModel销毁时注销回调
    override fun onCleared() {
        super.onCleared()
        repository.unregisterCallback()
    }
}

