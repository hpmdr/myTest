package cn.debubu.mytest.ui.screen.signal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SignalStrengthIndicator(
    signalLevel: Int,
    modifier: Modifier = Modifier,
    barWidth: Dp = 4.dp,
    barHeight: Dp = 16.dp,
    barSpacing: Dp = 2.dp,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
) {
    val clampedLevel = signalLevel.coerceIn(0, 4)
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(barSpacing)
    ) {
        repeat(4) { index ->
            val isActive = index < clampedLevel
            val barHeightFactor = (index + 1).toFloat() / 4
            
            SignalBar(
                isActive = isActive,
                width = barWidth,
                height = barHeight * barHeightFactor,
                activeColor = activeColor,
                inactiveColor = inactiveColor
            )
        }
    }
}

@Composable
private fun SignalBar(
    isActive: Boolean,
    width: Dp,
    height: Dp,
    activeColor: Color,
    inactiveColor: Color
) {
    Spacer(
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(2.dp))
            .background(if (isActive) activeColor else inactiveColor)
    )
}
