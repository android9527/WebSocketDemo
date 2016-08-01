#!/bin/bash

chmod a+x packageJar.sh

#delete the unused files
rm -rf build/intermediates/classes/release/com/qfpay/pushsdk/R\$*
rm build/intermediates/classes/release/com/qfpay/pushsdk/BuildConfig.class
rm build/intermediates/classes/release/com/qfpay/pushsdk/R.class
echo "文件清理完毕..."

#jar
jar cvf $1 -C build/intermediates/classes/release .
echo "打包jar完毕..."

#proguard jar
/Users/qfpay/Documents/Android/proguard5.2.1/bin/proguard.sh @proguard-rules.pro -injars $1 -outjars $2
echo "混淆完毕"

#copy to current files
mkdir release
cp $2 release/
rm $1

