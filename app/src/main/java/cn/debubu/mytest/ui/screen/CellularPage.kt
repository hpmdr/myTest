package cn.debubu.mytest.ui.screen

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.debubu.mytest.ui.screen.signal.GsmSignalContent
import cn.debubu.mytest.ui.screen.signal.LteSignalContent
import cn.debubu.mytest.ui.screen.signal.NrSignalContent
import cn.debubu.mytest.ui.screen.signal.SignalParameterItem
import cn.debubu.mytest.ui.viewmodel.CellularViewModel

/**
 * 蜂窝网络详细信息页面
 */
@Composable
fun CellularPage(viewModel: CellularViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        viewModel.onPermissionResult(result, activity)
    }

    // 页面加载完成后，手动触发权限检查，确保Activity已经准备好
    LaunchedEffect(Unit) {
        viewModel.checkPermissions(activity)
    }

    // 监听 ON_RESUME 事件 - 应用进入前台时检查权限并注册回调
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.checkPermissionsAndRegisterCallback()
    }

    // 监听 ON_PAUSE 事件 - 应用进入后台时注销回调
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        viewModel.unregisterCallback()
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        val permissionState = uiState.permissionState

        // 只有当加载中或者所有权限都已授予时，才显示加载指示器和信号列表
        if (uiState.isLoading || permissionState.allGranted) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 12.dp))
            }

            if (permissionState.allGranted && uiState.signals != null) {
                val signalInfo = uiState.signals

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    // 网络类型和通用信号参数合并到一个Card中
                    item {
                        Card(modifier = Modifier.padding(bottom = 12.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // 第一行：网络类型信息
                                Text(
                                    text = "网络类型：${signalInfo.networkType}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                // 第二行：信号强度信息
                                Text(
                                    text = "信号强度：${signalInfo.dbm} dBm ${signalInfo.signalLevel}格信号",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }

                    // 详细信号参数放在一个Card中
                    item {
                        Card(modifier = Modifier.padding(bottom = 12.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (signalInfo.gsmSignal != null) {
                                    GsmSignalContent(signal = signalInfo.gsmSignal)
                                }

                                if (signalInfo.lteSignal != null) {
                                    LteSignalContent(signal = signalInfo.lteSignal)
                                }

                                if (signalInfo.nrSignal != null) {
                                    NrSignalContent(signal = signalInfo.nrSignal)
                                }
                            }
                        }
                    }

                    // 小区信息保持不变
                    if (signalInfo.cells.isNotEmpty()) {
                        item {
                            SectionTitle(title = "小区信息")
                        }

                        signalInfo.cells.forEach { cell ->
                            item {
                                SignalParameterItem(
                                    name = "Cell #${cell.index}",
                                    value = "${cell.type} 信号: ${cell.dbm} dBm, 等级: ${cell.level}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 权限说明弹窗
    if (uiState.showPermissionDialog) {
        PermissionExplanationDialog(
            onAccept = {
                viewModel.onPermissionDialogAccepted()
                permissionLauncher.launch(uiState.permissionState.missingPermissions.toTypedArray())
            },
            onDecline = {
                viewModel.onPermissionDialogDeclined()
            }
        )
    }

    // 永久拒绝权限引导弹窗
    if (uiState.showPermanentDenialDialog) {
        PermanentDenialDialog(
            onGoToSettings = {
                viewModel.hidePermanentDenialDialog()
                openAppSettings(context)
            },
            onCancel = {
                viewModel.hidePermanentDenialDialog()
            }
        )
    }
}

private fun openAppSettings(context: android.content.Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

/**
 * 分组标题组件
 */
@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = MaterialTheme.typography.headlineSmall.fontWeight,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(all = 12.dp)
    )
}

/**
 * 权限说明弹窗组件
 */
@Composable
fun PermissionExplanationDialog(
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDecline,
        title = {
            Text(
                text = "需要蜂窝网络权限",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "为了提供蜂窝网络信号强度检测功能，我们需要获取以下权限：",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = "1. 精确位置权限",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "   - 用途：获取网络基站信息和定位相关的网络信号数据",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )

                Text(
                    text = "2. 粗略位置权限",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "   - 用途：在无法获取精确位置时提供基本的网络信号信息",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )

                Text(
                    text = "3. 电话状态权限",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "   - 用途：读取设备的蜂窝网络状态和手机信息，用于分析信号强度",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp)
                )

                Text(
                    text = "所有数据仅在本地使用，不会上传至服务器或用于其他用途。",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onAccept) {
                Text("授予权限")
            }
        },
        dismissButton = {
            TextButton(onClick = onDecline) {
                Text("拒绝")
            }
        }
    )
}

/**
 * 永久拒绝权限引导弹窗
 */
@Composable
fun PermanentDenialDialog(
    onGoToSettings: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = "权限被永久拒绝",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = "您已永久拒绝了必要的权限请求。为了使用蜂窝网络信号检测功能，请前往系统设置手动开启所需权限。",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onGoToSettings) {
                Text("前往设置")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("取消")
            }
        }
    )
}