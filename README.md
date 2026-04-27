# VeriSchol

VeriSchol is an end-to-end system for issuing and verifying digital credentials (Verifiable Credentials - VCs) utilizing an Android app and a mock Express.js issuer backend. The system signs credentials utilizing `tweetnacl` (Ed25519) and validates them on the Android client using BouncyCastle.

## Project Structure

*   **/app (Android App)**: Built with Jetpack Compose, Kotlin, Room Database, CameraX + ML Kit (Barcode Scanning), and Retrofit for network calls. The app enables users to verify credentials and manage them locally.
*   **index.js (Mock Issuer)**: A Node.js backend using Express that issues Verifiable Credentials (signed using Ed25519 public key cryptography) and exposes endpoints for credential issuance and verification.

## Features
*   📱 **Android Frontend**: Modern UI with Jetpack Compose.
*   📸 **QR/Barcode Scanning**: Scan credentials easily using ML Kit Barcode Scanning via CameraX.
*   🔐 **Local Storage**: Save verified credentials locally using Room Database.
*   🗝️ **Ed25519 Signatures**: Cryptographic signing and validation of payloads.

## Prerequisites
*   [Node.js](https://nodejs.org/en/) (v14 or higher)
*   [Android Studio](https://developer.android.com/studio) (Koala/Ladybug or later)
*   JDK 17 (or 11 as required by the gradle daemon)

## Setup & Running

### 1. Mock Issuer Backend
Navigate to the root directory and start the Express server.

```bash
# Install dependencies
npm install

# Start the mock issuer
npm start
# or
node index.js
```
*Note: This will generate an `issuer-key.json` the first time it runs, which acts as your mock private/public keypair. This file is `.gitignore`d to prevent leaking the secret key.*

The server will run on `http://localhost:8080`.

### 2. Android App
1. Open the repository root folder in **Android Studio**.
2. Sync the project with Gradle files.
3. Update specific IP configurations in the Android app (often needed to talk to `localhost` from an emulator/device — use `10.0.2.2` for emulator or your machine's local IP address for a physical device).
4. Run the app on an emulator or physical device.

## API Endpoints (Backend)
*   **POST** `/issue`
    *   Issues a Verifiable DegreeCredential with a cryptographic signature.
    *   Body: `{ "subject": { "id": "...", "name": "Student Name" } }`
*   **POST** `/verify`
    *   Verifies a Verifiable Credential's signature.
    *   Body: `{ "vc": { ... } }`

## Technologies Used
*   **Android**: Kotlin, Jetpack Compose, Retrofit, Room, CameraX, ML Kit, BouncyCastle
*   **Backend**: Node.js, Express, `tweetnacl`, `uuid`

## Preview

![democredentials.png](/assets/demo.png)

![homepage.png](/assets/home%20(2).png)