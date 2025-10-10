import React from 'react';
import { Card, Button, Tag, Alert, Spin, Row, Col, message } from 'antd';
import { fetchPauseStatus, pauseAll, resumeAll } from '../../api/control';
import './index.less';

class PauseControl extends React.PureComponent {
  state = {
    loading: true,
    actionLoading: false,
    paused: false,
    runningCount: 0,
    pausedAt: null,
    error: null,
  };

  componentDidMount() {
    this.loadStatus();
  }

  loadStatus = async () => {
    this.setState({ loading: true, error: null });
    try {
      const resp = await fetchPauseStatus();
      if (resp.code === 0) {
        const data = resp.data || {};
        this.setState({
          paused: !!data.paused,
          runningCount: data.runningCount || 0,
          pausedAt: data.pausedAt && data.pausedAt > 0 ? data.pausedAt : null,
          loading: false,
        });
      } else {
        this.setState({ loading: false, error: resp.msg || '获取状态失败' });
      }
    } catch (e) {
      this.setState({ loading: false, error: e.message || '网络异常' });
    }
  };

  handlePause = async () => {
    this.setState({ actionLoading: true, error: null });
    try {
      const resp = await pauseAll();
      if (resp.code === 0) {
        message.success('已进入暂停状态');
        await this.loadStatus();
      } else {
        message.warning(resp.msg || '操作未生效');
      }
    } catch (e) {
      message.error(e.message || '网络请求失败');
    } finally {
      this.setState({ actionLoading: false });
    }
  };

  handleResume = async () => {
    this.setState({ actionLoading: true, error: null });
    try {
      const resp = await resumeAll();
      if (resp.code === 0) {
        message.success('已恢复任务调度');
        await this.loadStatus();
      } else {
        message.warning(resp.msg || '操作未生效');
      }
    } catch (e) {
      message.error(e.message || '网络请求失败');
    } finally {
      this.setState({ actionLoading: false });
    }
  };

  renderActions() {
    const { paused, actionLoading } = this.state;
    if (paused) {
      return (
        <Button type="primary" loading={actionLoading} onClick={this.handleResume}>
          恢复调度
        </Button>
      );
    }
    return (
      <Button type="danger" loading={actionLoading} onClick={this.handlePause}>
        一键暂停
      </Button>
    );
  }

  render() {
    const { loading, paused, runningCount, pausedAt, error } = this.state;

    return (
      <div className="pause-control-container">
        <Spin spinning={loading} tip="加载中...">
          <Row gutter={24}>
            <Col xs={24} sm={12}>
              <Card title="任务执行状态" className="pause-card" extra={this.renderActions()}>
                <div className="status-row">
                  <Tag color={paused ? 'red' : 'green'}>{paused ? '已暂停' : '运行中'}</Tag>
                  <span className="status-desc">
                    {paused ? '调度器不会触发新的任务执行' : '系统按照计划调度任务'}
                  </span>
                </div>
                <div className="pause-metric">
                  <div className="pause-metric-label">当前运行中的任务</div>
                  <div className="pause-metric-value">{runningCount}</div>
                </div>
                {pausedAt && (
                  <div className="paused-time">
                    暂停时间：{new Date(pausedAt).toLocaleString()}
                  </div>
                )}
              </Card>
            </Col>
            <Col xs={24} sm={12}>
              <Card title="说明" className="pause-card">
                <p>· 暂停为临时控制，任务配置状态不会被修改。</p>
                <p>· 暂停期间，已在执行中的任务会继续运行，新触发将被忽略。</p>
                <p>· 恢复后，调度器会按原计划继续执行。</p>
              </Card>
            </Col>
          </Row>
          {error && <Alert className="pause-error" type="error" message={error} showIcon />} 
        </Spin>
      </div>
    );
  }
}

export default PauseControl;
