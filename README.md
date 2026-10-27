# MeteorChecker

Lightweight Paper / Folia plugin to detect Meteor Client connections using sign packet probes powered by **PacketEvents**.

### How it works
When a player joins, the server sends a fake sign update with a translatable text key specific to Meteor Client. If the player is running Meteor, the client resolves the translation key and responds with an `UPDATE_SIGN` packet, which gets caught at the packet level and kicks the player.

### Requirements
- Paper or Folia 1.20.4+
- Java 21+
- [PacketEvents](https://github.com/retrooper/packetevents) (2.x)

### Build
```bash
./gradlew build
```
Compiled jar will be in `build/libs/`.

### Configuration (`config.yml`)
```yaml
# Delay in ticks before probing a player after they join (20 ticks = 1 second)
probe-delay: 25

# Kick message sent to players detected with Meteor Client
kick-message: "<red>meteor client detect please remove and join back"
```

### Permissions
- `meteorchecker.bypass` - Bypass the check (default: op)
---
by akvarez
