"""
Upload the generated Lotofacil bundle JSON to Firebase (Realtime Database + Storage)
and optionally set a user as admin (custom claim).

Prereqs:
- Have a Firebase project (Spark is fine for this flow).
- Download a service account key (Project Settings -> Service Accounts -> Generate new private key).
- Install deps: `python -m pip install firebase-admin`
- Generate the bundle first:
    $env:PYTHONPATH='_pylib'; python tools/convert_lotofacil_excel.py --input BD_full_lotoFacil.xlsx --output processed/draws.json

Usage (PowerShell):
    $env:FIREBASE_PROJECT_ID='<your-project-id>'
    $env:FIREBASE_DATABASE_URL='https://<your-project-id>-default-rtdb.firebaseio.com'
    $env:FIREBASE_STORAGE_BUCKET='<your-project-id>.appspot.com'
    python tools/upload_bundle.py --key path/to/serviceAccountKey.json --bundle processed/draws.json

Optional: set admin claim
    python tools/upload_bundle.py --key ... --bundle ... --set-admin <firebase-uid>
"""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any, Dict, Optional

import firebase_admin
from firebase_admin import auth, credentials, db, storage


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Upload Lotofacil bundle to Firebase.")
    parser.add_argument("--key", type=Path, required=True, help="Path to serviceAccountKey.json")
    parser.add_argument("--bundle", type=Path, required=True, help="Path to processed/draws.json")
    parser.add_argument(
        "--database-url",
        type=str,
        default=None,
        help="Realtime Database URL (override env FIREBASE_DATABASE_URL).",
    )
    parser.add_argument(
        "--storage-bucket",
        type=str,
        default=None,
        help="Storage bucket (override env FIREBASE_STORAGE_BUCKET).",
    )
    parser.add_argument(
        "--metadata-path",
        type=str,
        default="/metadata/latest",
        help="RTDB path to write metadata (default: /metadata/latest).",
    )
    parser.add_argument(
        "--storage-path",
        type=str,
        default="processed/draws.json",
        help="Storage object path for the bundle file.",
    )
    parser.add_argument(
        "--set-admin",
        type=str,
        default=None,
        help="Optional: Firebase UID to receive custom claim admin=True.",
    )
    return parser.parse_args()


def load_bundle(path: Path) -> Dict[str, Any]:
    with path.open("r", encoding="utf-8") as f:
        return json.load(f)


def init_firebase(key_path: Path, db_url: Optional[str], bucket: Optional[str]) -> None:
    cred = credentials.Certificate(str(key_path))
    opts = {}
    if db_url:
        opts["databaseURL"] = db_url
    if bucket:
        opts["storageBucket"] = bucket
    firebase_admin.initialize_app(cred, opts)


def upload_to_storage(bundle_path: Path, bucket_path: str) -> None:
    bucket = storage.bucket()
    blob = bucket.blob(bucket_path)
    blob.upload_from_filename(str(bundle_path))
    # Make sure it is readable to authenticated users; keep private otherwise.
    blob.content_type = "application/json"
    blob.patch()
    print(f"[storage] Uploaded {bundle_path} to gs://{bucket.name}/{bucket_path}")


def write_metadata(metadata_path: str, bundle: Dict[str, Any]) -> None:
    wanted_keys = [
        "version",
        "checksum",
        "generatedAt",
        "schemaVersion",
        "rowCount",
        "contestMin",
        "contestMax",
        "numbersPerDraw",
    ]
    data = {k: bundle.get(k) for k in wanted_keys}
    ref = db.reference(metadata_path)
    ref.set(data)
    print(f"[rtdb] Wrote metadata at {metadata_path}: {data}")


def set_admin_claim(uid: str) -> None:
    auth.set_custom_user_claims(uid, {"admin": True})
    print(f"[auth] Set admin=True for uid={uid}")


def main() -> None:
    args = parse_args()
    bundle = load_bundle(args.bundle)
    init_firebase(args.key, args.database_url, args.storage_bucket)

    # Upload JSON to Storage
    if args.storage_bucket:
        upload_to_storage(args.bundle, args.storage_path)
    else:
        print("[storage] Skipped: no storage bucket configured.")

    # Write metadata to RTDB
    write_metadata(args.metadata_path, bundle)

    # Optionally set admin claim
    if args.set_admin:
        set_admin_claim(args.set_admin)


if __name__ == "__main__":
    try:
        main()
    except Exception as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        sys.exit(1)
