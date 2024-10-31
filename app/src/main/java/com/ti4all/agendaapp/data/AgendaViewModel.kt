package com.ti4all.agendaapp.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ti4all.agendaapp.AgendaApplication
import com.ti4all.agendaapp.dao.AgendaDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class AgendaViewModel(application: AgendaApplication) : ViewModel() {
    private val agendaDao = application.database.agendaDao()
    private val _agendaList = MutableStateFlow<List<Agenda>>(emptyList())
    val agendaList : StateFlow<List<Agenda>> = _agendaList

    init {
        listarTodos()
    }

    // Construtor padrão necessário para ViewModelProvider
    @Suppress("unused")
    constructor() : this(AgendaApplication.instance)

    fun listarTodos() {
        viewModelScope.launch {
            _agendaList.value = agendaDao.listarTodos()
        }
    }

    fun inserir(agenda: Agenda) {
        viewModelScope.launch {
            agendaDao.inserir(agenda)
            listarTodos()
        }
    }

    fun deletar(id: Int) {
        viewModelScope.launch {
            agendaDao.deletear(id)
            listarTodos()
        }
    }

    fun editar(agenda: Agenda) {
        viewModelScope.launch {
            agendaDao.editar(agenda)
            listarTodos()
        }
    }

    //Para o Cep
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://viacep.com.br/ws/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val cepApiService = retrofit.create(ViaCepService::class.java)

    fun buscarCep(cep: String, onResponse: (Endereco?) -> Unit) {
        cepApiService.buscarCep(cep).enqueue(object : retrofit2.Callback<Endereco> {
            override fun onResponse(call: Call<Endereco>, response: retrofit2.Response<Endereco>) {
                onResponse(response.body())
            }

            override fun onFailure(call: Call<Endereco>, t: Throwable) {
                onResponse(null)
            }
        })
    }


}