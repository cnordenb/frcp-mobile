import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(subprocess.run(["git", "rev-parse", "--show-toplevel"], capture_output=True, text=True).stdout.strip())
TESTS = ROOT / "app/src/test/java"
SUITE = TESTS / "net/frcp/app/AllTests.kt"
DOC = ROOT / "documentation/TESTS.md"
CONTENTS = "## Table of contents per category"
CATEGORIES = [
    "Functional completeness & correctness tests:",
    "Accessibility tests:",
    "Maintainability tests:",
]
TEST = re.compile(r"@Test(?:\([^)]*\))?\s+fun\s+(?:t(\d+)_)?(\w+)\s*\(")
STUB = "TO WRITE"


def read(path):
    return path.read_text(encoding="utf-8").replace("\r\n", "\n")


def write(path, text):
    path.write_text(text, encoding="utf-8", newline="\n")


def suite_files():
    text = read(SUITE)
    imports = dict((i.split(".")[-1], i) for i in re.findall(r"^import (net\.frcp\.app\.[\w.]+)$", text, re.M))
    names = re.findall(r"^\s+(\w+)::class,", text, re.M)
    missing = [n for n in names if n not in imports]
    if missing:
        sys.exit("AllTests.kt lists classes it does not import: " + ", ".join(missing))
    return [TESTS / (imports[n].replace(".", "/") + ".kt") for n in names]


def last_line(lines, start):
    indent = len(lines[start]) - len(lines[start].lstrip())
    depth = 0
    opened = False
    for n in range(start, len(lines)):
        depth += lines[n].count("{") - lines[n].count("}")
        opened = opened or "{" in lines[n]
        if opened and depth <= 0:
            return n
        following = next((line for line in lines[n + 1:] if line.strip()), None)
        if not opened and (following is None or len(following) - len(following.lstrip()) <= indent):
            return n
    return len(lines) - 1


def residence(path, text, name):
    lines = text.split("\n")
    at = next(n for n, line in enumerate(lines) if re.search(r"\bfun\s+" + name + r"\s*\(", line))
    first = at
    while first > 0 and lines[first - 1].strip().startswith("@"):
        first -= 1
    last = last_line(lines, at)
    span = f"lines {first + 1}-{last + 1}" if last > first else f"line {first + 1}"
    return f"`{path.relative_to(ROOT).as_posix()}`, {span}"


def renumber_code(files):
    found = [(path, m.group(1), m.group(2)) for path in files for m in TEST.finditer(read(path))]
    width = max(3, len(str(len(found))))
    tests = []
    for number, (path, old, method) in enumerate(found, 1):
        tests.append({"path": path, "old": old and int(old), "method": method, "number": number})
        tests[-1]["name"] = f"t{number:0{width}d}_{method}"
    for path in files:
        text = read(path)
        for t in (t for t in tests if t["path"] == path):
            text = re.sub(r"\bfun\s+(?:t\d+_)?" + t["method"] + r"\s*\(", "fun " + t["name"] + "(", text, count=1)
        write(path, text)
        for t in (t for t in tests if t["path"] == path):
            t["residence"] = residence(path, text, t["name"])
    return tests


def old_entries(body):
    entries = {}
    for block in re.split(r"(?m)^(?=## Test \d+$)", body):
        m = re.match(r"## Test (\d+)\n", block)
        if m:
            entries[int(m.group(1))] = block
    return entries


def old_contents(body):
    listed = {}
    category = None
    for line in body.split("\n"):
        if line in CATEGORIES:
            category = line
        m = re.match(r"- \[Test (\d+)\]\(#test-\d+\): (.*)$", line)
        if m:
            listed[int(m.group(1))] = (category, m.group(2))
    return listed


def words(method):
    return re.sub(r"([a-z0-9])([A-Z])", r"\1 \2", method).lower()


def entry(t, entries):
    block = entries.get(t["old"])
    if block is None:
        fields = ["Purpose", "Assertion expectation", "Origin"]
        block = "## Test 0\n\n### File residence:\nx\n\n" + "".join(f"### {f}:\n{STUB}\n\n" for f in fields)
    block = re.sub(r"\A## Test \d+", f"## Test {t['number']}", block)
    block = re.sub(r"(### File residence:\n).*", lambda m: m.group(1) + t["residence"], block, count=1)
    return block.rstrip("\n") + "\n"


def rewrite_doc(tests):
    head, body = read(DOC).split(CONTENTS, 1)
    entries = old_entries(body)
    listed = old_contents(body)
    by_class = {}
    for t in tests:
        category, text = listed.get(t["old"], (None, words(t["method"])))
        t["text"] = text
        if category:
            by_class[t["path"]] = category
    out = [head.rstrip("\n"), "", CONTENTS, ""]
    for category in CATEGORIES:
        out += [category, ""]
        out += [
            f"- [Test {t['number']}](#test-{t['number']}): {t['text']}"
            for t in tests if by_class.get(t["path"], CATEGORIES[0]) == category
        ]
        out.append("")
    out += [entry(t, entries) for t in tests]
    write(DOC, "\n".join(out).rstrip("\n") + "\n")
    return [t["number"] for t in tests if t["old"] not in entries]


def main():
    tests = renumber_code(suite_files())
    new = rewrite_doc(tests)
    moved = sum(1 for t in tests if t["old"] != t["number"])
    print(f"{len(tests)} tests, {moved} renumbered.")
    if new:
        print(f"Write the fields marked {STUB} in documentation/TESTS.md for: " + ", ".join(f"Test {n}" for n in new))


main()
