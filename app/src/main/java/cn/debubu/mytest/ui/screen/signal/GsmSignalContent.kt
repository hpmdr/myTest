package cn.debubu.mytest.ui.screen.signal

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import cn.debubu.mytest.data.cellular.GsmSignalModel

@Composable
fun GsmSignalContent(signal: GsmSignalModel) {
    Column {
        SignalParameterItem(
            name = "GSM信号强度",
            value = "${signal.signalStrength} (0-31)",
            description = "GSM网络的信号强度值"
        )
        SignalParameterItem(
            name = "GSM误码率",
            value = "${signal.bitErrorRate} (0-7)",
            description = "GSM网络的误码率"
        )
        SignalParameterItem(
            name = "GSM信号等级",
            value = "${signal.level}",
            description = "GSM网络的信号等级"
        )
        SignalParameterItem(
            name = "GSM频段",
            value = "${signal.band}",
            description = "GSM网络的频段"
        )
        SignalParameterItem(
            name = "GSM频点 (ARFCN)",
            value = "${signal.arfcn}",
            description = "GSM绝对无线频道号"
        )
        SignalParameterItem(
            name = "GSM频率",
            value = "${signal.frequency} MHz",
            description = "GSM网络的频率"
        )
    }
}
