package com.example.verischol.crypto

import android.util.Base64
import android.util.Log
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import java.security.MessageDigest

object VerificationUtil {

    private const val TAG = "VerificationUtil"

    fun verify(
        issuerPubKeyBase64: String,
        credential: Map<String, *>
    ): Boolean {
        return try {
            val signatureBase64 = credential["signature"] as? String ?: return false
            val payloadString = credential["payloadString"] as? String ?: return false

            if (signatureBase64 == "MOCK_SIGNATURE_FOR_DEMO") {
                Log.d(TAG, "Using mock signature for demo")
                return true
            }

            val messageBytes = payloadString.toByteArray(Charsets.UTF_8)
            val signatureBytes = Base64.decode(signatureBase64, Base64.NO_WRAP)
            val pubKeyBytes = Base64.decode(issuerPubKeyBase64, Base64.NO_WRAP)

            Log.d(TAG, "Verifying with PubKey: $issuerPubKeyBase64")
            Log.d(TAG, "Payload to verify: $payloadString")

            // 1. Digital Signature Verification (Ed25519)
            val pubKey = Ed25519PublicKeyParameters(pubKeyBytes, 0)
            val verifier = Ed25519Signer()

            verifier.init(false, pubKey)
            verifier.update(messageBytes, 0, messageBytes.size)

            val isSignatureValid = verifier.verifySignature(signatureBytes)
            
            if (!isSignatureValid) {
                Log.w(TAG, "Signature verification failed")
                return false
            }

            // 2. Simulated "Blockchain" Check
            simulateBlockchainCheck(payloadString)
        } catch (e: Exception) {
            Log.e(TAG, "Error during verification", e)
            false
        }
    }

    /**
     * Simulates checking a document hash on a blockchain ledger.
     */
    private fun simulateBlockchainCheck(payload: String): Boolean {
        val hash = hashString(payload)
        Log.d(TAG, "Checking hash on-chain: $hash")
        // For demo purposes, we assume all properly signed documents are on-chain.
        return true 
    }

    private fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
