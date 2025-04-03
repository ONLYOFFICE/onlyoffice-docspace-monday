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

function webSocketConfiguration() {
    return {
      isConnected: false,
      async getSessionToken() {
        try {
          const { data: sessionToken } = await mondaySdk().get("sessionToken");
          return sessionToken;
        } catch (error) {
          return null;
        }
      },
      async updateWebSocketConnection(sessionToken) {
        if (!sessionToken) return;
        const wsElement = this.$el.closest('[hx-ext]');
        if (wsElement) {
          wsElement.setAttribute("ws-connect", `/notifications?sessionToken=${sessionToken}`);
          htmx.process(wsElement);
        }
      },
      async init() {
        const sessionToken = await this.getSessionToken();
        if (sessionToken) {
          this.isConnected = true;
          await this.updateWebSocketConnection(sessionToken);
        }
        htmx.on('htmx:wsClose', async () => {
          this.isConnected = false;
          const newToken = await this.getSessionToken();
          if (newToken) {
            this.isConnected = true;
            await this.updateWebSocketConnection(newToken);
          }
        });
        htmx.on('htmx:wsOpen', () => {
          this.isConnected = true;
        });
      }
    }
  }