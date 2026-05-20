package com.bananchiki.wakeup.data.model

import com.bananchiki.wakeup.R

enum class AlarmSoundCategory(val displayName: String) {
    CALM("🌙 Спокойные"),
    MEDIUM("⏰ Средние"),
    LOUD("🔊 Громкие")
}

enum class AlarmSound(
    val displayName: String,
    val category: AlarmSoundCategory,
    val rawResId: Int
) {
    // Calm
    CALM_QUIET("Тихая спокойная мелодия", AlarmSoundCategory.CALM, R.raw.alarm_calm_quiet),
    CALM_GRADUAL("Плавное нарастание", AlarmSoundCategory.CALM, R.raw.alarm_calm_gradual),
    CALM_BIRDS("Щебетание птиц", AlarmSoundCategory.CALM, R.raw.alarm_calm_birds),
    CALM_PIANO("Фортепиано", AlarmSoundCategory.CALM, R.raw.alarm_calm_piano),

    // Medium
    MEDIUM_MELODY("Красивая мелодия", AlarmSoundCategory.MEDIUM, R.raw.alarm_medium_melody),
    MEDIUM_SHEPHERD("The Lonely Shepherd", AlarmSoundCategory.MEDIUM, R.raw.alarm_medium_shepherd),
    MEDIUM_SIREN("Сирена", AlarmSoundCategory.MEDIUM, R.raw.alarm_medium_siren),
    MEDIUM_SIGNAL("Сигнал", AlarmSoundCategory.MEDIUM, R.raw.alarm_medium_signal),
    MEDIUM_ALERT("Оповещение", AlarmSoundCategory.MEDIUM, R.raw.alarm_medium_alert),

    // Loud
    LOUD_CHEMICAL("Химическая тревога", AlarmSoundCategory.LOUD, R.raw.alarm_loud_chemical),
    LOUD_FIRE("Пожарная тревога", AlarmSoundCategory.LOUD, R.raw.alarm_loud_fire),
    LOUD_GERMAN("Немецкая тревога", AlarmSoundCategory.LOUD, R.raw.alarm_loud_german),
    LOUD_BIOHAZARD("Биологическая угроза", AlarmSoundCategory.LOUD, R.raw.alarm_loud_biohazard),
    LOUD_DEAFENING("Оглушительная сирена", AlarmSoundCategory.LOUD, R.raw.alarm_loud_deafening),
    LOUD_NUCLEAR("Ядерная эвакуация", AlarmSoundCategory.LOUD, R.raw.alarm_loud_nuclear);

    companion object {
        val DEFAULT = MEDIUM_MELODY

        fun fromName(name: String): AlarmSound {
            return entries.find { it.name == name } ?: DEFAULT
        }
    }
}
