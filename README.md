# Bank Account Command Microservice

This microservice handles the **Command** side of the [Bank Account Management System](../README.md). It processes requests to change the state of bank accounts, stores events in MongoDB, and publishes those events to Kafka.

## Key Features

- **Event Sourcing**: Stores every state change as an event in a MongoDB-backed event store.
- **RESTful API**: Provides endpoints for opening accounts, depositing/withdrawing funds, and closing accounts.
- **Kafka Integration**: Publishes events to Kafka for consumption by the query side.

## Configuration

Settings can be found in `src/main/resources/application.yaml`, including:
- Server port (`5000`)
- Kafka bootstrap servers
- MongoDB connection details

## API

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/v1/accounts` | Open a new bank account |
| `PUT` | `/v1/accounts/{id}/deposit` | Deposit funds |
| `PUT` | `/v1/accounts/{id}/withdraw` | Withdraw funds |
| `DELETE` | `/v1/accounts/{id}` | Close an account |

Refer to the main [README](../README.md) for setup and build instructions.