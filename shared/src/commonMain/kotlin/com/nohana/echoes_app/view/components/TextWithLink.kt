package com.nohana.echoes_app.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nohana.echoes_app.ui.theme.DarkBlue

@Composable
fun TextWithLink(
    prefixText: String = "",
    linkText: String,
    sufixText: String = "",
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (prefixText.isNotBlank()) {
            Text(
                text = prefixText,
                color = Color.Black,
                fontSize = 12.sp
            )
        }

        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
            TextButton(
                onClick = onClick,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
            ) {
                Text(
                    text = linkText,
                    color = DarkBlue,
                    fontSize = 12.sp
                )
            }
        }

        if (sufixText.isNotBlank()) {
            Text(
                text = sufixText,
                color = Color.Black,
                fontSize = 12.sp
            )
        }

    }
}
