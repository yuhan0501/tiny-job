import React from 'react';
import './index.less';

/**
 * 展示欢迎界面
 */
class Welcome extends React.PureComponent {

  render() {
    return (
      <div>
        <div className="welcome-text">
          <h1>Tiny Job 管理平台</h1>
          <p>轻量级分布式任务调度中心，提供可视化任务维护、执行监控与多服务触发能力。</p>
          <p>核心特性：Spring Boot + MyBatis 后端，React + Ant Design 前端，支持 H2 / MySQL 环境切换。</p>
          <p>快速开始：配置任务 → 绑定执行器 → 观察调度日志，体验秒级调度。</p>
        </div>
      </div>
    );
  }

}

export default Welcome;
