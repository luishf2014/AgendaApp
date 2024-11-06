package com.ti4all.agendaapp.data

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

// Definir a interface da API (separado da classe Endereco para boas práticas)
interface ViaCepService {
    @GET("ws/{cep}/json/")
    fun buscarCep(@Path("cep") cep: String): Call<Endereco>
}

data class Endereco(
    val cep: String,
    val logradouro: String,
    val bairro: String,
    val localidade: String,
    val uf: String
)
