package cn.debubu.mytest.ui.cellular

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun CellularPage(viewModel: CellularViewModel = hiltViewModel()) {
    val state by viewModel.currentSignalState

    val themeColor = remember(state.dbm) {
        when {
            state.dbm > -85 -> Color(0xFF386B28)
            state.dbm > -105 -> Color(0xFF6C5D00)
            else -> Color(0xFFBA1A1A)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        SimSwitcher(
            activeSim = viewModel.activeSim,
            onSimSelected = { viewModel.switchSim(it) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        SignalGaugeCard(state = state, color = themeColor)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "网络详细指标",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )

        MetricsGrid(state = state)

        Spacer(modifier = Modifier.height(24.dp))

        val neighborCellsList = viewModel.getCurrentNeighborCells()
        if (neighborCellsList.isNotEmpty()) {
            NeighborCellsSection(neighborCells = neighborCellsList)
        }
    }
}


@Composable
fun SimSwitcher(activeSim: Int, onSimSelected: (Int) -> Unit) {
    Surface(
        tonalElevation = 2.dp,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            val modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
            SimTabItem(modifier, "SIM 1 (主卡)", activeSim == 1) { onSimSelected(1) }
            SimTabItem(modifier, "SIM 2 (副卡)", activeSim == 2) { onSimSelected(2) }
        }
    }
}

@Composable
fun SimTabItem(modifier: Modifier, label: String, selected: Boolean, onClick: () -> Unit) {
    val containerColor =
        if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val contentColor =
        if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .background(containerColor, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun SignalGaugeCard(state: SignalState, color: Color) {
    val animatedProgress by animateFloatAsState(
        targetValue = state.progress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                // 圆环 Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = color.copy(alpha = 0.1f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // 中央文本
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "SIGNAL LEVEL",
                        style = MaterialTheme.typography.labelSmall,
                        color = color.copy(alpha = 0.6f)
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            "${state.dbm}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                        Text(
                            "dBm",
                            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp),
                            color = color.copy(alpha = 0.5f)
                        )
                    }
                    // 运营商标签
                    Surface(
                        color = color.copy(alpha = 0.12f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "${state.operatorName} ${state.networkType}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricsGrid(state: SignalState) {
    val items = listOf(
        "RSRP" to state.rsrp to Icons.Default.SignalCellularAlt,
        "RSRQ" to state.rsrq to Icons.Default.Timeline,
        "SINR" to state.sinr to Icons.Default.Wifi,
        "RSSI" to state.rssi to Icons.Default.Bolt,
        "PCI" to state.pci to Icons.Default.Memory,
        "EARFCN" to state.earfcn to Icons.Default.Layers,
        "Band" to state.band to Icons.Default.Radio,
        "TAC" to state.tac to Icons.Default.Map
    )

    // 使用 Column + Row 模拟网格，因为在 Scrollable 中嵌入 LazyGrid 会有冲突
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        for (i in items.indices step 2) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricItem(
                    items[i].first.first,
                    items[i].first.second,
                    items[i].second,
                    Modifier.weight(1f)
                )
                if (i + 1 < items.size) {
                    MetricItem(
                        items[i + 1].first.first,
                        items[i + 1].first.second,
                        items[i + 1].second,
                        Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: String, icon: ImageVector, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun NeighborCellsSection(neighborCells: List<NeighborCellUiModel>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.SignalCellularAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "邻小区信息",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ) {
                    Text(
                        text = "${neighborCells.size}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                neighborCells.forEach { cell ->
                    NeighborCellItem(cell = cell)
                }
            }
        }
    }
}

@Composable
fun NeighborCellItem(cell: NeighborCellUiModel) {
    val signalColor = when {
        cell.signalStrength > 0.6f -> Color(0xFF386B28)
        cell.signalStrength > 0.3f -> Color(0xFF6C5D00)
        else -> Color(0xFFBA1A1A)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "PCI: ${cell.pci}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(8.dp))
                    if (cell.band.isNotEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = cell.band,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SignalMetric("RSRP", cell.rsrp)
                    SignalMetric("RSRQ", cell.rsrq)
                    SignalMetric("SINR", cell.sinr)
                }
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = signalColor.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SignalCellularAlt,
                    contentDescription = null,
                    tint = signalColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun SignalMetric(label: String, value: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}