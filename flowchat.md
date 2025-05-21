flowchart TD
A[Start: Launch App] --> B{Main Menu}
B -->|"• Create chatroom"| C[Prompt for name & port]
C --> D[Check port availability]
D -->|Port in use| E[Suggest new port]
D -->|Port OK| F[Start server thread]
E --> F
F --> G[Join as host client]
B -->|"• Join chatroom"| H[Prompt for server IP/hostname & port]
H --> I[Test connection]
I -->|Fail| J{Try anyway?}
J -->|No| K[Exit]
J -->|Yes| L[Prompt for username]
I -->|Success| L
L --> M[Connect as client]
M --> N[Start message listener thread]
N --> O[Display chat commands]
O --> P[Chat input loop]
P -->|exit| Q[Disconnect & close]
B -->|"• Display network info"| R[Show hostname, IPs, port status]
R --> S[Wait for Enter]
S --> B
B -->|"• Exit"| K
G --> B
Q --> B