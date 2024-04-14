package com.stylianosgakis.nav.saving.state.repro

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import androidx.navigation.navigation
import com.stylianosgakis.nav.saving.state.repro.ui.theme.NavsavingstatereproTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    setContent {
      NavsavingstatereproTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          App(modifier = Modifier.padding(innerPadding))
        }
      }
    }
  }
}

sealed interface Dest {
  data object Root : Dest
  data object GraphA : Dest
  data object GraphADestA : Dest
  data object GraphB : Dest
  data object GraphBDestA : Dest
  data object GraphLogin : Dest
  data object GraphLoginDestA : Dest
}

@Composable
fun App(modifier: Modifier = Modifier) {
  val navController = rememberNavController()
  LogDebugBackstackEffect(navController)
  NavHost(
    navController = navController,
    startDestination = Dest.GraphA.toString(),
    route = Dest.Root.toString(),
    modifier = modifier,
  ) {
    loggedInGraph(navController)
    loggedOutGraph(navController)
  }
}

private fun NavGraphBuilder.loggedInGraph(navController: NavHostController) {
  navigation(
    route = Dest.GraphA.toString(),
    startDestination = Dest.GraphADestA.toString()
  ) {
    composable(Dest.GraphADestA.toString()) {
      Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(1f))
        Text("I am route ${Dest.GraphADestA}")
        Spacer(Modifier.weight(1f))
        NavBar(navController)
      }
    }
  }
  navigation(
    route = Dest.GraphB.toString(),
    startDestination = Dest.GraphBDestA.toString()
  ) {
    composable(Dest.GraphBDestA.toString()) {
      Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(1f))
        Text("I am route ${Dest.GraphBDestA}")
        TextButton(
          onClick = {
            navController.navigate(Dest.GraphLogin.toString()) {
              popUpTo(Dest.Root.toString()) {
                inclusive = true
                saveState = true
              }
            }
          }
        ) {
          Text("Logout. (BA -> Login and pop root)")
        }
        Spacer(Modifier.weight(1f))
        NavBar(navController)
      }
    }
  }
}

private fun NavGraphBuilder.loggedOutGraph(navController: NavHostController) {
  navigation(
    route = Dest.GraphLogin.toString(),
    startDestination = Dest.GraphLoginDestA.toString()
  ) {
    composable(Dest.GraphLoginDestA.toString()) {
      Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(1f))
        Text("I am route ${Dest.GraphLoginDestA}")
        TextButton(
          onClick = {
            navController.navigate(Dest.Root.toString()) {
              popUpTo(route = Dest.GraphLogin.toString()) {
                inclusive = true
              }
              restoreState = true
            }
          }
        ) {
          Text("Login (Go to root (and try to restore state))")
        }
        Spacer(Modifier.weight(1f))
      }
    }
  }
}

@Composable
private fun NavBar(navController: NavController) {
  val navigateToTopLevelGraph: (route: String) -> Unit = { topLevelRoute ->
    // todo: BUG REPORT: Turn this to `false` to experience proper behavior. `true` to experience the bug reported.
    val andSaveStateLikeInNowInAndroid = true
    // Impl like https://github.com/android/nowinandroid/blob/59ed7f402b2fd43f38ed89ca2edb26c0503c9e3d/app/src/main/kotlin/com/google/samples/apps/nowinandroid/ui/NiaAppState.kt#L159-L171
    val topLevelNavOptions = navOptions {
      popUpTo(navController.graph.findStartDestination().id) {
        if (andSaveStateLikeInNowInAndroid) {
          saveState = true
        }
      }
      launchSingleTop = true
      if (andSaveStateLikeInNowInAndroid) {
        restoreState = true
      }
    }
    navController.navigate(topLevelRoute, topLevelNavOptions)
  }
  NavigationBar {
    NavigationBarItem(
      label = { Text("A") },
      selected = navController.currentDestination?.route == Dest.GraphADestA.toString(),
      onClick = { navigateToTopLevelGraph(Dest.GraphA.toString()) },
      icon = { Icon(Icons.Default.Home, "Home") },
    )
    NavigationBarItem(
      label = { Text("B") },
      selected = navController.currentDestination?.route == Dest.GraphBDestA.toString(),
      onClick = { navigateToTopLevelGraph(Dest.GraphB.toString()) },
      icon = { Icon(Icons.Default.AccountCircle, "AccountCircle") }
    )
  }
}

@SuppressLint("RestrictedApi")
@Composable
private fun LogDebugBackstackEffect(navController: NavController) {
  LaunchedEffect(navController) {
    navController.currentBackStack.collect { navBackStackEntryList ->
      navBackStackEntryList.map { navBackStackEntry ->
        navBackStackEntry.destination.route
      }.joinToString(" -> ").also { routeLastPart ->
        Log.d("NavDebug", "routes list:$routeLastPart")
      }
    }
  }
}