package kr.co.uxn.agms_p.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kr.co.uxn.agms_p.ui.model.ItemData

class EventScreenViewModel : ViewModel() {


    private val _eventItemList = MutableStateFlow<List<ItemData>>(emptyList())
    val eventItemList = _eventItemList.asStateFlow()

    fun setItems(list: List<ItemData>) {
        _eventItemList.value = list.sortedBy { it.time }.reversed()
    }

    fun addItem(item: ItemData) {
        _eventItemList.value = (_eventItemList.value + item).reversed()
    }
}