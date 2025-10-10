# 接口

## 查询job列表
### 请求方式
GET 
### 请求地址
tiny-job/jobinfo/list
### 请求参数

| 参数名      | 是否必填 | 描述                   |
| ----------- | -------- | ---------------------- |
| jobStatus   | 否       | job的状态，0停止 1运行 |
| jobDesc     | 否       | job描述                |
| currentPage | 是       | 当前页码               |
| pageSize    | 是       | 每页大小               |

### 返回参数

| 参数名 | 数据类型      | 描述                   |
| ------ | ------------- | ---------------------- |
| code   | int           | 0表示成功，非0表示失败 |
| msg    | String        | 失败原因               |
| data   | List<JobInfo> | 返回数据体             |
JobInfo

| 参数名                | 数据类型  | 描述                 |
| --------------------- | --------- | -------------------- |
| id                    | Long      | 唯一主键             |
| jobCron               | String    | cron表达式           |
| jobDesc               | String    | job描述              |
| author                | String    | 作者                 |
| jobType               | String    | 任务类型             |
| configId              | Long      | jobConfig的id        |
| executeBlockStrategy  | String    | 阻塞策略             |
| executeTimeout        | Long      | 调度超时             |
| executeFailRetryCount | Long      | 重试次数             |
| childJobId            | String    | 子任务id             |
| jobStatus             | Integer   | job状态，0停止 1运行 |
| triggerLastTime       | Long      | 最后一次触发时间     |
| triggerNextTime       | Long      | 下一次触发时间       |
| updateTime            | Timestamp | 变更时间             |
| createTime            | Timestamp | 创建时间             |
| jobConfig             | JobConfig | job配置              |

JobConfig

| 参数名         | 数据类型 | 描述         |
| -------------- | -------- | ------------ |
| id             | Long     | 唯一主键     |
| executeService | String   | 服务名       |
| executeMethod  | String   | 服务方法     |
| executeParam   | String   | 服务参数     |
| serviceType    | String   | 服务请求类型 |

## 新增job
### 请求方式
POST application/json
### 请求地址
tiny-job/jobinfo/add
### 请求参数
| 参数名                | 数据类型  | 描述                            |
| --------------------- | --------- | ------------------------------- |
| jobCron               | String    | cron表达式                      |
| jobDesc               | String    | job描述                         |
| jobType               | String    | 任务类型                        |
| configId              | Long      | jobConfig的id                   |
| executeBlockStrategy  | String    | 阻塞策略（未完成）              |
| executeTimeout        | Long      | 调度超时（未完成）              |
| executeFailRetryCount | Long      | 重试次数（未完成）              |
| childJobId            | String    | 子任务id（未完成）              |
| jobStatus             | Integer   | job状态，0停止 1运行（默认为0） |
| jobConfig             | JobConfig | job配置                         |

JobConfig

| 参数名         | 数据类型 | 描述     |
| -------------- | -------- | -------- |
| executeService | String   | 服务名   |
| executeMethod  | String   | 服务方法 |
| executeParam   | String   | 服务参数 |

## 修改job
### 请求方式
POST application/json
### 请求地址
tiny-job/jobinfo/update
### 请求参数
| 参数名                | 数据类型  | 描述                            |
| --------------------- | --------- | ------------------------------- |
| id                    | Long      | 要修改任务的主键                |
| jobCron               | String    | cron表达式                      |
| jobDesc               | String    | job描述                         |
| jobType               | String    | 任务类型                        |
| configId              | Long      | jobConfig的id                   |
| executeBlockStrategy  | String    | 阻塞策略（未完成）              |
| executeTimeout        | Long      | 调度超时（未完成）              |
| executeFailRetryCount | Long      | 重试次数（未完成）              |
| childJobId            | String    | 子任务id（未完成）              |
| jobStatus             | Integer   | job状态，0停止 1运行（默认为0） |
| jobConfig             | JobConfig | job配置                         |

JobConfig

| 参数名         | 数据类型 | 描述     |
| -------------- | -------- | -------- |
| executeService | String   | 服务名   |
| executeMethod  | String   | 服务方法 |
| executeParam   | String   | 服务参数 |

## 删除job
### 请求方式
GET ### 请求地址
tiny-job/jobinfo/update
### 请求参数
| 参数名 | 数据类型 | 描述         |
| ------ | -------- | ------------ |
| id     | Long     | 要删除的主键 |

## 暂停job
### 请求方式
POST application/json
### 请求地址
tiny-job/jobinfo/pause
### 请求参数
| 参数名 | 数据类型 | 描述           |
| ------ | -------- | -------------- |
| id     | Long     | 要暂停的任务ID |
### 返回参数
沿用通用返回格式，`code` 为 0 表示暂停成功。

## 恢复job
### 请求方式
POST application/json
### 请求地址
tiny-job/jobinfo/resume
### 请求参数
| 参数名 | 数据类型 | 描述           |
| ------ | -------- | -------------- |
| id     | Long     | 要恢复的任务ID |
### 返回参数
沿用通用返回格式，`code` 为 0 表示恢复成功。
