瓦解核心：非原子单位 | Brea:Nonatomic
=======

[click here to read English version](README_en.md)

---

# 概述

Nonatomic Lib，允许玩家在游戏中部署干员。干员与干员实体进行对应，玩家可以部署它们来执行各种任务。

本库提供了整个干员系统的一系列常用功能：

- **干员自定义**：允许自定义多种干员类型，每种都可以拥有独特的属性和行为。
- **干员信息存储**：除了可定制的属性之外，干员具有可变且可以持久化保存的信息能力。
- **实体与信息通信**：干员实体与其信息互相引用，在恰当的时机清除与刷新，保证信息的同步与状态的正常。
- **玩家行为处理**：玩家的特殊行为影响实体状态以保证功能的正常，包括登录、登出、传送等。
- **事件监听**：模组监听游戏事件，如玩家登录、退出，以及干员的部署和回收，以提高功能的灵活性。

# 代码框架

在最理想的情况下，我们可以直接将数据绑定在维度上进行统一管理——这可以保证玩家不在线时干员数据引用的正常，也可以避免玩家数据过多导致的崩溃。

按包含关系从大到小，其数据结构大致如下：

1. 世界数据存储。这一层级我们没有提供规范，
   但是提供了[GroupProvider接口(翻到最底下)](src/main/java/com/phasetranscrystal/nonatomic/core/OpeHandler.java)
   用于便捷的处理玩家与实体的事件。
2. 玩家干员数据([`OpeHandler`](src/main/java/com/phasetranscrystal/nonatomic/core/OpeHandler.java))
   存储一个玩家的所拥有的干员的数据，以及部署状态干员列表。由于一个玩家可能在不同模组实现下可能具有多个干员数据，您需要为其配置合适的`ContainerId`
   以便于实体在加载时正确的捕获对应的数据。捕获数据的部分请参考[`FindOperatorEvent`](src/main/java/com/phasetranscrystal/nonatomic/event/FindOperatorEvent.java)，
   若实现了第一步提到的`GroupProvider`并进行了注册，相关事件可以自动完成。
3. 干员([`Operator`](src/main/java/com/phasetranscrystal/nonatomic/core/Operator.java))
   是干员实体可以直接捕获并引用的数据，也是存储干员信息的单元。这里面包括了干员实体基础行为相关的方法，例如部署，撤退，检查合法性；
   同时也包含了干员的基本信息，例如干员种类，状态，最后位置记录等。在其中引用的干员实体，在实体存在且被加载时就不会为空。
4. 干员数据([`OperatorInfo`](src/main/java/com/phasetranscrystal/nonatomic/core/OperatorInfo.java)
   负责存储干员的其它附加信息。有需要的话，干员实体内可以存储相同类的信息，并在恰当时机请求合并以实现临时信息功能。

您可以参考[`TestObjects`](src/main/java/com/phasetranscrystal/nonatomic/TestObjects.java)类来查看我们的测试代码，
在mod主类中调用`TestObjects#initTest`方法即可生效——不要忘记在之后删掉。

您可以在[`EventHooks`](src/main/java/com/phasetranscrystal/nonatomic/EventHooks.java)中看到我们提供的事件，
事件的类中均有关于其用途的javadoc内容。

另外，您也可以参考我们的[测试内容表](TEST_LIST.md)来查看与校对正确配置下可以自动实现的功能。

# 贡献

欢迎对Nonatomic模组进行贡献。你可以提交Issues来报告问题，或者Fork本项目来提交你的改进。

# 开发者

- **Mon-landis**：提供技术支持和开发。

---

