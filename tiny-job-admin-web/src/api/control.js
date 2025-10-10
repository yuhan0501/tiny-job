import superagent from 'superagent';
import globalConfig from '../config';

const buildUrl = (path) => `${globalConfig.getAPIPath()}${path}`;

const request = (method, path, payload) => {
  const url = buildUrl(path);
  const req = superagent(method, url);
  if (globalConfig.isCrossDomain()) {
    req.withCredentials();
  }
  req.set('Accept', 'application/json');
  if (payload) {
    req.send(payload);
  }
  return req.then(res => res.body);
};

export const fetchPauseStatus = () => request('GET', '/control/pause');
export const pauseAll = () => request('POST', '/control/pause');
export const resumeAll = () => request('DELETE', '/control/pause');

export const fetchRuntimeStats = () => request('GET', '/jobinfo/stats/runtime');
