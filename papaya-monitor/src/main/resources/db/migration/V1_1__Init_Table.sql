drop table if exists monitor_client;

drop table if exists monitor_service;

drop table if exists monitor_service_method;

/*==============================================================*/
/* Table: monitor_client                                        */
/*==============================================================*/
create table monitor_client
(
   id                   int not null auto_increment comment '主键',
   client_zk            char(255) comment 'Zk地址',
   client_ip            char(20) not null comment '客户端IP',
   client_port          int not null comment '客户端端口',
   client_status        tinyint(1) not null comment '客户端状态',
   client_info          varchar(200) comment '客户端描述',
   primary key (id)
);

alter table monitor_client comment '客户端管理';

/*==============================================================*/
/* Table: monitor_service                                       */
/*==============================================================*/
create table monitor_service
(
   id                   int not null auto_increment comment '主键',
   service_zk           char(255) comment 'Zk地址',
   service_ip           char(20) not null comment '服务端IP',
   service_port         int not null comment '服务端端口',
   service_status       tinyint(1) not null comment '服务端状态',
   service_code         char(50) comment '服务名',
   service_info         varchar(200) comment '服务端描述',
   primary key (id)
);

alter table monitor_service comment '服务端管理';

/*==============================================================*/
/* Table: monitor_service_method                                */
/*==============================================================*/
create table monitor_service_method
(
   id                   int not null auto_increment comment '主键',
   service_id           int not null comment '服务编号',
   method_name          char(50) not null comment '方法名',
   method_status        tinyint(1) not null comment '方法状态',
   method_info          varchar(200) comment '方法描述',
   primary key (id)
);

alter table monitor_service_method comment '服务方法管理';
