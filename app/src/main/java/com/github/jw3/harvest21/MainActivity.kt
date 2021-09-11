package com.github.jw3.harvest21

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.view.MapView
import kotlinx.android.synthetic.main.activity_main.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage


class MainActivity : FragmentActivity() {
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        startActivity(Intent(this, SettingsActivity::class.java))
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        askGpsPermission()

        val map = MapFragment.newInstance()

        supportFragmentManager
            .beginTransaction()
            .disallowAddToBackStack()
            .replace(R.id.mapContainer, map)
            .commit()

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val deviceId = prefs.getString("device_uid", "androidz")
        val brokerUrl = prefs.getString("broker_url", "localhost")
        val brokerUser = prefs.getString("broker_user", "admin")
        val brokerPass = prefs.getString("broker_pass", "admin")

        val msg = "connecting to $brokerUrl as $brokerUser"

        try {
            val client = MqttAndroidClient(this, "ssl://$brokerUrl:443", deviceId)
            val opts = MqttConnectOptions()
            opts.userName = brokerUser
            opts.password = brokerPass?.toCharArray()

            client.connect(opts, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    asyncActionToken?.let {
                        it.client.publish("test", MqttMessage("!!!!".toByteArray()))
                        Toast.makeText(applicationContext, "$msg ✅", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Toast.makeText(applicationContext, "$msg\n❗${exception?.cause?.message}", Toast.LENGTH_LONG).show()
                }
            })
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "$msg\n❗${e.cause?.message}", Toast.LENGTH_LONG).show()
            return
        }
    }

    private fun askGpsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            + ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            + ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                111
            )
        }
    }
}
