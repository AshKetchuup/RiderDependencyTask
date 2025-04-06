package rider.dependency.task

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
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



        // State for holding the rendered image
        var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

        // Use LaunchedEffect to handle async rendering
        LaunchedEffect(text,toggleStates.toMap()) {
            val activeEdges = toggleStates.filter{it.value== true}
            val umlSource = generatePlantUMLSource(text,activeEdges)


            // Run in background thread
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    renderPlantUMLtoImage(umlSource).toComposeImageBitmap()
                } catch (e: Exception) {
                    // Handle rendering errors
                    println("Error rendering image: ${e.message}")
                    null
                    // puts a loading screen
                }
            }
            // Update UI on main thread
            imageBitmap = bitmap
        }


        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Graph Display Area
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 220.dp, bottom = 130.dp) // Leave space for side panel & bottom input
            ) {
                Text(
                    text = "Graph Diagram",
                    style = MaterialTheme.typography.h5,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    imageBitmap?.let {
                        Image(
                            bitmap = it,
                            contentDescription = "Generated Graph",
                            modifier = Modifier.fillMaxSize()
                        )
                    } ?: Text("Loading...", style = MaterialTheme.typography.body2)
                }
            }

            // Edge Toggle Panel
            Column(
                modifier = Modifier
                    .width(200.dp)
                    .height(400.dp)
                    .align(Alignment.TopEnd)
                    .padding(start = 8.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8F8F8)),
            ) {
                Text(
                    text = "Edge List",
                    style = MaterialTheme.typography.h6
                    ,
                    modifier = Modifier.padding(12.dp)
                )

                val scrollState = rememberScrollState()
                Box {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        edges.forEach { edge ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Switch(
                                    checked = toggleStates[edge] == true,
                                    onCheckedChange = { toggleStates[edge] = it }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(edge)
                            }
                        }
                    }

                    VerticalScrollbar(
                        adapter = ScrollbarAdapter(scrollState),
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }

            // Input Field at Bottom
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .align(Alignment.BottomCenter)
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = "Enter Edges:",
                    style = MaterialTheme.typography.h4,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                )

                val inputScrollState = rememberScrollState()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        .background(Color(0xFFF0F0F0))
                        .padding(8.dp)
                ) {
                    BasicTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(inputScrollState),
                        maxLines = Int.MAX_VALUE
                    )

                    VerticalScrollbar(
                        adapter = rememberScrollbarAdapter(inputScrollState),
                        modifier = Modifier.align(Alignment.CenterEnd)
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
                "circle ${parts[0]}\n"+ "circle ${parts[1]}\n"+
                "${parts[0]} --> ${parts[1]}\n"
            } else {
                null
            }
        }


    return buildString {
        appendLine("@startuml")
        appendLine("left to right direction")
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



