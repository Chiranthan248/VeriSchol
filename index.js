import express from "express";
import bodyParser from "body-parser";
import nacl from "tweetnacl";
import util from "tweetnacl-util";
import fs from "fs";
import { v4 as uuidv4 } from "uuid";

// ----------------------
// Load or create keys
// ----------------------
const keyFile = "issuer-key.json";

let keys;

if (fs.existsSync(keyFile)) {
  keys = JSON.parse(fs.readFileSync(keyFile));
} else {
  const kp = nacl.sign.keyPair();
  keys = {
    publicKey: util.encodeBase64(kp.publicKey),
    secretKey: util.encodeBase64(kp.secretKey),
  };
  fs.writeFileSync(keyFile, JSON.stringify(keys, null, 2));
}

const issuerDid = "did:key:" + keys.publicKey;
console.log("Issuer DID:", issuerDid);

// ----------------------
// Build Express app
// ----------------------
const app = express();
app.use(bodyParser.json());

// SIGN (ISSUE) VC
// SIGN (ISSUE) VC — updated to include payloadString
app.post("/issue", async (req, res) => {
  try {
    const subject = req.body.subject || {
      id: "urn:uuid:" + uuidv4(),
      name: "Unknown Student",
    };

    const unsigned = {
      iss: issuerDid,
      sub: subject.id,
      vc: {
        "@context": ["https://www.w3.org/2018/credentials/v1"],
        type: ["VerifiableCredential", "DegreeCredential"],
        credentialSubject: subject,
      },
      iat: Date.now(),
    };

    // EXACT string that we sign (Node's JSON.stringify output)
    const payloadString = JSON.stringify(unsigned);


    const message = util.decodeUTF8(payloadString);

    const signature = nacl.sign.detached(
      message,
      util.decodeBase64(keys.secretKey)
    );

    const jwt = {
      payload: unsigned,
      payloadString: payloadString,      // <-- include exact signed string
      signature: util.encodeBase64(signature),
    };

    res.json({ vc: jwt });
  } catch (e) {
    res.status(500).json({ error: e.toString() });
  }
});


// VERIFY VC
app.post("/verify", async (req, res) => {
  try {
    const { payload, signature } = req.body.vc;

    const message = util.decodeUTF8(JSON.stringify(payload));

    const valid = nacl.sign.detached.verify(
      message,
      util.decodeBase64(signature),
      util.decodeBase64(keys.publicKey)
    );

    res.json({ verified: valid });
  } catch (e) {
    res.status(400).json({ error: e.toString() });
  }
});

app.listen(8080, () => console.log("Issuer running on http://localhost:8080"));
