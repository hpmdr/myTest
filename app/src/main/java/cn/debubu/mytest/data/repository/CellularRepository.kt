package cn.debubu.mytest.data.repository

import android.os.Build
import android.telephony.CellInfo
import android.telephony.TelephonyManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CellularRepository @Inject constructor(
    private val telephonyManager: TelephonyManager
) {

    fun fetchSignalInfo(): List<String> {
        val result = mutableListOf<String>()

        // SignalStrength overview (API 28+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            telephonyManager.signalStrength?.let { strength ->
                result.add("系统分级 level (0-4): ${strength.level}")
                val dbm = when (strength) {
                    is android.telephony.CellSignalStrengthLte -> strength.dbm
                    is android.telephony.CellSignalStrengthGsm -> strength.dbm
                    is android.telephony.CellSignalStrengthWcdma -> strength.dbm
                    is android.telephony.CellSignalStrengthCdma -> strength.dbm
                    is android.telephony.CellSignalStrengthNr -> strength.csiRsrp // 5G NR RSRP
                    else -> 0
                }
                val exampleDbm = strength.cellSignalStrengths.firstOrNull()?.dbm
                exampleDbm?.let { dbm -> result.add("示例小区 dBm: $dbm dBm") }
            }
        } else {
            result.add("系统版本过低，无法读取整体信号强度")
        }

        // Per cell info (API 24+ available, but signalStrength read guarded)
        val cells: List<CellInfo>? = telephonyManager.allCellInfo
        cells?.forEachIndexed { index, cell ->
            val s = cell.cellSignalStrength
            val type = cell.javaClass.simpleName.removeSuffix("Info")
            result.add("Cell#$index 类型:$type dBm:${s.dbm} level:${s.level}")

        }

        return result
    }
}

