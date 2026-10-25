# Hertzs' Abstract MC Mod

Hertzs 的抽象 Minecraft 模组（Fabric，Minecraft 1.21.11）。

## 功能

### 动作系统
- 按 **G** 键打开动作轮盘，用鼠标选择动作：
  - **吓唬**：播放吓人音效并散发黑色烟雾粒子
  - **挥手**：挥手打招呼
  - **坐在地上**：保持坐姿（移动 / 跳跃后取消）
  - **躺在地上**：保持躺姿（移动 / 跳跃后取消）
- 姿势由服务端权威同步，周围玩家也能看到你的坐 / 躺动作。

## 开发

```bash
./gradlew build      # 产物 build/libs/hertzsabstract-1.0.0.jar
./gradlew runClient  # 启动开发客户端
```

环境要求：JDK 21（如 `E:\jdk-21.0.1`）。

## 许可

MIT
