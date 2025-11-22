import datetime
import hashlib
import json
from typing import Optional, Tuple
import os

import firebase_admin
import functions_framework
from firebase_admin import auth, credentials, db, storage

# Inicializa Firebase Admin apenas uma vez
if not firebase_admin._apps:
    cred = credentials.ApplicationDefault()
    project_id = os.environ.get("GCP_PROJECT") or os.environ.get("GCLOUD_PROJECT")
    default_project = project_id or "baseforfirebase-f3545"
    storage_bucket = os.environ.get("FIREBASE_STORAGE_BUCKET")
    database_url = os.environ.get("FIREBASE_DATABASE_URL")
    if not storage_bucket:
        # Firebase para este projeto usa domínio firebasestorage.app
        storage_bucket = f"{default_project}.firebasestorage.app"
    if not database_url:
        database_url = f"https://{default_project}-default-rtdb.firebaseio.com"

    firebase_admin.initialize_app(
        cred,
        {
            "storageBucket": storage_bucket,
            "databaseURL": database_url,
        },
    )


def verify_admin(request) -> Optional[str]:
    """Valida o token JWT do Firebase e checa o claim admin. Retorna UID se ok."""
    auth_header = request.headers.get("Authorization", "")
    if not auth_header.startswith("Bearer "):
        return None
    token_str = auth_header.split(" ", 1)[1].strip()
    decoded = auth.verify_id_token(token_str)
    if not decoded or not decoded.get("admin"):
        return None
    return decoded.get("uid")


def compute_checksum(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def update_dataset(bundle_bytes: bytes) -> dict:
    """
    Upload de processed/draws.json no Storage e atualização de metadata no RTDB.
    bundle_bytes: conteúdo do JSON de sorteios (bytes).
    """
    # Grava no Storage
    bucket = storage.bucket()
    blob = bucket.blob("processed/draws.json")
    blob.upload_from_string(bundle_bytes, content_type="application/json")

    # Atualiza metadata no RTDB
    checksum = compute_checksum(bundle_bytes)
    now = datetime.datetime.utcnow().isoformat() + "Z"
    ref = db.reference("/metadata/latest")
    ref.set({
        "generatedAt": now,
        "version": checksum[:12],
        "checksum": checksum,
    })
    return {"generatedAt": now, "checksum": checksum}


def parse_body(request) -> Tuple[bytes, bool]:
    """
    Lê o corpo JSON da requisição. Se não houver body, usa stub.
    Retorna (bytes_do_json, usado_stub: bool).
    """
    if request.data:
        return request.data, False
    raise ValueError("Body vazio: envie o JSON de draws (processed/draws.json).")


@functions_framework.http
def update_bundle(request):
    """Endpoint HTTP protegido por token Firebase com claim admin."""
    uid = verify_admin(request)
    if not uid:
        return ("Unauthorized", 401)

    try:
        bundle_bytes, used_stub = parse_body(request)
        result = update_dataset(bundle_bytes)
        return (
            json.dumps({"ok": True, "result": result, "used_stub": used_stub}),
            200,
            {"Content-Type": "application/json"},
        )
    except ValueError as exc:
        return (
            json.dumps({"ok": False, "error": str(exc)}),
            400,
            {"Content-Type": "application/json"},
        )
    except Exception as exc:
        return (
            json.dumps({"ok": False, "error": str(exc)}),
            500,
            {"Content-Type": "application/json"},
        )
