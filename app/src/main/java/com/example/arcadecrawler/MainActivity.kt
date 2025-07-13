package com.example.arcadecrawler

import android.R.attr.fontFamily
import android.R.attr.fontWeight
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.arcadecrawler.ui.theme.ArcadeCrawlerTheme

class MainActivity : ComponentActivity() {

    private val gameViewModel:GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.decorView.apply {
            systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                    )
        }
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            // If system bars are visible, reapply immersive mode
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                window.decorView.apply {
                    systemUiVisibility = (
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                            )
                }
            }
        }
        setContent {
            ArcadeCrawlerTheme {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Transparent)) {
                        StartGame(gameViewModel = gameViewModel)
                    }
            }
        }
    }
    override fun onPause(){
        super.onPause()
        gameViewModel.PauseMusic()
    }

    override fun onResume() {
        super.onResume()
        gameViewModel.ResumeMusic()
    }
}
@Composable
fun StartGame(gameViewModel: GameViewModel, navController: NavHostController = rememberNavController()){
    //Log.d("apierror","${gameViewModel.fetchalldata?.isActive}")
    NavHost(navController = navController, startDestination = Screens.LOADING.name, modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()){
        composable (Screens.LOADING.name){
            LoadingScreen(gameViewModel=gameViewModel,
                onloadcomplete = {
                    navController.navigateUp()
                    navController.navigate(Screens.HOME.name)
                },
                onnavigaterestart = {
                    navController.navigateUp()
                    navController.navigate(Screens.LOADING.name)
                }
            )

        }
        composable(Screens.HOME.name){
            StartScreen(onsettingclick = {navController.navigate(Screens.SETTINGS.name)},
                onplayclick = {navController.navigate(Screens.GAME.name)},
                gameViewModel=gameViewModel)
            val context= LocalContext.current
            val sharedprefs=context.getSharedPreferences(shared_pref_filename, Context.MODE_PRIVATE)
            //val editor=sharedprefs.edit()

        }
        composable(Screens.SETTINGS.name) {
            ArcadeSettings(onnavigateup = {navController.navigateUp()}, gameViewModel = gameViewModel)
        }
        composable(Screens.GAME.name) {
            MainGame(gameViewModel=gameViewModel,
                onnavigateup = {
                    gameViewModel.ResetGame()
                    navController.navigateUp()},
                onnavigaterestart = {
                    navController.navigateUp()
                    gameViewModel.ResetGame()
                    navController.navigate(Screens.GAME.name)
                })
        }
    }
}

@Composable
fun LoadingScreen(gameViewModel: GameViewModel,onloadcomplete:()->Unit,onnavigaterestart:() ->Unit){
    val fetchdummy =0
    val context=LocalContext.current
    var should_show_errordialog by remember { mutableStateOf(false) }
    var total_progress by remember { mutableStateOf(0f) }
    val cur_progress by animateFloatAsState(total_progress)
    var should_show_bg_img by remember { mutableStateOf(false) }
    LaunchedEffect(fetchdummy) {
        gameViewModel.FetchAllResources(context=context)
        gameViewModel.fetchalldata?.invokeOnCompletion {
            if(gameViewModel.fetchalldata!!.isCancelled==false && gameViewModel.error_msg==""){
                Log.d("apisuccess","loading screen complete, going to main screen")
                total_progress=1f
                gameViewModel.GetPlayerName(context=context)
                onloadcomplete()
            }
            else{
                should_show_errordialog=true
            }
        }

        //add gameviewmodel.fetchxxx to each if removed try catch for each FetchXXX

        gameViewModel.fetchbgimagejob?.invokeOnCompletion {
            total_progress+=if(gameViewModel.error_msg=="") 1f/gameViewModel.all_jobs.size else 0f
            //cur_loading="Fetching Random Color"
            should_show_bg_img=(gameViewModel.error_msg=="")
        }
        gameViewModel.fetchrandomcolorjob?.invokeOnCompletion {
            //cur_loading="Fetching Skins"
            total_progress+=if(gameViewModel.error_msg=="") 1f/gameViewModel.all_jobs.size else 0f
        }
        gameViewModel.fetchskinsjob?.invokeOnCompletion {
            //cur_loading="Fetching Mushroom Layout"
            total_progress+=if(gameViewModel.error_msg=="") 1f/gameViewModel.all_jobs.size else 0f
        }
        gameViewModel.fetchmushroomlayoutjob?.invokeOnCompletion {
            //cur_loading="Fetching Leaderboard"
            total_progress+=if(gameViewModel.error_msg=="") 1f/gameViewModel.all_jobs.size else 0f
        }
        gameViewModel.fetchleaderboardjob!!.invokeOnCompletion {
            //cur_loading="Fetching Background Music"
            total_progress+=if(gameViewModel.error_msg=="") 1f/gameViewModel.all_jobs.size else 0f
        }
        gameViewModel.fetchbgaudiojob?.invokeOnCompletion {
            total_progress += if (gameViewModel.error_msg == "") 1f / gameViewModel.all_jobs.size else 0f
        }
    }
    if(should_show_errordialog){
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                TextButton(onClick = {
                    gameViewModel.error_msg=""
                    onnavigaterestart()}) {
                    Text("Retry") }
                            },
            title = {Text(
                text="Error",
                fontFamily = FontFamily(Font(R.font.arcade)),
                textAlign = TextAlign.Center)},
            text = {Text(
                text="Could not load game, Please check your internet connection\n${gameViewModel.error_msg}",
                fontFamily = FontFamily(Font(R.font.arcadebody)))}
            )
    }
        Box(modifier = Modifier.fillMaxSize()){
            if(should_show_bg_img){
                Image(
                    bitmap = gameViewModel.bg_bitmap!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            }
            Text(
                text = "Loading...",
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.arcade)),
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 100.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 50.dp), // give space at the bottom
                contentAlignment = Alignment.BottomCenter // centers the bar horizontally at the bottom
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .padding(start = 30.dp, end = 30.dp)
                        .clip(RoundedCornerShape(100.dp)),
                    color = colorResource(R.color.little_dark_purple),
                    trackColor = Color.Gray,
                    progress = cur_progress
                )
            }

    }
}

fun SetBrightness(context: Context,newbrightness:Float){
    val activity = context as Activity
    val layoutParams = activity.window.attributes
    layoutParams.screenBrightness=newbrightness
    activity.window.attributes=layoutParams
}
fun SetPreviousGameplayBrightness(context: Context,gameViewModel: GameViewModel){
    val prefs=context.getSharedPreferences(shared_pref_filename,Context.MODE_PRIVATE)
    val brightness=prefs.getFloat("gameplaybrightness",0.7f)
    gameViewModel.gameplay_brightness=brightness
}
fun SetPreviousHomeScreenBrightness(context: Context,gameViewModel: GameViewModel){
    val prefs=context.getSharedPreferences(shared_pref_filename,Context.MODE_PRIVATE)
    val brightness=prefs.getFloat("homescreenbrightness",1f)
    gameViewModel.homescreen_brightness=brightness
}
fun SetPreviousGunMovement(context: Context,gameViewModel: GameViewModel){
    val prefs=context.getSharedPreferences(shared_pref_filename,Context.MODE_PRIVATE)
    val isgyro=prefs.getBoolean("isgyro",false)
    gameViewModel.isgyro=isgyro
}
fun SetPreviousGyroSensitivity(context: Context,gameViewModel: GameViewModel){
    val prefs=context.getSharedPreferences(shared_pref_filename,Context.MODE_PRIVATE)
    val sensitvity=prefs.getFloat("gyrosensitvity",gameViewModel.gyro_sensitivity)
    gameViewModel.SetGyroSensitivity(sensitvity)
}