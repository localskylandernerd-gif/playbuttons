EASIEST BUILD: install JDK 21, then double-click build.bat (Windows) or run ./build.sh (Mac/Linux).

PAY BUTTONS - Fabric mod for Minecraft Java 1.21.4

BUILD:
 1. Install JDK 21 (e.g. Adoptium Temurin 21) and IntelliJ IDEA Community.
 2. IntelliJ -> File -> Open -> select this folder -> let Gradle import finish.
 3. Open the Gradle panel -> Tasks -> build -> build   (or run: gradle build)
 4. Your mod is in build/libs/paybuttons-1.0.0.jar  (NOT the "-sources" jar)

INSTALL:
 - Install Fabric Loader for 1.21.4 + Fabric API.
 - Put paybuttons-1.0.0.jar (and fabric-api jar) into .minecraft/mods

TEST IN A SINGLEPLAYER WORLD:
 Cheats on, then type:  /tellraw @s {"text":"Steve paid you $2.5K"}
 You should see green money and [1x] [2x] [3x] buttons.

TROUBLESHOOTING:
 - Open .minecraft/logs/latest.log and search for "paybuttons".
   "Pay Buttons loaded" = the mod is running.
   "Saw a 'paid' message but the pattern did not match: [...]" = send that line to me.
 - Make sure your Minecraft version is exactly 1.21.4 (or edit gradle.properties + fabric.mod.json).
