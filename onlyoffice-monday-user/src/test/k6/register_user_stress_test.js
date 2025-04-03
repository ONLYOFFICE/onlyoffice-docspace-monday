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
import { SharedArray } from 'k6/data';

export let failedRegistrations = new Counter('failed_registrations');

const users = new SharedArray('users', function () {
    const data = [];
    for (let i = 1; i <= 100000; i++) {
        data.push({
            tenant_id: 123,
            monday_id: i,
            docspace_id: `docspace-${i}-${Math.random().toString(36).substring(2, 15)}`,
            email: `user${i}@example.com`,
            hash: `hash-${i}-${Math.random().toString(36).substring(2, 15)}`,
        });
    }
    return data;
});

export let options = {
    stages: [
        { duration: '1m', target: 25 },
        { duration: '2m', target: 25 },
        { duration: '1m', target: 50 },
        { duration: '2m', target: 50 },
        { duration: '1m', target: 100 },
        { duration: '2m', target: 100 },
        { duration: '1m', target: 200 },
        { duration: '2m', target: 200 },
        { duration: '1m', target: 0 },
    ],
    thresholds: {
        'failed_registrations': ['rate<0.1'],
        'http_req_duration': ['p(95)<500'],
    },
};

export default function () {
    const user = users[Math.floor(Math.random() * users.length)];
    const url = `http://host.docker.internal:8080/users`;
    const payload = JSON.stringify({
        tenant_id: user.tenant_id,
        monday_id: user.monday_id,
        docspace_id: user.docspace_id,
        email: user.email,
        hash: user.hash,
    });
    const params = {
        headers: {
            'Content-Type': 'application/json',
            'X-Timeout': '3500',
        },
    };

    let res = http.post(url, payload, params);
    let isSuccess = check(res, {
        'status is 201 or 429': (r) => r.status === 201 || r.status === 429,
        'duration was <= 500ms': (r) => r.timings.duration <= 500,
    });

    if (!isSuccess) {
        failedRegistrations.add(1);
    }

    sleep(1);
}
