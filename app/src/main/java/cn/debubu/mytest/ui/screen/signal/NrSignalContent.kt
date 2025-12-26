package cn.debubu.mytest.ui.screen.signal

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cn.debubu.mytest.data.model.NrSignalModel

@Composable
fun NrSignalContent(signal: NrSignalModel) {
    Column {
        SignalParameterItem(
            name = "SS参考信号接收功率 (RSRP)",
            value = "${signal.ssRsrp} dBm",
            description = "5G NR网络的SS参考信号接收功率"
        )
        SignalParameterItem(
            name = "SS参考信号接收质量 (RSRQ)",
            value = "${signal.ssRsrq} dBm",
            description = "5G NR网络的SS参考信号接收质量"
        )
        SignalParameterItem(
            name = "SS信号与干扰加噪声比 (SINR)",
            value = "${signal.ssSinr} dB",
            description = "5G NR网络的SS信号与干扰加噪声比"
        )
        
        SignalParameterItem(
            name = "5G NR信号等级",
            value = "${signal.level}",
            description = "5G NR网络的信号等级"
        )
        SignalParameterItem(
            name = "5G NR频段",
            value = "${signal.band}",
            description = "5G NR网络的频段"
        )
        SignalParameterItem(
            name = "5G NR频点 (NR-ARFCN)",
            value = "${signal.nrarfcn}",
            description = "5G NR绝对无线频道号"
        )
        SignalParameterItem(
            name = "5G NR频率",
            value = "${signal.frequency} MHz",
            description = "5G NR网络的频率"
        )
        SignalParameterItem(
            name = "5G NR物理小区标识 (PCI)",
            value = "${signal.pci}",
            description = "5G NR物理小区标识"
        )
    }
}
