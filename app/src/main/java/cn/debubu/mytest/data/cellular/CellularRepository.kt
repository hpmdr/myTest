package cn.debubu.mytest.data.cellular

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.CellInfo
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 蜂窝信号数据仓库
 * 负责从 Android TelephonyManager API 获取蜂窝信号数据
 * 支持双卡和邻小区信息
 *
 * @param context 应用上下文
 */
@Singleton
class CellularRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val telephonyManager: TelephonyManager by lazy {
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }

    /**
     * 获取指定 SIM 卡槽的 TelephonyManager
     *
     * @param slotId SIM 卡槽 ID (0 或 1)
     * @return 对应的 TelephonyManager 实例，如果不可用则返回 null
     */
    private fun getTelephonyManagerForSlot(slotId: Int): TelephonyManager? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyManager.createForSubscriptionId(getSubscriptionIdForSlot(slotId))
        } else {
            if (slotId == 0) telephonyManager else null
        }
    }

    /**
     * 获取指定卡槽的订阅 ID
     *
     * @param slotId SIM 卡槽 ID
     * @return 订阅 ID，如果不可用则返回默认值
     */
    private fun getSubscriptionIdForSlot(slotId: Int): Int {
        val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE)
                as android.telephony.SubscriptionManager

        val activeSubscriptionInfoList = subscriptionManager.activeSubscriptionInfoList
        if (activeSubscriptionInfoList != null && activeSubscriptionInfoList.size > slotId) {
            return activeSubscriptionInfoList[slotId].subscriptionId
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            android.telephony.SubscriptionManager.INVALID_SUBSCRIPTION_ID
        } else {
            Int.MAX_VALUE
        }
    }

    /**
     * 检查是否有必要的权限
     *
     * @return 是否拥有所有必要的权限
     */
    private fun hasRequiredPermissions(): Boolean {
        val permissions = mutableListOf<String>()

        // 1. 位置权限：获取基站参数（PCI/EARFCN）的核心
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)

        // 2. 电话状态权限：获取双卡 SubId 和运营商信息的关键
        permissions.add(Manifest.permission.READ_PHONE_STATE)

        // 3. Android 10+ 的额外建议
        // 如果你在后台也需要获取信号（例如 Service 中），则需要：
        // permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * 获取运营商名称
     *
     * @param slotId SIM 卡槽 ID
     * @return 运营商名称
     */
    private fun getOperatorName(slotId: Int): String {
        val tm = getTelephonyManagerForSlot(slotId) ?: return "未知"
        return tm.networkOperatorName ?: "未知"
    }

    /**
     * 获取网络类型
     *
     * @param slotId SIM 卡槽 ID
     * @return 网络类型字符串
     */
    private fun getNetworkType(slotId: Int): String {
        val tm = getTelephonyManagerForSlot(slotId) ?: return "未知"
        return CellularSignalInfo.getNetworkTypeName(tm.networkType)
    }

    /**
     * 从 CellInfo 列表中提取服务小区和邻小区信息
     *
     * @param cellInfoList CellInfo 列表
     * @param slotId SIM 卡槽 ID
     * @param isPrimary 是否为主卡
     * @return CellularData 对象
     */
    private fun extractCellularData(
        cellInfoList: List<CellInfo>,
        slotId: Int,
        isPrimary: Boolean
    ): CellularData {
        if (cellInfoList.isEmpty()) {
            Timber.w("CellInfo 列表为空")
            return CellularData(
                servingCell = CellularSignalInfo(
                    simSlotId = slotId,
                    operatorName = getOperatorName(slotId),
                    networkType = getNetworkType(slotId),
                    isPrimary = isPrimary
                )
            )
        }

        var servingCell: CellularSignalInfo? = null
        val neighborCells = mutableListOf<NeighborCellInfo>()

        for (cellInfo in cellInfoList) {
            if (cellInfo.isRegistered) {
                servingCell = CellularSignalInfo.fromCellInfo(cellInfo, slotId, isPrimary)
                Timber.d("服务小区: PCI=${servingCell.pci}, RSRP=${servingCell.rsrp} dBm")
            } else {
                val neighborCell = NeighborCellInfo.fromCellInfo(cellInfo, isServing = false)
                neighborCells.add(neighborCell)
                Timber.d("邻小区: PCI=${neighborCell.pci}, RSRP=${neighborCell.rsrp} dBm")
            }
        }

        if (servingCell == null) {
            servingCell = CellularSignalInfo(
                simSlotId = slotId,
                operatorName = getOperatorName(slotId),
                networkType = getNetworkType(slotId),
                isPrimary = isPrimary
            )
        }

        return CellularData(
            servingCell = servingCell,
            neighborCells = neighborCells
        )
    }

    /**
     * 获取指定 SIM 卡槽的蜂窝数据
     *
     * @param slotId SIM 卡槽 ID (0 或 1)
     * @return CellularData 对象
     */
    @SuppressLint("MissingPermission")
    fun getCellularData(slotId: Int): CellularData {
        if (!hasRequiredPermissions()) {
            Timber.w("缺少必要的权限")
            return CellularData(
                servingCell = CellularSignalInfo(
                    simSlotId = slotId,
                    operatorName = getOperatorName(slotId),
                    networkType = getNetworkType(slotId),
                    isPrimary = slotId == 0
                )
            )
        }

        val tm = getTelephonyManagerForSlot(slotId)
        val cellInfoList = tm?.allCellInfo ?: emptyList()

        return extractCellularData(cellInfoList, slotId, slotId == 0)
    }

    /**
     * 获取指定 SIM 卡槽的蜂窝数据流
     * 使用 callbackFlow 监听 CellInfo 变化
     *
     * @param slotId SIM 卡槽 ID (0 或 1)
     * @return CellularData 流
     */
    @SuppressLint("MissingPermission")
    fun getCellularDataFlow(slotId: Int): Flow<CellularData> = callbackFlow {
        if (!hasRequiredPermissions()) {
            Timber.w("缺少必要的权限")
            trySend(
                CellularData(
                    servingCell = CellularSignalInfo(
                        simSlotId = slotId,
                        operatorName = getOperatorName(slotId),
                        networkType = getNetworkType(slotId),
                        isPrimary = slotId == 0
                    )
                )
            )
            awaitClose()
            return@callbackFlow
        }

        val tm = getTelephonyManagerForSlot(slotId)
        if (tm == null) {
            Timber.w("无法获取 SIM 卡槽 $slotId 的 TelephonyManager")
            trySend(
                CellularData(
                    servingCell = CellularSignalInfo(
                        simSlotId = slotId,
                        operatorName = getOperatorName(slotId),
                        networkType = getNetworkType(slotId),
                        isPrimary = slotId == 0
                    )
                )
            )
            awaitClose()
            return@callbackFlow
        }

        val callback = object : TelephonyCallback(), TelephonyCallback.CellInfoListener {
            override fun onCellInfoChanged(cellInfoList: List<CellInfo>) {
                Timber.d("onCellInfoChanged: SIM $slotId, CellInfo 数量 = ${cellInfoList.size}")
                val data = extractCellularData(cellInfoList, slotId, slotId == 0)
                trySend(data)
            }
        }

        try {
            tm.registerTelephonyCallback(context.mainExecutor, callback)

            val initialCellInfo = tm.allCellInfo ?: emptyList()
            val initialData = extractCellularData(initialCellInfo, slotId, slotId == 0)
            trySend(initialData)

            Timber.d("已注册 CellInfo 监听器: SIM $slotId")
        } catch (e: Exception) {
            Timber.e(e, "注册 CellInfo 监听器失败: SIM $slotId")
        }

        awaitClose {
            try {
                tm.unregisterTelephonyCallback(callback)
                Timber.d("已注销 CellInfo 监听器: SIM $slotId")
            } catch (e: Exception) {
                Timber.e(e, "注销 CellInfo 监听器失败: SIM $slotId")
            }
        }
    }.distinctUntilChanged()

    /**
     * 获取双卡蜂窝数据流
     * 返回一个包含两个 SIM 卡数据的流
     *
     * @return Pair<CellularData, CellularData> 流，第一个是 SIM 1，第二个是 SIM 2
     */
    fun getDualSimCellularDataFlow(): Flow<Pair<CellularData, CellularData>> = callbackFlow {
        val sim1Flow = getCellularDataFlow(0)
        val sim2Flow = getCellularDataFlow(1)

        val sim1Job = launch {
            sim1Flow.collect { sim1Data ->
                val sim2Data = trySendWithFallback(sim2Flow, sim1Data)
                trySend(Pair(sim1Data, sim2Data))
            }
        }

        val sim2Job = launch {
            sim2Flow.collect { sim2Data ->
                val sim1Data = trySendWithFallback(sim1Flow, sim2Data)
                trySend(Pair(sim1Data, sim2Data))
            }
        }

        awaitClose {
            sim1Job.cancel()
            sim2Job.cancel()
        }
    }

    private suspend fun trySendWithFallback(
        flow: Flow<CellularData>,
        fallbackData: CellularData
    ): CellularData {
        return try {
            kotlinx.coroutines.withTimeoutOrNull(100) {
                flow.first()
            } ?: fallbackData
        } catch (e: Exception) {
            Timber.e(e, "获取数据失败，使用回退数据")
            fallbackData
        }
    }
}