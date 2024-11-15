package com.ti4all.agendaapp

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.setContent
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
import androidx.lifecycle.ViewModelProvider
import com.ti4all.agendaapp.data.Agenda
import com.ti4all.agendaapp.data.AgendaViewModel
import com.ti4all.agendaapp.data.AgendaViewModelFactory
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat.startActivity
import com.ti4all.agendaapp.ui.theme.AgendaAppTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AgendaViewModel by lazy {
        ViewModelProvider(this, AgendaViewModelFactory(application as AgendaApplication)).get(AgendaViewModel::class.java)
    }

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
)
    {
        Card(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        agenda.nome.firstOrNull()?.toString() ?: "",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = agenda.nome,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = agenda.telefone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Minha Agenda",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )

        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar contato")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Lista de Contatos",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (agendaList.isEmpty()) {
                Text(
                    "Nenhum contato disponível.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            } else {
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
        }
    }

            if (showDialog) {
                AgendaFormDialog(
                    onDismissRequest = { showDialog = false },
                    viewModel = viewModel,
                    onAddClick = { nome, telefone, cep, logradouro, bairro, localidade, uf, numero ->
                        viewModel.inserir(
                            Agenda(
                                nome = nome,
                                telefone = telefone,
                                cep = cep,
                                logradouro = logradouro,
                                bairro = bairro,
                                localidade = localidade,
                                uf = uf,
                                numero = numero
                            )
                        )
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
                    numeroInicial = agendaToEdit!!.numero,
                    onDismissRequest = {
                        showEditDialog = false
                        agendaToEdit = null
                    },
                    onSaveClick = { nome, telefone, cep, logradouro, bairro, localidade, uf, numero ->
                        viewModel.editar(
                            agendaToEdit!!.copy(
                                nome = nome,
                                telefone = telefone,
                                cep = cep,
                                logradouro = logradouro,
                                bairro = bairro,
                                localidade = localidade,
                                uf = uf,
                                numero = numero
                            )
                        )
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
    onAddClick: (String, String, String, String, String, String, String, String) -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
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
                var numero by remember { mutableStateOf("") }

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
                        if (newCep.length == 8) { // Verifica se o CEP tem 8 caracteres
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
                    value = numero,
                    onValueChange = { numero = it },
                    label = { Text("Número") }
                )
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
                        onAddClick(nome, telefone, cep, logradouro, bairro, localidade, uf, numero)
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
    numeroInicial: String,
    onDismissRequest: () -> Unit,
    onSaveClick: (String, String, String, String, String, String, String, String) -> Unit
) {
    val context = LocalContext.current
    fun compartilharDados() {
        // Formatar os dados a serem compartilhados
        val data = """
            Nome: $nomeInicial
            Telefone: $telefoneInicial
            CEP: $cepInicial
            Logradouro: $logradouroInicial
            Número: $numeroInicial            
            Bairro: $bairroInicial
            Localidade: $localidadeInicial
            UF: $ufInicial
        """.trimIndent()

        // Criando o Intent de compartilhamento
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, data)  // Dados que serão compartilhados
            type = "text/plain"  // Tipo de conteúdo: texto simples
        }

        // Verificando se há algum app disponível para lidar com o Intent de compartilhamento
        try {
            startActivity(context, Intent.createChooser(sendIntent, "Compartilhar com"), null)
        } catch (e: Exception) {
            Toast.makeText(context, "Não foi possível compartilhar os dados", Toast.LENGTH_SHORT).show()
        }
    }
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
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
                var numero by remember { mutableStateOf(numeroInicial) }

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
                    value = numero,
                    onValueChange = { numero = it },
                    label = { Text("Número") }
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,  // Alinha os elementos verticalmente no centro
                    horizontalArrangement = Arrangement.spacedBy(16.dp)  // Espaçamento entre os elementos
                ) {
                    Button(
                        onClick = {
                            onSaveClick(nome, telefone, cep, logradouro, bairro, localidade, uf, numero)
                            onDismissRequest()
                        }
                    ) {
                        Text("Salvar alterações")
                    }

                    // Ícone ao lado do botão
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Adicionar contato",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { compartilharDados() }
                    )
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
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
