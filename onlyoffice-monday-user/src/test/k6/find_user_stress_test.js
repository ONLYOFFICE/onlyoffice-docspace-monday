/**
 *
 * (c) Copyright Ascensio System SIA 2024
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

export let failedRequests = new Counter('failed_requests');
export let options = {
    stages: [
        { duration: '1m', target: 100 },
        { duration: '1m', target: 100 },
        { duration: '1m', target: 200 },
        { duration: '1m', target: 200 },
        { duration: '1m', target: 250 },
        { duration: '1m', target: 250 },
        { duration: '1m', target: 0 },
    ],
    thresholds: {
        'failed_requests': ['rate<0.1'],
        'http_req_duration': ['p(95)<600'],
    },
};
export default function () {
    const tenantId = 123;
    const mondayId = Math.floor(Math.random() * 100000) + 1;
    const url = `http://host.docker.internal:8080/users/${tenantId}/${mondayId}`;
    const headers = {
        'Content-Type': 'application/json',
        'X-Timeout': '3500',
    };

    let res = http.get(url, { headers: headers });
    let isSuccess = check(res, {
        'status is 200 or 429': (r) => r.status === 200 || r.status === 429,
        'duration was <= 500ms': (r) => r.timings.duration <= 500,
    });

    if (!isSuccess) {
        failedRequests.add(1);
    }

    sleep(1);
}
