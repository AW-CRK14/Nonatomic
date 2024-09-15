瓦解核心：非原子单位 | Brea:Nonatomic
=======

[click here to read Chinese version](README.md)

---

## Overview

The Nonatomic Lib allows players to deploy Operators in the game. Operators correspond to Operator entities, and players
can deploy them to perform various tasks.

This library provides a range of commonly used features for the entire Operator system:

- **Operator Customization**: Allows the customization of various types of Operators, each with unique attributes and
  behaviors.
- **Operator Information Storage**: In addition to customizable attributes, Operators have the capability to store
  mutable and persistent information.
- **Entity and Information Communication**: Operator entities and their information reference each other, with
  appropriate clearing and refreshing to ensure synchronization and proper status.
- **Player Behavior Handling**: Special player behaviors affect entity states to ensure proper functionality, including
  login, logout, teleportation, etc.
- **Event Listening**: The mod listens to game events, such as player login, logout, and the deployment and retrieval of
  Operators, to enhance flexibility.

## Code Framework

Ideally, we can directly bind data to dimensions for unified management—this ensures that Operator data references are
maintained when players are offline and avoids crashes due to excessive player data.

The data structure is roughly as follows, from broadest to most specific:

1. World Data Storage. We do not provide a standard for this level,
   but we offer
   the [GroupProvider interface (see at the bottom)](src/main/java/com/phasetranscrystal/nonatomic/core/OpeHandler.java)
   for convenient handling of player and entity events.
2. Player Operator Data ([`OpeHandler`](src/main/java/com/phasetranscrystal/nonatomic/core/OpeHandler.java))
   Stores data on Operators owned by a player and a list of deployed Operators. Since a player might have multiple
   Operator data under different mod implementations, you need to configure the appropriate `ContainerId`
   to ensure the entity correctly captures the corresponding data upon loading. For data capturing, refer
   to [`FindOperatorEvent`](src/main/java/com/phasetranscrystal/nonatomic/event/FindOperatorEvent.java).
   If the `GroupProvider` mentioned in the first step is implemented and registered, related events can be automatically
   handled.
3. Operator ([`Operator`](src/main/java/com/phasetranscrystal/nonatomic/core/Operator.java))
   Is the unit that can be directly captured and referenced by the Operator entity and is responsible for storing
   Operator information. This includes methods related to basic Operator behaviors such as deployment, withdrawal,
   legality checks, as well as basic information such as Operator type, status, last recorded position, etc. The
   referenced Operator entities will not be null as long as the entity exists and is loaded.
4. Operator Data ([`OperatorInfo`](src/main/java/com/phasetranscrystal/nonatomic/core/OperatorInfo.java))
   Responsible for storing additional information about Operators. If needed, Operator entities can store similar types
   of information and request merging at appropriate times to implement temporary information functionality.

You can refer to the [`TestObjects`](src/main/java/com/phasetranscrystal/nonatomic/TestObjects.java) class to see our
test code.
Calling the `TestObjects#initTest` method in the mod's main class will activate it—don't forget to remove it afterward.

You can also view the provided events in [`EventHooks`](src/main/java/com/phasetranscrystal/nonatomic/EventHooks.java).
Each event class contains javadoc content about its usage.

Additionally, you can refer to our [Test Content Table](TEST_LIST.md) to see and verify the features that can be
automatically implemented under correct configurations.

## Contributing

We welcome contributions to the Nonatomic mod. You can submit Issues to report problems or Fork this project to submit
your improvements.

## Developer

- **Mon-landis**: Provides technical support and development.

---