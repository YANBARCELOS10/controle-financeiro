package br.com.ysenerbyte.comandospro.core

object LaboratoryEngine {
    val expectedBenchFlow = listOf("QF", "PS1", "S", "KSR", "CPU", "RL", "KM", "M")

    fun isBenchFlowValid(assembly: List<String>): Boolean = assembly == expectedBenchFlow

    fun locateInputSignal(sensorLed: Boolean, channelLed: Boolean, inputBit: Boolean): String = when {
        !sensorLed -> "FIELD_DEVICE"
        !channelLed -> "FIELD_TO_CHANNEL"
        !inputBit -> "CHANNEL_TO_LOGIC"
        else -> "SIGNAL_COMPLETE"
    }
}
