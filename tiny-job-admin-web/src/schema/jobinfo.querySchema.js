// 定义某个表的querySchema
// schema的结构和含义参考下面的例子
// 注意: 所有的key不能重复
module.exports = [
  {
    key: 'id',
    title: 'ID',
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
    showType: 'select',  // 下拉框选择, antd版本升级后, option的key要求必须是string, 否则会有个warning, 后端反序列化时要注意
    options: [{ key: '', value: '全部' }, { key: '0', value: '已停止' }, { key: '1', value: '运行中' }],
    defaultValue: '',
  },
];
