package com.dede.android_eggs.util

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.app.ActivityCompat
import androidx.core.text.HtmlCompat
import com.dede.android_eggs.R
import com.dede.android_eggs.ui.FontIconsDrawable
import com.dede.android_eggs.ui.Icons
import com.dede.basic.getBoolean
import com.dede.basic.putBoolean
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.reflect.KClass

class ActivityActionDispatcher : Application.ActivityLifecycleCallbacks {

    interface ActivityAction {

        fun onCreate(activity: Activity) {}

        fun onStart(activity: Activity) {}

        fun onResume(activity: Activity) {}

        fun onPause(activity: Activity) {}
    }

    private class WarningDialogAction : ActivityAction {

        private class ActionInfo(
            val key: String,
            @StringRes val title: Int,
            @StringRes val message: Int,
        )

        companion object {
            private val trypophobiaActionInfo = ActionInfo(
                "key_warning_t",
                android.R.string.dialog_alert_title,
                R.string.message_trypophobia_warning
            )
            private val target = mapOf<KClass<*>, ActionInfo>(
                com.android_t.egg.PlatLogoActivity::class to trypophobiaActionInfo,
                com.android_s.egg.PlatLogoActivity::class to trypophobiaActionInfo,
            )
        }

        private fun getThemeWrapperContext(base: Activity): Context {
            if (base is AppCompatActivity) {
                return base
            }
            // androidx.appcompat.app.AppCompatDelegateImpl.attachBaseContext2
            val themeWrapper = ContextThemeWrapper(base, R.style.Theme_EasterEggs)
            val mode = when (AppCompatDelegate.getDefaultNightMode()) {
                AppCompatDelegate.MODE_NIGHT_YES -> Configuration.UI_MODE_NIGHT_YES
                AppCompatDelegate.MODE_NIGHT_NO -> Configuration.UI_MODE_NIGHT_NO
                else -> {
                    val appConfig = base.applicationContext.resources.configuration
                    appConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
                }
            }
            val config = Configuration()
            config.fontScale = 0f
            config.uiMode = mode or (config.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv())
            themeWrapper.applyOverrideConfiguration(config)
            return themeWrapper
        }

        override fun onCreate(activity: Activity) {
            val info = target[activity.javaClass.kotlin] ?: return
            val agreed = activity.getBoolean(info.key, false)
            if (agreed) return

            val spanned = HtmlCompat.fromHtml(
                activity.getString(info.message),
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
            val wrapperContext = getThemeWrapperContext(activity)
            val icon = FontIconsDrawable(wrapperContext, Icons.TIPS_AND_UPDATES, 48f)
            MaterialAlertDialogBuilder(wrapperContext)
                .setIcon(icon)
                .setTitle(info.title)
                .setMessage(spanned)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    activity.putBoolean(info.key, true)
                }
                .setNegativeButton(android.R.string.cancel) { _, _ ->
                    activity.finish()
                }
                .show()
        }

    }

    private class PermissionRequestAction : ActivityAction {
        companion object {

            private val empty = emptyArray<String>()

            private val NOTIFICATION = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                arrayOf(Manifest.permission.POST_NOTIFICATIONS) else empty

            private val mapping = mapOf(
                com.android_t.egg.ComponentActivationActivity::class to NOTIFICATION,
                com.android_s.egg.ComponentActivationActivity::class to NOTIFICATION,
                com.android_r.egg.neko.NekoActivationActivity::class to NOTIFICATION,
                com.android_n.egg.neko.NekoActivationActivity::class to NOTIFICATION,
            )
        }

        override fun onCreate(activity: Activity) {
            val permissions = mapping[activity.javaClass.kotlin]
            if (permissions != null && permissions.isNotEmpty()) {
                ActivityCompat.requestPermissions(activity, permissions, 0)
            }
        }
    }

    companion object {
        fun register(application: Application) {
            application.registerActivityLifecycleCallbacks(ActivityActionDispatcher())
        }
    }

    private val actions = arrayListOf(
        PermissionRequestAction(),
        WarningDialogAction()
    )

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        for (action in actions) {
            action.onCreate(activity)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        for (action in actions) {
            action.onStart(activity)
        }
    }

    override fun onActivityResumed(activity: Activity) {
        for (action in actions) {
            action.onResume(activity)
        }
    }

    override fun onActivityPaused(activity: Activity) {
        for (action in actions) {
            action.onPause(activity)
        }
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }
}