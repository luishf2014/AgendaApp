package com.ti4all.agendaapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.ti4all.agendaapp.data.Agenda
import com.ti4all.agendaapp.data.AgendaViewModel
import com.ti4all.agendaapp.data.Endereco
import com.ti4all.agendaapp.ui.theme.AgendaAppTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AgendaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AgendaAppTheme {
                AgendaScreen(viewModel = viewModel, context = this)
            }
        }
    }
}

@Composable
fun AgendaList(
    agenda: Agenda,
    onRemoveClick: (Agenda) -> Unit,
    onEditClick: (Agenda) -> Unit,
    onCallClick: (Agenda) -> Unit,
    onSmsClick: (Agenda) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Nome: ${agenda.nome}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Telefone: ${agenda.telefone}")
                }

                IconButton(onClick = { onCallClick(agenda) }) {
                    Icon(Icons.Filled.Call, contentDescription = "Ligar para contato")
                }

                IconButton(onClick = { onSmsClick(agenda) }) {
                    Icon(Icons.Filled.AccountBox, contentDescription = "Enviar SMS")
                }

                IconButton(onClick = { onEditClick(agenda) }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar contato")
                }

                IconButton(onClick = { onRemoveClick(agenda) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remover contato")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(viewModel: AgendaViewModel, context: ComponentActivity) {
    val agendaList by viewModel.agendaList.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var agendaToEdit by remember { mutableStateOf<Agenda?>(null) }
    var showRemoveDialog by remember { mutableStateOf(false) }
    var agendaToRemove by remember { mutableStateOf<Agenda?>(null) }

    LaunchedEffect(Unit) {
        viewModel.listarTodos()
    }

    Scaffold(topBar = {
        TopAppBar(
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Agenda Telefônica")
                }
            }
        )
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            agendaList.forEach { agenda ->
                AgendaList(
                    agenda = agenda,
                    onRemoveClick = { selectedAgenda ->
                        agendaToRemove = selectedAgenda
                        showRemoveDialog = true
                    },
                    onEditClick = { selectedAgenda ->
                        agendaToEdit = selectedAgenda
                        showEditDialog = true
                    },
                    onCallClick = { selectedAgenda ->
                        makePhoneCall(context, selectedAgenda.telefone)
                    },
                    onSmsClick = { selectedAgenda ->
                        sendSms(context, selectedAgenda.telefone)
                    }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FloatingActionButton(onClick = { showDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Adicionar contato")
                }
            }
        }

        if (showDialog) {
            AgendaFormDialog(
                onDismissRequest = { showDialog = false },
                viewModel = viewModel,
                onAddClick = { nome, telefone, endereco ->
                    viewModel.inserir(Agenda(
                        nome = nome,
                        telefone = telefone,
                        endereco = endereco
                    ))
                    showDialog = false
                }
            )
        }

        if (showEditDialog && agendaToEdit != null) {
            Edit(
                nomeInicial = agendaToEdit!!.nome,
                telefoneInicial = agendaToEdit!!.telefone,
                cepInicial = agendaToEdit!!.cep,
                logradouroInicial = agendaToEdit!!.logradouro,
                bairroInicial = agendaToEdit!!.bairro,
                localidadeInicial = agendaToEdit!!.localidade,
                ufInicial = agendaToEdit!!.uf,
                onDismissRequest = {
                    showEditDialog = false
                    agendaToEdit = null
                },
                onSaveClick = { nome, telefone, endereco ->
                    viewModel.editar(agendaToEdit!!.copy(
                        nome = nome,
                        telefone = telefone,
                        endereco = endereco
                    ))
                    showEditDialog = false
                    agendaToEdit = null
                }
            )
        }

        if (showRemoveDialog && agendaToRemove != null) {
            Remove(
                nome = agendaToRemove!!.nome,
                onDismissRequest = {
                    showRemoveDialog = false
                    agendaToRemove = null
                },
                onRemoveClick = {
                    viewModel.deletar(agendaToRemove!!.id)
                    showRemoveDialog = false
                    agendaToRemove = null
                }
            )
        }
    }
}

fun makePhoneCall(context: ComponentActivity, telefone: String) {
    val intent = Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse("tel:$telefone")
    }
    ContextCompat.startActivity(context, intent, null)
}

fun sendSms(context: ComponentActivity, telefone: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("smsto:$telefone")
    }
    ContextCompat.startActivity(context, intent, null)
}

@Composable
fun AgendaFormDialog(
    onDismissRequest: () -> Unit,
    viewModel: AgendaViewModel,
    onAddClick: (String, String, Endereco) -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                var nome by remember { mutableStateOf("") }
                var telefone by remember { mutableStateOf("") }
                var cep by remember { mutableStateOf("") }
                var logradouro by remember { mutableStateOf("") }
                var bairro by remember { mutableStateOf("") }
                var localidade by remember { mutableStateOf("") }
                var uf by remember { mutableStateOf("") }

                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = telefone,
                    onValueChange = { telefone = it },
                    label = { Text("Telefone") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = cep,
                    onValueChange = { newCep ->
                        cep = newCep
                        if (newCep.length == 8) {
                            viewModel.buscarCep(newCep) { endereco ->
                                endereco?.let {
                                    logradouro = it.logradouro
                                    bairro = it.bairro
                                    localidade = it.localidade
                                    uf = it.uf
                                }
                            }
                        }
                    },
                    label = { Text("CEP") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = logradouro,
                    onValueChange = { logradouro = it },
                    label = { Text("Logradouro") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = bairro,
                    onValueChange = { bairro = it },
                    label = { Text("Bairro") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = localidade,
                    onValueChange = { localidade = it },
                    label = { Text("Localidade") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uf,
                    onValueChange = { uf = it },
                    label = { Text("UF") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        onAddClick(nome, telefone, Endereco(cep, logradouro, bairro, localidade, uf))
                        onDismissRequest()
                    }
                ) {
                    Text("Adicionar contato")
                }
            }
        }
    }
}

@Composable
fun Edit(
    nomeInicial: String,
    telefoneInicial: String,
    cepInicial: String,
    logradouroInicial: String,
    bairroInicial: String,
    localidadeInicial: String,
    ufInicial: String,
    onDismissRequest: () -> Unit,
    onSaveClick: (String, String, Endereco) -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                var nome by remember { mutableStateOf(nomeInicial) }
                var telefone by remember { mutableStateOf(telefoneInicial) }
                var cep by remember { mutableStateOf(cepInicial) }
                var logradouro by remember { mutableStateOf(logradouroInicial) }
                var bairro by remember { mutableStateOf(bairroInicial) }
                var localidade by remember { mutableStateOf(localidadeInicial) }
                var uf by remember { mutableStateOf(ufInicial) }

                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = telefone,
                    onValueChange = { telefone = it },
                    label = { Text("Telefone") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = cep,
                    onValueChange = { cep = it },
                    label = { Text("CEP") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = logradouro,
                    onValueChange = { logradouro = it },
                    label = { Text("Logradouro") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = bairro,
                    onValueChange = { bairro = it },
                    label = { Text("Bairro") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = localidade,
                    onValueChange = { localidade = it },
                    label = { Text("Localidade") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uf,
                    onValueChange = { uf = it },
                    label = { Text("UF") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        onSaveClick(nome, telefone, Endereco(cep, logradouro, bairro, localidade, uf))
                        onDismissRequest()
                    }
                ) {
                    Text("Salvar alterações")
                }
            }
        }
    }
}

@Composable
fun Remove(
    nome: String,
    onDismissRequest: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Tem certeza que deseja remover o contato $nome?")
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Button(onClick = onRemoveClick) {
                        Text("Sim")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = onDismissRequest) {
                        Text("Não")
                    }
                }
            }
        }
    }
}
