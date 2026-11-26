package com.koimusic
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors

class MainActivity:ComponentActivity(){
 private var controller:MediaController?=null
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState)
 setContent { MusicUI() }
 }
 @Composable fun MusicUI(){
 var songs by remember { mutableStateOf(listOf<Song>()) }; var selected by remember { mutableStateOf<Song?>(null) }; var playing by remember { mutableStateOf(false) }
 val perm=if(Build.VERSION.SDK_INT>=33) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
 val launcher=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){ granted->if(granted)songs=scan() }
 LaunchedEffect(Unit){if(ContextCompat.checkSelfPermission(this@MainActivity,perm)==PackageManager.PERMISSION_GRANTED)songs=scan() else launcher.launch(perm)}
 MaterialTheme(colorScheme=darkColorScheme(primary=Color(0xFFB38CFF),background=Color(0xFF10101A),surface=Color(0xFF1A1A28))) {
 Column(Modifier.fillMaxSize().background(Color(0xFF10101A)).padding(18.dp)) {
 Text("KOIMUSIC",fontSize=28.sp,fontWeight=FontWeight.Black,color=Color(0xFFBDA1FF));Text("LOCAL AUDIO PLAYER",color=Color.Gray)
 Spacer(Modifier.height(20.dp)); Text("Library · ${songs.size} songs",fontSize=20.sp,color=Color.White,fontWeight=FontWeight.Bold)
 LazyColumn(Modifier.weight(1f)) {items(songs){song->Row(Modifier.fillMaxWidth().clickable { play(song,songs);selected=song;playing=true }.padding(vertical=12.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(song.title,color=Color.White,fontWeight=FontWeight.Medium);Text(song.artist,color=Color.LightGray,fontSize=12.sp)}Text("▶",color=Color(0xFFBDA1FF))};Divider(color=Color.DarkGray)}}
 selected?.let{Column {Text(it.title,color=Color.White);Row {Button(onClick={controller?.let{c->if(c.isPlaying){c.pause();playing=false}else{c.play();playing=true}}}){Text(if(playing)"Pause" else "Play")};Button(onClick={controller?.seekToNext();playing=true}){Text("Next")}}}}
 Button(onClick={songs=scan()}){Text("Scan ulang")}
 }
 }
 }
 fun scan():List<Song>{val out=mutableListOf<Song>();val projection=arrayOf(MediaStore.Audio.Media._ID,MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.ARTIST);val cursor=contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,projection,"${MediaStore.Audio.Media.IS_MUSIC}=1",null,MediaStore.Audio.Media.TITLE+" ASC");cursor?.use{while(it.moveToNext()){val id=it.getLong(0);out.add(Song(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString()+"/"+id,it.getString(1)?:"Unknown",it.getString(2)?:"Unknown"))}};return out}
 fun play(song:Song,list:List<Song>){val token=SessionToken(this,android.content.ComponentName(this,PlaybackService::class.java));val future=MediaController.Builder(this,token).buildAsync();future.addListener({controller=future.get();controller?.setMediaItems(list.map{MediaItem.fromUri(it.uri)},list.indexOf(song),0);controller?.prepare();controller?.play()},MoreExecutors.directExecutor())}
 data class Song(val uri:String,val title:String,val artist:String)
}
