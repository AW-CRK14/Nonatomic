
瓦解核心：非原子单位 | Brea:Nonatomic
=======

[点击阅读中文版本](README.md) 

# Nonatomic Mod

## Overview
Nonatomic lib, introducing a new gameplay mechanic: the Operator system. Operators are special entities that players can control to perform various tasks. They have unique attributes and behaviors.

## Features
- **Operator System**: Players can unlock, deploy, and retrieve operators.
- **Operator Types**: A variety of operator types, each with specific attributes and behaviors.
- **Operator Interaction**: Players can interact with operators, including logging in, logging out, and refreshing status.
- **Operator Attributes**: Operators have customizable attributes that affect their performance in the game.
- **Event Listening**: The mod listens to game events such as player login, logout, and operator deployment and retrieval.

## Code Framework

### Main Classes and Interfaces

- **Operator**: Represents an operator, containing attributes, status, and behaviors.
- **OperatorType**: Defines the type of operator, with methods to create instances of operators.
- **OpeHandler**: Interface for managing operators, including deployment, retrieval, and attribute modification.
- **OperatorEntity**: The in-game entity representation of an operator.
- **OpeHandlerNoRepetition**: Concrete class implementing `OpeHandler`, managing player operator deployment.
- **IAttributesProvider**: Interface for providing operator attributes.

### Core Functionality Implementation

- **Operator Creation and Initialization**: Create operator instances through `OperatorType` and manage them in `OpeHandlerNoRepetition`.
- **Operator Deployment and Retrieval**: Deploy and retrieve operators through implementations of `OpeHandler`, ensuring synchronization of operator status.
- **Attribute Management**: Attach and remove attribute modifiers of operators through the `IAttributesProvider` interface.
- **Event Listening**: Manage operator behavior in response to game events through the `GameBusConsumer` class.

### Events and Listening

- **Player Events**: Listen to player login, logout, death, and dimension change events.
- **Operator Events**: Handle operator deployment, retrieval, and damage events.

### Registration and Management

- **Registries**: Manages the registry of operator information and types.
- **ModBusConsumer**: Registers operator information and types on the mod event bus.

## Installation and Usage

1. **Install Forge**: Ensure you have the correct version of Forge installed for Minecraft.
2. **Download the Mod**: Download the Nonatomic mod from the release page.
3. **Place the Mod**: Put the downloaded mod file into the `mods` folder.
4. **In-Game Operations**: Unlock and deploy operators through specific in-game mechanics.

## Contributing

Contributions to the Nonatomic mod are welcome. You can submit Issues to report problems or Fork this project to contribute your improvements.

## Developers

- **Moonshot AI**: Provides technical support and development.

---

This README provides an overview of the Nonatomic mod, detailing its features, code framework, and how to use it.
