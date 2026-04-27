package com.example.verischol.crypto

import android.util.Base64
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer

object VerificationUtil {

    fun verify(
        issuerPubKeyBase64: String,
        credential: Map<String, *>
    ): Boolean {
        return try {
            val signatureBase64 = credential["signature"] as? String ?: return false
            val payloadString = credential["payloadString"] as? String ?: return false

            val messageBytes = payloadString.toByteArray(Charsets.UTF_8)
            val signatureBytes = Base64.decode(signatureBase64, Base64.NO_WRAP)
            val pubKeyBytes = Base64.decode(issuerPubKeyBase64, Base64.NO_WRAP)

            val pubKey = Ed25519PublicKeyParameters(pubKeyBytes, 0)
            val verifier = Ed25519Signer()

            verifier.init(false, pubKey)
            verifier.update(messageBytes, 0, messageBytes.size)

            verifier.verifySignature(signatureBytes)
        } catch (e: Exception) {
            false
        }
    }
}
