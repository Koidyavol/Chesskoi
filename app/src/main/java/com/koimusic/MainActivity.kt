package com.koimusic

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors

class MainActivity : ComponentActivity() {
    private lateinit var list: LinearLayout
    private var controller: MediaController? = null
    private val songs = mutableListOf<Pair<String,String>>()
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { if(it) loadSongs() }
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); buildUI(); requestPermission() }
    private fun buildUI() {
        val root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(24,24,24,24); setBackgroundColor(0xff101018.toInt()) }
        val title=TextView(this).apply { text="KOI MUSIC"; textSize=26f; setTextColor(0xffe8b5ff.toInt()); gravity=Gravity.CENTER; setPadding(0,8,0,20) }
        root.addView(title)
        val scan=Button(this).apply { text="SCAN MUSIK"; setOnClickListener { requestPermission() } }; root.addView(scan)
        list=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }
        val scroll=ScrollView(this); scroll.addView(list); root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
        val controls=LinearLayout(this).apply { gravity=Gravity.CENTER }
        listOf("⏮","▶ / ⏸","⏭").forEachIndexed { i,s -> controls.addView(Button(this).apply { text=s; setOnClickListener { when(i){0->controller?.seekToPrevious();1->controller?.let{if(it.isPlaying)it.pause() else it.play()};2->controller?.seekToNext()} } }) }
        root.addView(controls); setContentView(root)
        val token=SessionToken(this,ComponentName(this,PlaybackService::class.java)); MediaController.Builder(this,token).buildAsync().addListener({controller=MediaController.Builder(this,token).buildAsync().get()},MoreExecutors.directExecutor())
    }
    private fun requestPermission(){ val p=if(Build.VERSION.SDK_INT>=33) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE; if(ContextCompat.checkSelfPermission(this,p)==PackageManager.PERMISSION_GRANTED)loadSongs() else permissionLauncher.launch(p) }
    private fun loadSongs(){ songs.clear(); list.removeAllViews(); val projection=arrayOf(MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.DATA); val c=contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,projection,"${MediaStore.Audio.Media.IS_MUSIC} != 0",null,MediaStore.Audio.Media.TITLE+" ASC"); c?.use { while(it.moveToNext()){songs.add(it.getString(0) to it.getString(1))} }; songs.forEachIndexed { index,song -> list.addView(TextView(this).apply { text="♫  ${song.first}"; textSize=16f; setTextColor(0xffeeeeee.toInt()); setPadding(8,18,8,18); setOnClickListener { controller?.setMediaItems(songs.map { MediaItem.fromUri(it.second) },index,0); controller?.prepare(); controller?.play() } }) }; if(songs.isEmpty())list.addView(TextView(this).apply{text="Tidak ada musik ditemukan.";setTextColor(-1)}) }
}