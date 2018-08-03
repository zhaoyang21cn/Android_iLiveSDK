# Android_TRTC
腾讯实时音视频(TRTC)，集成了账号登录、音视频通话、文本消息聊天等基础功能，可在无音视频基础技术的情况下，快速接入开发定制化的实时音视频产品。

# 集成音视频SDK
我们将底层音视频能力封装成了一套SDK集合，包含有

SDK  | 说明
-- | --
[IMSDK](https://cloud.tencent.com/product/im)  | 提供 IM 即时通信功能
AVSDK  | 提供底层音视频功能
iLiveSDK  | 在 AVSDK 基础上封装而成，提供更简单易用的音视频功能接口
[ilivefilter(可选)](https://github.com/zhaoyang21cn/iLiveSDK_Android_Suixinbo/blob/master/doc/ILiveSDK/ilivefiltersdk-README.md)  | 提供美颜预处理功能

在开发自己的实时音视频产品前需要先将上述SDK集成在工程中。

> 详细集成方法请参考[集成SDK](https://cloud.tencent.com/document/product/647/16796)
# API调用
在集成完SDK后，只需要**4步**API调用，即可完成音视频通话、文本消息聊天等功能，具体调用接口如下：

**1、初始化SDK**

```Java
// 初始化iLiveSDK
ILiveSDK.getInstance().initSdk(this, Constants.SDKAPPID, Constants.ACCOUNTTYPE);
// 初始化iLiveSDK房间管理模块
ILiveRoomManager.getInstance().init(new ILiveRoomConfig());
```

**2、账号登录**
```Java
ILiveLoginManager.getInstance().iLiveLogin(identifer, userSig, callback);
```
> 详情参见[登录](https://cloud.tencent.com/document/product/647/16805)

**3、设置渲染控件**
```Java
ILiveRoomManager.getInstance().initAvRootView(avRootView);
```

**4、创建房间或者加入房间**
- 如果房间号不存在使用创建房间接口
```Java
ILiveRoomOption option = new ILiveRoomOption()
                .imsupport(false)       // 不需要IM功能
                .privateMapKey          // 设置进房签名
                .controlRole("Host")  // 使用Host角色
                .exceptionListener(this)  // 监听异常事件处理
                .roomDisconnectListener(this)   // 监听房间中断事件
ILiveRoomManager.getInstance().createRoom(roomid, option, callback);
```
详情参见[创建并加入房间](https://cloud.tencent.com/document/product/647/16806)

- 如果房间号已存在则使用加入房间接口
```Java
ILiveRoomOption option = new ILiveRoomOption()
                .imsupport(false)       // 不需要IM功能
                .privateMapKey(privateMapKey) // 进房签名
                .exceptionListener(this)  // 监听异常事件处理
                .roomDisconnectListener(this)   // 监听房间中断事件
                .controlRole("Guest")  // 使用Guest角色
                .autoCamera(false)       // 进房间后不需要打开摄像头
                .autoMic(false);         // 进房间后不需打开Mic
ILiveRoomManager.getInstance().joinRoom(roomid, option, callback);
```
详情参见[加入房间](https://cloud.tencent.com/document/product/647/16807)

# 一步接入视频通话
Demo中的VideoCall为视频通话的代码:

包名|描述
:--:|:--
business|业务模块，用于与业务服务器通讯(使用自己的业务服务器时需要修改)
trtcsdk|sdk模块，用于调用SDK接口实现音视频功能(可以直接使用)
ui|界面展示，用户可以根据自己的需求自行修改
