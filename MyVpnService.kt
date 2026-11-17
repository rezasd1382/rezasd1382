package com.example.myvpn

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.*

class MyVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var serviceJob: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_VPN") {
            stopVpn()
            return START_NOT_STICKY
        }

        startVpn()
        return START_STICKY
    }

    private fun startVpn() {
        // ۱. پیکربندی رابط مجازی شبکه (TUN)
        val builder = Builder()
            .setSession("MyCustomVPN")
            .addAddress("10.0.0.2", 32)       // IP داخلی تونل
            .addRoute("0.0.0.0", 0)           // هدایت کل ترافیک اینترنت به سمت VPN
            .addDnsServer("1.1.1.1")          // تنظیم DNS کلودفلر
            .setMtu(1500)

        // جلوگیری از لوپ شدن ترافیک خود برنامه VPN
        try {
            builder.addDisallowedApplication(packageName)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // ۲. ایجاد رابط شبکه
        vpnInterface = builder.establish()

        // ۳. اجرای هسته (Core) در پس‌زمینه
        serviceJob = CoroutineScope(Dispatchers.IO).launch {
            runTunnel()
        }
    }

    private fun runTunnel() {
        val fd = vpnInterface?.fd ?: return
        
        // در این بخش، File Descriptor (fd) به هسته‌ای مثل Sing-box یا Xray (libbox) داده می‌شود
        // و کانفیگ سرور (مثلاً لینک VLESS یا WireGuard) به آن پاس داده می‌شود تا ترافیک رمزنگاری و ارسال شود.
        // مثال مفهومی:
        // Libbox.startService(fd, configJson)
    }

    private fun stopVpn() {
        serviceJob?.cancel()
        vpnInterface?.close()
        vpnInterface = null
        stopSelf()
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }
}
