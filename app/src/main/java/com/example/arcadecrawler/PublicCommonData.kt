package com.example.arcadecrawler

import android.R.attr.data
import android.graphics.Point
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.time.Duration

enum class Screens{
    HOME,
    LOADING,
    GAME,
    SETTINGS
}
enum class Speed{
    SLOW,
    MEDIUM,
    FAST
}
enum class Movement{
    LEFT,
    RIGHT,
    UP,
    DOWN,
    DIAGONAL_TOP_RIGHT,
    DIAGONAL_TOP_LEFT,
    DIAGONAL_BOTTOM_RIGHT,
    DIAGONAL_BOTTOM_LEFT
}
enum class SnakeHierarchy{
    HEAD,
    NODE
}
enum class MushroomType{
    NORMAL,
    POISON
}

val blackwhitegridheight=100.dp
val speed_options = listOf(Speed.SLOW,Speed.MEDIUM,Speed.FAST)
val shared_pref_filename="ArcadeCrawler"
val all_movements=Movement.values().toList()
val all_powerup_imgs=mapOf(
    1 to R.drawable.scorpionshield,
    2 to R.drawable.rapidfire,
    3 to R.drawable.speedup,
    4 to R.drawable.mushroombomb,
    5 to R.drawable.triple_bullet,
    6 to R.drawable.poisonremover
)


@RequiresApi(Build.VERSION_CODES.O)
fun GetUtcInLocalTime(utc_time:String):String{
    val formatter= DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val utcDateTime = LocalDateTime.parse(utc_time, formatter).atZone(ZoneOffset.UTC)
    val localDateTime = utcDateTime.withZoneSameInstant(ZoneId.systemDefault())
    val displayFormat = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
    return localDateTime.format(displayFormat)
}


val SCORE_INCREMENT=10
data class Joystick(var thumbpositon:Offset=Offset.Zero,var outerradius:Float=0f,var innerradius:Float=0f)
data class Bullet(val id:Int,var bullet_position:MutableState<Offset> = mutableStateOf(Offset.Zero),var bitmap_width:Float,var bitmap_height:Float)
data class Mushroom(val id:Int,var mushroomType: MushroomType,var mushroom_position:Offset,var health:Int=5,var bitmap_width: Float,var bitmap_height: Float)
data class Snake(val id:Int,var node_lst:SnapshotStateList<SnakeNode>,var head_position:MutableState<Offset>,val bitmap_width: Float,val bitmap_height: Float)
data class SnakeNode(
    var hierarchy: SnakeHierarchy,
    var movement: Movement,
    var node_position: MutableState<Offset>,
    var previous: SnakeNode?=null
)
data class Spider(
    var id:Int,
    var spider_position:MutableState<Offset>,
    var movement: MutableState<Movement>,
    val bitmap_width: Float,
    val bitmap_height: Float
)
data class Scorpion(
    var id:Int,
    var scorpion_position:MutableState<Offset>,
    val bitmap_width: Float,
    val bitmap_height: Float
)
data class RandomColor(val color:String)
data class Skins(
    val mushrooms:List<String>,
    val guns:List<String>,
    val scorpions:List<String>
)
data class RelativeMushroomLayout(
    val layout:List<List<Float>>
)
data class LeaderboardGet(
    val name:String,
    val score:Int,
    val created_at:String,
)
data class LeaderboardPost(
    val name:String,
    val password:String,
    val score:Int
)
data class PowerUpPost(
    val centipedeDestroyed: Boolean
)
data class PowerUpData(
    val id: Int,
    val name: String,
    val description: String,
    val duration: Int
)
