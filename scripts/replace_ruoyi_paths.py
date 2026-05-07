# -*- coding: utf-8 -*-
"""Update path references after repo layout migration. Does not rewrite git mv lines."""
import pathlib

root = pathlib.Path(__file__).resolve().parent.parent
SKIP_SUBSTR = "\u76ee\u5f55\u91cd\u6784\u4e0e\u5f00\u53d1\u6307\u5357.md"


def patch_text(text: str) -> str:
    text = text.replace("F:\\chuangye\\RuoYi-Vue-master", "F:\\chuangye\\apps\\qiyuan-backend")
    text = text.replace("RuoYi-Vue-master/", "apps/qiyuan-backend/")
    return text


def read_text(path: pathlib.Path) -> str:
    raw = path.read_bytes()
    for enc in ("utf-8-sig", "utf-8", "gbk"):
        try:
            return raw.decode(enc)
        except UnicodeDecodeError:
            continue
    return raw.decode("utf-8", errors="replace")


def main() -> None:
    paths = list(root.glob("docs/**/*.md"))
    paths.append(root / "CLAUDE.md")
    paths.extend(root.glob(".idea/*.xml"))
    seen = set()
    for p in paths:
        if not p.is_file() or p in seen:
            continue
        seen.add(p)
        if SKIP_SUBSTR in p.name:
            continue
        old = read_text(p)
        new = patch_text(old)
        if new != old:
            p.write_text(new, encoding="utf-8-sig", newline="\n")
            print("updated", p.relative_to(root))


if __name__ == "__main__":
    main()
