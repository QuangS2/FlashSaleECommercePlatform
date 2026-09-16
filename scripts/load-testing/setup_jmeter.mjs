import fs from 'fs';
import path from 'path';
import https from 'https';
import { execSync } from 'child_process';

const targetDir = path.resolve('src/scripts/load-testing/apache-jmeter-5.6.3');
const zipPath = path.resolve('src/scripts/load-testing/jmeter.zip');
const destExtract = path.resolve('src/scripts/load-testing');

if (fs.existsSync(path.join(targetDir, 'bin/jmeter.bat'))) {
  console.log('✅ Apache JMeter 5.6.3 đã có sẵn tại:', targetDir);
  process.exit(0);
}

console.log('⏳ Đang tải Apache JMeter 5.6.3 từ CDN...');
const url = 'https://dlcdn.apache.org//jmeter/binaries/apache-jmeter-5.6.3.zip';

execSync(`curl.exe -L -o "${zipPath}" "${url}"`, { stdio: 'inherit' });
console.log('📦 Đang giải nén Apache JMeter...');
execSync(`powershell -Command "Expand-Archive -Path '${zipPath}' -DestinationPath '${destExtract}' -Force"`, { stdio: 'inherit' });

if (fs.existsSync(zipPath)) {
  fs.unlinkSync(zipPath);
}

console.log('🎉 Cài đặt Apache JMeter 5.6.3 thành công tại:', targetDir);
