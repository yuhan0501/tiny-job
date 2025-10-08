import React from 'react';
import { Link } from 'react-router';
import ajax from '../utils/ajax';
import globalConfig from '../config';
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
    showType: 'select',
    options: [{ key: 'http', value: 'HTTP调用' }, { key: 'shell', value: '脚本' }],
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
      return text ? new Date(text).format('yyyy-MM-dd HH:mm:ss') : '';
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
    key: 'triggerLastTime',
    title: '上次触发时间',
    dataType: 'datetime',
    showInForm: false,
    render: (text) => {
      return text ? new Date(text).format('yyyy-MM-dd HH:mm:ss') : '';
    },
  },
  {
    key: 'triggerNextTime',
    title: '下次触发时间',
    dataType: 'datetime',
    showInForm: false,
    render: (text) => {
      return text ? new Date(text).format('yyyy-MM-dd HH:mm:ss') : '';
    },
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
  {
    key: 'singleRecordActions',
    title: '操作',
    width: 200,
    actions: [
      {
        name: '暂停',
        type: 'request',
        confirm: '确认暂停该任务吗？',
        visible: record => record.$$rawData && record.$$rawData.jobStatus === 1,
        request: record => ajax.post(`${globalConfig.getAPIPath()}/jobinfo/pause`, { id: record.$$rawData.id }),
        successMessage: '任务已暂停',
      },
      {
        name: '恢复',
        type: 'request',
        confirm: '确认恢复该任务吗？',
        visible: record => record.$$rawData && record.$$rawData.jobStatus === 0,
        request: record => ajax.post(`${globalConfig.getAPIPath()}/jobinfo/resume`, { id: record.$$rawData.id }),
        successMessage: '任务已恢复',
      },
    ],
  },
];
