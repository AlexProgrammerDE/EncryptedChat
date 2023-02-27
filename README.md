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

### Generate a server certificate

Select the instance type "GENERATE_CERTIFICATE" and a new certificate will be generated.

### Start the server

Select instance type "SERVER" and the server will start.

### Start the client

Select instance type "CLIENT" and the client will start.

