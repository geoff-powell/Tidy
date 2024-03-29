package com.greenmiststudios.common.presenters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.benasher44.uuid.uuid4
import com.greenmiststudios.common.components.screens.EditListScreen
import com.greenmiststudios.common.data.TidyList
import com.greenmiststudios.common.data.TidyListItem
import com.greenmiststudios.common.data.asTidyList
import com.greenmiststudios.common.navigation.Back
import com.greenmiststudios.common.navigation.Navigator
import com.greenmiststudios.common.persistance.createList
import com.greenmiststudios.common.persistance.updateList
import com.greenmiststudios.common.viewmodels.EditListViewEvent
import com.greenmiststudios.common.viewmodels.EditListViewModel
import com.greenmiststudios.tidy.Database
import com.greenmiststudios.tidy.TidyListQueries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

public class EditListPresenter(
  private val screen: EditListScreen,
  private val navigator: Navigator,
  database: Database,
  private val ioContext: CoroutineContext,
) : MoleculePresenter<EditListViewModel, EditListViewEvent> {
  private val tidyListQueries: TidyListQueries = database.tidyListQueries

  @Composable
  override fun models(events: Flow<EditListViewEvent>): EditListViewModel {
    return when (val config = screen.params) {
      is EditListScreen.Config.CreateList -> createModels(events)
      is EditListScreen.Config.EditList -> config.editModels(events)
    }
  }

  @Composable
  private fun createModels(events: Flow<EditListViewEvent>): EditListViewModel {
    val defaultList = remember {
      DEFAULT.copy(id = uuid4().toString())
    }
    var tidyList by remember {
      mutableStateOf(defaultList)
    }

    var newItemText by remember {
      mutableStateOf("")
    }

    LaunchedEffect(Unit) {
      events.collect { event ->
        when (event) {
          is EditListViewEvent.UpdateName -> {
            tidyList = tidyList.copy(name = event.name)
          }

          is EditListViewEvent.UpdateItemCompletion -> {
            tidyList = tidyList.updateItem(event.id) {
              copy(completed = event.completed)
            }
          }

          is EditListViewEvent.UpdateList -> {
            launch(ioContext) {
              if (newItemText.isNotEmpty()) {
                tidyList = tidyList.copy(
                  items = tidyList.items + TidyListItem(
                    id = uuid4().toString(),
                    text = newItemText
                  )
                )
                newItemText = ""
              }
              tidyListQueries.createList(tidyList)
              navigator.goTo(Back)
            }
          }

          is EditListViewEvent.UpdateItemText -> {
            tidyList = tidyList.updateItem(event.id) {
              copy(text = event.text)
            }
          }

          EditListViewEvent.AddItem -> {
            tidyList = tidyList.copy(
              items = tidyList.items + TidyListItem(
                id = uuid4().toString(),
                text = ""
              )
            )
          }

          is EditListViewEvent.AddNewItem -> {
            tidyList = tidyList.copy(
              items = tidyList.items + TidyListItem(
                id = uuid4().toString(),
                text = event.text
              )
            )
            newItemText = ""
          }

          is EditListViewEvent.UpdateNewItemText -> newItemText = event.text
          EditListViewEvent.DeleteList -> navigator.goTo(Back)
        }

      }
    }

    return EditListViewModel.Loaded.Add(
      title = "Add List",
      name = tidyList.name,
      newItemText = newItemText,
      items = tidyList.items,
    )
  }

  @Composable
  private fun EditListScreen.Config.EditList.editModels(events: Flow<EditListViewEvent>): EditListViewModel {
    val tidyList: TidyList? by remember {
      tidyListQueries.getListWithItems(id)
        .asFlow()
        .mapToList(ioContext)
        .map { it.asTidyList() }
    }.collectAsState(null)

    var editableTidyList: TidyList? by remember {
      mutableStateOf(null)
    }

    if (editableTidyList == null && tidyList != null) {
      editableTidyList = tidyList
    }

    var newItemText by remember {
      mutableStateOf("")
    }

    LaunchedEffect(Unit) {
      events.collect {
        when (it) {
          is EditListViewEvent.UpdateName -> {
            editableTidyList = editableTidyList?.copy(name = it.name)
          }

          is EditListViewEvent.UpdateItemCompletion -> {
            editableTidyList = editableTidyList?.updateItem(it.id) {
              copy(completed = it.completed)
            }
          }

          is EditListViewEvent.UpdateList -> {
            launch(ioContext) {
              val list = requireNotNull(editableTidyList)
              tidyListQueries.updateList(list)
              navigator.goTo(Back)
            }
          }

          is EditListViewEvent.UpdateItemText -> {
            editableTidyList = editableTidyList?.updateItem(it.id) {
              copy(text = it.text)
            }
          }

          EditListViewEvent.AddItem -> {
            editableTidyList = editableTidyList?.copy(
              items = editableTidyList?.items.orEmpty() + TidyListItem(
                id = uuid4().toString(),
                text = ""
              )
            )
          }

          is EditListViewEvent.AddNewItem -> {
            editableTidyList = editableTidyList?.copy(
              items = editableTidyList?.items.orEmpty() + TidyListItem(
                id = uuid4().toString(),
                text = it.text
              )
            )
            newItemText = ""
          }

          is EditListViewEvent.UpdateNewItemText -> newItemText = it.text
          EditListViewEvent.DeleteList -> {
            launch(ioContext) {
              val list = requireNotNull(editableTidyList)
              // TODO: Add confirmation dialog
              tidyListQueries.deleteListItems(list.id)
              tidyListQueries.deleteList(list.id)
            }
            navigator.goTo(Back)
          }
        }
      }
    }

    return editableTidyList?.let {
      EditListViewModel.Loaded.Edit(
        title = "Edit List",
        name = it.name,
        newItemText = newItemText,
        items = it.items,
      )
    } ?: EditListViewModel.Loading
  }

  public companion object {
    public val DEFAULT: TidyList = TidyList(id = uuid4().toString(), name = "", items = emptyList())
  }
}

private fun TidyList.updateItem(id: String, update: TidyListItem.() -> TidyListItem): TidyList {
  return copy(items = items.map {
    if (it.id == id) {
      it.update()
    } else {
      it
    }
  })
}
