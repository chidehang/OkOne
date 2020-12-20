# OkOne
基于okhttp库的网络性能优化框架

## 简介
在APP项目中可能会包含多个组件模块，或依赖多个三方库，其中可能都有使用到okhttp框架进行网络请求。不同的组件模块和三方
库间各自创建OkHttpClient实例，或有开发者未通过单例缓存OkHttpClient，而是每次请求每次新建，这样将造成浪费并且导致不能充分利用
okhttp的请求队列和连接池等控制和优化措施。

借助该OkOne库可以**无侵入**地将分散在不同组件中的OkHttpClient进行收敛，由OkOne进行统一复用和管理。OkOne会比较OkHttpClient.Builder
进行区分复用，即相同配置的OkHttpClient.Builder将自动复用同一个OkHttpClient实例。

## 快速集成

> Minimum supported Gradle version is 6.5

- 1.在项目根目录的build.gradle里添加依赖
```
dependencies {
    classpath 'com.cdh.okone:gradle:0.1.0'
}
```

- 2.在app module的build.gradle里应用插件
```
apply plugin: 'plugin.cdh.okone'
```

- 3.在app module的build.gradle的dependencies里添加依赖
```
implementation 'com.cdh.okone:okone:0.1.2'
```

至此已完成接入，后续直接打包apk运行即可。

## 高级功能
#### 关闭开关
是否启用或关闭OkHttpClient统一复用和管理，需要在创建OkHttpClient前设置。
```
// true启用，false关闭。默认true。
OkOne.useGlobalClient = true;
```

#### 打印日志
打开或关闭OkOne打印日志。
```
// true打印，false不打印。默认true。
OkOne.setLogEnable(true);
```

#### 单独创建不受控的OkHttpClient实例
单独创建一个不经OkOne管理和复用的OkHttpClient。
```
OkHttpClient client = new OkHttpClient(builder); 
```

## Wiki
[OkOne-Home](https://github.com/chidehang/OkOne/wiki/OkOne-Home)

