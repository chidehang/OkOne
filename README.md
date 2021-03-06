# OkOne
基于okhttp库的网络性能优化框架

* [简介](#简介)
* [快速集成](#快速集成)
* [高级功能](#高级功能)
   * [关闭开关](#关闭开关)
   * [打印日志](#打印日志)
   * [单独创建不受控的OkHttpClient实例](#单独创建不受控的OkHttpClient实例)
   * [预建连](#预建连)
   * [请求优先级](#请求优先级)
   * [全局EventListener](#全局EventListener)
   * [全局拦截器](#全局拦截器)
* [更新日志](#更新日志)
* [BLOG](#BLOG)


## 简介
在APP项目中可能会包含多个组件模块，或依赖多个三方库，其中可能都有使用到okhttp框架进行网络请求。不同的组件模块和三方
库间各自创建OkHttpClient实例，或有开发者未通过单例缓存OkHttpClient，而是每次请求每次新建，这样将造成浪费并且导致不能充分利用
okhttp的请求队列和连接池等控制和优化措施。

借助该OkOne库可以**无侵入、无感知**地将分散在不同组件中的OkHttpClient进行收敛，由OkOne进行统一复用和管理。OkOne会比较OkHttpClient.Builder
进行区分复用，即相同配置的OkHttpClient.Builder将自动复用同一个OkHttpClient实例。

除此之外，OkOne还提供了其他扩展的高级功能，更多详细介绍可以[查看相关博文](#blog)。

## 快速集成

> Minimum supported Gradle version is 6.5

> Minimum supported OkHttp version is 4.1.0

**由于jCenter将于2021.05.01不再维护，OkOne现迁移至JitPack，请接入方重新依赖OkOne最新版本。**

- 0.添加jitpack仓库
```
maven { url 'https://jitpack.io' }
```

- 1.在项目根目录的build.gradle里添加依赖
```
dependencies {
    classpath 'com.github.chidehang.OkOne:okone-gradle-plugin:2.3.0'
}
```

- 2.在app module的build.gradle里应用插件
```
apply plugin: 'plugin.cdh.okone'
```

- 3.在app module的build.gradle的dependencies里添加依赖
```
implementation 'com.github.chidehang.OkOne:okone:2.3.0'
```

至此已完成接入，后续直接打包apk运行即可。

## 高级功能
### 关闭开关
是否启用或关闭OkHttpClient统一复用和管理，需要在创建OkHttpClient前设置。
```
// true启用，false关闭。默认true。
OkOne.useGlobalClient = true;
```

### 打印日志
打开或关闭OkOne打印日志。
```
// true打印，false不打印。默认true。
OkOne.setLogEnable(true);
```

### 单独创建不受控的OkHttpClient实例
单独创建一个不经OkOne管理和复用的OkHttpClient。
```
OkHttpClient client = new OkHttpClient(builder); 
```

### 预建连
开发者可以在合适的时机提前建立连接，若连接成功则添加进okhttp连接池。
```
OkOne.preBuildConnection(okHttpClient, url, new PreConnectCallback() {
    @Override
    public void connectCompleted(String url) {
        Log.d(TAG, "预建连成功: " + url);
    }

    @Override
    public void connectFailed(Throwable t) {
        Log.e(TAG, "预建连失败", t);
    }
});
```

### 请求优先级
支持给请求Request设置优先级，尽可能让高优先级请求任务先发起。
**优先级范围[-10,10]，默认值0，值越大优先级越高。**

- 1. 启用请求优先级功能（默认关闭）
需要在创建OkHttpClient之前设置。
```
OkOne.enableRequestPriority(true);
```

- 2. 给Request设置优先级
```
Request request = Request.Builder().url(URL_FOR_TEST).build();
OkOne.setRequestPriority(request, priority);
```

### 全局EventListener
支持设置全局EventListener用于监控通过OkHttp发起的网络请求。
设置包括**主module、子module、三方库**在内的所有OkHttpClient，但不影响业务方原先配置的EventListener和EventListener.Factory。

```
// 设置全局EventListener
OkOne.setGlobalEventListener(mTrackEventListener);
```

### 全局拦截器
支持添加全局Interceptor和NetworkInterceptor用于拦截通过OkHttp发起的网络请求。
设置包括**主module、子module、三方库**在内的所有OkHttpClient，但不影响业务方原先配置的Interceptor和NetworkInterceptor。
```
// 添加全局Interceptor
OkOne.addGlobalInterceptor(mGlobalInterceptor);

// 添加全局NetworkInterceptor
OkOne.addGlobalNetworkInterceptor(mGlobalNetworkInterceptor);
```

## 更新日志
[Change Log](https://github.com/chidehang/OkOne/wiki/Change-Log)

## BLOG

[《OkOne-基于okhttp的网络性能优化框架》](https://juejin.cn/post/6908178914779561997)

[《OkOne-高级功能之OkHttp预建连以及原理剖析》](https://juejin.cn/post/6909817749493514247)

[《OkOne-如何给okhttp的请求设置优先级》](https://juejin.cn/post/6920850276437983239/)
