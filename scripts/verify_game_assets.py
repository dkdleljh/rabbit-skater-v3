#!/usr/bin/env python3
"""Verify actual APK/AAB asset bytes against the repository assets.

This checks packaging only. It does not certify gameplay, signing, privacy,
native compatibility, or readiness for Google Play.
"""
import argparse
import hashlib
from pathlib import Path
import zipfile


def verify(artifact: Path, asset_dir: Path) -> None:
    expected = {p.relative_to(asset_dir).as_posix(): p
                for p in asset_dir.rglob("*") if p.is_file()}
    if not expected:
        raise SystemExit(f"No reference assets found: {asset_dir}")
    with zipfile.ZipFile(artifact) as archive:
        # 파일 확장자를 신뢰하지 않고 AAB의 실제 내부 구조로 구분합니다.
        prefix = "base/assets/" if "BundleConfig.pb" in archive.namelist() else "assets/"
        names = set(archive.namelist())
        missing = [name for name in expected if prefix + name not in names]
        changed = [name for name, source in expected.items()
                   if prefix + name in names
                   and hashlib.sha256(archive.read(prefix + name)).digest()
                   != hashlib.sha256(source.read_bytes()).digest()]
    if missing or changed:
        raise SystemExit(f"{artifact}: missing={missing}, changed={changed}")
    print(f"{artifact}: verified {len(expected)} packaged assets (SHA-256)")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("artifacts", type=Path, nargs="+")
    parser.add_argument("--assets", type=Path,
                        default=Path(__file__).resolve().parents[1] / "assets")
    args = parser.parse_args()
    for artifact in args.artifacts:
        verify(artifact, args.assets)
