package cn.debubu.mytest.ui.cellular

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.debubu.mytest.data.cellular.CellularData
import cn.debubu.mytest.data.cellular.CellularRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CellularViewModel @Inject constructor(
    private val repository: CellularRepository
) : ViewModel() {

    var activeSim by mutableIntStateOf(1)
        private set

    private val _sim1Data = mutableStateOf<CellularData?>(null)
    private val _sim2Data = mutableStateOf<CellularData?>(null)

    private val _currentSignalState = mutableStateOf(SignalState())
    val currentSignalState: State<SignalState> = _currentSignalState

    init {
        startDataCollection()
    }

    private fun startDataCollection() {
        viewModelScope.launch {
            repository.getDualSimCellularDataFlow().collect { (sim1Data, sim2Data) ->
                _sim1Data.value = sim1Data
                _sim2Data.value = sim2Data

                updateCurrentSignalState()

                Timber.d("SIM 1: ${sim1Data.servingCell?.operatorName}, 邻小区: ${sim1Data.neighborCells.size}")
                Timber.d("SIM 2: ${sim2Data.servingCell?.operatorName}, 邻小区: ${sim2Data.neighborCells.size}")
            }
        }
    }

    private fun updateCurrentSignalState() {
        val currentData = if (activeSim == 1) _sim1Data.value else _sim2Data.value
        val signalState = currentData?.servingCell?.let { cell ->
            SignalState(
                dbm = cell.dbm,
                progress = ((cell.dbm + 120) / 60f).coerceIn(0f, 1f),
                operatorName = cell.operatorName,
                networkType = cell.networkType,
                rsrp = "${cell.rsrp} dBm",
                rsrq = "${cell.rsrq} dB",
                sinr = if (cell.sinr != -20) "${cell.sinr} dB" else "N/A",
                rssi = if (cell.rssi != -120) "${cell.rssi} dBm" else "N/A",
                pci = cell.pci.toString(),
                earfcn = cell.earfcn.toString(),
                band = cell.band,
                tac = if (cell.tac != 0) cell.tac.toString() else "N/A"
            )
        } ?: SignalState()

        _currentSignalState.value = signalState
    }

    fun switchSim(simId: Int) {
        activeSim = simId
        updateCurrentSignalState()
    }

    fun getCurrentNeighborCells(): List<NeighborCellUiModel> {
        val currentData = if (activeSim == 1) _sim1Data.value else _sim2Data.value
        return currentData?.neighborCells?.map { neighbor ->
            NeighborCellUiModel(
                pci = neighbor.pci,
                rsrp = "${neighbor.rsrp} dBm",
                rsrq = if (neighbor.rsrq != -20) "${neighbor.rsrq} dB" else "N/A",
                sinr = if (neighbor.sinr != -20) "${neighbor.sinr} dB" else "N/A",
                rssi = if (neighbor.rssi != -120) "${neighbor.rssi} dBm" else "N/A",
                band = neighbor.band,
                signalStrength = calculateSignalStrength(neighbor.rsrp)
            )
        } ?: emptyList()
    }

    private fun calculateSignalStrength(rsrp: Int): Float {
        return ((rsrp + 120) / 60f).coerceIn(0f, 1f)
    }
}

data class NeighborCellUiModel(
    val pci: Int,
    val rsrp: String,
    val rsrq: String,
    val sinr: String,
    val rssi: String,
    val band: String,
    val signalStrength: Float
)