#!/usr/bin/env python3
"""Generate all Sus Archaeology resources, generated Java data, and pixel art."""

from __future__ import annotations

import io
import json
import math
import random
import zipfile
import zlib
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
TOOLS = ROOT / "tools"
RESOURCES = ROOT / "src" / "main" / "resources"
ASSETS = RESOURCES / "assets" / "susarchy"
DATA = RESOURCES / "data" / "susarchy"
JAVA = ROOT / "src" / "main" / "java" / "com" / "aelux" / "susarchy"

CLIENT_JAR = Path(
    r"C:\Users\85198\.gradle\caches\neoformruntime\artifacts\minecraft_1.21.1_client.jar"
)

BRUSHES = [
    {
        "id": "feather_brush",
        "en": "Feather Brush",
        "zh": "羽毛刷",
        "bristle": "#F2EFE4",
        "bristle_dark": "#BCB5A3",
        "ferrule": "#C7A96B",
        "ferrule_dark": "#7B6037",
        "glint": "#FFFFFF",
        "feather": True,
    },
    {
        "id": "wood_brush",
        "en": "Wood Brush",
        "zh": "木刷",
        "bristle": "#B97A3C",
        "bristle_dark": "#71431D",
        "ferrule": "#8A5A2B",
        "ferrule_dark": "#4A2B10",
        "glint": "#E0B06A",
        "feather": False,
    },
    {
        "id": "stone_brush",
        "en": "Stone Brush",
        "zh": "石刷",
        "bristle": "#A8A8A8",
        "bristle_dark": "#686868",
        "ferrule": "#787878",
        "ferrule_dark": "#3E3E3E",
        "glint": "#D8D8D8",
        "feather": False,
    },
    {
        "id": "flint_brush",
        "en": "Flint Brush",
        "zh": "燧石刷",
        "bristle": "#53515A",
        "bristle_dark": "#29282E",
        "ferrule": "#3B3942",
        "ferrule_dark": "#1B1A1F",
        "glint": "#A9A6B5",
        "feather": False,
    },
    {
        "id": "iron_brush",
        "en": "Iron Brush",
        "zh": "铁刷",
        "bristle": "#D6DCE1",
        "bristle_dark": "#8D969D",
        "ferrule": "#AAB3BA",
        "ferrule_dark": "#586168",
        "glint": "#FFFFFF",
        "feather": False,
    },
    {
        "id": "gold_brush",
        "en": "Gold Brush",
        "zh": "金刷",
        "bristle": "#FFE47A",
        "bristle_dark": "#C79517",
        "ferrule": "#F1C438",
        "ferrule_dark": "#90670D",
        "glint": "#FFF8C2",
        "feather": False,
    },
    {
        "id": "diamond_brush",
        "en": "Diamond Brush",
        "zh": "钻石刷",
        "bristle": "#8BF6EA",
        "bristle_dark": "#2A9F99",
        "ferrule": "#54D8CD",
        "ferrule_dark": "#1C716D",
        "glint": "#E0FFFC",
        "feather": False,
    },
    {
        "id": "netherite_brush",
        "en": "Netherite Brush",
        "zh": "下界合金刷",
        "bristle": "#795B6E",
        "bristle_dark": "#3A2A36",
        "ferrule": "#544154",
        "ferrule_dark": "#22161F",
        "glint": "#C18FC0",
        "feather": False,
    },
]

BRUSH_MATERIALS = [
    ("wood_brush", "feather_brush", "minecraft:oak_planks"),
    ("stone_brush", "wood_brush", "minecraft:cobblestone"),
    ("flint_brush", "stone_brush", "minecraft:flint"),
    ("iron_brush", "flint_brush", "minecraft:iron_ingot"),
    ("gold_brush", "iron_brush", "minecraft:gold_ingot"),
    ("diamond_brush", "gold_brush", "minecraft:diamond"),
    ("netherite_brush", "diamond_brush", "minecraft:netherite_ingot"),
]


def ensure(path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)


def write_json(path: Path, value) -> None:
    ensure(path)
    path.write_text(json.dumps(value, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def load_blocks() -> list[dict]:
    return json.loads((TOOLS / "blocks.json").read_text(encoding="utf-8"))


def normalize_block_texture(image: Image.Image) -> Image.Image:
    image = image.convert("RGBA")
    if image.height > 16:
        image = image.crop((0, 0, 16, 16))
    if image.size != (16, 16):
        image = image.resize((16, 16), Image.Resampling.NEAREST)
    return image


def load_vanilla_texture(name: str) -> Image.Image:
    with zipfile.ZipFile(CLIENT_JAR) as jar:
        raw = jar.read(f"assets/minecraft/textures/block/{name}.png")
    with Image.open(io.BytesIO(raw)) as image:
        return normalize_block_texture(image)


def load_custom_texture(relative_path: str) -> Image.Image:
    path = ROOT / relative_path
    if not path.is_file():
        raise SystemExit(f"missing custom texture: {path}")
    with Image.open(path) as image:
        return normalize_block_texture(image)


def load_vanilla_item_texture(name: str) -> Image.Image:
    with zipfile.ZipFile(CLIENT_JAR) as jar:
        raw = jar.read(f"assets/minecraft/textures/item/{name}.png")
    return Image.open(io.BytesIO(raw)).convert("RGBA")


def load_vanilla_model(path: str) -> dict:
    with zipfile.ZipFile(CLIENT_JAR) as jar:
        return json.loads(jar.read(f"assets/minecraft/{path}"))


def replace_strings(value, replacements: dict[str, str]):
    if isinstance(value, str):
        return replacements.get(value, value)
    if isinstance(value, list):
        return [replace_strings(entry, replacements) for entry in value]
    if isinstance(value, dict):
        return {key: replace_strings(entry, replacements) for key, entry in value.items()}
    return value


def hex_rgb(value: str) -> tuple[int, int, int]:
    value = value.lstrip("#")
    return tuple(int(value[i : i + 2], 16) for i in (0, 2, 4))


def tint(image: Image.Image, color: str) -> Image.Image:
    out = image.copy()
    px = out.load()
    tr, tg, tb = hex_rgb(color)
    for y in range(16):
        for x in range(16):
            r, g, b, a = px[x, y]
            if a:
                px[x, y] = (r * tr // 255, g * tg // 255, b * tb // 255, a)
    return out


def blend_pixel(px, x: int, y: int, color: tuple[int, int, int], amount: float) -> None:
    if not (0 <= x < 16 and 0 <= y < 16):
        return
    r, g, b, a = px[x, y]
    if a == 0:
        return
    nr = int(r * (1.0 - amount) + color[0] * amount)
    ng = int(g * (1.0 - amount) + color[1] * amount)
    nb = int(b * (1.0 - amount) + color[2] * amount)
    px[x, y] = (nr, ng, nb, a)


_SUSPICIOUS_TEMPLATE = None
_LAYERED_TEMPLATES = None


def signed_luma_delta(original: Image.Image, suspicious: Image.Image):
    original_px = original.load()
    suspicious_px = suspicious.load()
    template = [[0.0 for _ in range(16)] for _ in range(16)]
    for y in range(16):
        for x in range(16):
            base = original_px[x, y]
            changed = suspicious_px[x, y]
            if base[3] == 0 or changed[3] == 0:
                continue
            base_luma = 0.2126 * base[0] + 0.7152 * base[1] + 0.0722 * base[2]
            changed_luma = 0.2126 * changed[0] + 0.7152 * changed[1] + 0.0722 * changed[2]
            delta = (changed_luma - base_luma) / max(18.0, base_luma)
            template[y][x] = max(-0.35, min(0.35, delta))
    return template


def combine_delta(sand, gravel):
    return [
        [(sand[y][x] + gravel[y][x]) * 0.5 for x in range(16)]
        for y in range(16)
    ]


def build_suspicious_template():
    global _SUSPICIOUS_TEMPLATE
    if _SUSPICIOUS_TEMPLATE is not None:
        return _SUSPICIOUS_TEMPLATE

    _SUSPICIOUS_TEMPLATE = combine_delta(
        signed_luma_delta(
            load_vanilla_texture("sand"),
            load_vanilla_texture("suspicious_sand_3"),
        ),
        signed_luma_delta(
            load_vanilla_texture("gravel"),
            load_vanilla_texture("suspicious_gravel_3"),
        ),
    )
    return _SUSPICIOUS_TEMPLATE


def build_layered_templates():
    global _LAYERED_TEMPLATES
    if _LAYERED_TEMPLATES is not None:
        return _LAYERED_TEMPLATES

    sand = load_vanilla_texture("sand")
    gravel = load_vanilla_texture("gravel")
    _LAYERED_TEMPLATES = [
        combine_delta(
            signed_luma_delta(sand, load_vanilla_texture(f"suspicious_sand_{stage}")),
            signed_luma_delta(gravel, load_vanilla_texture(f"suspicious_gravel_{stage}")),
        )
        for stage in range(4)
    ]
    return _LAYERED_TEMPLATES


def transform_template(template, seed: str):
    key = zlib.crc32(seed.encode("utf-8"))
    mode = (key >> 8) % 8
    out = [[0.0 for _ in range(16)] for _ in range(16)]
    for y in range(16):
        for x in range(16):
            if mode == 0:
                sx, sy = x, y
            elif mode == 1:
                sx, sy = 15 - x, y
            elif mode == 2:
                sx, sy = x, 15 - y
            elif mode == 3:
                sx, sy = 15 - x, 15 - y
            elif mode == 4:
                sx, sy = y, 15 - x
            elif mode == 5:
                sx, sy = 15 - y, x
            elif mode == 6:
                sx, sy = y, x
            else:
                sx, sy = 15 - y, 15 - x
            out[y][x] = template[sy][sx]
    return out


def transformed_template(seed: str):
    return transform_template(build_suspicious_template(), seed)


def add_suspicion(image: Image.Image, seed: str, avoid_transparent: bool = False) -> Image.Image:
    out = image.copy()
    px = out.load()
    template = transformed_template(seed)

    for y in range(16):
        for x in range(16):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            delta = template[y][x]
            if abs(delta) < 0.018:
                continue
            factor = max(0.74, min(1.10, 1.0 + delta))
            px[x, y] = (
                max(0, min(255, round(r * factor))),
                max(0, min(255, round(g * factor))),
                max(0, min(255, round(b * factor))),
                a,
            )
    return out


def add_layered_suspicion(image: Image.Image, seed: str, stage: int) -> Image.Image:
    out = image.copy()
    px = out.load()
    masks = [transform_template(template, seed) for template in build_layered_templates()]
    for layer_index in range(stage + 1):
        mask = masks[layer_index]
        for y in range(16):
            for x in range(16):
                r, g, b, a = px[x, y]
                if a == 0:
                    continue
                delta = mask[y][x]
                if abs(delta) < 0.01:
                    continue
                if delta < 0:
                    overlay = (
                        round(r * 0.45 + 92 * 0.55),
                        round(g * 0.45 + 76 * 0.55),
                        round(b * 0.45 + 62 * 0.55),
                    )
                    alpha = min(0.16, abs(delta) * 0.8)
                else:
                    overlay = (
                        round(r * 0.60 + 238 * 0.40),
                        round(g * 0.60 + 228 * 0.40),
                        round(b * 0.60 + 206 * 0.40),
                    )
                    alpha = min(0.128, abs(delta) * 0.6)
                px[x, y] = (
                    max(0, min(255, round(r * (1.0 - alpha) + overlay[0] * alpha))),
                    max(0, min(255, round(g * (1.0 - alpha) + overlay[1] * alpha))),
                    max(0, min(255, round(b * (1.0 - alpha) + overlay[2] * alpha))),
                    a,
                )
    return out


def prepare_texture(name: str, definition: dict) -> Image.Image:
    image = load_vanilla_texture(name)
    tint_color = definition.get("tint")
    if tint_color:
        image = tint(image, tint_color)
    return image


CLIPPED_MARK_STYLES = {"leaves", "ice", "spawner", "pointed_dripstone", "amethyst"}


def texture_outputs(definition: dict) -> dict[str, Path]:
    block_id = definition["id"]
    textures = definition["textures"]
    if definition["style"] == "pointed_dripstone":
        return {
            role: ASSETS / "textures" / "block" / f"{block_id}_{role}.png"
            for role in textures
        }
    if len(textures) == 1:
        return {"all": ASSETS / "textures" / "block" / f"{block_id}.png"}
    if "overlay" in textures:
        return {
            "top": ASSETS / "textures" / "block" / f"{block_id}_top.png",
            "side": ASSETS / "textures" / "block" / f"{block_id}_side.png",
            "bottom": ASSETS / "textures" / "block" / f"{block_id}_bottom.png",
            "overlay": ASSETS / "textures" / "block" / f"{block_id}_overlay.png",
        }
    if set(textures) >= {"top", "side", "bottom"}:
        return {
            "top": ASSETS / "textures" / "block" / f"{block_id}_top.png",
            "side": ASSETS / "textures" / "block" / f"{block_id}_side.png",
            "bottom": ASSETS / "textures" / "block" / f"{block_id}_bottom.png",
        }
    return {
        "side": ASSETS / "textures" / "block" / f"{block_id}_side.png",
        "end": ASSETS / "textures" / "block" / f"{block_id}_end.png",
    }


def generate_layered_block_textures(definition: dict) -> None:
    block_id = definition["id"]
    outputs = texture_outputs(definition)
    custom_textures = definition.get("custom_textures", {})
    for role, texture_name in definition["textures"].items():
        custom = custom_textures.get(role)
        if custom:
            image = load_custom_texture(custom)
        else:
            image = prepare_texture(texture_name, definition)
        base_output = outputs[role]
        for stage in range(4):
            stage_image = add_layered_suspicion(image, f"{block_id}:{role}:layered", stage)
            if stage == 0:
                output = base_output
            else:
                output = base_output.with_name(f"{base_output.stem}_{stage}{base_output.suffix}")
            ensure(output)
            stage_image.save(output)


def generate_block_textures(definition: dict) -> None:
    if definition.get("texture_mode") == "layered":
        generate_layered_block_textures(definition)
        return
    outputs = texture_outputs(definition)
    custom_textures = definition.get("custom_textures", {})
    for role, texture_name in definition["textures"].items():
        if role in custom_textures:
            image = load_custom_texture(custom_textures[role])
        else:
            image = prepare_texture(texture_name, definition)
            image = add_suspicion(
                image,
                f"{definition['id']}:{role}:{texture_name}",
                avoid_transparent=definition["style"] in CLIPPED_MARK_STYLES,
            )
        ensure(outputs[role])
        image.save(outputs[role])


def texture_refs(definition: dict) -> dict[str, str]:
    outputs = texture_outputs(definition)
    return {
        role: f"susarchy:block/{path.stem}"
        for role, path in outputs.items()
    }


def generate_layered_model_and_state(definition: dict) -> None:
    block_id = definition["id"]
    style = definition["style"]
    if style not in {"cube", "ice", "grate", "grass_block", "sandstone", "column", "pillar"}:
        raise SystemExit(f"unsupported layered style {style}: {block_id}")
    outputs = texture_outputs(definition)
    state_path = ASSETS / "blockstates" / f"{block_id}.json"
    item_path = ASSETS / "models" / "item" / f"{block_id}.json"
    models = {}
    for stage in range(4):
        refs = {
            role: f"susarchy:block/{path.stem if stage == 0 else f'{path.stem}_{stage}'}"
            for role, path in outputs.items()
        }
        model_name = block_id if stage == 0 else f"{block_id}_{stage}"
        if style in {"cube", "ice"}:
            model = {
                "parent": "minecraft:block/cube_all",
                "textures": {"all": refs["all"]},
            }
            if style == "ice":
                model["render_type"] = "minecraft:translucent"
        elif style == "grate":
            model = {
                "parent": "minecraft:block/cube_all",
                "textures": {"all": refs["all"]},
                "render_type": "minecraft:cutout",
            }
        elif style == "grass_block":
            model = {
                "parent": "minecraft:block/grass_block",
                "textures": {
                    "particle": refs["bottom"],
                    "bottom": refs["bottom"],
                    "top": refs["top"],
                    "side": refs["side"],
                    "overlay": refs["overlay"],
                },
                "render_type": "minecraft:cutout_mipped",
            }
        elif style == "sandstone":
            model = {
                "parent": "minecraft:block/cube_bottom_top",
                "textures": {
                    "bottom": refs["bottom"],
                    "side": refs["side"],
                    "top": refs["top"],
                },
            }
        else:
            model = {
                "parent": "minecraft:block/cube_column",
                "textures": {"end": refs["end"], "side": refs["side"]},
            }
        write_json(ASSETS / "models" / "block" / f"{model_name}.json", model)
        models[stage] = f"susarchy:block/{model_name}"
    write_json(item_path, {"parent": models[0]})
    if style == "pillar":
        variants = {}
        for stage in range(4):
            for axis in ("x", "y", "z"):
                entry = {"model": models[stage]}
                if axis == "x":
                    entry["x"] = 90
                    entry["y"] = 90
                elif axis == "z":
                    entry["x"] = 90
                variants[f"axis={axis},dusted={stage}"] = entry
    else:
        variants = {
            f"dusted={stage}": {"model": models[stage]}
            for stage in range(4)
        }
    write_json(state_path, {"variants": variants})


def generate_model_and_state(definition: dict) -> None:
    if definition.get("texture_mode") == "layered":
        generate_layered_model_and_state(definition)
        return
    block_id = definition["id"]
    style = definition["style"]
    refs = texture_refs(definition)
    model_path = ASSETS / "models" / "block" / f"{block_id}.json"
    state_path = ASSETS / "blockstates" / f"{block_id}.json"
    item_path = ASSETS / "models" / "item" / f"{block_id}.json"

    if style == "path":
        model = load_vanilla_model("models/block/dirt_path.json")
        model = replace_strings(
            model,
            {
                "block/dirt_path_top": refs["top"],
                "minecraft:block/dirt_path_top": refs["top"],
                "block/dirt_path_side": refs["side"],
                "minecraft:block/dirt_path_side": refs["side"],
                "block/dirt": refs["bottom"],
                "minecraft:block/dirt": refs["bottom"],
            },
        )
        write_json(model_path, model)
        write_json(item_path, {"parent": f"susarchy:block/{block_id}"})
        variants = {
            f"dusted={dusted}": {"model": f"susarchy:block/{block_id}"}
            for dusted in range(4)
        }
        write_json(state_path, {"variants": variants})
        return

    if style == "snow_layer":
        for layers in range(1, 9):
            if layers == 8:
                model_name = f"{block_id}_16"
                write_json(
                    ASSETS / "models" / "block" / f"{model_name}.json",
                    {"parent": "minecraft:block/cube_all", "textures": {"all": refs["all"]}},
                )
            else:
                height = layers * 2
                model_name = f"{block_id}_{height}"
                model = load_vanilla_model(f"models/block/snow_height{height}.json")
                model = replace_strings(
                    model,
                    {
                        "block/snow": refs["all"],
                        "minecraft:block/snow": refs["all"],
                    },
                )
                write_json(ASSETS / "models" / "block" / f"{model_name}.json", model)
        write_json(item_path, {"parent": f"susarchy:block/{block_id}_2"})
        variants = {}
        for layers in range(1, 9):
            model_name = f"{block_id}_{layers * 2}"
            for dusted in range(4):
                variants[f"layers={layers},dusted={dusted}"] = {
                    "model": f"susarchy:block/{model_name}"
                }
        write_json(state_path, {"variants": variants})
        return

    if style == "pointed_dripstone":
        for role in definition["textures"]:
            model_name = f"{block_id}_{role}"
            write_json(
                ASSETS / "models" / "block" / f"{model_name}.json",
                {
                    "parent": "minecraft:block/pointed_dripstone",
                    "textures": {"cross": refs[role]},
                    "render_type": "minecraft:cutout",
                },
            )
        variants = {}
        for thickness in ("base", "frustum", "middle", "tip", "tip_merge"):
            for direction in ("up", "down"):
                for dusted in range(4):
                    variants[
                        f"thickness={thickness},vertical_direction={direction},dusted={dusted}"
                    ] = {
                        "model": f"susarchy:block/{block_id}_{direction}_{thickness}"
                    }
        write_json(state_path, {"variants": variants})
        vanilla_item = load_vanilla_model("models/item/pointed_dripstone.json")
        item_model = {
            "parent": "minecraft:item/generated",
            "textures": {"layer0": "susarchy:item/sus_pointed_dripstone"},
        }
        if "display" in vanilla_item:
            item_model["display"] = vanilla_item["display"]
        write_json(item_path, item_model)
        return

    if style == "amethyst":
        write_json(
            model_path,
            {
                "parent": "minecraft:block/cross",
                "textures": {"cross": refs["all"]},
                "render_type": "minecraft:cutout",
            },
        )
        vanilla_name = definition["textures"]["all"]
        vanilla_item = load_vanilla_model(f"models/item/{vanilla_name}.json")
        item_model = {
            "parent": "minecraft:item/generated",
            "textures": {"layer0": refs["all"]},
        }
        if "display" in vanilla_item:
            item_model["display"] = vanilla_item["display"]
        write_json(item_path, item_model)

        rotations = {
            "up": {},
            "down": {"x": 180},
            "north": {"x": 90},
            "south": {"x": 90, "y": 180},
            "east": {"x": 90, "y": 90},
            "west": {"x": 90, "y": 270},
        }
        variants = {}
        for facing, rotation in rotations.items():
            for dusted in range(4):
                entry = {"model": f"susarchy:block/{block_id}"}
                entry.update(rotation)
                variants[f"facing={facing},dusted={dusted}"] = entry
        write_json(state_path, {"variants": variants})
        return

    if style == "carpet":
        write_json(
            model_path,
            {
                "parent": "minecraft:block/carpet",
                "textures": {"wool": refs["all"]},
            },
        )
        write_json(item_path, {"parent": f"susarchy:block/{block_id}"})
        variants = {
            f"dusted={dusted}": {"model": f"susarchy:block/{block_id}"}
            for dusted in range(4)
        }
        write_json(state_path, {"variants": variants})
        return

    if style == "grass_block":
        model = {
            "parent": "minecraft:block/grass_block",
            "textures": {
                "particle": refs["bottom"],
                "bottom": refs["bottom"],
                "top": refs["top"],
                "side": refs["side"],
                "overlay": refs["overlay"],
            },
            "render_type": "minecraft:cutout_mipped",
        }
    elif style == "leaves":
        model = {
            "parent": "minecraft:block/leaves",
            "textures": {"all": refs["all"]},
            "render_type": "minecraft:cutout_mipped",
        }
    elif style == "column" or style == "pillar":
        model = {
            "parent": "minecraft:block/cube_column",
            "textures": {"end": refs["end"], "side": refs["side"]},
        }
    elif style in {"grass", "sandstone", "cactus"}:
        model = {
            "parent": "minecraft:block/cube_bottom_top",
            "textures": {
                "bottom": refs["bottom"],
                "side": refs["side"],
                "top": refs["top"],
            },
        }
    elif style == "spawner":
        model = {
            "parent": "minecraft:block/cube_all_inner_faces",
            "textures": {"all": refs["all"]},
            "render_type": "minecraft:cutout",
        }
    else:
        model = {
            "parent": "minecraft:block/cube_all",
            "textures": {"all": refs["all"]},
        }
        if style == "ice":
            model["render_type"] = "minecraft:translucent"
        elif style == "grate":
            model["render_type"] = "minecraft:cutout"

    write_json(model_path, model)
    write_json(item_path, {"parent": f"susarchy:block/{block_id}"})

    if style == "spawner":
        variants = {"": {"model": f"susarchy:block/{block_id}"}}
    elif style == "pillar":
        variants = {}
        for dusted in range(4):
            for axis in ("x", "y", "z"):
                entry = {"model": f"susarchy:block/{block_id}"}
                if axis == "x":
                    entry["x"] = 90
                    entry["y"] = 90
                elif axis == "z":
                    entry["x"] = 90
                variants[f"axis={axis},dusted={dusted}"] = entry
    else:
        variants = {
            f"dusted={dusted}": {"model": f"susarchy:block/{block_id}"}
            for dusted in range(4)
        }
    write_json(state_path, {"variants": variants})

def generate_generated_java(definitions: list[dict]) -> None:
    ensure(JAVA / "SusBlockDefinitions.java")
    lines = [
        "package com.aelux.susarchy;",
        "",
        "import java.util.List;",
        "",
        "public final class SusBlockDefinitions {",
        "    private SusBlockDefinitions() {",
        "    }",
        "",
        "    public enum Style {",
        "        CUBE, COLUMN, PILLAR, LEAVES, ICE, GRASS, GRASS_BLOCK, SANDSTONE, CACTUS, SPAWNER, SNOW_LAYER, POINTED_DRIPSTONE, CARPET, AMETHYST, GRATE, PATH",
        "    }",
        "",
        "    public record Definition(String id, List<String> sources, String baseBlock, Style style, double chance, String loot) {",
        "    }",
        "",
        "    public static final List<Definition> ALL = List.of(",
    ]
    for index, definition in enumerate(definitions):
        sources = ", ".join(json.dumps(source) for source in definition["sources"])
        loot = json.dumps(definition.get("loot"))
        comma = "," if index != len(definitions) - 1 else ""
        lines.append(
            f'            new Definition("{definition["id"]}", List.of({sources}), '
            f'"{definition["base"]}", Style.{definition["style"].upper()}, {definition["chance"]}, {loot}){comma}'
        )
    lines += [
        "    );",
        "}",
        "",
    ]
    (JAVA / "SusBlockDefinitions.java").write_text("\n".join(lines), encoding="utf-8")


def generate_entity_loot_data() -> None:
    table_defs = json.loads((TOOLS / "loot_tables.json").read_text(encoding="utf-8"))
    table_defs.pop("special", None)
    lines = [
        "package com.aelux.susarchy.compat.jei;",
        "",
        "import java.util.List;",
        "import java.util.Map;",
        "",
        "public final class SusEntityLootData {",
        "    public record Loot(String item, int weight, int min, int max) {",
        "    }",
        "",
        "    public static final Map<String, List<Loot>> LOOT = Map.ofEntries(",
    ]
    entity_tables = [
        (category, entries)
        for category, entries in table_defs.items()
        if category.startswith("entity_")
    ]
    for index, (category, entries) in enumerate(entity_tables):
        loot = ["susarchy:sus_dust", 3, 1, 2]
        values = [loot] + entries
        rendered = ", ".join(
            f'new Loot("{item}", {weight}, {min_count}, {max_count})'
            for item, weight, min_count, max_count in values
        )
        comma = "," if index != len(entity_tables) - 1 else ""
        lines.append(f'            Map.entry("{category}", List.of({rendered})){comma}')
    lines += [
        "    );",
        "",
        "    private SusEntityLootData() {",
        "    }",
        "}",
        "",
    ]
    output = JAVA / "compat" / "jei" / "SusEntityLootData.java"
    ensure(output)
    output.write_text("\n".join(lines), encoding="utf-8")


def generate_lang(definitions: list[dict]) -> None:
    en = {
        "itemGroup.susarchy": "Sus Archaeology",
        "item.susarchy.sus_dust": "Suspicious Dust",
        "message.susarchy.entity_brushed": "Brushed! It dropped its normal loot. Somehow.",
        "message.susarchy.entity_already_brushed": "This creature has already been archaeologized.",
        "message.susarchy.entity_nothing": "You brushed the creature. It had nothing. Archaeology!",
        "susarchy.suspicious_name": "Suspicious %1$s",
        "tooltip.susarchy.brush": "Right-click blocks or living entities. Suspicion is a renewable resource.",
        "tooltip.susarchy.sus_dust": "Right-click a normal block with a suspicious variant to coat it. Consumes 1 dust.",
        "config.jade.plugin_susarchy.suspicious_name": "Suspicious entity name",
        "jei.susarchy.entity_archaeology": "Entity Archaeology",
        "jei.susarchy.entity_archaeology.input": "Brush this suspicious entity",
        "jei.susarchy.entity_loot.range": "Quantity: %s-%s",
        "jei.susarchy.entity_loot.weight": "Weight: %s",
    }
    zh = {
        "itemGroup.susarchy": "可疑考古",
        "item.susarchy.sus_dust": "可疑的粉末",
        "message.susarchy.entity_brushed": "刷完了！它掉落了正常战利品。真是见鬼。",
        "message.susarchy.entity_already_brushed": "这个生物已经被考古过了。",
        "message.susarchy.entity_nothing": "你刷了这个生物。它什么也没有。考古，很神奇吧。",
        "susarchy.suspicious_name": "可疑的%1$s",
        "tooltip.susarchy.brush": "右键方块或生物。可疑是一种可再生资源。",
        "tooltip.susarchy.sus_dust": "对着有可疑变体的普通方块右键，将其变成可疑方块。消耗 1 个。",
        "config.jade.plugin_susarchy.suspicious_name": "显示可疑生物名称",
        "jei.susarchy.entity_archaeology": "实体考古",
        "jei.susarchy.entity_archaeology.input": "用刷子刷这个可疑生物",
        "jei.susarchy.entity_loot.range": "数量：%s-%s",
        "jei.susarchy.entity_loot.weight": "权重：%s",
    }
    for definition in definitions:
        en[f"block.susarchy.{definition['id']}"] = definition["en"]
        zh[f"block.susarchy.{definition['id']}"] = definition["zh"]
    for brush in BRUSHES:
        en[f"item.susarchy.{brush['id']}"] = brush["en"]
        zh[f"item.susarchy.{brush['id']}"] = brush["zh"]
    write_json(ASSETS / "lang" / "en_us.json", dict(sorted(en.items())))
    write_json(ASSETS / "lang" / "zh_cn.json", dict(sorted(zh.items())))


def draw_pixel_line(draw: ImageDraw.ImageDraw, points, fill, width=1):
    draw.line(points, fill=fill, width=width)


RING_ROLES = {
    (5, 8): "base",
    (6, 8): "base",
    (4, 9): "base",
    (5, 9): "light",
    (6, 9): "highlight",
    (7, 9): "base",
    (5, 10): "dark",
    (6, 10): "light",
    (7, 10): "dark",
}

RING_PALETTES = {
    "wood_brush": {
        "dark": (59, 34, 15, 255),
        "base": (122, 74, 36, 255),
        "light": (169, 107, 53, 255),
        "highlight": (216, 165, 109, 255),
    },
    "stone_brush": {
        "dark": (62, 62, 62, 255),
        "base": (119, 119, 119, 255),
        "light": (163, 163, 163, 255),
        "highlight": (210, 210, 210, 255),
    },
    "flint_brush": {
        "dark": (29, 28, 33, 255),
        "base": (59, 57, 66, 255),
        "light": (93, 90, 102, 255),
        "highlight": (141, 137, 153, 255),
    },
    "iron_brush": {
        "dark": (84, 93, 100, 255),
        "base": (152, 163, 170, 255),
        "light": (195, 204, 210, 255),
        "highlight": (243, 246, 248, 255),
    },
    "gold_brush": {
        "dark": (138, 98, 16, 255),
        "base": (216, 167, 31, 255),
        "light": (243, 204, 70, 255),
        "highlight": (255, 242, 164, 255),
    },
    "diamond_brush": {
        "dark": (27, 111, 107, 255),
        "base": (51, 183, 172, 255),
        "light": (117, 233, 223, 255),
        "highlight": (217, 255, 251, 255),
    },
    "netherite_brush": {
        "dark": (33, 21, 29, 255),
        "base": (68, 49, 63, 255),
        "light": (108, 75, 94, 255),
        "highlight": (166, 125, 154, 255),
    },
}


def generate_brush_icons() -> None:
    vanilla = load_vanilla_item_texture("brush")
    vanilla_model = load_vanilla_model("models/item/brush.json")

    for brush in BRUSHES:
        image = vanilla.copy()
        palette = RING_PALETTES.get(brush["id"])
        if palette is not None:
            px = image.load()
            for (x, y), role in RING_ROLES.items():
                px[x, y] = palette[role]

        output = ASSETS / "textures" / "item" / f"{brush['id']}.png"
        ensure(output)
        image.save(output)

        model = {
            "parent": vanilla_model.get("parent", "minecraft:item/generated"),
            "textures": {"layer0": f"susarchy:item/{brush['id']}"},
        }
        if "display" in vanilla_model:
            model["display"] = vanilla_model["display"]
        write_json(ASSETS / "models" / "item" / f"{brush['id']}.json", model)

def generate_sus_dust() -> None:
    image = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    outline = (56, 37, 13, 255)
    mound = (206, 169, 87, 255)
    light = (242, 207, 121, 255)
    dark = (156, 116, 49, 255)
    # Small dust mound.
    draw.rectangle((4, 11, 12, 14), fill=outline)
    draw.rectangle((5, 9, 11, 13), fill=outline)
    draw.rectangle((6, 8, 10, 12), fill=mound)
    draw.rectangle((5, 10, 11, 13), fill=mound)
    draw.line((6, 8, 10, 8), fill=outline)
    draw.line((6, 9, 9, 9), fill=light)
    draw.point((6, 12), fill=dark)
    draw.point((10, 12), fill=dark)
    draw.point((8, 13), fill=light)
    # Sparkles.
    for x, y, c in [(3, 6, light), (12, 5, light), (2, 10, light), (12, 10, (255, 245, 196, 255)), (4, 3, (232, 184, 57, 255))]:
        draw.point((x, y), fill=c)
    ensure(ASSETS / "textures" / "item" / "sus_dust.png")
    image.save(ASSETS / "textures" / "item" / "sus_dust.png")
    write_json(
        ASSETS / "models" / "item" / "sus_dust.json",
        {"parent": "minecraft:item/generated", "textures": {"layer0": "susarchy:item/sus_dust"}},
    )


def generate_icon() -> None:
    image = Image.new("RGBA", (128, 128), (31, 22, 14, 255))
    draw = ImageDraw.Draw(image)
    for y in range(128):
        for x in range(128):
            r = int(26 + 20 * (x / 127) + 12 * math.sin(y / 11))
            g = int(18 + 14 * (y / 127))
            b = int(10 + 5 * (x / 127))
            draw.point((x, y), fill=(r, g, b, 255))
    block = Image.open(ASSETS / "textures" / "block" / "sus_diamond_ore.png").convert("RGBA")
    block = block.resize((96, 96), Image.Resampling.NEAREST)
    image.alpha_composite(block, (16, 20))
    brush = Image.open(ASSETS / "textures" / "item" / "diamond_brush.png").convert("RGBA")
    brush = brush.resize((72, 72), Image.Resampling.NEAREST)
    image.alpha_composite(brush, (48, 39))
    draw.rectangle((0, 0, 127, 127), outline=(110, 74, 28, 255), width=3)
    ensure(RESOURCES / "icon.png")
    image.save(RESOURCES / "icon.png")


def generate_loot_tables() -> None:
    table_defs = json.loads((TOOLS / "loot_tables.json").read_text(encoding="utf-8"))
    special_entries = table_defs.pop("special", {})
    unknown_special = set(special_entries) - set(table_defs)
    if unknown_special:
        raise SystemExit(f"special loot entries reference unknown tables: {sorted(unknown_special)}")

    for category, vanilla_entries in table_defs.items():
        entries = []
        if category == "sus":
            entries.append(["susarchy:sus_dust", 6, 1, 2])
        elif category.startswith("entity_"):
            entries.append(["susarchy:sus_dust", 3, 1, 2])
        entries.extend(vanilla_entries)

        loot_entries = []
        for item, weight, min_count, max_count in entries:
            entry = {
                "type": "minecraft:item",
                "name": item,
                "weight": weight,
            }
            if min_count != 1 or max_count != 1:
                entry["functions"] = [
                    {
                        "function": "minecraft:set_count",
                        "count": {
                            "type": "minecraft:uniform",
                            "min": float(min_count),
                            "max": float(max_count),
                        },
                    }
                ]
            loot_entries.append(entry)
        loot_entries.extend(special_entries.get(category, []))

        loot = {
            "type": "minecraft:archaeology",
            "pools": [
                {
                    "rolls": 1.0,
                    "bonus_rolls": 0.0,
                    "entries": loot_entries,
                }
            ],
            "random_sequence": f"susarchy:archaeology/{category}",
        }
        write_json(DATA / "loot_table" / "archaeology" / f"{category}.json", loot)


def generate_loot_and_recipes() -> None:
    generate_loot_tables()

    # Base brush recipe.
    feather_recipe = {
        "type": "minecraft:crafting_shaped",
        "category": "equipment",
        "group": "sus_brushes",
        "pattern": [" F ", " S ", " T "],
        "key": {
            "F": {"item": "minecraft:feather"},
            "S": {"item": "minecraft:stick"},
            "T": {"item": "minecraft:string"},
        },
        "result": {"id": "susarchy:feather_brush", "count": 1},
    }
    write_json(DATA / "recipe" / "feather_brush.json", feather_recipe)

    for result, previous, material in BRUSH_MATERIALS:
        recipe = {
            "type": "minecraft:crafting_shaped",
            "category": "equipment",
            "group": "sus_brushes",
            "pattern": ["MMM", "MBM", "MMM"],
            "key": {
                "M": {"item": material},
                "B": {"item": f"susarchy:{previous}"},
            },
            "result": {"id": f"susarchy:{result}", "count": 1},
        }
        write_json(DATA / "recipe" / f"{result}.json", recipe)

    advancements = {
        "feather_brush": ("minecraft:feather", "has_feather"),
        "wood_brush": ("susarchy:feather_brush", "has_feather_brush"),
        "stone_brush": ("susarchy:wood_brush", "has_wood_brush"),
        "flint_brush": ("susarchy:stone_brush", "has_stone_brush"),
        "iron_brush": ("susarchy:flint_brush", "has_flint_brush"),
        "gold_brush": ("susarchy:iron_brush", "has_iron_brush"),
        "diamond_brush": ("susarchy:gold_brush", "has_gold_brush"),
        "netherite_brush": ("susarchy:diamond_brush", "has_diamond_brush"),
    }
    for recipe, (criterion_item, criterion_name) in advancements.items():
        advancement = {
            "parent": "minecraft:recipes/root",
            "criteria": {
                "has_the_recipe": {
                    "trigger": "minecraft:recipe_unlocked",
                    "conditions": {"recipe": f"susarchy:{recipe}"},
                },
                criterion_name: {
                    "trigger": "minecraft:inventory_changed",
                    "conditions": {"items": [{"items": criterion_item}]},
                },
            },
            "requirements": [["has_the_recipe", criterion_name]],
            "rewards": {"recipes": [f"susarchy:{recipe}"]},
        }
        write_json(DATA / "advancement" / "recipes" / "equipment" / f"{recipe}.json", advancement)

    tag_values = [f"susarchy:{brush['id']}" for brush in BRUSHES]
    write_json(
        RESOURCES / "data" / "minecraft" / "tags" / "item" / "enchantable" / "durability.json",
        {"replace": False, "values": tag_values},
    )
    write_json(
        RESOURCES / "data" / "c" / "tags" / "item" / "tools" / "brush.json",
        {"replace": False, "values": tag_values},
    )


def main() -> None:
    definitions = load_blocks()
    if len(definitions) != 138:
        raise SystemExit(f"expected 138 block definitions, got {len(definitions)}")
    if not CLIENT_JAR.exists():
        raise SystemExit(f"missing vanilla client jar: {CLIENT_JAR}")

    for definition in definitions:
        generate_block_textures(definition)
        generate_model_and_state(definition)
    generate_brush_icons()
    generate_sus_dust()
    generate_icon()
    generate_generated_java(definitions)
    generate_entity_loot_data()
    generate_lang(definitions)
    generate_loot_and_recipes()
    print(f"Generated {len(definitions)} blocks, {len(BRUSHES)} brushes, and all resources.")


if __name__ == "__main__":
    main()
