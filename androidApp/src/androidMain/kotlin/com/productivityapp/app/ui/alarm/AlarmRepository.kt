package com.productivityapp.app.ui.alarm

import androidx.compose.runtime.mutableStateListOf
import com.productivityapp.model.Alarm
import java.util.UUID

object AlarmRepository {
    val alarms = mutableStateListOf<Alarm>()

    fun addAlarm(
        time: String,
        label: String,
        repeatDays: List<String> = emptyList(),
        isEnabled: Boolean = true,
        isVibrate: Boolean = true,
        escalationType: String = "Standard",
        sound: String = "Default"
    ) {
        val now = java.time.Instant.now().toEpochMilli()
        alarms.add(Alarm(
            id = UUID.randomUUID().toString(),
            time = time,
            label = label,
            repeatDays = repeatDays.mapNotNull { Alarm.stringToDayOfWeek(it) },
            isEnabled = isEnabled,
            isVibrate = isVibrate,
            escalationType = escalationType,
            sound = sound,
            createdAt = now,
            updatedAt = now
        ))
    }

    fun toggleAlarm(id: String) {
        val index = alarms.indexOfFirst { it.id == id }
        if (index != -1) {
            alarms[index] = alarms[index].copy(isEnabled = !alarms[index].isEnabled)
        }
    }

    fun deleteAlarm(id: String) {
        alarms.removeAll { it.id == id }
    }

    fun updateAlarm(
        id: String,
        time: String,
        label: String,
        repeatDays: List<String>,
        isVibrate: Boolean,
        escalationType: String,
        sound: String
    ) {
        val index = alarms.indexOfFirst { it.id == id }
        if (index != -1) {
            alarms[index] = alarms[index].copy(
                time = time,
                label = label,
                repeatDays = repeatDays.mapNotNull { Alarm.stringToDayOfWeek(it) },
                isVibrate = isVibrate,
                escalationType = escalationType,
                sound = sound,
                updatedAt = java.time.Instant.now().toEpochMilli()
            )
        }
    }
}
