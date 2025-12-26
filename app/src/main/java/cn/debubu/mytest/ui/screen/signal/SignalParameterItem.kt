package cn.debubu.mytest.ui.screen.signal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SignalParameterItem(
    name: String,
    value: String,
    description: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row {
            Text(
                text = "$name: ",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = MaterialTheme.typography.bodyMedium.fontWeight
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (!description.isNullOrEmpty()) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, start = 8.dp)
            )
        }
    }
}
