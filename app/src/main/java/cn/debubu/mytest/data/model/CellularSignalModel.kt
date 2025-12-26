package cn.debubu.mytest.data.model

import android.telephony.CellInfo
import android.telephony.CellSignalStrength

/**
 * 蜂窝网络信号强度主模型
 */
data class CellularSignalModel(
    // 通用信号参数
    val signalLevel: Int = 0, // 信号强度等级 (0-4)
    val dbm: Int = 0, // 信号强度 (dBm)
    
    // 网络类型信息
    val networkType: String = "未知", // 网络类型描述 (2G/3G/4G/5G)
    val networkTypeName: String = "", // 网络技术名称 (GSM/LTE/NR等)
    
    // 按网络类型划分的信号参数
    val gsmSignal: GsmSignalModel? = null,
    val lteSignal: LteSignalModel? = null,
    val nrSignal: NrSignalModel? = null,
    
    // 小区信息列表
    val cells: List<CellInfoModel> = emptyList()
)

/**
 * GSM网络信号参数
 */
data class GsmSignalModel(
    val signalStrength: Int = 0, // GSM信号强度 (0-31)
    val bitErrorRate: Int = 0, // GSM误码率 (0-7)
    val arfcn: Int = 0, // GSM绝对无线频道号
    val band: Int = 0, // GSM频段
    val frequency: Double = 0.0, // GSM频率 (MHz)
    val level: Int = 0 // GSM信号等级
)

/**
 * LTE网络信号参数
 */
data class LteSignalModel(
    val rsrp: Int = 0, // 参考信号接收功率 (dBm)
    val rsrq: Int = 0, // 参考信号接收质量 (dBm)
    val sinr: Int = 0, // 信号与干扰加噪声比 (dB)
    val cqi: Int = 0, // 信道质量指示
    val earfcn: Int = 0, // LTE绝对无线频道号
    val pci: Int = 0, // LTE物理小区标识
    val band: Int = 0, // LTE频段
    val frequency: Double = 0.0, // LTE频率 (MHz)
    val level: Int = 0 // LTE信号等级
)

/**
 * 5G NR网络信号参数
 */
data class NrSignalModel(
    val ssRsrp: Int = 0, // SS参考信号接收功率 (dBm)
    val ssRsrq: Int = 0, // SS参考信号接收质量 (dBm)
    val ssSinr: Int = 0, // SS信号与干扰加噪声比 (dB)
    val nrarfcn: Int = 0, // NR绝对无线频道号
    val pci: Int = 0, // NR物理小区标识
    val band: Int = 0, // 5G NR频段
    val frequency: Double = 0.0, // 5G NR频率 (MHz)
    val level: Int = 0 // 5G NR信号等级
)

/**
 * 单个小区信息
 */
data class CellInfoModel(
    val index: Int, // 小区索引
    val type: String, // 小区类型 (GSM/LTE/NR等)
    val dbm: Int, // 小区信号强度 (dBm)
    val level: Int, // 小区信号等级
    val band: Int = 0, // 频段
    val channel: Int = 0, // 频点
    val frequency: Double = 0.0 // 频率 (MHz)
)
