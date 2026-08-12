package org.wirelessredstone.model

data class ChipData(
    val type: ChipType,
    val signalId: String,
    val condition: TriggerCondition = TriggerCondition.ON_CLICK
)