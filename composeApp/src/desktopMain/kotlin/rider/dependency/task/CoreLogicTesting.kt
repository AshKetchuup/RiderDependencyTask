package rider.dependency.task
import org.junit.Assert.assertEquals
import org.junit.Test

// Because I already made sure to check that the UI doesn't freeze
// added independent unit tests to ensure that I test the logic separate from UI
class CoreLogicTesting {
    @Test
    fun `should generate correct UML source`() {
        val input = "A -> B\nB -> C"
        val enabledVertices = mapOf("A" to true, "B" to true, "C" to true)
        val result = generatePlantUMLSource(input, enabledVertices).trim()
        val expected =
            """
            @startuml
            left to right direction
            circle A
            circle B
            A --> B

            circle B
            circle C
            B --> C
            
            @enduml
            """.trimIndent()
        assertEquals(expected, result)
    }

    @Test
    fun `should skip disabled vertices`() {
        val input = "A -> B\nB -> C"
        val enabledVertices = mapOf("A" to true, "B" to false, "C" to true)
        val result = generatePlantUMLSource(input, enabledVertices).trim()
        val expected =
            """
            @startuml
            left to right direction
            @enduml
            """.trimIndent()
        assertEquals(expected, result)
    }

    @Test
    fun `should handle complex multi-line relationships`() {
        val input =
            """
            User -> AuthService
            AuthService -> Database
            User -> PaymentGateway
            PaymentGateway -> FraudDetection
            FraudDetection -> Database
            AuthService -> LoggingService
            """.trimIndent()
        val enabledVertices =
            mapOf(
                "User" to true,
                "AuthService" to true,
                "Database" to true,
                "PaymentGateway" to true,
                "FraudDetection" to true,
                "LoggingService" to true,
            )
        val result = generatePlantUMLSource(input, enabledVertices)
        val expected =
            """
            @startuml
            left to right direction
            circle User
            circle AuthService
            User --> AuthService

            circle AuthService
            circle Database
            AuthService --> Database

            circle User
            circle PaymentGateway
            User --> PaymentGateway

            circle PaymentGateway
            circle FraudDetection
            PaymentGateway --> FraudDetection

            circle FraudDetection
            circle Database
            FraudDetection --> Database

            circle AuthService
            circle LoggingService
            AuthService --> LoggingService
            
            @enduml
            """.trimIndent()
        assertEquals(expected, result)
    }

    @Test
    fun `should handle partial disabling of complex relationships`() {
        val input =
            """
            A -> B
            B -> C
            C -> D
            D -> E
            E -> F
            F -> A
            """.trimIndent()
        val enabledVertices =
            mapOf(
                "A" to true,
                "B" to false,
                "C" to true,
                "D" to true,
                "E" to false,
                "F" to true,
            )
        val result = generatePlantUMLSource(input, enabledVertices)
        val expected =
            """
            @startuml
            left to right direction
            circle C
            circle D
            C --> D

            circle F
            circle A
            F --> A
            
            @enduml
            """.trimIndent()
        assertEquals(expected, result)
    }

    @Test
    fun `should handle self-referencing nodes`() {
        val input = "Node -> Node"
        val enabledVertices = mapOf("Node" to true)
        val result = generatePlantUMLSource(input, enabledVertices)
        val expected =
            """
            @startuml
            left to right direction
            circle Node
            Node --> Node
            
            @enduml
            """.trimIndent()
        assertEquals(expected, result)
    }

    @Test
    fun `should handle duplicate relationships`() {
        val input =
            """
            ServiceA -> ServiceB
            ServiceB -> ServiceC
            ServiceA -> ServiceB
            ServiceC -> ServiceA
            
            """.trimIndent()
        val enabledVertices =
            mapOf(
                "ServiceA" to true,
                "ServiceB" to true,
                "ServiceC" to true,
            )
        val result = generatePlantUMLSource(input, enabledVertices)
        val expected =
            """
            @startuml
            left to right direction
            circle ServiceA
            circle ServiceB
            ServiceA --> ServiceB

            circle ServiceB
            circle ServiceC
            ServiceB --> ServiceC

            circle ServiceA
            circle ServiceB
            ServiceA --> ServiceB

            circle ServiceC
            circle ServiceA
            ServiceC --> ServiceA
            
            @enduml
            """.trimIndent()
        assertEquals(expected, result)
    }

    @Test
    fun `should handle mixed case and special characters`() {
        val input =
            """
            API_Gateway -> User-Service
            User-Service -> Database_V2
            Cache_Service -> Redis_Cluster
            """.trimIndent()
        val enabledVertices =
            mapOf(
                "API_Gateway" to true,
                "User-Service" to true,
                "Database_V2" to true,
                "Cache_Service" to true,
                "Redis_Cluster" to true,
            )
        val result = generatePlantUMLSource(input, enabledVertices)
        val expected =
            """
            @startuml
            left to right direction
            circle API_Gateway
            circle User-Service
            API_Gateway --> User-Service

            circle User-Service
            circle Database_V2
            User-Service --> Database_V2

            circle Cache_Service
            circle Redis_Cluster
            Cache_Service --> Redis_Cluster
            
            @enduml
            """.trimIndent()
        assertEquals(expected, result)
    }

    @Test
    fun `should handle multi-line input with empty lines`() {
        val input =
            """
            Client -> API
            
            
            API -> Service
            
            
            Service -> Database
            """.trimIndent()
        val enabledVertices =
            mapOf(
                "Client" to true,
                "API" to true,
                "Service" to true,
                "Database" to true,
            )
        val result = generatePlantUMLSource(input, enabledVertices)
        val expected =
            """
            @startuml
            left to right direction
            circle Client
            circle API
            Client --> API

            circle API
            circle Service
            API --> Service

            circle Service
            circle Database
            Service --> Database
            
            @enduml
            """.trimIndent()
        assertEquals(expected, result)
    }

    @Test
    fun `should handle invalid lines gracefully`() {
        val input =
            """
            Valid -> Relationship
            InvalidLine
            Another -> Valid -> Relationship
            MissingArrow
            -> OnlyRight
            LeftOnly ->
            """.trimIndent()
        val enabledVertices =
            mapOf(
                "Valid" to true,
                "Relationship" to true,
                "Another" to true,
                "Valid" to true,
                "OnlyRight" to true,
                "LeftOnly" to true,
            )
        val result = generatePlantUMLSource(input, enabledVertices)
        val expected =
            """
            @startuml
            left to right direction
            circle Valid
            circle Relationship
            Valid --> Relationship
            
            @enduml
            """.trimIndent()
        assertEquals(expected, result)
    }
}
