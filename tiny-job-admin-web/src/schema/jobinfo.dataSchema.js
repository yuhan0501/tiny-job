import React from 'react';
import { Switch } from 'antd';
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
    showType: 'cron',
    placeholder: '请选择或填写cron表达式',
    validator: [{ required: true, message: '请配置cron表达式' }],
  },
  {
    key: 'jobDesc',
    title: '任务描述',
    dataType: 'varchar',
    validator: [{ required: true, message: '请填写任务描述' }],
  },
  {
    key: 'jobStatus',  // 传递给后端的字段名
    title: '任务状态',  // 前端显示的名称
    dataType: 'int',
    showType: 'select',
    options: [{ key: 0, value: '已停止' }, { key: 1, value: '运行中' }],
    showInForm: false,
  },
  {
    key: 'jobStatusToggle',
    title: '任务开关',
    dataType: 'int',
    showInForm: false,
    width: 120,
    render: function (text, record) {
      const raw = record.$$rawData || {};
      const statusValue = raw.jobStatus;
      const isRunning = statusValue === 1 || statusValue === '1';
      const primaryKey = this.primaryKey || 'id';
      const rowKey = record[primaryKey] || record.key;

      return (
        <Switch
          size="small"
          checked={isRunning}
          checkedChildren="运行"
          unCheckedChildren="暂停"
          onChange={(checked) => {
            if (!rowKey) {
              return;
            }
            const nextStatus = checked ? 1 : 0;
            const payload = {
              [primaryKey]: rowKey,
              jobStatus: nextStatus,
            };
            this.handleUpdate(payload, [rowKey]);
          }}
        />
      );
    },
  },
  {
    key: 'jobType',
    title: '任务类型',
    dataType: 'varchar',
    showType: 'select',
    options: [
      { key: 'http', value: 'HTTP 接口' },
    ],
    defaultValue: 'http',
    validator: [{ required: true, message: '请选择任务类型' }],
  },
  {
    key: 'jobConfig.executeService',
    title: '执行服务地址',
    dataType: 'varchar',
    showInTable: false,
    placeholder: 'http://service/path/to/resource',
    validator: [{ required: true, message: '请填写执行服务地址' }],
  },
  {
    key: 'jobConfig.executeMethod',
    title: '执行方法',
    dataType: 'varchar',
    showType: 'select',
    options: [
      { key: 'GET', value: 'HTTP GET' },
      { key: 'POST', value: 'HTTP POST' },
      { key: 'PUT', value: 'HTTP PUT' },
      { key: 'DELETE', value: 'HTTP DELETE' },
      { key: 'PATCH', value: 'HTTP PATCH' },
    ],
    defaultValue: 'POST',
    showInTable: false,
    validator: [{ required: true, message: '请选择执行方法' }],
  },
  {
    key: 'jobConfig.executeParam',
    title: '执行参数',
    dataType: 'varchar',
    showInTable: false,
    showType: 'textarea',
    placeholder: 'a=b&c=d',
  },
  {
    key: 'executeBlockStrategy',
    title: '阻塞策略',
    dataType: 'varchar',
    validator: [{ required: true, message: '请填写阻塞策略' }],
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
  {
    key: 'singleRecordActions',
    title: '操作',
    width: 160,
    actions: [
      {
        name: '修改',
        type: 'update',
      },
      {
        name: '删除',
        type: 'delete',
      },
    ],
    showInForm: false,
  },
];
