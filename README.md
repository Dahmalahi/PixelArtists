# PixelArtists
PixelArtists is a full-featured mobile studio inspired by Aseprite and FlipaClip, engineered to run on legacy Java ME devices (Nokia S40/S60, Sony Ericsson, etc.). It brings desktop-class pixel art tools to constrained mobile hardware.
# 🎨 PixelArtists
<img width="1024" height="1024" alt="icon" src="https://github.com/user-attachments/assets/d7c90333-e399-46d4-9f3f-225e103c8139" />
**Professional Pixel Art Editor for J2ME/MIDP 2.0**

![Version](https://img.shields.io/badge/version-3.0-blue)
![Java](https://img.shields.io/badge/java-1.3-orange)
![Platform](https://img.shields.io/badge/platform-J2ME-green)

A full-featured pixel art editor inspired by Aseprite & FlipaClip, built for Java ME devices.

## ✨ Features

### 🖌️ Drawing Tools
- Pencil (smooth)
- Eraser
- Flood Fill (Bucket)
- Line Tool (two-point)
- Rectangle Tool
- Eyedropper (Color Picker)
- Spray Paint
- Circle Tool

### 🎬 Animation
- Up to **64 frames**
- FPS control (1-30 FPS)
- Loop mode
- Onion skinning
- Frame duration per frame
- Real-time preview

### 📑 Layer System
- Up to **8 layers** per frame
- Layer visibility toggle
- Opacity control (25%, 50%, 75%, 100%)
- Merge down
- Flatten all layers
- Reorder layers (up/down)

### 💾 Export Formats
- **BMP** - Uncompressed
- **PNG** - Compressed (best quality)
- **JPG** - Lossy (smallest size)
- **GIF** - Animated
- **MP4** - Image sequence (for video editing)

### 🎨 Palettes
- Default 32-color
- GameBoy (4-color)
- NES (56-color)
- CGA (16-color)
- Custom palettes support

### ⚙️ Other Features
- Zoom x1 to x64
- Grid toggle
- Symmetry drawing (H/V)
- Undo/Redo (10 states)
- Auto-save (RMS)
- SD Card save/load
- Dark theme UI

## 📱 Compatible Devices

- Nokia S40/S60
- Sony Ericsson
- Motorola
- Samsung J2ME phones
- Any device with **CLDC 1.1** + **MIDP 2.0**
- J2ME emulators (Wireless Toolkit, Eclipse ME)

## 🚀 Installation

### On Real Device
1. Download `PixelArtists.jar` and `PixelArtists.jad`
2. Transfer to phone via Bluetooth/USB
3. Open `.jad` file to install

### On Emulator
1. Open **J2ME Wireless Toolkit**
2. File → Open Project
3. Select project folder
4. Run → Run

## 🎮 Controls

| Key | Action |
|-----|--------|
| DPAD | Move cursor |
| FIRE/5 | Toggle draw |
| 1 | Zoom IN |
| 3 | Zoom OUT |
| 7 | Previous color |
| 9 | Next color |
| * | Previous tool |
| # | Next tool |
| 0 | Undo |
| G | Toggle grid |
| O | Toggle onion skin |
| S | Symmetry H |
| V | Symmetry V |
| F | Flip H |
| I | Invert colors |
| H | Grayscale |
| [ | Previous frame |
| ] | Next frame |

## 🛠️ Build from Source

### Requirements
- Java JDK 1.3 or 1.4
- J2ME Wireless Toolkit 2.5.2
- Apache Ant (optional)

### Build Steps
```bash
# Clone repository
git clone https://github.com/Dahmalahi/PixelArtists.git
cd PixelArtists

# Open in J2ME Wireless Toolkit
# File → Open Project → Select folder

# Build
# Project → Build

# JAR will be in bin/PixelArtists.jar

