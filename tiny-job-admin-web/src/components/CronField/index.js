import React from 'react';
import { Select, TimePicker, Input, Row, Col } from 'antd';
import moment from 'moment';

const Option = Select.Option;

const PRESET_KEYS = {
  EVERY_MINUTE: 'EVERY_MINUTE',
  EVERY_FIVE_MINUTES: 'EVERY_FIVE_MINUTES',
  HOURLY: 'HOURLY',
  DAILY: 'DAILY',
  WEEKLY: 'WEEKLY',
  CUSTOM: 'CUSTOM',
};

const WEEKDAY_OPTIONS = [
  { key: 'MON', label: '周一' },
  { key: 'TUE', label: '周二' },
  { key: 'WED', label: '周三' },
  { key: 'THU', label: '周四' },
  { key: 'FRI', label: '周五' },
  { key: 'SAT', label: '周六' },
  { key: 'SUN', label: '周日' },
];

const DEFAULT_TIME = moment('00:00', 'HH:mm');

function buildCronExpression(mode, timeMoment, weekday) {
  const minute = timeMoment.minute();
  const hour = timeMoment.hour();
  switch (mode) {
    case PRESET_KEYS.EVERY_MINUTE:
      return '0 0/1 * * * ?';
    case PRESET_KEYS.EVERY_FIVE_MINUTES:
      return '0 0/5 * * * ?';
    case PRESET_KEYS.HOURLY:
      return '0 0 * * * ?';
    case PRESET_KEYS.DAILY:
      return `0 ${minute} ${hour} * * ?`;
    case PRESET_KEYS.WEEKLY:
      return `0 ${minute} ${hour} ? * ${weekday}`;
    default:
      return '';
  }
}

function parseCronExpression(value) {
  if (!value) {
    return {
      mode: PRESET_KEYS.CUSTOM,
      time: DEFAULT_TIME,
      weekday: WEEKDAY_OPTIONS[0].key,
      currentValue: '',
    };
  }

  if (value === '0 0/1 * * * ?') {
    return {
      mode: PRESET_KEYS.EVERY_MINUTE,
      time: DEFAULT_TIME,
      weekday: WEEKDAY_OPTIONS[0].key,
      currentValue: value,
    };
  }

  if (value === '0 0/5 * * * ?') {
    return {
      mode: PRESET_KEYS.EVERY_FIVE_MINUTES,
      time: DEFAULT_TIME,
      weekday: WEEKDAY_OPTIONS[0].key,
      currentValue: value,
    };
  }

  if (value === '0 0 * * * ?') {
    return {
      mode: PRESET_KEYS.HOURLY,
      time: DEFAULT_TIME,
      weekday: WEEKDAY_OPTIONS[0].key,
      currentValue: value,
    };
  }

  const dailyMatch = /^0\s+(\d{1,2})\s+(\d{1,2})\s+\*\s+\*\s+\?$/.exec(value);
  if (dailyMatch) {
    const minute = parseInt(dailyMatch[1], 10);
    const hour = parseInt(dailyMatch[2], 10);
    return {
      mode: PRESET_KEYS.DAILY,
      time: moment({ hour, minute }),
      weekday: WEEKDAY_OPTIONS[0].key,
      currentValue: value,
    };
  }

  const weeklyMatch = /^0\s+(\d{1,2})\s+(\d{1,2})\s+\?\s+\*\s+([A-Z]{3})$/.exec(value);
  if (weeklyMatch) {
    const minute = parseInt(weeklyMatch[1], 10);
    const hour = parseInt(weeklyMatch[2], 10);
    const weekday = WEEKDAY_OPTIONS.find(item => item.key === weeklyMatch[3]) ?
      weeklyMatch[3] : WEEKDAY_OPTIONS[0].key;
    return {
      mode: PRESET_KEYS.WEEKLY,
      time: moment({ hour, minute }),
      weekday,
      currentValue: value,
    };
  }

  return {
    mode: PRESET_KEYS.CUSTOM,
    time: DEFAULT_TIME,
    weekday: WEEKDAY_OPTIONS[0].key,
    currentValue: value,
  };
}

class CronField extends React.PureComponent {
  constructor(props) {
    super(props);
    const parsed = parseCronExpression(props.value);
    this.state = {
      mode: parsed.mode,
      time: parsed.time,
      weekday: parsed.weekday,
      currentValue: parsed.currentValue,
    };
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.value !== this.props.value) {
      const parsed = parseCronExpression(nextProps.value);
      this.setState({
        mode: parsed.mode,
        time: parsed.time,
        weekday: parsed.weekday,
        currentValue: parsed.currentValue,
      });
    }
  }

  triggerChange = (value) => {
    if (this.props.onChange) {
      this.props.onChange(value);
    }
  };

  handleModeChange = (mode) => {
    if (mode === PRESET_KEYS.CUSTOM) {
      this.setState({ mode, currentValue: this.state.currentValue });
      this.triggerChange(this.state.currentValue);
      return;
    }

    const time = this.state.time || DEFAULT_TIME;
    const weekday = this.state.weekday || WEEKDAY_OPTIONS[0].key;
    const cron = buildCronExpression(mode, time, weekday);
    this.setState({
      mode,
      time,
      weekday,
      currentValue: cron,
    });
    this.triggerChange(cron);
  };

  handleTimeChange = (time) => {
    const newTime = time || DEFAULT_TIME;
    const cron = buildCronExpression(this.state.mode, newTime, this.state.weekday);
    this.setState({
      time: newTime,
      currentValue: cron,
    });
    this.triggerChange(cron);
  };

  handleWeekdayChange = (weekday) => {
    const cron = buildCronExpression(this.state.mode, this.state.time || DEFAULT_TIME, weekday);
    this.setState({
      weekday,
      currentValue: cron,
    });
    this.triggerChange(cron);
  };

  handleCustomChange = (event) => {
    const value = event.target.value;
    this.setState({
      mode: PRESET_KEYS.CUSTOM,
      currentValue: value,
    });
    this.triggerChange(value);
  };

  renderPresetControls() {
    const { mode, time, weekday } = this.state;
    const disabled = this.props.disabled;

    if (mode === PRESET_KEYS.DAILY) {
      return (
        <Col span={12}>
          <TimePicker
            style={{ width: '100%' }}
            value={time}
            format="HH:mm"
            minuteStep={1}
            onChange={this.handleTimeChange}
            disabled={disabled}
          />
        </Col>
      );
    }

    if (mode === PRESET_KEYS.WEEKLY) {
      return [
        (
          <Col span={6} key="weekday">
            <Select
              style={{ width: '100%' }}
              value={weekday}
              onChange={this.handleWeekdayChange}
              disabled={disabled}
            >
              {WEEKDAY_OPTIONS.map(option => (
                <Option key={option.key} value={option.key}>{option.label}</Option>
              ))}
            </Select>
          </Col>
        ),
        (
          <Col span={6} key="time">
            <TimePicker
              style={{ width: '100%' }}
              value={time}
              format="HH:mm"
              minuteStep={1}
              onChange={this.handleTimeChange}
              disabled={disabled}
            />
          </Col>
        ),
      ];
    }

    return null;
  }

  render() {
    const { mode, currentValue } = this.state;
    const disabled = this.props.disabled;

    return (
      <div>
        <Row gutter={8}>
          <Col span={12}>
            <Select
              style={{ width: '100%' }}
              value={mode}
              onChange={this.handleModeChange}
              disabled={disabled}
            >
              <Option value={PRESET_KEYS.EVERY_MINUTE}>每分钟</Option>
              <Option value={PRESET_KEYS.EVERY_FIVE_MINUTES}>每5分钟</Option>
              <Option value={PRESET_KEYS.HOURLY}>每小时</Option>
              <Option value={PRESET_KEYS.DAILY}>每天</Option>
              <Option value={PRESET_KEYS.WEEKLY}>每周</Option>
              <Option value={PRESET_KEYS.CUSTOM}>自定义</Option>
            </Select>
          </Col>
          {this.renderPresetControls()}
        </Row>
        <Row style={{ marginTop: 8 }}>
          <Col span={24}>
            <Input
              value={currentValue}
              onChange={this.handleCustomChange}
              placeholder={this.props.placeholder || '请输入cron表达式'}
              disabled={disabled}
            />
          </Col>
        </Row>
      </div>
    );
  }
}

export default CronField;
