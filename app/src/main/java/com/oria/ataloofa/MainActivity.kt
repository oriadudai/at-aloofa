package com.oria.ataloofa

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val pink = Color.parseColor("#F49AC2")
    private val lilac = Color.parseColor("#B57EDC")
    private val ink = Color.parseColor("#4A3B57")

    private lateinit var list: LinearLayout
    private lateinit var countdown: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val tick = object : Runnable {
        override fun run() {
            render()
            handler.postDelayed(this, 30_000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL

        Notifier.createChannel(this)
        Notifier.scheduleAll(this)
        askNotificationPermission()

        setContentView(buildLayout())
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        handler.post(tick)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(tick)
    }

    private fun handleIntent(intent: Intent?) {
        val id = intent?.getIntExtra(EXTRA_ID, -1) ?: -1
        ENVELOPES.firstOrNull { it.id == id && it.isOpen() }?.let { openEnvelope(it) }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun rounded(color: Int, radius: Int = 20) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }

    private fun buildLayout(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(28), dp(20), dp(20))
        }
        root.addView(TextView(this).apply {
            text = "את אלופה 🏆"
            textSize = 34f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(lilac)
            gravity = Gravity.CENTER
        })
        root.addView(TextView(this).apply {
            text = "מעטפות ממני להדס 💌"
            textSize = 16f
            setTextColor(ink)
            gravity = Gravity.CENTER
            setPadding(0, dp(4), 0, dp(12))
        })
        countdown = TextView(this).apply {
            textSize = 15f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            background = rounded(pink, 30)
            setPadding(dp(16), dp(10), dp(16), dp(10))
        }
        root.addView(countdown)

        list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(16), 0, dp(8))
        }
        root.addView(list)

        root.addView(smallButton("שלחי לי התראת בדיקה 🔔") {
            Notifier.show(this, 999, "🔔 בדיקה", "אם את רואה את זה, הכל עובד! מחר מחכות לך מעטפות 💛")
        })
        root.addView(smallButton("שהתראות לא יתעכבו (סוללה) 🔋") { askBatteryExemption() })

        return ScrollView(this).apply { addView(root, MATCH_PARENT, WRAP_CONTENT) }
    }

    private fun smallButton(label: String, onClick: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        setTextColor(ink)
        background = rounded(Color.parseColor("#EFE3F7"), 16)
        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            topMargin = dp(10)
        }
        setOnClickListener { onClick() }
    }

    @SuppressLint("SetTextI18n")
    private fun render() {
        val now = System.currentTimeMillis()
        val next = ENVELOPES.firstOrNull { !it.isOpen(now) }
        countdown.text = when {
            next == null -> "כל המעטפות נפתחו. עשית את זה! 🎉"
            else -> {
                val mins = ((next.timeMillis() - now) / 60_000).toInt() + 1
                val h = mins / 60
                val m = mins % 60
                val left = when {
                    h >= 24 -> "מחר בבוקר ☀️"
                    h > 0 -> "בעוד $h שע׳ ו-$m דק׳"
                    else -> "בעוד $m דקות"
                }
                "המעטפה הבאה: $left"
            }
        }

        list.removeAllViews()
        for (e in ENVELOPES) {
            val open = e.isOpen(now)
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                background = rounded(if (open) Color.WHITE else Color.parseColor("#F3E8F8"))
                elevation = if (open) dp(3).toFloat() else 0f
                setPadding(dp(16), dp(14), dp(16), dp(14))
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                    bottomMargin = dp(10)
                }
            }
            card.addView(TextView(this).apply {
                text = if (open) "💌" else "🔒"
                textSize = 26f
                setPadding(0, 0, 0, 0)
            })
            card.addView(TextView(this).apply {
                text = if (open) e.title else "מעטפה סגורה"
                textSize = 17f
                setTextColor(if (open) ink else Color.parseColor("#9C8AAE"))
                setTypeface(typeface, if (open) Typeface.BOLD else Typeface.NORMAL)
                setPadding(dp(12), 0, dp(12), 0)
                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
            })
            card.addView(TextView(this).apply {
                text = e.timeText
                textSize = 14f
                setTextColor(lilac)
            })
            card.setOnClickListener {
                if (open) openEnvelope(e)
                else it.animate().translationX(dp(8).toFloat()).setDuration(60)
                    .withEndAction { it.animate().translationX(0f).setDuration(60).start() }.start()
            }
            list.addView(card)
        }
    }

    private fun openEnvelope(e: Envelope) {
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(24), dp(24), dp(16))
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }
        content.addView(TextView(this).apply {
            text = e.title
            textSize = 22f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(lilac)
        })
        content.addView(TextView(this).apply {
            text = e.body
            textSize = 18f
            setTextColor(ink)
            setLineSpacing(0f, 1.25f)
            setPadding(0, dp(14), 0, dp(14))
        })
        content.addView(TextView(this).apply {
            text = SIGNATURE
            textSize = 16f
            setTextColor(pink)
            gravity = Gravity.END
        })
        content.scaleX = 0.6f; content.scaleY = 0.6f; content.alpha = 0f
        val dialog = AlertDialog.Builder(this)
            .setView(content)
            .setPositiveButton("💛") { d, _ -> d.dismiss() }
            .create()
        dialog.window?.setBackgroundDrawable(rounded(Color.parseColor("#FFF5FA"), 24))
        dialog.setOnShowListener {
            content.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(350).start()
        }
        dialog.show()
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
    }

    @SuppressLint("BatteryLife")
    private fun askBatteryExemption() {
        val pm = getSystemService(PowerManager::class.java)
        if (pm.isIgnoringBatteryOptimizations(packageName)) {
            AlertDialog.Builder(this).setMessage("כבר מסודר ✔️").setPositiveButton("אוקיי", null).show()
            return
        }
        try {
            startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                .setData(Uri.parse("package:$packageName")))
        } catch (_: Exception) {
            startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }
    }
}
