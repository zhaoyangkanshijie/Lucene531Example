# 全文搜索lucene5.3.1说明文档

[1. 工作原理](#工作原理)

[2. 项目结构](#项目结构)

[3. 数据结构](#数据结构)

[4. 业务逻辑](#业务逻辑)

## 工作原理
全文检索(Full-Text Retrieval)是计算机程序通过扫描文章中的每一个词，对每一个词建立一个索引，指明该词在文章中出现的次数和位置。当用户查询时根据建立的索引查找，类似于通过字典的检索字表查字的过程。关于全文检索的特性，我们要知道：1，只处理文本。2，不处理语义。3，搜索时英文不区分大小写。4，结果列表有相关度排序。

参考：
1. [lucene全文检索精华](https://wenku.baidu.com/view/876b700ee009581b6ad9eb15.html)
2. [全文检索的基本原理](https://blog.csdn.net/forfuture1978/article/details/4711308)
3. [Lucene5.3.1 API](http://lucene.apache.org/core/5_3_1/core/index.html)

### (1) 分词器
lucene需要通过分词器(analyzer)拆分句子、词组，以便存储和搜索，本项目用的是IK分词器2012V5版本。

友情提示：创建索引的分词方法须与搜索时的分词方法一致。

### (2) 创建索引
我们会从数据库拿到一些数据，每个字段对应lucene的每个域(field)，经过分词处理的域的组合形成一个文档(document)。如下图：

* 数据表

    |         |  字段1  |  字段2  |
    | --------| ------ | ------  |
    | id=1    |   abc  |   abc   |
    | id=2    |   abc  |   abc   |
    | id=3    |   abc  |   abc   |

* lucene文档

    |         |   域1  |    域2   |
    | --------| ------ | ------  |
    | 索引1    |  a,b,c |  a,b,c  |
    | 索引2    |  a,b,c |  a,b,c  |
    | 索引3    |  a,b,c |  a,b,c  |

当然，需要事先设置文档的保存路径(directory)，设置文档的名字(type)，创建写入器(IndexWriter)，写入器导致数据每次的改动都要保存(commit)和关闭(close)。

### (3) 搜索
* 调用lucene的搜索方法，并设置出现条件：必须出现(MUST)、应该出现(SHOULD)、不出现(MUST_NOT)
    * [多域搜索 MultiFieldQueryParser](http://www.cnblogs.com/xudong-bupt/archive/2013/05/08/3065297.html)
    * [通配符搜索 WildcardQuery](https://blog.csdn.net/u012965373/article/details/44783817)
    * [模糊搜索 FuzzyQuery](https://blog.csdn.net/iamaboyy/article/details/7747095)
    * [精确搜索 TermQuery](https://yq.aliyun.com/articles/45354)
    * [更多搜索方法参考1](https://blog.csdn.net/pangliang_csdn/article/details/51752665)
    * [更多搜索方法参考2](https://www.cnblogs.com/linjiqin/archive/2013/06/08/3125861.html)

* 设置搜索结果的权重(setBoost)来控制排序

* 设置sort函数控制排序
    * [lucene排序---相关度与其他组合排序](https://blog.csdn.net/JackieLiuLixi/article/details/40149101)
    * [lucene sort field 排序字段](http://www.fengxiaochuang.com/?p=160)
    * [lucene按时间排序显示](https://www.cnblogs.com/lingyi/articles/6249664.html)
    * [lucene/IK查询怎么在得分一样的情况下按照另一个字段排序](https://ask.csdn.net/questions/679733)
    * [Lucene sort 排序](https://blog.csdn.net/jackieliulixi/article/details/40149313)
    * 简单总结：先在创建单条索引时，向文档加入可比较域，如NumericDocValuesField,搜索时定义sort，并用sortfield初始化，然后search（query，limit，sort，[hasScoreCalculate=]true，[hasMaxScore=]true）确保排序的同时计算分数。

* 关键词替换为包含html class的关键词，以便高亮

### (4) 索引表的增删改

* 增(add)：同创建索引
* 删(delete)：删除指定索引(deleteDocuments)，删除全部索引(deleteAll)
* 改(update)：更新索引,机制为先删除再创建(updateDocument)

>[参考样例1](https://blog.csdn.net/napoay/article/details/53074118)

>[参考样例2](https://blog.csdn.net/wd501771382/article/details/51190437)

## 项目结构

* Lucene531
    * src
        * com.search
            * SearchHelper.java --索引表的增删改查
        * com.model
            * SearchModel.java --数据结构模型类
        * com.servlet
            * InitialServlet.java --初始化索引表入口
            * ManagerServlet.java --索引表增删改入口
            * SearchServlet.java --搜索入口
        * com.util
            * EntryUtil.java --初始化索引表、定时任务、搜索组装
            * HelpUtil.java --工具函数
            * Output.java --异常信息打印
            * SQLUtil.java --获取数据库数据
    * config.properties
    * WebRoot
        * index.html --搜索页面
        * initial.html --初始化索引表页面
        * manager.html --索引表增删改页面

## 数据结构

1. 数据模型 SearchModel.java

    与数据库表对应如下：

<table border="1" cellspacing="0">
    <tr>
        <td></td>
        <td>id(int)</td>
        <td>name(String)</td>
        <td>model(String)</td>
        <td>detail(String)</td>
        <td>classid(int)</td>
    </tr>
    <tr>
        <td>Test</td>
        <td>id(int)</td>
        <td>name(varchar)</td>
        <td>model(varchar)</td>
        <td>detail(text/varchar)</td>
        <td>classid(int)</td>
    </tr>
</table>
<br>
<table border="1" cellspacing="0">
    <tr>
        <td></td>
        <td>hits(int)</td>
        <td>updatetime(Date)</td>
        <td>other(String)</td>
        <td>visible(Boolean)</td>
    </tr>
    <tr>
        <td>Test</td>
        <td>hits(bigint)</td>
        <td>updatetime(datetime)</td>
        <td>other(varchar)</td>
        <td>visible(bit/smallint)</td>
    </tr>
</table>
<br>
<table border="1" cellspacing="0">
    <tr>
        <td></td>
        <td>classname(String)</td>
    </tr>
    <tr>
        <td>TestClass</td>
        <td>classname(varchar)</td>
    </tr>
</table>

2. 搜索数据组装 EntryUtil.java

    2.1 json：空集合和搜索结果数量。（原因：搜索不到结果，或关键词长度小于2）

    数据样例：
    ```json
    {
        “result” :[],
        “testCount”:”0”
    }
    ```

    2.2 test的json数据和test的搜索结果数量。（最多不超过500条）
    
    数据样例：
    ```json
    {
        “result” :
        [
            {
                “classid”:”1”,
                ”detail”:”aaa”,
                ”displayorder”:”1”,
                ”hits”:”999”,
                ”id”:”1”,
                ”model”:”sadsad”,
                ”name”:”sad”,
                ”other”:”asd”,
                ”updatetime”:”2012-08-17 16:22:00”,
                ”visible”:”true”,
                ”classname”:”asddd”
            },
            {...},
            ...,
            {...}
        ],
        “testCount”:”500”
    }
    ```

    2.3 增删改和初始化索引表

    成功：
    ```json
    [{“errCode”:0}]
    ```

    失败：
    ```json
    [{“errCode”:-1}]
    ```

## 业务逻辑

### 函数实现

1. EntryUtil.java --初始化索引表、定时任务、搜索组装

    <span id="1-1">1.1</span> static 静态构造函数

    * 第一次访问会初始化索引表和设置定时初始化索引表任务。

    <span id="1-2">1.2</span> Boolean initializeIndex() 初始化索引表

    * 创建索引表
    * 返回标志符

    <span id="1-3">1.3</span> void setTime() 定时初始化索引表任务

    * 设置每天24:00调用初始化索引表函数

    <span id="1-4">1.4</span> String searchServer(String keywords,String type) 返回搜索数据

    * 过滤关键词长度小于2
    * 搜索关键词，并获得搜索结果数量
    * 数据组装(详见[数据结构](#数据结构)第2点)


2. SQLUtil.java --获取数据库数据

    <span id="2-1">2.1</span> static SQLUtil getInstance() 获取数据库实例

    <span id="2-2">2.2</span> Connection getConnection() 数据库连接

    * 获取配置文件中的链接字符串
    * 设置数据库驱动
    * 获取连接并返回

    <span id="2-3">2.3</span> ArrayList<SearchModel> getProductList() 获取test列表

    * 执行SQL语句
    * 填充数据模型(详见[数据结构](#数据结构)第1点)
    * 关闭连接

    <span id="2-4">2.4</span> SearchModel getProductChangedData(String id) 获取test改动数据

    * 执行SQL语句
    * 填充数据模型(详见[数据结构](#数据结构)第1点)
    * 关闭连接

3. HelpUtil.java --工具函数

    <span id="3-1">3.1</span> String filterHtml(String html) 过滤危险字符

    * 去除js的&nbsp
    * 去除html及其属性
    * 将标点符号 Unicode空白符替换为“|”

    <span id="3-2">3.2</span> Boolean filterString_setBootst(String s) 判断是否常用词

    * 常用词包括aa，bb，cc，dd

    <span id="3-3">3.3</span> String[] mergeIdentical(String[] str) 合并字符串数组中相同的字符串

    <span id="3-4">3.4</span> String convertToJson(ArrayList<SearchModel> datalist) 列表转json

    <span id="3-5">3.5</span> ArrayList<SearchModel> fillDisplayorder(ArrayList<SearchModel> datalist) 填充列表的displayorder项，按升序排列

    <span id="3-6">3.6</span> String responseFormat(String maindata,String dataCount) 数据组装函数

    <span id="3-7">3.7</span> 日期和字符串相互转换函数

    * String dateToString(Date date)
    * Date stringToDate(String dateString)

4. SearchHelper.java --索引表的增删改查

    <span id="4-1">4.1</span> FSDirectory getDirectory(String type) 创建文档

    * 获取配置文件的文档文件夹路径
    * 创建文件并返回

    <span id="4-2">4.2</span> String Token(String str) 分词

    * 使用IK分词，词组以“|”隔开

    <span id="4-3">4.3</span> boolean createIndex(ArrayList<SearchModel> datalist, String type) 创建索引

    * 创建IK分析器 Analyzer
    * 创建文档 getDirectory
    * 配置分析器 IndexWriterConfig
    * 创建写入器 IndexWriter
    * 清空文档索引内容 deleteAll
    * 创建每一条索引 createSingleIndex
    * 保存和关闭

    <span id="4-4">4.4</span> boolean createSingleIndex(IndexWriter iwriter, SearchModel data) 创建一条索引

    * 新建文档 Document
    * 设置文档属性 FieldType

        * setStoreTermVectorOffsets 记录相对增量
        * setStoreTermVectorPositions 记录位置信息
        * setStoreTermVectors 存储向量信息
        * freeze 阻止改动信息

    * 创建域 Field
    * 设置域权重 setBoost
    * 向文档加入域 add
    * 写入文档 addDocument

    <span id="4-5">4.5</span> ArrayList<SearchModel> search(String keywords, String type) 查询关键词

    * 过滤危险字符
    * 打开文档，创建分析器
    * 关键词分词
        * IK分词数组，权重最小
        * 纯数字和字母数组（字母和数字分开）
        * 纯数字和字母数组（字母和数字可以连在一起），权重最大
    * 使用多种搜索方式组合搜索(详见[工作原理](#工作原理)第(3)点)
    * 查询结果高亮

        * FragListBuilder使用SimpleFragListBuilder
        * FragmentsBuilder使用ScoreOrderFragmentsBuilder
        * FastVectorHighlighter
        * getFieldQuery
        * getBestFragment

    * 数据按需填充，如限制得分或条数
    * 关闭写入器和文档

    <span id="4-6">4.6</span> int searchByTerm(String field,String name,String type)  查找域目标数量

    * 通过精确查询，计算目标数量

    <span id="4-7">4.7</span> int getSearchCount(String keywords, String type) 获取搜索结果条数

    * 与搜索关键词的搜索逻辑相同，搜索完后计数

    <span id="4-8">4.8</span> 索引增删改
    
    * boolean addIndex(SearchModel data, String type) 
    * boolean updateIndex(String id, SearchModel data, String type)
    * boolean deleteAllIndex(String type)
    * boolean deleteIndex(String id, String type)

5. Output.java --异常信息打印

    <span id="5-1">5.1</span> boolean createFile(String fileName,String filecontent) 创建文件

    <span id="5-2">5.2</span> boolean writeFileContent(String filepath,String newstr) 向文件中写入内容

### 函数关系

#### InitialServlet 初始化索引表流程

直接初始化索引，详见[1.2](#1-2)。

#### ManagerServlet 索引表增删改流程

1. java获取post参数
2. 判断type
3. 判断id是否存在
    * 若存在：判断visible：1为更新索引操作；0为删除索引操作
    * 若不存在：判断visible：1为插入索引；0不做任何操作

* visible：[2.4](#2-4)
* 更新索引操作：[4.8](#4-8)
* 删除索引操作：[4.8](#4-8)
* 插入索引：[4.8](#4-8)
* 异常：获取数据库数据为空

#### SearchServlet 搜索流程

大致流程：

|0|1|→|2|→|3|→|4|→|5|→|6|→|7|→|8|
|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|
|1|[1.1](#1-1)|→|[1.2](#1-2)|→|[1.3](#1-3)|→|return|
|↓| | | |↘|[4.3](#4-3)|→|[4.8](#4-8)|→|[4.4](#4-4)|→|return|
|2|[1.4](#1-4)|→|[3.1](#3-1)|→|[4.5](#4-5)|→|[4.7](#4-7)|→|[3.5](#3-5)|→|[3.4](#3-4)|→|[3.6](#3-6)|→|return|





