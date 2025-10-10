/**
 * 定义sidebar和header中的菜单项
 *
 * 一些约定:
 * 1.菜单最多3层;
 * 2.只有"叶子"节点才能跳转;
 * 3.所有的key都不能重复;
 */

// 其实理论上可以嵌套更多层菜单的, 但是我觉得超过3层就不好看了
// 可用的图标见这里: https://ant.design/components/icon-cn/

// 定义siderbar菜单
const sidebarMenu = [
  {
    key: 'job',
    name: '定时任务',
    icon: 'clock-circle',
    child: [
      {
        key: 'job-manage',
        name: '任务管理',
      },
    ],
  },
  {
    key: 'operations',
    name: '运维中心',
    icon: 'tool',
    child: [
      {
        key: 'pause-control',
        name: '任务总控',
      },
      {
        key: 'job-dashboard',
        name: '调度监控',
      },
    ],
  },
];

export default sidebarMenu;

// 定义header菜单, 格式和sidebar是一样的
// 特殊的地方在于, 我规定header的最右侧必须是用户相关操作的菜单, 所以定义了一个特殊的key
// 另外注意这个菜单定义的顺序是从右向左的, 因为样式是float:right
export const headerMenu = [
  {
    key: 'usageGuide',
    name: '使用指南',
    icon: 'book',
    url: '/tiny-job/static/index.html',
  },
  {
    key: 'releaseNotes',
    name: '发布记录',
    icon: 'profile',
    url: '/tiny-job/static/index.html#release',
  },
];
