package rider.dependency.task

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.sourceforge.plantuml.SourceStringReader
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@Composable
@Preview
fun App() {
    MaterialTheme {
        var text by remember { mutableStateOf("") }

        val edges = text.split("\n").filter { it.isNotBlank() }

        // toggleStates should be for each vertex
        val toggleStates = remember { mutableStateMapOf<String, Boolean>() }
        edges.forEach { s ->

            val parts = s.split("->").map { it.trim() }
            if (parts.size == 2 && !parts.contains("")) {

                // lazy populates when vertices are toggled or accessed
                toggleStates.getOrPut(parts[0]) { true }
                toggleStates.getOrPut(parts[1]) { true }
            }
        }

        // state for holding the rendered image
        var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }


        LaunchedEffect(text, toggleStates.toMap())
     {
            val activeVertices = toggleStates.filter { it.value }
            val umlSource = generatePlantUMLSource(text, activeVertices)

            val bitmap =
                withContext(Dispatchers.IO) {
                    // runs a thread in the background for optimisation
                        convertPlantUMLtoImage(umlSource).toComposeImageBitmap()
                }
            // then updates UI on main thread
            imageBitmap = bitmap
        }

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
        ) {
            // Graph display
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(end = 220.dp, bottom = 130.dp),
            ) {
                Text(
                    text = "Graph Diagram",
                    style = MaterialTheme.typography.h5,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                            .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    imageBitmap?.let {
                        Image(
                            bitmap = it,
                            contentDescription = "Generated Graph",
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            // Vertex Toggle area
            Column(
                modifier =
                    Modifier
                        .width(200.dp)
                        .height(400.dp)
                        .align(Alignment.TopEnd)
                        .padding(start = 8.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        .background(Color(0xFFF8F8F8)),
            ) {
                Text(
                    text = "Vertex List",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(12.dp),
                )

                val scrollState = rememberScrollState()
                Box {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxHeight()
                                .verticalScroll(scrollState)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        // each vertex in the states list
                        toggleStates.keys.forEach { vertex ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                            ) {
                                Switch(
                                    checked = toggleStates[vertex] == true,
                                    onCheckedChange = { toggleStates[vertex] = it },
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(vertex)
                            }
                        }
                    }

                    VerticalScrollbar(
                        adapter = ScrollbarAdapter(scrollState),
                        modifier = Modifier.align(Alignment.CenterEnd),
                    )
                }
            }

            // Input edge
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .align(Alignment.BottomCenter)
                        .padding(top = 8.dp),
            ) {
                Text(
                    text = "Enter Edges:",
                    style = MaterialTheme.typography.h4,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                )

                val inputScrollState = rememberScrollState()
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                            .background(Color(0xFFF0F0F0))
                            .padding(8.dp),
                ) {
                    BasicTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(inputScrollState),
                        maxLines = Int.MAX_VALUE,
                    )

                    VerticalScrollbar(
                        adapter = rememberScrollbarAdapter(inputScrollState),
                        modifier = Modifier.align(Alignment.CenterEnd),
                    )
                }
            }
        }
    }
}

fun generatePlantUMLSource(
    text: String,
    toggleStates: Map<String, Boolean>,
): String {
    val lines =
        text.lines().mapNotNull { line ->
            val parts = line.split("->").map { it.trim() }
            if (parts.size == 2 &&
                toggleStates[parts[0]] == true &&
                toggleStates[parts[1]] == true
            ) {

                if(parts[0] != parts[1])
                // adds each vertex as a circle then makes the connection
                "circle ${parts[0]}\n" + "circle ${parts[1]}\n" +
                    "${parts[0]} --> ${parts[1]}\n"

                // dont add duplicate nodes to Uml
                else
                    "circle ${parts[0]}\n"+
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
    }.trim()
}

// generates bufferedImage by making bytearray from umlSource
private fun convertPlantUMLtoImage(umlSource: String): BufferedImage {
    val reader = SourceStringReader(umlSource)
    val os = ByteArrayOutputStream()
    reader.generateImage(os)
    val bytes = os.toByteArray()
    return ImageIO.read(ByteArrayInputStream(bytes))
}
