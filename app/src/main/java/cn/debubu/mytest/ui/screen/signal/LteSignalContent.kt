package cn.debubu.mytest.ui.screen.signal

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cn.debubu.mytest.data.model.LteSignalModel

@Composable
fun LteSignalContent(signal: LteSignalModel) {
    Column {
        SignalParameterItem(
            name = "参考信号接收功率 (RSRP)",
            value = "${signal.rsrp} dBm",
            description = "LTE网络的参考信号接收功率"
        )
        SignalParameterItem(
            name = "参考信号接收质量 (RSRQ)",
            value = "${signal.rsrq} dBm",
            description = "LTE网络的参考信号接收质量"
        )
        SignalParameterItem(
            name = "信号与干扰加噪声比 (SINR)",
            value = "${signal.sinr} dB",
            description = "LTE网络的信号与干扰加噪声比"
        )
        SignalParameterItem(
            name = "信道质量指示 (CQI)",
            value = "${signal.cqi}",
            description = "LTE网络的信道质量指示"
        )
        SignalParameterItem(
            name = "LTE信号等级",
            value = "${signal.level}",
            description = "LTE网络的信号等级"
        )
        SignalParameterItem(
            name = "LTE频段",
            value = "${signal.band}",
            description = "LTE网络的频段"
        )
        SignalParameterItem(
            name = "LTE频点 (EARFCN)",
            value = "${signal.earfcn}",
            description = "LTE绝对无线频道号"
        )
        SignalParameterItem(
            name = "LTE频率",
            value = "${signal.frequency} MHz",
            description = "LTE网络的频率"
        )
        SignalParameterItem(
            name = "LTE物理小区标识 (PCI)",
            value = "${signal.pci}",
            description = "LTE物理小区标识"
        )
    }
}
