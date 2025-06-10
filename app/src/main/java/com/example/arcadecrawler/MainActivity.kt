package com.example.arcadecrawler

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
fun StartScreen(onsettingclick:() ->Unit,onplayclick:() ->Unit,gameViewModel: GameViewModel){
    var about_dialog_visible by remember { mutableStateOf(false) }
    var snake_dialog_visible by remember{mutableStateOf(false)}
    var leaderboard_dialog_visible by remember { mutableStateOf(false) }
    var player_info_visible by remember { mutableStateOf(gameViewModel.cur_player_name=="") }
    val context= LocalContext.current
    SetPreviousSpeeds(gameViewModel=gameViewModel,context=context)
    SetPreviousBrightness(context=context,gameViewModel=gameViewModel)
    SetBrightness(context=context, newbrightness = 0.7f)
    SetPreviousGunMovement(context=context,gameViewModel=gameViewModel)
    SetPreviousGyroSensitivity(context=context,gameViewModel=gameViewModel)

    if (gameViewModel.bgplayer == null) {
        Log.d("arcadething", "set up music players")
        gameViewModel.SetMusicPlayers(context = context)
    }
    if (gameViewModel.IsBgPlayerInitialized() && !gameViewModel.bgplayer!!.isPlaying) {
        Log.d("arcadething", "started bg music,$")
        gameViewModel.StartBgMusic()
    }
    SetPreviousBgVolume(gameViewModel=gameViewModel,context=context)
    Box(modifier=Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.homescreen),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            contentScale = ContentScale.FillBounds
        )
        Text(
            text = "ARCADE",
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = colorResource(R.color.little_dark_purple),
            fontFamily = FontFamily(Font(R.font.arcade)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 32.dp)
        )
        Text(
            text = "CRAWLER",
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = colorResource(R.color.little_dark_purple),
            fontFamily = FontFamily(Font(R.font.arcade)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 92.dp)
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(R.drawable.leaderboard),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .clickable {
                            gameViewModel.PlayButtonClick()
                            leaderboard_dialog_visible = true
                        }
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(R.drawable.settings),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(8.dp)
                            .size(110.dp)
                            .clip(CircleShape)
                            .clickable {
                                gameViewModel.PlayButtonClick()
                                onsettingclick()
                            }
                    )
                    Image(
                        painter = painterResource(R.drawable.play),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .clickable {
                                gameViewModel.PlayButtonClick()
                                snake_dialog_visible = true
                            }
                            .align(Alignment.Center)
                        //.padding(8.dp)
                    )
                    Image(
                        painter = painterResource(R.drawable.info),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(8.dp)
                            .size(110.dp)
                            .clip(CircleShape)
                            .clickable {
                                gameViewModel.PlayButtonClick()
                                about_dialog_visible = true
                            }
                    )
                }
            }
        }
        if (about_dialog_visible) {
            AboutDialog(
                ondismiss = { about_dialog_visible = false },
                gameViewModel = gameViewModel
            )
        }
        if (snake_dialog_visible) {
            SnakeDialog(
                ondismiss = { snake_dialog_visible = false },
                onplayclick = { onplayclick() },
                gameViewModel = gameViewModel
            )
        }
        if (leaderboard_dialog_visible) {
            gameViewModel.GetLeaderboardData()
            LeaderboardDialog(
                ondismiss = { leaderboard_dialog_visible = false },
                gameViewModel = gameViewModel
            )
        }
        if(player_info_visible){
            PlayerInfoDialog(ondismiss = {player_info_visible=false},gameViewModel=gameViewModel)
        }
    }
    }
@Composable
fun AboutDialog(ondismiss:() ->Unit,gameViewModel: GameViewModel){
    Dialog(onDismissRequest = {ondismiss()}) {
            Box(modifier=Modifier
                .size(350.dp, 400.dp)
                .background(
                    color = colorResource(R.color.blueish),
                    shape = RoundedCornerShape(33.dp)
                )) {
                Image(
                    painter = painterResource(R.drawable.close),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .clickable {
                            gameViewModel.PlayButtonClick()
                            ondismiss()
                        }
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "ABOUT",
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                        color = colorResource(R.color.little_dark_purple),
                        fontFamily = FontFamily(Font(R.font.arcade))
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            text = stringResource(R.string.about_message),
                            textAlign = TextAlign.Center,
                            color= colorResource(R.color.dark_gold),
                            fontFamily = FontFamily(Font(R.font.arcadebody))
                        )
                    }
                }
            }
    }
}
@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun SnakeDialog(ondismiss: () -> Unit,onplayclick: () -> Unit,gameViewModel: GameViewModel){
    val items = listOf(1,2,3,4,5,6,7,8,9,10)
    var expanded by remember { mutableStateOf(false) }
    val sharedprefs= LocalContext.current.getSharedPreferences(shared_pref_filename,Context.MODE_PRIVATE)
    var selectedItem by remember { mutableStateOf(sharedprefs.getInt("selecteditem",1).toString()) }

    Dialog(onDismissRequest = {ondismiss()}) {
        Box (
            modifier=Modifier
                .size(300.dp, 250.dp)
                .background(
                    shape = RoundedCornerShape(16.dp),
                    color = colorResource(R.color.blueish)
                )){
            Column (verticalArrangement = Arrangement.SpaceBetween,horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier
                .align(Alignment.Center)
                .fillMaxSize()
                .padding(8.dp)){
                Box(modifier=Modifier.fillMaxWidth()) {
                    Text(
                        text = "Snakes",
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily(Font(R.font.arcade)),
                        modifier=Modifier.align(Alignment.Center),
                        color= colorResource(R.color.little_dark_purple)
                    )
                    Image(
                        painter=painterResource(R.drawable.close),
                        contentDescription = null,
                        modifier=Modifier
                            .size(48.dp)
                            .padding(8.dp)
                            .align(Alignment.TopEnd)
                            .clip(CircleShape)
                            .clickable {
                                gameViewModel.PlayButtonClick()
                                ondismiss()
                            }
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedItem,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Number of Snakes") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier=Modifier
                            .heightIn(max = 150.dp)
                    ) {
                        items.forEach { element ->
                            DropdownMenuItem(
                                text = { Text(element.toString()) },
                                onClick = {
                                    gameViewModel.PlayButtonClick()
                                    selectedItem = element.toString()
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Button(
                    onClick = {
                        gameViewModel.PlayButtonClick()
                        val editor=sharedprefs.edit()
                        editor.putInt("selecteditem",selectedItem.toInt())
                        editor.apply()
                        gameViewModel.SetSnakes(selectedItem.toInt()-1)
                        gameViewModel.ResetGame()
                        ondismiss()
                        onplayclick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.green))
                ){
                    Text(
                        text="PLAY",
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color= colorResource(R.color.ivory),
                        fontFamily = FontFamily(Font(R.font.arcadebody))
                    )
                }
            }
        }
    }
}

@Composable
fun LeaderboardDialog(ondismiss: () -> Unit,gameViewModel: GameViewModel){
    Dialog(onDismissRequest = {ondismiss()}) {
        Box(modifier=Modifier
            .size(350.dp, 400.dp)
            .background(color = colorResource(R.color.blueish), shape = RoundedCornerShape(33.dp))) {
            Image(
                painter = painterResource(R.drawable.close),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(CircleShape)
                    .clickable {
                        gameViewModel.PlayButtonClick()
                        ondismiss()
                    }
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = "Leaderboard",
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    color = colorResource(R.color.little_dark_purple),
                    fontFamily = FontFamily(Font(R.font.arcade)),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally,modifier= Modifier.verticalScroll(rememberScrollState())) {
                    for(i in 0 until gameViewModel.top10.size){
                        LeaderboardCard(rank = i+1, leaderboard = gameViewModel.top10[i], cur_player_name = gameViewModel.cur_player_name)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerInfoDialog(ondismiss: () -> Unit,gameViewModel: GameViewModel){
    var cur_name by remember { mutableStateOf("") }
    var cur_password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val context=LocalContext.current
    Dialog(onDismissRequest = {ondismiss()}){
        Card(
            colors = CardDefaults.cardColors(colorResource(R.color.blueish))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text="Player Info",
                    color=colorResource(R.color.little_dark_purple)
                )
                Text(
                    text="Please tell us about yourself",
                    color=colorResource(R.color.charcoal)
                )
                Row(){
                    Text(
                        text="Your Name:",
                        modifier=Modifier.padding(4.dp),
                        color=colorResource(R.color.dark_gold)

                    )
                    TextField(
                        value = cur_name,
                        onValueChange = {
                            cur_name=it
                        },
                        placeholder = {Text("Enter Your Name")},
                        modifier=Modifier.padding(4.dp)
                    )
                }
                Row(){
                    Text(
                        text="Password:",
                        modifier=Modifier.padding(4.dp),
                        color=colorResource(R.color.dark_gold)
                    )
                    TextField(
                        value = cur_password,
                        onValueChange = { cur_password = it },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        placeholder = {Text("Enter Your Password")},
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = null)
                            }
                        },
                        modifier=Modifier.padding(4.dp)
                    )
                }
                Button(onClick = {
                    ondismiss()
                    val prefs=context.getSharedPreferences(shared_pref_filename, Context.MODE_PRIVATE)
                    val edit=prefs.edit()
                    edit.putString("player_name",cur_name)
                    edit.putString("player_password",cur_password)
                    edit.apply()
                },
                    colors= ButtonDefaults.buttonColors(colorResource(R.color.baby_blue))
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
fun LeaderboardCard(rank:Int,leaderboard: LeaderboardGet,cur_player_name:String){
    Card(
        elevation = CardDefaults.cardElevation(16.dp),
        colors = CardDefaults.cardColors(containerColor =  colorResource(R.color.baby_blue))
    )
    {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(8.dp)) {
            Text(
                text = "Rank $rank",
                color=colorResource(R.color.dark_gold),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if(leaderboard.name==cur_player_name) "You" else "Name: ${leaderboard.name}",
                color = colorResource(R.color.charcoal),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Bullets Shot: ${leaderboard.score}",
                color=colorResource(R.color.deep_purple),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Date,Time: ${leaderboard.created_at}",
                fontWeight = FontWeight.Bold
            )
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
        gameViewModel.fetchbgaudiojob?.invokeOnCompletion {
            total_progress+=if(gameViewModel.error_msg=="") 1f/gameViewModel.all_jobs.size else 0f
        }
        gameViewModel.fetchbgimagejob?.invokeOnCompletion {
            total_progress+=if(gameViewModel.error_msg=="") 1f/gameViewModel.all_jobs.size else 0f
            should_show_bg_img=(gameViewModel.error_msg=="")
        }
        gameViewModel.fetchskinsjob?.invokeOnCompletion {
            total_progress+=if(gameViewModel.error_msg=="") 1f/gameViewModel.all_jobs.size else 0f
        }
        gameViewModel.fetchrandomcolorjob?.invokeOnCompletion {
            total_progress+=if(gameViewModel.error_msg=="") 1f/gameViewModel.all_jobs.size else 0f
        }
        gameViewModel.fetchmushroomlayoutjob?.invokeOnCompletion {
            total_progress+=if(gameViewModel.error_msg=="") 1f/gameViewModel.all_jobs.size else 0f
        }
        gameViewModel.fetchleaderboardjob!!.invokeOnCompletion {
            total_progress+=if(gameViewModel.error_msg=="") 1f/gameViewModel.all_jobs.size else 0f
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
                textAlign = TextAlign.Center)},
            text = {Text("Could not load game, Please check your internet connection\n${gameViewModel.error_msg}")}
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
                fontSize = 32.sp,
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
fun SetPreviousBrightness(context: Context,gameViewModel: GameViewModel){
    val prefs=context.getSharedPreferences(shared_pref_filename,Context.MODE_PRIVATE)
    val brightness=prefs.getFloat("screenbrightness",0.7f)
    gameViewModel.cur_brightness=brightness
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