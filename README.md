Develop a GUI application in Kotlin that allows users to visualize and interact with directed graphs. You can use any UI framework you prefer, such as Swing, JavaFX, or Compose for Desktop.

The application should have a diagram display area where the graph is rendered. To generate the diagram, you can use a library like PlantUML or Mermaid, either by calling a local function or making a request to a remote service that returns the generated image.

Users will define their graphs through a graph input area, which should be a simple text box where they can enter an edge list. Each line represents a connection between two vertices, for example, A -> B means there is a directed edge from A to B. This input should always be editable, allowing users to modify the graph at any time.

There should be a vertex list displaying all vertices in the graph. The vertex list is automatically updated when editing the graph input area. This list should give you the ability to enable or disable specific vertices — if a vertex is "disabled", it should no longer appear in the diagram even if it's declared in the graph input area.

Functional Considerations

The diagram should refresh automatically when the user modifies the graph definition or toggles vertices.
The application should remain responsive, with no UI freezes, even with large graphs.
Implement tests to validate the core logic.
