package cn.debubu.mytest.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.telephony.CellInfo
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoNr
import android.telephony.CellInfoWcdma
import android.telephony.CellIdentityGsm
import android.telephony.CellIdentityLte
import android.telephony.CellIdentityNr
import android.telephony.CellIdentityWcdma
import android.content.Context
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import cn.debubu.mytest.data.model.CellInfoModel
import cn.debubu.mytest.data.model.CellularSignalModel
import cn.debubu.mytest.data.model.GsmSignalModel
import cn.debubu.mytest.data.model.LteSignalModel
import cn.debubu.mytest.data.model.NrSignalModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CellularRepository @Inject constructor(
    private val telephonyManager: TelephonyManager,
    private val context: Context
) {
    // 信号数据的StateFlow，用于实时监听信号变化
    private val _signalFlow = MutableStateFlow<CellularSignalModel>(CellularSignalModel())
    val signalFlow: StateFlow<CellularSignalModel> = _signalFlow

    // TelephonyCallback实例（Android 12+）
    @SuppressLint("MissingPermission")
    private val cellInfoCallback = object : TelephonyCallback(), TelephonyCallback.CellInfoListener {
        override fun onCellInfoChanged(cellInfoList: List<CellInfo>) {
            // 当系统信号变化时，更新StateFlow
            val signalInfo = generateSignalInfo(cellInfoList)
            _signalFlow.value = signalInfo
        }
    }

    /**
     * 注册信号变化回调
     */
    fun registerCallback() {
        telephonyManager.registerTelephonyCallback(context.mainExecutor, cellInfoCallback)
    }

    /**
     * 注销信号变化回调
     */
    fun unregisterCallback() {
        telephonyManager.unregisterTelephonyCallback(cellInfoCallback)
    }

    /**
     * 手动获取信号信息
     */
    @SuppressLint("MissingPermission")
    fun fetchSignalInfo(): CellularSignalModel {
        val cells = telephonyManager.allCellInfo
        val signalInfo = generateSignalInfo(cells as List<CellInfo>?)
        _signalFlow.value = signalInfo // 同时更新StateFlow
        return signalInfo
    }

    /**
     * 生成信号信息模型
     */
    @SuppressLint("MissingPermission")
    private fun generateSignalInfo(cells: List<CellInfo>?): CellularSignalModel {
        // 获取网络类型信息
        val networkType = getNetworkType()
        val networkTypeName = getNetworkTypeName(networkType)
        
        // 初始化默认信号模型
        var signalLevel = 0
        var dbm = 0
        var gsmSignal: GsmSignalModel? = null
        var lteSignal: LteSignalModel? = null
        var nrSignal: NrSignalModel? = null
        var wcdmaSignalLevel = 0
        var wcdmaSignalDbm = 0

        // 获取小区信息和信号参数（更可靠的方式）
        val cellInfoList = mutableListOf<CellInfoModel>()
        cells?.forEachIndexed { index, cell ->
            val s = cell.cellSignalStrength
            val type = getCellType(cell)
            
            // 添加小区信息
            cellInfoList.add(CellInfoModel(
                index = index,
                type = type,
                dbm = s.dbm,
                level = s.level
            ))
            
            // 从CellInfo中获取详细信号参数（比signalStrength更可靠）
            when (cell) {
                is CellInfoLte -> {
                    // LTE网络信号参数
                    cell.cellSignalStrength?.let { strength ->
                        // 使用当前连接的小区作为主信号
                        if (signalLevel == 0 || signalLevel < strength.level) {
                            signalLevel = strength.level
                            dbm = strength.dbm
                        }
                        
                        // 获取LTE网络的频点、频段、频率
                        val cellIdentity = cell.cellIdentity as CellIdentityLte
                        val earfcn = cellIdentity.earfcn
                        val pci = cellIdentity.pci
                        val band = cellIdentity.bands.firstOrNull() ?: getLteBand(earfcn)
                        val frequency = getLteFrequency(earfcn)
                        
                        val rsrpRaw = strength.rsrp
                        val rsrqRaw = strength.rsrq
                        val sinrRaw = strength.rsrp
                        val cqiRaw = strength.cqi
                        
                        // 处理无效的LTE信号参数（2147483647是Int.MAX_VALUE，表示无效）
                        val rsrp = if (rsrpRaw == Int.MAX_VALUE) 0 else rsrpRaw
                        val rsrq = if (rsrqRaw == Int.MAX_VALUE) 0 else rsrqRaw
                        val sinr = if (sinrRaw == Int.MAX_VALUE) 0 else sinrRaw
                        val cqi = if (cqiRaw == Int.MAX_VALUE) 0 else cqiRaw
                        
                        lteSignal = LteSignalModel(
                            rsrp = rsrp,
                            rsrq = rsrq,
                            sinr = sinr,
                            cqi = cqi,
                            earfcn = earfcn,
                            pci = pci,
                            band = band,
                            frequency = frequency,
                            level = strength.level
                        )
                        
                        // 更新小区信息
                        cellInfoList.add(CellInfoModel(
                            index = index,
                            type = type,
                            dbm = strength.dbm,
                            level = strength.level,
                            band = band,
                            channel = earfcn,
                            frequency = frequency
                        ))
                    }
                }
                is CellInfoGsm -> {
                    // GSM网络信号参数
                    cell.cellSignalStrength?.let { strength ->
                        // 使用当前连接的小区作为主信号
                        if (signalLevel == 0 || signalLevel < strength.level) {
                            signalLevel = strength.level
                            dbm = strength.dbm
                        }
                        
                        // 获取GSM网络的频点、频段、频率
                        val cellIdentity = cell.cellIdentity as CellIdentityGsm
                        val arfcn = cellIdentity.arfcn
                        val band = getGsmBand(arfcn)
                        val frequency = getGsmFrequency(arfcn)
                        
                        val signalStrengthValueRaw = strength.dbm
                        val bitErrorRateRaw = 0
                        
                        // 处理无效的GSM信号参数（2147483647是Int.MAX_VALUE，表示无效）
                        val signalStrengthValue = if (signalStrengthValueRaw == Int.MAX_VALUE) 0 else signalStrengthValueRaw
                        val bitErrorRate = if (bitErrorRateRaw == Int.MAX_VALUE) 0 else bitErrorRateRaw
                        
                        gsmSignal = GsmSignalModel(
                            signalStrength = signalStrengthValue,
                            bitErrorRate = bitErrorRate,
                            arfcn = arfcn,
                            band = band,
                            frequency = frequency,
                            level = strength.level
                        )
                        
                        // 更新小区信息
                        cellInfoList.add(CellInfoModel(
                            index = index,
                            type = type,
                            dbm = strength.dbm,
                            level = strength.level,
                            band = band,
                            channel = arfcn,
                            frequency = frequency
                        ))
                    }
                }
                is CellInfoNr -> {
                    // 5G NR网络信号参数
                    cell.cellSignalStrength?.let { strength ->
                        if (strength is android.telephony.CellSignalStrengthNr) {
                            // 使用当前连接的小区作为主信号
                            if (signalLevel == 0 || signalLevel < strength.level) {
                                signalLevel = strength.level
                                // 获取SS参数
                                val ssRsrp = if (strength.ssRsrp == Int.MAX_VALUE) 0 else strength.ssRsrp
                                dbm = ssRsrp
                            }
                            
                            // 获取5G NR网络的频点、频段、频率
                            val cellIdentity = cell.cellIdentity as CellIdentityNr
                            val nrarfcn = cellIdentity.nrarfcn
                            val pci = cellIdentity.pci
                            val band = cellIdentity.bands.firstOrNull() ?: getNrBand(nrarfcn)
                            val frequency = getNrFrequency(nrarfcn)
                            
                            // 获取SS参数
                            val ssRsrp = if (strength.ssRsrp == Int.MAX_VALUE) 0 else strength.ssRsrp
                            val ssRsrq = if (strength.ssRsrq == Int.MAX_VALUE) 0 else strength.ssRsrq
                            val ssSinr = if (strength.ssSinr == Int.MAX_VALUE) 0 else strength.ssSinr
                            
                            nrSignal = NrSignalModel(
                                ssRsrp = ssRsrp,
                                ssRsrq = ssRsrq,
                                ssSinr = ssSinr,
                                nrarfcn = nrarfcn,
                                pci = pci,
                                band = band,
                                frequency = frequency,
                                level = strength.level
                            )
                            
                            // 更新小区信息
                            cellInfoList.add(CellInfoModel(
                                index = index,
                                type = type,
                                dbm = ssRsrp,
                                level = strength.level,
                                band = band,
                                channel = nrarfcn,
                                frequency = frequency
                            ))
                        }
                    }
                }
                is CellInfoWcdma -> {
                    // WCDMA网络信号参数
                    cell.cellSignalStrength?.let { strength ->
                        // 使用当前连接的小区作为主信号
                        if (signalLevel == 0 || signalLevel < strength.level) {
                            signalLevel = strength.level
                            dbm = strength.dbm
                            wcdmaSignalLevel = strength.level
                            wcdmaSignalDbm = strength.dbm
                        }
                        
                        // 获取WCDMA网络的频点、频段、频率
                        val cellIdentity = cell.cellIdentity as CellIdentityWcdma
                        val uarfcn = cellIdentity.uarfcn
                        val band = getWcdmaBand(uarfcn)
                        val frequency = getWcdmaFrequency(uarfcn)
                        
                        // 更新小区信息
                        cellInfoList.add(CellInfoModel(
                            index = index,
                            type = type,
                            dbm = strength.dbm,
                            level = strength.level,
                            band = band,
                            channel = uarfcn,
                            frequency = frequency
                        ))
                    }
                }
            }
        }
        
        // 如果allCellInfo没有获取到信号参数，尝试使用telephonyManager.signalStrength作为备选
        if (signalLevel == 0 && dbm == 0) {
            telephonyManager.signalStrength?.let { strength ->
                signalLevel = strength.level
                dbm = try {
                    // 使用反射尝试获取dbm属性
                    val dbmField = strength.javaClass.getDeclaredField("dbm")
                    dbmField.isAccessible = true
                    dbmField.getInt(strength)
                } catch (e: Exception) {
                    // 如果反射失败，使用默认值
                    0
                }
            }
        }

        return CellularSignalModel(
            signalLevel = signalLevel,
            dbm = dbm,
            networkType = getNetworkTypeDescription(networkType),
            networkTypeName = networkTypeName,
            gsmSignal = gsmSignal,
            lteSignal = lteSignal,
            nrSignal = nrSignal,
            cells = cellInfoList
        )
    }
    
    /**
     * 获取当前网络类型
     */
    @SuppressLint("MissingPermission")
    private fun getNetworkType(): Int {
        return telephonyManager.dataNetworkType
    }
    
    /**
     * 获取网络类型名称
     */
    private fun getNetworkTypeName(networkType: Int): String {
        return when (networkType) {
            TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE -> "2G"
            TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_HSDPA, 
            TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, 
            TelephonyManager.NETWORK_TYPE_HSPAP -> "3G"
            TelephonyManager.NETWORK_TYPE_LTE -> "4G"
            TelephonyManager.NETWORK_TYPE_NR -> "5G"
            else -> "未知"
        }
    }
    
    /**
     * 获取网络类型描述
     */
    private fun getNetworkTypeDescription(networkType: Int): String {
        return when (networkType) {
            TelephonyManager.NETWORK_TYPE_GPRS -> "2G GPRS"
            TelephonyManager.NETWORK_TYPE_EDGE -> "2G EDGE"
            TelephonyManager.NETWORK_TYPE_UMTS -> "3G UMTS"
            TelephonyManager.NETWORK_TYPE_HSDPA -> "3G HSDPA"
            TelephonyManager.NETWORK_TYPE_HSUPA -> "3G HSUPA"
            TelephonyManager.NETWORK_TYPE_HSPA -> "3G HSPA"
            TelephonyManager.NETWORK_TYPE_HSPAP -> "3G HSPA+"
            TelephonyManager.NETWORK_TYPE_LTE -> "4G LTE"
            TelephonyManager.NETWORK_TYPE_NR -> "5G NR"
            else -> "未知网络类型"
        }
    }
    
    /**
     * 获取小区类型
     */
    private fun getCellType(cell: CellInfo): String {
        return when (cell) {
            is CellInfoGsm -> "GSM"
            is CellInfoLte -> "LTE"
            is CellInfoNr -> "NR"
            is CellInfoWcdma -> "WCDMA"
            else -> cell.javaClass.simpleName.removeSuffix("Info")
        }
    }
    
    /**
     * 获取LTE频段 - 基于3GPP规范的准确计算
     */
    private fun getLteBand(earfcn: Int): Int {
        // 根据3GPP规范，LTE频段和EARFCN的映射关系
        // 参考：3GPP TS 36.101 V15.6.0 (2019-12)
        return when {
            // FDD频段
            earfcn in 0..599 -> 1     // n1: 2100 MHz
            earfcn in 600..1199 -> 2   // n2: 1900 MHz
            earfcn in 1200..1949 -> 3  // n3: 1800 MHz
            earfcn in 1950..2399 -> 4  // n4: AWS-1
            earfcn in 2400..2649 -> 5  // n5: 850 MHz
            earfcn in 2650..2749 -> 6  // n6: 800 MHz
            earfcn in 2750..3449 -> 7  // n7: 2600 MHz
            earfcn in 3450..3799 -> 8  // n8: 900 MHz
            earfcn in 3800..4149 -> 9  // n9: 1800 MHz
            earfcn in 4150..4749 -> 10 // n10: AWS-1
            earfcn in 4750..4949 -> 11 // n11: 1500 MHz
            earfcn in 5010..5179 -> 12 // n12: 700 MHz
            earfcn in 5180..5279 -> 13 // n13: 700 MHz
            earfcn in 5280..5379 -> 14 // n14: 700 MHz
            earfcn in 5730..5849 -> 17 // n17: 700 MHz
            earfcn in 5850..5999 -> 18 // n18: 800 MHz
            earfcn in 6000..6149 -> 19 // n19: 800 MHz
            earfcn in 6150..6449 -> 20 // n20: 800 MHz
            earfcn in 6450..6599 -> 21 // n21: 1500 MHz
            earfcn in 6600..7399 -> 22 // n22: 3500 MHz
            earfcn in 7500..7699 -> 23 // n23: 2100 MHz
            earfcn in 7700..8039 -> 24 // n24: 1600 MHz
            earfcn in 8040..8689 -> 25 // n25: 1900 MHz
            earfcn in 8690..9039 -> 26 // n26: 850 MHz
            earfcn in 9040..9209 -> 27 // n27: 800 MHz
            earfcn in 9210..9659 -> 28 // n28: 700 MHz
            earfcn in 9660..9769 -> 29 // n29: 700 MHz
            earfcn in 9770..9869 -> 30 // n30: 2300 MHz
            earfcn in 9870..9919 -> 31 // n31: 450 MHz
            earfcn in 9920..10359 -> 32 // n32: 1500 MHz
            
            // TDD频段
            earfcn in 37750..38249 -> 38 // n38: 2600 MHz
            earfcn in 38250..38649 -> 39 // n39: 1900 MHz
            earfcn in 38650..39649 -> 40 // n40: 2300 MHz
            earfcn in 39650..41589 -> 41 // n41: 2500 MHz
            earfcn in 41590..43589 -> 42 // n42: 3500 MHz
            earfcn in 43590..45589 -> 43 // n43: 3700 MHz
            earfcn in 45590..46589 -> 44 // n44: 700 MHz
            earfcn in 46590..47589 -> 45 // n45: 1500 MHz
            earfcn in 47590..48589 -> 46 // n46: 5 GHz
            earfcn in 48590..49589 -> 47 // n47: 6 GHz
            earfcn in 50300..51499 -> 48 // n48: CBRS
            earfcn in 51500..52549 -> 49 // n49: 3.5 GHz
            
            // 其他频段
            earfcn in 65536..66435 -> 71 // n71: 600 MHz
            earfcn in 66436..67235 -> 72 // n72: 450 MHz
            earfcn in 67236..67335 -> 73 // n73: 450 MHz
            earfcn in 67586..67635 -> 74 // n74: 1500 MHz
            earfcn in 67636..67685 -> 75 // n75: 1500 MHz
            earfcn in 67686..67735 -> 76 // n76: 1500 MHz
            earfcn in 67736..68585 -> 77 // n77: 3.5 GHz
            earfcn in 68586..74585 -> 78 // n78: 3.5 GHz
            earfcn in 74586..75585 -> 79 // n79: 4.9 GHz
            
            else -> 0
        }
    }
    
    /**
     * 获取LTE频率 (MHz)
     */
    private fun getLteFrequency(earfcn: Int): Double {
        // 基于3GPP TS 36.101规范计算频率
        // FDD频段的下行频率范围：EARFCN 0-35999
        // TDD频段的中心频率范围：EARFCN 36000-59999
        // 扩展TDD频段：EARFCN 60000+ (n71-n79等)
        return when {
            earfcn in 0..35999 -> {
                // FDD下行频率 (MHz)
                2110.0 + 0.010 * earfcn
            }
            earfcn in 36000..59999 -> {
                // TDD中心频率 (MHz)
                2300.0 + 0.010 * (earfcn - 36000)
            }
            earfcn in 60000..65535 -> {
                // TDD扩展频段A (MHz) - n71
                2500.0 + 0.010 * (earfcn - 60000)
            }
            earfcn in 65536..75585 -> {
                // TDD扩展频段B (MHz) - n72-n79等
                when {
                    // n77-n78: 3300-4200 MHz
                    earfcn in 67586..74585 -> {
                        3300.0 + 0.010 * (earfcn - 67586)
                    }
                    // n79: 4400-5000 MHz
                    earfcn in 74586..75585 -> {
                        4400.0 + 0.010 * (earfcn - 74586)
                    }
                    // 其他扩展频段
                    else -> {
                        2500.0 + 0.010 * (earfcn - 65536)
                    }
                }
            }
            else -> 0.0
        }
    }
    
    /**
     * 获取GSM频段 - 基于3GPP TS 45.005规范
     */
    private fun getGsmBand(arfcn: Int): Int {
        return when (arfcn) {
            // GSM 1800 (DCS) - 优先级高于其他频段
            in 512..885 -> 3   // DCS 1800: 1710-1785 MHz (UL), 1805-1880 MHz (DL)
            
            // GSM 900
            in 0..124 -> 8    // P-GSM 900: 890-915 MHz (UL), 935-960 MHz (DL)
            in 975..1023 -> 8  // E-GSM 900: 880-915 MHz (UL), 925-960 MHz (DL)
            
            // GSM 850 - 确保范围不重叠
            in 128..251 -> 5   // GSM 850: 824-849 MHz (UL), 869-894 MHz (DL)
            
            // 其他频段
            in 259..293 -> 9   // GSM 810
            in 306..340 -> 10  // GSM 1700
            in 350..380 -> 11  // GSM 450
            in 438..449 -> 12  // GSM 480
            in 1024..1045 -> 13 // GSM 710
            in 1059..1114 -> 14 // GSM 750
            in 1121..1142 -> 15 // GSM 850
            in 1143..1164 -> 16 // GSM 850
            in 1165..1187 -> 17 // GSM 850
            in 1200..1244 -> 18 // GSM 850
            in 1245..1279 -> 19 // GSM 800
            in 1280..1314 -> 20 // GSM 800
            in 1315..1328 -> 21 // GSM 800
            in 1329..1358 -> 22 // GSM 350
            in 1359..1399 -> 23 // GSM 450
            in 1400..1449 -> 24 // GSM 450
            in 1450..1499 -> 25 // GSM 450
            in 1500..1519 -> 26 // GSM 850
            in 1520..1559 -> 27 // GSM 800
            in 1560..1589 -> 28 // GSM 700
            in 1590..1619 -> 29 // GSM 700
            in 1620..1639 -> 30 // GSM 700
            else -> 0
        }
    }
    
    /**
     * 获取GSM频率 (MHz) - 基于3GPP TS 45.005规范
     */
    private fun getGsmFrequency(arfcn: Int): Double {
        return when (arfcn) {
            in 0..124 -> 935.0 + arfcn * 0.2 // P-GSM 900下行频率
            in 128..251 -> 869.0 + (arfcn - 128) * 0.2 // GSM 850下行频率
            in 512..885 -> 1805.0 + (arfcn - 512) * 0.2 // DCS 1800下行频率
            in 975..1023 -> 925.0 + (arfcn - 975) * 0.2 // E-GSM 900下行频率
            else -> 0.0
        }
    }
    
    /**
     * 获取WCDMA频段
     */
    private fun getWcdmaBand(uarfcn: Int): Int {
        return when (uarfcn) {
            in 10560..10839 -> 1
            in 9610..9879 -> 2
            in 10410..10559 -> 3
            in 10360..10399 -> 4
            in 10260..10359 -> 5
            in 10160..10259 -> 6
            in 10010..10159 -> 7
            in 9910..10009 -> 8
            in 9880..9909 -> 9
            in 9840..9879 -> 10
            in 2045..2395 -> 11
            in 1955..2044 -> 12
            in 1850..1944 -> 13
            in 1750..1844 -> 14
            in 1300..1359 -> 17
            in 1250..1299 -> 18
            in 1000..1249 -> 19
            in 600..999 -> 20
            in 500..599 -> 21
            in 300..499 -> 22
            in 0..299 -> 23
            in 2400..2649 -> 24
            in 2650..3449 -> 25
            in 3450..3799 -> 26
            in 3800..3999 -> 27
            in 4000..4149 -> 28
            in 4150..4359 -> 29
            in 4360..4559 -> 30
            in 4560..4639 -> 31
            in 4640..4739 -> 32
            else -> 0
        }
    }
    
    /**
     * 获取WCDMA频率 (MHz)
     */
    private fun getWcdmaFrequency(uarfcn: Int): Double {
        return 2110.0 + (uarfcn - 10000) * 0.2
    }
    
    /**
     * 获取5G NR频段 - 基于3GPP TS 38.101规范的精确映射
     */
    private fun getNrBand(nrarfcn: Int): Int {
        // 获取频率 (MHz) 用于更精确的频段计算
        val freq = getNrFrequency(nrarfcn) // 已经是MHz
        
        return when {
            // FR1 (Sub-6 GHz)
            nrarfcn in 0..599999 -> when {
                // 频分双工(FDD)频段
                freq in 2110.0..2170.0 -> 1     // n1: 2100 MHz
                freq in 1850.0..1910.0 -> 2     // n2: 1900 MHz
                freq in 1710.0..1785.0 -> 3     // n3: 1800 MHz
                freq in 1710.0..1755.0 -> 4     // n4: AWS
                freq in 824.0..849.0 -> 5       // n5: 850 MHz
                freq in 830.0..840.0 -> 6       // n6: 800 MHz
                freq in 2500.0..2570.0 -> 7     // n7: 2600 MHz
                freq in 880.0..915.0 -> 8       // n8: 900 MHz
                freq in 1749.9..1784.9 -> 9     // n9: 1800 MHz
                freq in 2110.0..2170.0 -> 10    // n10: 2100 MHz
                freq in 1427.9..1452.9 -> 11    // n11: 1500 MHz
                freq in 698.0..716.0 -> 12      // n12: 700 MHz
                freq in 777.0..787.0 -> 13      // n13: 700 MHz
                freq in 788.0..798.0 -> 14      // n14: 700 MHz
                freq in 1920.0..1980.0 -> 18    // n18: 800 MHz
                freq in 832.0..862.0 -> 19      // n19: 800 MHz
                freq in 832.0..862.0 -> 20      // n20: 800 MHz
                freq in 1447.9..1462.9 -> 21    // n21: 1500 MHz
                freq in 3410.0..3490.0 -> 22    // n22: 3500 MHz
                freq in 2180.0..2200.0 -> 23    // n23: 2100 MHz
                freq in 1625.5..1660.5 -> 24    // n24: 1600 MHz
                freq in 1930.0..1990.0 -> 25    // n25: 1900 MHz
                freq in 850.0..869.0 -> 26      // n26: 850 MHz
                freq in 807.0..824.0 -> 27      // n27: 800 MHz
                freq in 703.0..748.0 -> 28      // n28: 700 MHz
                freq in 717.0..728.0 -> 29      // n29: 700 MHz
                freq in 2305.0..2315.0 -> 30    // n30: 2300 MHz
                
                // 时分双工(TDD)频段
                freq in 2570.0..2620.0 -> 38    // n38: 2600 MHz
                freq in 1880.0..1920.0 -> 39    // n39: 1900 MHz
                freq in 2300.0..2400.0 -> 40    // n40: 2300 MHz
                freq in 2496.0..2690.0 -> 41    // n41: 2500 MHz
                freq in 3400.0..3600.0 -> 42    // n42: 3500 MHz
                freq in 3600.0..3800.0 -> 43    // n43: 3700 MHz
                freq in 703.0..803.0 -> 44      // n44: 700 MHz
                freq in 1427.0..1518.0 -> 45    // n45: 1500 MHz
                freq in 5150.0..5925.0 -> 46    // n46: 5 GHz
                freq in 5850.0..7125.0 -> 47    // n47: 6 GHz
                freq in 3550.0..3700.0 -> 48    // n48: CBRS
                freq in 1670.0..1680.0 -> 50    // n50: 1600 MHz
                freq in 1690.0..1700.0 -> 51    // n51: 1600 MHz
                freq in 1432.0..1437.0 -> 65    // n65: 1500 MHz
                freq in 2110.0..2200.0 -> 66    // n66: AWS-3
                freq in 717.0..728.0 -> 70      // n70: 700 MHz
                freq in 617.0..652.0 -> 71      // n71: 600 MHz
                freq in 410.0..430.0 -> 72      // n72: 450 MHz
                freq in 450.0..470.0 -> 73      // n73: 450 MHz
                freq in 1452.9..1490.9 -> 74    // n74: 1500 MHz
                freq in 1495.9..1510.9 -> 75    // n75: 1500 MHz
                freq in 1510.9..1525.9 -> 76    // n76: 1500 MHz
                
                // 额外的TDD频段 - 先匹配更具体的频段
                freq in 3300.0..3800.0 -> 78    // n78: 3.3-3.8 GHz (优先匹配)
                freq in 3800.0..4200.0 -> 77    // n77: 3.8-4.2 GHz
                freq in 4400.0..5000.0 -> 79    // n79: 4.4-5.0 GHz
                
                else -> 0
            }
            // FR2 (Millimeter Wave)
            nrarfcn in 600000..2016666 -> when {
                freq in 24250.0..27500.0 -> 257  // n257: 26 GHz
                freq in 27500.0..28350.0 -> 258  // n258: 28 GHz
                freq in 37000.0..40000.0 -> 259  // n259: 39 GHz
                freq in 39500.0..43500.0 -> 260  // n260: 41 GHz
                freq in 45500.0..49000.0 -> 261  // n261: 47 GHz
                freq in 27500.0..29500.0 -> 262  // n262: 28 GHz
                freq in 37000.0..38600.0 -> 263  // n263: 37 GHz
                freq in 38600.0..40000.0 -> 264  // n264: 39 GHz
                freq in 40500.0..42500.0 -> 265  // n265: 41 GHz
                freq in 42500.0..43500.0 -> 266  // n266: 42 GHz
                freq in 45500.0..47000.0 -> 267  // n267: 45 GHz
                freq in 47200.0..50200.0 -> 268  // n268: 49 GHz
                freq in 50400.0..52600.0 -> 271  // n271: 52 GHz
                
                else -> 0
            }
            
            else -> 0
        }
    }
    
    /**
     * 获取5G NR频率 (MHz) - 基于3GPP TS 38.101规范
     */
    private fun getNrFrequency(nrarfcn: Int): Double {
        return when (nrarfcn) {
            in 0..599999 -> 0.005 * nrarfcn + 0.1 // FR1: f = 0.005*N + 0.1 MHz
            in 600000..2016666 -> 0.015 * (nrarfcn - 600000) + 2457.6 // FR2: f = 0.015*(N-600000) + 2457.6 MHz
            else -> 0.0
        }
    }
}

