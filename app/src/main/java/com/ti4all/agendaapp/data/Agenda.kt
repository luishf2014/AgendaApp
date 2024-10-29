package com.ti4all.agendaapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agenda") // Tabela deve ser em minúsculas
data class Agenda(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String,
    val telefone: String,
    val cep: String,
    val logradouro: String,
    val bairro: String,
    val localidade: String,
    val uf: String
)
Endereco.Cep = Agenda.cep