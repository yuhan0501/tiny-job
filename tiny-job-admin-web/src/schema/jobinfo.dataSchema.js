import React from 'react';
import { Link } from 'react-router';
module.exports = [
  {
    key: 'id',
    title: 'ID',
    dataType: 'int',
    primary: true,
    // 表格中根据这一列排序, 排序规则可以配置
    sorter: (a, b) => a.id - b.id,
  },
  {
    key: 'jobCron',
    title: 'cron表达式',
    dataType: 'varchar',
  },
  {
    key: 'jobDesc',
    title: '任务描述',
    dataType: 'varchar',
  },
  {
    key: 'jobStatus',  // 传递给后端的字段名
    title: '任务状态',  // 前端显示的名称
    dataType: 'int',
    showType: 'select',
    options: [{ key: 0, value: '已停止' }, { key: 1, value: '运行中' }],
  },
  {
    key: 'jobType',
    title: '任务类型',
    dataType: 'varchar',
  },
  {
    key: 'executeBlockStrategy',
    title: '阻塞策略',
    dataType: 'varchar',
  },
  {
    key: 'author',
    title: '创建者',
    dataType: 'varchar',
    showInForm: false,
  },
  {
    key: 'createTime',
    title: '创建时间',
    dataType: 'datetime',
    showInForm: false,
    render: (text, record) => {
      return new Date(text).format('yyyy-MM-dd HH:mm:ss');
    },
  },
  {
    key: 'updateTime',
    title: '变更时间',
    dataType: 'datetime',
    showInForm: false,
    render: (text, record) => {
      return new Date(text).format('yyyy-MM-dd HH:mm:ss')
    },
  },
  {
    key: 'configId',
    title: 'configId',
    dataType: 'int',
    showInTable: false,
    showInForm: false,
  },
  {
    key: 'executeTimeout',
    title: 'executeTimeout',
    dataType: 'int',
    showInTable: false,
    showInForm: false,
  },
  {
    key: 'executeFailRetryCount',
    title: 'executeFailRetryCount',
    dataType: 'int',
    showInTable: false,
    showInForm: false,
  },
  {
    key: 'childJobId',
    title: 'childJobId',
    dataType: 'int',
    showInTable: false,
    showInForm: false,
  },
  {
    key: 'jobStatus',
    title: 'jobStatus',
    dataType: 'int',
    showInTable: false,
    showInForm: false,
  },
  {
    key: 'triggerLastTime',
    title: 'triggerLastTime',
    dataType: 'int',
    showInTable: false,
    showInForm: false,
  },
  {
    key: 'triggerNextTime',
    title: 'triggerNextTime',
    dataType: 'int',
    showInTable: false,
    showInForm: false,
  },
  {
    key: 'jobVersion',
    title: 'jobVersion',
    dataType: 'int',
    showInTable: false,
    showInForm: false,
  },
  {
    key: 'jobConfig',
    title: 'jobConfig',
    dataType: 'varchar',
    showInTable: false,
    showInForm: false,
  },
];
