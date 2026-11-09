REM combine username and password (on maven) : https://mixedanalytics.com/tools/basic-authentication-generator/
rem call ./gradlew jreleaserConfig
call ./gradlew jreleaserConfig
rem call ./gradlew clean
call ./gradlew clean
rem call ./gradlew -x test publish
call ./gradlew -x test publish
rem call ./gradlew -x test jreleaserFullRelease
call ./gradlew -x test jreleaserFullRelease
REM call 7z.exe a "./build/staging-deploy/one.zip" "./build/staging-deploy/one/"
REM cmd curl.exe --request POST  --verbose --header 'Authorization: Bearer S0JkMUVvaEU6RldzNmkxSVMzYXZ3cStSeXZlcFlTQkFZbDB5eHVDem1GRGtMdTAzNVJZc2k=' --form bundle=@one.zip https://central.sonatype.com/api/v1/publisher/upload
