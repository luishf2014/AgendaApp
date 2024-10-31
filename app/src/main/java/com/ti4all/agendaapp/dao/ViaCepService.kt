// CepApiService.kt
package com.ti4all.agendaapp.data

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ViaCepService {
    @GET("ws/{cep}/json/")
    fun buscarCep(@Path("cep") cep: String): Call<Endereco>
}
