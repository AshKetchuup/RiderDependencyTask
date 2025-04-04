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
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import riderdependencytask.composeapp.generated.resources.Res
import riderdependencytask.composeapp.generated.resources.compose_multiplatform
import net.sourceforge.plantuml.SourceStringReader

@Composable
@Preview
fun App() {

    MaterialTheme {
        var text by remember { mutableStateOf("") }
        // by lets you access the property directly (no neeed to put text.value as text is a property)
        // remember means  that whenver UI changes the state, Compose recomposes everything so if you don't use remember, state would reset every time

        val vertices = text.split("\n").filter { it.isNotBlank() }

        val toggleStates = remember { mutableStateMapOf<String, Boolean>() }
        vertices.forEach { if (it !in toggleStates) toggleStates[it] = true }

        val enabledVertices = toggleStates.filterValues { it }.keys

        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
        ) {
            Box(
                modifier =
                Modifier
                    .size(200.dp, 300.dp)
                    .align(Alignment.TopEnd)
                    .border(1.dp, Color.Gray),
            ) {
                Text(
                    text = "List of vertices",
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
                        vertices.forEach { vertex ->
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
                Text("Enter vertices:")

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



