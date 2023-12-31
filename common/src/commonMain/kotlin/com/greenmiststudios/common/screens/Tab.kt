package com.greenmiststudios.common.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.greenmiststudios.common.components.screens.CalendarScreen
import com.greenmiststudios.common.components.screens.HomeScreen
import com.greenmiststudios.common.components.screens.ListsScreen
import com.greenmiststudios.common.providers.LocalStringManager
import com.greenmiststudios.common.resources.StringKey
import cafe.adriel.voyager.core.screen.Screen as VoyagerScreen

public sealed class Tab(
  private val initialScreen: VoyagerScreen
) : Tab {
  @Composable
  override fun Content() {
    Navigator(initialScreen) {
      CurrentScreen()
    }
  }

  public data object Home : com.greenmiststudios.common.screens.Tab(HomeScreen) {
    override val options: TabOptions
      @Composable
      get() = TabOptions(
        index = 0u,
        icon = rememberVectorPainter(Icons.Outlined.Home),
        title = LocalStringManager.current[StringKey.TAB_HOME],
      )
  }

  public data object Lists : com.greenmiststudios.common.screens.Tab(ListsScreen) {
    override val options: TabOptions
      @Composable
      get() = TabOptions(
        index = 1u,
        icon = rememberVectorPainter(Icons.Outlined.Assignment),
        title = LocalStringManager.current[StringKey.TAB_LISTS],
      )
  }

  public data object Calendar : com.greenmiststudios.common.screens.Tab(CalendarScreen) {
    override val options: TabOptions
      @Composable
      get() = TabOptions(
        index = 2u,
        icon = rememberVectorPainter(Icons.Outlined.CalendarMonth),
        title = LocalStringManager.current[StringKey.TAB_CALENDAR],
      )
  }
}
