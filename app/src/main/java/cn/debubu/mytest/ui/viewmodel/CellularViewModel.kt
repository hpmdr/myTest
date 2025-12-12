package cn.debubu.mytest.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.debubu.mytest.data.repository.CellularRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CellularUiState(
    val status: String = "准备获取权限",
    val signals: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val showSettingsButton: Boolean = false
)

@HiltViewModel
class CellularViewModel @Inject constructor(
    private val repository: CellularRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(CellularUiState())
    val uiState: StateFlow<CellularUiState> = _uiState.asStateFlow()

    val requiredPermissions: List<String> = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        if (Build.VERSION.SDK_INT >= 34) {
            Manifest.permission.READ_BASIC_PHONE_STATE
        } else {
            Manifest.permission.READ_PHONE_STATE
        }
    )

    fun missingPermissions(): List<String> =
        requiredPermissions.filter {
            ContextCompat.checkSelfPermission(appContext, it) != PackageManager.PERMISSION_GRANTED
        }

    fun onPermissionResult(allGranted: Boolean, permanentlyDenied: Boolean) {
        if (allGranted) {
            refreshSignals()
        } else {
            _uiState.value = CellularUiState(
                status = if (permanentlyDenied) {
                    "权限被永久拒绝，请在系统设置中开启定位/电话权限"
                } else {
                    "需要定位/电话权限以读取蜂窝信号，仅用于本地展示，不会上报。"
                },
                signals = emptyList(),
                isLoading = false,
                showSettingsButton = permanentlyDenied
            )
        }
    }

    fun refreshSignals() {
        val missing = missingPermissions()
        if (missing.isNotEmpty()) {
            _uiState.value = CellularUiState(
                status = "需要定位/电话权限以读取蜂窝信号，仅用于本地展示，不会上报。",
                signals = emptyList(),
                isLoading = false
            )
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, status = "读取信号中...")
        viewModelScope.launch(Dispatchers.Default) {
            val signals = repository.fetchSignalInfo()
            val status = if (signals.isEmpty()) {
                "未能读取到信号数据"
            } else {
                "已读取到 ${signals.size} 条信号参数"
            }
            _uiState.value = CellularUiState(
                status = status,
                signals = signals,
                isLoading = false
            )
        }
    }
}

