# game-server

The official game-server of cryptic-game.  
This system connects the microservices with clients via json-based sockets.

## Run!

### Build..

Build the game-server by using `mvn clean install` .  

### Execute

Run it with `java -jar target/server-0.1.0-jar-with-dependencies.jar` .

### Environment variables

| key              | default value |
| ---------------- | ------------- |
| MSSOCKET_HOST    | 127.0.0.1     |
| MSSOCKET_PORT    | 1239          |
| WEBSOCKET_HOST   | 0.0.0.0       |
| WEBSOCKET_PORT   | 80            |
| HTTP_PORT        | 8080          |
| AUTH_ENABLED     | true          |
| STORAGE_LOCATION | data/         |
| SESSION_EXPIRE   | 172800        |
| MYSQL_HOSTNAME   | cryptic       |
| MYSQL_PORT       | 3306          |
| MYSQL_USERNAME   | cryptic       |
| MYSQL_PASSWORD   | cryptic       |
| MYSQL_DATABASE   | cryptic       |
| RESPONSE_TIMEOUT | 20            |
| PRODUCTIVE       | true          |
| LOG_LEVEL        | INFO          |

## Docker

### Docker Hub

Pull the docker-image from [Docker Hub](https://hub.docker.com/r/useto/cryptic-game-server)!

## Documentation

Visit the [wiki](https://github.com/cryptic-game/server/wiki) for more information.

