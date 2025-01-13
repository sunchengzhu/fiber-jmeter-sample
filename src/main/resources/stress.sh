set -x
mvn jmeter:configure
basedir=`pwd`
for file in ${basedir}/target/*/jmeter
do
  cp ${basedir}/target/fiber-jmeter-sample-1.0-SNAPSHOT-jar-with-dependencies.jar ${file}/lib/ext
  cp -r ${basedir}/src/test/jmeter/* ${file}/bin
#  [ -f "${file}/lib/bcprov-jdk15on-1.49.jar" ] && rm "${file}/lib/bcprov-jdk15on-1.49.jar"
done