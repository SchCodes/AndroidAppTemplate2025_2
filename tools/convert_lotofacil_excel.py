"""
Utility to convert `BD_full_lotoFacil.xlsx` into a JSON bundle with
basic stats for the Lotofacil draws. Intended as the same logic
you would run inside a Cloud Function Gen2 trigger after an Excel
upload to Storage.

Usage:
    python tools/convert_lotofacil_excel.py \
        --input BD_full_lotoFacil.xlsx \
        --output processed/draws.json
"""

from __future__ import annotations

import argparse
import json
import hashlib
from collections import Counter
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path
from typing import Dict, List, Tuple

import pandas as pd


Number = int


@dataclass
class Draw:
    contest_id: int
    date_iso: str
    numbers: List[Number]

    def to_dict(self) -> Dict:
        return {"id": self.contest_id, "date": self.date_iso, "numbers": self.numbers}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Convert Lotofacil Excel to JSON with stats.")
    parser.add_argument("--input", required=True, type=Path, help="Path to BD_full_lotoFacil.xlsx")
    parser.add_argument(
        "--output",
        required=True,
        type=Path,
        help="Where to write the JSON bundle (e.g., processed/draws.json)",
    )
    parser.add_argument(
        "--schema-version",
        type=int,
        default=1,
        help="Schema version to embed in the JSON metadata.",
    )
    return parser.parse_args()


def load_draws(xlsx_path: Path) -> List[Draw]:
    df = pd.read_excel(xlsx_path)
    if df.shape[1] < 17:
        raise ValueError("Unexpected Excel format: need at least 17 columns (Concurso, Data, 15 números).")

    draws: List[Draw] = []
    for _, row in df.iterrows():
        contest_id = int(row.iloc[0])
        raw_date = row.iloc[1]
        # Dates come as "DD/MM/YYYY"
        date_iso = (
            datetime.strptime(str(raw_date), "%d/%m/%Y").date().isoformat()
            if isinstance(raw_date, str)
            else pd.to_datetime(raw_date).date().isoformat()
        )
        numbers = [int(x) for x in row.iloc[2:17].tolist()]
        draws.append(Draw(contest_id, date_iso, numbers))
    return draws


def longest_consecutive_run(nums: List[Number]) -> int:
    if not nums:
        return 0
    nums_sorted = sorted(nums)
    best = cur = 1
    for idx in range(1, len(nums_sorted)):
        if nums_sorted[idx] == nums_sorted[idx - 1] + 1:
            cur += 1
            best = max(best, cur)
        else:
            cur = 1
    return best


def compute_stats(draws: List[Draw]) -> Dict:
    if not draws:
        return {}

    # All numbers across all draws
    flattened = [n for d in draws for n in d.numbers]
    freq_abs = Counter(flattened)
    total_numbers = len(flattened)

    def choose_extreme(counter: Counter, pick_max: bool) -> int:
        if not counter:
            return 0
        if pick_max:
            max_freq = max(counter.values())
            return min([n for n, c in counter.items() if c == max_freq])
        min_freq = min(counter.values())
        return min([n for n, c in counter.items() if c == min_freq])

    num_most = choose_extreme(freq_abs, pick_max=True)
    num_least = choose_extreme(freq_abs, pick_max=False)

    # Pair/odd distribution
    evens = sum(1 for n in flattened if n % 2 == 0)
    odds = total_numbers - evens
    dist_pares = {
        "pares": round((evens / total_numbers) * 100, 2),
        "impares": round((odds / total_numbers) * 100, 2),
    }

    # Mean of sums of each draw
    mean_sum = sum(sum(d.numbers) for d in draws) / len(draws)

    # Mean repeated numbers between consecutive draws (chronological)
    draws_sorted = sorted(draws, key=lambda d: d.contest_id)
    repeated_counts: List[int] = []
    for i in range(1, len(draws_sorted)):
        prev = set(draws_sorted[i - 1].numbers)
        cur = set(draws_sorted[i].numbers)
        repeated_counts.append(len(prev.intersection(cur)))
    mean_repetition = (sum(repeated_counts) / len(repeated_counts)) if repeated_counts else 0.0

    # Max sequence of consecutive numbers in any draw
    max_seq = max(longest_consecutive_run(d.numbers) for d in draws) if draws else 0

    # Interval statistics per number (contest id differences)
    last_seen: Dict[int, int] = {n: None for n in range(1, 26)}
    intervals: Dict[int, List[int]] = {n: [] for n in range(1, 26)}
    for draw in draws_sorted:
        for n in draw.numbers:
            if last_seen[n] is not None:
                intervals[n].append(draw.contest_id - last_seen[n])
            last_seen[n] = draw.contest_id

    last_contest = draws_sorted[-1].contest_id
    interval_stats = {
        str(n): {
            "media_intervalo": (sum(intervals[n]) / len(intervals[n])) if intervals[n] else None,
            "max_intervalo": max(intervals[n]) if intervals[n] else None,
            "ultima_aparicao": (last_contest - last_seen[n]) if last_seen[n] is not None else None,
        }
        for n in range(1, 26)
    }

    return {
        "frequencia_absoluta": {str(k): v for k, v in sorted(freq_abs.items())},
        "numero_mais_frequente": num_most,
        "numero_menos_frequente": num_least,
        "distribuicao_pares": dist_pares,
        "media_soma": round(mean_sum, 4),
        "media_repeticao": round(mean_repetition, 4),
        "maximo_sequencia": max_seq,
        "intervalo_aparicoes": interval_stats,
    }


def build_bundle(draws: List[Draw], schema_version: int) -> Dict:
    generated_at = datetime.utcnow().isoformat() + "Z"
    contests = [d.contest_id for d in draws]
    stats = compute_stats(draws)

    bundle = {
        "schemaVersion": schema_version,
        "generatedAt": generated_at,
        "version": int(datetime.utcnow().timestamp()),  # simple monotonic version
        "rowCount": len(draws),
        "contestMin": min(contests) if contests else None,
        "contestMax": max(contests) if contests else None,
        "numbersPerDraw": 15,
        "draws": [d.to_dict() for d in draws],
        "stats": stats,
    }

    # Compute checksum over draws+stats so clients can compare quickly
    checksum_payload = json.dumps(
        {"draws": bundle["draws"], "stats": bundle["stats"]},
        ensure_ascii=False,
        separators=(",", ":"),
        sort_keys=True,
    ).encode("utf-8")
    bundle["checksum"] = "sha256:" + hashlib.sha256(checksum_payload).hexdigest()
    return bundle


def ensure_parent_dir(path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)


def main() -> None:
    args = parse_args()
    draws = load_draws(args.input)
    bundle = build_bundle(draws, args.schema_version)
    ensure_parent_dir(args.output)
    with args.output.open("w", encoding="utf-8") as f:
        json.dump(bundle, f, ensure_ascii=False, indent=2)
    print(f"Wrote {args.output} with {bundle['rowCount']} draws. Checksum={bundle['checksum']}")


if __name__ == "__main__":
    main()
