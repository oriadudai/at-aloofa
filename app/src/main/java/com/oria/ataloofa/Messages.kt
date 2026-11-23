package com.oria.ataloofa

import java.util.Calendar

// ===== כאן עורכים את התוכן =====

/** היום שבו ההודעות נשלחות (שנה, חודש 1-12, יום) */
const val YEAR = 2026
const val MONTH = 11
const val DAY = 24

const val SIGNATURE = "אוהבת, אוריה 💛"

data class Envelope(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val title: String,
    val body: String,
) {
    val timeText: String get() = "%02d:%02d".format(hour, minute)

    fun timeMillis(): Long = Calendar.getInstance().apply {
        set(YEAR, MONTH - 1, DAY, hour, minute, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun isOpen(now: Long = System.currentTimeMillis()) = now >= timeMillis()
}

val ENVELOPES = listOf(
    Envelope(1, 7, 45, "☀️ בוקר טוב הדס!",
        "בוקר טוב אלופה! היום עמוס, אבל את יותר חזקה ממנו. תאכלי משהו לפני שאת יוצאת 💛"),
    Envelope(2, 8, 20, "📝 רגע לפני הבוחן",
        "נשימה עמוקה. את יודעת את החומר יותר טוב ממה שנדמה לך. לכי תראי להם 💪"),
    Envelope(3, 9, 15, "🎉 הבוחן מאחורייך!",
        "החלק הכי מלחיץ של הבוקר נגמר. מה שלא יהיה, אני גאה בך. עכשיו רק לשרוד את המעבדה 😄"),
    Envelope(4, 11, 35, "🧪 סיימת מעבדה!",
        "שלוש שעות מעבדה ✔️ לשתות מים ולאכול משהו לפני שממשיכים"),
    Envelope(5, 13, 0, "✨ חצי יום מאחורייך",
        "את באמצע היום ועדיין בועטת. רק שתדעי שאני חושבת עלייך"),
    Envelope(6, 14, 25, "📚 הישורת האחרונה",
        "עוד הרצאה אחת. שעתיים ואת חופשייה!"),
    Envelope(7, 16, 35, "🏆 את אלופה!",
        "עשית את זה! כל היום הזה מאחורייך. עכשיו מגיע לך לנוח ולהתפנק"),
)
