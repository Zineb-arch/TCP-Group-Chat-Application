# TCP Group Chat Application

A real-time group chat application built with **Java Sockets (TCP)** and **JavaFX**, following the MVC (Model-View-Controller) architecture with strict **Separation of Concerns**.

---

## Features

### Client
- **Username Authentication** — enter a username to join the chat
- **Read-Only Mode** — connect without a username to observe without sending
- **Real-Time Messaging** — send messages via the SEND button or Enter key
- **Active Users Query** — type `allUsers` to see who's online
- **Graceful Disconnect** — type `bye` or `end` to leave; or click Disconnect
- **Online Status Indicator** — green/red circle + label

### Server
- **Multiple Simultaneous Clients** — thread-per-connection model
- **Message Broadcasting** — formats messages with sender name + timestamp
- **Live Client List** — ListView with random color-coded user badges
- **Activity Log** — "Server Started", "Waiting for Client", "Welcome [User]", etc.

---

## Architecture

```
MVC Pattern — Separation of Concerns
├── Model   → pure Java/socket logic (no JavaFX)
├── View    → JavaFX UI (no business logic)
└── Controller → bridges Model ↔ View via callbacks
```

### Technology Stack
| Layer | Technology |
|-------|-----------|
| Language | Java 11+ |
| Networking | Java Sockets (TCP) |
| GUI | JavaFX 17 (GridPane, ListView, CSS styling) |
| Build | Maven 3.x |
| IDE | IntelliJ IDEA |

### Server-Side Concurrency
Uses a **thread-per-connection** model. Each accepted client socket spawns a dedicated `ClientHandler` thread. For an alternative, the project notes also reference I/O multiplexing (NIO Selector) for higher scalability.

---

## Project Structure

```
GroupChatApp/
├── TCPServer/
│   ├── pom.xml
│   └── src/main/
│       ├── java/server/
│       │   ├── ServerApp.java          ← Entry point
│       │   ├── model/ServerModel.java  ← Business logic
│       │   ├── model/ClientHandler.java← Per-client thread
│       │   ├── controller/ServerController.java
│       │   └── view/ServerView.java    ← JavaFX UI
│       └── resources/server.properties
│
├── TCPClient/
│   ├── pom.xml
│   └── src/main/
│       ├── java/client/
│       │   ├── ClientApp.java          ← Entry point
│       │   ├── model/ClientModel.java  ← Networking logic
│       │   ├── controller/ClientController.java
│       │   └── view/ClientView.java    ← JavaFX UI
│       └── resources/client.properties
│
├── UML/
│   ├── class_diagram.puml
│   ├── deployment_diagram.puml
│   ├── sequence_diagram.puml
│   └── usecase_diagram.puml
│
└── README.md
```

---

## Configuration

### Server (`TCPServer/src/main/resources/server.properties`)
```properties
server.port=3000
server.host=localhost
```

### Client (`TCPClient/src/main/resources/client.properties`)
```properties
server.host=localhost
server.port=3000
```
> Settings are loaded at runtime — no recompilation needed when changing host/port.

---

## Build & Run

### Prerequisites
- Java 11 or higher
- Maven 3.6+

### Build

```bash
# Build Server
cd TCPServer
mvn clean package

# Build Client
cd ../TCPClient
mvn clean package
```

This produces both a regular JAR and a fat JAR (`*-jar-with-dependencies.jar`) in each `target/` directory.

### Run

**Start the Server:**
```bash
java -jar TCPServer/target/TCPServer-1.0-SNAPSHOT-jar-with-dependencies.jar
# Or via Maven:
cd TCPServer && mvn javafx:run
```

**Start a Client:**
```bash
# Using config file defaults:
java -jar TCPClient/target/TCPClient-1.0-SNAPSHOT-jar-with-dependencies.jar

# Or with CLI arguments:
java -jar TCPClient/target/TCPClient-1.0-SNAPSHOT-jar-with-dependencies.jar localhost 3000
# (Equivalent to: java TCPClient localhost 3000)
```

---

## Chat Commands

| Command | Description |
|---------|-------------|
| `allUsers` | Lists all currently connected users |
| `bye` or `end` | Disconnects from the server |
| *(any text)* | Sends a message to all users |

---

## UML Diagrams

Located in the `UML/` directory (PlantUML `.puml` format):

| File | Description |
|------|-------------|
| `class_diagram.puml` | Full class structure, fields, methods, relationships |
| `deployment_diagram.puml` | Physical nodes, JVM environments, TCP/IP links |
| `sequence_diagram.puml` | Connect → chat → allUsers → disconnect message flow |
| `usecase_diagram.puml` | User and admin interactions |

Render with [PlantUML](https://plantuml.com/) or the IntelliJ PlantUML plugin.

---

## Design Decisions

1. **Thread-per-connection** — simple and sufficient for the expected client count in this project
2. **Callbacks instead of direct references** — Model notifies View via `Consumer<>` / `Runnable` lambdas, keeping layers decoupled
3. **`CopyOnWriteArrayList`** for the client list — thread-safe iteration without locking during broadcasts
4. **Config file at runtime** — `.properties` file loaded from classpath; override via CLI args for flexible deployment
5. **Read-Only mode** — enforced on both server (ignores messages from guest) and client (UI disabled)

---

