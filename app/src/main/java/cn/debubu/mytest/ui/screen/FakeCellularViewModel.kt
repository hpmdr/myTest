import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FakeCellularViewModel : ViewModel() {

    // 当前选中的 SIM 卡槽 (1 或 2)
    var activeSim by mutableIntStateOf(1)
        private set

    // 内部私有数据源
    private val _sim1State =
        MutableStateFlow(SignalState(operatorName = "中国移动", networkType = "5G NR"))
    private val _sim2State = MutableStateFlow(
        SignalState(
            operatorName = "中国联通",
            networkType = "4G LTE",
            dbm = -108,
            progress = 0.25f
        )
    )

    // 暴露给 UI 的统一状态流
    val currentSignalState: StateFlow<SignalState> = snapshotFlow { activeSim }
        .flatMapLatest { sim -> if (sim == 1) _sim1State else _sim2State }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SignalState())

    init {
        // 启动模拟信号更新任务
        startSignalSimulation()
    }

    fun switchSim(simId: Int) {
        activeSim = simId
    }

    private fun startSignalSimulation() {
        viewModelScope.launch {
            while (true) {
                updateMockData(_sim1State, -70, -95)
                updateMockData(_sim2State, -90, -115)
                delay(3000) // 每 3 秒更新一次
            }
        }
    }

    private fun updateMockData(flow: MutableStateFlow<SignalState>, max: Int, min: Int) {
        val newDbm = (min..max).random()
        val newProgress = ((newDbm + 120) / 60f).coerceIn(0f, 1f)
        flow.value = flow.value.copy(
            dbm = newDbm,
            progress = newProgress,
            rsrp = "${newDbm - 8} dBm",
            rssi = "${newDbm + 20} dBm"
        )
    }
}
