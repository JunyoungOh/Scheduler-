# Character Design & UI Guide

## Open Source Pixel Art Platforms (Commercial Use Friendly)

To introduce charming pixel art characters into *CutenerRaising* while maintaining open-source compliance and commercial viability, we recommend the following tools:

### 1. LibreSprite
*   **Description:** A fork of the famous Aseprite (before it went proprietary). It provides a classic, robust pixel art workflow.
*   **License:** GNU GPLv2 (Safe for creating commercial assets; the tool itself is open source).
*   **Best For:** Desktop users (Windows/Linux) who want a professional-grade editor without the cost.
*   **Website:** [libresprite.github.io](https://libresprite.github.io/)

### 2. Pixelorama
*   **Description:** A free and open-source pixel art editor built with the Godot Engine. It is modern, feature-rich, and supports layers, animations, and tilemaps.
*   **License:** MIT License (Very permissive).
*   **Best For:** Users who want a modern UI and cross-platform support (Web, Windows, Mac, Linux).
*   **Website:** [orama-interactive.itch.io/pixelorama](https://orama-interactive.itch.io/pixelorama)

### 3. Piskel
*   **Description:** A web-based pixel art editor. It is simple, accessible, and allows for quick prototyping.
*   **License:** Apache License 2.0.
*   **Best For:** Quick edits, browser-based work, and simple animations.
*   **Website:** [piskelapp.com](https://www.piskelapp.com/)

---

## Design Integration Guide

Currently, *CutenerRaising* uses a programmatic "Grid System" in `CharacterRenderer.kt` to draw characters using code. To switch to high-quality pixel art assets, follow these steps:

### Step 1: Asset Creation
1.  **Canvas Size:** Start with a small canvas (e.g., 32x32 or 64x64 pixels) to maintain the "retro" look.
2.  **Export:**
    *   **Static:** Export as `.png` with a transparent background.
    *   **Animated:** Export as a **Sprite Sheet** (a single image containing all animation frames in a row or grid) or individual `.png` files (e.g., `warrior_idle_01.png`, `warrior_idle_02.png`).
    *   *Recommendation:* Use separate PNGs for simplicity in Android `drawable` resources, or a single Sprite Sheet if you implement a sprite cutter.

### Step 2: Android Resource Setup
1.  Place your exported PNG files in the `app/src/main/res/drawable` directory.
2.  Naming convention: `char_[class]_[state]_[frame].png`
    *   Example: `char_warrior_idle.png`, `char_mage_attack.png`

### Step 3: Updating `CharacterRenderer.kt`

You will need to refactor the `CharacterRenderer` composable to use `Image` or `drawImage` instead of looping through a string grid.

**Current Approach (Code-based):**
```kotlin
// Uses Canvas to drawRect for every "pixel" in a string grid
Canvas(modifier = Modifier.size(120.dp)) {
    // ... logic iterating over grid strings ...
    drawRect(...)
}
```

**New Approach (Asset-based):**
1.  Define a mapping between `CharacterClass` + `CharacterAnimState` and your Drawable Resource ID.
2.  Use `painterResource` to load the image.

```kotlin
@Composable
fun CharacterRenderer(
    charClass: CharacterClass,
    animState: CharacterAnimState,
    modifier: Modifier = Modifier
) {
    // 1. Determine the resource ID
    val resourceId = when(charClass) {
        CharacterClass.WARRIOR -> R.drawable.char_warrior_idle // Simplified logic
        CharacterClass.MAGE -> R.drawable.char_mage_idle
        else -> R.drawable.char_default
    }

    // 2. Render the image
    // Using pixel art scaling (FilterQuality.None is crucial for crisp pixels)
    Image(
        painter = painterResource(id = resourceId),
        contentDescription = "Character",
        modifier = modifier.size(120.dp),
        filterQuality = FilterQuality.None
    )
}
```

### Step 4: Handling Animations
For animations (e.g., Idle vs Attack), you can either:
*   **Swap Images:** Change the `resourceId` based on `animState` (e.g., `if (animState == ATTACK) R.drawable.warrior_attack else R.drawable.warrior_idle`).
*   **Sprite Sheet Animation:** Use a custom `Canvas` drawing that takes a `srcOffset` and `srcSize` to draw only a specific frame from a larger sprite sheet image.

### Summary
1.  **Pick a Tool:** Pixelorama is highly recommended for a modern experience.
2.  **Draw & Export:** Create 32x32 characters and export as PNGs.
3.  **Import:** Add to `res/drawable`.
4.  **Refactor:** Replace `CharacterRenderer`'s grid logic with standard Compose `Image` components.
