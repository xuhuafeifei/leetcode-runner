# README.md



## 插件介绍

`Leetcode-runner`是一个idea插件，实现一个核心功能完备，且简洁容易上手的LC刷题插件支持日常刷题所需的一切功能，实现脱离Web端LeetCode刷算法



### 1. 加载插件

插件项目启动后，会出现两块工具栏

<img src="README.assets/image-20241128122102670.png" alt="image-20241128122102670" style="zoom: 33%;" />



如果没有出现，可以通过`View -> Tool Windows -> Leetcode Console Plugin / Leetcode Plugin` 打开工具窗口

<img src="README.assets/image-20241128122143896.png" alt="image-20241128122143896" style="zoom:50%;" />





### 2. 插件初始化

1. 打开设置栏

   <img src="README.assets/image-20241128122708307.png" alt="image-20241128122708307" style="zoom:50%;" />

2. 选择lang type和store path![image-20241128122750039](README.assets/image-20241128122750039.png)

   > lang type: 指定当前用何种语言解决问题
   >
   > store path: 指定code文件创建的目录

3. 点击登录按钮，进行登录操作

   ![image-20241128122907408](README.assets/image-20241128122907408.png)

4. 正常情况下，插件会模拟浏览器，出现leetcode官网登录界面，具体如下

   <img src="README.assets/image-20241128123239830.png" alt="image-20241128123239830" style="zoom: 50%;" />

   如果登录成功，则会加载数据内容，并在右侧的工具栏中出现问题内容

<img src="README.assets/image-20241128123603101.png" alt="image-20241128123603101" style="zoom:50%;" />





### 3. 预览内容

点击右侧问题内容，插件会创建代码文件，并进行问题内容预览

![image-20241128123740673](README.assets/image-20241128123740673.png)



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