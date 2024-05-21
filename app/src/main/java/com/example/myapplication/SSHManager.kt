package com.cookandroid.myapplication

import android.util.Log
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

object SSHManager {
    private var lastFetchedData: String? = null
    private var session: Session? = null

    suspend fun loadFromServer(): String? = withContext(Dispatchers.IO) {
        var channel: ChannelExec? = null
        Log.d("SSH 연결시도", "연결됨")
        try {
            if (session?.isConnected == true) {
                closeSession() // Ensure previous session is closed
            }

            val jsch = JSch()
            session = jsch.getSession("chaenoa", "withcap.iptime.org", 22).apply {
                setPassword("Ca123oa!")
                setConfig("StrictHostKeyChecking", "no")
                connect()
            }

            channel = session!!.openChannel("exec") as ChannelExec
            channel.setCommand("cat test")
            val outputStream = ByteArrayOutputStream()
            channel.outputStream = outputStream
            channel.connect()

            while (!channel.isClosed) {
                Thread.sleep(100)
            }

            val output = outputStream.toString()
            if (output != lastFetchedData) {
                lastFetchedData = output
                Log.d("SSH Output", output)
                output
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("SSH Error", "Error during SSH connection: ${e.message}")
            null
        } finally {
            closeChannel(channel)
            closeSession()
        }
    }

    suspend fun writeToServer(value: String) = withContext(Dispatchers.IO) {
        var channel: ChannelExec? = null
        try {
            if (session?.isConnected == true) {
                closeSession() // Ensure previous session is closed
            }

            val jsch = JSch()
            session = jsch.getSession("chaenoa", "withcap.iptime.org", 22).apply {
                setPassword("Ca123oa!")
                setConfig("StrictHostKeyChecking", "no")
                connect()
            }

            channel = session!!.openChannel("exec") as ChannelExec
            channel.setCommand("echo \"$value\" > test")
            channel.connect()

            while (!channel.isClosed) {
                Thread.sleep(100)
            }
        } catch (e: Exception) {
            Log.e("SSH Error", "Error during SSH connection: ${e.message}")
        } finally {
            closeChannel(channel)
            closeSession()
        }
    }

    private fun closeChannel(channel: ChannelExec?) {
        if (channel != null) {
            Log.d("closeChannel", "채널 닫기")
            channel.disconnect()
        }
    }

    private fun closeSession() {
        session?.let {
            if (it.isConnected) {
                Log.d("closeSession", "session 닫기")
                it.disconnect()
            }
        }
        session = null
    }
}
