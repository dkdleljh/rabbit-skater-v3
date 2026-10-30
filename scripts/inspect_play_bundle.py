#!/usr/bin/env python3
"""Inventory a local APK/AAB. This is not a Play compliance verdict."""
import argparse
import hashlib
import json
from pathlib import Path
import re
import zipfile


def inspect(path: Path, category: str) -> dict:
    with zipfile.ZipFile(path) as archive:
        entries = archive.infolist()
        names = [entry.filename for entry in entries]
        if len(names) != len(set(names)):
            raise ValueError("Duplicate archive paths; inventory is ambiguous")
        if "BundleConfig.pb" in names:
            kind = "aab"
            dex_pattern = r"[^/]+/dex/classes(?:[2-9][0-9]*|1[0-9]+)?\.dex"
        elif "AndroidManifest.xml" in names:
            kind = "apk"
            dex_pattern = r"classes(?:[2-9][0-9]*|1[0-9]+)?\.dex"
        else:
            raise ValueError("Not a recognized APK or AAB")
        dex = sorted(
            ({"path": e.filename, "uncompressed_bytes": e.file_size}
             for e in entries if re.fullmatch(dex_pattern, e.filename)),
            key=lambda e: e["path"],
        )
        if not dex:
            raise ValueError("No standard DEX entries found; manual inspection required")
        native = sorted(e.filename for e in entries if e.filename.endswith(".so"))
    total = sum(entry["uncompressed_bytes"] for entry in dex)
    # Google's page says MB without specifying a byte conversion. Report both
    # conventions; never resolve a borderline case as a local compliance pass.
    threshold_mb = 50 if category == "game" else 10
    decimal_limit = threshold_mb * 1_000_000
    binary_limit = threshold_mb * 1_048_576
    if total <= decimal_limit:
        screening = "at_or_below_both_byte_conventions"
    elif total > binary_limit:
        screening = "above_both_byte_conventions"
    else:
        screening = "unit_boundary_requires_console_confirmation"
    with path.open("rb") as stream:
        digest = hashlib.file_digest(stream, "sha256").hexdigest()
    return {
        "artifact": path.name, "sha256": digest, "kind": kind,
        "category_for_screening": category,
        "dex_entries": dex, "dex_total_bytes": total,
        "dex_decimal_mb": total / 1_000_000,
        "dex_mib": total / 1_048_576,
        "threshold_label_mb": threshold_mb, "local_size_screening": screening,
        "native_entries": native,
        "limitations": [
            "Confirm Play Console store category, DEX size and optimization percentages.",
            "No optimization percentage, memory P90, permissions or SDK behavior verified.",
            "No signing, upload, deployment or policy-compliance assertion performed.",
        ],
    }


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("artifact", type=Path)
    parser.add_argument("--category", choices=("game", "app"), required=True)
    args = parser.parse_args()
    try:
        result = inspect(args.artifact, args.category)
    except (OSError, ValueError, zipfile.BadZipFile) as error:
        parser.exit(2, f"Inventory failed: {error}\n")
    print(json.dumps(result, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
