# System Agents and Roles

This document outlines the user roles and their capabilities within the Receiving Service.

## Roles

-   **`BOX_MANAGER` (Manager)**: A supervisor who orchestrates the receiving process.
-   **`BOX_CAT` (Worker)**: An operator who performs the physical scanning of goods.

---

## Permissions

### Manager (`BOX_MANAGER`)

| Endpoint                               | Method | Description                               |
| -------------------------------------- | ------ | ----------------------------------------- |
| `/api/auth/box-cats`                   | `POST` | Registers a new worker (`BOX_CAT`).       |
| `/api/goods-receipts/open`             | `POST` | Opens a new goods receipt for receiving.  |
| `/api/goods-receipts/{receipt-id}/close` | `POST` | Closes an active goods receipt.           |
| `/api/goods-receipts`                  | `GET`  | Retrieves a list of goods receipts.       |

### Worker (`BOX_CAT`)

| Endpoint                                    | Method | Description                                         |
| ------------------------------------------- | ------ | --------------------------------------------------- |
| `/api/receiving-sessions/{receiptId}/join`  | `POST` | Joins a goods receipt to start a session.           |
| `/api/receiving-sessions/scans/handling-units` | `POST` | Scans a handling unit (e.g., pallet, box).          |
| `/api/receiving-sessions/scans/handling-units/content` | `POST` | Scans the content (SKU) of a handling unit.       |
| `/api/receiving-sessions/handling-units/navigate-back` | `PATCH`| Navigates to the parent handling unit.            |
| `/api/receiving-sessions/worker-sessions/complete` | `POST` | Marks the current worker's session as complete.     |
| `/api/goods-receipts`                       | `GET`  | Retrieves a list of goods receipts.                 |

### Unauthenticated

| Endpoint       | Method | Description                                  |
| -------------- | ------ | -------------------------------------------- |
| `/api/auth/login` | `POST` | Authenticates a user and returns a JWT.      |
| `/swagger-ui/` | `GET`  | Provides API documentation.                  |
| `/v3/api-docs` | `GET`  | Provides the OpenAPI specification.          |

---

## Architecture Overview

### Main Services

-   **`GoodsReceiptService`**: Manages the lifecycle of a goods receipt (open, close, view).
-   **`ReceivingProcessService`**: Handles worker sessions and scanning logic.
-   **`InboundDeliveryService`**: Integrates with the WMS to validate scans against the ASN.
-   **`DiscrepanciesReportService`**: Compares expected vs. actual quantities and publishes a report.
-   **`AuthService`**: Manages user authentication and registration.

### Integrations (Kafka)

-   **Outgoing**: Publishes a `DiscrepanciesReport` on topic `goods.received.v1` when a receipt is closed. Publishes a `WorkerSessionClosedEvent` on topic `worker.session.closed.v1` when a worker completes their session.
-   **Incoming**: None. The service fetches ASN data synchronously.