# fkl Mod v2.1.0 — Source Code

Fabric mod for Minecraft 1.21.1  
Full rewrite with all new features.

---

## How to Build

**Requirements:** Java 21, internet connection (first build downloads Gradle + Minecraft mappings)

```bash
# Linux / macOS
./gradlew build

# Windows
gradlew.bat build
```

The compiled `.jar` will appear in `build/libs/fkl-mod-2.1.0.jar`.  
Copy it to your `.minecraft/mods/` folder alongside Fabric Loader and Fabric API.

> First build takes ~5 minutes — it downloads Yarn mappings and Minecraft itself.

---

## New Features in v2.1.0

### 💬 Chat & Reply System
- She reads everything you type in chat (within 32 blocks)
- Replies after a short natural delay (not instant)
- 15+ topic categories: greetings, food, danger, love, weather, jokes, etc.
- Hunger-aware replies — if starving, she'll beg for food first
- Generic fallback lines when no keyword matches

### 🤗 Smarter Hug / Kiss Logic (replaces random proximity triggers)
| Time away | What happens |
|-----------|-------------|
| Same day  | Nothing automatic — only chat replies |
| 1 day     | She hugs you when you get close |
| 2+ days   | She runs to you, hugs **and** kisses |
- 2-minute cooldown between greetings so it doesn't spam

### 💋 Blush Cheeks (replaces heart particles)
- No more floating hearts
- Renderer draws soft red semi-transparent quads on her cheeks when blushing
- Gentle pulse animation

### 🧱 Block Climbing
- Detects 2-block walls ahead while following
- Places a dirt block to step up and continue following you

### 🚪 Door & Redstone Use
- `OpenDoorGoal` — she opens wooden doors and iron doors (via redstone/button logic) and closes them behind her

### ⚔️ Combat Retreat System
| Damage dealt (last 3s) | Behaviour |
|------------------------|-----------|
| ≥ 5 hearts             | Backs away to 6-block safe distance |
| ≥ 10 hearts            | Full retreat — runs back to owner |
- Damage counter resets every 3 seconds

### 🍖 Hunger & Health Regen
- Hunger drains **1 point every 5 minutes** (was much faster before)
- Regenerates **1 HP every 30 seconds** if hunger > 5, costs 1 hunger
- Chat warnings at low / zero hunger

### 🐱 Nature Interactions
- Wanders to nearby **cats** and **villagers** (within 8 blocks)
- Sends emote messages to owner: *"gently pets the cat"* / *"waves at the villager"*
- 1–2 minute cooldown between interactions

### 📟 Custom Commands
| Command | Effect |
|---------|--------|
| `/gf status` | Shows HP, hunger, affection, sitting state |
| `/gf follow` | Start following |
| `/gf wait` | Stop and sit |
| `/gf come` | Teleport to you |
| `/gf tp` | Same as come |
| `/gf skin <0-21>` | Change skin variant |
| `/gf rename <name>` | Give her a new name |
| `/gf feed <1-20>` | Add hunger points |
| `/gf dismiss` | Remove her (permanent) |

### 📊 HUD Overlay
- Name, HP and hunger displayed top-right corner of screen

---

## File Structure

```
src/main/java/com/beckytidus/girlfriendmod/
├── GirlfriendMod.java               Main initializer
├── GirlfriendModClient.java         Client initializer (renderer/model)
├── client/render/
│   ├── GirlFriendEntityModel.java   Biped model with walk animation
│   ├── GirlFriendEntityRenderer.java Renderer + blush overlay
│   └── GirlFriendEntityRenderState.java
├── command/
│   └── GirlFriendCommand.java       /gf command tree
├── dialogue/
│   ├── DelayedChatReply.java        Queued reply with delay
│   ├── DialogueData.java            All keyword→response mappings
│   └── WaitAndFollowLines.java      Wait/follow dialogue lines
├── entity/
│   └── GirlFriendEntity.java        Main entity (all goals inside)
├── event/
│   └── EntityAttributeHandler.java
├── item/
│   └── GirlFriendSummonerItem.java
├── mixin/
│   ├── PlayerManagerChatMixin.java  Chat interception
│   ├── InGameHudGirlfriendStatsMixin.java  HUD stats
│   └── EntityRenderManagerGirlfriendMixin.java
└── registry/
    ├── EntityRegistry.java
    ├── FemaleNames.java             50 random names for summoning
    ├── GirlfriendSkins.java         22 skin variants
    └── ItemRegistry.java
```
