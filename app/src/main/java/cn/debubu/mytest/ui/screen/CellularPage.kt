package cn.debubu.mytest.ui.screen

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
        val allGranted = viewModel.requiredPermissions.all { perm ->
            result[perm] == true
        }
        val missing = viewModel.missingPermissions()
        val permanentlyDenied = missing.any { perm ->
            activity != null &&
                !ActivityCompat.shouldShowRequestPermissionRationale(activity, perm) &&
                ContextCompat.checkSelfPermission(
                    context,
                    perm
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        viewModel.onPermissionResult(allGranted, permanentlyDenied)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = uiState.status,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        val missing = viewModel.missingPermissions()
        if (missing.isNotEmpty()) {
            Text(
                text = "需要定位/电话权限来读取蜂窝信号，只在本地展示，不会上传服务器。",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        Button(
            onClick = {
                if (missing.isNotEmpty()) {
                    permissionLauncher.launch(missing.toTypedArray())
                } else {
                    viewModel.refreshSignals()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (missing.isNotEmpty()) "申请权限" else "重新获取")
        }
        if (uiState.showSettingsButton && activity != null) {
            Button(
                onClick = { openAppSettings(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("去系统设置开启权限")
            }
        }
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(vertical = 12.dp))
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            items(uiState.signals) { item ->
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
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