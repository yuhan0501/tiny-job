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
    disabled: true,
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
  },
  {
    key: 'createTime',
    title: '创建时间',
    dataType: 'datetime',
  },
  {
    key: 'updateTime',
    title: '变更时间',
    dataType: 'datetime',
  },
];
