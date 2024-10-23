package com.example.appfirebasefirestore

import android.content.ContentValues.TAG
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appfirebasefirestore.ui.theme.AppFirebaseFirestoreTheme
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    private val db = FirebaseFirestore.getInstance()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App(db)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun App(db: FirebaseFirestore) {
    var nome by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var clientes by remember { mutableStateOf<List<Map<String, String>>>(listOf()) }

    // Função para carregar os clientes e logar no Logcat
    fun loadClientes() {
        db.collection("Clientes")
            .get()
            .addOnSuccessListener { result ->
                val fetchedClientes = result.documents.map { document ->
                    mapOf(
                        "nome" to (document.getString("nome") ?: ""),
                        "telefone" to (document.getString("telefone") ?: "")
                    )
                }
                clientes = fetchedClientes

                // Logar clientes no Logcat
                for (cliente in fetchedClientes) {
                    Log.d(TAG, "Cliente: Nome=${cliente["nome"]}, Telefone=${cliente["telefone"]}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    // Carregar clientes ao inicializar
    LaunchedEffect(Unit) {
        loadClientes()
    }

    AppFirebaseFirestoreTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "App Firebase - Cadastrar Clientes",
                        textAlign = TextAlign.Center,
                        fontSize = 24.sp,
                    )
                }

                // Formulário de Cadastro
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(text = "Nome:", fontSize = 18.sp)
                    TextField(
                        value = nome,
                        onValueChange = { nome = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Telefone:", fontSize = 18.sp)
                    TextField(
                        value = telefone,
                        onValueChange = { telefone = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Botão Centralizado
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(onClick = {
                            val client = hashMapOf(
                                "nome" to nome,
                                "telefone" to telefone
                            )
                            db.collection("Clientes").add(client)
                                .addOnSuccessListener {
                                    Log.d(TAG, "DocumentSnapshot added")
                                    loadClientes() // Recarregar a lista de clientes após adicionar um novo
                                }
                                .addOnFailureListener { e ->
                                    Log.w(TAG, "Error adding document", e)
                                }
                        }) {
                            Text(text = "Cadastrar")
                        }
                    }
                }

                // Lista de Clientes com rolagem
                Text(
                    text = "Lista de Clientes:",
                    fontSize = 18.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f) // Ocupa o espaço restante
                ) {
                    items(clientes) { cliente ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = cliente["nome"] ?: "")
                            Text(text = cliente["telefone"] ?: "")
                        }
                    }
                }
            }
        }
    }
}
