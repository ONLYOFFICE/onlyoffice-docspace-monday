/**
 *
 * (c) Copyright Ascensio System SIA 2025
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

import fs from "node:fs/promises";
import browserslist from "browserslist";
import * as esbuild from "esbuild";
import * as lightning from "lightningcss";
import { exec } from "node:child_process";
import { promisify } from "util";

const execAsync = promisify(exec);

async function main() {
  await fs.mkdir("dist", { recursive: true });
  await compileTailwind();
  await css();
}

async function compileTailwind() {
  await execAsync('tailwindcss -i ./styles/styles.css -o dist/main.css');
}

async function css() {
  const l = browserslist("> 0.2%");
  const t = lightning.browserslistToTargets(l);
  const r = await lightning.bundleAsync({
    filename: "dist/main.css",
    minify: true,
    targets: t,
  });
  const c = String(r.code);
  await fs.writeFile("../resources/static/main.css", c);
}

await main();
