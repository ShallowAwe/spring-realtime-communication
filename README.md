# 📡 Real-Time API Practice Project

> **Note**: This is a **practice project** created to study and demonstrate different real-time communication mechanisms. It is **not intended for production use**. The goal is to smooth the learning curve around these technologies and provide a visual playground for understanding how they work.

---

## 🎯 Project Purpose

This project demonstrates and compares four key techniques for real-time web communication:

1.  **Short Polling**
2.  **Long Polling**
3.  **Server-Sent Events (SSE)**
4.  **WebSocket**

It provides a **Learning Sandbox** where you can toggle each mechanism, observe the data flow, and analyze connection behavior in real-time.

---

## 🏗️ Mechanisms Explained

### 1. ⏱️ Short Polling

**"Are we there yet? Are we there yet?"**

- **How it works**: The client (browser) repeatedly sends HTTP requests to the server at fixed intervals (e.g., every 2 seconds). The server immediately responds with the current data, regardless of whether it has changed.
- **Who controls data flow**: **Client-controlled**. The client decides when to ask for data.
- **Connection Behavior**: Creates a **new HTTP connection** for every single request. This involves repeated TCP handshakes and HTTP header overhead.
- **Scalability & Cost**: **Low**. Inefficient for the server as it must handle a flood of requests, many of which may return redundant data.
- **When to use**: Simple applications where real-time updates aren't critical, or when server resources are not a concern (e.g., a dashboard updating every 10 minutes).

### 2. ⏳ Long Polling

**"Tell me when you get there... (waiting)..."**

- **How it works**: The client sends a request, and the server **holds** the connection open until new data is available or a timeout occurs (30s in this demo). Once the client receives a response (or timeout), it immediately sends a new request.
- **Who controls data flow**: **Client-initiated**, but **Server-driven response**. The server holds the reins on _when_ to reply.
- **Connection Behavior**: Uses a **hanging HTTP request**. The connection stays open for longer periods but still requires re-establishing after every message.
- **Scalability & Cost**: **Medium**. Better than short polling for infrequent updates, but still consumes server threads/resources while waiting.
- **When to use**: When you need faster updates than short polling but cannot use WebSocket/SSE (e.g., legacy browser support or strict firewall rules).

### 3. 🌊 Server-Sent Events (SSE)

**"Here is the news... and here is more news..."**

- **How it works**: The client opens a single persistent HTTP connection. The server uses this connection to **push** text-based events to the client whenever new data is available. It is **unidirectional** (Server -> Client).
- **Who controls data flow**: **Server-controlled**. The server decides when to push updates.
- **Connection Behavior**: **Single persistent HTTP connection**. Efficient and lightweight. Reconnects automatically if dropped.
- **Scalability & Cost**: **High**. efficient for broadcasting one-way data (like stock tickers or news feeds).
- **When to use**: "One-to-many" broadcasts where the client doesn't need to send data back (e.g., live sports scores, progress bars, notifications).

### 4. 🔌 WebSocket

**"Let's talk! (Two-way conversation)"**

- **How it works**: A full-duplex communication channel over a single TCP connection. After an initial HTTP handshake, the connection "upgrades" to the WebSocket protocol. Both server and client can send data at any time.
- **Who controls data flow**: **Bidirectional**. Both parties can send data independently.
- **Connection Behavior**: **Single persistent TCP connection**. Extremely low overhead (no HTTP headers after handshake).
- **Scalability & Cost**: **Very High**. The most efficient for frequent, low-latency communication.
- **When to use**: Highly interactive applications like chat apps, multiplayer games, collaborative editing, or financial trading platforms.

---

## 📊 Comparison Table

| Feature            | Short Polling              | Long Polling                | SSE (Server-Sent Events) | WebSocket            |
| :----------------- | :------------------------- | :-------------------------- | :----------------------- | :------------------- |
| **Direction**      | Client → Server            | Client → Server (Hanging)   | Server → Client          | Bidirectional ⇄      |
| **Protocol**       | HTTP                       | HTTP                        | HTTP                     | TCP (WebSocket)      |
| **Connection**     | New per request            | New per message             | Single Persistent        | Single Persistent    |
| **Latency**        | High (depends on interval) | Medium (depends on network) | Low                      | Lowest               |
| **Server Load**    | High (frequent requests)   | High (held threads)         | Low                      | Low                  |
| **Data Format**    | Any (JSON/XML)             | Any (JSON/XML)              | Text/Event Stream        | Text or Binary       |
| **Reconnection**   | Manual Loop                | Manual Loop                 | Automatic                | Manual code required |
| **Ideal Use Case** | Non-critical updates       | Legacy compatibility        | News feeds, Dashboards   | Chat, Games, Trading |

---

## 🖥️ UI Monitor Features

The project includes a unified dashboard to visualize these differences:

- **Single Dashboard**: View all mechanisms side-by-side.
- **Switchable Transport**: Toggle buttons to Activate/Deactivate each protocol independently.
- **Live Connection Logs**: A scrolling log panel showing connection events (Connect, Disconnect, Errors) with color-coded tags for each protocol.
- **Real-Time Data Feed**: See the actual payloads arriving from the server, stamped with the protocol used.
- **Status Indicators**: "Active/Inactive" badges and message counters to track throughput.

### How to Run

1.  Open the project in your IDE.
2.  Run the `RealTimeApiApplication` class.
3.  Navigate to `http://localhost:8080` in your browser.
4.  Use the buttons to start/stop different polling/streaming methods.
