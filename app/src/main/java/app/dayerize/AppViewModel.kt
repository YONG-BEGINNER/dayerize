package app.dayerize

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.dayerize.core.*
import app.dayerize.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.time.*
import java.math.BigDecimal

data class AppState(val assets:List<Asset> = emptyList(),val config:Map<String,String> = emptyMap(),val loaded:Boolean=false)
class AppViewModel(application:Application):AndroidViewModel(application) {
    private var exportPassword:CharArray?=null
    fun stageExportPassword(value:CharArray){exportPassword?.fill('\u0000');exportPassword=value}
    fun takeExportPassword():CharArray?=exportPassword.also{exportPassword=null}
    override fun onCleared(){exportPassword?.fill('\u0000');exportPassword=null;super.onCleared()}
    val store=AssetStore(application)
    val state=MutableStateFlow(AppState())
    val message=MutableStateFlow<Int?>(null)
    init { launch {
        store.initialize()
        combine(store.dao.observe(),store.dao.observeConfig()) { assets,config ->
            AppState(assets.map { store.json.decodeFromString<Asset>(it.payload) },config.associate { it.key to it.value },true)
        }.collect { state.value=it }
    } }
    fun launch(block:suspend ()->Unit) { viewModelScope.launch { try { withContext(Dispatchers.IO) { block() } } catch(e:Exception) { if(e is CancellationException) throw e;message.value=R.string.failed } } }
    fun save(asset:Asset,onError:()->Unit={},done:()->Unit={}) { viewModelScope.launch {
        try { withContext(Dispatchers.IO){store.save(asset)};done() }
        catch(e:Exception){if(e is CancellationException)throw e;message.value=R.string.failed;onError()}
    } }
    fun mutate(id:String,transform:(Asset)->Asset,onError:()->Unit={},done:()->Unit={}) { viewModelScope.launch {
        try{withContext(Dispatchers.IO){store.mutate(id,transform)};done()}
        catch(e:Exception){if(e is CancellationException)throw e;message.value=R.string.failed;onError()}
    } }
    fun delete(id:String,done:()->Unit)=launch { store.delete(id);withContext(Dispatchers.Main){done()} }
    fun setting(key:String,value:String)=launch { store.preference(key,value) }
    fun today():LocalDate=LocalDate.now(ZoneId.of(state.value.config["timezone"] ?: ZoneId.systemDefault().id))
    fun rates():List<SavedRate> = state.value.config["rates"]?.let { store.json.decodeFromString(it) } ?: emptyList()
    fun fxRates()=rates().map { FxRate(it.from,it.to,BigDecimal(it.value),LocalDate.parse(it.date)) }
    fun addRate(rate:SavedRate)=launch {
        FxRate(rate.from,rate.to,BigDecimal(rate.value),LocalDate.parse(rate.date))
        require(LocalDate.parse(rate.date)<=store.today())
        val updated=rates().filterNot { it.from==rate.from && it.to==rate.to && it.date==rate.date }+rate
        store.preference("rates",store.json.encodeToString(updated))
    }
}
