[TOC]



# README.md



## 插件介绍

`Leetcode-runner`是一款idea插件，旨在实现一个核心功能完备，且简洁容易上手的LC刷题插件。Leetcode-runner支持日常刷题所需的一切功能，实现脱离Web端LeetCode刷算法



### 1. 加载插件

插件项目启动后，会出现两块工具栏

![image-20241213160625306](README.assets/image-20241213160625306.png)



如果没有出现，可以通过`View -> Tool Windows -> Leetcode Console Plugin / Leetcode Plugin` 打开工具窗口

<img src="README.assets/image-20241128122143896.png" alt="image-20241128122143896" style="zoom:50%;" />





### 2. 插件初始化

1. 打开设置栏

   <img src="README.assets/image-20241213160716483.png" alt="image-20241213160716483" style="zoom: 50%;" />

2. 选择lang type和store path![image-20241128122750039](README.assets/image-20241128122750039.png)

   > lang type: 指定当前用何种语言解决问题
   >
   > store path: 指定code文件创建的目录

3. 点击登录按钮，进行登录操作

   <img src="README.assets/image-20241213160752415.png" alt="image-20241213160752415" style="zoom:50%;" />

4. 正常情况下，插件会模拟浏览器，出现leetcode官网登录界面，具体如下

   <img src="README.assets/image-20241128123239830.png" alt="image-20241128123239830" style="zoom: 50%;" />

   如果登录成功，则会加载数据内容，并在右侧的工具栏中出现问题内容

<img src="README.assets/image-20241213160955109.png" alt="image-20241213160955109" style="zoom:50%;" />



> tip: 第一次登录需要加载所有题目，这可能会耗费3-5s左右的时间，在此期间会在后台加载数据，并锁定部分功能。这是正常行为





### 3. 预览内容

点击右侧问题内容，插件会创建代码文件，并进行问题内容预览

![image-20241213161112054](README.assets/image-20241213161112054.png)



编辑栏右上角存在三个按钮

第一个按钮，会隐藏右侧内容，只留下代码部分

第二个按钮，保留左右两侧所有的内容

第三个按钮，会隐藏左侧代码编辑区域，只留下右侧部分

<img src="README.assets/image-20241128123830344.png" alt="image-20241128123830344" style="zoom:50%;" />





### 4. 运行代码

编辑栏左侧存在三个按钮

![image-20241128124011434](README.assets/image-20241128124011434.png)

第一个按钮，runcode，使用题目的默认测试样例运行代码

第二个按钮，submit code

第三个按钮，Test Cases



点击第一、第二个按钮后，会在**插件自带的控制台**输出执行信息

<img src="README.assets/image-20241128124351474.png" alt="image-20241128124351474" style="zoom:50%;" />



第三个按钮，会出现弹框，可以自定义测试数据

<img src="README.assets/image-20241128124505627.png" alt="image-20241128124505627" style="zoom:50%;" />

Reset按钮可以恢复题目的默认测试样例



## 插件安装

当前版本，leetcode-runner并未在idea的插件市场上注册，如果想要使用插件，需要通过手动方式按照



1. 在根目录下找到/jar/目录，里面有打包好的插件jar包【或者可以自行通过gradle打包项目】

   <img src="README.assets/image-20241213161325118.png" alt="image-20241213161325118" style="zoom:50%;" />

2. 打开自己的idea，File->Settings->Plugin，打开插件设置界面

   <img src="README.assets/image-20241213161442826.png" alt="image-20241213161442826" style="zoom:50%;" />

<img src="README.assets/image-20241213161425797.png" alt="image-20241213161425797" style="zoom:50%;" />



3. 点击齿轮图标，选择从disk安装plugin

<img src="README.assets/image-20241213161522585.png" alt="image-20241213161522585" style="zoom:50%;" />

4. 选择第一步获取的jar包，点击确定

<img src="README.assets/image-20241213161615696.png" alt="image-20241213161615696" style="zoom:50%;" />

5. 当出现Restart IDE按钮时，就表示插件安装成功！

<img src="README.assets/image-20241213161719788.png" alt="image-20241213161719788" style="zoom:50%;" />



## 辅助功能介绍

### 1. Reposition

该功能所在位置如图所示

![image-20241215154455937](README.assets/image-20241215154455937.png)

在插件使用的时候，可能出现如下情况。原本的code文件无法通过插件提供的编辑器形式打开，具体如下图所示

<img src="README.assets/image-20241215154420353.png" alt="image-20241215154420353" style="zoom:50%;" />



`Reposition`允许用户重新定位当前文件所代表的问题，重新加载文件，通过插件提供的编辑器打开

![image-20241215154642283](README.assets/image-20241215154642283.png)

![image-20241215154655400](README.assets/image-20241215154655400.png)



### 2. submission界面

在大于2.6.2版本中，争对提交界面做出了优化，额外增加`显示错误信息`, `代码替换`，`新增测试样例`功能，具体如下

![image-20241216205632916](README.assets/image-20241216205632916.png)



对于错误的提交记录，界面不仅显示历史代码，还会在Top栏显示错误原因，以及测试样例等信息



另外，此版本还允许用户直接替换代码，新增测试样例



**代码cv/替换**

![代码替换](README.assets/代码替换.gif)



**测试样例cv/新增**

![测试样例](README.assets/测试样例.gif)