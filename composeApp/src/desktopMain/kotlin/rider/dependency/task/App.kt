package rider.dependency.task

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import riderdependencytask.composeapp.generated.resources.Res
import riderdependencytask.composeapp.generated.resources.compose_multiplatform
import net.sourceforge.plantuml.SourceStringReader
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@Composable
@Preview
fun App() {

    MaterialTheme {
        var text by remember { mutableStateOf("") }
        // by lets you access the property directly (no neeed to put text.value as text is a property)
        // remember means  that whenver UI changes the state, Compose recomposes everything so if you don't use remember, state would reset every time

        val edges = text.split("\n").filter { it.isNotBlank() }
        val toggleStates = remember { mutableStateMapOf<String, Boolean>() }
        edges.forEach { if (it !in toggleStates) toggleStates[it] = true }

        println(toggleStates.filter { it.value })
        val umlSource = remember(text, toggleStates)
        {
            generatePlantUMLSource(text, toggleStates.filter { it.value })
        }

        // State for holding the rendered image
        var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

        // Use LaunchedEffect to handle async rendering
        LaunchedEffect(umlSource) {
            // Run in background thread
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    renderPlantUMLtoImage(umlSource).toComposeImageBitmap()
                } catch (e: Exception) {
                    // Handle rendering errors
                    null
                }
            }
            // Update UI on main thread
            imageBitmap = bitmap
        }


        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
        ) {

            // Image box (PlantUML generated graph)
            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                Box(
                    modifier = Modifier
                        .size(500.dp, 360.dp)  // Adjust the size of the image
                        .border(1.dp, Color.Gray)
                        .align(Alignment.Start),
                ) {
                 imageBitmap?.let {
                     Image(bitmap = it, contentDescription = "Graph Diagram",
                         modifier = Modifier.fillMaxSize())
                 } ?: Text("Loading ...", modifier = Modifier.align(Alignment.Center))
                }
            }


            // toggle list box
            Box(
                modifier =
                Modifier
                    .size(200.dp, 300.dp)
                    .align(Alignment.TopEnd)
                    .border(1.dp, Color.Gray),
            ) {
                Text(
                    text = "List of edges",
                    modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .offset(y = (-24).dp),
                )

                Box(
                    modifier =
                    Modifier
                        .fillMaxSize()
                        .border(1.dp, Color.Gray),
                ) {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(top = 24.dp)
                            .verticalScroll(scrollState),
                    ) {
                        edges.forEach { vertex ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Switch(
                                    checked = toggleStates[vertex] == true,
                                    onCheckedChange = { toggleStates[vertex] = it },
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = vertex)
                            }
                        }
                    }

                    VerticalScrollbar(
                        adapter = ScrollbarAdapter(scrollState),
                        modifier = Modifier.align(Alignment.CenterEnd),
                    )
                }
            }

            // Vertices input
            Row(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                val scrollState = rememberScrollState()
                Text("Enter edges:")

                Box(
                    modifier =
                    Modifier
                        .height(100.dp)
                        .fillMaxWidth()
                        .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                        .padding(8.dp),
                ) {
                    BasicTextField(
                        value = text,
                        onValueChange = { newText ->
                            text = newText
                        },
                        modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState),
                        maxLines = Int.MAX_VALUE,
                    )

                    VerticalScrollbar(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        adapter = rememberScrollbarAdapter(scrollState),
                    )
                }
            }
        }
    }
}

private fun generatePlantUMLSource(
    text: String,
    toggleStates: Map<String, Boolean>,
): String {
    val lines =
        text.lines().mapNotNull { line ->
            val parts = line.split("->").map { it.trim() }
            if (parts.size == 2 &&
                toggleStates["${parts[0]}->${parts[1]}"] == true
                ) {
                "${parts[0]} --> ${parts[1]}"
            } else {
                null
            }
        }
    println(lines.forEach{it.toString()})


    return buildString {
        appendLine("@startuml")
        lines.forEach { appendLine(it) }
        appendLine("@enduml")
    }


}

private fun renderPlantUMLtoImage(umlSource: String): BufferedImage {



    val reader = SourceStringReader(umlSource)
    val os = ByteArrayOutputStream()
    reader.generateImage(os)
    val bytes = os.toByteArray()
    return ImageIO.read(ByteArrayInputStream(bytes))
}



