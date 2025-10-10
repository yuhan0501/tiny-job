import React from 'react';
import { Card, Row, Col, Spin, Alert, Table, Tag } from 'antd';
import { fetchRuntimeStats } from '../../api/control';
import './index.less';

class JobDashboard extends React.PureComponent {
  state = {
    loading: true,
    error: null,
    data: {
      paused: false,
      runningJobs: 0,
      totalExecutions: 0,
      successCount: 0,
      failedCount: 0,
      dailyStats: [],
      topJobs: [],
    },
  };

  componentDidMount() {
    this.loadStats();
  }

  loadStats = async () => {
    this.setState({ loading: true, error: null });
    try {
      const resp = await fetchRuntimeStats();
      if (resp.code === 0) {
        const data = resp.data || {};
        this.setState({
          loading: false,
          data: {
            paused: !!data.paused,
            runningJobs: data.runningJobs || 0,
            totalExecutions: data.totalExecutions || 0,
            successCount: data.successCount || 0,
            failedCount: data.failedCount || 0,
            dailyStats: data.dailyStats || [],
            topJobs: data.topJobs || [],
          },
        });
      } else {
        this.setState({ loading: false, error: resp.msg || '获取统计信息失败' });
      }
    } catch (e) {
      this.setState({ loading: false, error: e.message || '网络异常' });
    }
  };

  renderSummaryCard(label, value, color) {
    return (
      <Col xs={24} sm={12} md={6} key={label}>
        <Card>
          <div className="dashboard-metric-label">{label}</div>
          <div className="dashboard-metric-value" style={color ? { color } : null}>{value}</div>
        </Card>
      </Col>
    );
  }

  renderBarChart() {
    const { dailyStats } = this.state.data;
    if (!dailyStats || dailyStats.length === 0) {
      return <div className="chart-empty">暂无调度记录</div>;
    }
    const maxTotal = dailyStats.reduce((max, item) => Math.max(max, item.total || 0), 1);
    return (
      <div className="chart-container">
        {dailyStats.map(item => {
          const total = item.total || 0;
          const successHeight = maxTotal ? (item.success / maxTotal) * 100 : 0;
          const failedHeight = maxTotal ? (item.failed / maxTotal) * 100 : 0;
          return (
            <div className="chart-bar" key={item.date} title={`成功 ${item.success} / 失败 ${item.failed}`}>
              <div className="chart-bar-stack">
                <div className="chart-bar-fail" style={{ height: `${failedHeight}%` }} />
                <div className="chart-bar-success" style={{ height: `${successHeight}%` }} />
              </div>
              <span className="chart-bar-label">{item.date.substring(5)}</span>
              <span className="chart-bar-total">{total}</span>
            </div>
          );
        })}
      </div>
    );
  }

  render() {
    const { loading, error, data } = this.state;

    const columns = [
      { title: '任务ID', dataIndex: 'jobId', key: 'jobId', width: 100 },
      { title: '任务描述', dataIndex: 'jobDesc', key: 'jobDesc' },
      { title: '成功次数', dataIndex: 'success', key: 'success', width: 120 },
      { title: '失败次数', dataIndex: 'failed', key: 'failed', width: 120 },
      { title: '总次数', key: 'total', width: 120, render: (_, record) => record.success + record.failed },
    ];

    return (
      <div className="job-dashboard-container">
        <Spin spinning={loading} tip="加载中...">
          {error && <Alert className="dashboard-error" type="error" message={error} showIcon />} 

          <Row gutter={24} className="summary-row">
            {this.renderSummaryCard('总执行次数', data.totalExecutions)}
            {this.renderSummaryCard('成功次数', data.successCount)}
            {this.renderSummaryCard('失败次数', data.failedCount, '#ff4d4f')}
            <Col xs={24} sm={12} md={6}>
              <Card>
                <div className="dashboard-metric-label">运行中的任务</div>
                <div className="dashboard-metric-value">{data.runningJobs}
                  {data.paused ? <Tag color="red" style={{ marginLeft: 8 }}>已暂停</Tag> : <Tag color="green" style={{ marginLeft: 8 }}>运行中</Tag>}
                </div>
              </Card>
            </Col>
          </Row>

          <Card title="近七日触发概览" className="chart-card">
            {this.renderBarChart()}
          </Card>

          <Card title="任务执行排行" className="table-card">
            <Table columns={columns} dataSource={data.topJobs} rowKey="jobId" pagination={false} locale={{ emptyText: '暂无数据' }} />
          </Card>
        </Spin>
      </div>
    );
  }
}

export default JobDashboard;
