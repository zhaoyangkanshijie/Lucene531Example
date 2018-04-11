use master
go

if not exists(select * from sysdatabases where name='Test1')
begin
    create database Test1
    on  primary        --��ʾ���� primary �ļ���
    (
        name='Test_data',        -- �������ļ����߼�����
        filename='D:\��Ŀ\Lucene531Example\Test_data.mdf',    -- �������ļ�����������
        size=5mb,    --�������ļ��ĳ�ʼ��С
        maxsize=100mb,     -- �������ļ����������ֵ
        filegrowth=15%        --�������ļ���������
    )
    log on
    (
        name='Test_log',        -- ��־�ļ����߼�����
        filename='D:\��Ŀ\Lucene531Example\Test_log.ldf',    -- ��־�ļ�����������
        size=2mb,            --��־�ļ��ĳ�ʼ��С
        maxsize=20mb,        --��־�ļ����������ֵ
        filegrowth=1mb        --��־�ļ���������
    )
end

go

use Test1    --��ʾ����Ϊ�ڸ����ݿ⣨Test��ִ�������SQL���
go

if not exists(select * from sysobjects where name='Test')
begin
    create table Test
    (
        id int not null identity(1,1) primary key,    --����Ϊ�������������У���ʼֵΪ1��ÿ������1
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
        id int not null identity(1,1) primary key,    --����Ϊ�������������У���ʼֵΪ1��ÿ������1
        classname varchar(50) not null,
		classtip varchar(50) not null
    )
end

go



insert into Test values ('aaa','aaa','aaa',1,1,getdate(),'aaa',1);



insert into TestClass(classname,classtip) values ('classname1','classtip1');

go
