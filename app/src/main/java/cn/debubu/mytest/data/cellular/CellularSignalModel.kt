package cn.debubu.mytest.data.cellular

import android.telephony.CellInfo
import android.telephony.TelephonyManager

/**
 * 蜂窝信号信息数据类
 * 包含从 Android TelephonyManager API 获取的原始信号数据
 * 
 * @param simSlotId SIM 卡槽 ID (0 或 1)
 * @param operatorName 运营商名称
 * @param networkType 网络类型 (如 "5G NR", "4G LTE", "3G" 等)
 * @param dbm 信号强度 (dBm)
 * @param rsrp 参考信号接收功率 (dBm)
 * @param rsrq 参考信号接收质量 (dB)
 * @param sinr 信号与干扰加噪声比 (dB)
 * @param rssi 接收信号强度指示 (dBm)
 * @param pci 物理小区标识
 * @param earfcn E-UTRA 绝对无线频率信道号
 * @param band 频段 (如 "n78", "B3" 等)
 * @param tac 跟踪区域码
 * @param isPrimary 是否为主卡
 */
data class CellularSignalInfo(
    val simSlotId: Int = 0,
    val operatorName: String = "未知",
    val networkType: String = "未知",
    val dbm: Int = -120,
    val rsrp: Int = -120,
    val rsrq: Int = -20,
    val sinr: Int = -20,
    val rssi: Int = -120,
    val pci: Int = 0,
    val earfcn: Int = 0,
    val band: String = "",
    val tac: Int = 0,
    val isPrimary: Boolean = false
) {
    companion object {
        /**
         * 从 CellInfo 创建 CellularSignalInfo
         */
        fun fromCellInfo(cellInfo: CellInfo, simSlotId: Int, isPrimary: Boolean): CellularSignalInfo {
            val signalStrength = when (cellInfo) {
                is android.telephony.CellInfoLte -> {
                    val signalStrengthLte = cellInfo.cellSignalStrength
                    CellularSignalInfo(
                        simSlotId = simSlotId,
                        networkType = "4G LTE",
                        dbm = signalStrengthLte.dbm,
                        rsrp = signalStrengthLte.rsrp,
                        rsrq = signalStrengthLte.rsrq,
                        rssi = signalStrengthLte.rssi,
                        pci = cellInfo.cellIdentity.pci,
                        earfcn = cellInfo.cellIdentity.earfcn,
                        band = "B${cellInfo.cellIdentity.earfcn / 1000}",
                        tac = cellInfo.cellIdentity.tac,
                        isPrimary = isPrimary
                    )
                }
                is android.telephony.CellInfoNr -> {
                    val signalStrengthNr = cellInfo.cellSignalStrength as android.telephony.CellSignalStrengthNr
                    val cellIdentityNr = cellInfo.cellIdentity as android.telephony.CellIdentityNr
                    CellularSignalInfo(
                        simSlotId = simSlotId,
                        networkType = "5G NR",
                        dbm = signalStrengthNr.dbm,
                        rsrp = signalStrengthNr.ssRsrp,
                        rsrq = signalStrengthNr.ssRsrq,
                        sinr = signalStrengthNr.ssSinr,
                        pci = cellIdentityNr.pci,
                        earfcn = cellIdentityNr.nrarfcn,
                        band = "n${cellIdentityNr.nrarfcn / 1000}",
                        tac = cellIdentityNr.tac,
                        isPrimary = isPrimary
                    )
                }
                is android.telephony.CellInfoWcdma -> {
                    val signalStrengthWcdma = cellInfo.cellSignalStrength
                    CellularSignalInfo(
                        simSlotId = simSlotId,
                        networkType = "3G WCDMA",
                        dbm = signalStrengthWcdma.dbm,
                        pci = cellInfo.cellIdentity.psc,
                        earfcn = cellInfo.cellIdentity.uarfcn,
                        tac = cellInfo.cellIdentity.lac,
                        isPrimary = isPrimary
                    )
                }
                is android.telephony.CellInfoGsm -> {
                    val signalStrengthGsm = cellInfo.cellSignalStrength
                    CellularSignalInfo(
                        simSlotId = simSlotId,
                        networkType = "2G GSM",
                        dbm = signalStrengthGsm.dbm,
                        rssi = signalStrengthGsm.rssi,
                        pci = cellInfo.cellIdentity.cid,
                        tac = cellInfo.cellIdentity.lac,
                        isPrimary = isPrimary
                    )
                }
                else -> CellularSignalInfo(simSlotId = simSlotId, isPrimary = isPrimary)
            }
            return signalStrength
        }

        /**
         * 获取网络类型字符串
         */
        fun getNetworkTypeName(networkType: Int): String {
            return when (networkType) {
                TelephonyManager.NETWORK_TYPE_NR -> "5G NR"
                TelephonyManager.NETWORK_TYPE_LTE -> "4G LTE"
                TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "3G WCDMA"
                TelephonyManager.NETWORK_TYPE_HSPA,
                TelephonyManager.NETWORK_TYPE_HSPAP,
                TelephonyManager.NETWORK_TYPE_HSUPA,
                TelephonyManager.NETWORK_TYPE_HSDPA,
                TelephonyManager.NETWORK_TYPE_UMTS -> "3G WCDMA"
                TelephonyManager.NETWORK_TYPE_GSM,
                TelephonyManager.NETWORK_TYPE_GPRS,
                TelephonyManager.NETWORK_TYPE_EDGE -> "2G GSM"
                else -> "未知"
            }
        }
    }
}

/**
 * 邻小区信息数据类
 * 包含邻小区的信号和网络信息
 * 
 * @param pci 物理小区标识
 * @param rsrp 参考信号接收功率 (dBm)
 * @param rsrq 参考信号接收质量 (dB)
 * @param sinr 信号与干扰加噪声比 (dB)
 * @param rssi 接收信号强度指示 (dBm)
 * @param earfcn E-UTRA 绝对无线频率信道号
 * @param band 频段
 * @param isServing 是否为服务小区
 */
data class NeighborCellInfo(
    val pci: Int = 0,
    val rsrp: Int = -120,
    val rsrq: Int = -20,
    val sinr: Int = -20,
    val rssi: Int = -120,
    val earfcn: Int = 0,
    val band: String = "",
    val isServing: Boolean = false
) {
    companion object {
        /**
         * 从 CellInfo 创建 NeighborCellInfo
         */
        fun fromCellInfo(cellInfo: CellInfo, isServing: Boolean = false): NeighborCellInfo {
            return when (cellInfo) {
                is android.telephony.CellInfoLte -> {
                    val signalStrengthLte = cellInfo.cellSignalStrength
                    NeighborCellInfo(
                        pci = cellInfo.cellIdentity.pci,
                        rsrp = signalStrengthLte.rsrp,
                        rsrq = signalStrengthLte.rsrq,
                        rssi = signalStrengthLte.rssi,
                        earfcn = cellInfo.cellIdentity.earfcn,
                        band = "B${cellInfo.cellIdentity.earfcn / 1000}",
                        isServing = isServing
                    )
                }
                is android.telephony.CellInfoNr -> {
                    val signalStrengthNr = cellInfo.cellSignalStrength as android.telephony.CellSignalStrengthNr
                    val cellIdentityNr = cellInfo.cellIdentity as android.telephony.CellIdentityNr
                    NeighborCellInfo(
                        pci = cellIdentityNr.pci,
                        rsrp = signalStrengthNr.ssRsrp,
                        rsrq = signalStrengthNr.ssRsrq,
                        sinr = signalStrengthNr.ssSinr,
                        earfcn = cellIdentityNr.nrarfcn,
                        band = "n${cellIdentityNr.nrarfcn / 1000}",
                        isServing = isServing
                    )
                }
                else -> NeighborCellInfo(isServing = isServing)
            }
        }
    }
}

/**
 * 完整的蜂窝数据信息
 * 包含主服务小区和所有邻小区信息
 * 
 * @param servingCell 服务小区信息
 * @param neighborCells 邻小区列表
 * @param timestamp 数据时间戳
 */
data class CellularData(
    val servingCell: CellularSignalInfo? = null,
    val neighborCells: List<NeighborCellInfo> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * GSM 信号信息数据类
 * 包含 GSM 网络的详细信号参数
 * 
 * @param signalStrength GSM 信号强度 (0-31)
 * @param bitErrorRate GSM 误码率 (0-7)
 * @param level GSM 信号等级
 * @param band GSM 频段
 * @param arfcn GSM 绝对无线频道号
 * @param frequency GSM 频率 (MHz)
 */
data class GsmSignalModel(
    val signalStrength: Int = 0,
    val bitErrorRate: Int = 0,
    val level: Int = 0,
    val band: String = "",
    val arfcn: Int = 0,
    val frequency: Double = 0.0
)

/**
 * LTE 信号信息数据类
 * 包含 LTE 网络的详细信号参数
 * 
 * @param rsrp 参考信号接收功率 (dBm)
 * @param rsrq 参考信号接收质量 (dB)
 * @param sinr 信号与干扰加噪声比 (dB)
 * @param cqi 信道质量指示
 * @param level LTE 信号等级
 * @param band LTE 频段
 * @param earfcn LTE 绝对无线频道号
 * @param frequency LTE 频率 (MHz)
 * @param pci LTE 物理小区标识
 */
data class LteSignalModel(
    val rsrp: Int = -120,
    val rsrq: Int = -20,
    val sinr: Int = -20,
    val cqi: Int = 0,
    val level: Int = 0,
    val band: String = "",
    val earfcn: Int = 0,
    val frequency: Double = 0.0,
    val pci: Int = 0
)

/**
 * 5G NR 信号信息数据类
 * 包含 5G NR 网络的详细信号参数
 * 
 * @param ssRsrp SS 参考信号接收功率 (dBm)
 * @param ssRsrq SS 参考信号接收质量 (dB)
 * @param ssSinr SS 信号与干扰加噪声比 (dB)
 * @param level 5G NR 信号等级
 * @param band 5G NR 频段
 * @param nrarfcn 5G NR 绝对无线频道号
 * @param frequency 5G NR 频率 (MHz)
 * @param pci 5G NR 物理小区标识
 */
data class NrSignalModel(
    val ssRsrp: Int = -120,
    val ssRsrq: Int = -20,
    val ssSinr: Int = -20,
    val level: Int = 0,
    val band: String = "",
    val nrarfcn: Int = 0,
    val frequency: Double = 0.0,
    val pci: Int = 0
)