# game-server [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=cryptic-game_server&metric=coverage)](https://sonarcloud.io/dashboard?id=cryptic-game_server)

The official game-server of cryptic-game.  
This system connects the microservices with clients via json-based sockets.

## Run!

### Build..

Build the game-server by using `mvn clean install` .  

### Execute

Run it with `java -jar target/server-0.1.0-jar-with-dependencies.jar` .

### Environment variables

| key              | default value    |
| ---------------- | ---------------- |
| MSSOCKET_HOST    | 127.0.0.1        |
| MSSOCKET_PORT    | 1239             |
| WEBSOCKET_HOST   | 0.0.0.0          |
| WEBSOCKET_PORT   | 80               |
| HTTP_PORT        | 8080             |
| AUTH_ENABLED     | true             |
| STORAGE_LOCATION | data/            |
| SESSION_EXPIRE   | 172800           |
| SQL_SERVER_TYPE  | [MARIADB_10_03](https://github.com/cryptic-game/server/blob/master/src/main/java/net/cryptic_game/server/sql/SqlServerType.java#L11-L14)    |
| SQL_SERVER_LOCATION     | //localhost:3306 |
| SQL_SERVER_USERNAME     | cryptic          |
| SQL_SERVER_PASSWORD     | cryptic          |
| SQL_SERVER_DATABASE     | cryptic          |
| RESPONSE_TIMEOUT | 20               |
| PRODUCTIVE       | true             |
| LOG_LEVEL        | WARN             |
| SENTRY_DSN       |                  |

## Docker

### Docker Hub

Pull the docker-image from [Docker Hub](https://hub.docker.com/r/crypticcp/cryptic-game-server)!

## Documentation

Visit the [wiki](https://github.com/cryptic-game/server/wiki) for more information.

