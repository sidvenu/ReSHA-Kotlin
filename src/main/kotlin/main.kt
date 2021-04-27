import androidx.compose.desktop.LocalAppWindow
import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data_classes.HashResult
import java.awt.FileDialog
import java.io.File
import java.util.*

fun main() = Window {
    var fileName by remember { mutableStateOf("") }
    var hashResult by remember { mutableStateOf<HashResult?>(null) }

    LaunchedEffect(fileName) {
        val file = File(fileName)
        if (file.isFile) {
            hashResult = HashAlgorithm.hash(file.readBytes())
        }
    }
    MaterialTheme {
        Surface {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(text = "ReSHA")
                        },
                        elevation = 12.dp
                    )
                }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(modifier = Modifier.widthIn(max = 1000.dp)) {
                        FileChooser(
                            file = fileName,
                            onFileNameChanged = {
                                fileName = it
                            },
                            modifier = Modifier.padding(20.dp)
                        )
                        HashResultDisplay(hashResult)
                    }
                }
            }
        }
    }
}

@Composable
fun FileChooser(file: String, onFileNameChanged: (String) -> Unit, modifier: Modifier = Modifier) {
    val window = LocalAppWindow.current
    Row(modifier) {
        TextField(value = TextFieldValue(file.ifEmpty { "Please select a file" }),
            onValueChange = {},
            enabled = false,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                disabledTextColor = Color.DarkGray,
            ),
            trailingIcon = {
                IconButton(onClick = {
                    val dialog = FileDialog(window.window)
                    dialog.isVisible = true
                    if (!dialog.directory.isNullOrBlank() && !dialog.file.isNullOrBlank()) {
                        onFileNameChanged(dialog.directory + dialog.file)
                    }
                }) {
                    Icon(Icons.Filled.Search, "", tint = MaterialTheme.colors.primary)
                }
            }
        )
    }
}

@Composable
fun HashResultDisplay(hashResult: HashResult?) {
    Row {
        if (hashResult == null) {
            Text("Select file to compute the hashes")
        } else {
            Row {
                HashResultCard("SHA-1 hash", hashResult.sha1sum,
                modifier = Modifier.weight(1f).padding(vertical = 20.dp).padding(start = 20.dp))
                Spacer(Modifier.requiredWidth(20.dp))
                HashResultCard("SHA-256 hash", hashResult.sha256sum,
                    modifier = Modifier.weight(1f).padding(vertical = 20.dp).padding(end = 20.dp))
            }
        }
    }
}

@Composable
fun HashResultCard(title: String, hash: ByteArray, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = 12.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(title, fontSize = 42.sp)
            Spacer(Modifier.requiredHeight(20.dp))
            Text("In Hex encoding", fontSize = 24.sp)
            Spacer(Modifier.requiredHeight(10.dp))
            SelectionContainer {
                Text(
                    hash.joinToString(separator = "") { "%02x".format(it) }
                )
            }
            Spacer(Modifier.requiredHeight(20.dp))
            Text("In Base64 encoding", fontSize = 24.sp)
            Spacer(Modifier.requiredHeight(10.dp))
            SelectionContainer {
                Text(
                    Base64.getEncoder().encodeToString(hash)
                )
            }
        }
    }
}
