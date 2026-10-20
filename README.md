# MeteorChecker

Lightweight Paper / Folia plugin to detect Meteor Client connections using sign packet probes.

### How it works
When a player joins, the server sends a fake sign update with a translatable text key specific to Meteor Client. If the player is running Meteor, the client resolves the translation key and responds with an `UPDATE_SIGN` packet, which gets caught by the listener and kicks the player.

### Requirements
- Paper or Folia 1.20.4+
- Java 21+
- [ProtocolLib](https://github.com/dmulloy2/ProtocolLib)

### Build
```bash
./gradlew build
```
Compiled jar will be in `build/libs/`.

### Permissions
- `meteorchecker.bypass` - Bypass the check (default: op)
