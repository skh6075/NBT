# Kotrock-NBT
A lightweight, high-performance, and type-safe NBT (Named Binary Tag) library designed for Kotlin multiplatform. It features an intuitive DSL for building NBT structures, low-overhead binary serialization via kotlinx.io, and native SNBT (String NBT) parsing capabilities without external heavy dependencies.

# Features
- Zero-Allocation Primitives: Maximizes performance by utilizing Kotlin value class for primitive tags.
- Declarative DSL Builder: Clean, nested DSL syntax that replaces boilerplate Java code.
- Dual Pipeline Support: Seamlessly handles both Network (VarInt compressed) and Disk (Uncompressed/Standard) NBT formats.
- Built-in SNBT Engine: Direct conversion between NbtCompound and String with support for multi-layered arrays and complex objects.

# Installation
Add the dependency to your `build.gradle.kts`:
```kotlin
repositories {
    mavenCentral()
    maven("https://nexus.hforest.org/repository/maven-public/")
}

dependencies {
    implementation("org.kotrock.nbt:nbt:1.0.0-SNAPSHOT")
}
```

# Usage

### File IO (Reading/Writing `.nbt` Files)
This example demonstrates how to serialize and deserialize NBT data for local storage uses (e.g., schematics, structures) using the standard disk format.
```kotlin
class World(private val name: String) {
    private val levelDatPath = Path("worlds/$name/level.dat")
    private lateinit var worldSettings: NbtCompound
    
    fun load() {
        if (!FileSystem.SYSTEM.exists(levelDatPath)) {
            throw IllegalStateException("World: Cannot find level.dat for world '$name'")
        }

        FileSystem.SYSTEM.source(levelDatPath).buffered().use { source ->
            this.worldSettings = NBTIO.read(source, isNetwork = false)
        }

        val seed = worldSettings.getInt("RandomSeed")
        val generator = worldSettings.getString("generatorName", "infinite")
        val spawnX = worldSettings.getInt("SpawnX")
        val spawnY = worldSettings.getInt("SpawnY")
        val spawnZ = worldSettings.getInt("SpawnZ")

        println("World Name: $worldName")
        println("Seed: $seed | Generator: $generator")
        println("Spawn: X:$spawnX, Y:$spawnY, Z:$spawnZ")
    }
    
    fun save(updatedSettings: NbtCompound) {
        FileSystem.SYSTEM.sink(levelDatPath).buffered().use { sink ->
            KotrockNbtIo.write(sink, updatedSettings, isNetwork = false)
        }
    }
}
```

### SNBT Pipeline (String NBT Parsing & Interoperability)
Convert raw textual NBT representations directly back into active objects or string-serialize them for configuration profiles and Discord bot interfaces.
```kotlin
val vortexSword = nbtCompound {
    string("id", "custom:vortex_sword")
    int("rarity", 5)
    nbtCompound("display") {
        string("Name", "§bVortex Sword")
    }
}

val encodeString: String = vortexSword.toSnbt()
println(encodeString)

val decodeNbt = encodeString.toNbtCompound()
val id = decodeNbt.getString("id")
val rarity = decodeNbt.getInt("rarity")

println("ID: $id")
println("Rarity: $rarity")
```