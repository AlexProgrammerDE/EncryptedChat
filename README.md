# EncryptedChat
Secure chat messaging through your terminal.

## Execution Instructions

### Make gradle executable
```bash
chmod +x gradlew
```

### Create EncryptedChat Database
```bash
./gradlew createServerDatabase
```

This writes to a local sqlite database. The database file will be called `server.sqlite`.

### Generate java code based on the server database
```bash
./gradlew generateJooq
```

### Run the application
```bash
./gradlew run
```

## Usage Instructions

When you are given to input some text, you will see the question above your cursor.
Simply press enter if you want to use the default value.

### Generate a platform certificate

Select the instance type "GENERATE_CERTIFICATE" and a new certificate will be generated.

### Start the server

Select instance type "SERVER" and the server will start. (You need to generate a certificate first)

#### Server commands

##### Stop the server

Execute the command: `stop` to stop the server.

### List all connected users

Execute the command: `list` to list all connected users.

### Start the client

Select instance type "CLIENT" and the client will start. (You need to generate a certificate first)

#### Client commands

##### Create a group

Execute the command: `group create <name>` to create a group.

##### Join a group

Execute the command: `group join <name>` to join a group.

##### Add a user to the current group

Execute the command: `group add <username>` to add a user to the current group.

##### Say something in the current group

Execute the command: `group say <message (Can be multiple words)>` to say something in the current group.

##### Say something to everyone on the platform

Execute the command: `sayunsafe <message (Can be multiple words)>` to say something to everyone on the platform. This is not encrypted.

##### Stop the client

Execute the command: `stop` to stop the client.
