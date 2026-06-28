package com.mihara.billio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Icon
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp

data class SwipeAction(
    val icon: ImageVector,
    val background: Color,
    val onTrigger: () -> Unit
)

/**
 * A card that reveals a colored icon background while swiping. Triggering does not
 * remove the item directly — [SwipeAction.onTrigger] should open a confirmation, so
 * the row snaps back on release. Haptic feedback fires on each trigger.
 */
@Composable
fun SwipeableCard(
    rightAction: SwipeAction? = null,   // revealed when swiping left-to-right (start->end)
    leftAction: SwipeAction? = null,    // revealed when swiping right-to-left (end->start)
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> rightAction?.let {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress); it.onTrigger()
                }
                SwipeToDismissBoxValue.EndToStart -> leftAction?.let {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress); it.onTrigger()
                }
                SwipeToDismissBoxValue.Settled -> Unit
            }
            false // never auto-dismiss; confirmation handles the real action
        }
    )

    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        enableDismissFromStartToEnd = rightAction != null,
        enableDismissFromEndToStart = leftAction != null,
        backgroundContent = {
            val action = when (state.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> rightAction
                SwipeToDismissBoxValue.EndToStart -> leftAction
                else -> null
            }
            val alignment = if (state.dismissDirection == SwipeToDismissBoxValue.StartToEnd)
                Alignment.CenterStart else Alignment.CenterEnd
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(action?.background ?: Color.Transparent)
                    .padding(horizontal = 24.dp),
                contentAlignment = alignment
            ) {
                action?.let { Icon(it.icon, contentDescription = null, tint = Color.White) }
            }
        },
        content = { content() }
    )
}
