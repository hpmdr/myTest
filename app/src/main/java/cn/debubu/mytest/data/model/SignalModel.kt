/**
 * 信号状态数据类
 * 包含 UI 所需的所有原始数据
 */
data class SignalState(
    val dbm: Int = -95,
    val progress: Float = 0.4f,
    val operatorName: String = "未知",
    val networkType: String = "5G NR",
    val rsrp: String = "-105 dBm",
    val rsrq: String = "-12 dB",
    val sinr: String = "15.0 dB",
    val rssi: String = "-68 dBm",
    val pci: String = "382",
    val earfcn: String = "39150",
    val band: String = "n78",
    val tac: String = "29810"
)