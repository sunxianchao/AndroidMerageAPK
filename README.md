# AndroidMerageAPK
实现android自动打包的程序

## 行业背景
从事了三年手游运营平台的相关工作，从最开始不知道如何做一个sdk 到后来的几乎覆盖国内90%渠道的sdk，完成这些工作的同时也发现了
其中很多的问题，比如在前期设计好android客户端的基础架构后，就需要接入非常多的渠道，这就需要很多android客户端的开发人员
但是接入渠道sdk这件事很多程序员不是很喜欢做，因为做这些事情可能浪费时间比较多，但是从中有不能获得更多的有价值的东西，而且多个游戏的话 会涉及到很多重复性的工作。

## 解决问题
从13年年底的时候开始研究android逆向工程，从中有一些启发，感觉接入sdk这件事是可以非人工完成的，但是那期间个人事情比较多
未加以验证，直到后来发现了像棱镜这样的平台，号称云端打包sdk，发现已经有这样的平台了。于是也开始研究这个东西，其实原理有些绕
但是实现起来还是比较简单的，*主要解决的就是合并的两个平台中的资源文件并产生R.java 这个文件* 
我的解决方式就是  
1. 首先发行商的sdk必须按照一定的软件架构来接入，首先生成一个原始的sdk工程，这个工程是需要游戏开发商接入的，并且只有这一次需要
cp接入，这次的接入主要是把sdk中的所有接口让cp进行一个调试，方便以后我们自动生成其他渠道的游戏包  
2. 原始的sdk生成的游戏apk后，这个就是我们的原包了，然后我们拿一个只接入第三方渠道的apk，将他们两个进行合并，最后生成渠道的游戏包  
3. 先将两个工程的资源进行合并，并生成一个Manifest xml文件 这个文件中只有一个节点<manifest package='xxx' versionName='' versionCode='' xxxx/>  
4. 然后手动编译这个目录，生成一个apk文件，这里我开始是使用ant进行编译的，后来觉得有的机器上可能不会安装ant 所以就用android sdk
中自带的aapt 进行手动编译 classes.dex resource R.java  
5. 把第二步生成的apk使用apktool进行解压，会得到Smali 文件夹，这里就是classes.dex   生成的也就是java代码反编译后生成的文件，他是文本文件，打开看的话里面的逻辑还是可以看的懂的，也有专门的这种语法，可以查下相关的资料  
6. 然后将原包和渠道包中的lib、smali、assets进行合并，用apktool生成一个apk就可以了  

## 完善
这个工具可以在命令行下使用，后续可以将他做到一个web平台上，方便出游戏包，以后出渠道包这种事就可以交给运营的人自己来做了


##  USEAGE
* 由于使用了apktool 2.0版本 所以需要使用jdk1.7以上才可以通过
