use master
go

if not exists(select * from sysdatabases where name='Test1')
begin
    create database Test1
    on  primary        --表示属于 primary 文件组
    (
        name='Test_data',        -- 主数据文件的逻辑名称
        filename='D:\项目\Lucene531Example\Test_data.mdf',    -- 主数据文件的物理名称
        size=5mb,    --主数据文件的初始大小
        maxsize=100mb,     -- 主数据文件增长的最大值
        filegrowth=15%        --主数据文件的增长率
    )
    log on
    (
        name='Test_log',        -- 日志文件的逻辑名称
        filename='D:\项目\Lucene531Example\Test_log.ldf',    -- 日志文件的物理名称
        size=2mb,            --日志文件的初始大小
        maxsize=20mb,        --日志文件增长的最大值
        filegrowth=1mb        --日志文件的增长率
    )
end

go

use Test1    --表示设置为在该数据库（Test）执行下面的SQL语句
go

if not exists(select * from sysobjects where name='Test')
begin
    create table Test
    (
        id int not null identity(1,1) primary key,    --设置为主键和自增长列，起始值为1，每次自增1
        [name] varchar(50) not null unique,
        model varchar(50) not null,
        detail varchar(200) not null,
        classid int not null,
        hits bigint not null,
		updatetime datetime not null,
		other varchar(50) not null,
		visible bit not null
    )
end

if not exists(select * from sysobjects where name='TestClass')
begin
    create table TestClass
    (
        id int not null identity(1,1) primary key,    --设置为主键和自增长列，起始值为1，每次自增1
        classname varchar(50) not null,
		classtip varchar(50) not null
    )
end

go



insert into Test values ('aaa','aaa','aaa',1,1,getdate(),'aaa',1);



insert into TestClass(classname,classtip) values ('classname1','classtip1');

go
