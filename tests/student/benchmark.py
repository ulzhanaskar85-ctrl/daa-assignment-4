
import json
import random
import statistics
import subprocess
import time
import csv
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
JAVA_BUILD = ROOT / "Java" / "build"

ALGORITHMS = ["naive", "kmp", "rk"]
SIZES = [500, 1500, 3000]
REPEATS = 5

rng = random.Random(42)


def make_cases(n):
    m = n // 3

    # Random text and an independently generated pattern
    alphabet = "abcdefghijklmnopqrstuvwxyz"
    random_text = "".join(rng.choice(alphabet) for _ in range(n))
    random_pattern = "".join(rng.choice(alphabet) for _ in range(m))

    # Repetitive text with a pattern that almost matches
    repetitive_text = "a" * n
    repetitive_pattern = "a" * (m - 1) + "b"

    return {
        "random": (random_text, random_pattern),
        "repetitive": (repetitive_text, repetitive_pattern)
    }


def main():
    command = [
        "java", "-Xmx384m",
        "-cp", str(JAVA_BUILD),
        "Main"
    ]

    results = []

    process = subprocess.Popen(
        command,
        cwd=ROOT,
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        encoding="utf-8",
        bufsize=1
    )

    try:
        def request(task, text, pattern):
            data = {
                "task": task,
                "text": text,
                "pattern": pattern
            }

            process.stdin.write(json.dumps(data) + "\n")
            process.stdin.flush()

            line = process.stdout.readline()
            if not line:
                raise RuntimeError("Java process stopped unexpectedly")

            answer = json.loads(line)
            if "positions" not in answer:
                raise RuntimeError(f"Unexpected Java output: {answer}")

            return answer["positions"]

        for n in SIZES:
            cases = make_cases(n)

            for case_name, (text, pattern) in cases.items():
                print(f"\n{case_name.upper()}, n={n}, m={len(pattern)}")

                expected = None
                timings = {algorithm: [] for algorithm in ALGORITHMS}

                # Warm-up and correctness check
                for algorithm in ALGORITHMS:
                    answer = request(algorithm, text, pattern)

                    if expected is None:
                        expected = answer
                    elif answer != expected:
                        raise AssertionError(
                            f"Different results for {algorithm}, {case_name}, n={n}"
                        )

                # Timed runs; Java process remains alive
                for repetition in range(REPEATS):
                    order = ALGORITHMS[repetition % 3:] + ALGORITHMS[:repetition % 3]

                    for algorithm in order:
                        start = time.perf_counter()
                        answer = request(algorithm, text, pattern)
                        elapsed = time.perf_counter() - start

                        if answer != expected:
                            raise AssertionError(
                                f"Incorrect result from {algorithm}"
                            )

                        timings[algorithm].append(elapsed * 1000)

                for algorithm in ALGORITHMS:
                    median_ms = statistics.median(timings[algorithm])
                    results.append({
                        "case": case_name,
                        "n": n,
                        "m": len(pattern),
                        "algorithm": algorithm,
                        "median_ms": round(median_ms, 4)
                    })

                    print(
                        f"{algorithm:6} median: {median_ms:.4f} ms"
                    )

    finally:
        process.stdin.close()
        process.wait(timeout=10)

    output = ROOT / "tests" / "student" / "benchmark_results.csv"

    with open(output, "w", newline="", encoding="utf-8") as file:
        writer = csv.DictWriter(
            file,
            fieldnames=["case", "n", "m", "algorithm", "median_ms"]
        )
        writer.writeheader()
        writer.writerows(results)

    print("\nBenchmark completed.")
    print(f"Results saved to: {output}")


if __name__ == "__main__":
    main()
